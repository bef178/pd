package pd.logger;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class Test_Slf4jImpl {

    @Test
    public void test_Logger() {
        log.info("asdf");
    }

    @Test
    public void test_Logger_exception() {
        log.info("asdf-{}", 123, new RuntimeException("aaa"));
    }
}
