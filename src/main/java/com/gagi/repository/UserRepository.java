package com.gagi.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.gagi.domain.User;

public interface UserRepository extends PagingAndSortingRepository<User, Long>, CrudRepository<User, Long>{

	List<User> findByName(@Param("name") String name);
	
	User findByEmail(@Param("email") String email);
}
