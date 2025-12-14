package com.orangeplanet.zjuhelper.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "courses")
public class Course {
    @Id
    private String id;          // 课程代码
    private String name;        // 课程名称
    private String teacher;     // 教师姓名
    private String location;    // 上课地点
    private String dayOfWeek;   // 星期几 
    private String startTime;    // 节次 
    private String weeks;       // 周次 
    private String credits;     // 学分
    private String test;        // 考试
    private String periodCount; // 节数

    public Course() {
    }

    public Course(String id, String name, String teacher, String location, String dayOfWeek, String startTime, String weeks, String credits, String test) {
        this.id = id;
        this.name = name;
        this.teacher = teacher;
        this.location = location;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.weeks = weeks;
        this.credits = credits;
        this.test = test;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getWeeks() {
        return weeks;
    }

    public void setWeeks(String weeks) {
        this.weeks = weeks;
    }

    public String getCredits() {
        return credits;
    }

    public void setCredits(String credits) {
        this.credits = credits;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getPeriodCount() {
        return periodCount;
    }

    public void setPeriodCount(String periodCount) {
        this.periodCount = periodCount;
    }
}