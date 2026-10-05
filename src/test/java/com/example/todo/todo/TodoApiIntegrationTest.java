package com.example.todo.todo;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TodoApiIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	TodoRepository todoRepository;

	@BeforeEach
	void cleanDatabase() {
		todoRepository.deleteAll();
	}

	@Test
	void fullCrudFlow() throws Exception {
		String created = mockMvc
			.perform(post("/api/todos").contentType(MediaType.APPLICATION_JSON).content("""
					{"title":"  buy milk  "}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.title").value("buy milk"))
			.andReturn()
			.getResponse()
			.getContentAsString();
		long id = ((Number) JsonPath.read(created, "$.id")).longValue();

		mockMvc.perform(put("/api/todos/{id}", id).contentType(MediaType.APPLICATION_JSON).content("""
				{"title":"buy oat milk","completed":true}
				"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("buy oat milk"))
			.andExpect(jsonPath("$.completed").value(true));

		mockMvc.perform(get("/api/todos"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].completed").value(true));

		mockMvc.perform(delete("/api/todos/{id}", id)).andExpect(status().isNoContent());
		mockMvc.perform(delete("/api/todos/{id}", id)).andExpect(status().isNotFound());
		mockMvc.perform(get("/api/todos")).andExpect(jsonPath("$", hasSize(0)));
	}
}
