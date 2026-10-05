package com.example.todo.todo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class TodoDtos {

	private TodoDtos() {
	}

	// Titles are stripped here so @NotBlank/@Size validate the value that is actually stored.
	public record CreateRequest(@NotBlank @Size(max = 200) String title) {

		public CreateRequest {
			title = strip(title);
		}
	}

	public record UpdateRequest(@NotBlank @Size(max = 200) String title, boolean completed) {

		public UpdateRequest {
			title = strip(title);
		}
	}

	public record Response(Long id, String title, boolean completed, Instant createdAt, Instant updatedAt) {

		static Response from(Todo todo) {
			return new Response(todo.getId(), todo.getTitle(), todo.isCompleted(), todo.getCreatedAt(),
					todo.getUpdatedAt());
		}
	}

	private static String strip(String value) {
		return value == null ? null : value.strip();
	}
}
