package textrank;

import utils.TextReader;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Description: 递归计算每个词组得分
 *
 * @author Soong
 */
public class TextRank {

    private static final double k1 = 2d;
    private static final double d = 0.85d;

    private static final int RECURSION_TIMES = 10;

    TRStructure trStructure;

    private static List<String> stopWords = new CopyOnWriteArrayList<>();

    private static final String STOPWPRDS_FILE_PATH = "stop_words.txt";

    {
        try {
            String BAYONET_PATH = Objects.requireNonNull(this.getClass().getClassLoader().getResource(STOPWPRDS_FILE_PATH)).getPath();
            stopWords = TextReader.read(new File(BAYONET_PATH));
        } catch (Exception e) {
            System.out.println("No stop-words lib has been found. Put it to resources/");
        }
    }

    /**
     * 参与排名的词
     */
    private List<String> candidates;

    /**
     * 参与排名的词 以及 其"邻居"
     */
    private final ConcurrentHashMap<String, List<String>> windows;

    //should be TreeMap
    private final TreeMap<String, Double> rankMap;

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

    // "a b c d e"
    public String getTopNKeyWords(int top) {

        List<Map.Entry<String, Double>> list = new ArrayList<>(rankMap.entrySet());
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        int i = 0;
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, Double> e : list) {
            if (stopWords.contains(e.getKey())) {
                continue;
            }
            if (i++ >= top) break;
            if (i == 1) {
                result.append(e.getKey());
                continue;
            }
            result.append(" ").append(e.getKey());
        }

        return result.toString();
    }

    /**
     * 递归计算，论文建议 预计20~30次将会收敛. we trebling
     */
    public void rankRecursion() {
        List<String> allTask = new ArrayList<>();
        rankMap.forEach((key, value) -> {
            allTask.add(key);
        });

//        final long start = System.currentTimeMillis();
        for (int i = 0; i < RECURSION_TIMES; i++) {
//            final long eachStart = System.currentTimeMillis();
            final RankTask rankTask = new RankTask(allTask, 0, allTask.size());
            ForkJoinPool.commonPool().invoke(rankTask);

//            rankMap.forEach((key, value) -> {
//                double newScore;
//                //e的邻居
//                final List<String> adjacent = windows.get(key);
//                //用邻居来计算自己
//                double adjRef = calculateAdjacent(adjacent);
//
//                newScore = (1 - d) + d * adjRef;
//                rankMap.put(key, newScore);
//            });
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

    class RankTask extends RecursiveAction {
        List<String> tasks;
        int start;
        int end;

        public RankTask(List<String> tasks, int start, int end) {
            this.tasks = tasks;
            this.start = start;
            this.end = end;
        }

        @Override
        protected void compute() {
            if (end - start < 500) {
                for (int i = start; i < end; i++) {
                    double newScore;
                    //e的邻居
                    final List<String> adjacent = windows.get(tasks.get(i));
                    //用邻居来计算自己
                    double adjRef = calculateAdjacent(adjacent);

                    newScore = (1 - d) + d * adjRef;
                    rankMap.put(tasks.get(i), newScore);
                }
                return;
            }
            int middle = (end + start) / 2;
//            System.out.println(String.format("split %d~%d ==> %d~%d, %d~%d", start, end, start, middle, middle, end));
            RankTask subtask1 = new RankTask(this.tasks, start, middle);
            RankTask subtask2 = new RankTask(this.tasks, middle, end);
            invokeAll(subtask1, subtask2);
            subtask1.join();
            subtask2.join();
        }
    }
}
