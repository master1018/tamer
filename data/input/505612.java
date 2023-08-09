public class RadioGroupPreCheckedTest extends ActivityInstrumentationTestCase2<RadioGroupActivity> {
    public RadioGroupPreCheckedTest() {
        super("com.android.frameworks.coretests", RadioGroupActivity.class);
    }
    @LargeTest
    public void testRadioButtonPreChecked() throws Exception {
        final RadioGroupActivity activity = getActivity();
        RadioButton radio = (RadioButton) activity.findViewById(R.id.value_one);
        assertTrue("The first radio button should be checked", radio.isChecked());
        RadioGroup group = (RadioGroup) activity.findViewById(R.id.group);
        assertEquals("The first radio button should be checked", R.id.value_one,
                group.getCheckedRadioButtonId());
    }
    @LargeTest
    public void testRadioButtonChangePreChecked() throws Exception {
        final RadioGroupActivity activity = getActivity();
        RadioButton radio = (RadioButton) activity.findViewById(R.id.value_two);
        TouchUtils.clickView(this, radio);
        RadioButton old = (RadioButton) activity.findViewById(R.id.value_one);
        assertFalse("The first radio button should not be checked", old.isChecked());
        assertTrue("The second radio button should be checked", radio.isChecked());
        RadioGroup group = (RadioGroup) activity.findViewById(R.id.group);
        assertEquals("The second radio button should be checked", R.id.value_two,
                group.getCheckedRadioButtonId());
    }
}
