package com.erp.clinique.controller;

import com.erp.clinique.model.Consultation;
import com.erp.clinique.model.Users;
import com.erp.clinique.model.MedecinUser;

import com.erp.clinique.service.ConsultationService;
import com.erp.clinique.service.UserService;
import com.erp.clinique.repository.MedecinUserRepository;
import com.erp.clinique.repository.ConsultationRepository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;





import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1/consultations")
public class ConsultationApiController {

    @Autowired
    private ConsultationService consultationService;

    @Autowired
    private UserService userService;

    @Autowired
    private MedecinUserRepository medecinUserRepo;

    @Autowired
    private ConsultationRepository consultationRepository;

    @GetMapping("/medecin/{userId}")
public ResponseEntity<?> getConsultationsByMedecin(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size
) {

    // 1. user
    Users user = userService.findById(userId)
            .orElseThrow(() -> new RuntimeException(
                    "Utilisateur introuvable"));

    // 2. médecin link
    MedecinUser medUser = medecinUserRepo.findByUserId(user.getId());
    if (medUser == null) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Pas médecin"));
    }

    Long medecinId = medUser.getMedecinId();

    // 3. pageable (IMPORTANT)
    Pageable pageable = PageRequest.of(page, size);

    // 4. repo paginé (TON BON METHOD)
    Page<Consultation> consultations =
            consultationRepository.findByRendezVousMedecinId(medecinId, pageable);

    return ResponseEntity.ok(Map.of(
            "medecinId", medecinId,
            "totalElements", consultations.getTotalElements(),
            "totalPages", consultations.getTotalPages(),
            "currentPage", page,
            "data", consultations.getContent()
    ));
}
}