package com.erp.clinique.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.erp.clinique.model.ActeMedical;
import com.erp.clinique.model.Paiement;
import com.erp.clinique.model.Patient;
import com.erp.clinique.model.Prestation;
import com.erp.clinique.model.RendezVous;
import com.erp.clinique.repository.ActeMedicalRepository;
import com.erp.clinique.repository.MedecinRepository;
import com.erp.clinique.repository.PaiementRepository;
import com.erp.clinique.repository.PatientRepository;
import com.erp.clinique.repository.PrestationRepository;
import com.erp.clinique.repository.RendezVousRepository;

@Service
@Transactional
public class RendezVousService {

    @Autowired
    private RendezVousRepository rendezVousRepository;
    @Autowired
    private PaiementRepository paiementRepository;
    @Autowired
    private PrestationRepository prestationRepository;
    @Autowired
    private ActeMedicalRepository acteMedicalRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private MedecinRepository medecinRepository;
    @Autowired
    private PatientService patientService;
    
    

    public List<RendezVous> findAll() {
        return rendezVousRepository.findAll();
    }

    public Optional<RendezVous> findById(Long id) {
        return rendezVousRepository.findById(id);
    }

    public RendezVous save(RendezVous rendezVous) {
        return rendezVousRepository.save(rendezVous);
    }

    public void deleteById(Long id) {
        rendezVousRepository.deleteById(id);
    }

    public List<RendezVous> findByMedecinId(Long medecinId) {
        return rendezVousRepository.findByMedecinId(medecinId);
    }

    public List<RendezVous> findByPatientId(Long patientId) {
        return rendezVousRepository.findByPatientId(patientId);
    }

    public List<RendezVous> findByStatut(String statut) {
        return rendezVousRepository.findByStatut(statut);
    }
    
    public List<RendezVous> findByMedecinIdAndDateBetween(Long medecinId, LocalDate start, LocalDate end) {
        return rendezVousRepository.findByMedecinIdAndDateBetween(medecinId, start, end);
    }
    
    public List<LocalTime> getHeuresDisponibles(Long medecinId, LocalDate date) {
        List<RendezVous> rdvs = rendezVousRepository.findByMedecinIdAndDate(medecinId, date)
            .stream()
            .filter(r -> !r.getStatut().equals("ANNULE")) 
            .collect(Collectors.toList());

        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(18, 0);
        Duration dureeRdv = Duration.ofMinutes(60);

        List<LocalTime> heuresDisponibles = new ArrayList<>();
        for (LocalTime t = start; t.isBefore(end); t = t.plus(dureeRdv)) {
            final LocalTime horaire = t; 
            boolean libre = rdvs.stream().noneMatch(r -> r.getHeure().equals(horaire));
            if (libre) heuresDisponibles.add(horaire);
        }

        return heuresDisponibles;
    }
    
    public List<RendezVous> search(String keyword, LocalDate date, String statut) {
       
        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null; 
        String searchStatut = (statut != null && !statut.trim().isEmpty()) ? statut : null;

        return rendezVousRepository.searchRendezVous(searchKeyword, date, searchStatut);
    }
    
    
    
    
    
    // Partie patient : Création complète (Patient + RDV + Paiement + Prestations)
    @Transactional
    public RendezVous enregistrerRendezVousComplet(
        Patient patientInfo, 
        Long medecinId, 
        LocalDate date, 
        LocalTime heure, 
        List<Long> acteIds, 
        String nomExpediteur, 
        String codeTransaction, 
        Double montantEnvoye
    ) {
        // 1. GESTION DU PATIENT (Création ou Récupération)
        Patient patientFinal;
        
        // On vérifie si le patient existe déjà par son email
        if (patientService.existsByEmail(patientInfo.getEmail())) {
            // S'il existe, on le récupère via le service
            patientFinal = patientService.getByEmail(patientInfo.getEmail());
           
            patientRepository.save(patientFinal);
        } else {
            // Si c'est un nouveau, on l'enregistre proprement
            patientFinal = patientRepository.save(patientInfo);
        }

        // 2. VÉRIFICATION DISPONIBILITÉ (Sécurité double réservation)
        List<LocalTime> heuresDisponibles = getHeuresDisponibles(medecinId, date);
        if (!heuresDisponibles.contains(heure)) {
            throw new RuntimeException("Ce créneau n'est plus disponible. Veuillez en choisir un autre parmis "+heuresDisponibles);
        }

        // 3. CRÉATION DU RENDEZ-VOUS
        RendezVous rdv = new RendezVous();
        rdv.setPatient(patientFinal);
        rdv.setMedecin(medecinRepository.findById(medecinId)
            .orElseThrow(() -> new RuntimeException("Médecin non trouvé")));
        rdv.setDate(date);
        rdv.setHeure(heure);
        rdv.setStatut("EN_ATTENTE_VALIDATION");
        rdv.setMotif("Consultation en ligne");
        RendezVous rdvSauvegarde = rendezVousRepository.save(rdv);

        // 4. CRÉATION DU PAIEMENT (Preuve de transaction)
        Paiement paiement = new Paiement();
        paiement.setNomExpediteur(nomExpediteur);
        paiement.setCodeTransaction(codeTransaction);
        paiement.setMontantEnvoye(montantEnvoye);
        paiement.setStatut("EN_COURS"); // La secrétaire devra valider
        paiement.setRendezvous(rdvSauvegarde);
        paiementRepository.save(paiement);

        // 5. CRÉATION DES PRESTATIONS (Détails des actes)
        if (acteIds != null && !acteIds.isEmpty()) {
            for (Long acteId : acteIds) {
                ActeMedical acte = acteMedicalRepository.findById(acteId)
                    .orElseThrow(() -> new RuntimeException("Acte médical non trouvé ID: " + acteId));
                
                // On utilise le constructeur que tu as créé précédemment
                Prestation p = new Prestation(rdvSauvegarde, acte, acte.getPrix());
                prestationRepository.save(p);
            }
        }

        return rdvSauvegarde;
    }
}
