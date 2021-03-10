import ocr.iflytek.WebOCR;
import org.ansj.splitWord.analysis.DicAnalysis;
import utils.PDF2pngUtil;
import utils.PDFExtractText;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Description: tess4j
 *
 * @author Soong
 */
public class KeyStatistics {

    public static void main(String[] args) {
        final KeyStatistics keyStatistics = new KeyStatistics();
        keyStatistics.execute("D:\\data\\docShop\\participle_task\\test\\test");
    }

    public void execute(String path) {
        File root = new File(path);
        // 如果这个路径是文件夹
        if (root.isDirectory()) {
            // 获取路径下的所有文件
            File[] files = root.listFiles();
            assert files != null;
            for (File file : files) {
                if (file.isDirectory()) {
                    execute(file.getPath());
                } else {
                    try {
                        process(file.getPath());
                    } catch (Exception e) {
                        System.out.println("counting error, skip...");
                    }
                }
            }
        }
    }

    public void process(String fileName) throws Exception {
        //1. pdf to String
        String pdfContent = pdf2String(fileName);
        //2. participle
        String parseResult = DicAnalysis.parse(pdfContent).toStringWithOutNature();
        //3. cout result of step 2 to a file named by pdf's name
        final String sortedParseResult = sortParticipleResult(parseResult);

        flushStringTodisk(sortedParseResult, fileName + ".txt");
    }

    private String pdf2String(String fileName) throws IOException {
        boolean pdfIsPng = 2 << 5 < 100;
        String result;
        if (pdfIsPng) {
            PDF2pngUtil.pdf2png(fileName);
            StringBuilder partialResult = new StringBuilder();
            File root = new File(fileName + "dir");
            // 如果这个路径是文件夹
            if (root.isDirectory()) {
                // 获取路径下的所有文件
                File[] files = root.listFiles();
                assert files != null;
                for (File file : files) {
                    try {
                        Thread.sleep(1000);
//                        partialResult.append(WebOCR.execute(file.getPath()));
                    } catch (Exception e) {
                        System.out.println("counting error, skip...");
                    }
                }
            }
            result = partialResult.toString();
        } else {
            result = PDFExtractText.extractString(fileName);
        }
        return result;
    }


    private String sortParticipleResult(String input) {
        Map<String, Integer> map = new HashMap<>();
        String[] words = input.split(",");
        for (String word : words) {
            String str = word.trim();
            // 过滤空白字符
            if (str.equals(""))
                continue;
                // 过滤一些高频率的符号
            else if (str.matches("[）|（|.|，|。|+|-|“|”|：|？|\\s]"))
                continue;
                // 此处过滤长度为1的str
            else if (str.length() < 2)
                continue;

            if (!map.containsKey(word)) {
                map.put(word, 1);
            } else {
                int n = map.get(word);
                map.put(word, ++n);
            }
        }

        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        //no idea why cannot use lamda
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        return Arrays.toString(list.toArray());
    }

    private void flushStringTodisk(String input, String targetFileName) {
        try (FileWriter writer = new FileWriter(targetFileName);) {
            writer.write(input);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
