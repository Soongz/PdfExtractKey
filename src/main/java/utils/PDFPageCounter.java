package utils;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description: introWebMagic
 *
 * @author Soong
 */
public class PDFPageCounter {

    private AtomicInteger totalPages = new AtomicInteger(0);
    public ThreadPoolExecutor threadPoolExecutor;

    public PDFPageCounter() {
        threadPoolExecutor =
                new ThreadPoolExecutor(4,
                        4,
                        1000,
                        TimeUnit.MICROSECONDS,
                        new ArrayBlockingQueue<Runnable>(10000));
    }


    public void counter(String filename) {

        if (!filename.contains(".pdf")) {
            return;
        }
        File file = new File(filename);
        try (PDDocument doc = PDDocument.load(file)) {
            int pageCount = doc.getNumberOfPages();
            System.out.println("文件：" + file.getPath() + " 页数" + pageCount);
            totalPages.getAndAdd(pageCount);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void counterPath(String path) {
        File root = new File(path);
        // 如果这个路径是文件夹
        if (root.isDirectory()) {
            // 获取路径下的所有文件
            File[] files = root.listFiles();
            assert files != null;
            for (int i = 0; i < files.length; i++) {
                // 如果还是文件夹 递归获取里面的文件 文件夹
                File file = files[i];
                if (file.isDirectory()) {

                    counterPath(file.getPath());
//                    threadPoolExecutor.execute(() -> {
//
//                    });
                } else {
                    try {
                        System.out.println("正在处理第" + i + "个文件");
                        counter(file.getPath());
                    } catch (Exception e) {
                        System.out.println("page counter error, skip..." + file.getName());
                    }
                }
            }
        }
    }

    public AtomicInteger getTotalPages() {
        return totalPages;
    }

    public static void main(String[] args) {
        final PDFPageCounter counter = new PDFPageCounter();
        String path = "D:\\data\\docShop\\participle_task\\pageNumber\\研报";
        counter.counterPath(path);

        System.out.println(path + "共：" + counter.getTotalPages() + "页");
    }

}