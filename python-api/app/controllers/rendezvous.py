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

@router.post("/save")
async def save_rendez_vous(data: Dict[str, Any], authorization: str = Header(...)):
    print(f"DEBUG FastAPI - Données reçues : {data}")
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

@router.get("/list") # <--- Changé de "" à "/list" pour correspondre au front
async def list_rendez_vous(
    authorization: str = Header(...),
    patient_id: Optional[int] = Query(None),
    medecin_id: Optional[int] = Query(None),
    date: Optional[str] = Query(None) # Query(None) rend le paramètre optionnel
):
    """Liste les rendez-vous avec filtres"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient(follow_redirects=True) as client:
        try:
            # Synchronisation des noms de paramètres avec Spring Boot
            params = {}
            if patient_id: params["patientId"] = patient_id
            if medecin_id: params["medecinId"] = medecin_id
            if date: params["date"] = date
            
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/rendez-vous", # URL Spring Boot
                params=params,
                headers={"Authorization": authorization}
            )

            if response.status_code != 200:
                raise HTTPException(status_code=response.status_code, detail=response.text)

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


# Dans rendezvous.py

# Dans rendezvous.py

@router.delete("/{rendez_vous_id}")
async def delete_rendez_vous(rendez_vous_id: int, authorization: str = Header(...)):
    await verify_token(authorization) # Vérifie que l'utilisateur est connecté
    
    async with httpx.AsyncClient() as client:
        response = await client.delete(
            f"{settings.spring_boot_url}/api/v1/rendez-vous/{rendez_vous_id}",
            headers={"Authorization": authorization}
        )
        
        # Si Spring renvoie 302, c'est un problème de sécurité/auth côté Java
        if response.status_code == 302:
            raise HTTPException(status_code=403, detail="Redirection détectée : vérifiez la sécurité Spring Boot")
            
        if response.status_code != 200:
            raise HTTPException(status_code=response.status_code, detail="Erreur lors de la suppression")
            
        return {"success": True}


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

# rendezvous.py

@router.put("/{rdv_id}")
async def update_rendez_vous(rdv_id: int, data: Dict[str, Any], authorization: str = Header(...)):
    await verify_token(authorization)
    async with httpx.AsyncClient() as client:
        try:
            response = await client.put(
                f"{settings.spring_boot_url}/api/v1/rendez-vous/{rdv_id}",
                json=data,
                headers={"Authorization": authorization}
            )
            if response.status_code != 200:
                raise HTTPException(status_code=response.status_code, detail=response.text)
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail="Backend inaccessible")
        
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
            
            
@router.post("/save-public")
async def save_public_rendez_vous(data: Dict[str, Any]):
    """
    Crée un rendez-vous complet (Patient + RDV + Paiement + Prestations).
    ACCÈS PUBLIC : Pas de vérification de token ici.
    """
    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(
                f"{settings.spring_boot_url}/api/v1/rendez-vous/save-public",
                json=data,
                timeout=10.0 
            )
            
            if response.status_code == 400:
                error_detail = response.json().get("error", "Données invalides ou créneau indisponible")
                raise HTTPException(status_code=400, detail=error_detail)
            
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur interne du serveur de santé")
            
            return response.json()
            
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Connexion au service Spring Boot impossible : {str(e)}")

"""
partie secretaire valider paiement d'un rdv
"""
@router.patch("/{rendez_vous_id}/valider-paiement")
async def valider_paiement_rdv(rendez_vous_id: int, authorization: str = Header(...)):
    """
    Action Secrétaire : Valide le paiement d'un rendez-vous.
    Appelle la méthode métier rdv.validerPaiement() côté Spring Boot.
    """
    # On vérifie que la secrétaire est bien connectée
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.patch(
                f"{settings.spring_boot_url}/api/v1/rendez-vous/{rendez_vous_id}/valider-paiement",
                headers={"Authorization": authorization}
            )
            
            if response.status_code == 404:
                raise HTTPException(status_code=404, detail="Rendez-vous introuvable")
                
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la validation du paiement")
                
            return response.json()
            
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Service Spring Boot indisponible: {str(e)}")