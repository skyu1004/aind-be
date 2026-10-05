package com.example.todo.todo;

import com.example.todo.todo.TodoDtos.CreateRequest;
import com.example.todo.todo.TodoDtos.Response;
import com.example.todo.todo.TodoDtos.UpdateRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {

	private final TodoService todoService;

	@GetMapping
	public List<Response> list() {
		return todoService.findAll();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Response create(@Valid @RequestBody CreateRequest request) {
		return todoService.create(request);
	}

	@PutMapping("/{id}")
	public Response update(@PathVariable Long id, @Valid @RequestBody UpdateRequest request) {
		return todoService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		todoService.delete(id);
	}
}
