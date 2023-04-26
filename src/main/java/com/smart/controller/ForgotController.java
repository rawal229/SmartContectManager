package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import com.smart.dao.UserRepo;
import com.smart.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.service.EmailService;

@Controller
public class ForgotController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    Random random = new Random(1000);

    // email id form open handler
    @RequestMapping("/forgot")
    public String openEmailForm() {
        return "forgot_email_form";
    }

    // email id form open handler
    @PostMapping("/send-otp")
    public String sendOTP(@RequestParam("email") String email, HttpSession session) {
        System.out.println("EMAIL : " + email);

        // generating otp of 4 digit

        int otp = random.nextInt(9999);

        System.out.println("OTP : " + otp);

        // code for send otp email
        String subject = "OTP From SCM";
        String message = "<div style='border:1px solid #e2e2e2; padding:20px'>" +
                "<h1>" +
                "OTP is " +
                "<b>"+otp +
                "</b>" +
                "</h1" +
                "</div>";
        String to = email;

        boolean flag = this.emailService.sendEmail(subject, message, to);

        if (flag) {

            session.setAttribute("myotp",otp);
            session.setAttribute("email",email);
            return "varify_otp";
        } else {
            session.setAttribute("message", "Check your email id !!");
            return "forgot_email_form";
        }
    }

    //verify otp
    @PostMapping("/varify-otp")
    public String varifyOtp(@RequestParam("otp") int otp,HttpSession session) {
        int myOtp = (int) session.getAttribute("myotp");
        String email = (String) session.getAttribute("email");
        if (myOtp==otp) {
            //password change form
            User user = this.userRepo.getUserByUserName(email);

            if (user==null) {
                // send error message
                session.setAttribute("message", "User does not exits with this email !!");
                return "forgot_email_form";
            } else {
                // send change password form

            }

            return "password_change_form";
        }else {
            session.setAttribute("message", "You have entered wrong otp !!");
            return "varify_otp";
        }
    }

    //change password
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("newpassword") String newpassword,HttpSession session){
        String email=(String)session.getAttribute("email");
        User user = this.userRepo.getUserByUserName(email);
        user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
        this.userRepo.save(user);

        return "redirect:/signin?change=password change successfully..";
    }


}
