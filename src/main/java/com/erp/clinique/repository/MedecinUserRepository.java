package com.erp.clinique.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.erp.clinique.model.MedecinUser;

@Repository
public interface MedecinUserRepository extends JpaRepository<MedecinUser, Long> {
	MedecinUser findByUserId(Long userId);
	Optional<MedecinUser> findByMedecinId(Long medecinId);

}
