package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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


    public static List<File> readFor(File file) {
        final List<String> paths = read(file);
        List<File> result = new ArrayList<>(paths.size());
        paths.forEach(e -> result.add(new File(e)));
        return result;
    }

}
