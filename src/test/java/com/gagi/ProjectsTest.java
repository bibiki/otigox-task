package com.gagi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.gagi.domain.Project;
import com.gagi.domain.User;

import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebClient
public class ProjectsTest {

	@Autowired
	private WebTestClient testClient;
	
	@Test
	public void shouldCreateProjectAndThenRetrieveTheSameProject() {
		Project project = new Project("project one", "a simple project");
		testClient.post().uri("/projects").body(Mono.just(project), Project.class)
		.exchange().expectStatus().isCreated()
		.expectHeader().contentType(MediaTypes.HAL_JSON)
		.expectBody()
		.jsonPath("$.name").isEqualTo(project.getName())
		.jsonPath("$.description").isEqualTo(project.getDescription())
		.jsonPath("$._links.project.href").isNotEmpty()
		.jsonPath("$._links.project.href")
		.value((v)-> {
			testClient.get().uri(String.valueOf(v))
			.exchange().expectStatus().isOk()
			.expectHeader().contentType(MediaTypes.HAL_JSON)
			.expectBody()
			.jsonPath("$.name").isEqualTo(project.getName())
			.jsonPath("$.description").isEqualTo(project.getDescription());
		});
	}
	
	@Test
	public void shouldSearchProjectByName() {
		Project project = new Project("project_name", "a simple project");
		testClient.post().uri("/projects").body(Mono.just(project), Project.class)
		.exchange().expectStatus().isCreated();
		
		testClient.get().uri("/projects/search/findByName?name={name}", "project_name")
		.exchange().expectStatus().isOk()
		.expectHeader().contentType(MediaTypes.HAL_JSON)
		.expectBody()
		.jsonPath("$._embedded.projects[0].name").isEqualTo(project.getName())
		.jsonPath("$._embedded.projects[0].description").isEqualTo(project.getDescription())
		.jsonPath("$._embedded.projects[1]").doesNotExist();
	}
}
