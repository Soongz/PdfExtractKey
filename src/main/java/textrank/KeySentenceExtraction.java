package textrank;

import utils.script.BatchExtractKeyWords;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description: tess4j
 *
 * @author Soong
 */
public class KeySentenceExtraction {
    public static void main(String[] args) throws Exception {
        KeySentenceExtraction extraction = new KeySentenceExtraction();

        extraction.temp();
    }

    public static List<String> getTopNSentences(String content, int topN) {
        final SentenceRank sentenceRank = new SentenceRank(content);
        return sentenceRank.getTopN(topN);
    }

    private void temp() throws Exception {
        String path = "D:\\data\\docShop\\618All\\W55B582553653D8BBEC82D47245C67A57.pdf";
        System.out.println(path);
        final String content = BatchExtractKeyWords.fileExtractStringCache(path, "W000B7EB41D423B238F14D3437DC615E9.pdf");
        System.out.println(content);
        System.out.println(content.length());
        final SentenceRank sentenceRank = new SentenceRank(content);
        final Map<String, Double> rankResult = sentenceRank.getRankResult();

        AtomicInteger i = new AtomicInteger();
        rankResult.forEach((key, value) -> {
            if (i.getAndIncrement() > 10) return;
            System.out.println(key.replace((char) 12288, ' ').trim());
        });
    }

}
