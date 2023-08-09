@TestTargetClass(Character.class)
public class CharacterImplTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueOf",
        args = {char.class}
    )
    public void test_valueOfC() {
        for (char c = '\u0000'; c < 128; c++) {
            Character e = new Character(c);
            Character a = Character.valueOf(c);
            assertEquals(e, a);
            assertSame(Character.valueOf(c), Character.valueOf(c));
        }
        for (int c = '\u0512'; c <= Character.MAX_VALUE; c++) {
            assertEquals(new Character((char) c), Character.valueOf((char) c));
        }
    }
}
