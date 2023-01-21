package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.UserRepository;
import com.smart.entity.User;
import com.smart.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepo;

	@RequestMapping("/")
	public String home(Model model) {
		
		model.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
		
		model.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}
	
	@RequestMapping("/signup")
	public String signup(Model model) {
		
		model.addAttribute("user", new User());
		model.addAttribute("title", "Register - Smart Contact Manager");
		
		return "signup";
	}
	
	//handler for registering user
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user, 
			BindingResult result,
			@RequestParam("profileImg") MultipartFile file , 
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, 
			Model model, HttpSession session) {
		
		try {
			if(!agreement) {
				System.out.println("You have not agreed the Terms and Conditions!");
				throw new Exception("You have not agreed the Terms and Conditions!");
			}
			
			if(result.hasErrors()) {
				model.addAttribute("user",user);
				return "signup";
			}
			
			//processing and uploading image
			if(file.isEmpty()) {
				// if file is empty
				System.out.println("file is empty");
				user.setImageUrl("contact.png");
			}
			else {
				// upload the file in folder and update image name in contact
				user.setImageUrl(file.getOriginalFilename());
				File file2 = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(file2.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING );
			}
			
			user.setEnabled(true);
			user.setRole("ROLE_USER");
			user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
			
			userRepo.save(user);
			model.addAttribute("user",new User());
			session.setAttribute("message", new Message("Successfully Registered!","alert-success"));
			return "signup";
		} catch (Exception e) {
			String errrMsg = "Something went wrong! ";
			if(!e.getMessage().substring(0,32).equals("could not execute statement; SQL")) {
				errrMsg+=e.getMessage();
			}
			else {
				errrMsg+="Email must be Unique !";
			}
			System.out.println(errrMsg);
			session.setAttribute("message", new Message(errrMsg,"alert-danger"));
			return "signup";
		}
		
	}
	
	@GetMapping("/signin")
	public String login(Model model) {
		model.addAttribute("title","Login page");
		return "login";
	}

}
