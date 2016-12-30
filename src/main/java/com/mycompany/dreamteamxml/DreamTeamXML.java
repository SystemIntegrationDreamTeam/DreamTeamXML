/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.dreamteamxml;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.ejb.Stateless;

/**
 *
 * @author Buhrkall
 */
@WebService(serviceName = "requestLoan")
@Stateless()
public class DreamTeamXML {

    static final String SENDING_QUEUE_NAME = "NormalizerQueue";


    @WebMethod(operationName = "request")
    public String request(@WebParam(name = "ssn") String ssn,
            @WebParam(name = "creditScore") int creditScore,
            @WebParam(name = "loanAmount") double loanAmount,
            @WebParam(name = "loanDuration") int loanDuration) throws IOException {

        double interestRate = 15;

        if (creditScore > 600) {
            interestRate -= 5;
        } else if (creditScore < 601 && creditScore > 500) {
            interestRate -= 3;
        } else if (creditScore < 501 && creditScore > 400) {
            interestRate -= 1;
        }

        int durationCut = loanDuration / 360;

        interestRate -= (durationCut * 0.2);

        double amountCut = loanAmount / 100000;

        interestRate -= (amountCut * 0.2);

        // Upper Limit 15%
        // CR Under 400, ingen ændring
        // CR mellem 400 og 500 1%
        // CR mellem 501 og 600 2%
        // CR over 600 3%
        // Falder 0,20 pr. år
        // Falder 0,20 pr. 100.000
        // Lower Limit 5%
        System.out.println("We in the webservice ");
        
        System.out.println("tryinng the send method");
        sender(interestRate,ssn);
        
        return "Message sent";
    }
    
    
    
    public void sender(double interestRate,String ssn) throws IOException{
    
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("datdb.cphbusiness.dk");
        factory.setVirtualHost("student");
        factory.setUsername("Dreamteam");
        factory.setPassword("bastian");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(SENDING_QUEUE_NAME, false, false, false, null);

        String message = "<LoanResponse>"
                + "<interestRate>" + interestRate + "</interestRate>"
                + "<ssn>" + ssn + "</ssn>"
                + "</LoanResponse>";
        
        
        
        System.out.println("Message created as soap");

        channel.basicPublish("", SENDING_QUEUE_NAME, null, message.getBytes());
   
        System.out.println(" [x] Sent '" + message + "'");

        channel.close();
        connection.close();

        
    }

}
