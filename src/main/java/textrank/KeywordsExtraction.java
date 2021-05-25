package textrank;

import utils.script.BatchExtractKeyWords;

import java.util.Map;
import java.util.TreeMap;

/**
 * Description: tess4j
 *
 * @author Soong
 */
public class KeywordsExtraction {

    public static void main(String[] args) throws Exception {
//        String content = "程序员(英文Programmer)是从事程序开发、维护的专业人员。一般将程序员分为程序设计人员和程序编码人员，但两者的界限并不非常清楚，特别是在中国。软件从业人员分为初级程序员、高级程序员、系统分析员和项目经理四大类。";
        String content = "1885年，美国佐治亚州的（约翰·彭伯顿），发明了深色的糖浆称为彭伯顿法国酒可乐（Pemberton's French Wine Coka）1885年政府发出禁酒令，因此彭伯顿发明无酒精的Pemberton's French Wine Coka。1886年5月8日他想发明一种饮料，一种让很多需要补充营养的人喜欢喝的饮料。那天，他正在搅拌做好了的饮料，发现它具有提神、镇静的作用以及减轻头痛的作用，他将这种液体加入了糖浆和水，然后加上冰块，他尝了尝，味道好极了，不过在倒第二杯时，助手一不小心加入了苏打水（二氧化碳+水）这回味道更好了，合伙人罗宾逊（Frank M.Robinson）从糖浆的两种成分，激发出命名的灵感，这两种成分就是古柯（Coca）的叶子和可拉（Kola）的果实，罗宾逊为了整齐划一，将Kola的K改C，然后在两个词中间加一横，于是Coca-Cola便诞生了，第一份可口可乐售价为五美分。";
        KeywordsExtraction extraction = new KeywordsExtraction();
//        extraction.execute(content);
//
//        final String nkeys = getNkeys(content, 10);
//        System.out.println(nkeys);

        extraction.temp();
    }

    private void temp() throws Exception {
        String path = "D:\\data\\docShop\\518All\\W0134229B275AA1B168CA60E3B1C80BC6.pdf";
        final String content = BatchExtractKeyWords.fileExtractStringCache(path, "W000B7EB41D423B238F14D3437DC615E9.pdf").trim().replace(" ", "");
        System.out.println(content);
        final String nkeys = getNkeys(content, 20);
        System.out.println(nkeys);
    }

    public void execute(String content) {
        final TextRank textRank = new TextRank(content, TRStructure.Dimension.WORD);
        textRank.rankRecursion();
        final TreeMap<String, Double> rankMap = textRank.getRankMap();
    }

    public static String getNkeys(String content, int topN) {
        final TextRank textRank = new TextRank(content, TRStructure.Dimension.WORD);
        textRank.rankRecursion();
        return textRank.getTopNKeyWords(topN);
    }


}
