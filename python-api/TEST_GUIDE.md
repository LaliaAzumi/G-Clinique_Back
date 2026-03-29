# Guide de Test Complet - API G-Clinique FastAPI

## 🚀 Démarrage

### Prérequis
```bash
# Vérifier que Spring Boot est démarré sur le port 8080
curl http://localhost:8080/api/health

# Démarrer FastAPI (si ce n'est pas déjà fait)
cd python-api
python main.py
```

### URLs de base
- **FastAPI** : http://localhost:8000
- **Spring Boot** : http://localhost:8080
- **Documentation FastAPI** : http://localhost:8000/docs

---

## 🔐 ÉTAPE 1 : Authentification

### 1.1 Login (récupérer le token)
```bash
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Réponse attendue :**
```json
{
  "success": true,
  "message": "Authentification réussie",
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@gclinique.com",
    "role": "ADMIN"
  },
  "token": {
    "access_token": "eyJ0eXAiOiJKV1Qi...",
    "token_type": "bearer",
    "expires_in": 1800
  }
}
```

### 1.2 Vérifier le token
```bash
# Remplacer <TOKEN> par le token reçu
curl -X POST http://localhost:8000/api/auth/verify \
  -H "Authorization: Bearer <TOKEN>"
```

### 1.3 Health Check
```bash
curl http://localhost:8000/api/auth/health
```

---

## 👨‍⚕️ ÉTAPE 2 : Médecins

### 2.1 Liste des médecins
```bash
curl http://localhost:8000/api/v1/medecins \
  -H "Authorization: Bearer <TOKEN>"
```

### 2.2 Détail d'un médecin
```bash
curl http://localhost:8000/api/v1/medecins/1 \
  -H "Authorization: Bearer <TOKEN>"
```

### 2.3 Créer un médecin avec compte utilisateur
```bash
curl -X POST http://localhost:8000/api/v1/medecins/create-with-user \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Martin",
    "prenom": "Pierre",
    "email": "dr.martin@gclinique.com",
    "specialite": "Cardiologie",
    "telephone": "0123456789",
    "username": "dr.martin",
    "password": "temp123"
  }'
```

---

## 👩‍💼 ÉTAPE 3 : Secrétaires

### 3.1 Liste des secrétaires
```bash
curl http://localhost:8000/api/v1/secretaires \
  -H "Authorization: Bearer <TOKEN>"
```

### 3.2 Créer une secrétaire
```bash
curl -X POST http://localhost:8000/api/v1/secretaires/create \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Dupont",
    "prenom": "Marie",
    "email": "marie.dupont@gclinique.com",
    "telephone": "0987654321",
    "username": "secretaire1",
    "password": "temp123"
  }'
```

---

## 👥 ÉTAPE 4 : Utilisateurs

### 4.1 Liste des utilisateurs
```bash
curl http://localhost:8000/api/users/list \
  -H "Authorization: Bearer <TOKEN>"
```

### 4.2 Liste par rôle
```bash
curl "http://localhost:8000/api/users/list?role=MEDECIN" \
  -H "Authorization: Bearer <TOKEN>"
```

### 4.3 Détail d'un utilisateur
```bash
curl http://localhost:8000/api/users/admin \
  -H "Authorization: Bearer <TOKEN>"
```

### 4.4 Créer un utilisateur
```bash
curl -X POST http://localhost:8000/api/users/create \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@email.com",
    "password": "password123",
    "role": "PATIENT"
  }'
```

---

## 🧑‍⚕️ ÉTAPE 5 : Patients

### 5.1 Liste des patients (pagination)
```bash
curl "http://localhost:8000/api/v1/patients?page=0&size=10" \
  -H "Authorization: Bearer <TOKEN>"
```

### 5.2 Recherche de patients
```bash
curl "http://localhost:8000/api/v1/patients/search?keyword=dupont" \
  -H "Authorization: Bearer <TOKEN>"
```

### 5.3 Détail d'un patient
```bash
curl http://localhost:8000/api/v1/patients/1 \
  -H "Authorization: Bearer <TOKEN>"
```

### 5.4 Créer un patient
```bash
curl -X POST http://localhost:8000/api/v1/patients/save \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Durand",
    "prenom": "Sophie",
    "email": "sophie.durand@email.com",
    "telephone": "0145678901",
    "dateNaissance": "1990-05-15",
    "adresse": "15 Rue de Paris",
    "genre": "FEMININ"
  }'
```

### 5.5 Modifier un patient
```bash
curl -X PUT http://localhost:8000/api/v1/patients/update \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "nom": "Durand",
    "prenom": "Sophie Marie",
    "email": "sophie.durand@email.com",
    "telephone": "0145678901"
  }'
```

### 5.6 Supprimer un patient
```bash
curl -X DELETE http://localhost:8000/api/v1/patients/1 \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 🏥 ÉTAPE 6 : Consultations

### 6.1 Liste des consultations
```bash
curl http://localhost:8000/api/v1/consultations \
  -H "Authorization: Bearer <TOKEN>"
```

### 6.2 Consultations par patient
```bash
curl http://localhost:8000/api/v1/consultations/patient/1 \
  -H "Authorization: Bearer <TOKEN>"
```

### 6.3 Consultations par médecin
```bash
curl http://localhost:8000/api/v1/consultations/medecin/1 \
  -H "Authorization: Bearer <TOKEN>"
```

### 6.4 Créer une consultation
```bash
curl -X POST http://localhost:8000/api/v1/consultations/save \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-03-28",
    "patientId": 1,
    "medecinId": 1,
    "motif": "Consultation generale",
    "diagnostic": "Patient en bonne sante",
    "traitement": "Aucun"
  }'
```

---

## 📅 ÉTAPE 7 : Rendez-vous

### 7.1 Liste des rendez-vous
```bash
curl http://localhost:8000/api/v1/rendez-vous \
  -H "Authorization: Bearer <TOKEN>"
```

### 7.2 Rendez-vous filtrés
```bash
curl "http://localhost:8000/api/v1/rendez-vous?date=2024-03-28&medecin_id=1" \
  -H "Authorization: Bearer <TOKEN>"
```

### 7.3 Créer un rendez-vous
```bash
curl -X POST http://localhost:8000/api/v1/rendez-vous/save \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-03-28",
    "heure": "14:00",
    "patientId": 1,
    "medecinId": 1,
    "motif": "Consultation de suivi",
    "statut": "EN_ATTENTE"
  }'
```

### 7.4 Confirmer un rendez-vous
```bash
curl -X POST http://localhost:8000/api/v1/rendez-vous/1/confirm \
  -H "Authorization: Bearer <TOKEN>"
```

### 7.5 Supprimer un rendez-vous
```bash
curl -X DELETE http://localhost:8000/api/v1/rendez-vous/1 \
  -H "Authorization: Bearer <TOKEN>"
```

### 7.6 Calendrier (events)
```bash
curl "http://localhost:8000/api/v1/rendez-vous/calendar/events?start_date=2024-03-01&end_date=2024-03-31" \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 💊 ÉTAPE 8 : Ordonnances

### 8.1 Liste des ordonnances
```bash
curl http://localhost:8000/api/v1/ordonnances \
  -H "Authorization: Bearer <TOKEN>"
```

### 8.2 Ordonnances par patient
```bash
curl http://localhost:8000/api/v1/ordonnances/patient/1 \
  -H "Authorization: Bearer <TOKEN>"
```

### 8.3 Créer une ordonnance
```bash
curl -X POST http://localhost:8000/api/v1/ordonnances/save \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "medecinId": 1,
    "date": "2024-03-28",
    "medicaments": [
      {
        "medicamentId": 1,
        "dosage": "500mg",
        "frequence": "3 fois par jour",
        "duree": "7 jours"
      }
    ],
    "instructions": "Prendre apres les repas"
  }'
```

### 8.4 Générer PDF
```bash
curl http://localhost:8000/api/v1/ordonnances/1/pdf \
  -H "Authorization: Bearer <TOKEN>"
```

### 8.5 Envoyer par email
```bash
curl -X POST http://localhost:8000/api/v1/ordonnances/1/send-email \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 💊 ÉTAPE 9 : Médicaments

### 9.1 Liste des médicaments
```bash
curl "http://localhost:8000/api/v1/medicaments?page=0&size=10" \
  -H "Authorization: Bearer <TOKEN>"
```

### 9.2 Recherche de médicaments
```bash
curl "http://localhost:8000/api/v1/medicaments/search?keyword=doliprane" \
  -H "Authorization: Bearer <TOKEN>"
```

### 9.3 Créer un médicament
```bash
curl -X POST http://localhost:8000/api/v1/medicaments/save \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Efferalgan",
    "description": "Paracetamol 500mg effervescent",
    "type": "Comprime effervescent",
    "stock": 100,
    "prix": 6.20
  }'
```

---

## 🔔 ÉTAPE 10 : Notifications

### 10.1 Liste des notifications
```bash
curl http://localhost:8000/api/v1/notifications \
  -H "Authorization: Bearer <TOKEN>"
```

### 10.2 Notifications non lues
```bash
curl http://localhost:8000/api/v1/notifications/unread \
  -H "Authorization: Bearer <TOKEN>"
```

### 10.3 Compte des non lues
```bash
curl http://localhost:8000/api/v1/notifications/count \
  -H "Authorization: Bearer <TOKEN>"
```

### 10.4 Marquer comme lue
```bash
curl -X POST http://localhost:8000/api/v1/notifications/1/mark-read \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 📆 ÉTAPE 11 : Calendrier

### 11.1 Events du calendrier
```bash
curl "http://localhost:8000/api/v1/calendar/events?start_date=2024-03-01&end_date=2024-03-31" \
  -H "Authorization: Bearer <TOKEN>"
```

### 11.2 Disponibilités d'un médecin
```bash
curl "http://localhost:8000/api/v1/calendar/disponibilites/1?date=2024-03-28" \
  -H "Authorization: Bearer <TOKEN>"
```

### 11.3 Statistiques
```bash
curl "http://localhost:8000/api/v1/calendar/stats?start_date=2024-03-01&end_date=2024-03-31" \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 📋 Scénario de Test Complet

### Scénario 1 : Création d'un nouveau patient avec RDV
```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | \
  python -c "import sys,json; print(json.load(sys.stdin)['token']['access_token'])")

echo "Token: $TOKEN"

# 2. Créer un patient
PATIENT_ID=$(curl -s -X POST http://localhost:8000/api/v1/patients/save \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nom":"Test","prenom":"Patient","email":"test@email.com","telephone":"0102030405"}' | \
  python -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',{}).get('id',0))")

echo "Patient ID: $PATIENT_ID"

# 3. Créer un RDV pour ce patient
curl -X POST http://localhost:8000/api/v1/rendez-vous/save \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"date\":\"2024-03-28\",\"heure\":\"10:00\",\"patientId\":$PATIENT_ID,\"medecinId\":1,\"motif\":\"Premiere consultation\"}"
```

### Scénario 2 : Ordonnance complète
```bash
# 1. Créer une consultation
CONSULT_ID=$(curl -s -X POST http://localhost:8000/api/v1/consultations/save \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"date":"2024-03-28","patientId":1,"medecinId":1,"motif":"Consultation","diagnostic":"OK"}' | \
  python -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('id',0))")

# 2. Créer une ordonnance liée
curl -X POST http://localhost:8000/api/v1/ordonnances/save \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"patientId\":1,\"medecinId\":1,\"date\":\"2024-03-28\",\"medicaments\":[{\"medicamentId\":1,\"dosage\":\"500mg\",\"frequence\":\"3x/jour\",\"duree\":\"7j\"}],\"instructions\":\"Apres repas\"}"

# 3. Générer le PDF
curl http://localhost:8000/api/v1/ordonnances/1/pdf \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🐛 Dépannage

### Erreur 401 - Token invalide
- Vérifiez que le token n'est pas expiré (30 minutes)
- Reconnectez-vous pour obtenir un nouveau token

### Erreur 503 - Spring Boot indisponible
- Vérifiez que Spring Boot est démarré sur le port 8080
- Vérifiez la configuration `spring_boot_url` dans `.env`

### Erreur 404 - Ressource non trouvée
- Vérifiez que l'ID existe dans la base de données
- Vérifiez l'URL de l'endpoint

---

## 📚 Documentation

- **Swagger UI** : http://localhost:8000/docs
- **ReDoc** : http://localhost:8000/redoc
- **OpenAPI JSON** : http://localhost:8000/openapi.json

---

## 🔧 Variables d'environnement (.env)

```env
SPRING_BOOT_URL=http://localhost:8080
SECRET_KEY=votre-cle-secrete-pour-jwt
ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=30
```

---

**✅ Test terminé !**

Si tous les tests passent, votre API FastAPI est correctement configurée et connectée à Spring Boot.
