package textrank;

import org.apache.poi.ss.formula.functions.T;

import java.util.*;

/**
 * Description: tess4j
 *
 * @author Soong
 */
public class SentenceRank {
    private static final double d = 0.85d;

    private static final int RECURSION_TIMES = 2;

    private TRStructure trStructure;

    /**
     * 划分后的句子
     */
    private List<String> sentences;

    /**
     * 句子核心词汇表
     */
    private Map<Integer, Set<String>> purgedSentence;

    private double[][] similarity;

    private Map<Integer, Set<Integer>> sentenceWindows;

    private Map<Integer, Double> rankMap;


    public SentenceRank(String content) {
        trStructure = new TRStructure(content, TRStructure.Dimension.SENTENCE);
        this.sentences = trStructure.getSentences();
        this.purgedSentence = trStructure.getPurgedSentence();
        this.similarity = trStructure.getSimilarity();
        this.sentenceWindows = trStructure.getSentenceWindows();
        initial();
    }

    public static void main(String[] args) {
        String content = "1885年，美国佐治亚州的（约翰·彭伯顿），发明了深色的糖浆称为彭伯顿法国酒可乐（Pemberton's French Wine Coka）。" +
                "1885年政府发出禁酒令，因此彭伯顿发明无酒精的Pemberton's French Wine Coka。" +
                "1886年5月8日他想发明一种饮料，一种让很多需要补充营养的人喜欢喝的饮料。" +
                "那天，他正在搅拌做好了的饮料，发现它具有提神、镇静的作用以及减轻头痛的作用。" +
                "他将这种液体加入了糖浆和水，然后加上冰块。" +
                "他尝了尝，味道好极了。" +
                "不过在倒第二杯时，助手一不小心加入了苏打水（二氧化碳+水）这回味道更好了。" +
                "合伙人罗宾逊（Frank M.Robinson）从糖浆的两种成分，激发出命名的灵感。" +
                "这两种成分就是古柯（Coca）的叶子和可拉（Kola）的果实，罗宾逊为了整齐划一，将Kola的K改C，然后在两个词中间加一横，于是Coca-Cola便诞生了。" +
                "第一份可口可乐售价为五美分。";
        final SentenceRank sentenceRank = new SentenceRank(content);
        sentenceRank.getRankResult();
    }


    public Map<String, Double> getRankResult() {
        Map<String, Double> result = new HashMap<>();

        List<Map.Entry<Integer, Double>> list = new ArrayList<>(rankMap.entrySet());
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        for (int i = 0; i < list.size(); i++) {
            Map.Entry<Integer, Double> e = list.get(i);
            System.out.println(sentences.get(e.getKey()) + ":" + e.getValue());
            result.put(sentences.get(e.getKey()), e.getValue());
        }
        return result;
    }

    private void initial() {
        drawGraph();
        rankRecursion();
    }

    //todo 这个方法又存在的必要吗？
    private void drawGraph() {
        rankMap = new TreeMap<>();
        for (int i = 0; i < sentences.size(); i++) {
            //todo:初始值用1 还是用0
            rankMap.put(i, 1d);
        }
    }

    private void rankRecursion() {
        for (int i = 0; i < RECURSION_TIMES; i++) {

            for (Map.Entry<Integer, Set<Integer>> entry : sentenceWindows.entrySet()) {
                //每个句子的下标
                final Integer leadIndex = entry.getKey();
                //每个句子 有相似度的 句子下标们
                final Set<Integer> adjacent = entry.getValue();

                double adjRef = calculateAdjacent(leadIndex, adjacent);

                double newScore = (1 - d) + d * adjRef;
                rankMap.put(leadIndex, newScore);
            }
        }

    }

    /**
     * 计算 (1-d)+d*?  中的?
     *
     * @param leadIndex 当前句子
     * @param adjacent  当前句子有相似度的邻居们
     * @return 邻居给自己打分结果
     */
    private double calculateAdjacent(int leadIndex, Set<Integer> adjacent) {
        double result = 0;
        for (Integer j : adjacent) {
            final Set<Integer> outJAdjacent = sentenceWindows.get(j);
            double wji = similarity[j][leadIndex];
            double sum_wjk = 0;
            for (Integer k : outJAdjacent) {
                final double wjk = similarity[j][k];
                sum_wjk += wjk;
            }
            result += (wji / sum_wjk) * rankMap.get(j);
        }
        return result;
    }


}
