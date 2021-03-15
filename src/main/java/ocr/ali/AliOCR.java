package ocr.ali;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import sun.misc.BASE64Encoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Description: tess4j
 * 阿里云OCR产品简介： https://help.aliyun.com/document_detail/30402.html?spm=a2c4g.11186623.6.542.7fc6d680bKPmjB
 * @author Soong
 */
public class AliOCR {
    private static final String host = "https://tysbgpu.market.alicloudapi.com";
    private static final String path = "/api/predict/ocr_general";
    private static final String method = "POST";
    private static final String appcode = "01b57ce4e82c4e7e9778ef5598238f3f";

    public static void main(String[] args) throws Exception {
        final String result = execute("D:\\data\\docShop\\participle_task\\311\\test.png");
        System.out.println(result);
    }

    public static String execute(String fileName) {

        Map<String, String> headers = new HashMap<>(4);
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/json; charset=UTF-8");
        String result = "";
        Map<String, String> querys = new HashMap<>(4);
//        String bodys = "{\"image\":\"图片二进制数据的base64编码/图片url\",\"configure\":{\"min_size\":16,#图片中文字的最小高度，单位像素\"output_prob\":true,#是否输出文字框的概率\"output_keypoints\":false,#是否输出文字框角点\"skip_detection\":false#是否跳过文字检测步骤直接进行文字识别\"without_predicting_direction\":false#是否关闭文字行方向预测}}";

        String bodies = getRequestBody(fileName);

        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodies);
            System.out.println(response.toString());
            //获取response的body
//            System.out.println(EntityUtils.toString(response.getEntity()));
            result = parse(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String getRequestBody(String imagePath) {
        JSONObject reqJsonBody = new JSONObject();
//        reqJsonBody.put("image", BaseImg64.getImageStrFromPath(imagePath));
        reqJsonBody.put("image", changeToBase64(imagePath));

        JSONObject configure = new JSONObject();
        //图片中文字的最小高度，单位像素
        configure.put("min_size", 16);
        //是否输出文字框的概率
        configure.put("output_prob", true);
        //是否输出文字框角点
        configure.put("output_keypoints", false);
        //是否跳过文字检测步骤直接进行文字识别
        configure.put("skip_detection", false);
        //是否关闭文字行方向预测
        configure.put("without_predicting_direction", false);

        reqJsonBody.put("configure", configure);

        return reqJsonBody.toJSONString();
    }

    public static String changeToBase64(String fileName) {
        File file = new File(fileName);
        String base64Code = null;
        try (FileInputStream inputFile = new FileInputStream(file)) {
            byte[] buffer = new byte[(int)file.length()];
            inputFile.read(buffer);
            base64Code=new BASE64Encoder().encode(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return base64Code;
    }

    /**
     * 解析Json
     * @param string 获取response中的Json
     */
    public static String parse(String string) {
        StringBuilder result = new StringBuilder();
        JSONObject jsonObject = JSONObject.parseObject(string);
        JSONArray jsonArray = jsonObject.getJSONArray("ret");
        for (Object o : jsonArray) {
            JSONObject newJsonObject = (JSONObject) o;
//            output(newJsonObject.getString("word"));
            result.append(newJsonObject.getString("word"));
        }
        return result.toString();
    }

    /**
     * 输出获取的Json返回值，并把它输出到控制台和文件
     * @param content 获取的Json返回值
     */
    public static void output(String content) {
        System.out.println(content);
        File file = new File("D:\\tmp\\OCR\\ocrTests\\test5\\ali_pic5_result.txt");  //这里选择输出文件的地址
        try {
            FileWriter writer = new FileWriter(file, true);
            writer.write(content + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
