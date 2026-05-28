package com.tasktracker.service;

import com.tasktracker.dto.StatusRequest;
import com.tasktracker.dto.TaskRequest;
import com.tasktracker.dto.TaskResponse;
import com.tasktracker.entity.Task;
import com.tasktracker.entity.TaskStatus;
import com.tasktracker.entity.User;
import com.tasktracker.exception.TaskNotFoundException;
import com.tasktracker.repository.TaskRepository;
import com.tasktracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock TaskRepository taskRepository;
    @Mock UserRepository userRepository;
    @Mock EmailService emailService;
    @InjectMocks TaskService taskService;

    private User owner;
    private Task task;

    @BeforeEach
    void setUp() {
        owner = new User("owner@example.com", "hashed");
        owner.setId(1L);

        task = new Task();
        task.setId(10L);
        task.setTitle("Test task");
        task.setStatus(TaskStatus.WAITING);
        task.setCreatedAt(Instant.now());
        task.setOwner(owner);
    }

    @Test
    void create_success() {
        var req = new TaskRequest("New task", "desc");
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(taskRepository.save(any())).thenAnswer(i -> {
            Task t = i.getArgument(0);
            t.setId(1L);
            return t;
        });

        TaskResponse resp = taskService.create(req, "owner@example.com");

        assertThat(resp.title()).isEqualTo("New task");
        assertThat(resp.status()).isEqualTo(TaskStatus.WAITING);
    }

    @Test
    void getAll_returnsOnlyOwnerTasks() {
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(taskRepository.findAllByOwnerId(1L)).thenReturn(List.of(task));

        List<TaskResponse> tasks = taskService.getAll("owner@example.com");

        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).title()).isEqualTo("Test task");
    }

    @Test
    void changeStatus_toDone_setsDoneAt() {
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(taskRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TaskResponse resp = taskService.changeStatus(10L, new StatusRequest(true), "owner@example.com");

        assertThat(resp.status()).isEqualTo(TaskStatus.DONE);
        assertThat(resp.doneAt()).isNotNull();
    }

    @Test
    void changeStatus_toWaiting_clearsDoneAt() {
        task.setStatus(TaskStatus.DONE);
        task.setDoneAt(Instant.now());
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(taskRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TaskResponse resp = taskService.changeStatus(10L, new StatusRequest(false), "owner@example.com");

        assertThat(resp.status()).isEqualTo(TaskStatus.WAITING);
        assertThat(resp.doneAt()).isNull();
    }

    @Test
    void getOne_notOwned_throwsNotFound() {
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(taskRepository.findByIdAndOwnerId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getOne(99L, "owner@example.com"))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void delete_success() {
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(taskRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(task));

        taskService.delete(10L, "owner@example.com");

        verify(taskRepository).delete(task);
    }
}
