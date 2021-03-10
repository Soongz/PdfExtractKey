package participle;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.library.DicLibrary;
import org.ansj.splitWord.analysis.DicAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.io.*;
import java.util.*;

/**
 * Description: tess4j
 *
 * @author Soong
 */
public class ParticipleTest {
    public static void main(String[] args) throws IOException {

        customDictByConfig();
    }

    public static void defaultUsage() throws IOException {
        Map<String, Integer> map = new HashMap<>();

        String article = getString();
        String result = ToAnalysis.parse(article).toStringWithOutNature();
        String[] words = result.split(",");

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

        Iterator<Map.Entry<String, Integer>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        List<Map.Entry<String, Integer>> list = new ArrayList<>();
        Map.Entry<String, Integer> entry;

        while ((entry = getMax(map)) != null) {
            list.add(entry);
        }
        System.out.println(Arrays.toString(list.toArray()));
    }

    public static void customDict() {
        DicLibrary.insert(DicLibrary.DEFAULT, "增加新词", "我是词性", 1000);
        DicLibrary.insert(DicLibrary.DEFAULT, "的例子", "我是词性2", 1000);
        Result parse = DicAnalysis.parse("这是用户自定义词典增加新词的例子");
        System.out.println(parse);
        boolean flag = false;
        for (Term term : parse) {
            flag = flag || "增加新词".equals(term.getName());
        }
        DicLibrary.delete(DicLibrary.DEFAULT, "增加新词");
        final Result withoutConfig = DicAnalysis.parse("这是用户自定义词典增加新词的例子");
        System.out.println(withoutConfig);
    }

    public static void customDictByConfig() {
        final Result parse = ToAnalysis.parse("这是用户自定义词典增加新词的例子凹阳台");

//        Result parse = DicAnalysis.parse("这是用户自定义词典增加新词的例子凹阳台");
        System.out.println(parse);
    }

    /**
     * 找出map中value最大的entry, 返回此entry, 并在map删除此entry
     *
     * @param map
     * @return
     */
    public static Map.Entry<String, Integer> getMax(Map<String, Integer> map) {
        if (map.size() == 0) {
            return null;
        }
        Map.Entry<String, Integer> maxEntry = null;
        boolean flag = false;
        Iterator<Map.Entry<String, Integer>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            if (!flag) {
                maxEntry = entry;
                flag = true;
            }
            if (entry.getValue() > maxEntry.getValue()) {
                maxEntry = entry;
            }
        }
        map.remove(maxEntry.getKey());
        return maxEntry;
    }

    /**
     * 从文件中读取待分割的文章素材.
     * 　　　* 文件内容来自简书热门文章: https://www.jianshu.com/p/5b37403f6ba6
     *
     * @return
     * @throws IOException
     */
    public static String getString() throws IOException {
        StringBuilder strBuilder = new StringBuilder();
        try (FileInputStream inputStream = new FileInputStream(new File("D:\\participle.txt"));
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                strBuilder.append(line);
            }
        }
        return strBuilder.toString();
    }
}
