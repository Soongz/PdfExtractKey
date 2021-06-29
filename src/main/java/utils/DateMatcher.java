package utils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description: DateMatcher
 * 提取年份工具，以下格式能被识别，并提取出四位数字
 * 2020年****
 * 2020/****
 * 2020-****
 * 20200302
 *
 * @author Soong
 */
public class DateMatcher {

    public static void main(String[] args) {
        String str = "户号:0468773993 户名:李xx 时间:2019-01-20剩余金额不足,已超过警戒点B,请速续费." +
                "2020年ashfjsahjf iash  20020110 ";
        DateMatcher dateMatcher = new DateMatcher();
        final Set<String> matching = dateMatcher.matching(str);
        System.out.println("end...");
        System.out.println(matching);
    }

    public static Set<String> matching(String content) {

        Pattern p = Pattern.compile("(\\d{4})([-|年|/|\\d{2,4}])", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

        Matcher matcher = p.matcher(content);
        Set<String> list = new HashSet<>();
        while (matcher.find()) {
            final String eTime = matcher.group(1);

            try {
                int eYear = Integer.parseInt(eTime);
                if (eYear>=2000 && eYear<=2021) {
                    list.add(String.valueOf(eYear));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return list;
    }

}
