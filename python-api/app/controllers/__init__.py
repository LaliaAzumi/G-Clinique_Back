# app/controllers/__init__.py
# Controllers FastAPI - Tous les endpoints API

from .auth import auth_controller
#from .auth import router as auth_router
from .medecins import router as medecins_router
from .secretaires import router as secretaires_router
from .patients import router as patients_router
from .consultations import router as consultations_router
from .rendezvous import router as rendezvous_router
from .ordonnances import router as ordonnances_router
from .medicaments import router as medicaments_router
from .notifications import router as notifications_router
from .calendar import router as calendar_router
from .chambres import router as chambres_router
from .acteMedical import router as acteMedical_routeur

__all__ = [
    "auth_controller",
    "auth_router",
    "medecins_router",
    "secretaires_router",
    "patients_router",
    "consultations_router",
    "rendezvous_router",
    "ordonnances_router",
    "medicaments_router",
    "notifications_router",
    "calendar_router",
    "chambres_router",
    "acteMedical_routeur"
]
