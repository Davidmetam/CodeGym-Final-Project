package com.codegym.jira.bugtracking.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/tags")
public class TaskTagController {

    private final TaskTagService taskTagService;
    private final TaskService taskService;

    @Autowired
    public TaskTagController(TaskTagService taskTagService, TaskService taskService) {
        this.taskTagService = taskTagService;
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskTag> getTags(@PathVariable Long taskId) {
        Task task = taskService.findById(taskId);
        return taskTagService.getTagsForTask(task);
    }

    @PostMapping
    public TaskTag addTag(@PathVariable Long taskId, @RequestBody String tagName) {
        Task task = taskService.findById(taskId);
        return taskTagService.addTagToTask(task, tagName);
    }

    @DeleteMapping("/{tagId}")
    public void removeTag(@PathVariable Long tagId) {
        TaskTag tag = taskTagService.findById(tagId);
        taskTagService.removeTagFromTask(tag);
    }
}

