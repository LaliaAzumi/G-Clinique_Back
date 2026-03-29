# app/utils/__init__.py
# Utilitaires communs - Clients HTTP, helpers, etc.

from .http_client import get_spring_boot_client

__all__ = ["get_spring_boot_client"]
