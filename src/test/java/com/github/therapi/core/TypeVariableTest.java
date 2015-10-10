package com.github.therapi.core;

import com.github.therapi.core.annotation.Remotable;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class TypeVariableTest extends AbstractMethodRegistryTest {

    @Before
    public void setup() {
        registry.scan(new WidgetEchoService());
    }

    @Remotable("")
    @SuppressWarnings("unused")
    private interface EchoService<T> {
        T echo(T widget);

        List<T> echoAll(List<T> widgets);
    }

    private static class WidgetEchoService implements EchoService<Widget> {
        @Override
        public Widget echo(Widget widget) {
            return widget;
        }

        @Override
        public List<Widget> echoAll(List<Widget> widgets) {
            return widgets;
        }
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
