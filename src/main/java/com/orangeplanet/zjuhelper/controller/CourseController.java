package com.orangeplanet.zjuhelper.controller;

import com.orangeplanet.zjuhelper.model.Course;
import com.orangeplanet.zjuhelper.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @PostMapping("/courses")
    public List<Course> getCourses(@RequestParam String username, @RequestParam String password) {
        // 调用你现有的服务逻辑
        return courseService.getAndSaveCourseList(username, password);
    }

    @GetMapping("/my-courses")
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }
}
