"""
Gestionnaire de creation d'utilisateurs via FastAPI
Ce module delegue la creation des utilisateurs a Spring Boot
FastAPI ne fait que controller, pas de connexion directe a la base
"""
import bcrypt
import httpx
from config import settings
from schemas import UserCreateRequest, UserResponse
from typing import Optional, List

class UserManager:
    """Gere la creation et la gestion des utilisateurs via Spring Boot"""
    
    def __init__(self):
        self.spring_boot_url = settings.spring_boot_url
        self.client = httpx.AsyncClient(timeout=10.0)
    
    async def create_user(self, user_data: UserCreateRequest) -> UserResponse:
        """
        Cree un nouvel utilisateur via Spring Boot
        FastAPI ne touche pas directement a la base de donnees
        """
        # Hache le mot de passe avec bcrypt
        password_hash = bcrypt.hashpw(
            user_data.password.encode('utf-8'), 
            bcrypt.gensalt()
        ).decode('utf-8')
        
        # Prepare les donnees pour Spring Boot
        user_payload = {
            "username": user_data.username,
            "email": user_data.email,
            "password": password_hash,
            "role": user_data.role,
            "firstLogin": True
        }
        
        try:
            # Appelle Spring Boot pour creer l'utilisateur
            response = await self.client.post(
                f"{self.spring_boot_url}/api/internal/users/create",
                json=user_payload
            )
            
            if response.status_code == 409:
                raise ValueError(f"L'utilisateur '{user_data.username}' ou l'email '{user_data.email}' existe deja")
            
            if response.status_code != 200:
                raise RuntimeError(f"Erreur Spring Boot: {response.status_code}")
            
            result = response.json()
            
            return UserResponse(
                id=result.get("id", 0),
                username=user_data.username,
                email=user_data.email,
                role=user_data.role,
                first_login=True,
                message="Utilisateur cree avec succes via Spring Boot"
            )
                
        except httpx.RequestError as e:
            raise RuntimeError(f"Erreur de connexion a Spring Boot: {e}")
    
    async def get_user_by_username(self, username: str) -> Optional[dict]:
        """Recupere les informations d'un utilisateur via Spring Boot"""
        try:
            response = await self.client.get(
                f"{self.spring_boot_url}/api/internal/users/{username}"
            )
            if response.status_code == 200:
                return response.json()
            return None
        except httpx.RequestError:
            return None
    
    async def list_users(self, role: Optional[str] = None) -> List[dict]:
        """Liste tous les utilisateurs via Spring Boot"""
        try:
            params = {}
            if role:
                params["role"] = role
                
            response = await self.client.get(
                f"{self.spring_boot_url}/api/internal/users/list",
                params=params
            )
            if response.status_code == 200:
                result = response.json()
                return result.get("users", [])
            return []
        except httpx.RequestError as e:
            raise RuntimeError(f"Erreur lors de la recuperation des utilisateurs: {e}")
    
    async def delete_user(self, user_id: int) -> bool:
        """Supprime un utilisateur via Spring Boot"""
        try:
            response = await self.client.delete(
                f"{self.spring_boot_url}/api/internal/users/{user_id}"
            )
            return response.status_code == 200
        except httpx.RequestError:
            return False

# Instance globale du gestionnaire d'utilisateurs
user_manager = UserManager()
