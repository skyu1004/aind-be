package com.example.todo.todo;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.todo.todo.TodoDtos.CreateRequest;
import com.example.todo.todo.TodoDtos.Response;
import com.example.todo.todo.TodoDtos.UpdateRequest;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TodoController.class)
class TodoControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	TodoService todoService;

	private static Response response(long id, String title, boolean completed) {
		Instant now = Instant.parse("2026-01-01T00:00:00Z");
		return new Response(id, title, completed, now, now);
	}

	@Test
	void listReturnsTodos() throws Exception {
		when(todoService.findAll()).thenReturn(List.of(response(1, "a", false)));

		mockMvc.perform(get("/api/todos"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].title").value("a"));
	}

	@Test
	void createReturns201() throws Exception {
		when(todoService.create(new CreateRequest("buy milk"))).thenReturn(response(1, "buy milk", false));

		mockMvc.perform(post("/api/todos").contentType(MediaType.APPLICATION_JSON).content("""
				{"title":"buy milk"}
				"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.title").value("buy milk"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "{\"title\":\"\"}", "{\"title\":\"   \"}", "{}" })
	void createRejectsBlankTitleAsProblemDetail(String body) throws Exception {
		mockMvc.perform(post("/api/todos").contentType(MediaType.APPLICATION_JSON).content(body))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400));

		verifyNoInteractions(todoService);
	}

	@Test
	void createRejectsTitleOver200Chars() throws Exception {
		mockMvc.perform(post("/api/todos").contentType(MediaType.APPLICATION_JSON)
			.content("{\"title\":\"" + "a".repeat(201) + "\"}"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void createAcceptsPaddedTitleWithin200CharsAfterStrip() throws Exception {
		String title = "a".repeat(200);
		when(todoService.create(new CreateRequest(title))).thenReturn(response(1, title, false));

		mockMvc.perform(post("/api/todos").contentType(MediaType.APPLICATION_JSON)
			.content("{\"title\":\"  " + title + "  \"}"))
			.andExpect(status().isCreated());
	}

	@Test
	void malformedJsonReturnsProblemDetail() throws Exception {
		mockMvc.perform(post("/api/todos").contentType(MediaType.APPLICATION_JSON).content("{not json"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void updateReturnsUpdatedTodo() throws Exception {
		when(todoService.update(eq(1L), any(UpdateRequest.class))).thenReturn(response(1, "a", true));

		mockMvc.perform(put("/api/todos/1").contentType(MediaType.APPLICATION_JSON).content("""
				{"title":"a","completed":true}
				"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.completed").value(true));
	}

	@Test
	void updateMissingTodoReturns404ProblemDetail() throws Exception {
		when(todoService.update(eq(99L), any(UpdateRequest.class))).thenThrow(new TodoNotFoundException(99L));

		mockMvc.perform(put("/api/todos/99").contentType(MediaType.APPLICATION_JSON).content("""
				{"title":"a","completed":false}
				"""))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.detail").value("Todo not found: 99"));
	}

	@Test
	void nonNumericIdReturns400() throws Exception {
		mockMvc.perform(delete("/api/todos/abc")).andExpect(status().isBadRequest());
	}

	@Test
	void deleteReturns204() throws Exception {
		mockMvc.perform(delete("/api/todos/1")).andExpect(status().isNoContent());
	}

	@Test
	void deleteMissingTodoReturns404() throws Exception {
		doThrow(new TodoNotFoundException(99L)).when(todoService).delete(99L);

		mockMvc.perform(delete("/api/todos/99")).andExpect(status().isNotFound());
	}

	@Test
	void unexpectedErrorReturnsGeneric500WithoutLeakingMessage() throws Exception {
		when(todoService.findAll()).thenThrow(new IllegalStateException("db password is hunter2"));

		mockMvc.perform(get("/api/todos"))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.detail").value("Unexpected server error"));
	}
}
