"""
Controller Médecins
Endpoints pour la gestion des médecins (délégation à Spring Boot)
"""
from fastapi import APIRouter, HTTPException, Header
from typing import Dict, Any
import httpx

from config import settings

router = APIRouter(prefix="/api/v1/medecins", tags=["Médecins"])


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


@router.post("/create-with-user")
async def create_medecin_with_user(
    data: Dict[str, Any],
    authorization: str = Header(...)
):
    """Crée un médecin avec son compte utilisateur (Admin uniquement)"""
    token_data = await verify_token(authorization)
    if token_data.get("role") != "ADMIN":
        raise HTTPException(status_code=403, detail="Accès réservé aux administrateurs")
    
    # Reformate pour Spring Boot
    spring_data = {
        "medecin": {
            "nom": data.get("nom"),
            "prenom": data.get("prenom"),
            "specialite": data.get("specialite"),
            "telephone": data.get("telephone"),
            "adresse": data.get("adresse")
        },
        "username": data.get("username"),
        "email": data.get("email")
    }
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(
                f"{settings.spring_boot_url}/api/v1/medecins/create-with-user",
                json=spring_data,
                headers={"Authorization": authorization}
            )
            if response.status_code == 400:
                raise HTTPException(status_code=400, detail="Données manquantes")
            if response.status_code == 500:
                raise HTTPException(status_code=500, detail="Erreur lors de la création")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.get("")
async def list_medecins(authorization: str = Header(...)):
    """Liste tous les médecins"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/medecins",
                headers={"Authorization": authorization}
            )
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la récupération")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.post("/{medecin_id}/create-user")
async def create_user_for_medecin(
    medecin_id: int,
    data: Dict[str, Any],
    authorization: str = Header(...)
):
    """Crée un compte utilisateur pour un médecin existant"""
    token_data = await verify_token(authorization)
    if token_data.get("role") != "ADMIN":
        raise HTTPException(status_code=403, detail="Accès réservé aux administrateurs")
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(
                f"{settings.spring_boot_url}/api/v1/medecins/{medecin_id}/create-user",
                json=data,
                headers={"Authorization": authorization}
            )
            if response.status_code == 404:
                raise HTTPException(status_code=404, detail="Médecin non trouvé")
            if response.status_code == 500:
                raise HTTPException(status_code=500, detail="Erreur lors de la création du compte")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.get("/{medecin_id}")
async def get_medecin(medecin_id: int, authorization: str = Header(...)):
    """Récupère un médecin par ID"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/medecins/{medecin_id}",
                headers={"Authorization": authorization}
            )
            if response.status_code == 404:
                raise HTTPException(status_code=404, detail="Médecin non trouvé")
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la récupération")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")
            
            
@router.get("/{medecin_id}/portefeuille")
async def get_portefeuille_medecin(medecin_id: int, authorization: str = Header(...)):
    """
    Récupère les gains du médecin : 50% du total des prestations payées.
    """
    # 1. On vérifie l'identité du médecin (Token)
    await verify_token(authorization)
    # Dans ton fichier Python
    url = f"{settings.spring_boot_url}/api/v1/medecins/{medecin_id}/portefeuille"
    print(f"J'appelle Java ici : {url}") # Ajoute ce print pour débugger
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(url, headers={"Authorization": authorization})
            
            if response.status_code != 200:
                print(f"Erreur Java: {response.status_code}")
                raise HTTPException(status_code=500, detail="Erreur Spring Boot")
            
            # 1. On récupère l'objet ApiResponse complet
            resultat_api = response.json() 
            
            # 2. On extrait le dictionnaire "data" qui contient nos chiffres
            # C'est ici que se trouvent totalEncaisse et maPart
            stats = resultat_api.get("data", {}) 
            
            # Sécurité : on s'assure d'avoir des nombres (0.0 si absent)
            total = stats.get("totalEncaisse", 0.0)
            ma_part = stats.get("maPart", 0.0)
            clinique = stats.get("commissionClinique", 0.0)

            return {
                "medecin_id": medecin_id,
                "chiffre_affaire_total": total,
                "gain_net_medecin": ma_part,
                "commission_clinique": clinique,
                "devise": stats.get("devise", "Ar"),
                "message": "Calcul basé sur 50% des actes payés"
            }
            
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot hors ligne : {str(e)}")
