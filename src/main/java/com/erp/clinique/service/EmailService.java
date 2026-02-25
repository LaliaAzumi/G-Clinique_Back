package com.erp.clinique.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
	@Autowired
    private JavaMailSender mailSender;

    public void sendPasswordEmail(String email, String username,String motDePasse) {
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
}