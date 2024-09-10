package com.codegym.jira.bugtracking.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskTagService {

    private final TaskTagRepository taskTagRepository;

    @Autowired
    public TaskTagService(TaskTagRepository taskTagRepository) {
        this.taskTagRepository = taskTagRepository;
    }

    public List<TaskTag> getTagsForTask(Task task) {
        return taskTagRepository.findByTask(task);
    }

    public TaskTag addTagToTask(Task task, String tagName) {
        TaskTag tag = new TaskTag(tagName, task);
        return taskTagRepository.save(tag);
    }

    public void removeTagFromTask(TaskTag tag) {
        taskTagRepository.delete(tag);
    }


    public TaskTag findById(Long tagId) {
        Optional<TaskTag> taskTag = taskTagRepository.findById(tagId);
        return taskTag.orElse(null);
    }

}

