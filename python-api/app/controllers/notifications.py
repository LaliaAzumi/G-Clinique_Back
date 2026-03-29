"""
Controller Notifications
Endpoints pour la gestion des notifications
"""
from fastapi import APIRouter, HTTPException, Header
from typing import Dict, Any
import httpx

from config import settings

router = APIRouter(prefix="/api/v1/notifications", tags=["Notifications"])


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
async def list_notifications(authorization: str = Header(...)):
    """Liste les notifications de l'utilisateur connecté"""
    token_data = await verify_token(authorization)
    user_id = token_data.get("user_id")
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/notifications",
                params={"userId": user_id},
                headers={"Authorization": authorization}
            )
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la récupération")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.get("/unread")
async def get_unread_notifications(authorization: str = Header(...)):
    """Liste les notifications non lues"""
    token_data = await verify_token(authorization)
    user_id = token_data.get("user_id")
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/notifications/unread",
                params={"userId": user_id},
                headers={"Authorization": authorization}
            )
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la récupération")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.get("/count")
async def count_unread_notifications(authorization: str = Header(...)):
    """Compte les notifications non lues"""
    token_data = await verify_token(authorization)
    user_id = token_data.get("user_id")
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{settings.spring_boot_url}/api/v1/notifications/count",
                params={"userId": user_id},
                headers={"Authorization": authorization}
            )
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors du comptage")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.post("/{notification_id}/mark-read")
async def mark_notification_read(notification_id: int, authorization: str = Header(...)):
    """Marque une notification comme lue"""
    await verify_token(authorization)
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(
                f"{settings.spring_boot_url}/api/v1/notifications/{notification_id}/mark-read",
                headers={"Authorization": authorization}
            )
            if response.status_code == 404:
                raise HTTPException(status_code=404, detail="Notification non trouvée")
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la mise à jour")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")


@router.post("/mark-all-read")
async def mark_all_notifications_read(authorization: str = Header(...)):
    """Marque toutes les notifications comme lues"""
    token_data = await verify_token(authorization)
    user_id = token_data.get("user_id")
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(
                f"{settings.spring_boot_url}/api/v1/notifications/mark-all-read",
                params={"userId": user_id},
                headers={"Authorization": authorization}
            )
            if response.status_code != 200:
                raise HTTPException(status_code=500, detail="Erreur lors de la mise à jour")
            return response.json()
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Spring Boot indisponible: {str(e)}")
