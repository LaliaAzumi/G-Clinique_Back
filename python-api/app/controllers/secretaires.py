"""
Controller Secrétaires
Endpoints pour la gestion des secrétaires
"""
from fastapi import APIRouter, HTTPException, Header
from typing import Dict, Any
import httpx

from config import settings

router = APIRouter(prefix="/api/v1/secretaires", tags=["Secrétaires"])


async def verify_token(auth_header: str) -> dict:
    """Vérifie le token JWT avec Spring Boot"""
    if not auth_header or not auth_header.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Token manquant")
    
    token = auth_header[7:]
    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(
                f"{settings.spring_boot_url}/api/v1/auth/verify",
                headers={"Authorization": f"Bearer {token}"}
            )
            if response.status_code != 200:
                raise HTTPException(status_code=401, detail="Token invalide")
            data = response.json()
            if not data.get("success"):
                raise HTTPException(status_code=401, detail="Token invalide")
            return data.get("data", {})
        except httpx.RequestError:
            raise HTTPException(status_code=503, detail="Service Spring Boot indisponible")


@router.post("/create")
async def create_secretaire(data: Dict[str, Any], authorization: str = Header(...)):
    """Crée un compte secrétaire (Admin uniquement)"""
    token_data = await verify_token(authorization)
    if token_data.get("role") != "ADMIN":
        raise HTTPException(status_code=403, detail="Accès réservé aux administrateurs")
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(
                f"{settings.spring_boot_url}/api/v1/secretaires/create",
                json=data,
                headers={"Authorization": authorization}
            )
            if response.status_code == 400:
                raise HTTPException(status_code=400, detail="Données manquantes")
            if response.status_code == 500:
                raise HTTPException(status_code=500, detail="Erreur lors de la création")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.get("")
async def list_secretaires(authorization: str = Header(...)):
    """Liste tous les secrétaires"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/secretaires",
                headers={"Authorization": authorization}
            )
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la récupération")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")
