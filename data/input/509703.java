@TestTargetClass(InflateException.class)
public class InflateExceptionTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test InflateException",
            method = "InflateException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test InflateException",
            method = "InflateException",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test InflateException",
            method = "InflateException",
            args = {java.lang.String.class, java.lang.Throwable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test InflateException",
            method = "InflateException",
            args = {java.lang.Throwable.class}
        )
    })
   public void testInflateException(){
       InflateException ne = null;
       boolean isThrowed = false;
       try {
           ne = new InflateException();
           throw ne;
       } catch (InflateException e) {
           assertSame(ne, e);
           isThrowed = true;
       } finally {
           if (!isThrowed) {
               fail("should throw out InflateException");
           }
       }
       String detailMessage = "testInflateException";
       Throwable throwable = new Exception();
       isThrowed = false;
       try {
           ne = new InflateException(detailMessage, throwable);
           throw ne;
       } catch (InflateException e) {
           assertSame(ne, e);
           isThrowed = true;
       } finally {
           if (!isThrowed) {
               fail("should throw out InflateException");
           }
       }
       isThrowed = false;
       try {
           ne = new InflateException(detailMessage);
           throw ne;
       } catch (InflateException e) {
           assertSame(ne, e);
           isThrowed = true;
       } finally {
           if (!isThrowed) {
               fail("should throw out InflateException");
           }
       }
       isThrowed = false;
       try {
           ne = new InflateException(throwable);
           throw ne;
       } catch (InflateException e) {
           assertSame(ne, e);
           isThrowed = true;
       } finally {
           if (!isThrowed) {
               fail("should throw out InflateException");
           }
       }
   }
}
