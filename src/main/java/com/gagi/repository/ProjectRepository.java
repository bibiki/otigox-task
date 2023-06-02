package com.gagi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.gagi.domain.Project;

public interface ProjectRepository extends PagingAndSortingRepository<Project, Long>, CrudRepository<Project, Long>{

}
