package utils;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.FileInputStream;

public class DocumentExtractText {


    public static void main(String[] args) {
        String fileName = "D:\\data\\docShop\\participle_task\\311\\text.doc";
        System.out.println(extractText(fileName));
    }

    public static String extractText(String fileName) {
        if (!fileName.contains(".doc")) return "";

        String result = "";
        try (FileInputStream fis = new FileInputStream(fileName);
             XWPFDocument file = new XWPFDocument(OPCPackage.open(fis));
             XWPFWordExtractor ext = new XWPFWordExtractor(file)) {
            result = ext.getText();
        } catch (OLE2NotOfficeXmlFileException e) {
            try (FileInputStream fis = new FileInputStream(fileName);
                 HWPFDocument hwpfDocument = new HWPFDocument(fis);
                 WordExtractor wordExtractor = new WordExtractor(hwpfDocument)) {
                result = wordExtractor.getText();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}