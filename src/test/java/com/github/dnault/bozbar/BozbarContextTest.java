package com.github.dnault.bozbar;

import com.github.dnault.bozbar.annotation.Default;
import com.github.dnault.bozbar.annotation.Remotable;
import org.junit.Test;

public class BozbarContextTest {

    @Remotable("foo")
    public interface FooService {
        String greet(String name);
    }

    public static class FooServiceImpl implements FooService {
        @Override
        public String greet(@Default("stranger") String name) {
            return "Hello " + name;
        }
    }

    @Test
    public void foo() {
        BozbarContext context = new BozbarContext();
        context.scan(new FooServiceImpl());
    }


}
