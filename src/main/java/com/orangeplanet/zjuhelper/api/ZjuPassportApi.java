package com.orangeplanet.zjuhelper.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orangeplanet.zjuhelper.util.HttpClientUtil;
import com.orangeplanet.zjuhelper.util.RsaEncryptionUtil;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ZjuPassportApi {
    private static final Logger logger = LoggerFactory.getLogger(ZjuPassportApi.class);
    private static final String LOGIN_URL = "https://zjuam.zju.edu.cn/cas/login";       //浙大CAS登录地址
    private static final String PUBKEY_URL = "https://zjuam.zju.edu.cn/cas/v2/getPubKey";   //获取RSA公钥地址
    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean login(String username, String password) {
        try {
            logger.info("Starting login process for user: " + username);

            // 1.获取Execution参数（CAS登录时关键认证参数）
            String html = HttpClientUtil.doGet(LOGIN_URL);
            Document doc = Jsoup.parse(html);   //解析HTML
            String execution = doc.select("input[name=execution]").val();
            if (execution == null || execution.isEmpty()) {
                logger.error("Failed to retrieve execution ID");
                return false;
            }
            logger.info("Got execution ID: " + execution);

            // 2.获取RSA公钥
            String pubKeyJson = HttpClientUtil.doGet(PUBKEY_URL);
            JsonNode pubKeyNode = objectMapper.readTree(pubKeyJson);
            String modulus = pubKeyNode.get("modulus").asText();
            String exponent = pubKeyNode.get("exponent").asText();
            logger.info("Got RSA Public Key");

            // 3.加密密码
            String encryptedPassword = RsaEncryptionUtil.encrypt(password, modulus, exponent);

            // 4.构造提交登录表单
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", encryptedPassword));
            params.add(new BasicNameValuePair("authcode", ""));
            params.add(new BasicNameValuePair("execution", execution));
            params.add(new BasicNameValuePair("_eventId", "submit"));

            // 提交 POST 请求，禁用自动重定向，以避免 CircularRedirectException
            // 登录成功后，服务器通常会返回 302 重定向，我们只需要检查 Cookie 即可
            String responseHtml = HttpClientUtil.doPost(LOGIN_URL, new UrlEncodedFormEntity(params), false);
            
            // 5.检查登录结果
            boolean isLoggedIn = HttpClientUtil.getCookieStore().getCookies().stream()
                    .anyMatch(cookie -> cookie.getName().equals("iPlanetDirectoryPro"));    // 如果存在iPlanetDirectoryPro cookie，说明登录成功
            
            if (isLoggedIn) {
                logger.info("Login Successful!");
                return true;
            } else {
                logger.error("Login Failed. Response might contain error message.");
                // Optional: Parse responseHtml for error message
                return false;
            }

        } catch (IOException | ParseException e) {
            logger.error("Login process failed with exception", e);
            return false;
        }
    }

}
