package com.orangeplanet.zjuhelper.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.orangeplanet.zjuhelper.service.LearningInZjuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}
