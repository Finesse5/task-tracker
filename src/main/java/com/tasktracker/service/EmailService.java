package com.tasktracker.service;

import com.tasktracker.entity.Task;
import com.tasktracker.entity.TaskStatus;
import com.tasktracker.repository.TaskRepository;
import com.tasktracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:}")
    private String from;

    public EmailService(JavaMailSender mailSender,
                        UserRepository userRepository,
                        TaskRepository taskRepository) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    public void sendWelcome(String to) {
        if (!mailEnabled) return;
        send(to, "Добро пожаловать в Task Tracker!",
                "Вы успешно зарегистрировались. Приятной работы!");
    }

    public void sendAssigneeChanged(String to, String taskTitle, String assigneeEmail) {
        if (!mailEnabled) return;
        send(to, "Вы назначены исполнителем задачи",
                "Задача: " + taskTitle + "\nВас назначил: " + assigneeEmail);
    }

    @Scheduled(cron = "0 0 9 * * *", zone = "Europe/Moscow")
    public void sendDailyReport() {
        if (!mailEnabled) return;
        userRepository.findAll().forEach(user -> {
            List<Task> done = taskRepository.findAllByOwnerIdAndStatus(user.getId(), TaskStatus.DONE);
            List<Task> waiting = taskRepository.findAllByOwnerIdAndStatus(user.getId(), TaskStatus.WAITING);
            String body = "Ежедневный отчёт:\n" +
                    "Выполнено задач: " + done.size() + "\n" +
                    "Ожидает выполнения: " + waiting.size();
            send(user.getEmail(), "Ежедневный отчёт по задачам", body);
        });
    }

    private void send(String to, String subject, String text) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        mailSender.send(msg);
    }
}
