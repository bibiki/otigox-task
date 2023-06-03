package com.gagi;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.gagi.domain.User;

import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebClient
class OtigoxTaskApplicationTests {

	@Autowired
	private WebTestClient testClient;

	@Test
	void shouldCreateUserAndThenRetrieveTheSameUser() {
		User user = new User("Ngadhnjim", "unique@hotmail.com");
		testClient.post().uri("/users").body(Mono.just(user), User.class)
		.exchange().expectStatus().isCreated()
		.expectHeader().contentType(MediaTypes.HAL_JSON)
		.expectBody()
		.jsonPath("$.name").isEqualTo(user.getName())
		.jsonPath("$.email").isEqualTo(user.getEmail())
		.jsonPath("$._links.user.href").isNotEmpty()
		.jsonPath("$._links.user.href")
		.value((v)-> {
			testClient.get().uri(String.valueOf(v))
			.exchange().expectStatus().isOk()
			.expectHeader().contentType(MediaTypes.HAL_JSON)
			.expectBody()
			.jsonPath("$.name").isEqualTo(user.getName())
			.jsonPath("$.email").isEqualTo(user.getEmail());
		});
		
    }
	
	@Test
	void shouldTryButFailToAddADifferentUserWithTheSameEmailAsAnotherUser() {
		User user = new User("Ngadhnjim", "notunique@hotmail.com");
		testClient.post().uri("/users").body(Mono.just(user), User.class)
		.exchange().expectStatus().isCreated();
		
		User otherUser = new User("Berani", "notunique@hotmail.com");
		testClient.post().uri("/users").body(Mono.just(otherUser), User.class)
		.exchange().expectStatus().is4xxClientError()
		.expectBody()
		.jsonPath("$.message").isNotEmpty()
		.jsonPath("$.message").value((v) -> {
			assertTrue(String.valueOf(v).contains("Email must be unique"));
		});
	}
}
