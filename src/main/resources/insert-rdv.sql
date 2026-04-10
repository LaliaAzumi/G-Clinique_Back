-- Créer un rendez-vous pour médecin_id=16 et patient_id=6
INSERT INTO rendez_vous (motif, date, heure, patient_id, medecin_id, statut, google_event_id) 
VALUES 
('Consultation programmée - Contrôle médical', CURDATE(), '09:30:00', 6, 16, 'EN_ATTENTE', NULL);

-- Vérifier le rendez-vous créé
SELECT 
    rv.id,
    rv.date,
    rv.heure,
    rv.motif,
    rv.statut,
    CONCAT(p.prenom, ' ', p.nom) AS patient,
    CONCAT(m.prenom, ' ', m.nom) AS medecin
FROM rendez_vous rv
JOIN patients p ON rv.patient_id = p.id
JOIN medecins m ON rv.medecin_id = m.id
WHERE rv.patient_id = 6 AND rv.medecin_id = 16 AND rv.date = CURDATE();
