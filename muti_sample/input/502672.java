public abstract class ActivityTestCase extends InstrumentationTestCase {
    private Activity mActivity;
    protected Activity getActivity() {
        return mActivity;
    }
    protected void setActivity(Activity testActivity) {
        mActivity = testActivity;
    }
    protected void scrubClass(final Class<?> testCaseClass)
    throws IllegalAccessException {
        final Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            final Class<?> fieldClass = field.getDeclaringClass();
            if (testCaseClass.isAssignableFrom(fieldClass) && !field.getType().isPrimitive()) {
                try {
                    field.setAccessible(true);
                    field.set(this, null);
                } catch (Exception e) {
                    android.util.Log.d("TestCase", "Error: Could not nullify field!");
                }
                if (field.get(this) != null) {
                    android.util.Log.d("TestCase", "Error: Could not nullify field!");
                }
            }
        }
    }
}
