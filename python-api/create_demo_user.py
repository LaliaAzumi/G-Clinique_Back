"""
Script de création d'un utilisateur démo
Usage: python create_demo_user.py
"""
import pymysql
import bcrypt
from config import settings

def create_demo_user():
    """Crée un utilisateur démo dans la base de données MySQL"""
    
    # Configuration de la connexion MySQL
    # Extrait l'hôte et la base de données depuis l'URL SQLAlchemy ou utilise les valeurs par défaut
    host = "localhost"
    port = 3306
    database = "clinique"
    user = "root"
    password = ""
    
    # Informations de l'utilisateur démo
    demo_username = "demo"
    demo_email = "demo@gmail.com"
    demo_password = "demo123"
    demo_role = "ADMIN"  # Rôle administrateur pour les tests
    
    # Hachage du mot de passe avec BCrypt
    password_hash = bcrypt.hashpw(demo_password.encode('utf-8'), bcrypt.gensalt())
    password_hash_str = password_hash.decode('utf-8')
    
    print(f"Création de l'utilisateur démo...")
    print(f"Username: {demo_username}")
    print(f"Email: {demo_email}")
    print(f"Mot de passe: {demo_password}")
    print(f"Rôle: {demo_role}")
    print(f"Hash du mot de passe: {password_hash_str[:20]}...")
    
    try:
        # Connexion à la base de données
        connection = pymysql.connect(
            host=host,
            port=port,
            database=database,
            user=user,
            password=password,
            charset='utf8mb4'
        )
        
        with connection.cursor() as cursor:
            # Vérifie si l'utilisateur existe déjà
            check_sql = "SELECT id FROM users WHERE username = %s OR email = %s"
            cursor.execute(check_sql, (demo_username, demo_email))
            existing_user = cursor.fetchone()
            
            if existing_user:
                print(f"\n⚠️  L'utilisateur '{demo_username}' ou l'email '{demo_email}' existe déjà.")
                print(f"ID existant: {existing_user[0]}")
                
                # Option pour mettre à jour le mot de passe
                update_choice = input("Voulez-vous mettre à jour le mot de passe? (o/n): ")
                if update_choice.lower() == 'o':
                    update_sql = """
                        UPDATE users 
                        SET mdp = %s, first_login = 1 
                        WHERE username = %s
                    """
                    cursor.execute(update_sql, (password_hash_str, demo_username))
                    connection.commit()
                    print(f"✅ Mot de passe de '{demo_username}' mis à jour avec succès!")
                else:
                    print("Opération annulée.")
                    return
            else:
                # Insère le nouvel utilisateur
                insert_sql = """
                    INSERT INTO users (username, email, mdp, role, first_login) 
                    VALUES (%s, %s, %s, %s, %s)
                """
                cursor.execute(insert_sql, (
                    demo_username, 
                    demo_email, 
                    password_hash_str, 
                    demo_role, 
                    True
                ))
                connection.commit()
                
                # Récupère l'ID généré
                cursor.execute("SELECT LAST_INSERT_ID()")
                user_id = cursor.fetchone()[0]
                
                print(f"\n✅ Utilisateur démo créé avec succès!")
                print(f"   ID: {user_id}")
                print(f"   Username: {demo_username}")
                print(f"   Email: {demo_email}")
                print(f"   Rôle: {demo_role}")
                print(f"\n🔑 Vous pouvez maintenant vous connecter avec:")
                print(f"   Username: {demo_username}")
                print(f"   Password: {demo_password}")
                
    except pymysql.MySQLError as e:
        print(f"\n❌ Erreur MySQL: {e}")
        print("Vérifiez que:")
        print("  - MySQL est démarré")
        print("  - La base 'clinique' existe")
        print("  - Les identifiants de connexion sont corrects")
    except Exception as e:
        print(f"\n❌ Erreur inattendue: {e}")
    finally:
        if 'connection' in locals() and connection:
            connection.close()
            print("\n🔌 Connexion fermée.")

if __name__ == "__main__":
    create_demo_user()
