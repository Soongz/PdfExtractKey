package textrank;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.DicAnalysis;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description: 构造TextRank java数据结构
 *
 * @author Soong
 */
public class TRStructure {

    private static final Integer WINDOW_SIZE = 5;
    /**
     * 有用的词汇
     * 527 去掉副词
     * 名词、动词、形容词、地名、人名、机构名
     */
    private static final List<String> speeches = Arrays.asList("n", "a", "ns", "nr", "nt", "en");

    private static final char[] separator = new char[]{'.', '?', '。', '？', '\n'};

    /**
     * 划分后的句子
     */
    private List<String> sentences;

    /**
     * 句子核心词汇表
     */
    private ConcurrentHashMap<Integer, List<String>> purgedSentence;

    /**
     * 相似矩阵
     */
    private double[][] similarity;

    private ConcurrentHashMap<Integer, List<Integer>> sentenceWindows;

    private SentenceSimilarity sentenceSimilarity;
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
    private ConcurrentHashMap<String, List<String>> windows;

    public TRStructure(String content, Dimension dimension, SentenceSimilarity sentenceSimilarity) {
        this.content = content;
        this.dimension = dimension;
        this.sentenceSimilarity = sentenceSimilarity;
        initial();
    }

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
            //1. 划分句子
            //2. 句子分词 去除停用词
            //3. 计算句子相似度
            //4. 构造窗口
            splitSentence(separator);
            sentencePurging();
            //计算相似矩阵， 又不同的解法
//            calSimilarity();
//            final BM25 bm25 = new BM25(purgedSentence);
//            this.similarity = bm25.getSimilarity();
            if (sentenceSimilarity == null)
                throw new RuntimeException("未设置句子相似度算法. sentenceSimilarity is equals to null");
            this.similarity = sentenceSimilarity.calculateSimilarity(purgedSentence);
            buildSentenceWindow();
        }
    }

    /**
     * 构造句子窗口（有相似度的才会是邻居）
     */
    private void buildSentenceWindow() {
        sentenceWindows = new ConcurrentHashMap<>();
        for (int i = 0; i < similarity.length; i++) {
            List<Integer> adjNum = new ArrayList<>();
            for (int j = 0; j < similarity.length; j++) {
                if (similarity[i][j] != 0) adjNum.add(j);
            }
            sentenceWindows.put(i, adjNum);
        }
    }


    /**
     * 分词预处理整个文章
     */
    public void participle() {
        final Result origin = DicAnalysis.parse(this.content);
        useful_words = new LinkedList<>();
        final long start = System.currentTimeMillis();
        for (Term term : origin.getTerms()) {
            if (containAtList(term.natrue().natureStr, speeches) && term.getRealName().length() > 1) {
                useful_words.add(term.getRealName());
            }
        }
        System.out.println("分词耗时: " + (System.currentTimeMillis() - start));
    }

    /**
     * 构建关键词窗口
     */
    public void buildWindow() {
        final long start = System.currentTimeMillis();
        windows = new ConcurrentHashMap<>(useful_words.size());
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
//        System.out.println("build window耗时: " + (System.currentTimeMillis() - start));
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

    /**
     * 净化句子，刪除停用词，无用的词性
     */
    public void sentencePurging() {
        purgedSentence = new ConcurrentHashMap<>();
        for (int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i);
            final Result origin = DicAnalysis.parse(sentence);
            List<String> survior = new ArrayList<>();
            for (Term term : origin.getTerms()) {
                if (containAtList(term.natrue().natureStr, speeches) && term.getRealName().length() > 1) {
                    survior.add(term.getRealName());
                }
            }
            purgedSentence.put(i, survior);
        }
    }

    /**
     * 计算句子相似度
     * 此方法为论文中推荐的方法
     * todo item +1: class implaments Similarity {}
     */
    public void calSimilarity() {
        similarity = new double[purgedSentence.size()][purgedSentence.size()];
        for (Map.Entry<Integer, List<String>> entry : purgedSentence.entrySet()) {
            final Integer index = entry.getKey();
            final List<String> currentValue = entry.getValue();
            for (int i = 0; i < purgedSentence.size(); i++) {
                if (index == i) continue; //自己与自己不计算相似度


                final List<String> otherOne = purgedSentence.get(i);
                if (currentValue.size() == 0 || otherOne.size() == 0) {
                    similarity[index][i] = 0.0;
                    continue;
                }

                final int i1 = commonWordCount(currentValue, otherOne);
                double x;
                final double denominator = (x = Math.log10(currentValue.size() * otherOne.size())) == 0 ? 1 : x;
                double sij = i1 / denominator;
                similarity[index][i] = sij;
            }

        }
    }

    /**
     * 计算两个list中相同词的个数
     */
    private int commonWordCount(List<String> t1, List<String> t2) {
        int result = 0;
        for (String s : t1) {
            if (containAtList(s, t2)) {
                result++;
            }
        }
        return result;
    }


    private boolean containAtList(char s, char[] arr) {
        for (char c : arr) {
            if (c == s) {
                return true;
            }
        }
        return false;
    }

    private boolean containAtList(String t, Collection<String> list) {
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

    public ConcurrentHashMap<String, List<String>> getWindows() {
        return windows;
    }

    public List<String> getSentences() {
        return sentences;
    }

    public ConcurrentHashMap<Integer, List<String>> getPurgedSentence() {
        return purgedSentence;
    }

    public double[][] getSimilarity() {
        return similarity;
    }

    public ConcurrentHashMap<Integer, List<Integer>> getSentenceWindows() {
        return sentenceWindows;
    }

    enum Dimension {
        WORD, SENTENCE
    }
}
