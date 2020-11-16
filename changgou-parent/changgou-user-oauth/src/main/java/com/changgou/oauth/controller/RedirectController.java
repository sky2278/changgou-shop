package com.changgou.oauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/oauth")
public class RedirectController {

    @RequestMapping("/login")
    public String login(@RequestParam(required = false, value = "ReturnUrl") String ReturnUrl, Model model) {
        model.addAttribute("ReturnUrl", ReturnUrl);
        return "login";
    }

}
