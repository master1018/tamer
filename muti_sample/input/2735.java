final class TestEditor {
    private final PropertyEditor editor;
    TestEditor(Class type) {
        System.out.println("Property class: " + type);
        this.editor = PropertyEditorManager.findEditor(type);
        if (this.editor == null)
            throw new Error("could not find editor for " + type);
        System.out.println("PropertyEditor class: " + this.editor.getClass());
        validate(null, null);
    }
    void testJava(Object value) {
        this.editor.setValue(value);
        Object object = execute("Executor", "execute", this.editor.getJavaInitializationString());
        System.out.println("Property value before: " + value);
        System.out.println("Property value after: " + object);
        if (!areEqual(value, object))
            throw new Error("values are not equal");
    }
    void testValue(Object value, String text) {
        this.editor.setValue(value);
        validate(value, text);
    }
    void testText(String text, Object value) {
        this.editor.setAsText(text);
        validate(value, text);
    }
    private void validate(Object value, String text) {
        if (!areEqual(value, this.editor.getValue()))
            throw new Error("value should be " + value);
        if (!areEqual(text, this.editor.getAsText()))
            throw new Error("text should be " + text);
    }
    private static boolean areEqual(Object object1, Object object2) {
        return (object1 == null)
                ? object2 == null
                : object1.equals(object2);
    }
    private static Object execute(String classname, String methodname, String value) {
        String content
                = "public class " + classname + " {"
                + "    public static Object " + methodname + "() throws Exception {"
                + "        return " + value + ";"
                + "    }"
                + "}";
        try {
            MemoryClassLoader loader = new MemoryClassLoader();
            Class type = loader.compile(classname, content);
            return type.getMethod(methodname).invoke(null);
        }
        catch (Exception exception) {
            throw new Error(exception);
        }
    }
}
