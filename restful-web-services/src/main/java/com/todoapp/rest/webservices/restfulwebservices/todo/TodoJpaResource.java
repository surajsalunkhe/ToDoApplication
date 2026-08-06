package com.todoapp.rest.webservices.restfulwebservices.todo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class TodoJpaResource {

    private final TodoOwnershipService todoOwnershipService;

    public TodoJpaResource(TodoOwnershipService todoOwnershipService) {
        this.todoOwnershipService = todoOwnershipService;
    }

    /**
     * Returns the authenticated username from the security context.
     * Throws 401 if there is no valid authentication (guards against a
     * misconfigured security filter chain that lets unauthenticated requests
     * reach the controller).
     */
    private String authenticatedUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return auth.getName();
    }

    @GetMapping("/jpa/users/{username}/todos")
    public List<Todo> getAllTodos(@PathVariable String username) {
        return todoOwnershipService.listTodosForUser(username, authenticatedUsername());
    }

    @GetMapping("/jpa/users/{username}/todos/{id}")
    public Todo getTodo(@PathVariable String username, @PathVariable long id) {
        return todoOwnershipService.getTodoForUser(username, id, authenticatedUsername());
    }

    /**
     * Returns 201 Created with a Location header pointing to the new resource.
     * No body is returned, matching the original contract.
     */
    @PostMapping("/jpa/users/{username}/todos")
    public ResponseEntity<Void> addTodo(@PathVariable String username, @RequestBody Todo todo) {
        Todo created = todoOwnershipService.createTodoForUser(username, todo, authenticatedUsername());
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @PutMapping("/jpa/users/{username}/todos/{id}")
    public ResponseEntity<Todo> updateTodo(@PathVariable String username, @PathVariable long id, @RequestBody Todo todo) {
        Todo updated = todoOwnershipService.updateTodoForUser(username, id, todo, authenticatedUsername());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/jpa/users/{username}/todos/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable String username, @PathVariable long id) {
        todoOwnershipService.deleteTodoForUser(username, id, authenticatedUsername());
        return ResponseEntity.noContent().build();
    }
}
