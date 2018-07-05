package org.ozwillo.dcimporter.web.marchesecurise.rabbitMQ;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ozwillo.dcimporter.AbstractIntegrationTests;
import org.ozwillo.dcimporter.model.marchepublic.Consultation;
import org.ozwillo.dcimporter.model.marchepublic.FinaliteMarcheType;
import org.ozwillo.dcimporter.model.marchepublic.TypeMarcheType;
import org.ozwillo.dcimporter.model.marchepublic.TypePrestationType;
import org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ.JsonConverter;
import org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ.SenderMS;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CreationConsultationSenderTest{

    @Autowired
    SenderMS sender;

    @Test
    public void sendMessage() throws InterruptedException {

        String reference = "test référence";
        String objet = "test objet";
        LocalDateTime datePublication = LocalDateTime.now();
        LocalDateTime dateCloture = LocalDateTime.now();
        FinaliteMarcheType finaliteMarche = FinaliteMarcheType.AUTRE;
        TypeMarcheType typeMarche = TypeMarcheType.AUTRE;
        TypePrestationType typePrestation = TypePrestationType.AUTRES;
        List<Integer> departementsPrestation = Arrays.asList(74, 38, 69, 06);
        String passation = "test passation";
        boolean informatique = true;
        String passe = "test passe";
        List<String> emails = Arrays.asList("test@test.com");
        boolean enLigne = true;
        boolean alloti = true;
        boolean invisible = true;
        int nbLots = 0;

        Consultation consultation = new Consultation(reference, objet, datePublication, dateCloture, finaliteMarche, typeMarche, typePrestation, departementsPrestation, passation, informatique, passe, emails, enLigne, alloti, invisible, nbLots);

        JsonConverter.consultationToJson(consultation);
        try {
            sender.send();
        }catch (AmqpException e){
            e.getMessage();
        }
    }
}