-- =====================================================
-- DONNÉES DE TEST POUR CONSULTATIONS ET RENDEZ-VOUS
-- =====================================================
-- Ce script insère des données de test pour tester la page Consultations

-- Insérer des patients de test (si pas déjà présents)
INSERT INTO patients (nom, prenom, email, telephone, date_naissance, adresse) VALUES
('DUPONT', 'Marie', 'marie.dupont@email.com', '0341234567', '1985-03-15', '123 Rue de Paris, Antananarivo'),
('RAKOTO', 'Jean', 'jean.rakoto@email.com', '0342234567', '1990-07-22', '456 Avenue de l\'Indépendance, Antananarivo'),
('ANDRIANARISOA', 'Sophie', 'sophie.andriana@email.com', '0343234567', '1978-11-05', '789 Boulevard Ratsimilaho, Antananarivo'),
('RASOLOFOHERY', 'Paul', 'paul.rasolo@email.com', '0344234567', '1995-01-30', '321 Rue du Commerce, Antananarivo'),
('RAZAFINDRAKOTO', 'Lucie', 'lucie.razafindra@email.com', '0345234567', '1988-09-12', '654 Avenue des Champs, Antananarivo')
ON DUPLICATE KEY UPDATE nom = VALUES(nom);

-- Insérer un médecin de test (si pas déjà présent)
INSERT INTO medecins (nom, prenom, email, telephone, specialite) VALUES
('BOANA', 'Adriano Stanislas', 'adrianostanislas2@gmail.com', '12345690', 'Dermatologie')
ON DUPLICATE KEY UPDATE nom = VALUES(nom);

-- Récupérer les IDs
SET @patient1 = (SELECT id FROM patients WHERE nom = 'DUPONT' LIMIT 1);
SET @patient2 = (SELECT id FROM patients WHERE nom = 'RAKOTO' LIMIT 1);
SET @patient3 = (SELECT id FROM patients WHERE nom = 'ANDRIANARISOA' LIMIT 1);
SET @patient4 = (SELECT id FROM patients WHERE nom = 'RASOLOFOHERY' LIMIT 1);
SET @patient5 = (SELECT id FROM patients WHERE nom = 'RAZAFINDRAKOTO' LIMIT 1);
SET @medecin = (SELECT id FROM medecins WHERE nom = 'BOANA' LIMIT 1);

-- Insérer des rendez-vous pour AUJOURD'HUI (pour tester le filtre "Patients du jour")
INSERT INTO rendez_vous (motif, date, heure, patient_id, medecin_id, statut, google_event_id) VALUES
('Consultation de routine - Check-up annuel', CURDATE(), '08:30:00', @patient1, @medecin, 'EN_ATTENTE', NULL),
('Douleur thoracique - Examen cardiovasculaire', CURDATE(), '09:00:00', @patient2, @medecin, 'EN_ATTENTE', NULL),
('Suivi diabète - Contrôle glycémie', CURDATE(), '10:15:00', @patient3, @medecin, 'EN_ATTENTE', NULL),
('Fièvre persistante depuis 3 jours', CURDATE(), '11:30:00', @patient4, @medecin, 'EN_ATTENTE', NULL),
('Renouvellement ordonnance traitement hypertension', CURDATE(), '14:00:00', @patient5, @medecin, 'EN_ATTENTE', NULL),
('Bilan de santé complet', CURDATE(), '15:30:00', @patient1, @medecin, 'EN_ATTENTE', NULL),
('Consultation ORL - Sinusite', CURDATE(), '16:45:00', @patient3, @medecin, 'EN_ATTENTE', NULL);

-- Insérer des rendez-vous pour DEMAIN
INSERT INTO rendez_vous (motif, date, heure, patient_id, medecin_id, statut, google_event_id) VALUES
('Suivi post-opératoire', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:30:00', @patient2, @medecin, 'EN_ATTENTE', NULL),
('Vaccination annuelle', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00', @patient4, @medecin, 'EN_ATTENTE', NULL);

-- Insérer des rendez-vous pour HIER (déjà terminés avec consultation)
INSERT INTO rendez_vous (motif, date, heure, patient_id, medecin_id, statut, google_event_id) VALUES
('Consultation dermatologie', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:00:00', @patient5, @medecin, 'TERMINE', NULL),
('Examen de la vue', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '11:00:00', @patient1, @medecin, 'TERMINE', NULL);

-- Créer les consultations pour les rendez-vous d'hier (déjà terminés)
INSERT INTO consultations (date, diagnostique, rendez_vous_id, maladie)
SELECT 
    DATE_SUB(CURDATE(), INTERVAL 1 DAY),
    'Dermatite de contact. Prescription crème corticoïde 1% 2x/jour pendant 7 jours.',
    id,
    'Dermatite'
FROM rendez_vous 
WHERE motif = 'Consultation dermatologie' AND date = DATE_SUB(CURDATE(), INTERVAL 1 DAY);

INSERT INTO consultations (date, diagnostique, rendez_vous_id, maladie)
SELECT 
    DATE_SUB(CURDATE(), INTERVAL 1 DAY),
    'Presbytie débutante. Correction +1.5 dioptries recommandée.',
    id,
    'Presbytie'
FROM rendez_vous 
WHERE motif = 'Examen de la vue' AND date = DATE_SUB(CURDATE(), INTERVAL 1 DAY);

-- Afficher les données créées
SELECT 'DONNÉES DE TEST CRÉÉES AVEC SUCCÈS' AS Message;
SELECT CONCAT('Rendez-vous aujourd\'hui: ', COUNT(*)) AS Info FROM rendez_vous WHERE date = CURDATE();
SELECT CONCAT('Rendez-vous total: ', COUNT(*)) AS Info FROM rendez_vous;
