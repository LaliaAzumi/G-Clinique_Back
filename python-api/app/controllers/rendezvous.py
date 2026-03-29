"""
Controller Rendez-vous
Endpoints pour la gestion des rendez-vous
"""
from fastapi import APIRouter, HTTPException, Header, Query
from typing import Optional, Dict, Any
import httpx

from config import settings

router = APIRouter(prefix="/api/v1/rendez-vous", tags=["Rendez-vous"])


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
async def list_rendez_vous(
    authorization: str = Header(...),
    patient_id: Optional[int] = Query(None),
    medecin_id: Optional[int] = Query(None),
    date: Optional[str] = Query(None)
):
    """Liste les rendez-vous avec filtres"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            params = {}
            if patient_id:
                params["patientId"] = patient_id
            if medecin_id:
                params["medecinId"] = medecin_id
            if date:
                params["date"] = date
            
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/rendez-vous",
                params=params,
                headers={"Authorization": authorization}
            )
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la récupération")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.post("/save")
async def save_rendez_vous(data: Dict[str, Any], authorization: str = Header(...)):
    """Crée ou met à jour un rendez-vous"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(
                f"{settings.spring_boot_url}/api/v1/rendez-vous/save",
                json=data,
                headers={"Authorization": authorization}
            )
            if response.status_code == 400:
                raise HTTPException(status_code=400, detail="Données invalides")
            if response.status_code == 409:
                raise HTTPException(status_code=409, detail="Conflit de créneau")
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la création")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.get("/{rendez_vous_id}")
async def get_rendez_vous(rendez_vous_id: int, authorization: str = Header(...)):
    """Récupère un rendez-vous par ID"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/rendez-vous/{rendez_vous_id}",
                headers={"Authorization": authorization}
            )
            if response.status_code == 404:
                raise HTTPException(status_code=404, detail="Rendez-vous non trouvé")
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la récupération")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.delete("/{rendez_vous_id}")
async def delete_rendez_vous(rendez_vous_id: int, authorization: str = Header(...)):
    """Supprime un rendez-vous"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.delete(
                f"{settings.spring_boot_url}/api/v1/rendez-vous/{rendez_vous_id}",
                headers={"Authorization": authorization}
            )
            if response.status_code == 404:
                raise HTTPException(status_code=404, detail="Rendez-vous non trouvé")
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la suppression")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.post("/{rendez_vous_id}/confirm")
async def confirm_rendez_vous(rendez_vous_id: int, authorization: str = Header(...)):
    """Confirme un rendez-vous"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(
                f"{settings.spring_boot_url}/api/v1/rendez-vous/{rendez_vous_id}/confirm",
                headers={"Authorization": authorization}
            )
            if response.status_code == 404:
                raise HTTPException(status_code=404, detail="Rendez-vous non trouvé")
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la confirmation")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.get("/calendar/events")
async def get_calendar_events(
    authorization: str = Header(...),
    start_date: Optional[str] = Query(None),
    end_date: Optional[str] = Query(None)
):
    """Récupère les événements pour le calendrier"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            params = {}
            if start_date:
                params["startDate"] = start_date
            if end_date:
                params["endDate"] = end_date
            
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/rendez-vous/calendar/events",
                params=params,
                headers={"Authorization": authorization}
            )
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la récupération")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")
