package textrank;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Description: 递归计算每个词组得分
 *
 * @author Soong
 */
public class TextRank {

    private static final double k1 = 2d;
    private static final double d = 0.85d;

    private static final int RECURSION_TIMES = 30;

    TRStructure trStructure;

    /**
     * 参与排名的词
     */
    private List<String> candidates;

    /**
     * 参与排名的词 以及 其"邻居"
     */
    private Map<String, List<String>> windows;

    //should be TreeMap
    private TreeMap<String, Double> rankMap;

    public TextRank(String content, TRStructure.Dimension dimension) {
        trStructure = new TRStructure(content, dimension);
        this.candidates = trStructure.getUseful_words();
        this.windows = trStructure.getWindows();
        rankMap = new TreeMap<>();
        initial();
    }

    private void initial() {
        drawGraph();
    }

    public TreeMap<String, Double> getRankMap() {

        List<Map.Entry<String, Double>> list = new ArrayList<>(rankMap.entrySet());
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        for (Map.Entry<String, Double> e : list) {
            System.out.println(e.getKey() + ":" + e.getValue());
        }

        return rankMap;
    }

    /**
     * 递归计算，论文建议 预计20~30次将会收敛. we trebling
     */
    public void rankRecursion() {
        for (int i = 0; i < RECURSION_TIMES; i++) {
            rankMap.forEach((key, value) -> {
                double newScore;
                //e的邻居
                final List<String> adjacent = windows.get(key);
                //用邻居来计算自己
                double adjRef = calculateAdjacent(adjacent);

                newScore = (1 - d) + d * adjRef;
                rankMap.put(key, newScore);
            });
        }
    }

    // d * ?, this is ?
    private double calculateAdjacent(List<String> adjacent) {
        double rec = 0;
        for (String e : adjacent) {
            double wsj = rankMap.get(e);
            List<String> outJ = windows.get(e);
            double sumOutJ = outJ.stream().mapToDouble(a -> rankMap.get(a)).sum();
            rec += wsj / sumOutJ;
        }
        return rec;
    }


    /**
     * 用tf去初始化图
     */
    private void drawGraph() {
        candidates.forEach(e -> {
//            rankMap.put(e, tf(e, this.candidates));
            rankMap.put(e, 1d);
        });
    }

    /**
     * calculate  the tf of the given words and content which involve it.
     */
    private double tf(String target, List<String> content) {
        AtomicLong times = new AtomicLong(0);
        for (String e : content) {
            if (e.equals(target)) {
                times.incrementAndGet();
            }
        }
        return times.doubleValue() / content.size();
    }

}
