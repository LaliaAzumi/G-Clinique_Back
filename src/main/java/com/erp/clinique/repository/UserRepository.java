package com.erp.clinique.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.erp.clinique.model.Users;

public interface UserRepository extends JpaRepository<Users, Long> {
	Optional<Users> findByEmail(String email);
	
    Optional<Users> findByUsername(String username);
    
    List<Users> findByRole(String role);
    
    Page<Users> findByRole(String role, Pageable pageable);
    
    @Query("""
    	    SELECT u FROM Users u
    	    WHERE u.role = :role
    	    AND (
    	        LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
    	        OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
    	    )
    	""")
    	Page<Users> searchByRole(
    	        @Param("role") String role,
    	        @Param("keyword") String keyword,
    	        Pageable pageable
    	);
}