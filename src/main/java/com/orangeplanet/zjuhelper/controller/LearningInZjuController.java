package com.orangeplanet.zjuhelper.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.orangeplanet.zjuhelper.service.LearningInZjuService;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api/learning")
public class LearningInZjuController {

    @Autowired
    private LearningInZjuService learningService;

    @PostMapping("/login")
    public void login(@RequestParam String username, @RequestParam String password) {
        learningService.login(username, password);
    }

    @GetMapping("/courses")
    public JsonNode getMyCourses() {
        return learningService.getMyCourses();
    }

    @GetMapping("/courses/{courseId}/activities")
    public JsonNode getCourseActivities(@PathVariable String courseId) {
        return learningService.getCourseActivities(courseId);
    }
    
    @GetMapping("/courses/{courseId}/uploads")
    public JsonNode getCourseUploads(@PathVariable String courseId) {
        return learningService.getCourseUploads(courseId);
    }

    @GetMapping("/courses/{courseId}/rollcalls")
    public JsonNode getRollCalls(@PathVariable String courseId) {
        return learningService.getRollCalls(courseId);
    }

    @GetMapping("/file/proxy")
    public void proxyFile(@RequestParam String url, HttpServletResponse response) {
        try (CloseableHttpResponse proxyResponse = learningService.downloadFile(url)) {
            HttpEntity entity = proxyResponse.getEntity();
            if (entity != null) {
                response.setStatus(proxyResponse.getCode());
                Header contentType = proxyResponse.getFirstHeader("Content-Type");
                if (contentType != null) {
                    response.setContentType(contentType.getValue());
                }
                Header contentDisposition = proxyResponse.getFirstHeader("Content-Disposition");
                if (contentDisposition != null) {
                    response.setHeader("Content-Disposition", contentDisposition.getValue());
                }
                
                entity.writeTo(response.getOutputStream());
            }
        } catch (IOException e) {
            response.setStatus(500);
        }
    }
}
