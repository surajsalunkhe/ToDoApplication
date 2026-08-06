package com.todoapp.rest.webservices.restfulwebservices.todo;

public class TodoAccessDeniedException extends RuntimeException {
    public TodoAccessDeniedException() {
        super("Access denied");
    }
}
