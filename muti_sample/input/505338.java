public class TouchModeFlexibleAsserts {
    private static int MAX_ATTEMPTS = 2;
    private static int MAX_DELAY_MILLIS = 2000;
    public static void assertInTouchModeAfterClick(InstrumentationTestCase test, View viewToTouch) {
        int numAttemptsAtTouchMode = 0;
        while (numAttemptsAtTouchMode < MAX_ATTEMPTS &&
                !viewToTouch.isInTouchMode()) {
            TouchUtils.clickView(test, viewToTouch);
            numAttemptsAtTouchMode++;
        }
        Assert.assertTrue("even after " + MAX_ATTEMPTS + " clicks, did not enter "
                + "touch mode", viewToTouch.isInTouchMode());
    }
    public static void assertInTouchModeAfterTap(InstrumentationTestCase test, View viewToTouch) {
        int numAttemptsAtTouchMode = 0;
        while (numAttemptsAtTouchMode < MAX_ATTEMPTS &&
                !viewToTouch.isInTouchMode()) {
            TouchUtils.tapView(test, viewToTouch);
            numAttemptsAtTouchMode++;
        }
        Assert.assertTrue("even after " + MAX_ATTEMPTS + " taps, did not enter "
                + "touch mode", viewToTouch.isInTouchMode());
    }
    public static void assertNotInTouchModeAfterKey(InstrumentationTestCase test, int keyCode, View checkForTouchMode) {
        test.sendKeys(keyCode);
        int amountLeft = MAX_DELAY_MILLIS;
        while (checkForTouchMode.isInTouchMode() && amountLeft > 0) {
            amountLeft -= 200;
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Assert.assertFalse("even after waiting " + MAX_DELAY_MILLIS + " millis after " 
                + "pressing key event, still in touch mode", checkForTouchMode.isInTouchMode());
    }
}
