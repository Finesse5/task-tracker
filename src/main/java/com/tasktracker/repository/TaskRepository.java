package com.tasktracker.repository;

import com.tasktracker.entity.Task;
import com.tasktracker.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByOwnerId(Long ownerId);
    Optional<Task> findByIdAndOwnerId(Long id, Long ownerId);
    List<Task> findAllByOwnerIdAndStatus(Long ownerId, TaskStatus status);
}
