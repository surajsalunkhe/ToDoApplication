package com.todoapp.rest.webservices.restfulwebservices.todo;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoOwnershipService {

    private final TodoJpaRepository todoJpaRepository;

    public TodoOwnershipService(TodoJpaRepository todoJpaRepository) {
        this.todoJpaRepository = todoJpaRepository;
    }

    public void assertSameUser(String pathUsername, String authenticatedUsername) {
        if (!authenticatedUsername.equals(pathUsername)) {
            throw new TodoAccessDeniedException();
        }
    }

    public Todo getOwnedTodoOrThrow(Long id, String authenticatedUsername) {
        return todoJpaRepository.findByIdAndUsername(id, authenticatedUsername)
                .orElseThrow(TodoAccessDeniedException::new);
    }

    public List<Todo> listTodosForUser(String pathUsername, String authenticatedUsername) {
        assertSameUser(pathUsername, authenticatedUsername);
        return todoJpaRepository.findByUsername(authenticatedUsername);
    }

    public Todo createTodoForUser(String pathUsername, Todo todo, String authenticatedUsername) {
        assertSameUser(pathUsername, authenticatedUsername);
        todo.setUsername(authenticatedUsername);
        return todoJpaRepository.save(todo);
    }

    public Todo updateTodoForUser(String pathUsername, Long id, Todo todo, String authenticatedUsername) {
        assertSameUser(pathUsername, authenticatedUsername);
        // Verify the existing todo belongs to the authenticated user before updating
        getOwnedTodoOrThrow(id, authenticatedUsername);
        todo.setId(id);
        todo.setUsername(authenticatedUsername);
        return todoJpaRepository.save(todo);
    }

    public void deleteTodoForUser(String pathUsername, Long id, String authenticatedUsername) {
        assertSameUser(pathUsername, authenticatedUsername);
        // Verify ownership before deleting
        getOwnedTodoOrThrow(id, authenticatedUsername);
        todoJpaRepository.deleteById(id);
    }
}
