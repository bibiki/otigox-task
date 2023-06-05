package com.gagi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.gagi.domain.Project;
import com.gagi.domain.User;
import com.gagi.repository.ProjectRepository;
import com.gagi.repository.UserRepository;

@RestController
@RequestMapping("/projects")
public class ProjectController {

	@Autowired
	ProjectRepository projectRepository;
	@Autowired
	UserRepository userRepository;

	@PostMapping(consumes = "application/json")
	@ResponseStatus(HttpStatus.CREATED)
	public Project save(@RequestBody Project project) {
		return projectRepository.save(project);
	}

	@GetMapping("/{page}/{size}")
	public Iterable<Project> getProjects(@PathVariable(required = true, name = "page") Integer page,
			@PathVariable(required = true, name = "size") Integer size) {
		PageRequest pageRequest = PageRequest.of(page, size);
		return projectRepository.findAll(pageRequest).getContent().stream()
				.map(e -> new Project(e.getId(), e.getName(), e.getDescription())).toList();
	}

	@GetMapping
	public Iterable<Project> getProjects() {
		PageRequest pageRequest = PageRequest.of(0, 10);
		return projectRepository.findAll(pageRequest).getContent().stream()
				.map(e -> new Project(e.getId(), e.getName(), e.getDescription())).toList();
	}

	@GetMapping(path = "/{projectId}")
	public Project getProjectById(@PathVariable("projectId") Long projectId) {
		return projectRepository.findById(projectId).orElseThrow();
	}

	@PutMapping(path = "/{projectId}", consumes = "application/json")
	public Project update(@PathVariable("projectId") Long projectId, @RequestBody Project project) {
		Project fromDb = projectRepository.findById(projectId).orElseThrow();
		if(project.getName() != null) {
			fromDb.setName(project.getName());
		}
		if(project.getDescription() != null) {
			fromDb.setDescription(project.getDescription());
		}
		return projectRepository.save(fromDb);
	}

	@PutMapping(path = "/assign/{projectId}/{userId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void assignUserToProject(@PathVariable("projectId") Long projectId, @PathVariable("userId") Long userId) {
		Project project = projectRepository.findById(projectId).orElseThrow();
		User user = userRepository.findById(userId).orElseThrow();
		project.getUsers().add(user);
		projectRepository.save(project);
	}

	@PutMapping(path = "/remove/{projectId}/{userId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeUserFromProject(@PathVariable("projectId") Long projectId, @PathVariable("userId") Long userId) {
		Project project = projectRepository.findById(projectId).orElseThrow();
		project.getUsers().removeIf(u -> u.getId() == userId);
		projectRepository.save(project);
	}

	@DeleteMapping("/{projectId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("projectId") Long projectId) {
		projectRepository.deleteById(projectId);
	}

	@GetMapping(path = "/findbyname/{name}")
	public Project getProjectByName(@PathVariable("name") String name) {
		return projectRepository.findByName(name);
	}
}
