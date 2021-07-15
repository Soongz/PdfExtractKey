package utils.script;

import io.netty.util.internal.StringUtil;
import ocr.iflytek.WebOCR;
import org.apache.commons.lang.StringUtils;
import utils.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;


/**
 * Description: 批量抽取关键词
 * steps:
 * 1. 遍历文件, 读取文件内容
 * 2. 调用关键词提取类 拼接关键词top20  keyWords = "k0 k1 k2 k3 k4... k19"
 * 3. 拼接sql update tb_document set key_words = #{keyWords} where ess_key = #{fileName}
 *
 * @author Soong
 */
public class BatchExtractDate {

    private final ThreadPoolExecutor threadPool;
    private final ThreadPoolExecutor retryThreadPool;
    private final StringBuffer sqlScript = new StringBuffer();
    private final static String prefix = "UPDATE tb_document SET key_words = \"";
    private final static String middle = "\" WHERE ess_key = \"";
    private final static String DOCUMENT_PATH = "D:\\data\\docShop\\622batchUpload\\abstract_test\\";
    private final static String RESULT_PATH_PREFIX = "D:\\tmp\\clearAfterUsed\\702\\";

    private final static Integer TIMEOUT_THRESHOLD = 30;
    private final LongAdder counter = new LongAdder();

    private final ConcurrentLinkedQueue<String> retryList;

    private final ConcurrentLinkedQueue<String> errorList;


    public BatchExtractDate() {
        this.threadPool = new ThreadPoolExecutor(1, 1, 1000, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<>(100000));
        this.retryThreadPool = new ThreadPoolExecutor(5, 5, 1000, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<>(100000));
        retryList = new ConcurrentLinkedQueue<>();
        errorList = new ConcurrentLinkedQueue<>();
    }

    public static void main(String[] args) {
        BatchExtractDate batchExtractKeyWords = new BatchExtractDate();

        System.out.println("EXECUTE...");
//        batchExtractKeyWords.execute(DOCUMENT_PATH);
        batchExtractKeyWords.executeWithTitle();
    }

    private void executeWithTitle() {
        HashMap<String, String> titles = TextReader.getTitlesAndEssKey("D:\\tmp\\clearAfterUsed\\628\\20210628-1607.csv");
        for (Map.Entry<String, String> entry : titles.entrySet()) {
            threadPool.execute(new TitleWorker(entry.getKey(), entry.getValue()));
        }
        threadPool.shutdown();

        while (true) {
            if (threadPool.isTerminated() || titles.size() == counter.intValue()) {
                System.out.println("flush to dish first time");
                flushStringTodisk(sqlScript.toString(), RESULT_PATH_PREFIX + "keys.csv");
                flushStringTodisk(errorList.toString(), RESULT_PATH_PREFIX + "errorList.txt");
                break;
            }
            try {
                System.out.println("ready to flush temp result into disk...");
                flushStringTodisk(sqlScript.toString(), RESULT_PATH_PREFIX + "keys_log\\keys" + counter.intValue() + ".csv");
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void execute(String path) {
        File direct = new File(path);
        List<File> files;
        final File[] allFile = direct.listFiles();
        if (allFile == null) return;
        files = Arrays.asList(allFile);
        for (File file : files) {
            if (file.isDirectory()) continue;
            threadPool.execute(new Worker(file, false));
        }
        threadPool.shutdown();

        while (true) {
            if (threadPool.isTerminated() || files.size() == counter.intValue()) {
                System.out.println("flush to dish first time");
                flushStringTodisk(sqlScript.toString(), RESULT_PATH_PREFIX + "keys.csv");
                break;
            }
            try {
                System.out.println("ready to flush temp result into disk...");
                flushStringTodisk(sqlScript.toString(), RESULT_PATH_PREFIX + "keys_log\\keys" + counter.intValue() + ".csv");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    //根据标题提取日期
    class TitleWorker extends Thread {
        private final String essKey;
        private final String title;
        private static final String PATH_PREFIX_1 = "D:\\data\\docShop\\518All\\";
        private static final String PATH_PREFIX_2 = "D:\\data\\docShop\\618All\\";

        private final ExecutorService executorService;

        public TitleWorker(String essKey, String title) {
            this.essKey = essKey;
            this.title = title;
            executorService = Executors.newFixedThreadPool(1);
        }

        @Override
        public void run() {
            try {
                counter.increment();
                System.out.println(Thread.currentThread().getName() + "正在执行第" + counter.intValue() + "个任务");
                final DateMatcherResult matching = DateMatcher.matching(title);
                LinkedHashSet<String> dateSet = matching.getResult();
                String dateString;
                String type;
                if (dateSet!=null && dateSet.size() != 0) {
                    type = "来自标题";
                    dateString = StringUtils.join(dateSet.toArray(), ",");
                } else {
                    final Future<DateMatcherResult> future = executorService.submit(() -> getDates(essKey));
                    DateMatcherResult dateMatcherResult = future.get(TIMEOUT_THRESHOLD, TimeUnit.SECONDS);
                    type = dateMatcherResult.getType();
                    dateString = StringUtils.join(dateMatcherResult.getResult(), ",");
                    executorService.shutdown();
                }
                synchronized (sqlScript) {
                    sqlScript.append(title).append(",").append(essKey).append(",").append(type).append(",").append(dateString == null ? "" : dateString).append("\n");
                }
            } catch (TimeoutException e) {
                e.printStackTrace();
                System.out.println("超时跳过");
                errorList.offer(title);
            } catch (Exception e) {
                e.printStackTrace();
                errorList.offer(title);
                System.out.println("未知异常 跳过");
            }
        }

        private DateMatcherResult getDates(String fileName) throws Exception {
            String fileName2 = fileName.replaceAll("\"", "");
            String path1 = PATH_PREFIX_1 + fileName2;
            File file = null;

            file = new File(path1);
            if (!file.exists()) {
                file = new File(PATH_PREFIX_2 + fileName2);
            }
            if (file.exists()) {
                return getDatesFromContent(file);
            }
            return null;
        }

        private DateMatcherResult getDatesFromContent(File file) throws Exception {
            if (file == null) return null;
            String content = fileExtractStringCache(file.getPath(), file.getName());
//            Set<String> dateSet = DateMatcher.matching(content).getResult();
//            return StringUtils.join(dateSet.toArray(), ",");
            return DateMatcher.matching(content);
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
                        System.out.println(Thread.currentThread().getName() + "正在执行第" + counter.intValue() + "个任务");
                        String content = fileExtractStringCache(file.getPath(), file.getName());
                        Set<String> date = DateMatcher.matching(content).getResult();
                        if (date == null) return;
                        System.out.println(file.getName() + ":" + date.toString());
                        sqlScript.append(file.getName()).append(",").append(date.toString()).append("\n");
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
//                PDF2pngUtil.pdf2png(filePath);
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
                            System.out.println("a OCR error, skip and continue to append next page content...");
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
