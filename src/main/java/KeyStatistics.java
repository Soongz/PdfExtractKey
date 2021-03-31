import ocr.iflytek.WebOCR;
import org.ansj.splitWord.analysis.DicAnalysis;
import org.apache.commons.collections4.CollectionUtils;
import utils.DocumentExtractText;
import utils.ItextpdfUtil;
import utils.PDF2pngUtil;

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
        keyStatistics.execute("D:\\data\\docShop\\3.25招财社公众号研报\\1");
    }

    public void execute(String path) {
        File root = new File(path);
        LinkedList<String> failedList = new LinkedList<>();
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
                        System.out.println("handling file: " + file.getPath());
                        process(file);
                    } catch (Exception e) {
                        failedList.add(file.getPath());
                        System.out.println("counting error, skip...");
                    }
                }
            }
            String resultDesc = "共失败" + failedList.size() + "个, " + failedList.toString();
            System.out.println(resultDesc);
            if (!CollectionUtils.isEmpty(failedList)) {
                flushStringTodisk(resultDesc, root.getPath() + "\\result\\_failedList.txt");
            }
            failedList.clear();
        }

    }

    public void process(File file) throws Exception {
        String fileName = file.getPath();
        //1. pdf to String
        String pdfContent = fileExtractString(fileName);
        //2. participle
        String parseResult = DicAnalysis.parse(pdfContent).toStringWithOutNature();
        //3. cout result of step 2 to a file named by pdf's name
        final String sortedParseResult = sortParticipleResult(parseResult);

        flushStringTodisk(sortedParseResult, file.getParent() + "\\result\\" + file.getName() + ".txt");
    }

    private String fileExtractString(String fileName) throws IOException {
        String result = null;
        if (fileName.contains(".doc")) {
            result = DocumentExtractText.extractText(fileName);
            System.out.println("fileExtractString: " + result);
        } else if (fileName.contains(".pdf")) {
            //step1 直接从pdf读文字
//            result = PDFExtractText.extractString(fileName);
            result = ItextpdfUtil.getPdfContent(fileName);
            if ("".equals(result)) {
                //由图片组成的pdf， 先转图片
                PDF2pngUtil.pdf2png(fileName);
                StringBuilder partialResult = new StringBuilder();
                File root = new File(fileName + "dir");
                //遍历图片所在的路径，依次调用ocr接口，并将结果拼接
                if (root.isDirectory()) {
                    File[] files = root.listFiles();
                    assert files != null;
                    for (File file : files) {
                        try {
                            Thread.sleep(100);
                            partialResult.append(WebOCR.execute(file.getPath())); //科大讯飞OCR
//                            partialResult.append(AliOCR.execute(file.getPath())); //阿里OCR
                        } catch (Exception e) {
                            System.out.println("counting error, skip...");
                        }
                    }
                }
                result = partialResult.toString();
            }
        } else if (fileName.contains(".png") || fileName.contains(".jpg")) {
            //图片类型直接调用ocr
            result = WebOCR.execute(fileName);
        }

        System.out.println("fileExtractString: " + result);
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
        List<Map.Entry<String, Integer>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> mapEntry : list) {
            if (mapEntry.getValue() > 1) {
                result.add(mapEntry);
            }
        }
        return Arrays.toString(result.toArray());
    }

    private void flushStringTodisk(String input, String targetFileName) {
        File file = new File(targetFileName);
        if(!file.exists()){
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
