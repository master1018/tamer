public class TestPropertyEditor implements Runnable {
    private enum Enumeration {
        FIRST, SECOND, THIRD
    }
    private static final String[] SEARCH_PATH = { "editors" }; 
    public static void main(String[] args) throws InterruptedException {
        TestPropertyEditor test = new TestPropertyEditor();
        test.run();
        ThreadGroup group = new ThreadGroup("$$$"); 
        Thread thread = new Thread(group, test);
        thread.start();
        thread.join();
    }
    private static void test(Class<?> type, Class<? extends PropertyEditor> expected) {
        PropertyEditor actual = PropertyEditorManager.findEditor(type);
        if ((actual == null) && (expected != null)) {
            throw new Error("expected editor is not found");
        }
        if ((actual != null) && !actual.getClass().equals(expected)) {
            throw new Error("found unexpected editor");
        }
    }
    private boolean passed;
    public void run() {
        if (this.passed) {
            SunToolkit.createNewAppContext();
        }
        PropertyEditorManager.registerEditor(ThirdBean.class, ThirdBeanEditor.class);
        test(FirstBean.class, FirstBeanEditor.class);
        test(SecondBean.class, null);
        test(ThirdBean.class, ThirdBeanEditor.class);
        test(Byte.TYPE, ByteEditor.class);
        test(Short.TYPE, ShortEditor.class);
        test(Integer.TYPE, IntegerEditor.class);
        test(Long.TYPE, LongEditor.class);
        test(Boolean.TYPE, BooleanEditor.class);
        test(Float.TYPE, FloatEditor.class);
        test(Double.TYPE, DoubleEditor.class);
        test(Byte.class, ByteEditor.class);
        test(Short.class, ShortEditor.class);
        test(Integer.class, IntegerEditor.class);
        test(Long.class, LongEditor.class);
        test(Boolean.class, BooleanEditor.class);
        test(Float.class, FloatEditor.class);
        test(Double.class, DoubleEditor.class);
        test(String.class, StringEditor.class);
        test(Color.class, ColorEditor.class);
        test(Font.class, FontEditor.class);
        test(Enumeration.class, EnumEditor.class);
        PropertyEditorManager.registerEditor(ThirdBean.class, null);
        PropertyEditorManager.setEditorSearchPath(SEARCH_PATH);
        test(FirstBean.class, FirstBeanEditor.class);
        test(SecondBean.class, SecondBeanEditor.class);
        test(ThirdBean.class, ThirdBeanEditor.class);
        test(Byte.TYPE, ByteEditor.class);
        test(Short.TYPE, ShortEditor.class);
        test(Integer.TYPE, IntegerEditor.class);
        test(Long.TYPE, LongEditor.class);
        test(Boolean.TYPE, BooleanEditor.class);
        test(Float.TYPE, FloatEditor.class);
        test(Double.TYPE, DoubleEditor.class);
        test(Byte.class, null);
        test(Short.class, null);
        test(Integer.class, null);
        test(Long.class, null);
        test(Boolean.class, null);
        test(Float.class, null);
        test(Double.class, null);
        test(String.class, null);
        test(Color.class, null);
        test(Font.class, null);
        test(Enumeration.class, EnumEditor.class);
        this.passed = true;
    }
}
