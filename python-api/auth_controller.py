import httpx
from datetime import datetime, timedelta
import jwt
from passlib.context import CryptContext
from fastapi import HTTPException, status
from config import settings
from schemas import LoginRequest, AuthResponse, TokenResponse, UserInfo

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")  # Contexte de hachage des mots de passe

class AuthController:
    def __init__(self):
        self.spring_boot_base_url = settings.spring_boot_url
    
    async def validate_user_with_spring(self, username: str, password: str) -> dict:
        """Appelle Spring Boot pour valider les identifiants de l'utilisateur"""
        async with httpx.AsyncClient() as client:
            try:
                response = await client.post(
                    f"{self.spring_boot_base_url}/api/auth/validate",
                    json={"username": username, "password": password},
                    timeout=10.0
                )
                if response.status_code == 200:
                    return response.json()  # Retourne les données utilisateur si valide
                return None  # Identifiants invalides
            except httpx.RequestError:
                # Erreur de connexion au service Spring Boot
                raise HTTPException(
                    status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                    detail="Service Spring Boot indisponible"
                )
    
    async def get_user_info(self, username: str) -> dict:
        """Récupère les informations de l'utilisateur depuis Spring Boot"""
        async with httpx.AsyncClient() as client:
            try:
                response = await client.get(
                    f"{self.spring_boot_base_url}/api/users/{username}",
                    timeout=10.0
                )
                if response.status_code == 200:
                    return response.json()  # Informations utilisateur
                return None  # Utilisateur non trouvé
            except httpx.RequestError:
                return None  # Erreur de connexion
    
    def create_access_token(self, data: dict, expires_delta: timedelta = None) -> str:
        """Crée un token JWT signé avec les données fournies"""
        to_encode = data.copy()
        if expires_delta:
            expire = datetime.utcnow() + expires_delta  # Date d'expiration personnalisée
        else:
            expire = datetime.utcnow() + timedelta(minutes=settings.access_token_expire_minutes)  # Durée par défaut
        to_encode.update({"exp": expire})  # Ajoute la date d'expiration au payload
        encoded_jwt = jwt.encode(to_encode, settings.secret_key, algorithm=settings.algorithm)  # Signe le token
        return encoded_jwt
    
    async def login(self, login_data: LoginRequest) -> AuthResponse:
        """Authentifie l'utilisateur et retourne un token JWT"""
        # Validation via Spring Boot
        user_data = await self.validate_user_with_spring(login_data.username, login_data.password)
        
        if not user_data or not user_data.get("valid"):
            # Identifiants incorrects
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Nom d'utilisateur ou mot de passe invalide",
                headers={"WWW-Authenticate": "Bearer"},
            )
        
        # Récupération des informations utilisateur
        user_info = await self.get_user_info(login_data.username)
        if not user_info:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Utilisateur non trouvé"
            )
        
        # Création du token JWT
        access_token_expires = timedelta(minutes=settings.access_token_expire_minutes)
        token_data = {
            "sub": str(user_info.get("id")),  # ID utilisateur (subject)
            "username": user_info.get("username"),  # Nom d'utilisateur
            "role": user_info.get("role")  # Rôle de l'utilisateur
        }
        access_token = self.create_access_token(token_data, access_token_expires)
        
        return AuthResponse(
            success=True,
            message="Authentification réussie",
            user=UserInfo(
                id=user_info.get("id"),
                username=user_info.get("username"),
                email=user_info.get("email"),
                role=user_info.get("role")
            ),
            token=TokenResponse(
                access_token=access_token,
                expires_in=settings.access_token_expire_minutes * 60
            )
        )
    
    async def verify_token(self, token: str) -> dict:
        """Vérifie la validité d'un token JWT"""
        try:
            payload = jwt.decode(token, settings.secret_key, algorithms=[settings.algorithm])
            return payload  # Retourne le contenu du token
        except jwt.ExpiredSignatureError:
            # Token expiré
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Token expiré",
                headers={"WWW-Authenticate": "Bearer"},
            )
        except jwt.JWTError:
            # Token invalide
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Token invalide",
                headers={"WWW-Authenticate": "Bearer"},
            )

auth_controller = AuthController()
