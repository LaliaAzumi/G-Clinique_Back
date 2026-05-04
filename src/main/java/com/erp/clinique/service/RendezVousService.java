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
import com.erp.clinique.model.Medecin;
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
import java.util.Map;

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


    //partie secrétaire : create rdv
    public RendezVous createSimple(
        Long patientId,
        Long medecinId,
        LocalDate date,
        LocalTime heure,
        String motif
    ) {
        // 1. Vérif patient
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new RuntimeException("Patient introuvable"));

        // 2. Vérif médecin
        var medecin = medecinRepository.findById(medecinId)
            .orElseThrow(() -> new RuntimeException("Médecin introuvable"));

        // 3. Vérif disponibilité (tu l’as déjà 🔥)
        List<LocalTime> heuresDisponibles = getHeuresDisponibles(medecinId, date);
        if (!heuresDisponibles.contains(heure)) {
            throw new RuntimeException("Créneau indisponible");
        }

        // 4. Création
        RendezVous rdv = new RendezVous();
        rdv.setPatient(patient);
        rdv.setMedecin(medecin);
        rdv.setDate(date);
        rdv.setHeure(heure);
        rdv.setMotif(motif);
        rdv.setStatut("EN_ATTENTE");

        return rendezVousRepository.save(rdv);
    }

    // Partie secrétaire : update rdv
    public RendezVous updateSimple(Long id, Map<String, Object> data) {
        RendezVous rdv = rendezVousRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("RDV introuvable"));

        if (data.get("patientId") != null) {
            Long patientId = Long.valueOf(data.get("patientId").toString());
            Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient introuvable"));
            rdv.setPatient(patient);
        }

        if (data.get("medecinId") != null) {
            Long medecinId = Long.valueOf(data.get("medecinId").toString());
            Medecin medecin = medecinRepository.findById(medecinId)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable"));
            rdv.setMedecin(medecin);
        }

        if (data.get("date") != null) {
            rdv.setDate(LocalDate.parse(data.get("date").toString()));
        }

        if (data.get("heure") != null) {
            rdv.setHeure(LocalTime.parse(data.get("heure").toString()));
        }

        if (data.get("motif") != null) {
            rdv.setMotif((String) data.get("motif"));
        }

        if (data.get("statut") != null) {
            rdv.setStatut(data.get("statut").toString());
        }

        return rendezVousRepository.save(rdv);
    }

    //partie secrétaire : lister rdv avec paiement
    public List<RendezVous> findAllWithPaiement() {
        return rendezVousRepository.findAllWithPaiement();
    }
}
