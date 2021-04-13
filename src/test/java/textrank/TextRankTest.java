package textrank;

import org.junit.Test;

public class TextRankTest {


    @Test
    public void testSplitSentence() {
        String content = "1885年，美国佐治亚州的（约翰·彭伯顿），发明了深色的糖浆称为彭伯顿法国酒可乐（Pemberton's French Wine Coka）1885年政府发出禁酒令，因此彭伯顿发明无酒精的Pemberton's French Wine Coka。1886年5月8日他想发明一种饮料，一种让很多需要补充营养的人喜欢喝的饮料。那天，他正在搅拌做好了的饮料，发现它具有提神、镇静的作用以及减轻头痛的作用，他将这种液体加入了糖浆和水，然后加上冰块，他尝了尝，味道好极了，不过在倒第二杯时，助手一不小心加入了苏打水（二氧化碳+水）这回味道更好了，合伙人罗宾逊（Frank M.Robinson）从糖浆的两种成分，激发出命名的灵感，这两种成分就是古柯（Coca）的叶子和可拉（Kola）的果实，罗宾逊为了整齐划一，将Kola的K改C，然后在两个词中间加一横，于是Coca-Cola便诞生了，第一份可口可乐售价为五美分。";
        char[] separator = new char[] {',', '.'};
        TRStructure trStructure = new TRStructure(content, TRStructure.Dimension.SENTENCE);

//        trStructure.splitSentence(separator);
//        trStructure.sentencePurging();
//        trStructure.calSimilarity();
    }
}
