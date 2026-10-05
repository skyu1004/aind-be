package com.example.todo.todo;

import com.example.todo.todo.TodoDtos.CreateRequest;
import com.example.todo.todo.TodoDtos.Response;
import com.example.todo.todo.TodoDtos.UpdateRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

	private final TodoRepository todoRepository;

	public List<Response> findAll() {
		return todoRepository.findAllByOrderByCreatedAtDesc().stream().map(Response::from).toList();
	}

	@Transactional
	public Response create(CreateRequest request) {
		return Response.from(todoRepository.save(new Todo(request.title())));
	}

	@Transactional
	public Response update(Long id, UpdateRequest request) {
		Todo todo = todoRepository.findById(id).orElseThrow(() -> new TodoNotFoundException(id));
		todo.update(request.title(), request.completed());
		// Flush so @PreUpdate sets updatedAt before the response is built.
		return Response.from(todoRepository.saveAndFlush(todo));
	}

	@Transactional
	public void delete(Long id) {
		if (!todoRepository.existsById(id)) {
			throw new TodoNotFoundException(id);
		}
		todoRepository.deleteById(id);
	}
}
