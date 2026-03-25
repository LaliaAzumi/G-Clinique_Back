from pydantic import BaseModel
from typing import Optional

class LoginRequest(BaseModel):
    """Requête de connexion utilisateur"""
    username: str  # Nom d'utilisateur
    password: str  # Mot de passe

class TokenResponse(BaseModel):
    """Réponse contenant le token d'accès JWT"""
    access_token: str  # Token JWT signé
    token_type: str = "bearer"  # Type de token
    expires_in: int  # Durée de validité en secondes

class UserInfo(BaseModel):
    """Informations de l'utilisateur authentifié"""
    id: int  # Identifiant unique
    username: str  # Nom d'utilisateur
    email: str  # Adresse email
    role: str  # Rôle (ADMIN, DOCTOR, SECRETARY, PATIENT)

class AuthResponse(BaseModel):
    """Réponse complète de l'authentification"""
    success: bool  # Indique si l'authentification a réussi
    message: Optional[str] = None  # Message descriptif
    user: Optional[UserInfo] = None  # Informations utilisateur (si succès)
    token: Optional[TokenResponse] = None  # Token JWT (si succès)

class UserCreateRequest(BaseModel):
    """Requête de création d'un nouvel utilisateur"""
    username: str  # Nom d'utilisateur unique
    email: str  # Adresse email unique
    password: str  # Mot de passe (sera haché)
    role: str  # Rôle: ADMIN, DOCTOR, SECRETARY, PATIENT

class UserResponse(BaseModel):
    """Réponse après création d'un utilisateur"""
    id: int  # ID de l'utilisateur créé
    username: str  # Nom d'utilisateur
    email: str  # Email
    role: str  # Rôle attribué
    first_login: bool  # Indique si premier login
    message: str  # Message de confirmation
