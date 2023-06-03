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
	
	@Test
	public void shouldSearchExistingUserByNameAndEmail() {
		User user = new User("by_name", "by_email@host.com");
		testClient.post().uri("/users").body(Mono.just(user), User.class)
		.exchange().expectStatus().isCreated();
		
		testClient.get().uri("/users/search/findByNameAndEmail?name={name}&email={email}", "by_name", "by_email@host.com")
		.exchange().expectStatus().is2xxSuccessful()
		.expectBody()
		.jsonPath("$._embedded.users[0].name").isEqualTo("by_name")
		.jsonPath("$._embedded.users[0].email").isEqualTo("by_email@host.com")
		.jsonPath("$._embedded.users[1]").doesNotExist();
	}
	
	@Test
	public void shouldSearchInexsitentUserByNameAndEmail() {
		//I chose to use an email that I know there is no user for in the database
		User user = new User("by_name", "another_by_email@host.com");
		testClient.post().uri("/users").body(Mono.just(user), User.class)
		.exchange().expectStatus().isCreated();
		
		testClient.get().uri("/users/search/findByNameAndEmail?name={name}&email={email}", "by_name", "email@host.com")
		.exchange().expectStatus().is2xxSuccessful()
		.expectBody()
		.jsonPath("$._embedded.users[0]").doesNotExist();
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
