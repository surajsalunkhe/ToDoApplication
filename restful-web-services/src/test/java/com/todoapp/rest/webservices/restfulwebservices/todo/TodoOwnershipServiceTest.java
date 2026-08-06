package com.todoapp.rest.webservices.restfulwebservices.todo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoOwnershipServiceTest {

    @Mock
    private TodoJpaRepository todoJpaRepository;

    private TodoOwnershipService service;

    @BeforeEach
    void setUp() {
        service = new TodoOwnershipService(todoJpaRepository);
    }

    // --- assertSameUser ---

    @Test
    void assertSameUser_sameUsername_doesNotThrow() {
        service.assertSameUser("alice", "alice");
    }

    @Test
    void assertSameUser_differentUsername_throws403() {
        assertThatThrownBy(() -> service.assertSameUser("bob", "alice"))
                .isInstanceOf(TodoAccessDeniedException.class);
    }

    // --- getOwnedTodoOrThrow ---

    @Test
    void getOwnedTodoOrThrow_todoOwnedByUser_returnsIt() {
        Todo todo = new Todo(1L, "alice", "Learn JPA", new Date(), false);
        when(todoJpaRepository.findById(1L)).thenReturn(Optional.of(todo));

        Todo result = service.getOwnedTodoOrThrow(1L, "alice");

        assertThat(result).isEqualTo(todo);
    }

    @Test
    void getOwnedTodoOrThrow_todoDoesNotExist_throws404() {
        // Missing todo → 404, not 403 (no information leakage needed for a truly absent resource)
        when(todoJpaRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOwnedTodoOrThrow(42L, "alice"))
                .isInstanceOf(TodoNotFoundException.class);
    }

    @Test
    void getOwnedTodoOrThrow_todoOwnedByOtherUser_throws403() {
        // AC3: todo exists but belongs to bob — alice gets 403
        Todo bobsTodo = new Todo(42L, "bob", "Bob task", new Date(), false);
        when(todoJpaRepository.findById(42L)).thenReturn(Optional.of(bobsTodo));

        assertThatThrownBy(() -> service.getOwnedTodoOrThrow(42L, "alice"))
                .isInstanceOf(TodoAccessDeniedException.class);
    }

    // --- getTodoForUser ---

    @Test
    void getTodoForUser_pathMismatch_throws403BeforeQuery() {
        // AC1 variant: path mismatch is caught before any DB call
        assertThatThrownBy(() -> service.getTodoForUser("bob", 1L, "alice"))
                .isInstanceOf(TodoAccessDeniedException.class);

        verifyNoInteractions(todoJpaRepository);
    }

    @Test
    void getTodoForUser_ownTodo_returnsIt() {
        Todo todo = new Todo(1L, "alice", "Task", new Date(), false);
        when(todoJpaRepository.findById(1L)).thenReturn(Optional.of(todo));

        Todo result = service.getTodoForUser("alice", 1L, "alice");

        assertThat(result).isEqualTo(todo);
    }

    // --- listTodosForUser ---

    @Test
    void listTodosForUser_ownPath_returnsTodos() {
        Todo todo = new Todo(1L, "alice", "Task", new Date(), false);
        when(todoJpaRepository.findByUsername("alice")).thenReturn(List.of(todo));

        List<Todo> result = service.listTodosForUser("alice", "alice");

        assertThat(result).containsExactly(todo);
    }

    @Test
    void listTodosForUser_differentPath_throws403() {
        // AC1: alice calling /jpa/users/bob/todos gets 403
        assertThatThrownBy(() -> service.listTodosForUser("bob", "alice"))
                .isInstanceOf(TodoAccessDeniedException.class);

        verifyNoInteractions(todoJpaRepository);
    }

    // --- createTodoForUser ---

    @Test
    void createTodoForUser_overridesBodyUsername() {
        // AC4: body username is ignored; server enforces authenticated user
        Todo todo = new Todo(0L, "EVIL_USERNAME", "Task", new Date(), false);
        Todo saved = new Todo(10L, "alice", "Task", new Date(), false);
        when(todoJpaRepository.save(any())).thenReturn(saved);

        Todo result = service.createTodoForUser("alice", todo, "alice");

        assertThat(todo.getUsername()).isEqualTo("alice");
        assertThat(result).isEqualTo(saved);
    }

    @Test
    void createTodoForUser_pathMismatch_throws403() {
        Todo todo = new Todo(0L, "alice", "Task", new Date(), false);

        assertThatThrownBy(() -> service.createTodoForUser("bob", todo, "alice"))
                .isInstanceOf(TodoAccessDeniedException.class);

        verifyNoInteractions(todoJpaRepository);
    }

    // --- updateTodoForUser ---

    @Test
    void updateTodoForUser_ownTodo_succeeds() {
        Todo existing = new Todo(5L, "alice", "Old task", new Date(), false);
        Todo update = new Todo(5L, "alice", "New task", new Date(), true);
        when(todoJpaRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(todoJpaRepository.save(any())).thenReturn(update);

        Todo result = service.updateTodoForUser("alice", 5L, update, "alice");

        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getId()).isEqualTo(5L);
    }

    @Test
    void updateTodoForUser_todoOwnedByOtherUser_throws403() {
        // AC2: alice cannot PUT a todo owned by bob
        Todo bobsTodo = new Todo(99L, "bob", "Bob task", new Date(), false);
        when(todoJpaRepository.findById(99L)).thenReturn(Optional.of(bobsTodo));

        assertThatThrownBy(() -> service.updateTodoForUser("alice", 99L, new Todo(), "alice"))
                .isInstanceOf(TodoAccessDeniedException.class);

        verify(todoJpaRepository, never()).save(any());
    }

    @Test
    void updateTodoForUser_todoNotFound_throws404() {
        when(todoJpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateTodoForUser("alice", 99L, new Todo(), "alice"))
                .isInstanceOf(TodoNotFoundException.class);

        verify(todoJpaRepository, never()).save(any());
    }

    @Test
    void updateTodoForUser_pathMismatch_throws403() {
        assertThatThrownBy(() -> service.updateTodoForUser("bob", 5L, new Todo(), "alice"))
                .isInstanceOf(TodoAccessDeniedException.class);

        verifyNoInteractions(todoJpaRepository);
    }

    @Test
    void updateTodoForUser_forcesIdAndUsername() {
        // AC4-variant: body id/username are overwritten by the service
        Todo existing = new Todo(5L, "alice", "Old task", new Date(), false);
        Todo update = new Todo(999L, "EVIL", "New task", new Date(), true);
        when(todoJpaRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(todoJpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateTodoForUser("alice", 5L, update, "alice");

        assertThat(update.getId()).isEqualTo(5L);
        assertThat(update.getUsername()).isEqualTo("alice");
    }

    // --- deleteTodoForUser ---

    @Test
    void deleteTodoForUser_ownTodo_deletesIt() {
        Todo existing = new Todo(3L, "alice", "Task", new Date(), false);
        when(todoJpaRepository.findById(3L)).thenReturn(Optional.of(existing));

        service.deleteTodoForUser("alice", 3L, "alice");

        verify(todoJpaRepository).deleteById(3L);
    }

    @Test
    void deleteTodoForUser_todoOwnedByOtherUser_throws403() {
        // AC2: alice cannot DELETE a todo belonging to bob
        Todo bobsTodo = new Todo(7L, "bob", "Bob task", new Date(), false);
        when(todoJpaRepository.findById(7L)).thenReturn(Optional.of(bobsTodo));

        assertThatThrownBy(() -> service.deleteTodoForUser("alice", 7L, "alice"))
                .isInstanceOf(TodoAccessDeniedException.class);

        verify(todoJpaRepository, never()).deleteById(any());
    }

    @Test
    void deleteTodoForUser_todoNotFound_throws404() {
        when(todoJpaRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteTodoForUser("alice", 7L, "alice"))
                .isInstanceOf(TodoNotFoundException.class);

        verify(todoJpaRepository, never()).deleteById(any());
    }

    @Test
    void deleteTodoForUser_pathMismatch_throws403() {
        assertThatThrownBy(() -> service.deleteTodoForUser("bob", 7L, "alice"))
                .isInstanceOf(TodoAccessDeniedException.class);

        verifyNoInteractions(todoJpaRepository);
    }
}
