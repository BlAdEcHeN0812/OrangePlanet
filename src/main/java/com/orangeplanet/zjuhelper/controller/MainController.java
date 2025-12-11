package com.orangeplanet.zjuhelper.controller;

import com.orangeplanet.zjuhelper.model.Course;
import com.orangeplanet.zjuhelper.service.CourseService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MainController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private TextArea outputArea;

    private final CourseService courseService;

    @Autowired
    public MainController(CourseService courseService) {
        this.courseService = courseService;
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            outputArea.appendText("Please enter both username and password.\n");
            return;
        }

        loginButton.setDisable(true);
        outputArea.appendText("Logging in and fetching courses...\n");

        Task<List<Course>> task = new Task<>() {
            @Override
            protected List<Course> call() throws Exception {
                return courseService.getAndSaveCourseList(username, password);
            }
        };

        task.setOnSucceeded(e -> {
            List<Course> courses = task.getValue();
            Platform.runLater(() -> {
                outputArea.appendText("Successfully fetched " + courses.size() + " courses:\n");
                outputArea.appendText("--------------------------------------------------\n");
                for (Course course : courses) {
                    outputArea.appendText(String.format("ID: %s | Name: %s | Teacher: %s%n",
                            course.getId(), course.getName(), course.getTeacher()));
                }
                outputArea.appendText("--------------------------------------------------\n");
                loginButton.setDisable(false);
            });
        });

        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            Platform.runLater(() -> {
                outputArea.appendText("Error: " + exception.getMessage() + "\n");
                exception.printStackTrace(); // Print to console for debugging
                loginButton.setDisable(false);
            });
        });

        new Thread(task).start();
    }
}
