"""
G-Clinique FastAPI Application - Point d'entrée
Architecture propre et organisée
"""
from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from typing import Optional, Dict, Any
from fastapi.middleware.cors import CORSMiddleware

from config import settings
from app.controllers import (
    auth_controller,
    medecins_router,
    secretaires_router,
    patients_router,
    consultations_router,
    rendezvous_router,
    ordonnances_router,
    medicaments_router,
    notifications_router,
    calendar_router,
    chambres_router
)
from app.services import user_service

# Application FastAPI
app = FastAPI(
    title="G-Clinique API",
    description="API FastAPI - Proxy vers Spring Boot pour la gestion médicale",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8081"],  # ton frontend React
    allow_credentials=True,
    allow_methods=["*"],  # autorise GET, POST, PUT, DELETE…
    allow_headers=["*"],  # autorise tous les headers (y compris Authorization)
)

# Inclusion des routeurs par domaine
app.include_router(medecins_router)
app.include_router(secretaires_router)
app.include_router(patients_router)
app.include_router(consultations_router)
app.include_router(rendezvous_router)
app.include_router(ordonnances_router)
app.include_router(medicaments_router)
app.include_router(notifications_router)
app.include_router(calendar_router)
app.include_router(chambres_router)

# Sécurité Bearer JWT
security = HTTPBearer()


# ============ AUTHENTIFICATION ============

@app.post("/api/auth/login")
async def login(login_data: Dict[str, Any]):
    """Authentifie un utilisateur et retourne un token JWT"""
    return await auth_controller.login(login_data)


@app.post("/api/auth/verify")
async def verify_token(credentials: HTTPAuthorizationCredentials = Depends(security)):
    """Vérifie la validité d'un token JWT"""
    payload = await auth_controller.verify_token(credentials.credentials)
    return {
        "valid": True,
        "user_id": payload.get("sub"),
        "username": payload.get("username"),
        "role": payload.get("role")
    }


@app.get("/api/auth/health")
async def health_check():
    """Vérifie la santé du service"""
    return {"status": "healthy", "service": "g-clinique-api", "version": "1.0.0"}


# ============ GESTION DES UTILISATEURS ============

@app.post("/api/users/create")
async def create_user(user_data: Dict[str, Any]):
    """Crée un nouvel utilisateur (délègue à Spring Boot)"""
    try:
        result = await user_service.create_user(user_data)
        return result
    except ValueError as e:
        raise HTTPException(status_code=409, detail=str(e))
    except RuntimeError as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/api/users/list")
async def list_users(role: Optional[str] = None):
    """Liste tous les utilisateurs"""
    try:
        users = await user_service.list_users(role)
        return {"users": users, "count": len(users)}
    except RuntimeError as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/api/users/{username}")
async def get_user(username: str):
    """Récupère un utilisateur par username"""
    user = await user_service.get_user(username)
    if not user:
        raise HTTPException(status_code=404, detail="Utilisateur non trouvé")
    return user


@app.delete("/api/users/{user_id}")
async def delete_user(user_id: int):
    """Supprime un utilisateur par ID"""
    try:
        success = await user_service.delete_user(user_id)
        if not success:
            raise HTTPException(status_code=404, detail="Utilisateur non trouvé")
        return {"message": "Utilisateur supprimé avec succès"}
    except RuntimeError as e:
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
