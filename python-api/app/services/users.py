"""
Service de gestion des utilisateurs
Délègue toutes les opérations à Spring Boot (pas de DB directe)
"""
import bcrypt
import httpx
from typing import Optional, List, Dict, Any

from config import settings


class UserService:
    """Gère les utilisateurs via Spring Boot"""
    
    def __init__(self):
        self.spring_boot_url = settings.spring_boot_url
        self.client = httpx.AsyncClient(timeout=10.0)
    
    async def create_user(self, user_data: Dict[str, Any]) -> Dict[str, Any]:
        """Crée un utilisateur via Spring Boot"""
        # Hash du mot de passe
        password = user_data.get("password", "")
        password_hash = bcrypt.hashpw(
            password.encode('utf-8'), 
            bcrypt.gensalt()
        ).decode('utf-8')
        
        # Prépare les données pour Spring Boot
        user_payload = {
            "username": user_data.get("username"),
            "email": user_data.get("email"),
            "password": password_hash,
            "role": user_data.get("role"),
            "firstLogin": True
        }
        
        try:
            response = await self.client.post(
                f"{self.spring_boot_url}/api/internal/users/create",
                json=user_payload
            )
            
            if response.status_code == 409:
                error_data = response.json()
                error_msg = error_data.get("error", "User already exists")
                raise ValueError(f"{error_msg}")
            
            if response.status_code != 200:
                raise RuntimeError(f"Erreur Spring Boot: {response.status_code}")
            
            result = response.json()
            
            return {
                "id": result.get("id", 0),
                "username": user_data.get("username"),
                "email": user_data.get("email"),
                "role": user_data.get("role"),
                "first_login": True,
                "message": "Utilisateur créé avec succès"
            }
            
        except httpx.RequestError as e:
            raise RuntimeError(f"Erreur de connexion à Spring Boot: {e}")
    
    async def get_user(self, username: str) -> Optional[dict]:
        """Récupère un utilisateur par username"""
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
        """Liste tous les utilisateurs"""
        try:
            params = {"role": role} if role else {}
            response = await self.client.get(
                f"{self.spring_boot_url}/api/internal/users/list",
                params=params
            )
            if response.status_code == 200:
                return response.json().get("users", [])
            return []
        except httpx.RequestError as e:
            raise RuntimeError(f"Erreur: {e}")
    
    async def delete_user(self, user_id: int) -> bool:
        """Supprime un utilisateur"""
        try:
            response = await self.client.delete(
                f"{self.spring_boot_url}/api/internal/users/{user_id}"
            )
            return response.status_code == 200
        except httpx.RequestError:
            return False


# Instance singleton
user_service = UserService()
