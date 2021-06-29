package utils.script;

import io.netty.util.internal.StringUtil;
import ocr.iflytek.WebOCR;
import textrank.KeySentenceExtraction;
import textrank.KeywordsExtraction;
import utils.DocumentExtractText;
import utils.ItextpdfUtil;
import utils.PDF2pngUtil;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;


/**
 * Description: 批量抽取关键句
 *
 * @author Soong
 */
public class BatchExtractKeySentences {

    private final ThreadPoolExecutor threadPool;
    private final ThreadPoolExecutor retryThreadPool;
    private final StringBuffer sqlScript = new StringBuffer();
    private final static String prefix = "UPDATE tb_document SET key_words = \"";
    private final static String middle = "\" WHERE ess_key = \"";
    private final static String DOCUMENT_PATH = "D:\\data\\docShop\\622batchUpload\\abstract_test\\";
    private final static String RESULT_PATH_PREFIX = "D:\\tmp\\clearAfterUsed\\623\\";

    private final static Integer TIMEOUT_THRESHOLD = 60 * 60;
    private final LongAdder counter = new LongAdder();

    private final ConcurrentLinkedQueue<String> retryList;

    private final ConcurrentLinkedQueue<String> errorList;

    public BatchExtractKeySentences() {
        this.threadPool = new ThreadPoolExecutor(5, 5, 1000, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<>(50000));
        this.retryThreadPool = new ThreadPoolExecutor(5, 5, 1000, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<>(50000));
        retryList = new ConcurrentLinkedQueue<>();
        errorList = new ConcurrentLinkedQueue<>();
    }

    public static void main(String[] args) {
        BatchExtractKeySentences batchExtractKeyWords = new BatchExtractKeySentences();
        try {
            System.out.println("YOU GOT ONLY 10 SECONDS TO RELEASE THIS PC, GOOD LUCK...");
            Thread.sleep(10 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("EXECUTE...");
        batchExtractKeyWords.execute(DOCUMENT_PATH);
    }

    private void execute(String path) {
        File direct = new File(path);
//        for (File file : Objects.requireNonNull(direct.listFiles())) {
        List<File> files;
        final File[] allFile = direct.listFiles();
        if (allFile == null) return;
        files = Arrays.asList(allFile);
//        files = TextReader.readFor(new File(RESULT_PATH_PREFIX+"error_retry.txt"));
        for (File file : files) {
            if (file.isDirectory()) continue;
            threadPool.execute(new Worker(file, false));
        }
        threadPool.shutdown();

        while (true) {
            if (threadPool.isTerminated() || files.size() == counter.intValue()) {
                System.out.println("flush to dish first time");
                flushStringTodisk(sqlScript.toString(), RESULT_PATH_PREFIX + "keys.csv");
                System.out.println("第一遍结束，重试队列启动..." + retryList.size());


                int retryListSize = retryList.size();
                String filePath;
                while ((filePath = retryList.poll()) != null) {
                    retryThreadPool.execute(new Worker(new File(filePath), true));
                }
                retryThreadPool.shutdown();
                while (!retryThreadPool.isTerminated() || (files.size() + retryListSize) == counter.intValue()) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                flushStringTodisk(sqlScript.toString(), RESULT_PATH_PREFIX + "keys_after_retry.csv");

                System.out.println("重试队列完毕...查看失败队列 " + errorList.size());

                System.out.println(errorList);
                flushStringTodisk(errorList.toString(), RESULT_PATH_PREFIX + "errorList.txt");

                break;
            }
            try {
                System.out.println("ready to flush temp result into disk...");
                flushStringTodisk(sqlScript.toString(), RESULT_PATH_PREFIX + "keys_log\\keys" + counter.intValue() + ".csv");
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 加油打工人
     */
    class Worker extends Thread {
        private final File file;
        private final Boolean isRetry;

        private final ExecutorService executorService;

        public Worker(File file, Boolean isRetry) {
            this.file = file;
            this.isRetry = isRetry;
            executorService = Executors.newFixedThreadPool(1);
        }

        @Override
        public void run() {
            try {
                final Future<?> future = executorService.submit(() -> {
                    try {
                        counter.increment();
                        System.out.println("正在执行第" + counter.intValue() + "个任务");
                        String content = fileExtractStringCache(file.getPath(), file.getName());
                        if (content != null) {
                            content = content.trim().replace(" ", "");
                        }
                        final List<String> topNSentences = KeySentenceExtraction.getTopNSentences(content, 5);

                        sqlScript.append(file.getName());
                        if (topNSentences != null && topNSentences.size() > 0) {
                            topNSentences.forEach(e -> {
                                sqlScript.append(",").append(e);
                            });
                        }
                        sqlScript.append("\n");

                    } catch (Exception e) {
                        e.printStackTrace();
                        if (isRetry) {
                            errorList.offer(file.getPath());
                        } else {
                            retryList.offer(file.getPath());
                        }
                    }
                });
                future.get(TIMEOUT_THRESHOLD, TimeUnit.SECONDS);

                executorService.shutdown();
            } catch (TimeoutException e) {
                e.printStackTrace();
                errorList.offer(file.getPath());
                System.out.println("运行超时 快速跳过");
            } catch (Exception e) {
                e.printStackTrace();
                errorList.offer(file.getPath());
                System.out.println("未知异常 跳过");
            }
        }
    }


    public static String fileExtractStringCache(String filePath, String fileName) throws IOException {
        String result = null;
        if (filePath.contains(".doc")) {
            result = DocumentExtractText.extractText(filePath);
            System.out.println("fileExtractString: " + result);
        } else if (filePath.contains(".pdf")) {
            //step1 直接从pdf读文字
            result = ItextpdfUtil.getPdfContent(filePath);
            if ("".equals(result)) {
                //如果之前解析过，就不用再次走OCR了 直接读txt
                result = extractStringFromTxt(filePath + "dir\\" + fileName + ".txt");
                if (!StringUtil.isNullOrEmpty(result)) {
                    System.out.println("working with text....");
                    return result;
                }
                //由图片组成的pdf， 先转图片
                PDF2pngUtil.pdf2png(filePath);
                StringBuilder partialResult = new StringBuilder();
                File root = new File(filePath + "dir");
                //遍历图片所在的路径，依次调用ocr接口，并将结果拼接
                if (root.isDirectory()) {
                    File[] files = root.listFiles();
                    assert files != null;
                    for (File file : files) {
                        try {
//                            Thread.sleep(100);
                            partialResult.append(WebOCR.execute(file.getPath())); //科大讯飞OCR
//                            partialResult.append(AliOCR.execute(file.getPath())); //阿里OCR
                        } catch (Exception e) {
                            System.out.println("counting error, skip...");
                        }
                    }
                }
                result = partialResult.toString();
                flushStringTodisk(result, filePath + "dir\\" + fileName + ".txt");
            }
        } else if (filePath.contains(".png") || filePath.contains(".jpg")) {
            //图片类型直接调用ocr
            result = WebOCR.execute(filePath);
        }

        if (!StringUtil.isNullOrEmpty(result)) {
            result = result.replace("\n", " ");
            result = result.replaceAll("([A-Za-z-]) +([A-Za-z-])", "$1@$2");
            result = result.replaceAll("\\s+", "").replaceAll("@", " ");
        }

//        System.out.println("fileExtractString: " + result);
        return result;
    }

    public static String extractStringFromTxt(String filePath) {
        File file = new File(filePath);
        StringBuilder result = new StringBuilder();
        try (FileInputStream inputStream = new FileInputStream(file);
             InputStreamReader read = new InputStreamReader(inputStream)) {

            if (file.isFile() && file.exists()) {
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    result.append(lineTxt);
                }
                read.close();
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            return null;
        }
        return result.toString();
    }

    public static void flushStringTodisk(String input, String targetFileName) {
        File file = new File(targetFileName);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(input);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
