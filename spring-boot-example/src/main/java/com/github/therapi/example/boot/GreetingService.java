package com.github.therapi.example.boot;

import com.github.therapi.core.annotation.Default;
import com.github.therapi.core.annotation.Remotable;
import org.springframework.stereotype.Service;

@Service
@Remotable("greeting")
public class GreetingService {
    /**
     * Starts a conversation.
     *
     * @param name The name of the person to greet
     * @return A friendly greeting message
     */
    @Remotable
    public String greet(@Default("stranger") String name) {
        return "Hello, " + name + "!";
    }
}
