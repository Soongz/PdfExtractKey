package script;

import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Description: tess4j
 *
 * @author Soong
 */
public class DocClassify {
    private final static String SPERATOR = ",";

    public static void main(String[] args) {
        DocClassify docClassify = new DocClassify();
        docClassify.execute("D:\\data\\docShop\\0319 所有文件\\allFiles");
    }

    public void execute(String path) {
        File root = new File(path);
        LinkedList<String> failedList = new LinkedList<>();
        StringBuilder eachDirResult = new StringBuilder("文件, 一级分类, 二级分类").append("\n");
        // 如果这个路径是文件夹
        if (root.isDirectory()) {
            // 获取路径下的所有文件
            File[] files = root.listFiles();
            assert files != null;
            for (File file : files) {
                if (file.isDirectory()) {
                    execute(file.getPath());
                } else {
                    try {
                        System.out.println("handling file: " + file.getPath());
                        final String fileResult = process(file);
                        eachDirResult.append(fileResult).append("\n");
                    } catch (Exception e) {
                        failedList.add(file.getPath());
                        System.out.println("counting error, skip...");
                    }
                }
            }
            String resultDesc = "共失败" + failedList.size() + "个, " + failedList.toString();
            System.out.println(resultDesc);
            if (!CollectionUtils.isEmpty(failedList)) {
                KeyStatistics.flushStringTodisk(resultDesc, root.getPath() + "\\result\\_failedList.txt");
            }
            if (eachDirResult.length() > 20) {
                KeyStatistics.flushStringTodisk(eachDirResult.toString(), root.getPath() + "\\result\\classify.csv");
            }
            eachDirResult = new StringBuilder("文件, 一级分类, 二级分类").append("\n");
            failedList.clear();
        }
    }


    private String process(File file) throws IOException {
        StringBuilder result = new StringBuilder(file.getName());
        if (file.getName().contains(".doc") || file.getName().contains(".xls")) return null;
        //1. extract content
        final String content = file.getName() + KeyStatistics.fileExtractString(file.getPath());
        //2. analyze classification(first, sencond)
        final String classifyResult = classifyExec(file.getName(), content);
        //3. make up result
        result.append(",").append(classifyResult);
        return result.toString();
    }

    /**
     * analyze the classification of the file
     *
     * @param fileName file name
     * @param content  content of the file
     * @return "政策法规, 政策解读" ==> "firstClass, SecondClass"
     */
    private String classifyExec(String fileName, String content) {

        StringBuilder result = new StringBuilder();
        String first = null;
        String second = null;
        for (Classification classification : PrincipleClass.classifications) {
            first = classification.isFirst(fileName);
            if (first == null) continue;
            second = classification.isSecond(content);
            break;
        }
        result.append(first == null ? "其他" : first).append(SPERATOR).append(second == null ? "其他" : second);
        return result.toString();
    }

    static class Classification {
        String name;
        List<String> cls;
        List<Classification> next;

        public Classification(String name, List<String> cls) {
            this.name = name;
            this.cls = cls;
        }

        public Classification(String name, List<String> cls, List<Classification> next) {
            this.name = name;
            this.cls = cls;
            this.next = next;
        }

        public String isFirst(String titleName) {
            return containsAtList(titleName, cls) ? name : null;
        }

        public String isSecond(String content) {

            //特殊情况
            if ("政策法规".equals(name) || content.contains("解读")) {
                return "政策解读";
            }

            if (CollectionUtils.isEmpty(next)) return null;
            for (Classification classification : next) {
                final boolean isContains = containsAtList(content, classification.cls);
                if (isContains) return classification.name;
            }
            return null;
        }

        public static boolean containsAtList(String value, List<String> strings) {
            for (String element : strings) {
                if (value.contains(element)) {
                    return true;
                }
            }
            return false;
        }

    }


    static class PrincipleClass {

        public static List<Classification> classifications = new ArrayList<>(8);

        public static final List<String> F_ZHENGCEFAGUI = Arrays.asList("决议", "决定", "命令", "令", "公报", "公告", "通告", "意见", "通知", "通报", "报告", "请示", "批复", "议案", "函", "纪要", "条例", "规定", "办法");
        public static final List<String> F_YANJIUBAOGAO = Arrays.asList("案例", "分析", "预测", "总结", "预判", "报告", "快报", "研究", "行业", "动态", "业绩公告", "研判", "统计公报", "点评", "策略", "摘要", "调查", "方案", "总结", "要求", "展望", "解析", "洞察", "观点", "动态报告", "简报", "资讯", "探讨", "梳理", "解读", "起底", "追踪", "观察", "市场");
        public static final List<String> F_XIANGMUYUNYING = Arrays.asList("项目", "运营", "经营", "策划", "管理", "融资", "土地", "规划", "报建", "物业", "投资", "决策");
        public static final List<String> F_CHENGBENGUANKONG = Arrays.asList("成本", "采购", "供应商", "销售", "工程", "开发", "培训", "管控");
        public static final List<String> F_YINGXIAOTUIGUANG = Arrays.asList("营销", "推广", "策划");
        public static final List<String> F_DICHANSHEJI = Arrays.asList("设计", "景观", "图纸", "图集", "户型", "鉴赏", "欣赏", "建筑");
        public static final List<String> F_TONGYONGGONGJU = Arrays.asList("范文", "范本", "模板", "手册", "指南", "协议", "教材", "表格", "表", "报表", "表单", "示例", "制度", "声明", "流程", "合同", "规范", "模型", "模块", "示范", "教程", "大全", "汇编", "清单", "单", "说明", "问卷", "图表", "样本", "规程", "指引", "要求", "述职");


        public static final List<String> S_ZHENGCEFAGUI = Arrays.asList("决议", "决定", "命令", "令", "公报", "公告", "通告", "意见", "通知", "通报", "报告", "请示", "批复", "议案", "函", "纪要", "条例", "规定", "办法");
        public static final List<String> S_ZHENGCEJIEDU = Arrays.asList("决议", "决定", "命令", "令", "公报", "公告", "通告", "意见", "通知", "通报", "报告", "请示", "批复", "议案", "函", "纪要", "条例", "规定", "办法", "解读");
        public static final List<String> S_HANGYESHICHANG = Arrays.asList("市场", "楼市", "年", "半年", "季", "月", "周", "日报", "入市", "周刊月刊季刊年刊", "总结", "楼市", "风险", "房企", "金融", "养老", "康养", "不动产", "快报", "行业", "开盘", "点评", "证券", "观察", "商业", "分析");
        public static final List<String> S_QUANSHANGZHISHU = Arrays.asList("指数", "数据", "LPR", "指标", "价格", "地价", "动态监测", "测量", "IPO");
        public static final List<String> S_ANLIFENXI = Arrays.asList("带有企业名称", "社区", "带有城市名称", "国有企业", "带有项目名称", "物业", "企业", "服务", "集团", "可研", "可行性研究", "调研", "案例", "分析", "战略", "标准化", "产品", "方案");
        public static final List<String> S_DIAOCHAYANJIU = Collections.emptyList();
        public static final List<String> S_QIANQIKEYAN = Arrays.asList("可行性研究", "策划", "调研", "定位", "考察", "测算", "估算", "评估", "招商", "开发");
        public static final List<String> S_DICHANRONGZI = Arrays.asList("融资", "贷款", "债券", "债务", "现金", "银行", "资金", "投资");
        public static final List<String> S_TUDIJIAOYI = Arrays.asList("拿地", "取地", "均价", "金额", "区域", "土地", "招拍", "竞标", "产权", "地价", "成交", "出让", "拍卖", "招标");
        public static final List<String> S_GUIHUABAOJIAN = Arrays.asList("报批", "报建", "流程", "前期", "资料", "工程", "规划", "手续", "设计", "操作", "定位");
        public static final List<String> S_WUYEGUANLI = Arrays.asList("服务", "物管", "物业", "业主", "园区", "生活");
        public static final List<String> S_CHENGBENGUANLI = Arrays.asList("成本", "控制", "精细化", "谈判", "报价", "估算", "预算", "核算");
        public static final List<String> S_CAIGOUGUANLI = Arrays.asList("采购", "材料", "装修", "建材", "供材");
        public static final List<String> S_XIAOSHOUGUANLI = Arrays.asList("案场", "滞销", "热销", "脱销", "操盘", "开盘", "执行", "技巧", "交易");
        public static final List<String> S_GONGCHENGGUANGLI = Arrays.asList("建筑", "造价", "开发", "改造", "建设");
        public static final List<String> S_YINGXIAOCEHUA = Arrays.asList("包装", "营销", "策划", "策略", "活动", "病毒式", "分销", "渠道", "蓄客", "促销", "联动", "策动", "发布会", "品牌", "动销", "宣传", "全案", "提案");
        public static final List<String> S_TUIGUANGFNAGAN = Arrays.asList("推广", "创意", "思路", "案例", "整合");
        public static final List<String> S_HUXINGSHEJI = Arrays.asList("户型", "住宅", "风水", "排屋", "别墅", "赠送面积", "偷面积", "样板间", "空间", "公寓", "联排");
        public static final List<String> S_JIANZHUZHUANGXIU = Arrays.asList("装修", "装饰", "建筑", "风格", "施工", "地基", "精装", "室内");
        public static final List<String> S_XIANGMUGUIHUA = Arrays.asList("城市", "规划", "都市", "区域", "概念", "构想");
        public static final List<String> S_YUANLISHEJI = Arrays.asList("景观", "园林", "绿化", "造景", "植物");
        public static final List<String> S_QITASHEJI = Arrays.asList("车库", "地下室", "其他无法分类的设计资料");
        public static final List<String> S_CHANGYONGBIAOGE = Arrays.asList("表", "报表", "表格", "表单", "通知书", "申请书", "测算", "标准", "模块", "清单", "单", "问卷", "流程", "图表");
        public static final List<String> S_CHANGYONGHETONG = Arrays.asList("合同", "协议", "声明", "招标", "标书", "投标", "书");
        public static final List<String> S_ZHIDULIUCHENG = Arrays.asList("指南", "制度", "指导", "办法", "流程", "要求", "标准", "手册", "规程", "指引", "说明", "模板", "标准化");
        public static final List<String> S_RENLIZIYUAN = Arrays.asList("培训", "员工", "入职", "薪酬", "年终总结", "绩效", "求职", "招聘", "职业", "人力资源", "面试", "员工手册", "薪资", "述职", "组织架构");
        public static final List<String> S_QITAZILIAO = Collections.emptyList();


        static {
            Classification c1a = new Classification("政策法规", S_ZHENGCEFAGUI);
            Classification c1b = new Classification("政策解读", S_ZHENGCEJIEDU);
            Classification c1 = new Classification("政策法规", F_ZHENGCEFAGUI, Arrays.asList(c1a, c1b));
            classifications.add(c1);

            Classification c2a = new Classification("行业市场", S_HANGYESHICHANG);
            Classification c2b = new Classification("券商指数", S_QUANSHANGZHISHU);
            Classification c2c = new Classification("案例分析", S_ANLIFENXI);
            Classification c2d = new Classification("调查研究", S_DIAOCHAYANJIU);
            Classification c2 = new Classification("研究报告", F_YANJIUBAOGAO, Arrays.asList(c2a, c2b, c2c, c2d));
            classifications.add(c2);


            Classification c3a = new Classification("前期可研", S_QIANQIKEYAN);
            Classification c3b = new Classification("地产融资", S_DICHANRONGZI);
            Classification c3c = new Classification("土地交易", S_TUDIJIAOYI);
            Classification c3d = new Classification("规划报建", S_GUIHUABAOJIAN);
            Classification c3f = new Classification("物业管理", S_WUYEGUANLI);
            Classification c3 = new Classification("项目运营", F_XIANGMUYUNYING, Arrays.asList(c3a, c3b, c3c, c3d, c3f));
            classifications.add(c3);


            Classification c4a = new Classification("成本管理", S_CHENGBENGUANLI);
            Classification c4b = new Classification("采购管理", S_CAIGOUGUANLI);
            Classification c4c = new Classification("销售管理", S_XIAOSHOUGUANLI);
            Classification c4d = new Classification("工程管理", S_GONGCHENGGUANGLI);
            Classification c4 = new Classification("成本管控", F_CHENGBENGUANKONG, Arrays.asList(c4a, c4b, c4c, c4d));
            classifications.add(c4);

            Classification c5a = new Classification("营销策划", S_YINGXIAOCEHUA);
            Classification c5b = new Classification("推广方案", S_TUIGUANGFNAGAN);
            Classification c5 = new Classification("营销推广", F_YINGXIAOTUIGUANG, Arrays.asList(c5a, c5b));
            classifications.add(c5);

            Classification c6a = new Classification("户型设计", S_HUXINGSHEJI);
            Classification c6b = new Classification("建筑装修", S_JIANZHUZHUANGXIU);
            Classification c6c = new Classification("项目规划", S_XIANGMUGUIHUA);
            Classification c6d = new Classification("园林设计", S_YUANLISHEJI);
            Classification c6f = new Classification("其他设计", S_QITASHEJI);
            Classification c6 = new Classification("地产设计", F_DICHANSHEJI, Arrays.asList(c6a, c6b, c6c, c6d, c6f));
            classifications.add(c6);

            Classification c7a = new Classification("常用表格", S_CHANGYONGBIAOGE);
            Classification c7b = new Classification("常用合同", S_CHANGYONGHETONG);
            Classification c7c = new Classification("制度流程", S_ZHIDULIUCHENG);
            Classification c7d = new Classification("人力资源", S_RENLIZIYUAN);
            Classification c7f = new Classification("其他资料", S_QITAZILIAO);
            Classification c7 = new Classification("通用工具", F_TONGYONGGONGJU, Arrays.asList(c7a, c7b, c7c, c7d, c7f));
            classifications.add(c7);
        }
    }

}
