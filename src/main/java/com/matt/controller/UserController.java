package com.matt.controller;

import com.matt.entity.User;
import com.matt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("login")
    public String login(User user, HttpSession session){
        User userDB = userService.login(user);
        if (userDB!=null){
            session.setAttribute("user",userDB);
            return "redirect:/file/showAll";

        }else {
            return "redirect:/index";
        }
    }

}
