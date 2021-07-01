package utils;

import utils.script.BatchExtractDate;

import java.io.File;
import java.io.IOException;
import java.util.*;
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

    public static void main(String[] args) throws IOException {
        String str = "户号:0468773993 户名:李xx 时间:2019-01-20剩余金额不足,已超过警戒点B,请速续费." +
                "2004年ashfjsahjf iash  发布：20020110 ,.2021年09. 发〔2011〕70号文收悉。经研究，市政";

        String str2 = "户号:0468773993 户名:李xx 时间:0剩余金额不足,已超过警戒点B,请速续费.";
        File file = new File("D:\\data\\docShop\\618All\\WCE30997F993F37BDBDAB9EAEBA3757A6.pdf");

        String content = BatchExtractDate.fileExtractStringCache(file.getPath(), file.getName());

        LinkedHashSet<Integer> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.add(3);
        linkedHashSet.add(7);
        linkedHashSet.add(1);
        linkedHashSet.add(5);

        System.out.println(linkedHashSet.toString());
        System.out.println(linkedHashSet.toArray());
        final Object[] objects = linkedHashSet.toArray();

        System.out.println(objects[0].getClass().getName() + ">:" + objects[objects.length-1]);

//        final DateMatcherResult matching = matching(content);
//        System.out.println("end...");
//        System.out.println(matching.getType());
//        System.out.println(matching.getResult());
    }

    public static DateMatcherResult matching(String content) {

//        Pattern p = Pattern.compile("([发布时间：])(\\d{4})([-|年|/|\\d{2,4}])", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Pattern p = Pattern.compile("(发布时间[：|:])(\\d{4})([-|〕|年|/|\\d{2,4}])", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Pattern p2 = Pattern.compile("(\\d{4})([-|〕|年|/|\\d{2,4}])", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        DateMatcherResult dateMatcherResult = new DateMatcherResult();

        Matcher matcher = p.matcher(content);
        LinkedHashSet<String> list = new LinkedHashSet<>();
        if (matcher.find()) {
            final String eTime = matcher.group(2);
            final String verification = dateVerify(eTime);
            if (verification != null) {
                dateMatcherResult.setType("含发布时间");
                if (!verification.equals("-1")) {
                    list.add(verification);
                }
                dateMatcherResult.setResult(list);
                return dateMatcherResult;
            }
        }

        //没匹配到发布时间..
        //先统计个数，如果个数都一样，挑最大的
        //如果个数不一样，挑出现次数最多的
        matcher = p2.matcher(content);
        int maxYear = 0;
        Map<String, Integer> yearMap = new HashMap<>();

        while (matcher.find()) {
            final String eTime = matcher.group(1);
            try {
                int eYear = Integer.parseInt(eTime);
                if (eYear >= 2000 && eYear <= 2021) {
                    maxYear = Math.max(maxYear, eYear);
                    Integer count = yearMap.get(String.valueOf(eYear));
                    if (count == null) count = 0;
                    yearMap.put(String.valueOf(eYear), count + 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Set<Integer> noSet = new HashSet<>();
        int maxTimes = 0;
        String maxTimesYear = null;
        dateMatcherResult.setType("null");
        if (yearMap.size() > 0) {
            for (Map.Entry<String, Integer> entry : yearMap.entrySet()) {
                noSet.add(entry.getValue());
                if (entry.getValue() > maxTimes) {
                    maxTimes = entry.getValue();
                    maxTimesYear = entry.getKey();
                }
            }
        }
        if (noSet.size() == 1) {
            if (maxYear != 0) {
                dateMatcherResult.setType("取最大");
                list.add(String.valueOf(maxYear));
            }
        } else {
            if (maxTimesYear != null) {
                dateMatcherResult.setType("次数多");
                list.add(maxTimesYear);
            }
        }
        dateMatcherResult.setResult(list);

        return dateMatcherResult;
    }


    private static String dateVerify(String date) {
        try {
            int eYear = Integer.parseInt(date);
            if (eYear >= 2000 && eYear <= 2021) {
                return String.valueOf(eYear);
            } else {
                return "-1";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
