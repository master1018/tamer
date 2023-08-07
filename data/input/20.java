public class GenericBeanTest<B extends GenericBean<B>> extends ObjectTest<B> {
    final ArrayList<PropertyTest<Property<B, Object>, B, Object>> propertyTests;
    @SuppressWarnings("unchecked")
    protected GenericBeanTest(B testObject) {
        super(testObject);
        propertyTests = new ArrayList<PropertyTest<Property<B, Object>, B, Object>>();
        for (Property property : getTestObject().getProperties()) {
            propertyTests.add(PropertyTest.getInstance(property));
        }
    }
    public static final <B extends GenericBean<B>> GenericBeanTest<B> getInstance(B testObject) {
        return new GenericBeanTest<B>(testObject);
    }
    public void setTestValues(List<Object> testValues) {
        for (int i = 0; i < Math.min(propertyTests.size(), testValues.size()); i++) {
            propertyTests.get(i).setTestValue(testValues.get(i));
        }
    }
    public void assertConsistency() {
        super.assertConsistency();
        for (PropertyTest test : propertyTests) {
            test.assertConsistency();
        }
    }
    public void performCycle() {
        assertConsistency();
        for (PropertyTest<Property<B, Object>, B, Object> test : propertyTests) {
            test.performCycle(getTestObject());
        }
    }
}
