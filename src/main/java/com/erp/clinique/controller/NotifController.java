package com.erp.clinique.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class NotifController {

    @MessageMapping("/sendMessage") 
    @SendTo("/topic/messages") 
    public String sendMessage(String message) {
        return message; 
    }
}