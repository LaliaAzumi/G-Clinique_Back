from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    spring_boot_url: str = "http://localhost:8080"  # URL du service Spring Boot
    secret_key: str = "your-secret-key-change-this"  # Clé secrète pour signer les JWT
    algorithm: str = "HS256"  # Algorithme de chiffrement JWT
    access_token_expire_minutes: int = 30  # Durée de validité du token en minutes
    
    class Config:
        env_file = ".env"  # Fichier de variables d'environnement

settings = Settings()
