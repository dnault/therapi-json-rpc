package com.github.therapi.example.boot;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SomeOtherRestController {
    @RequestMapping("/rest")
    public String coexist() {
        return "I am a boring old REST controller. "
                + "My job is to show that REST and JSON-RPC can co-exist. "
                + "May I go on vacation now?";
    }
}