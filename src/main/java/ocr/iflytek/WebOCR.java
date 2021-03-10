package ocr.iflytek;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Description: tess4j
 *
 * @author Soong
 */
public class WebOCR {
    // OCR webapi 接口地址
    private static final String WEBOCR_URL = "http://webapi.xfyun.cn/v1/service/v1/ocr/general";
    // 应用ID (必须为webapi类型应用，并印刷文字识别服务，参考帖子如何创建一个webapi应用：http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=36481)
    private static final String APPID = "604736d7";
    // 接口密钥(webapi类型应用开通印刷文字识别服务后，控制台--我的应用---印刷文字识别---服务的apikey)
    private static final String API_KEY = "475afa8fafafe17867635ec47428a227";
    // 是否返回位置信息
    private static final String LOCATION = "false";
    // 语种(可选值：en（英文），cn|en（中文或中英混合)
    private static final String LANGUAGE = "cn|en";
    // 图片地址,图片最短边至少15px，最长边最大4096px，格式jpg、png、bmp
    private static final String PIC_PATH = "D:\\tmp\\OCR\\ocrTests\\test5\\pic5.png";

    /**
     * OCR WebAPI 调用示例程序
     *
     * @param args no
     * @throws IOException exception
     */
    public static void main(String[] args) throws IOException {
/*        Map<String, String> header = buildHttpHeader();
        byte[] imageByteArray = FileUtil.read(PIC_PATH);
        String imageBase64 = new String(Base64.encodeBase64(imageByteArray), StandardCharsets.UTF_8);
        String jsonResp = HttpUtil.doPost1(WEBOCR_URL, header, "image=" + URLEncoder.encode(imageBase64, "UTF-8"));
//        System.out.println("OCR WebAPI 接口调用结果：" + result);
        //  错误码链接：https://www.xfyun.cn/document/error-code (code返回错误码时必看)

        StringBuilder stringBuilder = new StringBuilder();
//        String jsonResp = "{\"code\":\"0\",\"data\":{\"block\":[{\"type\":\"text\",\"line\":[{\"confidence\":1,\"word\":[{\"content\":\"中华人民共和国食品安全法（2018修正）\"}]},{\"confidence\":1,\"word\":[{\"content\":\"第一章总则\"}]},{\"confidence\":1,\"word\":[{\"content\":\"第一条\"}]},{\"confidence\":1,\"word\":[{\"content\":\"为了保证食品安全，保障公众身体健康和生命安全，制定本法。\"}]},{\"confidence\":1,\"word\":[{\"content\":\"第二条\"}]},{\"confidence\":1,\"word\":[{\"content\":\"在中华人民共和国境内从事下列活动，应当遵守本法：(一）食品生产和加工（以下称食品生产）,食品销售和餐饮服务（以下称食品经营）;二）食品添加剂的生产经营；用于食品的包装材料、\"}]},{\"confidence\":1,\"word\":[{\"content\":\"容器、洗涤剂、消毒剂和用于食品生产经营的工具、设备（以下称食品相关产品）的生产经营；(四）食品生产经营者使用食品添加剂、食品相关产品；(五）食品的贮和运输；(六）对食品添加\"}]},{\"confidence\":1,\"word\":[{\"content\":\"剂、食品相关产品的安全管理。供食用的源于农业的初级产品（以下称食用农产品）的质量安全管理，道守《中华人民共和国农产品质量安全法》的规定。但是，食用农产品的市场销售、有关质量安全标准的\"}]},{\"confidence\":1,\"word\":[{\"content\":\"制定、有关安全信息的公布和本法对农业投入品作出规定的，应当遵守本法的规定。\"}]},{\"confidence\":1,\"word\":[{\"content\":\"第三条\"}]},{\"confidence\":1,\"word\":[{\"content\":\"食品安全工作实行预防为主、风险管理、全程控制、社会共治，建立科学、严格的监督管理制度。\"}]},{\"confidence\":1,\"word\":[{\"content\":\"第四条\"}]},{\"confidence\":1,\"word\":[{\"content\":\"锿\"}]},{\"confidence\":1,\"word\":[{\"content\":\"第五条\"}]},{\"confidence\":1,\"word\":[{\"content\":\"国务院设立食品安全委员会，其职责由国务院规定。国务院食品安全监管管理部门依照本法和国务院规定的职责，对食品生产经营活动实施监管理，国务院卫生行政部的依据规定的职责，组\"}]},{\"confidence\":1,\"word\":[{\"content\":\"只开展食品安全风险监测和风险评估，会同国务院食品安全监管管理部门制定并公布食品安全国家标准。国务院其他有关部门依照本法和国务院规定的职责，承担有关食品安全工作\"}]},{\"confidence\":1,\"word\":[{\"content\":\"第六条\"}]},{\"confidence\":1,\"word\":[{\"content\":\"县级以上地方人民政府对本行政区域的食品安全监督管理工作负责，统一领导、组织、协调本行政区域的食品安全监管理工作以及食品安全突发对工作，建立健全食程监管管理工作机制和信\"}]},{\"confidence\":1,\"word\":[{\"content\":\"息共享机制。县级以上地方人民政府依照本法和国务院的规定，确定本级食品安全监督管理、卫生行政部门和其他有关部门的职责，有关部门在各自职责范围内负责本行政区域的食品安全监管理工作。县\"}]},{\"confidence\":1,\"word\":[{\"content\":\"级人民政府食品安全监督管理部门可以在乡镇或者特定区域设立派出机构。\"}]},{\"confidence\":1,\"word\":[{\"content\":\"第七条\"}]},{\"confidence\":1,\"word\":[{\"content\":\"县级以上地方人民政府实行食品安全监督管理责任制。上级人民政府负责对下一级人民政府的食品安全监督管理工作进行评议、考核，县级以上地方人民政府负责对本级食业管理部门和其他有关部门\"}]},{\"confidence\":1,\"word\":[{\"content\":\"的食品安全监管管理工作进行评议、考核\"}]},{\"confidence\":1,\"word\":[{\"content\":\"第八条\"}]},{\"confidence\":1,\"word\":[{\"content\":\"县级以上人民政府应当将食品安全工作纳入本级国民经济和社会发展规划，将食品安全工作经费列入本级政府财政预算，加强食品安全监管理能力建设，为食品安全工作提供，县级以上人民政府食品安\"}]}]}]},\"desc\":\"success\",\"sid\":\"wcr00850971@dx2d2013a287486f2b00\"}";
        final JSONObject jsonObjectResp = JSONObject.parseObject(jsonResp);
        JSONObject jsonObjectData = (JSONObject) jsonObjectResp.get("data");
        final JSONArray block = jsonObjectData.getJSONArray("block");
        JSONObject blockFirst = (JSONObject) block.get(0);
        final JSONArray lines = blockFirst.getJSONArray("line");
        for (Object line : lines) {
            JSONObject element = (JSONObject) line;
            final JSONArray words = element.getJSONArray("word");
            for (Object word: words) {
                JSONObject eachWord = (JSONObject) word;
                final String content = (String) eachWord.get("content");
                stringBuilder.append(content).append("\n");
            }
        }

        System.out.println(stringBuilder.toString());*/
    }

    public static String execute(String fileName) throws IOException {
        Map<String, String> header = buildHttpHeader();
        byte[] imageByteArray = FileUtil.read(fileName);
        String imageBase64 = new String(Base64.encodeBase64(imageByteArray), StandardCharsets.UTF_8);
        String jsonResp = HttpUtil.doPost1(WEBOCR_URL, header, "image=" + URLEncoder.encode(imageBase64, "UTF-8"));

        StringBuilder stringBuilder = new StringBuilder();
        final JSONObject jsonObjectResp = JSONObject.parseObject(jsonResp);
        JSONObject jsonObjectData = (JSONObject) jsonObjectResp.get("data");
        final JSONArray block = jsonObjectData.getJSONArray("block");
        JSONObject blockFirst = (JSONObject) block.get(0);
        final JSONArray lines = blockFirst.getJSONArray("line");
        for (Object line : lines) {
            JSONObject element = (JSONObject) line;
            final JSONArray words = element.getJSONArray("word");
            for (Object word: words) {
                JSONObject eachWord = (JSONObject) word;
                final String content = (String) eachWord.get("content");
                stringBuilder.append(content).append("\n");
            }
        }
        System.out.println("1/8response: " + stringBuilder.toString().substring(stringBuilder.length() >> 3));
        return stringBuilder.toString();
    }

    /**
     * 组装http请求头
     */
    private static Map<String, String> buildHttpHeader() {
        String curTime = System.currentTimeMillis() / 1000L + "";
        String param = "{\"location\":\"" + LOCATION + "\",\"language\":\"" + LANGUAGE + "\"}";
        String paramBase64 = new String(Base64.encodeBase64(param.getBytes(StandardCharsets.UTF_8)));
        String checkSum = DigestUtils.md5Hex(API_KEY + curTime + paramBase64);
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        header.put("X-Param", paramBase64);
        header.put("X-CurTime", curTime);
        header.put("X-CheckSum", checkSum);
        header.put("X-Appid", APPID);
        return header;
    }
}
