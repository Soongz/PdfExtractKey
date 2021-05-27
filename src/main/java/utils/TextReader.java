package utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Description: tess4j
 *
 * @author Soong
 */
public class TextReader {

    public static List<String> read(File file) {
        List<String> result = new ArrayList<>();
        try (FileInputStream inputStream = new FileInputStream(file);
             InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(streamReader);
        ) {
            String lineTxt;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                result.add(lineTxt);
            }
        } catch (Exception e) {
            System.out.println("read异常");
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
        tmp();
    }


    public static void tmp() {
        File file = new File("D:\\tmp\\clearAfterUsed\\527\\keys_after_retry.sql");
        StringBuilder resullt = new StringBuilder();
        try (FileInputStream inputStream = new FileInputStream(file);
             InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(streamReader);
        ) {
            HashSet<String> set = new HashSet<>();
            String lineTxt;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                final String[] split = lineTxt.split("\"");
                String essKey = split[split.length - 2];

                if (set.contains(essKey)) {
                    System.out.println("dumplicate:" + lineTxt);
                    continue;
                }
                set.add(essKey);
                resullt.append(lineTxt).append("\n");
            }
            System.out.println("end");
        } catch (Exception e) {
            System.out.println("read异常");
            e.printStackTrace();
        }
        flushStringTodisk(resullt.toString(), "D:\\tmp\\clearAfterUsed\\527\\keys527_01.sql");
    }


    public static List<File> readFor(File file) {
        final List<String> paths = read(file);
        List<File> result = new ArrayList<>(paths.size());
        paths.forEach(e -> result.add(new File(e)));
        return result;
    }

    public static void flushStringTodisk(String input, String filename) {
        File file = new File(filename);
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
