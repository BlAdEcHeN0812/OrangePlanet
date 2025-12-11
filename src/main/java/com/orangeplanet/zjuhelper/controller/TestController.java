package com.orangeplanet.zjuhelper.controller;

import com.orangeplanet.zjuhelper.model.Course;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
public class TestController {

    @GetMapping("/api/test-course")
    public List<Course> getTestCourse() {
        Course course = new Course();
        course.setId("123");
        course.setName("Test Course");
        course.setDayOfWeek("星期一");
        course.setStartTime("3");
        course.setPeriodCount("2");
        return Collections.singletonList(course);
    }
}
