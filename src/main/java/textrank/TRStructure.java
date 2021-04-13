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

    private static final char[] separator = new char[]{',', '.'};

    /**
     * 划分后的句子
     */
    private List<String> sentences;

    /**
     * 相似句子关系
     */
    private Map<Integer, List<String>> similarityWindow;


    /**
     * 分析的内容主体
     */
    private String content;
    /**
     * 分词完毕 刪除停用词后的有用词表
     */
    private List<String> useful_words;

    private Dimension dimension;
    /**
     * 所有窗口
     */
    private Map<String, List<String>> windows;

    public TRStructure(String content, Dimension dimension) {
        this.content = content;
        this.dimension = dimension;
        initial();
    }

    private void initial() {
        if (Dimension.WORD.equals(dimension)) {
            participle();
            buildWindow();
        } else if (Dimension.SENTENCE.equals(dimension)) {
            splitSentence(separator);
        }
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

    /**
     * 划分句子
     *
     * @param sentenceSeparator 分隔符列表
     */
    public void splitSentence(char[] sentenceSeparator) {
        sentences = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < content.length(); i++) {
            if (containAtList(content.charAt(i), sentenceSeparator)) {
                if (i - start > 3) {
                    sentences.add(content.substring(start, i));
                    start = i + 1;
                }
            }
        }

    }


    private boolean containAtList(char s, char[] arr) {
        for (char c : arr) {
            if (c == s) {
                return true;
            }
        }
        return false;
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

    enum Dimension {
        WORD, SENTENCE
    }
}
