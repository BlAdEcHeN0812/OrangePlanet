package com.orangeplanet.zjuhelper;

import com.orangeplanet.zjuhelper.model.Course;
import com.orangeplanet.zjuhelper.service.AuthService;
import com.orangeplanet.zjuhelper.service.CourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.Scanner;

@SpringBootApplication
public class Main implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private CourseService courseService;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("================ H2 DATABASE CONNECTION DETAILS ================");
        logger.info("JDBC URL: {}", dbUrl);
        logger.info("Username: {}", dbUsername);
        logger.info("Password: {}", dbPassword);
        logger.info("=================================================================");

        logger.info("ZJU Helper Application Started");
        
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter Username (Student ID): ");
        String username = scanner.nextLine();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        if (authService.login(username, password)) {
            logger.info("Login successful! You can now access other services.");
            
            logger.info("Fetching course list...");
            try {
                List<Course> courses = courseService.getAndSaveCourseList(username, password);
                if (courses.isEmpty()) {
                    logger.warn("No courses found or parsing failed. Please check the logs for the raw response.");
                } else {
                    logger.info("Successfully fetched and saved {} courses.", courses.size());
                    courses.forEach(course -> logger.info("Saved Course: {} - {}", course.getId(), course.getName()));
                }
            } catch (Exception e) {
                logger.error("Error fetching course list: ", e);
            }

        } else {
            logger.error("Login failed. Please check your credentials.");
        }
        
        // scanner.close(); // Avoid closing System.in in Spring Boot if not necessary
    }
}
