package me.thegreatk.gradebook;

import me.thegreatk.gradebook.profile.Profile;
import me.thegreatk.gradebook.profile.ProfileRequestPacket;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("login", new ProfileRequestPacket());
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(@ModelAttribute("login") ProfileRequestPacket packet, Model model) {
        model.addAttribute("profile", new Profile(packet));
        return "result";
    }
}
