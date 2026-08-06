package com.todoapp.rest.webservices.restfulwebservices.todo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TodoJpaRepository extends JpaRepository<Todo,Long>{
    List<Todo> findByUsername(String username);
    Optional<Todo> findByIdAndUsername(Long id, String username);
}
