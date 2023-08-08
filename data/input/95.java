@RunWith(Parameterized.class)
public class IntegerValueRefuseTest {
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { null }, { "" }, { " " }, { "+" }, { "-" }, { "2147483648" }, { "+2147483648" }, { "-2147483649" }, { "1.0" }, { "++1" }, { "--1" }, { "+-1" }, { "-+1" } });
    }
    private String string;
    public IntegerValueRefuseTest(String string) {
        this.string = string;
    }
    @Test(expected = QTIParseException.class)
    public void testParseInteger() throws QTIParseException {
        IntegerValue.parseInteger(string);
    }
}
