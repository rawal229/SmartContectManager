package com.smart.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.smart.dao.ContectRepo;
import com.smart.dao.UserRepo;
import com.smart.entities.Contect;
import com.smart.entities.User;

@RestController
public class SearchController {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ContectRepo contectRepo;

    // search handler
    @GetMapping("/search/{query}")
    public ResponseEntity<?> search(@PathVariable("query") String query, Principal principal) {
        System.out.println("Query : " + query);

        User user = this.userRepo.getUserByUserName(principal.getName());

        List<Contect> contects = this.contectRepo.findByNameContainingAndUser(query, user);

        return ResponseEntity.ok(contects);
    }
}
