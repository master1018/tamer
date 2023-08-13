@TestTargetClass(AvoidXfermode.Mode.class)
public class AvoidXfermode_ModeTest extends AndroidTestCase{
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOf(){
        assertEquals(Mode.AVOID, Mode.valueOf("AVOID"));
        assertEquals(Mode.TARGET, Mode.valueOf("TARGET"));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "values",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "AvoidXfermode",
            args = {int.class, int.class, android.graphics.AvoidXfermode.Mode.class}
        )
    })
    public void testValues(){
        Mode[] mode = Mode.values();
        assertEquals(2, mode.length);
        assertEquals(Mode.AVOID, mode[0]);
        assertEquals(Mode.TARGET, mode[1]);
        assertNotNull(new AvoidXfermode(10, 24, Mode.AVOID));
        assertNotNull(new AvoidXfermode(10, 24, Mode.TARGET));
    }
}
