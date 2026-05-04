# G-Clinique FastAPI - Architecture

## Structure du projet (organisée et lisible)

```
python-api/
├── app/                          # Code source principal
│   ├── __init__.py              # Package app
│   ├── controllers/             # Endpoints API (par domaine)
│   │   ├── auth.py            # Authentification (login, verify)
│   │   ├── medecins.py        # Gestion des médecins
│   │   ├── secretaires.py     # Gestion des secrétaires
│   │   ├── patients.py        # Gestion des patients
│   │   └── ...                # Autres contrôleurs
│   ├── services/              # Logique métier
│   │   └── users.py           # Gestion des utilisateurs
│   └── utils/                 # Utilitaires
│       └── http_client.py     # Client HTTP Spring Boot
├── config.py                  # Configuration (URL Spring Boot, JWT)
├── main.py                    # Point d'entrée FastAPI
├── requirements.txt           # Dépendances Python
└── README.md                  # Ce fichier
```

## Principes de l'architecture

### 1. Séparation des responsabilités
- **Controllers** : Gèrent uniquement les endpoints HTTP (routes, validation, réponse)
- **Services** : Contiennent la logique métier (communication avec Spring Boot)
- **Utils** : Fonctions réutilisables (client HTTP, helpers)

### 2. Communication avec Spring Boot
- FastAPI ne touche **jamais** directement la base de données
- Toutes les opérations DB passent par Spring Boot via HTTP
- Le client HTTP est centralisé dans `app/utils/http_client.py`

### 3. Authentification
- JWT générés et vérifiés par FastAPI
- Spring Boot valide les identifiants (login/password)
- Token envoyé dans le header `Authorization: Bearer <token>`

## Comment ajouter un nouveau contrôleur

1. Créer un fichier dans `app/controllers/` (ex: `consultations.py`)
2. Définir un `APIRouter` avec prefix et tags
3. Implémenter les endpoints (GET, POST, PUT, DELETE)
4. Utiliser `verify_token()` pour sécuriser les endpoints
5. Déleguer à Spring Boot via HTTP client
6. Ajouter le router dans `main.py` avec `app.include_router()`

## Exemple de contrôleur simple

```python
from fastapi import APIRouter, HTTPException, Header
from typing import Dict, Any
import httpx
from config import settings

router = APIRouter(prefix="/api/v1/exemple", tags=["Exemple"])

async def verify_token(auth_header: str) -> dict:
    # Vérifie le token avec Spring Boot
    ...

@router.get("")
async def list_items(authorization: str = Header(...)):
    """Liste les items"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        response = await client.get(
            f"{settings.spring_boot_url}/api/v1/items",
            headers={"Authorization": authorization}
        )
        return response.json()
```

## Variables d'environnement (optionnel)

Créer un fichier `.env` :
```
SPRING_BOOT_URL=http://localhost:9090
SECRET_KEY=your-secret-key
ACCESS_TOKEN_EXPIRE_MINUTES=30
```

## Lancer l'application

```bash
cd python-api
python main.py
```

## Tests avec Postman

Importer la collection : `postman_collection.json`

## Points importants

- **Code simple** : Pas de complexité inutile
- **Commentaires** : Docstrings sur chaque fonction
- **Typage** : Utilisation de `typing` pour la clarté
- **Erreurs** : Gestion claire des codes HTTP (401, 403, 404, 409, 500, 503)
