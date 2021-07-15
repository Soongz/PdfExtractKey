package textrank;

import utils.script.BatchExtractKeyWords;

import java.util.TreeMap;

/**
 * Description: tess4j
 *
 * @author Soong
 */
public class KeywordsExtraction {

    public static void main(String[] args) throws Exception {
//        String content = "程序员(英文Programmer)是从事程序开发、维护的专业人员。一般将程序员分为程序设计人员和程序编码人员，但两者的界限并不非常清楚，特别是在中国。软件从业人员分为初级程序员、高级程序员、系统分析员和项目经理四大类。";
        String content = "苹果是蔷薇科苹果亚科苹果属植物，其树为落叶乔木。苹果营养价值很高，富含矿物质和维生素，含钙量丰富，有助于代谢掉体内多余盐分，苹果酸可代谢热量，防止下半身肥胖。";
        KeywordsExtraction extraction = new KeywordsExtraction();
//        extraction.execute(content);
//
//        final String nkeys = getNkeys(content, 10);
//        System.out.println(nkeys);

        extraction.temp();
    }

    private void temp() throws Exception {
//        String path = "D:\\data\\docShop\\518All\\W0134229B275AA1B168CA60E3B1C80BC6.pdf";
        String path = "D:\\data\\docShop\\618All\\WBDA3317101E189FD0E0B74F1543B8A82.pdf";
        System.out.println(path);
        final String content = BatchExtractKeyWords.fileExtractStringCache(path, "WD79CFD6809C621E15C948A223FAF7849.pdf");
        System.out.println(content);
        System.out.println(content.length());
        final String nkeys = getNkeys(content, 20);
        System.out.println(nkeys);
    }

    public void execute(String content) {
        final TextRank textRank = new TextRank(content, TRStructure.Dimension.WORD);
        textRank.rankRecursion();
        final TreeMap<String, Double> rankMap = textRank.getRankMap();
    }

    public static String getNkeys(String content, int topN) {
        final long start = System.currentTimeMillis();
        final TextRank textRank = new TextRank(content, TRStructure.Dimension.WORD);
//        System.out.println("new TextRank 耗时: " + (System.currentTimeMillis() - start));
        textRank.rankRecursion();
        return textRank.getTopNKeyWords(topN);
    }


}
