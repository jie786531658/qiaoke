package com.sunshine.qiaoke.controller;

import com.sunshine.qiaoke.Dao.Admin;
import com.sunshine.qiaoke.common.Msg;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/login")
public class LoginController {

    @GetMapping("/verifyLogin")
    @ResponseBody
    public Msg login(@RequestParam("username") String username, @RequestParam("password") String password,
                     HttpSession session) {
        if (username.equals("admin") && password.equals("123456")) {
            Admin admin = new Admin();
            session.setAttribute("user", admin);
            admin.setUsername(username);
            admin.setPassword(password);
            return Msg.success().add("url", "buyer/buyerList");
        }
        return Msg.fail();
    }

}
