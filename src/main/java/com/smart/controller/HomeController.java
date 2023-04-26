package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.smart.dao.UserRepo;
import com.smart.entities.Contect;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	// @GetMapping("/test")
	// @ResponseBody
	// public String test() {

	// User user = new User();
	// user.setName("Rawal Nishant");
	// user.setEmail("rawaln19@gmail.com");
	// user.setPassword("xyz123");
	// user.setAbout("I am testing test handler");

	// Contect contect = new Contect();

	// user.getContects().add(contect);

	// userRepo.save(user);
	// return "working";
	// }

	@GetMapping("/home")
	public String home(Model model) {
		System.out.println("Inside home controller handler !!");
		model.addAttribute("title", "Home - Smart Contect Manager");
		return "home";
	}

	@GetMapping("/about")
	public String about(Model model) {
		System.out.println("Inside about controller handler !!");
		model.addAttribute("title", "About - Smart Contect Manager");
		return "about";
	}

	@RequestMapping("/signup")
	public String signup(Model model) {
		System.out.println("Inside about controller handler !!");
		model.addAttribute("title", "Register - Smart Contect Manager");
		model.addAttribute("user", new User());
		return "signup";
	}

	// handler for register user
	@RequestMapping(value = "/do_register", method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
			Model model,
			HttpSession session) {

		try {

			if (!agreement) {
				System.out.println("You have not agreed the trems and conditions");
				throw new Exception("You have not agreed the trems and conditions");
			}

			if (result1.hasErrors()) {
				System.out.println("Error : " + result1.toString());
				model.addAttribute("user", user);
				return "signup";
			}

			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			System.out.println("Agreement : " + agreement);
			System.out.println("USER : " + user);

			User result = this.userRepo.save(user);

			model.addAttribute("user", new User());

			session.setAttribute("message", new Message("Successfully registerd !!", "alert-success"));
			return "signup";

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Somthing went wrong !!" + e.getMessage(), "alert-danger"));
			return "signup";
		}
		// return "signup";
	}

	// handler of custom login form
	@GetMapping("/signin")
	public String login(Model model) {
		model.addAttribute("title", "Login Page");
		return "login";
	}
}

// 34 number video external css