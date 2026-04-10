package com.erp.clinique.service;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendPasswordEmail(String email, String username, String motDePasse) {
        if (email == null || email.trim().isEmpty()) {
            System.err.println("Erreur : Impossible d'envoyer l'email de mot de passe, l'adresse est vide.");
            return;
        }

        try {
            // SimpleMailMessage message = new SimpleMailMessage();
            // message.setTo(email);
            // message.setSubject("Votre compte a ete cree - ERP Clinique");
            // message.setFrom("Admin clinique <adrianostanislas2@gmail.com>");
            // message.setText("Bonjour,\n\nVotre compte a ete cree.\n" +
            //                 "Nom d'utilisateur : " + username + "\n" +
            //                 "Mot de passe temporaire : " + motDePasse + "\n\n" +
            //                 "Merci de changer votre mot de passe lors de votre premiere connexion.");
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Votre compte a ete cree - ERP Clinique");
            helper.setFrom("Admin clinique <adrianostanislas2@gmail.com>");

            // String htmlContent = "<!DOCTYPE html>\n" +
            //     "<html>\n" +
            //     "<head>\n" +
            //     "    <meta charset=\"UTF-8\">\n" +
            //     "    <style>\n" +
            //     "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }\n" +
            //     "        .container { max-width: 600px; margin: 0 auto; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 15px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.2); }\n" +
            //     "        .header { background: rgba(255,255,255,0.1); padding: 30px; text-align: center; border-bottom: 1px solid rgba(255,255,255,0.2); }\n" +
            //     "        .header h1 { color: white; margin: 0; font-size: 28px; text-shadow: 2px 2px 4px rgba(0,0,0,0.3); }\n" +
            //     "        .header-icon { font-size: 50px; margin-bottom: 10px; }\n" +
            //     "        .content { background: white; padding: 40px; }\n" +
            //     "        .welcome { color: #333; font-size: 18px; margin-bottom: 25px; text-align: center; }\n" +
            //     "        .info-box { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 25px; border-radius: 12px; margin: 20px 0; }\n" +
            //     "        .info-label { font-size: 14px; text-transform: uppercase; letter-spacing: 1px; opacity: 0.9; margin-bottom: 5px; }\n" +
            //     "        .info-value { font-size: 20px; font-weight: bold; margin-bottom: 15px; }\n" +
            //     "        .password { background: #ff6b6b; color: white; padding: 15px; border-radius: 8px; text-align: center; font-size: 22px; font-weight: bold; letter-spacing: 2px; margin: 10px 0; }\n" +
            //     "        .warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 25px 0; border-radius: 5px; color: #856404; }\n" +
            //     "        .footer { background: #f8f9fa; padding: 25px; text-align: center; color: #666; font-size: 13px; border-top: 1px solid #e9ecef; }\n" +
            //     "        .btn { display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 15px 35px; text-decoration: none; border-radius: 30px; font-weight: bold; margin: 20px 0; box-shadow: 0 4px 15px rgba(102,126,234,0.4); }\n" +
            //     "    </style>\n" +
            //     "</head>\n" +
            //     "<body>\n" +
            //     "    <div class=\"container\">\n" +
            //     "        <div class=\"header\">\n" +
            //     "            <div class=\"header-icon\">🏥</div>\n" +
            //     "            <h1>ERP Clinique</h1>\n" +
            //     "        </div>\n" +
            //     "        <div class=\"content\">\n" +
            //     "            <div class=\"welcome\">\n" +
            //     "                <strong>Bienvenue !</strong> Votre compte a été créé avec succès.\n" +
            //     "            </div>\n" +
            //     "            <div class=\"info-box\">\n" +
            //     "                <div class=\"info-label\">Nom d'utilisateur</div>\n" +
            //     "                <div class=\"info-value\">" + username + "</div>\n" +
            //     "                <div class=\"info-label\">Mot de passe temporaire</div>\n" +
            //     "                <div class=\"password\">" + motDePasse + "</div>\n" +
            //     "            </div>\n" +
            //     "            <div class=\"warning\">\n" +
            //     "                <strong>⚠️ Important :</strong> Pour des raisons de sécurité, merci de changer votre mot de passe lors de votre première connexion.\n" +
            //     "            </div>\n" +
            //     "            <div style=\"text-align: center;\">\n" +
            //     "                <a href=\"#\" class=\"btn\">Accéder à mon compte</a>\n" +
            //     "            </div>\n" +
            //     "        </div>\n" +
            //     "        <div class=\"footer\">\n" +
            //     "            <p>Cet email a été envoyé automatiquement par le système ERP Clinique.</p>\n" +
            //     "            <p>Si vous n'êtes pas à l'origine de cette demande, veuillez contacter l'administrateur.</p>\n" +
            //     "        </div>\n" +
            //     "    </div>\n" +
            //     "</body>\n" +
            //     "</html>";

            
            // helper.setText(htmlContent, true);
            
            String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <style>\n" +
                "        body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f0f2f5; margin: 0; padding: 40px 0; }\n" +
                "        .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.08); border: 1px solid #e1e4e8; }\n" +
                "        .header { background-color: #ffffff; padding: 30px; text-align: center; border-bottom: 3px solid #0056b3; }\n" +
                "        .header h1 { color: #003366; margin: 0; font-size: 24px; letter-spacing: 0.5px; }\n" +
                "        .content { padding: 40px; line-height: 1.6; }\n" +
                "        .welcome { color: #2c3e50; font-size: 18px; margin-bottom: 20px; }\n" +
                "        .info-card { background: #f8f9fa; border: 1px solid #dee2e6; border-radius: 10px; padding: 25px; margin: 25px 0; }\n" +
                "        .info-row { margin-bottom: 15px; }\n" +
                "        .info-label { font-size: 12px; color: #6c757d; text-transform: uppercase; font-weight: bold; margin-bottom: 4px; }\n" +
                "        .info-value { font-size: 16px; color: #333; font-family: monospace; background: #eceef1; padding: 4px 8px; border-radius: 4px; }\n" +
                "        .password-display { display: block; background: #ffffff; border: 2px dashed #0056b3; color: #0056b3; padding: 15px; text-align: center; font-size: 20px; font-weight: bold; margin-top: 10px; border-radius: 6px; }\n" +
                "        .warning-box { background-color: #fff9db; border-left: 4px solid #fcc419; padding: 15px; margin: 25px 0; font-size: 14px; color: #664d03; }\n" +
                "        .btn-container { text-align: center; margin-top: 30px; }\n" +
                "        .btn { background-color: #0056b3; color: #ffffff !important; padding: 14px 32px; text-decoration: none; border-radius: 6px; font-weight: 600; display: inline-block; }\n" +
                "        .footer { background: #f8f9fa; padding: 20px; text-align: center; color: #95a5a6; font-size: 12px; border-top: 1px solid #eee; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h1>ERP CLINIQUE</h1>\n" +
                "        </div>\n" +
                "        <div class=\"content\">\n" +
                "            <p class=\"welcome\">Bonjour,</p>\n" +
                "            <p>Votre compte utilisateur pour la plateforme <strong>ERP Clinique</strong> a été créé avec succès par votre administrateur.</p>\n" +
                "            \n" +
                "            <div class=\"info-card\">\n" +
                "                <div class=\"info-row\">\n" +
                "                    <div class=\"info-label\">Identifiant</div>\n" +
                "                    <span class=\"info-value\">" + username + "</span>\n" +
                "                </div>\n" +
                "                <div class=\"info-row\">\n" +
                "                    <div class=\"info-label\">Mot de passe temporaire</div>\n" +
                "                    <div class=\"password-display\">" + motDePasse + "</div>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "\n" +
                "            <div class=\"warning-box\">\n" +
                "                <strong>Mesure de sécurité :</strong> Pour valider votre accès, vous devrez obligatoirement modifier ce mot de passe lors de votre première connexion.\n" +
                "            </div>\n" +
                "\n" +
                "            <div class=\"btn-container\">\n" +
                "                <a href=\"http://localhost:8081\" class=\"btn\">Se connecter à l'ERP</a>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        <div class=\"footer\">\n" +
                "            <p>Ceci est un message automatique, merci de ne pas y répondre.</p>\n" +
                "            <p>&copy; 2024 Système ERP Clinique - Support Technique</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
                
            helper.setText(htmlContent, true);
            mailSender.send(message);
            // System.out.println("Email envoye avec succes a : " + email);
            System.out.println("Email HTML envoye avec succes a : " + email);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
   
    public void sendRendezVousEmail(String to, String sujet, String corps) {
    	
        if (to == null || to.trim().isEmpty()) {
            System.err.println("Annulation de l'envoi : L'adresse email du patient est manquante.");
            return; 
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(sujet);
            message.setFrom("Clinique ERP <nekenarakotomalala760@gmail.com>");
            message.setText(corps);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Erreur technique lors de l'envoi de l'email : " + e.getMessage());
        }
   }
    
    public void sendPdfEmail(String to, String subject, String text, File pdf) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text);
        helper.addAttachment(pdf.getName(), pdf);

        mailSender.send(message);
    }

}