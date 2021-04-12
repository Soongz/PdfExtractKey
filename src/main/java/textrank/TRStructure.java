package textrank;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * Description: 构造TextRank java数据结构
 *
 * @author Soong
 */
public class TRStructure {

    private static final Integer WINDOW_SIZE = 5;
    /**
     * 有用的词汇
     * 名词、动词、形容词、副词
     */
    private static final List<String> speeches = Arrays.asList("n", "a", "vn", "d", "ns");


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
            if (containAtList(term.natrue().natureStr, speeches) && term.getRealName().length() > 1) {
                useful_words.add(term.getRealName());
            }
        }
    }

    /**
     * 构建窗口
     */
    public void buildWindow() {
        windows = new HashMap<>(useful_words.size());
        for (int i = 0; i < useful_words.size(); i++) {
            List<String> adjacent = new ArrayList<>(WINDOW_SIZE * 2);
            for (int j = i - WINDOW_SIZE; j < i + WINDOW_SIZE; j++) {
                if (j < 0 || j >= useful_words.size()) continue;
                adjacent.add(useful_words.get(j));
            }
            if (!CollectionUtils.isEmpty(windows.get(useful_words.get(i)))) {
                adjacent.addAll(windows.get(useful_words.get(i)));
            }
            this.windows.put(useful_words.get(i), adjacent);
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

    public List<String> getUseful_words() {
        return useful_words;
    }

    public Map<String, List<String>> getWindows() {
        return windows;
    }
}
