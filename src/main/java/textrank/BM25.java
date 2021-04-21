package textrank;

import java.util.*;

/**
 * Description: tess4j
 *
 * @author Soong
 */
public class BM25 {

    /**
     * 句子核心词汇表
     */
    private Map<Integer, List<String>> purgedSentence;

    private static final double k1 = 2;
    private static final double b = 0.75;

    /**
     * 相似矩阵
     */
    private double[][] similarity;

    public BM25(Map<Integer, List<String>> purgedSentence) {
        this.purgedSentence = purgedSentence;
        similarity = new double[purgedSentence.size()][purgedSentence.size()];
        calculateSimilarity();
    }

    public static void main(String[] args) {
        Map<Integer, List<String>> purgedSentence = new HashMap<>();
        purgedSentence.put(0, Arrays.asList("行政", "机关", "强行", "解除", "行政", "协议", "造成", "损失", "，", "如何", "索取", "赔偿"));
        purgedSentence.put(1, Arrays.asList("借钱", "给", "朋友", "到期", "不", "还", "得", "什么", "时候", "可以", "起诉", "？", "怎么", "起诉"));
        purgedSentence.put(2, Arrays.asList("我", "在", "微信", "上", "被", "骗", "了", "，", "请问", "被", "骗", "多少", "钱", "才", "可以", "立案"));
        purgedSentence.put(3, Arrays.asList("公民", "对于", "选举", "委员会", "对", "选民", "的", "资格", "申诉", "的", "处理", "决定", "不服", "，", "能", "不能", "去", "法院", "起诉"));
        purgedSentence.put(4, Arrays.asList("有人", "走私", "两万元", "怎么", "处置", "他"));
        purgedSentence.put(5, Arrays.asList("法律", "上", "餐具", "饮具", "集中", "消毒", "服务", "单位", "的", "责任", "是不是", "对", "消毒", "餐具", "、", "饮具", "进行", "检验"));
        //这句话时对照组 看他和上面的句子哪个最相似，运行结果显示和4最相似
        purgedSentence.put(6, Arrays.asList("走私", "两万", "元", "法律", "应该", "怎么", "量刑"));

//        purgedSentence.put(0, Arrays.asList("算法", "大致", "分", "基本", "算法", "数据", "结构", "算法", "数论", "算法", "计算", "几何", "算法", "图", "算法", "动态", "规划", "数值", "分析", "加密", "算法", "排序", "算法", "检索", "算法", "随机", "化", "算法", "并行", "算法", "厄", "米", "变形", "模型", "随机", "森林", "算法"));
//        purgedSentence.put(1, Arrays.asList("算法", "宽泛", "分为", "三类"));
//        purgedSentence.put(2, Collections.emptyList());
//        purgedSentence.put(3, Arrays.asList("有限", "确定性", "算法"));
//        purgedSentence.put(4, Arrays.asList("类", "算法", "有限", "一段", "时间", "终止"));
//        purgedSentence.put(5, Arrays.asList("可能", "花", "长", "时间", "执行", "指定", "任务"));
//        purgedSentence.put(6, Arrays.asList("一定", "时间", "终止"));
//        purgedSentence.put(7, Arrays.asList("类", "算法", "得出", "常", "取决", "输入", "值"));
//        purgedSentence.put(8, Collections.singletonList("二"));
//        purgedSentence.put(9, Arrays.asList("有限", "非", "确定", "算法"));
//        purgedSentence.put(10, Arrays.asList("类", "算法", "有限", "时间", "终止"));
//        purgedSentence.put(11, Collections.emptyList());
//        purgedSentence.put(12, Arrays.asList("一个", "定", "数值"));
//        purgedSentence.put(13, Arrays.asList("算法", "唯一", "确定"));
//        purgedSentence.put(14, Collections.singletonList("三"));
//        purgedSentence.put(15, Arrays.asList("无限", "算法"));
//        purgedSentence.put(16, Arrays.asList("没有", "定义", "终止", "定义", "条件"));
//        purgedSentence.put(17, Arrays.asList("定义", "条件", "无法", "输入", "数据", "满足", "终止", "运行", "算法"));
//        purgedSentence.put(18, Collections.singletonList("通常"));
//        purgedSentence.put(19, Arrays.asList("无限", "算法", "产生", "未", "确定", "定义", "终止", "条件"));

        BM25 bm25 = new BM25(purgedSentence);
        System.out.println(Arrays.deepToString(bm25.getSimilarity()));
    }

    public double[][] getSimilarity() {
        return similarity;
    }

    /**
     * 计算相似矩阵
     */
    private void calculateSimilarity() {
        for (int i = 0; i < similarity.length; i++) {
            for (int j = 0; j < similarity[i].length; j++) {
                //句子i
                final List<String> cuList = purgedSentence.get(i);
                //句子j
                final List<String> obList = purgedSentence.get(j);

                double score = scoreIJ(cuList, obList);
                similarity[i][j] = i == j ? 0 : score;
            }
        }
    }

    //BM25核心逻辑
    private double scoreIJ(List<String> cuList, List<String> obList) {
        double result = 0;
        int dl = obList.size();
        final double avgdl = getAvgdl();
        for (String c : cuList) {
            //c在obList的词频
            final double fi = tf(c, obList.toArray(new String[0]));
            final double cidf = idf(c, this.purgedSentence);

            result += cidf * fi * (k1 + 1) / (fi + k1 * (1 - b + b * dl / avgdl));
        }
        return result;
    }

    private double getAvgdl() {
        final int allLen = purgedSentence.values().stream().mapToInt(List::size).sum();
        return allLen / purgedSentence.size();
    }

    /**
     * 计算句子tf
     *
     * @param qi 语素
     * @param d  目标句子
     * @return 词频
     */
    private double tf(String qi, String[] d) {
        if (d.length == 0) return 0.0;
        double t = 0.0;
        for (String s : d) {
            t += s.equals(qi) ? 1 : 0;
        }
        return t / d.length;
    }


    /**
     * 计算句子idf
     *
     * @param qi 语素
     * @param d  全部文档
     * @return idf
     */
    private double idf(String qi, Map<Integer, List<String>> d) {
        int N = d.size();
        int nqi = 0;
        for (List<String> set : d.values()) {
            boolean involve = false;
            for (String s : set) {
                if (s.equals(qi)) {
                    involve = true;
                    break;
                }
            }
            if (involve) nqi++;
        }
        return Math.log10((N - nqi + 0.5) / (nqi + 0.5));
    }

}
