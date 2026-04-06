package com.rq.cloudpicturebackend.utill;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BingImageByOffset {

    private static final String BASE_URL = "https://cn.bing.com";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 根据关键词、偏移量、每页数量，获取该页的原始图片地址列表
     *
     * @param keyword 搜索关键词（中文即可，方法内自动编码）
     * @param first   起始索引（从0开始）
     * @param count   每页数量（建议35）
     * @return 原图URL列表
     */
    public static List<String> getImageUrlsByOffset(String keyword, int first, int count) throws IOException {
        // 1. 构造请求URL（使用您提供的URL作为模板，替换first和count）
        String urlTemplate = BASE_URL + "/images/async?q={q}&first={first}&count={count}&mmasync=1";
        String encodedKeyword = URLEncoder.encode(keyword, "UTF-8");
        String url = urlTemplate.replace("{q}", encodedKeyword)
                .replace("{first}", String.valueOf(first))
                .replace("{count}", String.valueOf(count));

        // 2. 发送请求
        String html = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .ignoreContentType(true)
                .execute()
                .body();

        // 3. 解析HTML，提取原图地址
        return extractOriginalImageUrls(count, html);
    }

    /**
     * 从返回的HTML片段中提取所有原始图片地址（murl）
     */
    private static List<String> extractOriginalImageUrls(int count, String html) {
        List<String> urls = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements items = doc.select("a.iusc");
        for (Element item : items) {
            String mAttr = item.attr("m");
            if (StrUtil.isBlank(mAttr)) {
                continue;
            }
            try {
                @SuppressWarnings("unchecked")
                Map<String, String> data = mapper.readValue(mAttr, Map.class);
                String murl = data.get("murl");
                if (murl != null && !murl.isEmpty()) {
                    urls.add(murl);
                }
            } catch (Exception e) {
                System.err.println("解析m属性失败: " + mAttr);
            }
            if (count == urls.size()) {
                break;
            }
        }
        return urls;
    }

    // 测试
    public static void main(String[] args) {
        try {
            // 示例：获取 first=71 的一页（与您提供的URL相同）
            List<String> urls = getImageUrlsByOffset("世界旅游胜地", 1, 1);
            System.out.println("共获取到 " + urls.size() + " 张图片");
            urls.stream().limit(5).forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}