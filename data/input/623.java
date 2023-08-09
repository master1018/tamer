public class bug7031551 {
    private static final String TEST_ELEMENT1 = "Test1";
    private static final String TEST_ELEMENT2 = "Test2";
    private static final String TEST_ELEMENT3 = "Test3";
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        testRawSignatures();
        testGenericSignatures();
    }
    @SuppressWarnings("unchecked")
    private static void testRawSignatures() {
        ComboBoxModel rawTestModel = new DefaultComboBoxModel();
        JComboBox rawTestComboBox = new JComboBox();
        rawTestComboBox = new JComboBox(rawTestModel);
        rawTestComboBox = new JComboBox(new Object[]{TEST_ELEMENT1});
        rawTestComboBox = new JComboBox(new Vector());
        Object unused1 = rawTestComboBox.getPrototypeDisplayValue();
        rawTestComboBox.setPrototypeDisplayValue(TEST_ELEMENT1);
        ListCellRenderer unused2 = rawTestComboBox.getRenderer();
        rawTestComboBox.setRenderer(new DefaultListCellRenderer());
        ComboBoxModel unused3 = rawTestComboBox.getModel();
        rawTestComboBox.setModel(rawTestModel);
        rawTestComboBox.addItem(TEST_ELEMENT2);
        rawTestComboBox.insertItemAt(TEST_ELEMENT3, 1);
        rawTestComboBox.removeItem(TEST_ELEMENT2);
        assertEquals(rawTestComboBox.getItemAt(0), TEST_ELEMENT3);
        rawTestComboBox.removeAllItems();
        DefaultComboBoxModel testModel = new DefaultComboBoxModel();
        testModel = new DefaultComboBoxModel(new Vector());
        testModel = new DefaultComboBoxModel(new Object[]{TEST_ELEMENT1});
        testModel.addElement(TEST_ELEMENT2);
        testModel.insertElementAt(TEST_ELEMENT3, 1);
        assertEquals(testModel.getElementAt(2), TEST_ELEMENT2);
    }
    private static void testGenericSignatures() {
        ComboBoxModel<String> stringTestModel = new DefaultComboBoxModel<String>();
        JComboBox<String> stringTestComboBox = new JComboBox<String>();
        stringTestComboBox = new JComboBox<String>(stringTestModel);
        stringTestComboBox = new JComboBox<String>(new String[]{TEST_ELEMENT1});
        stringTestComboBox = new JComboBox<String>(new Vector<String>());
        String unused1 = stringTestComboBox.getPrototypeDisplayValue();
        stringTestComboBox.setPrototypeDisplayValue(TEST_ELEMENT1);
        ListCellRenderer<? super String> unused2 = stringTestComboBox.getRenderer();
        stringTestComboBox.setRenderer(new DefaultListCellRenderer());
        ComboBoxModel unused3 = stringTestComboBox.getModel();
        stringTestComboBox.setModel(stringTestModel);
        stringTestComboBox.addItem(TEST_ELEMENT2);
        stringTestComboBox.insertItemAt(TEST_ELEMENT3, 1);
        stringTestComboBox.removeItem(TEST_ELEMENT2);
        assertEquals(stringTestComboBox.getItemAt(0), TEST_ELEMENT3);
        stringTestComboBox.removeAllItems();
        DefaultComboBoxModel<String> testModel = new DefaultComboBoxModel<String>();
        testModel = new DefaultComboBoxModel<String>(new Vector<String>());
        testModel = new DefaultComboBoxModel<String>(new String[]{TEST_ELEMENT1});
        testModel.addElement(TEST_ELEMENT2);
        testModel.insertElementAt(TEST_ELEMENT3, 1);
        assertEquals(testModel.getElementAt(2), TEST_ELEMENT2);
    }
    private static void assertEquals(Object expectedObject, Object actualObject) {
        if (!expectedObject.equals(actualObject)) {
            throw new RuntimeException("Expected: " + expectedObject + " but was: " + actualObject);
        }
    }
}
