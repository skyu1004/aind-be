package com.example.todo.todo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.todo.todo.TodoDtos.CreateRequest;
import com.example.todo.todo.TodoDtos.Response;
import com.example.todo.todo.TodoDtos.UpdateRequest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

	@Mock
	TodoRepository todoRepository;

	@InjectMocks
	TodoService todoService;

	@Test
	void findAllMapsEntitiesToResponses() {
		when(todoRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(new Todo("a"), new Todo("b")));

		List<Response> result = todoService.findAll();

		assertThat(result).extracting(Response::title).containsExactly("a", "b");
	}

	@Test
	void createSavesStrippedTitle() {
		when(todoRepository.save(any(Todo.class))).thenAnswer(inv -> inv.getArgument(0));

		Response result = todoService.create(new CreateRequest("  buy milk  "));

		assertThat(result.title()).isEqualTo("buy milk");
		assertThat(result.completed()).isFalse();
	}

	@Test
	void updateChangesTitleAndCompletion() {
		Todo todo = new Todo("old");
		when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
		when(todoRepository.saveAndFlush(todo)).thenReturn(todo);

		Response result = todoService.update(1L, new UpdateRequest("new", true));

		assertThat(result.title()).isEqualTo("new");
		assertThat(result.completed()).isTrue();
	}

	@Test
	void updateThrowsWhenTodoMissing() {
		when(todoRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> todoService.update(99L, new UpdateRequest("x", false)))
			.isInstanceOf(TodoNotFoundException.class)
			.hasMessageContaining("99");
	}

	@Test
	void deleteRemovesExistingTodo() {
		when(todoRepository.existsById(1L)).thenReturn(true);

		todoService.delete(1L);

		verify(todoRepository).deleteById(1L);
	}

	@Test
	void deleteThrowsWhenTodoMissing() {
		when(todoRepository.existsById(99L)).thenReturn(false);

		assertThatThrownBy(() -> todoService.delete(99L)).isInstanceOf(TodoNotFoundException.class);
		verify(todoRepository, never()).deleteById(any());
	}
}
