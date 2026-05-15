"""
Controller Calendrier
Endpoints pour la gestion du calendrier
"""
from fastapi import APIRouter, HTTPException, Header, Query
from typing import Optional, Dict, Any
import httpx

from config import settings

router = APIRouter(prefix="/api/v1/calendar", tags=["Calendrier"])


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


@router.get("/events")
async def get_calendar_events(
    authorization: str = Header(...),
    start_date: Optional[str] = Query(None),
    end_date: Optional[str] = Query(None),
    medecin_id: Optional[int] = Query(None)
):
    """Récupère les événements du calendrier"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            params = {}
            if start_date:
                params["startDate"] = start_date
            if end_date:
                params["endDate"] = end_date
            if medecin_id:
                params["medecinId"] = medecin_id
            
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/calendar/events",
                params=params,
                headers={"Authorization": authorization}
            )
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la récupération")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.post("/event")
async def create_calendar_event(data: Dict[str, Any], authorization: str = Header(...)):
    """Crée un événement dans le calendrier"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(
                f"{settings.spring_boot_url}/api/v1/calendar/event",
                json=data,
                headers={"Authorization": authorization}
            )
            if response.status_code == 400:
                raise HTTPException(status_code=400, detail="Données invalides")
            if response.status_code == 409:
                raise HTTPException(status_code=409, detail="Conflit d'horaire")
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la création")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.get("/disponibilites/{medecin_id}")
async def get_medecin_disponibilites(
    medecin_id: int,
    authorization: str = Header(...),
    date: Optional[str] = Query(None)
):
    """Récupère les disponibilités d'un médecin"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            params = {}
            if date:
                params["date"] = date
            
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/calendar/disponibilites/{medecin_id}",
                params=params,
                headers={"Authorization": authorization}
            )
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la récupération")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.get("/stats")
async def get_calendar_stats(
    authorization: str = Header(...),
    start_date: Optional[str] = Query(None),
    end_date: Optional[str] = Query(None)
):
    """Récupère les statistiques du calendrier"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            params = {}
            if start_date:
                params["startDate"] = start_date
            if end_date:
                params["endDate"] = end_date
            
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/calendar/stats",
                params=params,
                headers={"Authorization": authorization}
            )
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la récupération")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")

            
# get events
# @router.get("/eventsN")
# @router.get("/eventsN/{user_id}")
# async def get_calendar_eventsN(
#     authorization: str = Header(...),
#     start_date: Optional[str] = Query(None) # Ex: 2026-04-06
# ):
#     # 1. Vérifie le token via ton verify_token
#     # user_info doit contenir l'ID de l'utilisateur (ex: user_info["id"])
#     user_info = await verify_token(authorization)
#     user_id = user_info.get("id") 

#     if not user_id:
#         raise HTTPException(status_code=401, detail="ID utilisateur manquant dans le token")
    
#     # 2. Appelle l'API Java avec le nouveau chemin incluant l'ID
#     async with httpx.AsyncClient() as client:
#         # Note le changement d'URL ici : on ajoute /eventsN/{user_id}
#         spring_url = f"{settings.spring_boot_url}/api/v1/calendar/eventsN/{user_id}"
        
#         response = await client.get(
#             spring_url,
#             params={"startOfWeek": start_date},
#             headers={"Authorization": authorization}
#         )
        
#         if response.status_code != 200:
#             # Optionnel : log l'erreur pour débugger plus facilement
#             return response.json()
            
#         return response.json()

@router.get("/eventsN/{user_id}")
async def get_calendar_eventsN(
    user_id: int,  # On récupère l'ID directement depuis l'URL envoyée par le Front
    authorization: str = Header(...),
    start_date: Optional[str] = Query(None, alias="startOfWeek") # On s'aligne sur le nom attendu par Java
):
    # 1. On vérifie juste si le token est valide
    # Si verify_token échoue, il lèvera lui-même une 401
    user_info = await verify_token(authorization)
    
    # DEBUG : Pour être sûr de ce qu'on reçoit de l'auth
    print(f"DEBUG: User Info from Token: {user_info}")

    # 2. On appelle l'API Java
    async with httpx.AsyncClient() as client:
        # On utilise le user_id qui vient de l'URL (le 27 que ton front a envoyé)
        spring_url = f"{settings.spring_boot_url}/api/v1/calendar/eventsN/{user_id}"
        
        try:
            response = await client.get(
                spring_url,
                params={"startOfWeek": start_date},
                headers={"Authorization": authorization} # On transmet le token à Java
            )
            
            # Si Java renvoie une erreur (401, 403, 500), on la transmet au Front pour voir le message
            if response.status_code != 200:
                print(f"Erreur Spring Boot ({response.status_code}): {response.text}")
                raise HTTPException(status_code=response.status_code, detail=f"Erreur Backend Java: {response.text}")
                
            return response.json()

        except httpx.RequestError as exc:
            raise HTTPException(status_code=503, detail=f"Impossible de joindre le service Java: {str(exc)}")