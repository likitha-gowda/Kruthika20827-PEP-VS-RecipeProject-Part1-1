package com.revature.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.revature.model.Chef;

public class AuthenticationService {

    private ChefService chefService;

    public static Map<String, Chef> loggedInUsers = new HashMap<>();

    public AuthenticationService(ChefService chefService) {
        this.chefService = chefService;
        loggedInUsers = new HashMap<>();
    }

    // LOGIN
    public String login(Chef chef) {

        if (chef == null || chef.getUsername() == null || chef.getPassword() == null) {
            return null;
        }

        List<Chef> chefs = chefService.searchChefs(null);

        for (Chef c : chefs) {
            if (c.getUsername().equals(chef.getUsername()) &&
                c.getPassword().equals(chef.getPassword())) {

                String token = UUID.randomUUID().toString();
                loggedInUsers.put(token, c);
                return token;
            }
        }

        return null;
    }

    //  LOGOUT
    public void logout(String token) {
        if (token != null) {
            loggedInUsers.remove(token);
        }
    }

    //  REGISTER
    public Chef registerChef(Chef chef) {

        if (chef == null || chef.getUsername() == null) {
            return null;
        }

        List<Chef> chefs = chefService.searchChefs(null);

        for (Chef c : chefs) {
            if (c.getUsername().equals(chef.getUsername())) {
                return null; // username exists
            }
        }

        chefService.saveChef(chef);
        return chef;
    }

    //  GET USER FROM TOKEN
    public Chef getChefFromSessionToken(String token) {
        if (token == null) return null;
        return loggedInUsers.get(token);
    }
}