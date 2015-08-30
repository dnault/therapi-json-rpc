package com.github.dnault.therapi.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.dnault.therapi.core.annotation.Remotable;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class PolymorphicGenericsTest extends AbstractMethodRegistryTest {

    @Before
    public void setup() {
        registry.scan(new PetServiceImpl());
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
    @JsonSubTypes({
            @Type(value = Dog.class, name = "Dog"),
            @Type(value = Cat.class, name = "Cat")})
    private static abstract class Pet {
        private final String name;

        public Pet(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public abstract String speak();
    }

    private static class Dog extends Pet {
        @JsonCreator
        public Dog(String name) {
            super(name);
        }

        @Override
        public String speak() {
            return "arf";
        }
    }

    private static class Cat extends Pet {
        @JsonCreator
        public Cat(String name) {
            super(name);
        }

        @Override
        public String speak() {
            return "meow";
        }
    }

    @Remotable("pet")
    @SuppressWarnings("unused")
    private interface PetService<T> {
        List<T> list();

        Map<String, T> indexByName();

        Pet load(String name);

        String speak(T pet);

        List<String> speakAll(List<T> pet);
    }

    private static class PetServiceImpl implements PetService<Pet> {
        @Override
        public List<Pet> list() {
            return Arrays.asList(new Dog("Hachiko"), new Cat("Macak"));
        }

        @Override
        public Map<String, Pet> indexByName() {
            return list().stream().collect(toMap(Pet::getName, p -> p));
        }

        @Override
        public Pet load(String name) {
            for (Pet p : list()) {
                if (p.getName().equalsIgnoreCase(name)) {
                    return p;
                }
            }
            return null;
        }

        @Override
        public String speak(Pet pet) {
            return pet.speak();
        }

        @Override
        public List<String> speakAll(List<Pet> pet) {
            return pet.stream().map(Pet::speak).collect(toList());
        }
    }

    @Test
    public void list() throws Exception {
        check("pet.list", "[]", "[{'@type':'Dog','name':'Hachiko'},{'@type':'Cat','name':'Macak'}]");
    }

    @Test
    public void indexByName() throws Exception {
        check("pet.indexByName", "[]", "{Hachiko: {'@type':'Dog','name':'Hachiko'}, Macak: {'@type':'Cat','name':'Macak'}}");
    }

    @Test
    public void load() throws Exception {
        check("pet.load", "['Hachiko']", "{'@type':'Dog','name':'Hachiko'}");
    }

    @Test
    public void speak() throws Exception {
        check("pet.speak", "[{'@type':'Cat','name':'Macak'}]", "'meow'");
    }

    @Test
    public void speakAll() throws Exception {
        check("pet.speakAll", "[[{'@type':'Dog','name':'Hachiko'},{'@type':'Cat','name':'Macak'}]]", "['arf','meow']");
    }
}
