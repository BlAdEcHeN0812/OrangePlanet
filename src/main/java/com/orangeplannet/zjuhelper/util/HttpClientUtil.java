package com.orangeplannet.zjuhelper.util;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class HttpClientUtil {
    private static CloseableHttpClient httpClient;
    private static BasicCookieStore cookieStore;

    static {
        init();
    }

    private static void init() {
        try {
            // 1. 初始化 CookieStore，用于在多次请求之间保持登录状态（Session）
            cookieStore = new BasicCookieStore();

            // 2. 配置 SSL 信任策略：信任所有证书(但在生产环境中，这样做存在安全风险)。
            TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);

            // 3. 注册 HTTP 和 HTTPS 的连接工厂
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("https", sslsf)
                    .register("http", new PlainConnectionSocketFactory())
                    .build();

            // 4. 创建连接管理器
            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

            // 5. 构建 HttpClient 实例
            httpClient = HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .setDefaultCookieStore(cookieStore) // 绑定 CookieStore
                    // 设置 User-Agent 伪装成浏览器，防止被服务器拦截
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                    .build();

        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new RuntimeException("Failed to initialize HttpClient", e);
        }
    }

    /**
     * 发送 GET 请求
     * @param url 请求地址
     * @return 响应内容的字符串形式
     */
    public static String doGet(String url) throws IOException, ParseException {
        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity);
        }
    }

    /**
     * 发送 POST 请求
     * @param url 请求地址
     * @param entity 请求体（包含表单数据或 JSON）
     * @return 响应内容的字符串形式
     */
    public static String doPost(String url, HttpEntity entity) throws IOException, ParseException {
        return doPost(url, entity, true);
    }

    /**
     * 发送 POST 请求，可配置是否自动重定向
     * @param url 请求地址
     * @param entity 请求体
     * @param allowRedirects 是否允许自动重定向
     * @return 响应内容的字符串形式
     */
    public static String doPost(String url, HttpEntity entity, boolean allowRedirects) throws IOException, ParseException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(entity);
        
        if (!allowRedirects) {
            RequestConfig config = RequestConfig.custom()
                    .setRedirectsEnabled(false)
                    .build();
            httpPost.setConfig(config);
        }

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            HttpEntity responseEntity = response.getEntity();
            return responseEntity != null ? EntityUtils.toString(responseEntity) : "";
        }
    }
    
    /**
     * 获取 CookieStore，用于检查登录状态或获取特定 Cookie
     */
    public static BasicCookieStore getCookieStore() {
        return cookieStore;
    }
}
