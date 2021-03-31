package utils;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

import java.io.IOException;

/**
 * @author hsm
 */
public class ItextpdfUtil {

    public static void main(String[] args) {
        String PDFPATH = "D:\\data\\docShop\\3.25招财社公众号研报\\test\\test2.pdf";
        String content = getPdfContent(PDFPATH);
        System.out.println(content);
    }

    /**
     * 获取pdf的内容
     */
    public static String getPdfContent(String pdfPath) {
        PdfReader reader = null;
        StringBuilder result = new StringBuilder();
//        String prePageValue = "";
        try {
            reader = new PdfReader(pdfPath);
            PdfReaderContentParser parser = new PdfReaderContentParser(reader);
            int num = reader.getNumberOfPages();
            TextExtractionStrategy strategy;
            for (int i = 1; i <= num; i++) {
                strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
                final String value = strategy.getResultantText();

//                if (i >= 2) {
//                    //如果只能解析出水印的文字，那么前两页文字必定相同，直接返回空，启用ocr
//                    //length>=1 以防两个页面都是空白页面
//                    if (value.length() >= 1 && value.equals(prePageValue)) {
//                        return "";
//                    }
//                }
//                prePageValue = value;
                result.append(value);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //假设水印长度会小于30
        return result.length() > 30 ? result.toString() : "";
    }
}