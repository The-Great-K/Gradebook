package me.thegreatk.gradebook;

import me.thegreatk.gradebook.profile.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("login", new User());
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(@ModelAttribute("login") User user, Model model) {
        model.addAttribute("user", new Profile(user.getUsername(), user.getPassword()));
        model.addAttribute("adminAccess", (user.getUsername().equals("admin") && user.getPassword().equals("1234")));
        return "result";
    }
}
