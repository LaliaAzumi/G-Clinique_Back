"""
Client HTTP utilitaire pour communiquer avec Spring Boot.
Gère les connexions HTTP et la configuration de base.
"""
import httpx
from config import settings


class SpringBootClient:
    """Client HTTP singleton pour Spring Boot"""
    
    _instance = None
    
    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance.base_url = settings.spring_boot_url
        return cls._instance
    
    async def request(self, method: str, endpoint: str, **kwargs):
        """Fait une requête HTTP à Spring Boot"""
        url = f"{self.base_url}{endpoint}"
        async with httpx.AsyncClient() as client:
            return await client.request(method, url, **kwargs)
    
    async def get(self, endpoint: str, **kwargs):
        """GET request"""
        return await self.request("GET", endpoint, **kwargs)
    
    async def post(self, endpoint: str, **kwargs):
        """POST request"""
        return await self.request("POST", endpoint, **kwargs)
    
    async def put(self, endpoint: str, **kwargs):
        """PUT request"""
        return await self.request("PUT", endpoint, **kwargs)
    
    async def delete(self, endpoint: str, **kwargs):
        """DELETE request"""
        return await self.request("DELETE", endpoint, **kwargs)


def get_spring_boot_client():
    """Retourne une instance du client Spring Boot"""
    return SpringBootClient()
