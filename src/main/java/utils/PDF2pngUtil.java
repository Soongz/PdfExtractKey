package utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Description: introWebMagic
 *
 * @author Soong
 */
public class PDF2pngUtil {

    //可自由确定起始页和终止页
    public static void pdf2png(String fileAddress, String filename, int indexOfStart, int indexOfEnd) {
        // 将pdf装图片 并且自定义图片得格式大小
        String split = "/";
        if (System.getProperty("os.name").contains("Windows")) {
            split = "\\";
        }
        File file = new File(fileAddress + split + filename + ".pdf");
        try (PDDocument doc = PDDocument.load(file)) {
            PDFRenderer renderer = new PDFRenderer(doc);
            int pageCount = doc.getNumberOfPages();
            for (int i = indexOfStart; i < indexOfEnd && i < pageCount; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 144); // Windows native DPI
                // BufferedImage srcImage = resize(image, 240, 240);//产生缩略图
                ImageIO.write(image, "PNG", new File(fileAddress + split + filename + "_" + (i + 1) + ".png"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //转换全部的pdf
    public static void pdf2png(String filename) throws IOException {
        if (!filename.contains(".pdf")) {
            return;
        }
        // 将pdf装图片 并且自定义图片得格式大小
        File file = new File(filename);
        try (PDDocument doc = PDDocument.load(file)) {
            PDFRenderer renderer = new PDFRenderer(doc);
            int pageCount = doc.getNumberOfPages();
            BufferedImage image;
            for (int i = 0; i < pageCount; i++) {
                image = renderer.renderImageWithDPI(i, 144); // Windows native DPI
                // BufferedImage srcImage = resize(image, 240, 240);//产生缩略图
                String dirName = filename + "dir";
                File dir = new File(dirName);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                ImageIO.write(image, "PNG", new File(dirName + "\\" + (i + 1) + ".png"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        pdf2png("D:\\data\\docShop\\participle_task\\test\\《民用建筑修缮工程查勘与设计标准》.pdf");
    }

}