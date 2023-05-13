package pd.injano.demo.controller;

import javax.annotation.PostConstruct;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import pd.injano.annotation.FromProperty;
import pd.injano.annotation.Managed;
import pd.injano.annotation.OnConstructed;
import pd.injano.demo.DemoApplication;

@RestController
public class DemoController {

    @FromProperty("injano.name")
    private String name;

    @Managed
    private Greeting greeting;

    @PostConstruct
    public void onPostConstruct() {
        DemoApplication.injector.injectClassFields(this);
    }

    @GetMapping("/hello")
    public String hello() {
        return String.format("%s says \"%s\"", name, greeting.message());
    }
}

@Managed
class Greeting {

    @FromProperty("injano.greeting")
    private String message;

    @OnConstructed
    public void onConstructed() {
        message = message + "!";
    }

    public String message() {
        return message;
    }
}
