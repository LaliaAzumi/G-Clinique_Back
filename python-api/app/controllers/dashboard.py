from fastapi import APIRouter, Header, HTTPException
import httpx
from config import settings

router = APIRouter(prefix="/api/v1", tags=["Dashboard"])


# @router.get("/dashboard")
# async def get_dashboard(authorization: str = Header(...)):
#     if not authorization.startswith("Bearer "):
#         raise HTTPException(status_code=401, detail="Token manquant")

#     async with httpx.AsyncClient() as client:
#         try:
#             response = await client.get(
#                 f"{settings.spring_boot_url}/api/dashboard",
#                 headers={"Authorization": authorization}
#             )

#             if response.status_code != 200:
#                 raise HTTPException(status_code=500, detail="Erreur Spring Boot")

#             return response.json()

#         except httpx.RequestError:
#             raise HTTPException(status_code=503, detail="Spring Boot indisponible")
@router.get("/dashboard")
async def dashboard(authorization: str = Header(None)):
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Token manquant")

    async with httpx.AsyncClient() as client:
        try:
            res = await client.get(
                f"{settings.spring_boot_url}/api/v1/dashboard",
                headers={"Authorization": authorization}
            )

            # debug important
            print("SPRING STATUS:", res.status_code)
            print("SPRING TEXT:", res.text)

            if res.status_code != 200:
                raise HTTPException(
                    status_code=res.status_code,
                    detail=f"Erreur Spring Boot: {res.text}"
                )

            return res.json()

        except httpx.RequestError as e:
            raise HTTPException(
                status_code=503,
                detail=f"Spring inaccessible: {str(e)}"
            )
