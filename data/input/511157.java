public abstract class ManyEditTextActivityBaseTestCase<T extends Activity> extends ImfBaseTestCase<T> {
    public ManyEditTextActivityBaseTestCase(Class<T> activityClass){
        super(activityClass);
    }
    public abstract void testAllEditTextsAdjust();
    public void verifyAllEditTextAdjustment(int numEditTexts, int rootViewHeight) {
        for (int i = 0; i < numEditTexts; i++) {
            final EditText lastEditText = (EditText) mTargetActivity.findViewById(i);
            verifyEditTextAdjustment(lastEditText, rootViewHeight);
        }
    }
}
