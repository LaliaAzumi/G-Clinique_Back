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

@router.put("/{rendez_vous_id}")
async def update_rendez_vous(rendez_vous_id: int, data: Dict[str, Any], authorization: str = Header(...)):
    await verify_token(authorization)

    async with httpx.AsyncClient() as client:
        try:
            response = await client.put(
                f"{settings.spring_boot_url}/api/v1/rendez-vous/{rendez_vous_id}",
                json=data,
                headers={"Authorization": authorization}
            )

            print("STATUS:", response.status_code)
            print("BODY:", response.text)

            if response.status_code != 200:
                raise HTTPException(status_code=response.status_code, detail=response.text)

            # 🔥 sécurisation ici
            try:
                return response.json()
            except:
                return {"message": "updated"}

        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=str(e))

@router.get("")
async def list_rendez_vous(
    authorization: str = Header(...),
    patient_id: Optional[int] = Query(None),
    medecin_id: Optional[int] = Query(None),
    date: Optional[str] = Query(None)
):
    """Liste les rendez-vous avec filtres"""
    await verify_token(authorization)
    
    # Ajout de follow_redirects=True pour gérer le code 302 de Spring Boot
    async with httpx.AsyncClient(follow_redirects=True) as client:
        try:
            params = {}
            if patient_id: params["patientId"] = patient_id
            if medecin_id: params["medecinId"] = medecin_id
            if date: params["date"] = date
            
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/rendez-vous",
                params=params,
                headers={"Authorization": authorization}
            )

            # Vérification du contenu avant de tenter le .json()
            if response.status_code != 200:
                print(f"Erreur Spring Boot ({response.status_code}): {response.text}")
                raise HTTPException(
                    status_code=response.status_code, 
                    detail="Le service Spring Boot n'a pas renvoyé de données valides."
                )

            # Sécurité : on vérifie si le contenu est bien du JSON
            try:
                return response.json()
            except Exception:
                print(f"Contenu non-JSON reçu : {response.text}")
                raise HTTPException(status_code=500, detail="Réponse du serveur invalide (pas de JSON)")

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
        
#partie secretaire annuler rdv non payer
@router.put("/{medicament_id}/annuler")
async def annuler_medicament(medicament_id: int, authorization: str = Header(...)):

    token_data = await verify_token(authorization)

    if token_data.get("role") != "SECRETAIRE":
        raise HTTPException(403, "Accès réservé au secrétaire")

    async with httpx.AsyncClient() as client:
        response = await client.put(
            f"{settings.spring_boot_url}/api/v1/medicaments/{medicament_id}/annuler",
            headers={"Authorization": authorization}
        )

        if response.status_code != 200:
            raise HTTPException(500, response.text)

        return response.json()