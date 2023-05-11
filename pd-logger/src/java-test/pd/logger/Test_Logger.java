package pd.logger;

import org.junit.jupiter.api.Test;

public class Test_Logger {

    @Test
    public void test_ILogger() {
        LoggerManager.getLogger().logInfo("asdf");
    }
}
