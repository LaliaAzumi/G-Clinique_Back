# API Frontend Documentation

## Base URL
```
http://localhost:8080/api/v1
```

## Authentification
Toutes les requêtes protégées nécessitent un token JWT dans le header:
```
Authorization: Bearer <votre_token_jwt>
```

## Endpoints

### Authentification

#### POST `/api/v1/auth/login`
Connexion et récupération du token JWT.

**Request Body:**
```json
{
  "username": "demo",
  "password": "demo123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Authentification réussie",
  "data": {
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "token_type": "bearer",
    "expires_in": 1800,
    "username": "demo"
  }
}
```

**Response (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Identifiants invalides",
  "data": null
}
```

---

#### POST `/api/v1/auth/register`
Création d'un nouveau compte utilisateur.

**Request Body:**
```json
{
  "username": "nouvel_utilisateur",
  "email": "user@example.com",
  "password": "motdepasse123",
  "role": "PATIENT"
}
```

**Roles disponibles:** `ADMIN`, `DOCTOR`, `SECRETARY`, `PATIENT`

---

#### GET `/api/v1/auth/me`
Récupère le profil de l'utilisateur connecté.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "message": "Profil récupéré",
  "data": {
    "id": "1",
    "username": "demo",
    "role": "ADMIN",
    "email": "demo@gmail.com"
  }
}
```

---

#### POST `/api/v1/auth/verify`
Vérifie la validité d'un token JWT.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "message": "Token valide",
  "data": {
    "valid": true,
    "user_id": "1",
    "username": "demo",
    "role": "ADMIN"
  }
}
```

---

#### POST `/api/v1/auth/logout`
Déconnexion (invalide le token côté client).

---

### Utilisateurs (Admin uniquement)

#### GET `/api/v1/users`
Liste tous les utilisateurs.

**Query Parameters (optionnel):**
- `role` - Filtrer par rôle (ex: `?role=ADMIN`)

**Response:**
```json
{
  "success": true,
  "message": "Liste des utilisateurs",
  "data": {
    "users": [
      {"id": 1, "username": "admin", "email": "admin@gmail.com", "role": "ADMIN"},
      {"id": 2, "username": "demo", "email": "demo@gmail.com", "role": "ADMIN"}
    ],
    "count": 2
  }
}
```

---

### Médecins (Admin uniquement)

#### POST `/api/v1/medecins/create-with-user`
Crée un médecin avec son compte utilisateur via FastAPI.

**Headers:**
```
Authorization: Bearer <token_admin>
Content-Type: application/json
```

**Request Body:**
```json
{
  "medecin": {
    "nom": "Dr. Dupont",
    "specialite": "Cardiologie",
    "telephone": "0123456789",
    "adresse": "123 Rue de Paris"
  },
  "username": "drdupont",
  "email": "dupont@clinique.com"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Médecin et compte utilisateur créés avec succès",
  "data": {
    "medecin": {"id": 1, "nom": "Dr. Dupont", ...},
    "user": {"id": 5, "username": "drdupont", "role": "MEDECIN"},
    "password": "auto_generated_password"
  }
}
```

#### POST `/api/v1/medecins/{medecinId}/create-user`
Crée un compte utilisateur pour un médecin existant.

**Request Body:**
```json
{
  "username": "drdupont",
  "email": "dupont@clinique.com"
}
```

#### GET `/api/v1/medecins`
Liste tous les médecins.

#### GET `/api/v1/medecins/{id}`
Récupère un médecin par ID.

---

### Secrétaires (Admin uniquement)

#### POST `/api/v1/secretaires/create`
Crée un compte secrétaire via FastAPI.

**Headers:**
```
Authorization: Bearer <token_admin>
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "secretaire1",
  "email": "secretaire@clinique.com"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Secrétaire créé avec succès",
  "data": {
    "id": 6,
    "username": "secretaire1",
    "email": "secretaire@clinique.com",
    "role": "SECRETAIRE",
    "generatedPassword": "abc123xyz"
  }
}
```

#### GET `/api/v1/secretaires`
Liste tous les secrétaires.

---

## Exemple React

```jsx
// api.js
const API_URL = 'http://localhost:8080/api/v1';

export const login = async (username, password) => {
  const response = await fetch(`${API_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });
  return response.json();
};

export const getProfile = async (token) => {
  const response = await fetch(`${API_URL}/auth/me`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  return response.json();
};

// LoginComponent.jsx
import { useState } from 'react';
import { login } from './api';

function LoginComponent() {
  const [credentials, setCredentials] = useState({ username: '', password: '' });
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    const result = await login(credentials.username, credentials.password);
    
    if (result.success) {
      localStorage.setItem('token', result.data.token);
      // Redirection vers le dashboard
    } else {
      alert(result.message);
    }
  };
  
  return (
    <form onSubmit={handleSubmit}>
      <input 
        type="text" 
        placeholder="Username"
        onChange={(e) => setCredentials({...credentials, username: e.target.value})}
      />
      <input 
        type="password" 
        placeholder="Password"
        onChange={(e) => setCredentials({...credentials, password: e.target.value})}
      />
      <button type="submit">Login</button>
    </form>
  );
}
```

---

## Exemple Next.js

```typescript
// app/api/auth/route.ts (Route Handler)
export async function POST(request: Request) {
  const body = await request.json();
  
  const res = await fetch('http://localhost:8080/api/v1/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  
  const data = await res.json();
  return Response.json(data);
}
```

---

## Codes d'erreur

| Code | Description |
|------|-------------|
| 200 | Succès |
| 400 | Requête invalide |
| 401 | Non autorisé (token manquant ou invalide) |
| 403 | Accès interdit (rôle insuffisant) |
| 503 | Service FastAPI indisponible |

---

## Configuration CORS

L'API accepte les requêtes de toutes les origines (`*`). En production, configurez les origines spécifiques dans `CorsConfig.java`.

---

## Architecture

```
┌─────────────┐     ┌─────────────────┐     ┌─────────────┐     ┌─────────┐
│   React/    │────▶│  Spring Boot    │────▶│   FastAPI   │────▶│  MySQL  │
│  Next.js    │◀────│   (Port 8080)   │◀────│  (Port 8000)│     │         │
│   (Port     │ JSON│                 │ JSON│             │     │         │
│    3000)    │     │ ┌─────────────┐ │     │ Auth + Users│     │         │
└─────────────┘     │ │ FrontendApi │ │     │ Controller  │     │         │
                    │ │   Auth      │ │     └─────────────┘     │         │
                    │ │   Medecin   │ │                           │         │
                    │ │  Secretaire │ │                           │         │
                    │ └─────────────┘ │                           │         │
                    └─────────────────┘                           └─────────┘
```

### Flux de création Médecin:
```
Frontend ──POST /api/v1/medecins/create-with-user──▶ Spring Boot ──POST /api/users/create──▶ FastAPI ──▶ MySQL
```

### Flux de création Secrétaire:
```
Frontend ──POST /api/v1/secretaires/create──▶ Spring Boot ──POST /api/users/create──▶ FastAPI ──▶ MySQL
```
