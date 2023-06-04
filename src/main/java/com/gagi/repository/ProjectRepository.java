package com.gagi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.gagi.domain.Project;

public interface ProjectRepository extends PagingAndSortingRepository<Project, Long>, CrudRepository<Project, Long>{

	Project findByName(@Param("name") String name);
	
}
