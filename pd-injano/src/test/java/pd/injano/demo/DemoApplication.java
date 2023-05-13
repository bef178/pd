package pd.injano.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import pd.injano.Injector;

@SpringBootApplication
public class DemoApplication {

    public static final Injector injector = new Injector(DemoApplication.class);

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(DemoApplication.class);
        application.addListeners(new DemoApplicationListener());
        application.run(args);
    }
}
