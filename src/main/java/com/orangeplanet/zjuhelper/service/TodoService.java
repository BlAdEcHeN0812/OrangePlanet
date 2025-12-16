package com.orangeplanet.zjuhelper.service;

import com.orangeplanet.zjuhelper.model.Todo;
import com.orangeplanet.zjuhelper.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoService {

    @Autowired
    private TodoRepository todoRepository;

    public List<Todo> getAllTodos() {
        return todoRepository.findAllByOrderByCreatedAtDesc();
    }

    public Todo createTodo(Todo todo) {
        return todoRepository.save(todo);
    }

    public Todo updateTodo(Long id, Todo todoDetails) {
        Todo todo = todoRepository.findById(id).orElseThrow(() -> new RuntimeException("Todo not found"));
        todo.setTitle(todoDetails.getTitle());
        todo.setContent(todoDetails.getContent());
        todo.setCompleted(todoDetails.isCompleted());
        todo.setUpdatedAt(java.time.LocalDateTime.now());
        return todoRepository.save(todo);
    }

    public void deleteTodo(Long id) {
        todoRepository.deleteById(id);
    }
}
