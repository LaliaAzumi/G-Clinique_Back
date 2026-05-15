"""
Controller Médicaments
Endpoints pour la gestion des médicaments
"""
from fastapi import APIRouter, HTTPException, Header, Query
from typing import Optional, Dict, Any
import httpx

from config import settings

router = APIRouter(prefix="/api/v1/medicaments", tags=["Médicaments"])


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
async def list_medicaments(
    authorization: str = Header(...),
    page: int = Query(0),
    size: int = Query(10),
    keyword: Optional[str] = Query(None)
):
    """Liste les médicaments avec pagination et recherche"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            params = {"page": page, "size": size}
            if keyword:
                params["keyword"] = keyword
            
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/medicaments",
                params=params,
                headers={"Authorization": authorization}
            )
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la récupération")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.post("/save")
async def save_medicament(data: Dict[str, Any], authorization: str = Header(...)):
    """Crée ou met à jour un médicament"""
    # await verify_token(authorization)
    token_data = await verify_token(authorization)

    if token_data.get("role") != "SECRETAIRE":
        raise HTTPException(status_code=403, detail="Accès réservé au secrétaire")
    
    async with httpx.AsyncClient() as client:
        try:
            
            response = await client.post(
                f"{settings.spring_boot_url}/api/v1/medicaments/save",
                json=data,
                headers={"Authorization": authorization}
            )
            if response.status_code == 400:
                raise HTTPException(status_code=400, detail="Données invalides")
            if response.status_code == 409:
                raise HTTPException(status_code=409, detail="Médicament déjà existant")
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la sauvegarde")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.get("/{medicament_id}")
async def get_medicament(medicament_id: int, authorization: str = Header(...)):
    """Récupère un médicament par ID"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/medicaments/{medicament_id}",
                headers={"Authorization": authorization}
            )
            if response.status_code == 404:
                raise HTTPException(status_code=404, detail="Médicament non trouvé")
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la récupération")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


# @router.delete("/{medicament_id}")
# async def delete_medicament(medicament_id: int, 
#                             authorization: str = Header(...)):
#     """Supprime un médicament"""
#     # await verify_token(authorization)
#     token_data = await verify_token(authorization)

#     if token_data.get("role") != "SECRETAIRE":
#         raise HTTPException(status_code=403, detail="Accès réservé au secrétaire")
    
#     async with httpx.AsyncClient() as client:
#         try:
#             response = await client.delete(
#                 f"{settings.spring_boot_url}/api/v1/medicaments/{medicament_id}",
#                 headers={"Authorization": authorization}
#             )
#             if response.status_code == 404:
#                 raise HTTPException(status_code=404, detail="Médicament non trouvé")
#             if response.status_code == 409:
#                 raise HTTPException(status_code=409, detail="Impossible de supprimer: utilisé dans des ordonnances")
#             if response.status_code != 200:
#                 raise HTTPException(status_code=500, detail="Erreur lors de la suppression")
#             return response.json()
#         except httpx.RequestError as e:
#             raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")

@router.delete("/{medicament_id}")
async def delete_medicament(medicament_id: int, authorization: str = Header(...)):

    token_data = await verify_token(authorization)

    if token_data.get("role") != "SECRETAIRE":
        raise HTTPException(status_code=403, detail="Accès réservé au secrétaire")

    async with httpx.AsyncClient() as client:
        try:
            response = await client.delete(
                f"{settings.spring_boot_url}/api/v1/medicaments/{medicament_id}",
                headers={"Authorization": authorization}
            )

            if response.status_code == 404:
                raise HTTPException(status_code=404, detail="Médicament non trouvé")

            if response.status_code == 409:
                raise HTTPException(
                    status_code=409,
                    detail="Impossible de supprimer: utilisé dans des ordonnances"
                )

            if response.status_code not in [200, 204]:
                raise HTTPException(status_code=500, detail="Erreur lors de la suppression")

            # 🔥 IMPORTANT: ne pas parser du JSON vide
            return {"message": "Médicament supprimé avec succès"}

        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.get("/search")
async def search_medicaments(
    authorization: str = Header(...),
    keyword: str = Query(..., description="Mot-clé de recherche")
):
    """Recherche des médicaments par mot-clé"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/medicaments/search",
                params={"keyword": keyword},
                headers={"Authorization": authorization}
            )
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la recherche")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")

#update medocs
@router.put("/{medicament_id}")
async def update_medicament(
    medicament_id: int,
    data: Dict[str, Any],
    authorization: str = Header(...)
):
    """Met à jour un médicament"""

    token_data = await verify_token(authorization)

    if token_data.get("role") != "SECRETAIRE":
        raise HTTPException(status_code=403, detail="Accès réservé au secrétaire")

    async with httpx.AsyncClient() as client:
        try:
            response = await client.put(
                f"{settings.spring_boot_url}/api/v1/medicaments/{medicament_id}",
                json=data,
                headers={"Authorization": authorization}
            )

            if response.status_code == 404:
                raise HTTPException(status_code=404, detail="Médicament non trouvé")

            if response.status_code == 400:
                raise HTTPException(status_code=400, detail="Données invalides")

            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la mise à jour")

            return response.json()

        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")