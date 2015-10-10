package com.github.therapi.core;

import com.github.therapi.core.annotation.Remotable;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class PojoArgsTest extends AbstractMethodRegistryTest {

    @Before
    public void setup() {
        registry.scan(newEchoProxyInstance(PojoEchoService.class));
    }

    @Remotable("")
    @SuppressWarnings("unused")
    private interface PojoEchoService {
        Widget echo(Widget widget);

        List<Widget> echoAll(List<Widget> widgets);
    }

    private static class Widget {
        private final long id;
        private final String name;

        public Widget(long id, String name) {
            this.id = id;
            this.name = name;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    @Test
    public void echo() throws Exception {
        check("echo", "{widget:{id:1,name:'frobulator'}}", "{id:1,name:'frobulator'}");
    }

    @Test
    public void echoAll() throws Exception {
        check("echoAll", "{widgets:[{id:1,name:'frobulator'},{id:2,name:'zombifier'}]}",
                "[{id:1,name:'frobulator'},{id:2,name:'zombifier'}]");
    }
}
