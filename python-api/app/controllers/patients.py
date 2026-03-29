"""
Controller Patients
Endpoints pour la gestion des patients
"""
from fastapi import APIRouter, HTTPException, Header, Query
from typing import Optional, Dict, Any
import httpx

from config import settings

router = APIRouter(prefix="/api/v1/patients", tags=["Patients"])


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
async def list_patients(
    authorization: str = Header(...),
    page: int = Query(0),
    size: int = Query(5),
    keyword: Optional[str] = Query(None)
):
    """Liste les patients avec pagination"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            params = {"page": page, "size": size}
            if keyword:
                params["keyword"] = keyword
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/patients",
                params=params,
                headers={"Authorization": authorization}
            )
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la récupération")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.post("/save")
async def save_patient(data: Dict[str, Any], authorization: str = Header(...)):
    """Crée un nouveau patient"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(
                f"{settings.spring_boot_url}/api/v1/patients/save",
                json=data,
                headers={"Authorization": authorization}
            )
            if response.status_code == 400:
                raise HTTPException(status_code=400, detail="Données invalides")
            if response.status_code == 409:
                raise HTTPException(status_code=409, detail="Patient déjà existant")
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la création")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.put("/update")
async def update_patient(data: Dict[str, Any], authorization: str = Header(...)):
    """Met à jour un patient existant"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.put(
                f"{settings.spring_boot_url}/api/v1/patients/update",
                json=data,
                headers={"Authorization": authorization}
            )
            if response.status_code == 404:
                raise HTTPException(status_code=404, detail="Patient non trouvé")
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la mise à jour")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.get("/{patient_id}")
async def get_patient(patient_id: int, authorization: str = Header(...)):
    """Récupère un patient par ID"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/patients/{patient_id}",
                headers={"Authorization": authorization}
            )
            if response.status_code == 404:
                raise HTTPException(status_code=404, detail="Patient non trouvé")
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la récupération")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.delete("/{patient_id}")
async def delete_patient(patient_id: int, authorization: str = Header(...)):
    """Supprime un patient"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.delete(
                f"{settings.spring_boot_url}/api/v1/patients/{patient_id}",
                headers={"Authorization": authorization}
            )
            if response.status_code == 404:
                raise HTTPException(status_code=404, detail="Patient non trouvé")
            if response.status_code == 409:
                raise HTTPException(status_code=409, detail="Impossible de supprimer: a des rendez-vous associés")
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la suppression")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.get("/search")
async def search_patients(
    authorization: str = Header(...),
    keyword: str = Query(..., description="Mot-clé de recherche")
):
    """Recherche des patients par mot-clé"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/patients/search",
                params={"keyword": keyword},
                headers={"Authorization": authorization}
            )
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la recherche")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")
