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

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Votre compte secrétaire - ERP Clinique");
        message.setFrom("Admin clinique <nekenarakotomalala760@gmail.com>");
        message.setText("Bonjour,\n\nVotre compte a été créé.\n" +
                        "Nom d'utilisateur : " + username + "\n" +
                        "Mot de passe temporaire : " + motDePasse + "\n\n" +
                        "Merci de changer votre mot de passe lors de votre première connexion.");
        mailSender.send(message);
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