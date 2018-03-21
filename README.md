# Build Self-Describing JSON-RPC 2.0 APIs with Therapi

[![Build Status](https://travis-ci.org/dnault/therapi-json-rpc.svg?branch=master)](https://travis-ci.org/dnault/therapi-json-rpc)
[![Apache 2.0](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
![Java 1.8+](https://img.shields.io/badge/java-1.8+-lightgray.svg)

## Why should I consider Therapi?

Maintaining redundant API documentation is putting you on edge.
Arguing about which HTTP verb to use for partial updates is tearing your family apart.
When you were at school, Swagger bullied you into doing things you'd rather not talk about.

Relax. Everything is going to be okay. We'll work through this together.


## Maven Coordinates

```xml
<dependency>
  <groupId>com.github.therapi</groupId>
  <artifactId>therapi-json-rpc</artifactId>
  <version>0.4.0</version>
</dependency>
```

## No, really, what is this?

Therapi is a Java microframework for exposing backend services
over [JSON-RPC 2.0](http://www.jsonrpc.org/specification). It automatically
builds interactive API documentation from Javadoc on your services classes.

Right now it supports method invocations over HTTP. If you're willing
to get your hands dirty, it could be adapted to work with WebSockets as well.

Therapi plays well with Spring, and works fine on its own too. 


## The 3 minute cure

If you're in a hurry and don't want to deploy the example webapp locally,
speed over to the
[live example webapp](https://therapi-json-rpc-demo.appspot.com) hosted on Google App Engine.

While you're there, make sure to explore the
[interactive API documentation](https://therapi-json-rpc-demo.appspot.com/jsonrpc/apidoc).
Everything you see there was generated automatically
from Javadoc in the source code. Each method has a "Try It!" button
(clicking is recommended, and therapeutic).

If you'd like to tinker, start by cloning this repository and deploying the examples locally:

    ./gradlew appRunWar

Then visit [http://localhost:8080/examples](http://localhost:8080/examples). 

    
There's also a Spring Boot flavor which you can run with `./gradlew bootRun`
and visit at a slightly different URL: [http://localhost:8080](http://localhost:8080)


## Welcome to Therapi

Have a seat on the couch over there. Put your feet up if it makes you feel comfortable. 
If you're ready, let's begin.


```java
public class GreetingService {    
    /**
     * Starts a conversation.
     *
     * @param name The name of the person to greet
     * @return A friendly greeting message
     */
    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}
```

As you can see, this is a complex service typical of many Fortune 500 Enterprise deployments.
Let's make some money by sharing this service with the world using
Therapi's `@Remotable` annotation.

```java
import com.github.therapi.core.annotation.*;
 
@Remotable("greeting") // [1]
public class GreetingService {    
    /**
     * Starts a conversation.
     *
     * @param name The name of the person to greet
     * @return A friendly greeting message
     */
    @Remotable // [2]
    public String greet(@Default("stranger") String name) { // [3]
        return "Hello, " + name + "!";
    }
}
```

1. When applied to a class, `@Remotable` means the class should be scanned for
remotable methods. It also assigns a namespace for those methods.
2. When applied to a method, `@Remotable` indicates the method should be
included in the exposed API.
3. Therapi's `@Default` annotation makes it okay if non-confrontational clients
don't want to put up an argument.

Only `@Remotable` methods will be included in your API, and only `@Remotable` classes
will be scanned for remotable methods. 

> When an interface is `@Remotable`, all of the methods defined by the interface
> are implicitly `@Remotable`, and so are any classes that implement the interface. 

If you're using Spring, annotate the class with `@Service` and place it in a package
with component scanning enabled (`com.github.therapi.example.boot` for example).
Restart the webapp, and your new method
should appear in the API documentation, ready to be invoked.

Without Spring, you'll need to manually instantiate the service and register it with Therapi.
In `ExampleJsonRpcServlet`, create an instance of your service class and pass
it to the `scan` method of the `MethodRegistry`. Restart the webapp and play.


## Compilation complications

Therapi works best when method parameter names are available at runtime.
Make sure to specify the `-parameters` option when running the java compiler,
otherwise your parameters will be named `arg0`, `arg1`, and so on.

In order for Javadoc to be available at runtime, annotation processing must be enabled
during compilation, with the `therapi-runtime-javadoc-scribe` annotation processor in the
compiler's processor path.

Both of these details are handled by the example build scripts, but when running
from an IDE you may need to tweak your settings for best results. 
 
For IntelliJ IDEA, go to `Preferences > Build, Execution, Deployment > Compiler`.
Under `Java Compiler`, set "Additional command line parameters" to `-parameters`.
Under `Annotation Processors`, select "Enable annotation processing" and 
"Obtain processors from project class path".

> For painless development and debugging, try running the
> `com.github.therapi.example.boot.Application` class from the
> `spring-boot-example` sub-project in your IDE.


## You know what the music means...

There's a bit more documentation [over here](http://dnault.github.io/therapi-json-rpc/).

Some other features to explore on your own:

* The Boot webapp in `spring-boot-example` and the vanilla webapp in `examples` have different
example services. Try running them both. But not at the same time, unless you like port conflicts.
* Use complex model classes as method arguments (anything Jackson can serialize/deserialize is supported). 
* Wrap method invocations with AOP interceptors (see `MethodRegistry.intercept`).
* Generate a Java client for your API from a `@Remotable` interface (see `ServiceFactory`). 
* Customize how Therapi converts exceptions into JSON-RPC errors (see `ExceptionTranslator`).
* Add example models to the documentation (see `@ExampleModel`).
* Use the `@Default` annotation to provide a default value for any argument type that can be bound to JSON.
* Annotate parameters as `@Nullable` to allow null values.
* Annotate methods with `@DoNotLog` to suppress logging of sensitive requests/responses.
* Customize the Jackson ObjectMapper (See `MethodRegistry` constructors).
* When applying the `@Remotable` annotation to a method, specify a value to give the method a different name in the API.
* Omit the value when applying `@Remotable` to a class or service to use a default name.

## Credits

Therapi includes software developed by the following third parties.

* [json-forms](https://github.com/brutusin/json-forms) (Apache License 2.0) Ignacio del Valle Alles and contributors 
* [highlight](https://highlightjs.org) (BSD License) Copyright (c) 2006, Ivan Sagalaev
* [jquery.jsonrpc](https://github.com/datagraph/jquery-jsonrpc) (Public Domain) Josh Huckabee, Chris Petersen, Jerry Ablan 
* [es6-promise.js](https://github.com/stefanpenner/es6-promise) (MIT License) Copyright (c) 2014 Yehuda Katz, Tom Dale, Stefan Penner and contributors
* [Elegant Accordion Menu](http://cssmenumaker.com/menu/elegant-accordion-menu) by Russell Taylor
