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

    /**
     * Loads a todo by id, enforcing ownership against the authenticated user.
     * Throws TodoNotFoundException (→ 404) when the id doesn't exist at all;
     * throws TodoAccessDeniedException (→ 403) when it exists but belongs to
     * a different user. These are deliberately distinct: a 404 reveals that
     * the resource doesn't exist, while a 403 reveals that it does — callers
     * should be aware that this design prioritises debuggability over strict
     * non-disclosure of resource existence.
     */
    public Todo getOwnedTodoOrThrow(Long id, String authenticatedUsername) {
        Todo todo = todoJpaRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
        if (!authenticatedUsername.equals(todo.getUsername())) {
            throw new TodoAccessDeniedException();
        }
        return todo;
    }

    public Todo getTodoForUser(String pathUsername, Long id, String authenticatedUsername) {
        assertSameUser(pathUsername, authenticatedUsername);
        return getOwnedTodoOrThrow(id, authenticatedUsername);
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
        getOwnedTodoOrThrow(id, authenticatedUsername);
        todo.setId(id);
        todo.setUsername(authenticatedUsername);
        return todoJpaRepository.save(todo);
    }

    public void deleteTodoForUser(String pathUsername, Long id, String authenticatedUsername) {
        assertSameUser(pathUsername, authenticatedUsername);
        getOwnedTodoOrThrow(id, authenticatedUsername);
        todoJpaRepository.deleteById(id);
    }
}
