package utils;

import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Description: tess4j
 *
 * @author Soong
 */
public class TextReader {

    private static final String PREFIX = "http://cdndoc.tjdata.com/";
    private final static String SQL_PREFIX = "UPDATE tb_document SET year = \"";
    private final static String SQL_MIDDLE = "\" WHERE ess_key = \"";

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
//        getTitlesAndEssKey("D:\\tmp\\clearAfterUsed\\628\\20210628-1607.csv");
//        getTitles("D:\\tmp\\clearAfterUsed\\628\\20210628-1607.csv");
        tmp();
    }


    public static void tmp() {
        File file = new File("D:\\tmp\\clearAfterUsed\\705\\sql_use.csv");
        StringBuilder result = new StringBuilder();
        try (FileInputStream inputStream = new FileInputStream(file);
             InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(streamReader);
        ) {
            String lineTxt;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                try {
                    final String[] split = lineTxt.split(",");
                    String type = split[2];
                    if (split.length > 4) {
                        StringBuilder years = new StringBuilder(split[3]);
                        for (int i = 4; i < split.length; i++) {
                            years.append(",").append(split[i]);
                        }

                        String essKey = split[1];
                        result.append(SQL_PREFIX).append(years).append(SQL_MIDDLE).append(essKey).append("\";\n");
                    }
                } catch (Exception e) {
                    System.err.println("error happend...");
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println("read异常");
            e.printStackTrace();
        }
        flushStringTodisk(result.toString(), "D:\\tmp\\clearAfterUsed\\705\\GT1_year.sql");
    }

    public static HashMap<String, String> getTitlesAndEssKey(String filePath) {
        File file = new File(filePath);
        HashMap<String, String> result = new HashMap<>();
        try (FileInputStream inputStream = new FileInputStream(file);
             InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(streamReader);
        ) {
            String lineTxt;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                final String[] split = lineTxt.split(",");
                //essKey:fileName
                result.put(split[1], split[0]);
            }
        } catch (Exception e) {
            System.out.println("read异常");
            e.printStackTrace();
        }
        return result;
    }


    public static void getTitles(String filePath) {
        File file = new File(filePath);
        HashSet<String> result = new HashSet<>();

        String PATH_PREFIX_1 = "D:\\data\\docShop\\518All\\";
        String PATH_PREFIX_2 = "D:\\data\\docShop\\618All\\";
        String DOWNLOAD_PREFIX = "http://cdndoc.tjdata.com/";
        HashSet<String> downloadSet = new HashSet<>();

        try (FileInputStream inputStream = new FileInputStream(file);
             InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(streamReader);
        ) {
            String lineTxt;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                final String[] split = lineTxt.split(",");
                final String ess = split[1].replaceAll("\"", "");
                result.add(ess);
            }
        } catch (Exception e) {
            System.out.println("read异常");
            e.printStackTrace();
        }
        File file1 = null;
        for (String s : result) {
            file1 = new File(PATH_PREFIX_1 + s);
            if (file1.exists()) {
                continue;
            }
            file1 = new File(PATH_PREFIX_2 + s);
            if (file1.exists()) {
                continue;
            }
            downloadSet.add(DOWNLOAD_PREFIX + s);
        }
        StringBuilder sb = new StringBuilder();
        for (String s : downloadSet) {
            sb.append(s).append("\n");
        }
//        flushStringTodisk(sb.toString(), "D:\\tmp\\clearAfterUsed\\629_2\\629downloadUtls.txt");
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
