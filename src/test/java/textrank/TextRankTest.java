package textrank;

import org.junit.Test;

public class TextRankTest {


    @Test
    public void testSplitSentence() {
        String content = "小明喜欢小花, 有一天小明. 突然, 很快啊我当时什么都, 没看";
        char[] separator = new char[] {',', '.'};
        TRStructure trStructure = new TRStructure(content, TRStructure.Dimension.SENTENCE);

        trStructure.splitSentence(separator);
    }
}
