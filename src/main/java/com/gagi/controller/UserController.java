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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.gagi.domain.User;
import com.gagi.repository.UserRepository;

@RestController
@RequestMapping("/users")
public class UserController {

	@Autowired
	UserRepository userRepository;

	@PostMapping(consumes = "application/json")
	@ResponseStatus(HttpStatus.CREATED)
	public User save(@RequestBody User user) {
		return userRepository.save(user);
	}

	@GetMapping(path = "/{userId}")
	public User getUserById(@PathVariable("userId") Long userId) {
		return userRepository.findById(userId).orElseThrow();
	}

	@GetMapping(path = "/{page}/{size}")
	public Iterable<User> getUsers(@PathVariable(required = true, name = "page") Integer page, @PathVariable(required = true, name = "size") Integer size) {
		PageRequest pageRequest = PageRequest.of(page, size);
		return userRepository.findAll(pageRequest).getContent();
	}
	
	@GetMapping
	public Iterable<User> getUsers() {
		PageRequest pageRequest = PageRequest.of(0, 10);
		return userRepository.findAll(pageRequest).getContent();
	}

	@GetMapping(path = "/findbyname/{name}")
	public Iterable<User> getByName(@PathVariable(required = true, name = "name") String name) {
		PageRequest pageRequest = PageRequest.of(0, 10);
		return userRepository.findByName(name, pageRequest);
	}
	
	@GetMapping(path = "/findbyname/{name}/{page}/{size}")
	public Iterable<User> getByNameWithPaginationSpecified(@PathVariable(required = true, name = "name") String name,
			@PathVariable(required = true) Integer page, @PathVariable(required = true) Integer size) {
		PageRequest pageRequest = PageRequest.of(page, size);
		return userRepository.findByName(name, pageRequest);
	}

	@GetMapping(path = "/findbyemail/{email}")
	public User getByEmail(@PathVariable(required = true, name = "email") String email) {
		return userRepository.findByEmail(email);
	}

	@PutMapping(path = "/{userId}", consumes = "application/json")
	public User update(@PathVariable("userId") Long userId, @RequestBody User user) {
		user.setId(userId);
		return userRepository.save(user);
	}

	@PatchMapping(path = "/{userId}", consumes = "application/json")
	public User patchUser(@PathVariable("userId") Long userId, @RequestBody User patch) {
		User user = userRepository.findById(userId).orElseThrow();
		if (patch.getEmail() != null) {
			user.setEmail(patch.getEmail());
		}
		if (patch.getName() != null) {
			user.setName(patch.getName());
		}
		return userRepository.save(user);
	}

	@DeleteMapping(path = "/{userId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("userId") Long userId) {
		userRepository.deleteById(userId);
	}
}
