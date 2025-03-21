package pd.injector.demo.controller;

import javax.annotation.PostConstruct;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import pd.injector.annotation.FromProperty;
import pd.injector.annotation.Managed;
import pd.injector.annotation.OnConstructed;
import pd.injector.demo.DemoApplication;

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

    @Managed
    static class Greeting {

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
}
