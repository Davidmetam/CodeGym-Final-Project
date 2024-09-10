package com.codegym.jira.bugtracking.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TaskTagRepository extends JpaRepository<TaskTag, Long> {
    List<TaskTag> findByTask(Task task);
}

