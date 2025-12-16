package com.orangeplanet.zjuhelper.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orangeplanet.zjuhelper.api.ZjuPassportApi;
import com.orangeplanet.zjuhelper.util.HttpClientUtil;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LearningInZjuService {
    private static final Logger logger = LoggerFactory.getLogger(LearningInZjuService.class);
    private final ZjuPassportApi passportApi;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public LearningInZjuService(ZjuPassportApi passportApi) {
        this.passportApi = passportApi;
    }

    public void login(String username, String password) {
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

    public JsonNode getRollCalls(String courseId) {
        // API for roll calls
        String url = "https://courses.zju.edu.cn/api/courses/" + courseId + "/rollcalls";
        try {
            String json = HttpClientUtil.doGet(url);
            return objectMapper.readTree(json);
        } catch (Exception e) {
            logger.error("Failed to get roll calls for " + courseId, e);
            return null;
        }
    }
}
