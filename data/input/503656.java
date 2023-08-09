@TestTargetClass(targets.Charsets._Abstract.class)
public class Charset_SingleByte_ extends Charset_SingleByteAbstractTest {
    protected void setUp() throws Exception {
        charsetName = "";
        allChars = theseChars(new int[]{
            });
        super.setUp();
    }
}
