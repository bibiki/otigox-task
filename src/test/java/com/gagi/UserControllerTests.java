package com.gagi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.gagi.domain.User;

import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebClient
class UserControllerTests {

	@Autowired
	private WebTestClient testClient;

	@Test
	void shouldCreateUserAndThenRetrieveTheSameUser() {
		User user = new User("Ngadhnjim", "unique@hotmail.com");
		User fromServer = testClient.post().uri("/users").body(Mono.just(user), User.class)
		.exchange().expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody(User.class).returnResult().getResponseBody();
		
		assertNotNull(fromServer);
		assertEquals(user.getName(), fromServer.getName());
		assertEquals(user.getEmail(), fromServer.getEmail());
		assertTrue(fromServer.getId() > 0);
		
    }
	
	@Test
	public void shouldUpdateExistingUserName() {
		User user = new User("To be changed", "e@e.com");
		User created = testClient.post().uri("/users").body(Mono.just(user), User.class)
		.exchange().expectStatus().isCreated().expectBody(User.class)
		.returnResult().getResponseBody();
		
		created.setName("Username");
		User updated = testClient.put().uri("/users/{userId}", created.getId()).body(Mono.just(created), User.class)
		.exchange().expectStatus().is2xxSuccessful().expectBody(User.class).returnResult().getResponseBody();
		
		assertEquals(created, updated);
	}
	
	@Test
	public void shouldUpdateExistingUserEmail() {
		User user = new User("someusername", "tobechanged@e.com");
		User created = testClient.post().uri("/users").body(Mono.just(user), User.class)
		.exchange().expectStatus().isCreated().expectBody(User.class)
		.returnResult().getResponseBody();
		
		created.setEmail("changed@email.com");
		User updated = testClient.put().uri("/users/{userId}", created.getId()).body(Mono.just(created), User.class)
		.exchange().expectStatus().is2xxSuccessful().expectBody(User.class).returnResult().getResponseBody();
		
		assertEquals(created, updated);
	}
	
	@Test
	public void shouldCreateAndThenDeleteUser() {
		User user = new User("someusername", "tobechanged@e.com");
		User created = testClient.post().uri("/users").body(Mono.just(user), User.class)
		.exchange().expectStatus().isCreated().expectBody(User.class)
		.returnResult().getResponseBody();
		
		testClient.delete().uri("/users/{userId}", created.getId())
		.exchange().expectStatus().is2xxSuccessful();
		
		testClient.get().uri("/users/{userId}", created.getId())
		.exchange().expectStatus().isNotFound();
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
	
	@Test
	public void shouldSearchExistingUsersByName() {
		User user = new User("by_name", "by_email@host.com");
		testClient.post().uri("/users").body(Mono.just(user), User.class)
		.exchange().expectStatus().isCreated();
		
		User otherUser = new User("by_name", "other_by_email@otherhost.com");
		testClient.post().uri("/users").body(Mono.just(otherUser), User.class)
		.exchange().expectStatus().isCreated();
		
		User thirdUser = new User("shall_not_be_found", "shall@not.befound");
		testClient.post().uri("/users").body(Mono.just(thirdUser), User.class)
		.exchange().expectStatus().isCreated();
		
		List<User> retrieved = testClient.get().uri("/users/findbyname/{name}", "by_name")
		.exchange().expectStatus().is2xxSuccessful()
		.expectBodyList(User.class).returnResult().getResponseBody();

		Map<String, User> groupedBYEmail = retrieved.stream().collect(Collectors.toMap(User::getEmail, item -> item));
		
		assertEquals(2, retrieved.size());
		
		assertNotNull(groupedBYEmail.get("by_email@host.com"));
		assertNotNull(groupedBYEmail.get("other_by_email@otherhost.com"));
		assertNull(groupedBYEmail.get("shall@not.befound"));
		
	}
	
	@Test
	public void shouldSearchExistingUserByEmail() {
		User user = new User("by_name", "search_by_email@host.com");
		testClient.post().uri("/users").body(Mono.just(user), User.class)
		.exchange().expectStatus().isCreated();
		
		User retrieved = testClient.get().uri("/users/findbyemail/{email}", "search_by_email@host.com")
				.exchange().expectStatus().is2xxSuccessful()
				.expectBody(User.class).returnResult().getResponseBody();
		
		assertNotNull(retrieved);
		assertEquals("by_name", retrieved.getName());
		assertEquals("search_by_email@host.com", retrieved.getEmail());

	}
	
	@Test
	public void shouldEnforceValidityOfUserEmail() {
		User user = new User("by_name", "email");
		testClient.post().uri("/users").body(Mono.just(user), User.class)
		.exchange().expectStatus().is4xxClientError()
		.expectBody()
		.jsonPath("$.message").isEqualTo("email must be a well-formed email address");
	}
}
