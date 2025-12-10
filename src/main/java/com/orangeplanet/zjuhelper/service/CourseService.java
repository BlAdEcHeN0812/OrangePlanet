package com.orangeplanet.zjuhelper.service;
//获取浙大课表的服务层代码
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orangeplanet.zjuhelper.api.ZjuPassportApi;
import com.orangeplanet.zjuhelper.model.Course;
import com.orangeplanet.zjuhelper.repository.CourseRepository;
import com.orangeplanet.zjuhelper.util.HttpClientUtil;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
public class CourseService {
    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);
    private final ZjuPassportApi passportApi;
    private final CourseRepository courseRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public CourseService(ZjuPassportApi passportApi, CourseRepository courseRepository) {
        this.passportApi = passportApi;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public List<Course> getAndSaveCourseList(String username, String password) {
        List<Course> courses = getCourseList(username, password);
        return courseRepository.saveAll(courses);
    }

    public List<Course> getCourseList(String username, String password) {
        // 1. 尝试登录（如果已经登录，API内部会跳过）
        // 注意：如果 Main 中已经登录，这里其实是多余的，但为了保证 Service 独立性保留
        // 如果 ZjuPassportApi 内部没有正确处理重复登录，这里可能会报错
        // 鉴于 Main 中已经明确登录成功，这里我们可以先检查一下 Cookie，或者直接注释掉登录调用
        // 为了稳妥，我们依赖 ZjuPassportApi 的内部检查
        if (!passportApi.login(username, password)) {
            throw new RuntimeException("Login failed");
        }

        try {
            // 2. 模拟 C# 逻辑：访问教务系统登录入口，获取重定向地址
            // 修改为 HTTPS
            String jwLoginUrl = "https://zjuam.zju.edu.cn/cas/login?service=https%3A%2F%2Fzdbk.zju.edu.cn%2Fjwglxt%2Fxtgl%2Flogin_ssologin.html";
            
            String redirectLocation;
            try (CloseableHttpResponse response = HttpClientUtil.doGetForResponse(jwLoginUrl, false)) {
                int statusCode = response.getCode();
                if (statusCode == 302 || statusCode == 301) {
                    Header locationHeader = response.getFirstHeader("Location");
                    if (locationHeader != null) {
                        redirectLocation = locationHeader.getValue();
                        logger.info("Got redirect location: " + redirectLocation);
                    } else {
                        throw new RuntimeException("Redirect location not found");
                    }
                } else {
                    logger.warn("Expected redirect but got status code: " + statusCode);
                    // Fallback to HTTPS
                    redirectLocation = "https://zdbk.zju.edu.cn/jwglxt/xtgl/login_ssologin.html"; 
                }
            }

            // 3. 访问重定向地址，完成教务系统单点登录
            // 使用 doGetForResponse 并禁用自动重定向，因为我们只需要触发 Cookie 设置
            try (CloseableHttpResponse ssoResponse = HttpClientUtil.doGetForResponse(redirectLocation, false)) {
                int ssoStatus = ssoResponse.getCode();
                logger.info("Accessed redirect location. Status: " + ssoStatus);
                
                // 如果是 301/302，尝试跟随跳转
                if (ssoStatus == 301 || ssoStatus == 302) {
                    Header nextLocHeader = ssoResponse.getFirstHeader("Location");
                    if (nextLocHeader != null) {
                        String nextLoc = nextLocHeader.getValue();
                        logger.info("Following SSO redirect to: " + nextLoc);
                        try (CloseableHttpResponse nextResponse = HttpClientUtil.doGetForResponse(nextLoc, false)) {
                            logger.info("Accessed SSO next location. Status: " + nextResponse.getCode());
                        }
                    } else {
                        logger.warn("Redirect status " + ssoStatus + " but no Location header found.");
                    }
                }
            }
            logger.info("Completed SSO validation (cookies updated)");

            // 4. 请求课表数据
            // 使用 HTTPS 地址
            String courseUrl = "https://zdbk.zju.edu.cn/jwglxt/kbcx/xskbcx_cxXsKb.html";
            
            // 构造请求参数
            String year = "2025"; // 示例年份
            String xnm = year + "-" + (Integer.parseInt(year) + 1);
            // 尝试使用 "1|秋" 作为参数
            String xqm = "1|秋"; 
            
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("xnm", xnm));
            params.add(new BasicNameValuePair("xqm", xqm));
            
            logger.info("Fetching course data for " + xnm + " term " + xqm);
            
            String courseHtml = "";
            // 允许自动重定向，以防万一还有跳转
            try (CloseableHttpResponse courseResponse = HttpClientUtil.doPostForResponse(courseUrl, new UrlEncodedFormEntity(params, StandardCharsets.UTF_8), true)) {
                int statusCode = courseResponse.getCode();
                logger.info("Course request status: " + statusCode);
                
                if (statusCode == 200) {
                    HttpEntity entity = courseResponse.getEntity();
                    courseHtml = entity != null ? EntityUtils.toString(entity) : "";
                } else {
                    logger.warn("Course request failed with status: " + statusCode);
                    if (statusCode == 301 || statusCode == 302) {
                         Header loc = courseResponse.getFirstHeader("Location");
                         if (loc != null) logger.warn("Redirected to: " + loc.getValue());
                    }
                }
            } catch (Exception e) {
                logger.error("Error fetching course data", e);
            }

            if (courseHtml != null) {
                logger.info("Full course response:\n{}", courseHtml);
            } else {
                logger.warn("Course response is null.");
            }
            // 5. 解析返回的 JSON
            List<Course> courses = new ArrayList<>();
            try {
                JsonNode root = objectMapper.readTree(courseHtml);
                JsonNode kbList = root.get("kbList");

                if (kbList != null && kbList.isArray()) {
                    for (JsonNode node : kbList) {
                        Course course = new Course();
                        // 生成唯一ID，因为JSON中可能没有单一的主键
                        course.setId(UUID.randomUUID().toString());

                        // 解析 kcb 字段: 课程名称<br>时间周次<br>教师<br>地点
                        if (node.has("kcb")) {
                            String kcb = node.get("kcb").asText();
                            // 移除 HTML 标签以便更清晰，或者直接分割
                            // 格式示例: 健美（初级）<br>秋冬{第1-8周|2节/周}<br>史倩玉<br>紫金港游泳馆（健身房）zwfzwf
                            String[] parts = kcb.split("<br>");
                            if (parts.length > 0) course.setName(parts[0]);
                            if (parts.length > 1) {
                                course.setWeeks(parts[1]); // 这里包含了周次和节数信息
                                String part1 = parts[1]; // 如 "秋冬{第1-8周|2节/周}"

                                // 直接匹配第一个出现的 "数字+节"
                                Pattern pattern = Pattern.compile("\\d+节");
                                Matcher matcher = pattern.matcher(part1);

                                if (matcher.find()) {
                                    course.setPeriod(matcher.group()); // 设置如 "2节"
                                }
                            }
                            if (parts.length > 2) course.setTeacher(parts[2]);
                            if (parts.length > 3) course.setLocation(parts[3]);
                            if (parts.length > 3) {
                            String locationWithZwf = parts[3];
        
                            // 提取 zwf 中间的内容（考试信息）
                            String examInfo = "";
                            Pattern pattern = Pattern.compile("zwf(.*?)zwf");
                            Matcher matcher = pattern.matcher(locationWithZwf);
                            if (matcher.find()) {
                                examInfo = matcher.group(1).trim();
                         }
        
                            // 提取 zwf 之前的内容作为上课地点（去掉所有 zwf 相关内容）
                            String classLocation = locationWithZwf.replaceAll("zwf.*", "").trim();
                            
                            // 设置上课地点和考试信息
                            course.setLocation(classLocation);
                            if (!examInfo.isEmpty()) {
                                course.setTest(examInfo); // 假设 Course 类有 setTest 方法
                            }
                        }
                    }


                        // 尝试获取星期几和节次 (通常字段为 xqj 和 jcs)
                        if (node.has("xqj")) course.setDayOfWeek(node.get("xqj").asText());
                        if (node.has("djj")) course.setStartTime(node.get("djj").asText());
                        
                        // 如果没有从 kcb 解析出名称，尝试直接获取 kcmc
                        if (course.getName() == null && node.has("kcmc")) {
                            course.setName(node.get("kcmc").asText());
                        }
                        
                        // 其他字段映射
                        if (node.has("xf")) course.setCredits(node.get("xf").asText());

                        // 只有当解析出有效课程时才添加
                        if (course.getName() != null) {
                            courses.add(course);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to parse course JSON. Response content might be HTML (e.g. login page). Raw content: {}", courseHtml);
                throw new RuntimeException("Failed to parse course data", e);
            }
            
            return courses;

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch course list", e);
        }
    }
}