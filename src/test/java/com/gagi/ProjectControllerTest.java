package com.gagi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.gagi.domain.Project;
import com.gagi.domain.User;

import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebClient
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
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
	
	@Test
	public void projectListingShouldShowProjectsWithoutUsersAssignedToThem() {
		Project project = new Project("projectname", "project description");
		Project projectWithId = testClient.post().uri("/projects").body(Mono.just(project), Project.class)
		.exchange().expectStatus().isCreated().expectBody(Project.class).returnResult().getResponseBody();
		
		User user = new User("Assignable user", "assignable@email.com");
		User userWithId = testClient.post().uri("/users").body(Mono.just(user), User.class)
		.exchange().expectStatus().isCreated().expectBody(User.class).returnResult().getResponseBody();
		
		testClient.put().uri("/projects/assign/{projectId}/{userId}", projectWithId.getId(), userWithId.getId())
		.exchange().expectStatus().is2xxSuccessful();

		Project projectWithUser = testClient.get().uri("/projects/{projectId}", projectWithId.getId())
				.exchange().expectStatus().is2xxSuccessful().expectBody(Project.class).returnResult().getResponseBody();
		assertFalse(projectWithUser.getUsers().isEmpty());
		
		List<Project> retrieved = testClient.get().uri("/projects")
				.exchange().expectStatus().is2xxSuccessful()
				.expectBodyList(Project.class).returnResult().getResponseBody();
		
		assertTrue(retrieved.size() == 1);
		assertTrue(retrieved.get(0).getUsers().isEmpty());
	}
	
	@Test
	public void shouldTestPaginationOfProjectListing() {
		List<String> projectnames = Arrays.asList("One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve");
		for(String projectname : projectnames) {
			Project project = new Project(projectname, projectname + " description");
			testClient.post().uri("/projects").body(Mono.just(project), Project.class)
			.exchange().expectStatus().isCreated();
		}
		
		List<Project> retrieved = testClient.get().uri("/projects")
				.exchange().expectStatus().is2xxSuccessful()
				.expectBodyList(Project.class).returnResult().getResponseBody();
		assertEquals(10, retrieved.size());
		
		retrieved = testClient.get().uri("/projects?page=1&size=10")
				.exchange().expectStatus().is2xxSuccessful()
				.expectBodyList(Project.class).returnResult().getResponseBody();
		assertEquals(2, retrieved.size());
		
		retrieved = testClient.get().uri("/projects?page=1&size=7")
				.exchange().expectStatus().is2xxSuccessful()
				.expectBodyList(Project.class).returnResult().getResponseBody();
		assertEquals(5, retrieved.size());
		
		retrieved = testClient.get().uri("/projects?page=3&size=8")
				.exchange().expectStatus().is2xxSuccessful()
				.expectBodyList(Project.class).returnResult().getResponseBody();
		assertEquals(0, retrieved.size());
	}
	
	@Test
	public void shouldCreateProjectAndThenUpdateItsDescription() {
		Project project = new Project("name", "dscrptn");
		
		Project created = testClient.post().uri("/projects").body(Mono.just(project), Project.class)
		.exchange().expectStatus().isCreated().expectBody(Project.class).returnResult().getResponseBody();
		
		created.setDescription("description");
		
		Project updated = testClient.put().uri("/projects/{projectId}", created.getId()).body(Mono.just(created), Project.class)
		.exchange().expectStatus().is2xxSuccessful().expectBody(Project.class).returnResult().getResponseBody();
		
		assertEquals("description", updated.getDescription());
	}
	
	@Test
	public void shouldCreateProjectAndThenDeleteIt() {
		Project project = new Project("name", "dscrptn");
		
		Project created = testClient.post().uri("/projects").body(Mono.just(project), Project.class)
		.exchange().expectStatus().isCreated().expectBody(Project.class).returnResult().getResponseBody();
		assertNotNull(created);
		
		testClient.delete().uri("/projects/{projectId}", created.getId())
		.exchange().expectStatus().is2xxSuccessful();
		
		testClient.get().uri("/projects/{projectId}", created.getId())
		.exchange().expectStatus().isNotFound();
		
	}
	
	@Test
	public void shouldAddMultipleUsersToMultipleProjects() {
		Project one = new Project("one project", "one project description");
		Project other = new Project("other project", "other project description");
		
		Project oneProjectCreated = testClient.post().uri("/projects").body(Mono.just(one), Project.class)
		.exchange().expectStatus().isCreated().expectBody(Project.class).returnResult().getResponseBody();
		Project otherProjectCreated = testClient.post().uri("/projects").body(Mono.just(other), Project.class)
				.exchange().expectStatus().isCreated().expectBody(Project.class).returnResult().getResponseBody();
		
		User oneUser = new User("One user", "one@email.com");
		User otherUser = new User("Other user", "other@email.com");

		User oneUserCreated = testClient.post().uri("/users").body(Mono.just(oneUser), User.class)
		.exchange().expectStatus().isCreated().expectBody(User.class).returnResult().getResponseBody();
		User otherUserCreated = testClient.post().uri("/users").body(Mono.just(otherUser), User.class)
		.exchange().expectStatus().isCreated().expectBody(User.class).returnResult().getResponseBody();

		testClient.put().uri("/projects/assign/{projectId}/{userId}", oneProjectCreated.getId(), oneUserCreated.getId())
		.exchange().expectStatus().is2xxSuccessful();
		testClient.put().uri("/projects/assign/{projectId}/{userId}", oneProjectCreated.getId(), otherUserCreated.getId())
		.exchange().expectStatus().is2xxSuccessful();
		testClient.put().uri("/projects/assign/{projectId}/{userId}", otherProjectCreated.getId(), oneUserCreated.getId())
		.exchange().expectStatus().is2xxSuccessful();
		testClient.put().uri("/projects/assign/{projectId}/{userId}", otherProjectCreated.getId(), otherUserCreated.getId())
		.exchange().expectStatus().is2xxSuccessful();

		List<User> expectedUsers = Arrays.asList(oneUserCreated, otherUserCreated);
		
		Project first = testClient.get().uri("/projects/{projectId}", oneProjectCreated.getId())
				.exchange().expectBody(Project.class).returnResult().getResponseBody();
		Project second = testClient.get().uri("/projects/{projectId}", otherProjectCreated.getId())
				.exchange().expectBody(Project.class).returnResult().getResponseBody();

		assertNotNull(first);
		assertNotNull(second);
		
		assertTrue(first.getUsers().size() == 2);
		assertTrue(first.getUsers().containsAll(expectedUsers));

		assertTrue(second.getUsers().size() == 2);
		assertTrue(second.getUsers().containsAll(expectedUsers));
	}
}
