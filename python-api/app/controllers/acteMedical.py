"""
Controller Actes Médicaux
Gestion des services et tarifs (Délégation à Spring Boot)
"""
from fastapi import APIRouter, HTTPException
from typing import Dict, Any, List
import httpx

# Assure-toi que settings.spring_boot_url est bien défini dans ton config.py
from config import settings

router = APIRouter(prefix="/api/v1/actes", tags=["Actes Médicaux"])

@router.get("")
async def list_actes():
    """Récupère la liste de tous les actes (Consultation, Écho, etc.) depuis Spring Boot"""
    async with httpx.AsyncClient() as client:
        try:
            # Appel vers ton ActeMedicalApiController.java
            response = await client.get(f"{settings.spring_boot_url}/api/v1/actes")
            
            if response.status_code != 200:
                raise HTTPException(status_code=response.status_code, detail="Erreur lors de la récupération des actes")
                
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail="Service Spring Boot indisponible")

@router.get("/{acte_id}")
async def get_one_acte(acte_id: int):
    """Récupère les détails d'un acte spécifique par son ID"""
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(f"{settings.spring_boot_url}/api/v1/actes/{acte_id}")
            
            if response.status_code == 404:
                raise HTTPException(status_code=404, detail="Acte médical introuvable")
                
            return response.json()
        except httpx.RequestError:
            raise HTTPException(status_code=503, detail="Erreur de communication avec le backend")

@router.post("/create")
async def create_acte(data: Dict[str, Any]):
    """Ajoute un nouvel acte médical (Échographie, etc.)"""
    # Ici, on délègue la création à Spring Boot
    spring_data = {
        "nom": data.get("nom"),
        "prix": data.get("prix", 0.0)
    }
    
    async with httpx.AsyncClient(timeout=10.0) as client:
        try:
            response = await client.post(
                f"{settings.spring_boot_url}/api/v1/actes/create", # Assure-toi d'avoir ce endpoint en Java
                json=spring_data
            )
            return response.json()
        except httpx.RequestError:
            raise HTTPException(status_code=503, detail="Impossible de joindre le service de création")