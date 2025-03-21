package pd.injector.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import pd.injector.Injector;

@SpringBootApplication
public class DemoApplication {

    public static final Injector injector = new Injector(DemoApplication.class);

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(DemoApplication.class);
        application.addListeners(new DemoApplicationListener());
        application.run(args);
    }

    @Slf4j
    static class DemoApplicationListener implements ApplicationListener<ApplicationEvent> {

        @Override
        public void onApplicationEvent(ApplicationEvent event) {
            if (event instanceof ApplicationEnvironmentPreparedEvent) {
                injector.loadValuesFromResource("application.yml");
                injector.loadValuesFromResource("application-local.yml");
                log.info("injector: properties loaded");
            } else if (event instanceof ApplicationPreparedEvent) {
                injector.scan();
                log.info("injector: scanned");
            } else if (event instanceof ApplicationStartedEvent) {
                injector.dispose();
                log.info("injector: disposed");
            }
        }
    }
}
