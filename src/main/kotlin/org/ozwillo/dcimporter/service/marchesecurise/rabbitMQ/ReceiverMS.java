package org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ;

import org.ozwillo.dcimporter.model.marchepublic.Consultation;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

public class ReceiverMS{

    @RabbitListener(queues = "marchesecurise")
    public void receive(String incoming) throws InterruptedException{
        System.out.println("Réception du message : " + incoming);
    }
}