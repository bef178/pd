package pd.injano.demo;

import static pd.injano.demo.DemoApplication.injector;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DemoApplicationListener implements ApplicationListener<ApplicationEvent> {

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationEnvironmentPreparedEvent) {
            injector.loadProperties();
            log.info("injano: properties loaded");
        } else if (event instanceof ApplicationPreparedEvent) {
            injector.scan();
            log.info("injano: scanned");
        } else if (event instanceof ApplicationStartedEvent) {
            injector.dispose();
            log.info("injano: disposed");
        }
    }
}
