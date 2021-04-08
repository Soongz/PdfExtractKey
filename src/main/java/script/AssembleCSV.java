package script;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * Description: tess4j
 *
 * @author Soong
 */
public class AssembleCSV {

    private StringBuilder content = new StringBuilder("标题, 提取关键词").append("\n");
    private static final String SEPERATOR = ",";
    private static final List<String> INVALIDATE_WORDS = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z");

    public static void main(String[] args) {
        AssembleCSV assembleCSV = new AssembleCSV();
        assembleCSV.execute("D:\\data\\docShop\\3.25招财社公众号研报_done\\331result\\result");
    }

    public void execute(String path) {
        File root = new File(path);
        if (root.isDirectory()) {
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
                        System.out.println("met error, skip...");
                    }
                }
            }
            flushTodisk(content.toString(), root.getPath() + "\\result\\assemble.csv");
            content = new StringBuilder("标题, 提取关键词").append("\n");
        }
    }


    public void process(File file) {
        if (!file.getName().contains(".txt")) return;
        final String orinalContent = reader(file);
        final String[] split = orinalContent.split(SEPERATOR);
        int count = 0;
        content.append(file.getName()).append(SEPERATOR);
        for (int i = 0; i < split.length && count < 30; i++) {
            final String str = split[i];
            if (!containsAtList(str, INVALIDATE_WORDS)) {
                content.append(str).append(SEPERATOR);
                count++;
            }
        }
        content.append("\n");
    }

    private String reader(File file) {
        if (!file.getName().contains(".txt")) return "";
        StringBuilder result = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String s = null;
            while ((s = br.readLine()) != null) {
                result.append(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }


    //content以,分隔
    public void soutToCsv(String fileName, String content) {
        //1. 读出文件

        //2. 文件末尾加上\n+content.
    }

    public static void flushTodisk(String input, String fileName) {
        File file = new File(fileName);
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


    public static boolean containsAtList(String value, List<String> strings) {
        for (String element : strings) {
            if (value.contains(element)) {
                return true;
            }
        }
        return false;
    }
}
