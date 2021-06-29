package utils.script;

import utils.Downloader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * Description: tess4j
 *
 * @author Soong
 */
public class BatchDownload {

    private final ConcurrentLinkedQueue<String> urlQueue = new ConcurrentLinkedQueue<>();

    private final ThreadPoolExecutor poolExecutor;

    private final ConcurrentLinkedQueue<String> retryQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<String> errorQueue = new ConcurrentLinkedQueue<>();

    private final LongAdder counter = new LongAdder();


    public BatchDownload() {
        this.poolExecutor = new ThreadPoolExecutor(5, 5, 1000, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<>(50000));
    }

    public ConcurrentLinkedQueue<String> getRetryQueue() {
        return retryQueue;
    }

    public static void main(String[] args) throws Exception {
        BatchDownload batchDownload = new BatchDownload();
        batchDownload.execute();
//        System.out.println(batchKeywordExtraction.getErrorQueue());
    }

    public void execute() throws Exception {
        readUrl(new File("D:\\tmp\\clearAfterUsed\\629_2\\629downloadUtls.txt"));
        System.out.println("准备下载. size:" + urlQueue.size());
        download(urlQueue, false);

//        Thread.currentThread().join();
//        System.out.println("开始重试. retryQueue size: " + retryQueue.size());
//        download(retryQueue, true);
//
//        Thread.currentThread().join();
//        System.out.println("重试完毕，errorQueue Size: " + errorQueue.size());
//        System.out.println(errorQueue);
    }

    private void download(ConcurrentLinkedQueue<String> queue, boolean isRetry) {
        String url;
        final int jobCount = queue.size();
        while ((url = queue.poll()) != null) {
            final DownloadThread downloadThread = new DownloadThread(url, isRetry);
            poolExecutor.execute(downloadThread);
        }

        while(true) {
            if (poolExecutor.isTerminated() || counter.intValue() == jobCount) {
                System.out.println("首次运行结束...打印下载失败url");
                System.out.println(retryQueue);
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void readUrl(File file) {
        try (FileInputStream inputStream = new FileInputStream(file);
             InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(streamReader)
        ) {
            String lineTxt;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                urlQueue.offer(lineTxt);
            }
        } catch (Exception e) {
            System.out.println("read异常");
            e.printStackTrace();
        }
    }

    class DownloadThread extends Thread {
        private final String url;
        private static final String DOWNLOAD_PATH = "D:\\data\\docShop\\618All\\";
        private final boolean isRetry;

        public DownloadThread(String url, boolean isRetry) {
            this.url = url;
            this.isRetry = isRetry;
        }

        @Override
        public void run() {
            try {
                counter.increment();
                System.out.println("正在执行第" + counter.intValue() + "个任务");

                Downloader.downLoadFromUrl(url, spiltFileNameFromUrl(url), DOWNLOAD_PATH);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("下载失败: " + url + ", 即将重试");
                if (isRetry) {
                    errorQueue.offer(url);
                } else {
                    retryQueue.offer(url);

                }
            }
        }

        private String spiltFileNameFromUrl(String url) {
            final String[] split = url.split("/");
            return split[split.length - 1];
        }
    }
}
