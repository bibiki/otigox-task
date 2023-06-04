package com.gagi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.gagi.domain.Project;
import com.gagi.domain.User;

import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebClient
public class ProjectControllerTest {

	@Autowired
	private WebTestClient testClient;
	
	@Test
	public void shouldCreateProjectAndThenRetrieveTheSameProject() {
		Project project = new Project("project one", "a simple project");
		
		Project retrieved = testClient.post().uri("/projects").body(Mono.just(project), Project.class)
		.exchange().expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody(Project.class).returnResult().getResponseBody();
		
		project.setId(retrieved.getId());
		
		assertEquals(project, retrieved);
	}
	
	@Test
	public void shouldSearchProjectByName() {
		Project project = new Project("project_name", "a simple project");
		testClient.post().uri("/projects").body(Mono.just(project), Project.class)
		.exchange().expectStatus().isCreated();
		
		Project retrieved = testClient.get().uri("/projects/findbyname/{name}", "project_name")
		.exchange().expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody(Project.class).returnResult().getResponseBody();
		
		assertNotNull(retrieved);
		
		project.setId(retrieved.getId());
		assertEquals(project, retrieved);
	}
	
	@Test
	public void getProjectByIdShouldReturnProjectWithAllFields() {
		User user = new User("Project User", "project_user@company.com");
		User otherUser = new User("Other Project User", "other_project_user@company.com");
		User userWithId = testClient.post().uri("/users").body(Mono.just(user), User.class)
		.exchange().expectStatus().isCreated().expectBody(User.class).returnResult().getResponseBody();
		User otherUserWithId = testClient.post().uri("/users").body(Mono.just(otherUser), User.class)
		.exchange().expectStatus().isCreated().expectBody(User.class).returnResult().getResponseBody();
		
		Project project = new Project("Project", "Project description");

		Project projectWithId = testClient.post().uri("/projects").body(Mono.just(project), Project.class)
		.exchange().expectStatus().isCreated().expectBody(Project.class).returnResult().getResponseBody();
		
		testClient.put().uri("/projects/assign/{projectId}/{userId}", projectWithId.getId(), userWithId.getId())
		.exchange().expectStatus().is2xxSuccessful();
		testClient.put().uri("/projects/assign/{projectId}/{userId}", projectWithId.getId(), otherUserWithId.getId())
		.exchange().expectStatus().is2xxSuccessful();
		
		Project retrieved = testClient.get().uri("/projects/{id}", projectWithId.getId())
				.exchange().expectBody(Project.class).returnResult().getResponseBody();

		assertNotNull(retrieved);
		assertNotNull(retrieved.getUsers());
		assertEquals(2, retrieved.getUsers().size());
		assertTrue(retrieved.getId() > 0);
		assertEquals("Project", retrieved.getName());
		assertEquals("Project description", retrieved.getDescription());
	}
	
	@Test
	public void shouldAssignUserToProject() {
		User user = new User("Assignable user", "assignable@email.com");
		User userWithId = testClient.post().uri("/users").body(Mono.just(user), User.class)
		.exchange().expectStatus().isCreated().expectBody(User.class).returnResult().getResponseBody();
		
		Project project = new Project("Assigned project", "Some project for user to be assigned to");
		Project projectWithId = testClient.post().uri("/projects").body(Mono.just(project), Project.class)
				.exchange().expectStatus().isCreated().expectBody(Project.class).returnResult().getResponseBody();
		
		assertTrue(projectWithId.getUsers().isEmpty());
		
		testClient.put().uri("/projects/assign/{projectId}/{userId}", projectWithId.getId(), userWithId.getId())
		.exchange().expectStatus().is2xxSuccessful();
		
		Project retrieved = testClient.get().uri("/projects/{id}", projectWithId.getId())
				.exchange().expectBody(Project.class).returnResult().getResponseBody();
		
		assertTrue(retrieved.getUsers().contains(userWithId));
	}
}
