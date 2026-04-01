"""
Controller Chambres
Gestion des lits et des chambres (délégation à Spring Boot)
"""
from fastapi import APIRouter, HTTPException, Header
from typing import Dict, Any
import httpx

from config import settings

router = APIRouter(prefix="/api/v1/chambres", tags=["Chambres"])

async def verify_token(auth_header: str) -> dict:
    """Vérifie le token JWT avec Spring Boot (Copie conforme de la logique de ton collègue)"""
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
async def create_chambre(data: Dict[str, Any], authorization: str = Header(...)):
    token_data = await verify_token(authorization)
    
    if token_data.get("role") != "ADMIN":
         raise HTTPException(status_code=403, detail="Accès réservé aux administrateurs")

    # On utilise .get(key, default) pour éviter les erreurs si une clé manque
    spring_data = {
        "numero": data.get("numero"),
        "etat": data.get("etat", True),
        "prixJ": data.get("prixJ", 0.0),
        "etage": data.get("etage"),
        "isSoinsIntensifs": data.get("isSoinsIntensifs", False)
    }
    
    async with httpx.AsyncClient(timeout=10.0) as client:
        try:
            response = await client.post(
                f"{settings.spring_boot_url}/api/v1/chambres/create",
                json=spring_data,
                headers={"Authorization": authorization} # On propage le token
            )
            
            # Si Spring renvoie une erreur (400, 404, 500), on la propage proprement
            if response.status_code != 200:
                return response.json() # Renvoie l'ApiResponse d'erreur de Spring
                
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Erreur de communication avec le backend")

@router.get("/{chambre_id}")
async def get_one_chambre(chambre_id: int, authorization: str = Header(...)):
    """Récupère les détails d'une chambre spécifique"""
    # 1. Vérification du token (sécurité)
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            # 2. Appel à Spring Boot
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/chambres/{chambre_id}",
                headers={"Authorization": authorization}
            )
            
            if response.status_code == 404:
                raise HTTPException(status_code=404, detail="Chambre introuvable")
                
            return response.json()
        except httpx.RequestError:
            raise HTTPException(status_code=503, detail="Service Spring Boot indisponible")

@router.get("")
async def list_chambres(authorization: str = Header(...)):
    """Liste toutes les chambres de la clinique"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/chambres",
                headers={"Authorization": authorization}
            )
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")
 
 
@router.put("/{chambre_id}")
async def update_chambre(
    chambre_id: int, 
    data: Dict[str, Any], 
    authorization: str = Header(...)
):
    """Met à jour une chambre existante"""
    await verify_token(authorization) # Vérifie le token via Spring
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.put(
                f"{settings.spring_boot_url}/api/v1/chambres/{chambre_id}",
                json=data,
                headers={"Authorization": authorization}
            )
            return response.json()
        except httpx.RequestError:
            raise HTTPException(status_code=503, detail="Erreur de liaison avec Spring Boot")
            
                      
@router.delete("/{chambre_id}")
async def delete_chambre(
    chambre_id: int, 
    authorization: str = Header(...)
):
    """Supprime une chambre (Réservé à l'ADMIN)"""
    # 1. Vérification du token et du rôle
    token_data = await verify_token(authorization)
    if token_data.get("role") != "ADMIN":
        raise HTTPException(status_code=403, detail="Suppression réservée aux administrateurs")

    async with httpx.AsyncClient() as client:
        try:
            # 2. Appel du DELETE vers Spring Boot
            response = await client.delete(
                f"{settings.spring_boot_url}/api/v1/chambres/{chambre_id}",
                headers={"Authorization": authorization}
            )
            
            # Si Spring renvoie une erreur (ex: chambre liée à un patient)
            if response.status_code != 200:
                # On tente de renvoyer le JSON d'erreur de Spring
                try:
                    return response.json()
                except:
                    raise HTTPException(status_code=response.status_code, detail="Erreur lors de la suppression")
            
            return response.json()
            
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail="Serveur de base de données injoignable")