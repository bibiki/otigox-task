package com.gagi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.gagi.domain.Project;
import com.gagi.domain.User;
import com.gagi.repository.ProjectRepository;
import com.gagi.repository.UserRepository;

@RestController("/projects")
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
	
	@GetMapping(params = {"page", "size"})
	public Iterable<Project> getProjects(@PathVariable("page") int page, @PathVariable("size") int size) {
		PageRequest pageRequest = PageRequest.of(page, size);
		return projectRepository.findAll(pageRequest).getContent();
	}
	
	@PutMapping(path="/{projectId}", consumes="application/json")
	public Project update(@PathVariable("projectId") Long projectId, @RequestBody Project project) {
		project.setId(projectId);
		return projectRepository.save(project);
	}
	
	@PutMapping(path="/assign/{projectId}/{userId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void assignUserToProject(@PathVariable("projectId") Long projectId, @PathVariable("userId") Long userId) {
		Project project = projectRepository.findById(projectId).orElseThrow();
		User user = userRepository.findById(userId).orElseThrow();
		project.getUsers().add(user);
		projectRepository.save(project);
	}
	
	@PutMapping(path="/remove/{projectId}/{userId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeUserFromProject(@PathVariable("projectId") Long projectId, @PathVariable("userId") Long userId) {
		Project project = projectRepository.findById(projectId).orElseThrow();
		project.getUsers().removeIf(u -> u.getId()==userId);
		projectRepository.save(project);
	}
	
	@DeleteMapping("/projectId")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("projectId") Long projectId) {
		projectRepository.deleteById(projectId);
	}
}
