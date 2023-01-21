package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entity.User;
import com.smart.service.EmailService;

@Controller
public class ForgotController {
	
	@Autowired
	private EmailService service;
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	// email id form open handler
	@RequestMapping("forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOPT(@RequestParam("email") String email, HttpSession session) {
		System.out.println("Email: "+email);
		//generating random otp
		Random random = new Random();
		int otp = Integer.parseInt( String.format("%04d", random.nextInt(10000)) );
		System.out.println(otp);
		
		// sending otp to email
		String subject = "OTM from SCM";
		String message = "OTP = " + otp;
		String to = email;
		
		boolean flag = service.sendEmail(subject, message, to);
		if(flag) {
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
		}
		else {
			session.setAttribute("message", "Wrong Email Id!");
			return "forgot_email_form";
		}
	}
	
	// verify otp
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session) {
		int myotp = (int) session.getAttribute("myotp");
		String email = (String) session.getAttribute("email");
		
		if(myotp == otp) {
			// returning password change form view
			User user = userRepo.getUserByUserName(email);
			if(user == null) {
				// send error message
				session.setAttribute("message", "User does not exist with this email !");
				return "forgot_email_form";
			}
			else {
				// send password change form
			}
			
			return "password_change_form";
		}
		else {
			session.setAttribute("message", "You have entered wrong password!");
			return "verify_otp";
		}
	}
	
	// change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword, HttpSession session) {
		String email = (String) session.getAttribute("email");
		User user = userRepo.getUserByUserName(email);
		user.setPassword(bCryptPasswordEncoder.encode(newpassword));
		userRepo.save(user);
		return "redirect:/signin?change=password changed successfully...";
		
	}
}
