"""
Script de diagnostic pour tester la connexion FastAPI <-> Spring Boot
"""
import httpx
import asyncio

SPRING_BOOT_URL = "http://localhost:8080"
FASTAPI_URL = "http://localhost:8000"

# Token de test (à remplacer par un vrai token)
TEST_TOKEN = None

async def get_token():
    """Récupère un token valide"""
    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(
                f"{FASTAPI_URL}/api/auth/login",
                json={"username": "admin", "password": "admin123"}
            )
            data = response.json()
            if data.get("success"):
                return data["token"]["access_token"]
        except Exception as e:
            print(f"❌ Erreur login: {e}")
    return None

async def test_fastapi_verify(token):
    """Teste la vérification du token côté FastAPI"""
    print("\n=== Test 1: FastAPI /api/auth/verify ===")
    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(
                f"{FASTAPI_URL}/api/auth/verify",
                headers={"Authorization": f"Bearer {token}"}
            )
            print(f"Status: {response.status_code}")
            print(f"Response: {response.json()}")
            return response.status_code == 200
        except Exception as e:
            print(f"❌ Erreur: {e}")
            return False

async def test_spring_boot_list_medecins(token):
    """Teste la liste des médecins côté Spring Boot"""
    print("\n=== Test 2: Spring Boot /api/v1/medecins (GET) ===")
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{SPRING_BOOT_URL}/api/v1/medecins",
                headers={"Authorization": f"Bearer {token}"}
            )
            print(f"Status: {response.status_code}")
            if response.status_code == 200:
                print(f"✅ Succès!")
                return True
            else:
                print(f"❌ Erreur: {response.text[:200]}")
                return False
        except Exception as e:
            print(f"❌ Erreur connexion: {e}")
            return False

async def test_spring_boot_create_medecin(token):
    """Teste la création de médecin côté Spring Boot"""
    print("\n=== Test 3: Spring Boot /api/v1/medecins/create-with-user (POST) ===")
    
    # Format attendu par Spring Boot
    data = {
        "medecin": {
            "nom": "Test",
            "specialite": "Cardiologie",
            "telephone": "0123456789",
            "adresse": "Paris"
        },
        "username": "testmed123",
        "email": "testmed123@email.com"
    }
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(
                f"{SPRING_BOOT_URL}/api/v1/medecins/create-with-user",
                json=data,
                headers={"Authorization": f"Bearer {token}"}
            )
            print(f"Status: {response.status_code}")
            print(f"Response: {response.text[:500]}")
            return response.status_code == 200
        except Exception as e:
            print(f"❌ Erreur connexion: {e}")
            return False

async def test_fastapi_create_medecin(token):
    """Teste la création de médecin via FastAPI"""
    print("\n=== Test 4: FastAPI /api/v1/medecins/create-with-user (POST) ===")
    
    # Format simple (FastAPI reformatte pour Spring)
    data = {
        "nom": "Test",
        "prenom": "Medecin",
        "specialite": "Cardiologie",
        "telephone": "0123456789",
        "adresse": "Paris",
        "username": "testmed456",
        "email": "testmed456@email.com"
    }
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(
                f"{FASTAPI_URL}/api/v1/medecins/create-with-user",
                json=data,
                headers={"Authorization": f"Bearer {token}"}
            )
            print(f"Status: {response.status_code}")
            print(f"Response: {response.text[:500]}")
            return response.status_code == 200
        except Exception as e:
            print(f"❌ Erreur connexion: {e}")
            return False

async def test_spring_boot_health():
    """Teste si Spring Boot est accessible"""
    print("\n=== Test 0: Spring Boot Health ===")
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(f"{SPRING_BOOT_URL}/actuator/health")
            print(f"Status: {response.status_code}")
            if response.status_code == 200:
                print(f"✅ Spring Boot UP: {response.json()}")
                return True
            else:
                print(f"⚠️ Spring Boot status: {response.status_code}")
                return False
        except Exception as e:
            print(f"❌ Spring Boot inaccessible: {e}")
            return False

async def main():
    print("=" * 60)
    print("DIAGNOSTIC FastAPI <-> Spring Boot")
    print("=" * 60)
    
    # Test 0: Spring Boot health
    spring_up = await test_spring_boot_health()
    if not spring_up:
        print("\n⚠️  Spring Boot n'est pas accessible! Démarrez-le d'abord:")
        print("   .\\mvnw.cmd spring-boot:run")
        return
    
    # Récupérer un token
    print("\n--- Récupération du token ---")
    token = await get_token()
    if not token:
        print("❌ Impossible d'obtenir un token")
        return
    print(f"✅ Token obtenu: {token[:50]}...")
    
    # Tests
    results = []
    results.append(("FastAPI Verify", await test_fastapi_verify(token)))
    results.append(("Spring List Medecins", await test_spring_boot_list_medecins(token)))
    results.append(("Spring Create Medecin", await test_spring_boot_create_medecin(token)))
    results.append(("FastAPI Create Medecin", await test_fastapi_create_medecin(token)))
    
    # Résumé
    print("\n" + "=" * 60)
    print("RÉSUMÉ")
    print("=" * 60)
    for name, result in results:
        status = "✅ OK" if result else "❌ FAIL"
        print(f"{status}: {name}")
    
    print("\n" + "=" * 60)

if __name__ == "__main__":
    asyncio.run(main())
