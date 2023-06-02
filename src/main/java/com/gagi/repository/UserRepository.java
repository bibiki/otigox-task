package com.gagi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.gagi.domain.User;

public interface UserRepository extends PagingAndSortingRepository<User, Long>, CrudRepository<User, Long>{

}
