package com.smart.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;



@Service
public class EmailService {
	public boolean sendEmail(String subject, String message, String to) {
		
		boolean flag = false;
		
		// sending otp from this mail
		String from = "smartcontactmanager01@gmail.com";
		
		//variable for gmail
		String host = "smtp.gmail.com";
		
		// get the system properties
		Properties properties = System.getProperties();
		System.out.println("Properties: "+properties);
		
		// setting host
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", 465);
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");
		
		// step1: to get the session object
		Session session = Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("smartcontactmanager01@gmail.com", "bnrhximreetlymcv");
			}
		});
		
		session.setDebug(true);
		
		// step 2: compose the message[text/ multimedia]
		MimeMessage mimeMessage = new MimeMessage(session);
		
		try {
			// from email
			mimeMessage.setFrom(from);
			
			// adding recipient to message
			mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			
			// adding subject to message
			mimeMessage.setSubject(subject);
			
			// adding text to message
			mimeMessage.setText(message);
			
			// step 3: send the message using transport class
			Transport.send(mimeMessage);
			
			System.out.println("sent successfully.........");
			flag = true;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return flag;
	}
}
