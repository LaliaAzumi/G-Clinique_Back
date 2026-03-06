package com.erp.clinique.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class NotifController {

    @MessageMapping("/sendMessage") // url côté JS pour envoyer
    @SendTo("/topic/messages") // url pour diffuser
    public String sendMessage(String message) {
        return message; // ici tu peux enrichir le message avec date, user, etc.
    }
}