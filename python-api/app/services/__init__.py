# app/services/__init__.py
# Services métier - Logique métier et communication avec Spring Boot

from .users import user_service

__all__ = ["user_service"]
