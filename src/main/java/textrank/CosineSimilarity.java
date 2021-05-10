package textrank;

/**
 * Description: tess4j
 *
 * @author Soong
 */
public class CosineSimilarity {

    /**
     * 计算n维向量a与向量b的余弦相似度
     * <p>
     * cos(θ) = (a * b) / (|a| *|b|)
     * </p>
     */
    public static double calculate(double[] a, double[] b) throws Exception {
        if (a.length != b.length) throw new Exception("2 vetor dimension are not equal");
        double molecule = 0;
        double denominator;

        double vec_a_value = 0;
        double vec_b_value = 0;
        for (int i = 0; i < a.length; i++) {
            molecule += a[i] * b[i];
            vec_a_value += Math.pow(a[i], 2);
            vec_b_value += Math.pow(b[i], 2);
        }

        denominator = Math.pow(vec_a_value, 0.5d) * Math.pow(vec_b_value, 0.5d);
        return denominator == 0 ? 0 : molecule / denominator;
    }


    public static void main(String[] args) throws Exception {
        double[] a = {1, 2, 1, 2, 1, 1, 1, 1, 0, 0};
        double[] b = {1, 2, 1, 1, 0, 0, 1, 1, 1, 1};
        final double calculate = calculate(a, b);
        //expect result: 0.805823
        System.out.println(calculate);
    }
}
