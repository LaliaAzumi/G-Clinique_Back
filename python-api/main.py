from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from auth_controller import auth_controller
from user_manager import user_manager
from schemas import LoginRequest, AuthResponse, UserCreateRequest, UserResponse
from typing import Optional, List

app = FastAPI(
    title="Auth & User API",
    description="Microservice FastAPI pour l'authentification et la gestion des utilisateurs",
    version="1.1.0"
)

security = HTTPBearer()  # Schéma de sécurité Bearer pour les tokens JWT

@app.post("/api/auth/login", response_model=AuthResponse)
async def login(login_data: LoginRequest):
    """Endpoint d'authentification : valide l'utilisateur et retourne un token JWT"""
    return await auth_controller.login(login_data)

@app.post("/api/auth/verify")
async def verify_token(credentials: HTTPAuthorizationCredentials = Depends(security)):
    """Endpoint de vérification : valide la légitimité d'un token JWT"""
    token = credentials.credentials  # Extrait le token du header Authorization
    payload = await auth_controller.verify_token(token)
    return {
        "valid": True,  # Token valide
        "user_id": payload.get("sub"),  # ID de l'utilisateur
        "username": payload.get("username"),  # Nom d'utilisateur
        "role": payload.get("role")  # Rôle de l'utilisateur
    }

@app.get("/api/auth/health")
async def health_check():
    """Endpoint de vérification de santé du service"""
    return {"status": "healthy", "service": "auth-api"}

# ============ ENDPOINTS DE GESTION DES UTILISATEURS ============

@app.post("/api/users/create", response_model=UserResponse)
async def create_user(user_data: UserCreateRequest):
    """
    Endpoint de creation d'un nouvel utilisateur
    Delegue a Spring Boot via user_manager
    """
    try:
        result = await user_manager.create_user(user_data)
        return result
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except RuntimeError as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/api/users/list")
async def list_users(role: Optional[str] = None):
    """
    Liste tous les utilisateurs via Spring Boot
    """
    try:
        users = await user_manager.list_users(role)
        return {"users": users, "count": len(users)}
    except RuntimeError as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/api/users/{username}")
async def get_user(username: str):
    """
    Recupere les informations d'un utilisateur par son username via Spring Boot
    """
    user = await user_manager.get_user_by_username(username)
    if not user:
        raise HTTPException(status_code=404, detail="Utilisateur non trouve")
    return user

@app.delete("/api/users/{user_id}")
async def delete_user(user_id: int):
    """
    Supprime un utilisateur par son ID via Spring Boot
    """
    try:
        success = await user_manager.delete_user(user_id)
        if not success:
            raise HTTPException(status_code=404, detail="Utilisateur non trouve")
        return {"message": "Utilisateur supprime avec succes"}
    except RuntimeError as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
