package com.smartcontactmanager.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.smartcontactmanager.entity.Contact;
import com.smartcontactmanager.entity.User;
import com.smartcontactmanager.repo.ContactRepository;
import com.smartcontactmanager.repo.UserRepository;

@RestController
public class SeachController {
	@Autowired
private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	@GetMapping("/search/{query}")
	public ResponseEntity<?>Search(@PathVariable("query")String query, Principal principal){
		User user=this.userRepository.getUserByUserName(principal.getName());
		List<Contact> contacts=this.contactRepository.findByNameContainingAndUser(query, user);
		return ResponseEntity.ok(contacts);
	}
}
