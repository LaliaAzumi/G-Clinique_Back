package com.erp.clinique.service;

import com.erp.clinique.dto.DashboardDTO;
import com.erp.clinique.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final RendezVousRepository rdvRepository;
    private final PaiementRepository paiementRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrestationRepository prestationRepository;

    public DashboardService(
            UserRepository userRepository,
            RendezVousRepository rdvRepository,
            PaiementRepository paiementRepository,
            PrescriptionRepository prescriptionRepository,
            PrestationRepository prestationRepository
    ) {
        this.userRepository = userRepository;
        this.rdvRepository = rdvRepository;
        this.paiementRepository = paiementRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.prestationRepository = prestationRepository;
    }

    public DashboardDTO getDashboard() {

        DashboardDTO dto = new DashboardDTO();

        dto.setTotalMedecins(userRepository.countMedecins());
        dto.setTotalPatients(userRepository.countPatients());

        dto.setTotalRdv(rdvRepository.countAll());
        dto.setRdvEnAttente(rdvRepository.countEnAttente());
        dto.setRdvTermine(rdvRepository.countTermine());

        dto.setTotalPaiement(Optional.ofNullable(paiementRepository.totalPaiement()).orElse(0.0));
        dto.setTotalMedicaments(Optional.ofNullable(prescriptionRepository.totalMedicaments()).orElse(0.0));
        dto.setTotalPrestations(Optional.ofNullable(prestationRepository.totalPrestations()).orElse(0.0));

        dto.setRdvParJour(format(rdvRepository.getRdvParJour()));
        // dto.setPaiementParJour(format(paiementRepository.paiementParJour()));

        return dto;
    }

    private List<Map<String, Object>> format(List<Object[]> data) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] obj : data) {
            Map<String, Object> map = new HashMap<>();
            map.put("date", obj[0]);
            map.put("value", obj[1]);
            result.add(map);
        }

        return result;
    }
}