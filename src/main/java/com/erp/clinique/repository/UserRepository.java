package com.erp.clinique.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.erp.clinique.model.Users;

public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByUsername(String username);
    
 // Ajout pour récupérer tous les users d’un rôle donné
    List<Users> findByRole(String role);
}