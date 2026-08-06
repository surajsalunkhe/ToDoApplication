package com.todoapp.rest.webservices.restfulwebservices.todo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Standalone MockMvc tests — no Spring Security filter chain needed because
 * SecurityContextHolder is populated directly, matching what the JWT filter
 * does in production.
 */
@ExtendWith(MockitoExtension.class)
class TodoJpaResourceTest {

    @Mock
    private TodoOwnershipService todoOwnershipService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TodoJpaResource(todoOwnershipService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private void authenticateAs(String username) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                username, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // --- AC1: GET /jpa/users/bob/todos by alice → 403 ---

    @Test
    void getAllTodos_pathUserDiffersFromPrincipal_returns403() throws Exception {
        authenticateAs("alice");
        when(todoOwnershipService.listTodosForUser("bob", "alice"))
                .thenThrow(new TodoAccessDeniedException());

        mockMvc.perform(get("/jpa/users/bob/todos"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied"));

        verify(todoOwnershipService).listTodosForUser("bob", "alice");
    }

    // AC5: same-user GET succeeds

    @Test
    void getAllTodos_sameUser_returns200() throws Exception {
        authenticateAs("alice");
        Todo todo = new Todo(1L, "alice", "Task", new Date(), false);
        when(todoOwnershipService.listTodosForUser("alice", "alice")).thenReturn(List.of(todo));

        mockMvc.perform(get("/jpa/users/alice/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alice"));
    }

    // --- AC3: GET /jpa/users/alice/todos/{id} where todo belongs to bob → 403 ---

    @Test
    void getTodo_todoOwnedByOther_returns403() throws Exception {
        authenticateAs("alice");
        when(todoOwnershipService.getOwnedTodoOrThrow(99L, "alice"))
                .thenThrow(new TodoAccessDeniedException());

        mockMvc.perform(get("/jpa/users/alice/todos/99"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied"))
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    // AC5: same-user GET by id succeeds

    @Test
    void getTodo_ownTodo_returns200() throws Exception {
        authenticateAs("alice");
        Todo todo = new Todo(1L, "alice", "Task", new Date(), false);
        when(todoOwnershipService.getOwnedTodoOrThrow(1L, "alice")).thenReturn(todo);

        mockMvc.perform(get("/jpa/users/alice/todos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    // --- AC4: POST body username is overridden by server ---

    @Test
    void addTodo_serverEnforcesUsername_pathMismatchBlocked() throws Exception {
        authenticateAs("alice");
        when(todoOwnershipService.createTodoForUser(eq("bob"), any(), eq("alice")))
                .thenThrow(new TodoAccessDeniedException());

        String body = objectMapper.writeValueAsString(new Todo(0L, "alice", "Task", new Date(), false));
        mockMvc.perform(post("/jpa/users/bob/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void addTodo_ownPath_returns201() throws Exception {
        authenticateAs("alice");
        Todo created = new Todo(10L, "alice", "Task", new Date(), false);
        when(todoOwnershipService.createTodoForUser(eq("alice"), any(), eq("alice")))
                .thenReturn(created);

        String body = objectMapper.writeValueAsString(new Todo(0L, "EVIL", "Task", new Date(), false));
        mockMvc.perform(post("/jpa/users/alice/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        verify(todoOwnershipService).createTodoForUser(eq("alice"), any(), eq("alice"));
    }

    // --- AC2: PUT /jpa/users/alice/todos/{id} where todo belongs to bob → 403 ---

    @Test
    void updateTodo_todoOwnedByOther_returns403() throws Exception {
        authenticateAs("alice");
        when(todoOwnershipService.updateTodoForUser(eq("alice"), eq(99L), any(), eq("alice")))
                .thenThrow(new TodoAccessDeniedException());

        String body = objectMapper.writeValueAsString(new Todo(99L, "alice", "Task", new Date(), false));
        mockMvc.perform(put("/jpa/users/alice/todos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied"));
    }

    // AC5: same-user PUT succeeds

    @Test
    void updateTodo_ownTodo_returns200() throws Exception {
        authenticateAs("alice");
        Todo body = new Todo(5L, "alice", "Updated", new Date(), true);
        when(todoOwnershipService.updateTodoForUser(eq("alice"), eq(5L), any(), eq("alice")))
                .thenReturn(body);

        mockMvc.perform(put("/jpa/users/alice/todos/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    // --- AC2: DELETE /jpa/users/alice/todos/{id} where todo belongs to bob → 403 ---

    @Test
    void deleteTodo_todoOwnedByOther_returns403() throws Exception {
        authenticateAs("alice");
        doThrow(new TodoAccessDeniedException())
                .when(todoOwnershipService).deleteTodoForUser("alice", 99L, "alice");

        mockMvc.perform(delete("/jpa/users/alice/todos/99"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied"));

        verify(todoOwnershipService).deleteTodoForUser("alice", 99L, "alice");
    }

    // AC5: same-user DELETE succeeds

    @Test
    void deleteTodo_ownTodo_returns204() throws Exception {
        authenticateAs("alice");
        doNothing().when(todoOwnershipService).deleteTodoForUser("alice", 3L, "alice");

        mockMvc.perform(delete("/jpa/users/alice/todos/3"))
                .andExpect(status().isNoContent());
    }

    // --- AC6: 403 body has no stack trace ---

    @Test
    void forbidden_responseBody_hasNoStackTrace() throws Exception {
        authenticateAs("alice");
        when(todoOwnershipService.listTodosForUser("bob", "alice"))
                .thenThrow(new TodoAccessDeniedException());

        String responseBody = mockMvc.perform(get("/jpa/users/bob/todos"))
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();

        assert !responseBody.contains("at com.todoapp");
        assert !responseBody.contains("StackTrace");
        assert !responseBody.contains("TodoAccessDenied");
    }
}
