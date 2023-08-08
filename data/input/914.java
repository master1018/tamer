public class LoggerFactoryTest extends TestCase {
    private static Logger staticLogger = LoggerFactory.getLogger();
    public void testAutomaticNameInitialization() {
        assertEquals(getClass().getName(), staticLogger.getName());
        assertEquals(getClass().getName(), LoggerFactory.getLogger().getName());
    }
}
