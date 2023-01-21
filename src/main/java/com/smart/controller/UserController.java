package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entity.Contact;
import com.smart.entity.User;
import com.smart.helper.Message;

@RequestMapping("/user")
@Controller
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private ContactRepository contactRepo;
	
	// method for adding common data in response
	@ModelAttribute
	public void addCommonData(Model model, Principal principle){
		String name = principle.getName();
		User user = userRepo.getUserByUserName(name);
		model.addAttribute("user",user);
	}
	
	
	// hander for showing dashBoard
	@RequestMapping("/index")
	public String dashboard(Model model) {
		model.addAttribute("title", "User Dashboard");
		return "user/user_dashboard";
	}
	
	// open form to add contact handler
	@GetMapping("/add-contact")
	public String openAppContactForm(Model model) {
		model.addAttribute("title", "Add Contacts");
		model.addAttribute("contact", new Contact());
		return "user/add_contact";
	}
	
	// processing add-contact form handler
	@PostMapping("/process-contact")
	public String processContact(@Valid @ModelAttribute Contact contact, 
			BindingResult bindingResult, 
			@RequestParam("profileImage") MultipartFile file ,
			Principal principle, 
			Model model, 
			HttpSession session) {
		try {
			String name = principle.getName();
			User user = userRepo.getUserByUserName(name);
			
			//processing and uploading image
			if(file.isEmpty()) {
				// if file is empty
				System.out.println("file is empty");
				contact.setImage("contact.png");
			}
			else {
				// upload the file in folder and update image name in contact
				contact.setImage(file.getOriginalFilename());
				File file2 = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(file2.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING );
			}
			
			contact.setUser(user);
			user.getContact().add(contact);
			userRepo.save(user);
			model.addAttribute("contact",new Contact());
			session.setAttribute("message", new Message("Successfully Registered!","alert-success"));
			return "user/add_contact";
		} catch (Exception e) {
			String errrMsg = "Something went wrong! ";
			if(!e.getMessage().substring(0,50).equals("Could not commit JPA transaction; nested exception")) {
				errrMsg+=e.getMessage();
			}
			session.setAttribute("message", new Message(errrMsg,"alert-danger"));
			return "user/add_contact";
		}
	}
	
	// show contact handler
	@GetMapping("/show-contacts/{page}")
	public String showContact(@PathVariable("page") Integer page ,Model model, Principal principle) {
		model.addAttribute("title","View-Contacts");
		
		String userName = principle.getName();
		User user = userRepo.getUserByUserName(userName);
		
		Pageable pageable = PageRequest.of(page, 6);
		Page<Contact> contacts = contactRepo.findContactsByUserUser(user.getId(), pageable);
		
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages",contacts.getTotalPages());
		
		return "user/show_contact";
	}
	
	// show particular contact details
	@GetMapping("/contact/{CId}")
	public String showContactDetails(@PathVariable("CId") Integer CId, Model model, Principal principle) {
		
		Optional<Contact> contactOptional = contactRepo.findById(CId);
		Contact contact = contactOptional.get();
		
		// applying security to show only contact that login user has added
		String name = principle.getName();
		User user = userRepo.getUserByUserName(name);
		if(user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title",contact.getName().toUpperCase());
		}
		
		return "user/contact_details";
	}
	
	// handler to delete contact
	@GetMapping("/delete/{CId}")
	public String deletContact(@PathVariable("CId") Integer CId, Principal principle) {
		
		try {
			Optional<Contact> optional = contactRepo.findById(CId);
			Contact contact = optional.get();
			
			// check for granting permission to delete only login user contacts
			String name = principle.getName();
			User user = userRepo.getUserByUserName(name);
			
			if(user.getId() == contact.getUser().getId()) {
				// deleting image
				String image = contact.getImage();
				File file = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(file.getAbsolutePath()+File.separator+image);
				Files.delete(path);
				
				contactRepo.delete(contact);
			}
			
			return "redirect:/user/show-contacts/0";
			
		} catch (Exception e) {
			System.out.println("ERROR " + e.toString());
			return "redirect:/user/show-contacts/0";
		}
	}

	// open update form handler
	@PostMapping("/update-contact/{CId}")
	public String updateForm(@PathVariable("CId") Integer CId, Model model) {
		model.addAttribute("title", "Update-Contact");
		Contact contact = contactRepo.findById(CId).get();
		model.addAttribute("contact",contact);
		return "user/update_contact";
	}

	// update contact handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, 
			@RequestParam("profileImage") MultipartFile multipartFile,
			Model model, Principal principle) {
			
		try {
			// old contact details
			Contact oldContact = contactRepo.findById(contact.getCId()).get();
			
			// if image is updated
			if(!multipartFile.isEmpty()) {
				// delete old photo
				File deletFile = new ClassPathResource("static/img").getFile();
				File files = new File(deletFile, oldContact.getImage());
				files.delete();
				
				// uploading new photo
				File file2 = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(file2.getAbsolutePath()+File.separator+multipartFile.getOriginalFilename());
				Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING );
				
				// updating name of new photo  in database
				contact.setImage(multipartFile.getOriginalFilename());
			}
			else {
				// if image is not updated
				contact.setImage(oldContact.getImage());
			}
			
			User user = userRepo.getUserByUserName(principle.getName());
			contact.setUser(user);
			contactRepo.save(contact);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "redirect:/user/contact/" + contact.getCId();
	}

	// open profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title", "Profile");
		return "user/profile";
	}
	
	
	// open update user handler
	@PostMapping("/update-user-form/{id}")
	public String updateUserForm(@PathVariable("id") Integer id, Model model) {
		model.addAttribute("title", "Update-User");
		User user = userRepo.findById(id).get();
		model.addAttribute("user",user);
		return "user/user_update";
	}
	
	
	// update user handler
	@PostMapping("/user-update")
	public String updateUser(@ModelAttribute User user, 
			@RequestParam("profileImg") MultipartFile multipartFile,
			Model model, HttpSession session, Principal principle) {
		
		
		try {
			// getting old user
			User oldUser = userRepo.findById(user.getId()).get();
			
			// if image is updated
			if(!multipartFile.isEmpty()) {
				// delete old photo
				File deletFile = new ClassPathResource("static/img").getFile();
				File files = new File(deletFile, oldUser.getImageUrl());
				files.delete();
				
				// uploading new photo
				File file2 = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(file2.getAbsolutePath()+File.separator+multipartFile.getOriginalFilename());
				Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING );
				
				// updating name of new photo  in database
				user.setImageUrl(multipartFile.getOriginalFilename());
			}
			else {
				// if image is not updated
				user.setImageUrl(oldUser.getImageUrl());
			}
			
			// updating user
			User newUser = userRepo.getUserByUserName(principle.getName());
			userRepo.save(newUser);
			model.addAttribute("user",new User());
			session.setAttribute("message", new Message("Updated Successfully...","alert-success"));
			return "redirect:/user/profile";
			
		} catch (Exception e) {
			System.out.println(e.getCause());
			session.setAttribute("message", new Message("Something went wrong!" + e.getMessage(),"alert-danger"));
			return "user/user_update";
		}
			
	}
	
	// open settings handler
	@GetMapping("/settings")
	public String openSettings() {
		

		return "user/settings";
	}
	
	// change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, 
			@RequestParam("newPassword") String newPassword,
			Principal principle,
			HttpSession session) {
		
		String name = principle.getName();
		User currentUser = userRepo.getUserByUserName(name);
		String userPassword = currentUser.getPassword();
		
		if(bCryptPasswordEncoder.matches(oldPassword, userPassword)) {
			// change the password
			currentUser.setPassword(bCryptPasswordEncoder.encode(newPassword));
			userRepo.save(currentUser);
			session.setAttribute("message", new Message("Your Password is Successfully Updated!", "alert-success"));
			return "redirect:/user/index";
		}
		else {
			session.setAttribute("message", new Message("Old Password Does Not Match!", "alert-danger"));
			return "redirect:/user/settings";
		}
		
	}
	
}

