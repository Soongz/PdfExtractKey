package baiduocr;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * 图像文字识别服务类
 * infer: https://blog.csdn.net/qq_30515213/article/details/102897601
 * @Author : yangmin
 */
public class BaiduOCRTest {
    //百度图像识别访问地址，在开发者中心的  通用服务列表获取，一般为固定格式
    private static final String POST_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic?access_token=";

    /**
     * 识别本地图片的文字
     *
     * @param path 本地图片地址
     * @return 识别结果，为json格式
     * @throws URISyntaxException URI打开异常
     * @throws IOException        io流异常
     */
    public static String checkFile(String path) throws URISyntaxException, IOException {
        File file = new File(path);
        if (!file.exists()) {
            throw new NullPointerException("图片不存在");
        }
        String image = BaseImg64.getImageStrFromPath(path);
        String param = "image=" + image;
        return post(param);
    }

    /**
     * @param url 图片url
     * @return 识别结果，为json格式
     */
    public static String checkUrl(String url) throws IOException, URISyntaxException {
        String param = "url=" + url;
        return post(param);
    }

    /**
     * 通过传递参数：url和image进行文字识别
     *
     * @param param 区分是url还是image识别
     * @return 识别结果
     * @throws URISyntaxException URI打开异常
     * @throws IOException        IO流异常
     */
    private static String post(String param) throws URISyntaxException, IOException {
        //开始搭建post请求
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost();
        String posturl =   POST_URL+AuthService.getAuth();
        URI url = new URI(posturl);
        System.out.println("组装后的请求地址："+posturl);
        post.setURI(url);
        //设置请求头，请求头必须为application/x-www-form-urlencoded，因为是传递一个很长的字符串，不能分段发送
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        StringEntity entity = new StringEntity(param);
        post.setEntity(entity);
        HttpResponse response = httpClient.execute(post);
        System.out.println(response.toString());
        if (response.getStatusLine().getStatusCode() == 200) {
            String str;
            try {
                /*读取服务器返回过来的json字符串数据*/
                str = new String(EntityUtils.toString(response.getEntity(), "utf-8"));

                StringBuilder result = new StringBuilder();
                JSONArray jsonArray = (JSONArray) JSONObject.parseObject(str).get("words_result");
                for (Object o : jsonArray) {
                    JSONObject sub = (JSONObject) o;
                    result.append(sub.getString("words") + " ");
                }

                System.out.println(str);
                return str;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public static void main(String[] args) {
//        System.out.println("统一冰红茶");
//        String json = "{\"words_result\":[{\"words\":\"2、安装和运行 rabbitry\"},{\"words\":\"http://www.rabbitmg.com/install-windows.html#run-windows\"},{\"words\":\"下载: rabbitmq- server-37.7.exe\"},{\"words\":\"rabbit-server-377eXe,这个东西一看就是在 windows上的一个一键式安装 rabbitry的安装\"},{\"words\":\"工具,双击他,会显示一个界面,然后下一步下一步下一步,就可以通过这个东西装好\"},{\"words\":\"rabbitmq\"},{\"words\":\"双击: rabbitmq- server-37.7exe,直接会完成 rabbitry的安装,将其安装为 windows的一个\"},{\"words\":\"服务,而且会直接以默认的配置来启动 rabbitry\"},{\"words\":\"刚开始使用用默认的环境变量来运行 rabbitmq就k了,但是你也可以定制化修改 rabbitry\"},{\"words\":\"的环境变量,对于我们来说,刚开始使用,其实不用调整他的环境变量\"},{\"words\":\"3、 rabbitry服务的管理\"},{\"words\":\"rabbitmq作为一个 windows服务默认就安装完就启动了,后面如果停止、重启 rabbitmq服\"},{\"words\":\"务,直接在我刚才给大家演示的那个 windows服务界面就可以操作\"}],\"log_id\":1363747334264979456,\"words_result_num\":13}";
//        StringBuilder result = new StringBuilder();
//        JSONArray jsonArray = (JSONArray) JSONObject.parseObject(json).get("words_result");
//        for (Object o : jsonArray) {
//            JSONObject sub = (JSONObject) o;
//            result.append(sub.getString("words") + " ");
//        }
//        System.out.println(result.toString());
        String path = "D:\\reg.png";
        try {
            long now = System.currentTimeMillis();
            checkFile(path);
            System.out.println("耗时：" + (System.currentTimeMillis() - now) / 1000 + "s");
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }
}