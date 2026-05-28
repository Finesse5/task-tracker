package com.tasktracker.service;

import com.tasktracker.dto.AssigneeRequest;
import com.tasktracker.dto.StatusRequest;
import com.tasktracker.dto.TaskRequest;
import com.tasktracker.dto.TaskResponse;
import com.tasktracker.entity.Task;
import com.tasktracker.entity.TaskStatus;
import com.tasktracker.entity.User;
import com.tasktracker.exception.TaskNotFoundException;
import com.tasktracker.repository.TaskRepository;
import com.tasktracker.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public TaskService(TaskRepository taskRepository,
                       UserRepository userRepository,
                       EmailService emailService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public TaskResponse create(TaskRequest req, String ownerEmail) {
        User owner = findUserByEmail(ownerEmail);
        Task task = new Task();
        task.setTitle(req.title());
        task.setDescription(req.description());
        task.setOwner(owner);
        task.setCreatedAt(Instant.now());
        return toResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAll(String ownerEmail) {
        User owner = findUserByEmail(ownerEmail);
        return taskRepository.findAllByOwnerId(owner.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getOne(Long id, String ownerEmail) {
        User owner = findUserByEmail(ownerEmail);
        return toResponse(findOwned(id, owner.getId()));
    }

    public TaskResponse update(Long id, TaskRequest req, String ownerEmail) {
        User owner = findUserByEmail(ownerEmail);
        Task task = findOwned(id, owner.getId());
        task.setTitle(req.title());
        task.setDescription(req.description());
        return toResponse(taskRepository.save(task));
    }

    public TaskResponse changeStatus(Long id, StatusRequest req, String ownerEmail) {
        User owner = findUserByEmail(ownerEmail);
        Task task = findOwned(id, owner.getId());

        // Исправленный блок: используем тернарный оператор вместо switch по boolean
        TaskStatus newStatus = req.done() ? TaskStatus.DONE : TaskStatus.WAITING;

        task.setStatus(newStatus);
        task.setDoneAt(newStatus == TaskStatus.DONE ? Instant.now() : null);
        return toResponse(taskRepository.save(task));
    }

    public TaskResponse changeAssignee(Long id, AssigneeRequest req, String ownerEmail) {
        User owner = findUserByEmail(ownerEmail);
        Task task = findOwned(id, owner.getId());
        User assignee = userRepository.findById(req.assigneeId())
                .orElseThrow(() -> new TaskNotFoundException(req.assigneeId()));
        task.setAssignee(assignee);
        emailService.sendAssigneeChanged(assignee.getEmail(), task.getTitle(), ownerEmail);
        return toResponse(taskRepository.save(task));
    }

    public void delete(Long id, String ownerEmail) {
        User owner = findUserByEmail(ownerEmail);
        Task task = findOwned(id, owner.getId());
        taskRepository.delete(task);
    }

    private Task findOwned(Long taskId, Long ownerId) {
        return taskRepository.findByIdAndOwnerId(taskId, ownerId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("User not found"));
    }

    TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getCreatedAt(),
                task.getDoneAt(),
                task.getOwner().getId(),
                task.getAssignee() != null ? task.getAssignee().getId() : null
        );
    }
}