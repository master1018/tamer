@TestTargetClass(Formattable.class)
public class FormattableTest extends TestCase {
    class Mock_Formattable implements Formattable {
        boolean flag = false;
        public void formatTo(Formatter arg0, int arg1, int arg2, int arg3) {
            StringBuilder sb = new StringBuilder();
            if (arg3 == 1) {
                sb.append("single precision ");
            }
            if (arg3 == 2) {
                sb.append("double precision ");
            }
            arg0.format(sb.toString());
            flag = true;
        }
        public boolean isFormatToCalled() {
            return flag;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "formatTo",
        args = {java.util.Formatter.class, int.class, int.class, int.class}
    )
    public void testFormatTo() {
        Formatter fmt = new Formatter();
        Mock_Formattable mf = new Mock_Formattable();
        assertTrue(fmt.format("%1.1s", mf).toString().equals("single precision "));
        assertTrue(fmt.format("%2.1s", mf).toString().equals("single precision single precision "));
        assertTrue(fmt.format("%2.2s", mf).toString().equals("single precision single precision double precision "));
        assertTrue(mf.isFormatToCalled());
    }
}
