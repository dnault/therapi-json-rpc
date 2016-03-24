package com.github.therapi.example.boot;

import com.github.therapi.core.annotation.Default;
import com.github.therapi.core.annotation.Remotable;

@Remotable("greeting")
public interface GreetingService {
    /**
     * Generate a friendly greeting message.
     *
     * @param name The name of the person to greet
     * @return A friendly greeting message
     */
    String greet(@Default("stranger") String name);
}
