@TestTargetClass(ActionException.class)
public class RemoteViews_ActionExceptionTest extends InstrumentationTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "RemoteViews.ActionException",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "RemoteViews.ActionException",
            args = {java.lang.Exception.class}
        )
    })
    public void testConstructor() {
        String message = "This is exception message";
        new RemoteViews.ActionException(message);
        new RemoteViews.ActionException(new Exception());
    }
}
