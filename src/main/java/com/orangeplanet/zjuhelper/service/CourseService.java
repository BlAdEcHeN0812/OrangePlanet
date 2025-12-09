package com.orangeplanet.zjuhelper.service;
//获取浙大课表的服务层代码
import com.orangeplanet.zjuhelper.api.ZjuPassportApi;
import com.orangeplanet.zjuhelper.model.Course;
import com.orangeplanet.zjuhelper.util.HttpClientUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CourseService {

    private final ZjuPassportApi passportApi;

    @Autowired
    public CourseService(ZjuPassportApi passportApi) {
        this.passportApi = passportApi;
    }

    public List<Course> getCourseList(String username, String password) {
        // 1. 先登录
        if (!passportApi.login(username, password)) {
            throw new RuntimeException("Login failed");
        }

        // 2. 请求教务系统课表页面（假设地址）
        String timetableUrl = "https://jwgl.zju.edu.cn/timetable"; // 示例地址
        String html;
        try {
            html = HttpClientUtil.doGet(timetableUrl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch course list", e);
        }

        Document doc = Jsoup.parse(html);
        Elements courseElements = doc.select(".course-item"); // 假设 CSS 选择器

        List<Course> courses = new ArrayList<>();
        for (var element : courseElements) {
            Course course = new Course();
            course.setId(element.select(".id").text());
            course.setName(element.select(".name").text());
            course.setTeacher(element.select(".teacher").text());
            course.setLocation(element.select(".location").text());
            course.setDayOfWeek(element.select(".day").text());
            course.setTimeSlot(element.select(".time").text());
            course.setWeeks(element.select(".weeks").text());
            course.setCredits(element.select(".credits").text());
            course.setTest(element.select(".exam").text());

            courses.add(course);
        }

        return courses;
    }
}