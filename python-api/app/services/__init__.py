# app/services/__init__.py
# Services métier - Logique métier et communication avec Spring Boot

from .users import UserService

user_service = UserService()

__all__ = ["UserService"]
