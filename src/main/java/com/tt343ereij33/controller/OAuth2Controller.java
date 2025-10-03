package com.tt343ereij33.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/auth/oauth2")
public class OAuth2Controller {
    @GetMapping("/google")
    public RedirectView loginWithGoogle() {
        RedirectView redirectView = new RedirectView("/oauth2/authorization/GOOGLE");
        redirectView.setExposeModelAttributes(false);
        return redirectView;
    }

    @GetMapping("/error")
    public RedirectView oauth2Error() {
        RedirectView redirectView = new RedirectView("/home?oauth2=error");
        redirectView.setExposeModelAttributes(false);
        return redirectView;
    }
}


