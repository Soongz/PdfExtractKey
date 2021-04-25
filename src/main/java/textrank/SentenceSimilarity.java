package textrank;

import java.util.List;
import java.util.Map;

/**
 * Description: tess4j
 *
 * @author Soong
 */
public interface SentenceSimilarity {

    /**
     * 计算句子相似度
     */
    double[][] calculateSimilarity(Map<Integer, List<String>> purgedSentence);

}
