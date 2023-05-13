package pd.logger;

import org.junit.jupiter.api.Test;

public class Test_Logger {

    @Test
    public void test_Logger() {
        LoggerManager.singleton().getLogger().logInfo("asdf");
    }
}
