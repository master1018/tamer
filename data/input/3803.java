public class Test6447751 {
    public static void main(String[] args) {
        test(Manual.class, AutomaticCustomizer.class);
        test(Illegal.class, null);
        test(Automatic.class, AutomaticCustomizer.class);
    }
    private static void test(Class<?> type, Class<?> expected) {
        Class<?> actual;
        try {
            actual = Introspector.getBeanInfo(type).getBeanDescriptor().getCustomizerClass();
        }
        catch (IntrospectionException exception) {
            throw new Error("unexpected error", exception);
        }
        if (actual != expected) {
            StringBuilder sb = new StringBuilder();
            sb.append("bean ").append(type).append(": ");
            if (expected != null) {
                sb.append("expected ").append(expected);
                if (actual != null) {
                    sb.append(", but ");
                }
            }
            if (actual != null) {
                sb.append("found ").append(actual);
            }
            throw new Error(sb.toString());
        }
    }
    public static class Automatic {
    }
    public static class AutomaticCustomizer extends Component implements Customizer {
        public void setObject(Object bean) {
            throw new UnsupportedOperationException();
        }
    }
    public static class Illegal {
    }
    public static class IllegalCustomizer implements Customizer {
        public void setObject(Object bean) {
            throw new UnsupportedOperationException();
        }
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            throw new UnsupportedOperationException();
        }
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            throw new UnsupportedOperationException();
        }
    }
    public static class Manual {
    }
    public static class ManualBeanInfo extends SimpleBeanInfo {
        public BeanDescriptor getBeanDescriptor() {
            return new BeanDescriptor(Manual.class, AutomaticCustomizer.class);
        }
    }
}
