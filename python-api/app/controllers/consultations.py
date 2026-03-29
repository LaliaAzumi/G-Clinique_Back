"""
Controller Consultations
Endpoints pour la gestion des consultations médicales
"""
from fastapi import APIRouter, HTTPException, Header, Query
from typing import Optional, Dict, Any
import httpx

from config import settings

router = APIRouter(prefix="/api/v1/consultations", tags=["Consultations"])


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
async def list_consultations(
    authorization: str = Header(...),
    patient_id: Optional[int] = Query(None),
    medecin_id: Optional[int] = Query(None)
):
    """Liste les consultations avec filtres"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            params = {}
            if patient_id:
                params["patientId"] = patient_id
            if medecin_id:
                params["medecinId"] = medecin_id
            
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/consultations",
                params=params,
                headers={"Authorization": authorization}
            )
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la récupération")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.get("/patient/{patient_id}")
async def get_consultations_by_patient(patient_id: int, authorization: str = Header(...)):
    """Récupère les consultations d'un patient"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/consultations/patient/{patient_id}",
                headers={"Authorization": authorization}
            )
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la récupération")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.get("/medecin/{medecin_id}")
async def get_consultations_by_medecin(medecin_id: int, authorization: str = Header(...)):
    """Récupère les consultations d'un médecin"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/consultations/medecin/{medecin_id}",
                headers={"Authorization": authorization}
            )
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la récupération")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.post("/save")
async def save_consultation(data: Dict[str, Any], authorization: str = Header(...)):
    """Crée une nouvelle consultation"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(
                f"{settings.spring_boot_url}/api/v1/consultations/save",
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


@router.put("/update")
async def update_consultation(data: Dict[str, Any], authorization: str = Header(...)):
    """Met à jour une consultation existante"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.put(
                f"{settings.spring_boot_url}/api/v1/consultations/update",
                json=data,
                headers={"Authorization": authorization}
            )
            if response.status_code == 404:
                raise HTTPException(status_code=404, detail="Consultation non trouvée")
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la mise à jour")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.get("/{consultation_id}")
async def get_consultation(consultation_id: int, authorization: str = Header(...)):
    """Récupère une consultation par ID"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/consultations/{consultation_id}",
                headers={"Authorization": authorization}
            )
            if response.status_code == 404:
                raise HTTPException(status_code=404, detail="Consultation non trouvée")
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la récupération")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.delete("/{consultation_id}")
async def delete_consultation(consultation_id: int, authorization: str = Header(...)):
    """Supprime une consultation"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.delete(
                f"{settings.spring_boot_url}/api/v1/consultations/{consultation_id}",
                headers={"Authorization": authorization}
            )
            if response.status_code == 404:
                raise HTTPException(status_code=404, detail="Consultation non trouvée")
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la suppression")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")
