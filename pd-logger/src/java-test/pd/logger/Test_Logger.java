package pd.logger;

import org.junit.jupiter.api.Test;

public class Test_Logger {

    @Test
    public void test_Logger() {
        LoggerManager.singleton().getLogger().info("asdf");
    }

    @Test
    public void test_Logger_exception() {
        LoggerManager.singleton().getLogger().info("asdf-{}", 123, new RuntimeException("aaa"));
    }
}
