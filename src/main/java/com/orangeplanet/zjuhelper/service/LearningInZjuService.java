package com.orangeplanet.zjuhelper.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orangeplanet.zjuhelper.api.ZjuPassportApi;
import com.orangeplanet.zjuhelper.util.HttpClientUtil;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class LearningInZjuService {
    private static final Logger logger = LoggerFactory.getLogger(LearningInZjuService.class);
    private final ZjuPassportApi passportApi;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String currentUserId;

    @Autowired
    public LearningInZjuService(ZjuPassportApi passportApi) {
        this.passportApi = passportApi;
    }

    public void login(String username, String password) {
        this.currentUserId = null;
        // 1. Ensure ZJU Passport login
        if (!passportApi.login(username, password)) {
            throw new RuntimeException("ZJU Passport Login failed");
        }
    }

    public JsonNode getMyCourses() {
        String url = "https://courses.zju.edu.cn/api/my-courses";
        try {
            String json = HttpClientUtil.doGet(url);
            return objectMapper.readTree(json);
        } catch (Exception e) {
            logger.error("Failed to get my courses", e);
            throw new RuntimeException("Failed to get my courses", e);
        }
    }

    public JsonNode getCourseActivities(String courseId) {
        // Based on Login.cs: https://courses.zju.edu.cn/api/activities/{courseId}?sub_course_id=0
        String url = "https://courses.zju.edu.cn/api/activities/" + courseId + "?sub_course_id=0";
        try {
            String json = HttpClientUtil.doGet(url);
            return objectMapper.readTree(json);
        } catch (Exception e) {
            logger.error("Failed to get course activities for " + courseId, e);
            throw new RuntimeException("Failed to get course activities", e);
        }
    }
    
    public JsonNode getCourseUploads(String courseId) {
        // Try to get activities which usually contain the courseware/uploads
        String url = "https://courses.zju.edu.cn/api/courses/" + courseId + "/activities";
        try {
            String json = HttpClientUtil.doGet(url);
            return objectMapper.readTree(json);
        } catch (Exception e) {
            logger.error("Failed to get course activities/uploads for " + courseId, e);
            return null;
        }
    }

    public String getCurrentUserId() {
        return "246456";
    }

    public JsonNode getRollCalls(String courseId) {
        // Use the module specific endpoint to get roll calls
        String url = "https://courses.zju.edu.cn/api/courses/" + courseId + "/modules/rollcalls";
        try {
            String json = HttpClientUtil.doGet(url);
            
            return objectMapper.readTree(json);
        } catch (Exception e) {
            logger.error("Failed to get roll calls for " + courseId, e);
            return null;
        }
    }

    public CloseableHttpResponse downloadFile(String url) throws java.io.IOException {
        return HttpClientUtil.doGetForResponse(url, true);
    }
}
