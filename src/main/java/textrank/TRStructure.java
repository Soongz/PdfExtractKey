package textrank;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.util.*;

/**
 * Description: tess4j
 *
 * @author Soong
 */
public class TRStructure {

    private static final Integer WINDOW_SIZE = 5;
    private static final double k1 = 2d;
    private static final double d = 0.85d;

    /**
     * 有用的词汇
     * 名词 形容词
     */
    private static final List<String> speeches = Arrays.asList("n", "a");


    /**
     * 分析的内容主体
     */
    private String content;
    /**
     * 分词完毕 刪除停用词后的有用词表
     */
    private List<String> useful_words;

    /**
     * 所有窗口
     */
    private Map<String, List<String>> windows;


    public TRStructure(String content) {
        this.content = content;
        initial();
    }

    private void initial() {
        participle();
        buildWindow();
    }


    /**
     * 分词预处理整个文章
     */
    public void participle() {
        final Result origin = ToAnalysis.parse(this.content);
        useful_words = new LinkedList<>();
        for (Term term : origin.getTerms()) {
            if (containAtList(term.natrue().natureStr, speeches)) {
                useful_words.add(term.getRealName());
            }
        }
    }

    /**
     * 构建窗口
     */
    public void buildWindow() {
        for (int i = 0; i < useful_words.size(); i++) {
            List<String> adjacence = new ArrayList<>(WINDOW_SIZE * 2);
            for (int j = i - 5; j < i + 5; j++) {
                if (j < 0 || j > useful_words.size()) continue;
                adjacence.add(useful_words.get(j));
            }
            this.windows.put(useful_words.get(i), adjacence);
        }
    }

    private boolean containAtList(String t, List<String> list) {
        for (String o : list) {
            if (t.equals(o)) {
                return true;
            }
        }
        return false;
    }
}
