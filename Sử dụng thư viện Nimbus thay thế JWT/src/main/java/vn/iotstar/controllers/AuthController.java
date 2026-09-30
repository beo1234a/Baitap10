package vn.iotstar.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import vn.iotstar.entity.User;

@Controller
@RequestMapping("/")
public class AuthController {

    @GetMapping("/login")
    public String index() {
        return "login";
    }

    @GetMapping("/profile")
    public String profile(Model model) {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication != null
                && authentication.getPrincipal() instanceof User) {

            User user = (User) authentication.getPrincipal();

            model.addAttribute("user", user);
        }

        return "profile";
    }
}