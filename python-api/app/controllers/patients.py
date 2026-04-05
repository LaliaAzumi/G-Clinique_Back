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


# patients.py
@router.get("")
async def list_patients(authorization: str = Header(...), page: int = Query(0), size: int = Query(10)):
    print(f"DEBUG AUTH HEADER: {authorization}") 
    
    await verify_token(authorization)
    
    async with httpx.AsyncClient(follow_redirects=False) as client: # Empêche de suivre la 302
        response = await client.get(
            f"{settings.spring_boot_url}/api/v1/patients",
            params={"page": page, "size": size},
            headers={"Authorization": authorization}
        )
        
        if response.status_code == 302:
            raise HTTPException(status_code=401, detail="Spring Security a redirigé la requête (Vérifiez le Token)")
            
        return response.json()


@router.post("/save")
async def save_patient(data: Dict[str, Any], authorization: str = Header(...)):
    """Crée un nouveau patient"""
    print(f"DONNEES RECUES : {data}")
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        # Dans patients.py, remplacez la partie save_patient par :
        try:
            response = await client.post(
                f"{settings.spring_boot_url}/api/v1/patients/save",
                json=data,
                headers={"Authorization": authorization}
            )
            if response.status_code != 200:
                # On affiche TOUT ce que Spring renvoie dans la console FastAPI
                print(f"DEBUG SPRING STATUS: {response.status_code}")
                print(f"DEBUG SPRING BODY: {response.text}") 
                raise HTTPException(status_code=500, detail=f"Spring Error: {response.text[:100]}")
            return response.json()
        except Exception as e:
            print(f"EXCEPTION CAPTURÉE: {str(e)}")
            raise HTTPException(status_code=500, detail=str(e))

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


# .py
@router.delete("/{patient_id}")
async def delete_patient(patient_id: int, authorization: str = Header(...)):
    await verify_token(authorization)
    async with httpx.AsyncClient() as client:
        try:
            response = await client.delete(
                f"{settings.spring_boot_url}/api/v1/patients/{patient_id}",
                headers={"Authorization": authorization}
            )
            if response.status_code != 200:
                print(f"ERREUR SPRING DELETE: {response.text}") 
                raise HTTPException(
                    status_code=response.status_code, 
                    detail=f"Backend Error: {response.text}"
                )
            return {"message": "Patient supprimé"}
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Service indisponible: {str(e)}")


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
