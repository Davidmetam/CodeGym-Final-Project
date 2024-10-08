package com.codegym.jira.bugtracking.task;

import com.codegym.jira.bugtracking.Handlers;
import com.codegym.jira.bugtracking.ObjectType;
import com.codegym.jira.bugtracking.UserBelong;
import com.codegym.jira.bugtracking.UserBelongRepository;
import com.codegym.jira.bugtracking.task.mapper.TaskExtMapper;
import com.codegym.jira.bugtracking.task.mapper.TaskFullMapper;
import com.codegym.jira.common.error.DataConflictException;
import com.codegym.jira.common.error.NotFoundException;
import com.codegym.jira.common.util.Util;
import com.codegym.jira.login.AuthUser;
import com.codegym.jira.ref.RefType;
import com.codegym.jira.ref.ReferenceService;
import com.codegym.jira.bugtracking.sprint.Sprint;
import com.codegym.jira.bugtracking.sprint.SprintRepository;
import com.codegym.jira.bugtracking.task.to.TaskToExt;
import com.codegym.jira.bugtracking.task.to.TaskToFull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    static final String CANNOT_ASSIGN = "Cannot assign as %s to task with status=%s";
    static final String CANNOT_UN_ASSIGN = "Cannot unassign as %s from task with status=%s";

    private final Handlers.TaskExtHandler handler;
    private final Handlers.ActivityHandler activityHandler;
    private final TaskFullMapper fullMapper;
    private final SprintRepository sprintRepository;
    private final TaskExtMapper extMapper;
    private final UserBelongRepository userBelongRepository;

    @Transactional
    public void changeStatus(long taskId, String statusCode) {
        Assert.notNull(statusCode, "statusCode must not be null");
        Task task = handler.getRepository().getExisted(taskId);
        if (!statusCode.equals(task.getStatusCode())) {
            task.checkAndSetStatusCode(statusCode);
            switch (task.getStatusCode()) {
                case "in_progress" -> task.setInProgress(LocalDateTime.now());
                case "ready_for_review" -> task.setReadyForReview(LocalDateTime.now());
                case "done" -> task.setDone(LocalDateTime.now());
            }
            handler.getRepository().save(task);
            Duration timeInProgress = getTimeInProgress(task);
            Duration timeInReview = getTimeUnderTesting(task);
            if (timeInProgress != Duration.ZERO){
                log.info("Time in progress for task Id: {} is {} ", taskId, timeInProgress);
            }
            if (timeInReview != Duration.ZERO){
                log.info("Time in review for task Id: {} is {} ", taskId, timeInReview);
            }
            Activity statusChangedActivity = new Activity(null, taskId, AuthUser.authId());
            statusChangedActivity.setStatusCode(statusCode);
            activityHandler.create(statusChangedActivity);
            String userType = ReferenceService.getRefTo(RefType.TASK_STATUS, statusCode).getAux(1);
            if (userType != null) {
                handler.createUserBelong(taskId, ObjectType.TASK, AuthUser.authId(), userType);
            }
        }
    }

    @Transactional
    public void changeSprint(long taskId, Long sprintId) {
        Task task = handler.getRepository().getExisted(taskId);
        if (task.getParentId() != null) {
            throw new DataConflictException("Can't change subtask sprint");
        }
        if (sprintId != null) {
            Sprint sprint = sprintRepository.getExisted(sprintId);
            if (sprint.getProjectId() != task.getProjectId()) {
                throw new DataConflictException("Target sprint must belong to the same project");
            }
        }
        handler.getRepository().setTaskAndSubTasksSprint(taskId, sprintId);
    }

    @Transactional
    public Task create(TaskToExt taskTo) {
        Task created = handler.createWithBelong(taskTo, ObjectType.TASK, "task_author");
        activityHandler.create(TaskUtil.makeActivity(created.id(), taskTo));
        return created;
    }

    @Transactional
    public void update(TaskToExt taskTo, long id) {
        if (!taskTo.equals(get(taskTo.id()))) {
            handler.updateFromTo(taskTo, id);
            activityHandler.create(TaskUtil.makeActivity(id, taskTo));
        }
    }

    public TaskToFull get(long id) {
        Task task = Util.checkExist(id, handler.getRepository().findFullById(id));
        TaskToFull taskToFull = fullMapper.toTo(task);
        List<Activity> activities = activityHandler.getRepository().findAllByTaskIdOrderByUpdatedDesc(id);
        TaskUtil.fillExtraFields(taskToFull, activities);
        taskToFull.setActivityTos(activityHandler.getMapper().toToList(activities));
        return taskToFull;
    }

    public TaskToExt getNewWithSprint(long sprintId) {
        Sprint sprint = sprintRepository.getExisted(sprintId);
        Task newTask = new Task();
        newTask.setSprintId(sprintId);
        newTask.setProjectId(sprint.getProjectId());
        return extMapper.toTo(newTask);
    }

    public TaskToExt getNewWithProject(long projectId) {
        Task newTask = new Task();
        newTask.setProjectId(projectId);
        return extMapper.toTo(newTask);
    }

    public TaskToExt getNewWithParent(long parentId) {
        Task parent = handler.getRepository().getExisted(parentId);
        Task newTask = new Task();
        newTask.setParentId(parentId);
        newTask.setSprintId(parent.getSprintId());
        newTask.setProjectId(parent.getProjectId());
        return extMapper.toTo(newTask);
    }

    public void assign(long id, String userType, long userId) {
        checkAssignmentActionPossible(id, userType, true);
        handler.createUserBelong(id, ObjectType.TASK, userId, userType);
    }

    @Transactional
    public void unAssign(long id, String userType, long userId) {
        checkAssignmentActionPossible(id, userType, false);
        UserBelong assignment = userBelongRepository.findActiveAssignment(id, ObjectType.TASK, userId, userType)
                .orElseThrow(() -> new NotFoundException(String
                        .format("Not found assignment with userType=%s for task {%d} for user {%d}", userType, id, userId)));
        assignment.setEndpoint(LocalDateTime.now());
    }

    private void checkAssignmentActionPossible(long id, String userType, boolean assign) {
        Assert.notNull(userType, "userType must not be null");
        Task task = handler.getRepository().getExisted(id);
        String possibleUserType = ReferenceService.getRefTo(RefType.TASK_STATUS, task.getStatusCode()).getAux(1);
        if (!userType.equals(possibleUserType)) {
            throw new DataConflictException(String.format(assign ? CANNOT_ASSIGN : CANNOT_UN_ASSIGN, userType, task.getStatusCode()));
        }
    }

    public Task findById(Long taskId) {
        return Util.checkExist(taskId, handler.getRepository().findById(taskId));
    }

    public Duration getTimeInProgress(Task task) {
        if (task.getInProgress() != null && task.getReadyForReview() != null) {
            return Duration.between(task.getInProgress(), task.getReadyForReview());
        }
        return Duration.ZERO;
    }

    public Duration getTimeUnderTesting(Task task) {
        if (task.getReadyForReview() != null && task.getDone() != null) {
            return Duration.between(task.getReadyForReview(), task.getDone());
        }
        return Duration.ZERO;
    }

}
