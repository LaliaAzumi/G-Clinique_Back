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

    // public void sendPasswordEmail(String email, String username, String motDePasse) {
    //     if (email == null || email.trim().isEmpty()) {
    //         System.err.println("Erreur : Impossible d'envoyer l'email de mot de passe, l'adresse est vide.");
    //         return;
    //     }

    //     try {
    //         SimpleMailMessage message = new SimpleMailMessage();
    //         message.setTo(email);
    //         message.setSubject("Votre compte a ete cree - ERP Clinique");
    //         message.setFrom("Admin clinique <nekenarakotomalala760@gmail.com>");
    //         message.setText("Bonjour,\n\nVotre compte a ete cree.\n" +
    //                         "Nom d'utilisateur : " + username + "\n" +
    //                         "Mot de passe temporaire : " + motDePasse + "\n\n" +
    //                         "Merci de changer votre mot de passe lors de votre premiere connexion.");
    //         mailSender.send(message);
    //         System.out.println("Email envoye avec succes a : " + email);
    //     } catch (Exception e) {
    //         System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
    //         e.printStackTrace();
    //     }
    // }
    public void sendPasswordEmail(String email, String username, String motDePasse) {

        if (email == null || email.trim().isEmpty()) {
            System.err.println("Email vide");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Bienvenue sur G-Clinique");
            helper.setFrom("G-Clinique <noreply@gclinique.com>");

            String html =
                    "<div style='font-family:Arial;background:#f4f6f8;padding:30px'>" +

                    "<div style='max-width:600px;margin:auto;background:white;border-radius:12px;overflow:hidden'>" +

                    "<div style='background:#227c70;color:white;padding:20px;text-align:center'>" +
                    "<h2 style='margin:0'>G-Clinique</h2>" +
                    "</div>" +

                    "<div style='padding:25px'>" +

                    "<h3 style='color:#227c70'>Bienvenue</h3>" +

                    "<p>Votre compte a été créé avec succès.</p>" +

                    "<p><b>Nom d'utilisateur :</b> " + username + "</p>" +

                    "<div style='text-align:center;margin:25px 0'>" +
                    "<p style='color:#666;font-size:13px'>Mot de passe temporaire</p>" +

                    "<div style='background:#227c70;color:white;padding:15px 25px;" +
                    "display:inline-block;font-size:20px;letter-spacing:2px;border-radius:10px'>" +
                    motDePasse +
                    "</div>" +
                    "</div>" +

                    "<div style='background:#fff3cd;padding:10px;border-radius:8px;font-size:12px'>" +
                    "Attention : changez votre mot de passe dès la première connexion." +
                    "</div>" +

                    "</div>" +

                    "<div style='text-align:center;font-size:11px;color:#aaa;padding:10px'>" +
                    "© GClinique" +
                    "</div>" +

                    "</div>" +
                    "</div>";

            helper.setText(html, true);
            mailSender.send(message);

            System.out.println("Email envoyé à " + email);

        } catch (Exception e) {
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