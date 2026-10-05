package com.example.todo.todo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class TodoRepositoryTest {

	@Autowired
	TodoRepository todoRepository;

	@Test
	void persistSetsTimestampsAndDefaults() {
		Todo saved = todoRepository.saveAndFlush(new Todo("a"));

		assertThat(saved.getId()).isNotNull();
		assertThat(saved.isCompleted()).isFalse();
		assertThat(saved.getCreatedAt()).isNotNull().isEqualTo(saved.getUpdatedAt());
	}

	@Test
	void updateBumpsUpdatedAtOnly() throws InterruptedException {
		Todo saved = todoRepository.saveAndFlush(new Todo("a"));
		var createdAt = saved.getCreatedAt();
		Thread.sleep(5);

		saved.update("b", true);
		todoRepository.saveAndFlush(saved);

		assertThat(saved.getCreatedAt()).isEqualTo(createdAt);
		assertThat(saved.getUpdatedAt()).isAfter(createdAt);
	}

	@Test
	void findAllOrdersNewestFirst() throws InterruptedException {
		todoRepository.saveAndFlush(new Todo("first"));
		Thread.sleep(5);
		todoRepository.saveAndFlush(new Todo("second"));

		List<Todo> result = todoRepository.findAllByOrderByCreatedAtDesc();

		assertThat(result).extracting(Todo::getTitle).containsExactly("second", "first");
	}
}
