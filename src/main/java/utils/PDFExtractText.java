package utils;

import com.spire.pdf.PdfDocument;
import com.spire.pdf.PdfPageBase;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Description: super crawler
 *
 * @author Soong
 */
public class PDFExtractText {

    /**
     * pdf需由文字组成
     */
    public static void extract(String fileName) {
        if (!fileName.contains(".pdf")) {
            return;
        }
        PdfDocument doc = null;
        StringBuilder sb = new StringBuilder();
        try (FileWriter writer = new FileWriter("D:\\ExtractAllText2.txt");){
            //创建PdfDocument实例
            doc = new PdfDocument();
            //加载PDF文档
            doc.loadFromFile(fileName);

            PdfPageBase page;
            //遍历PDF页面，获取每个页面的文本并添加到StringBuilder对象
            for (int i = 0; i < doc.getPages().getCount(); i++) {
                page = doc.getPages().get(i);
                sb.append(page.extractText(true));
            }
            writer.write(sb.toString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            assert doc != null;
            doc.close();
        }
    }


    public static String extractString(String fileName) {
        if (!fileName.contains(".pdf")) {
            return "";
        }
        PdfDocument doc = null;
        StringBuilder result = new StringBuilder();
        try {
            doc = new PdfDocument();
            doc.loadFromFile(fileName);
            PdfPageBase page;
            //遍历PDF页面，获取每个页面的文本并添加到StringBuilder对象
            for (int i = 0; i < doc.getPages().getCount(); i++) {
                page = doc.getPages().get(i);
                result.append(page.extractText(true));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            assert doc != null;
            doc.close();
        }
        return result.toString();
    }

    public static void main(String[] args) {
        String path = "";
        extract(path);
    }
}