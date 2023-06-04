package com.gagi.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.gagi.domain.Project;

public interface ProjectRepository extends PagingAndSortingRepository<Project, Long>, CrudRepository<Project, Long>{

	List<Project> findByName(@Param("name") String name);
	
}
