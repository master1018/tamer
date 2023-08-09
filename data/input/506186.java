@TestTargetClass(targets.Charsets._Abstract.class)
public class Charset_MultiByte_ extends Charset_AbstractTest {
    @Override
    protected void setUp() throws Exception {
        charsetName = "";
        testChars = theseChars(new int[]{
            });
        testBytes = theseBytes(new int[]{
            });
        super.setUp();
    }
}
