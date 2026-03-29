"""
Controller Ordonnances
Endpoints pour la gestion des ordonnances
"""
from fastapi import APIRouter, HTTPException, Header, Query
from typing import Optional, Dict, Any
import httpx

from config import settings

router = APIRouter(prefix="/api/v1/ordonnances", tags=["Ordonnances"])


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


@router.get("")
async def list_ordonnances(
    authorization: str = Header(...),
    patient_id: Optional[int] = Query(None),
    medecin_id: Optional[int] = Query(None)
):
    """Liste les ordonnances avec filtres"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            params = {}
            if patient_id:
                params["patientId"] = patient_id
            if medecin_id:
                params["medecinId"] = medecin_id
            
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/ordonnances",
                params=params,
                headers={"Authorization": authorization}
            )
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la récupération")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.post("/save")
async def save_ordonnance(data: Dict[str, Any], authorization: str = Header(...)):
    """Crée une nouvelle ordonnance"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(
                f"{settings.spring_boot_url}/api/v1/ordonnances/save",
                json=data,
                headers={"Authorization": authorization}
            )
            if response.status_code == 400:
                raise HTTPException(status_code=400, detail="Données invalides")
            if response.status_code == 404:
                raise HTTPException(status_code=404, detail="Patient ou médecin non trouvé")
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la création")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.get("/{ordonnance_id}")
async def get_ordonnance(ordonnance_id: int, authorization: str = Header(...)):
    """Récupère une ordonnance par ID"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/ordonnances/{ordonnance_id}",
                headers={"Authorization": authorization}
            )
            if response.status_code == 404:
                raise HTTPException(status_code=404, detail="Ordonnance non trouvée")
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la récupération")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.delete("/{ordonnance_id}")
async def delete_ordonnance(ordonnance_id: int, authorization: str = Header(...)):
    """Supprime une ordonnance"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.delete(
                f"{settings.spring_boot_url}/api/v1/ordonnances/{ordonnance_id}",
                headers={"Authorization": authorization}
            )
            if response.status_code == 404:
                raise HTTPException(status_code=404, detail="Ordonnance non trouvée")
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la suppression")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.get("/{ordonnance_id}/pdf")
async def generate_pdf(ordonnance_id: int, authorization: str = Header(...)):
    """Génère le PDF d'une ordonnance"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/ordonnances/{ordonnance_id}/pdf",
                headers={"Authorization": authorization}
            )
            if response.status_code == 404:
                raise HTTPException(status_code=404, detail="Ordonnance non trouvée")
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la génération PDF")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.post("/{ordonnance_id}/send-email")
async def send_ordonnance_email(ordonnance_id: int, authorization: str = Header(...)):
    """Envoie l'ordonnance par email au patient"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(
                f"{settings.spring_boot_url}/api/v1/ordonnances/{ordonnance_id}/send-email",
                headers={"Authorization": authorization}
            )
            if response.status_code == 404:
                raise HTTPException(status_code=404, detail="Ordonnance non trouvée")
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de l'envoi de l'email")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.get("/patient/{patient_id}")
async def get_ordonnances_by_patient(patient_id: int, authorization: str = Header(...)):
    """Récupère les ordonnances d'un patient"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/ordonnances/patient/{patient_id}",
                headers={"Authorization": authorization}
            )
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la récupération")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")
