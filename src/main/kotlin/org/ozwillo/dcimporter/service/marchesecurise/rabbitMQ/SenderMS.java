package org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ;

import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight;
import org.ozwillo.dcimporter.model.marchepublic.Consultation;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;


public class SenderMS{

    @Autowired
    private RabbitTemplate template;


    @Autowired
    private TopicExchange topic;

    public void send() throws InterruptedException, AmqpException {

        final String KEY = "consultation.20003019500115.create";
        String message = "Message test d'envoi pour le groupe : '" + KEY + "'";


        template.convertAndSend(topic.getName(), KEY, message);
        System.out.println(message);
    }
}