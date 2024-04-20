package com.smartcontactmanager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smartcontactmanager.entity.Contact;
import com.smartcontactmanager.entity.User;
import com.smartcontactmanager.helper.Message;
import com.smartcontactmanager.repo.ContactRepository;
import com.smartcontactmanager.repo.UserRepository;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	// method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("USERNAME " + userName);

		// get the user using userNamne(Email)
		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER " + user);
		model.addAttribute("user", user);

	}

	// dashboard home handler
	@GetMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashbord";
	}
// this handler is for adding the contact
	// open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());

		return "normal/add_contact_form";
	}
	
	
//	 this handler is for the processing the contact detail to the new form
	
	@PostMapping("/process_contact")
	public String processContact(@ModelAttribute Contact contact
			,@RequestParam("profileImage" )MultipartFile file 
			,Principal principal, HttpSession session) {
		try {
			
		
	String name=principal.getName();
	User user=this.userRepository.getUserByUserName(name);
	if(file.isEmpty()) {
		System.out.println("file is empty!!");
		contact.setImage("contact.png");
	}
	else {
		contact.setImage(file.getOriginalFilename());
	File saveFile=new ClassPathResource("static/img").getFile();	
	Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
	Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
		 
		 }
	contact.setUser(user);
	user.getContacts().add(contact);
	this.userRepository.save(user);
session.setAttribute("message", new Message(" contact is succesfully Added!!", 
		"success"));
		}
		catch(Exception e){
			System.out.println("ERROR " + e.getMessage());
			e.printStackTrace();
			session.setAttribute("message", new Message("Some went wrong !! Try again..", "danger"));
		}
		
		return "normal/add_contact_form";
	}
	
//	this is the view contact details handler
	
	@GetMapping("/show-contact/{page}")
	public String showContacts(@PathVariable("page") Integer page ,Model m , Principal principal) {

		m.addAttribute("tittle", "show user contacts");
		
		
		String userName=principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		

	
Pageable pageable= PageRequest.of(page,10);


Page<Contact> contacts =  this.contactRepository.findContactsByUser(user.getId(), pageable);



  m.addAttribute("contacts", contacts);
  
 m.addAttribute("currentPage", page);
 
 m.addAttribute("totalPages", contacts.getTotalPages());

		return"normal/show_contacts";
	}
	
//	 details handler for the contact
	@GetMapping("/{Id}/contact")
	  public String showContactdetails(@PathVariable ("Id") Integer cId, Model model ,Principal principal) {
		Optional<Contact> optionalcontact=this.contactRepository.findById(cId);
		Contact contact=optionalcontact.get();
	String userName=	principal.getName();
	User user=this.userRepository.getUserByUserName(userName);
	if(user.getId()==contact.getUser().getId()) {
		model.addAttribute("contact", contact);
		model.addAttribute("title", contact.getName());
	}
	
		  return "normal/contact_detail";
	  }
	
//	handler for deleting the contact
	@GetMapping("/delete/{cId}")
	@Transactional
	public String deleteContact(@PathVariable("cId") Integer cId, Model model,Principal principal, HttpSession session) {
		
		Contact contact= this.contactRepository.findById(cId).get();
		User user=this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
	this.userRepository.save(user);
	session.setAttribute("message", new Message("Contact deleted succesfully...", "success"));

	return "redirect:/user/show-contact/0";
	}
	
	@PostMapping("/update-contact/{cId}")
public String updateForm(@PathVariable("cId")Integer cId, Model m) {
		m.addAttribute("tittle", "updatecontact");
		Contact contact= this.contactRepository.findById(cId).get();
		m.addAttribute("contact" ,contact);
	return "normal/update_form";
}
	
//	...............
	
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact,
			@RequestParam("profileImage")MultipartFile file, Model m ,Principal principal, HttpSession session) {
		
		
		
		Contact oldDetailContact=this.contactRepository.findById(contact.getcId()).get();
		
		try {
			if(!file.isEmpty()) {
				File deleteFile=new ClassPathResource("static/img").getFile();
				File file1= new File(deleteFile,oldDetailContact.getImage());
				file1.delete();
				
//				updating the new form
				
				
				File saveFile=new ClassPathResource("static/img").getFile();
				
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
			}else {
				contact.setImage(oldDetailContact.getImage());
			}
			User user=this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("your contact is updated...", "success"));
		}
		catch(Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("your contact is not updated...", "danger"));
		}


		return "redirect:/user/" + contact.getcId() + "/contact";
	}
	
	
}
