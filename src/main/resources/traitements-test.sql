-- =====================================================
-- DONNÉES DE TEST POUR TRAITEMENTS PAR SPÉCIALITÉ
-- =====================================================
-- Ce script insère des traitements (médicaments + actes médicaux) 
-- catégorisés par spécialité médicale

-- 1. CRÉER LES CATÉGORIES (Spécialités médicales)
INSERT INTO categories_traitement (nom, description, code) VALUES
('Cardiologie', 'Spécialité dédiée aux maladies du cœur et du système cardiovasculaire', 'CARDIO'),
('Dermatologie', 'Spécialité de la peau, des ongles et des cheveux', 'DERMA'),
('Imagerie Médicale', 'Examens par imagerie: scanner, radiographie, échographie', 'IMAGE'),
('Biologie Clinique', 'Examens de laboratoire: sang, urine, microbiologie', 'BIO'),
('Médecine Générale', 'Traitements et médicaments usuels', 'MG'),
('ORL', 'Oto-rhino-laryngologie: oreilles, nez, gorge', 'ORL'),
('Ophtalmologie', 'Spécialité des yeux et de la vision', 'OPHTA'),
('Rhumatologie', 'Maladies des articulations et système osseux', 'RHUM')
ON DUPLICATE KEY UPDATE nom = VALUES(nom);

-- Récupérer les IDs des catégories
SET @cat_cardio = (SELECT id FROM categories_traitement WHERE code = 'CARDIO');
SET @cat_derma = (SELECT id FROM categories_traitement WHERE code = 'DERMA');
SET @cat_image = (SELECT id FROM categories_traitement WHERE code = 'IMAGE');
SET @cat_bio = (SELECT id FROM categories_traitement WHERE code = 'BIO');
SET @cat_mg = (SELECT id FROM categories_traitement WHERE code = 'MG');
SET @cat_orl = (SELECT id FROM categories_traitement WHERE code = 'ORL');
SET @cat_ophta = (SELECT id FROM categories_traitement WHERE code = 'OPHTA');
SET @cat_rhum = (SELECT id FROM categories_traitement WHERE code = 'RHUM');

-- 2. TRAITEMENTS CARDIOLOGIE (Médicaments + Actes)
INSERT INTO traitements (nom, description, type, prix, categorie_id, forme, dosage) VALUES
('Aspirine Cardio', 'Anti-agrégant plaquettaire pour prévention cardiovasculaire', 'MEDICAMENT', 15000, @cat_cardio, 'Comprimé', '100mg'),
('Atorvastatine', 'Hypocholestérolémiant - Statine', 'MEDICAMENT', 25000, @cat_cardio, 'Comprimé', '20mg'),
('Bisoprolol', 'Bêtabloquant pour hypertension et insuffisance cardiaque', 'MEDICAMENT', 18000, @cat_cardio, 'Comprimé', '5mg'),
('ECG (Électrocardiogramme)', 'Enregistrement de l\'activité électrique du cœur', 'ACTE_MEDICAL', 35000, @cat_cardio, NULL, NULL),
('Échographie Cardiaque', 'Échographie Doppler cardiaque complète', 'ACTE_MEDICAL', 75000, @cat_cardio, NULL, NULL),
('Holter ECG', 'Enregistrement continu ECG sur 24h', 'ACTE_MEDICAL', 55000, @cat_cardio, NULL, NULL),
('Amlodipine', 'Inhibiteur calcique pour hypertension artérielle', 'MEDICAMENT', 20000, @cat_cardio, 'Comprimé', '5mg'),
('Furosemide', 'Diurétique pour insuffisance cardiaque', 'MEDICAMENT', 12000, @cat_cardio, 'Comprimé', '40mg')
ON DUPLICATE KEY UPDATE nom = VALUES(nom);

-- 3. TRAITEMENTS DERMATOLOGIE
INSERT INTO traitements (nom, description, type, prix, categorie_id, forme, dosage) VALUES
('Dermocorticoid Crème', 'Corticoïde topique pour dermatites', 'MEDICAMENT', 22000, @cat_derma, 'Crème', '0.1%'),
('Antibiotic Derma', 'Antibiotique local pour infections cutanées', 'MEDICAMENT', 18000, @cat_derma, 'Pommade', '2%'),
('Biopsie Cutanée', 'Prélèvement de tissu cutané pour analyse', 'ACTE_MEDICAL', 45000, @cat_derma, NULL, NULL),
('Cryothérapie', 'Destruction de lésions par le froid', 'ACTE_MEDICAL', 35000, @cat_derma, NULL, NULL),
('Aciclovir Derma', 'Antiviral pour herpès cutané', 'MEDICAMENT', 28000, @cat_derma, 'Crème', '5%'),
('Isotretinoin', 'Traitement de l\'acné sévère', 'MEDICAMENT', 45000, @cat_derma, 'Gélule', '20mg')
ON DUPLICATE KEY UPDATE nom = VALUES(nom);

-- 4. TRAITEMENTS IMAGERIE MÉDICALE (Actes uniquement)
INSERT INTO traitements (nom, description, type, prix, categorie_id, forme, dosage) VALUES
('Scanner Thoracique', 'TDM thorax sans injection', 'ACTE_MEDICAL', 150000, @cat_image, NULL, NULL),
('Scanner Cérébral', 'TDM cerveau avec injection', 'ACTE_MEDICAL', 180000, @cat_image, NULL, NULL),
('Radiographie Thorax', 'Radio de face et profil', 'ACTE_MEDICAL', 35000, @cat_image, NULL, NULL),
('Radiographie Os', 'Radio membre ou bassin', 'ACTE_MEDICAL', 30000, @cat_image, NULL, NULL),
('Échographie Abdominale', 'Écho complet abdomen', 'ACTE_MEDICAL', 55000, @cat_image, NULL, NULL),
('Échographie Pelvienne', 'Écho pelvis homme/femme', 'ACTE_MEDICAL', 50000, @cat_image, NULL, NULL),
('IRM Cérébrale', 'Imagerie par résonance magnétique', 'ACTE_MEDICAL', 250000, @cat_image, NULL, NULL)
ON DUPLICATE KEY UPDATE nom = VALUES(nom);

-- 5. TRAITEMENTS BIOLOGIE CLINIQUE (Examens de labo)
INSERT INTO traitements (nom, description, type, prix, categorie_id, forme, dosage) VALUES
('NFS (Numération Formule Sanguine)', 'Hémogramme complet', 'ACTE_MEDICAL', 25000, @cat_bio, NULL, NULL),
('Ionogramme Sanguin', 'Na, K, Cl, Ca', 'ACTE_MEDICAL', 30000, @cat_bio, NULL, NULL),
('Bilan Hépatique', 'ASAT, ALAT, PAL, GGT, Bilirubine', 'ACTE_MEDICAL', 40000, @cat_bio, NULL, NULL),
('Bilan Rénal', 'Créatinine, Urée, Ionogramme', 'ACTE_MEDICAL', 35000, @cat_bio, NULL, NULL),
('Glycémie à jeun', 'Taux de glucose sanguin', 'ACTE_MEDICAL', 15000, @cat_bio, NULL, NULL),
('HbA1C', 'Hémoglobine glyquée', 'ACTE_MEDICAL', 28000, @cat_bio, NULL, NULL),
('Bilan Lipidique', 'Cholestérol total, HDL, LDL, Triglycérides', 'ACTE_MEDICAL', 38000, @cat_bio, NULL, NULL),
('TP/INR', 'Temps de prothrombine', 'ACTE_MEDICAL', 22000, @cat_bio, NULL, NULL),
('Groupage Sanguin', 'ABO + Rhésus', 'ACTE_MEDICAL', 20000, @cat_bio, NULL, NULL),
('Sérologie Typhoïde', 'Recherche anticorps Salmonella', 'ACTE_MEDICAL', 32000, @cat_bio, NULL, NULL),
('Sérologie Paludisme', 'Recherche Plasmodium', 'ACTE_MEDICAL', 28000, @cat_bio, NULL, NULL),
('Urétroculture', 'Analyse urines + antibiogramme', 'ACTE_MEDICAL', 35000, @cat_bio, NULL, NULL)
ON DUPLICATE KEY UPDATE nom = VALUES(nom);

-- 6. TRAITEMENTS MÉDECINE GÉNÉRALE (Médicaments courants)
INSERT INTO traitements (nom, description, type, prix, categorie_id, forme, dosage) VALUES
('Paracétamol', 'Antalgique et antipyrétique', 'MEDICAMENT', 8000, @cat_mg, 'Comprimé', '500mg'),
('Ibuprofène', 'Anti-inflammatoire non stéroïdien', 'MEDICAMENT', 12000, @cat_mg, 'Comprimé', '400mg'),
('Amoxicilline', 'Antibiotique à large spectre', 'MEDICAMENT', 18000, @cat_mg, 'Gélule', '500mg'),
('Omeprazole', 'Inhibiteur de la pompe à protons', 'MEDICAMENT', 22000, @cat_mg, 'Gélule', '20mg'),
('Doliprane', 'Paracétamol effervescent', 'MEDICAMENT', 15000, @cat_mg, 'Effervescent', '500mg'),
('Spasfon', 'Antispasmodique', 'MEDICAMENT', 20000, @cat_mg, 'Comprimé', '80mg'),
('Smecta', 'Antidiarrhéique', 'MEDICAMENT', 18000, @cat_mg, 'Sachet', '3g'),
('Vitamine C', 'Complément vitamines', 'MEDICAMENT', 10000, @cat_mg, 'Comprimé', '500mg')
ON DUPLICATE KEY UPDATE nom = VALUES(nom);

-- 7. TRAITEMENTS ORL
INSERT INTO traitements (nom, description, type, prix, categorie_id, forme, dosage) VALUES
('Aerius', 'Antihistaminique pour rhinite allergique', 'MEDICAMENT', 25000, @cat_orl, 'Comprimé', '5mg'),
('Nasoclean', 'Sérum physiologique nasal', 'MEDICAMENT', 12000, @cat_orl, 'Spray', '100ml'),
('Otostick', 'Antiseptique auriculaire', 'MEDICAMENT', 18000, @cat_orl, 'Gouttes', '10ml'),
('Audiométrie', 'Test d\'audition tonal', 'ACTE_MEDICAL', 40000, @cat_orl, NULL, NULL),
('Fibroscopie ORL', 'Endoscopie nasale et laryngée', 'ACTE_MEDICAL', 60000, @cat_orl, NULL, NULL)
ON DUPLICATE KEY UPDATE nom = VALUES(nom);

-- 8. TRAITEMENTS OPHTALMOLOGIE
INSERT INTO traitements (nom, description, type, prix, categorie_id, forme, dosage) VALUES
('Visine', 'Collyre lubrifiant', 'MEDICAMENT', 15000, @cat_ophta, 'Collyre', '10ml'),
('Voltaren Ophta', 'Anti-inflammatoire ophtalmique', 'MEDICAMENT', 25000, @cat_ophta, 'Collyre', '5ml'),
('Acuitis', 'Collyre antibiotique', 'MEDICAMENT', 30000, @cat_ophta, 'Collyre', '5ml'),
('Acuité Visuelle', 'Test de vision + réfraction', 'ACTE_MEDICAL', 35000, @cat_ophta, NULL, NULL),
('Fond d\'Oeil', 'Examen du fond d\'œil au phoroptère', 'ACTE_MEDICAL', 30000, @cat_ophta, NULL, NULL),
('Tension Oculaire', 'Mesure de la PIO (tonométrie)', 'ACTE_MEDICAL', 25000, @cat_ophta, NULL, NULL)
ON DUPLICATE KEY UPDATE nom = VALUES(nom);

-- 9. TRAITEMENTS RHUMATOLOGIE
INSERT INTO traitements (nom, description, type, prix, categorie_id, forme, dosage) VALUES
('Doliprane Rhuma', 'Paracétamol pour douleurs articulaires', 'MEDICAMENT', 10000, @cat_rhum, 'Comprimé', '1000mg'),
('Diclofénac', 'AINS pour douleurs rhumatismales', 'MEDICAMENT', 15000, @cat_rhum, 'Gélule', '50mg'),
('Methotrexate', 'Antirhumatismal modificateur', 'MEDICAMENT', 45000, @cat_rhum, 'Comprimé', '10mg'),
('Infiltration', 'Injection intra-articulaire de corticoïdes', 'ACTE_MEDICAL', 55000, @cat_rhum, NULL, NULL),
('Échographie Articulaire', 'Échographie des articulations', 'ACTE_MEDICAL', 60000, @cat_rhum, NULL, NULL)
ON DUPLICATE KEY UPDATE nom = VALUES(nom);

-- Vérification des données insérées
SELECT 'DONNÉES DE TEST TRAITEMENTS INSÉRÉES AVEC SUCCÈS' AS Message;
SELECT 
    c.nom AS Categorie,
    COUNT(t.id) AS NombreTraitements,
    SUM(CASE WHEN t.type = 'MEDICAMENT' THEN 1 ELSE 0 END) AS Medicaments,
    SUM(CASE WHEN t.type = 'ACTE_MEDICAL' THEN 1 ELSE 0 END) AS ActesMedicaux
FROM categories_traitement c
LEFT JOIN traitements t ON t.categorie_id = c.id
GROUP BY c.nom;
