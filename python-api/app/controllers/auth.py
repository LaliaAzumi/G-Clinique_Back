"""
Controller d'authentification
Gère le login et la vérification des tokens JWT
"""
import httpx
from datetime import datetime, timedelta
import jwt
from fastapi import HTTPException, status
from typing import Dict, Any

from config import settings


class AuthController:
    """Service d'authentification - communique avec Spring Boot"""
    
    def __init__(self):
        self.spring_boot_base_url = settings.spring_boot_url
    
    async def validate_user_with_spring(self, username: str, password: str) -> dict:
        """Valide les identifiants via Spring Boot"""
        async with httpx.AsyncClient() as client:
            try:
                response = await client.post(
                    f"{self.spring_boot_base_url}/api/auth/validate",
                    json={"username": username, "password": password},
                    timeout=10.0
                )
                if response.status_code == 200:
                    return response.json()
                return None
            except httpx.RequestError:
                raise HTTPException(
                    status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                    detail="Service Spring Boot indisponible"
                )
    
    async def get_user_info(self, username: str) -> dict:
        """Récupère les infos utilisateur depuis Spring Boot"""
        async with httpx.AsyncClient() as client:
            try:
                response = await client.get(
                    f"{self.spring_boot_base_url}/api/users/{username}",
                    timeout=10.0
                )
                if response.status_code == 200:
                    return response.json()
                return None
            except httpx.RequestError:
                return None
    
    def create_access_token(self, data: dict, expires_delta: timedelta = None) -> str:
        """Crée un token JWT"""
        to_encode = data.copy()
        if expires_delta:
            expire = datetime.utcnow() + expires_delta
        else:
            expire = datetime.utcnow() + timedelta(minutes=settings.access_token_expire_minutes)
        to_encode.update({"exp": expire})
        return jwt.encode(to_encode, settings.secret_key, algorithm=settings.algorithm)
    
    async def login(self, login_data: Dict[str, Any]) -> Dict[str, Any]:
        """Authentifie l'utilisateur et retourne un token"""
        username = login_data.get("username")
        password = login_data.get("password")
        
        user_data = await self.validate_user_with_spring(username, password)
        
        if not user_data or not user_data.get("valid"):
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Nom d'utilisateur ou mot de passe invalide",
                headers={"WWW-Authenticate": "Bearer"},
            )
        
        user_info = await self.get_user_info(username)
        if not user_info:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Utilisateur non trouvé"
            )
        
        access_token_expires = timedelta(minutes=settings.access_token_expire_minutes)
        token_data = {
            "sub": str(user_info.get("id")),
            "username": user_info.get("username"),
            "role": user_info.get("role")
        }
        access_token = self.create_access_token(token_data, access_token_expires)
        
        return {
            "success": True,
            "message": "Authentification réussie",
            "user": {
                "id": user_info.get("id"),
                "username": user_info.get("username"),
                "email": user_info.get("email"),
                "role": user_info.get("role")
            },
            "token": {
                "access_token": access_token,
                "token_type": "bearer",
                "expires_in": settings.access_token_expire_minutes * 60
            }
        }
    
    async def verify_token(self, token: str) -> dict:
        """Vérifie un token JWT"""
        try:
            payload = jwt.decode(token, settings.secret_key, algorithms=[settings.algorithm])
            return payload
        except jwt.ExpiredSignatureError:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Token expiré",
                headers={"WWW-Authenticate": "Bearer"},
            )
        except jwt.JWTError:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Token invalide",
                headers={"WWW-Authenticate": "Bearer"},
            )


# Instance singleton
auth_controller = AuthController()
# À la fin de auth.py
router = None


