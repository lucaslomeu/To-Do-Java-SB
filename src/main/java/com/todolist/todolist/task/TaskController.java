package com.todolist.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.todolist.todolist.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        taskModel.setIdUser((UUID) request.getAttribute("idUser"));

        LocalDateTime currentDate = LocalDateTime.now();

        if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(401)
                    .body("A data de início  ou data de térmico deve ser maior do que a data atual.");
        }

        if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(401)
                    .body("A data de início deve ser menor do que a data término.");
        }

        TaskModel task = this.taskRepository.save(taskModel);
        return ResponseEntity.status(200).body(task);
    }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request) {
        List<TaskModel> tasks = this.taskRepository.findByIdUser((UUID) request.getAttribute("idUser"));
        return tasks;
    }

    @PutMapping("/{id}")
    public void update(@RequestBody TaskModel taskModel, @PathVariable UUID id, HttpServletRequest request) {
        var task = this.taskRepository.findById(id).orElse(null);

        Utils.copyNonNullProperties(taskModel, task);

        this.taskRepository.save(task);
    }

}
