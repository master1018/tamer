public class Test4274639 {
    private static String STRING_PROPERTY = "string";
    private static String INTEGER_PROPERTY = "integer";
    private static String STRING_VALUE = "Test Text";
    private static Integer INTEGER_VALUE = 261074;
    public static void main(String[] args) {
        TestBean bean = new TestBean(STRING_VALUE);
        if (!STRING_VALUE.equals(bean.getString()))
            throw new Error("unexpected string property: " + bean.getString());
        boolean string = false;
        boolean integer = false;
        for (PropertyDescriptor pd : BeanUtils.getPropertyDescriptors(bean.getClass())) {
            String name = pd.getName();
            System.out.println(" - " + name);
            if (name.equals(STRING_PROPERTY)) {
                Class type = pd.getPropertyEditorClass();
                if (!StringEditor.class.equals(type))
                    throw new Error("unexpected property editor type: " + type);
                PropertyEditor editor = pd.createPropertyEditor(bean);
                if (editor == null)
                    throw new Error("property editor cannot be created");
                if (STRING_VALUE != editor.getValue())
                    throw new Error("unexpected value: " + editor.getValue());
                Object source = ((PropertyEditorSupport) editor).getSource();
                if (source != bean)
                    throw new Error("unexpected source: " + source);
                string = true;
            }
            if (name.equals(INTEGER_PROPERTY)) {
                Class type = pd.getPropertyEditorClass();
                if (!IntegerEditor.class.equals(type))
                    throw new Error("unexpected property editor type: " + type);
                PropertyEditor editor = pd.createPropertyEditor(bean);
                if (editor == null)
                    throw new Error("property editor cannot be created");
                if (INTEGER_VALUE != editor.getValue())
                    throw new Error("unexpected value: " + editor.getValue());
                Object source = ((PropertyEditorSupport) editor).getSource();
                if (source != editor)
                    throw new Error("unexpected source: " + source);
                integer = true;
            }
        }
        if (!string)
            throw new Error("string property is not tested");
        if (!integer)
            throw new Error("integer property is not tested");
    }
    public static final class TestBean {
        private String string;
        private int integer;
        public TestBean() {
            this.string = "default";
            this.integer = 0;
        }
        public TestBean(String string) {
            setString(string);
        }
        public String getString() {
            return this.string;
        }
        public void setString(String string) {
            this.string = string;
        }
        public int getInteger() {
            return this.integer;
        }
        public void setInteger(int integer) {
            this.integer = integer;
        }
    }
    public static final class TestBeanBeanInfo extends SimpleBeanInfo {
        public PropertyDescriptor[] getPropertyDescriptors() {
            PropertyDescriptor[] pds = new PropertyDescriptor[2];
            try {
                pds[0] = new PropertyDescriptor(STRING_PROPERTY, TestBean.class);
                pds[0].setPropertyEditorClass(StringEditor.class);
                pds[1] = new PropertyDescriptor(INTEGER_PROPERTY, TestBean.class);
                pds[1].setPropertyEditorClass(IntegerEditor.class);
            }
            catch (IntrospectionException exception) {
                throw new Error("unexpected error", exception);
            }
            return pds;
        }
    }
    public static final class StringEditor extends PropertyEditorSupport {
        public StringEditor(Object source) {
            super(source);
            if (source instanceof TestBean) {
                TestBean test = (TestBean) source;
                setValue(test.getString());
            }
        }
    }
    public static final class IntegerEditor extends PropertyEditorSupport {
        public Object getValue() {
            return INTEGER_VALUE; 
        }
    }
}
