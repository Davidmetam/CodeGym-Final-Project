package com.codegym.jira.bugtracking.task;

import com.codegym.jira.common.HasCode;
import com.codegym.jira.common.model.TitleEntity;
import com.codegym.jira.common.util.validation.Code;
import com.codegym.jira.bugtracking.project.Project;
import com.codegym.jira.bugtracking.sprint.Sprint;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.codegym.jira.bugtracking.task.TaskUtil.checkStatusChangePossible;

@Entity
@Table(name = "task")
@Getter
@Setter
@NoArgsConstructor
public class Task extends TitleEntity implements HasCode {
    // title, typeCode, statusCode duplicated here and in Activity for sql simplicity

    // link to Reference.code with RefType.TASK
    @Code
    @Column(name = "type_code", nullable = false)
    private String typeCode;

    // link to Reference.code with RefType.TASK_STATUS
    @Code
    @Column(name = "status_code", nullable = false)
    private String statusCode;

    //    https://stackoverflow.com/a/44539145/548473
    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Task parent;

    @Column(name = "parent_id")
    private Long parentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, insertable = false, updatable = false)
    private Project project;

    @Column(name = "project_id")
    private long projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id", insertable = false, updatable = false)
    private Sprint sprint;

    @Column(name = "sprint_id")
    private Long sprintId;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskTag> taskTags = new ArrayList<>();

    //  history of comments and task fields changing
    @OneToMany(mappedBy = "taskId", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Activity> activities;

    @Column(name = "in_progress")
    private LocalDateTime inProgress;

    @Column(name = "ready_for_review")
    private LocalDateTime readyForReview;

    @Column(name = "done")
    private LocalDateTime done;

    public Task(Long id, String title, String typeCode, String statusCode, Long parentId, long projectId, Long sprintId) {
        super(id, title);
        this.typeCode = typeCode;
        this.statusCode = statusCode;
        this.parentId = parentId;
        this.projectId = projectId;
        this.sprintId = sprintId;
    }

    public void checkAndSetStatusCode(String statusCode) {
        checkStatusChangePossible(this.statusCode, statusCode);
        this.statusCode = statusCode;
    }

    @Override
    public String getCode() {
        return typeCode + '-' + id;
    }
}
