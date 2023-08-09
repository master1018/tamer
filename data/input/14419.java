public class Test4682386 {
    private static final String FOO = "foo";
    private static final String BAR = "bar";
    private static final int NUM_LISTENERS = 100;
    private static final boolean DEBUG = true;
    private static final Class[] TYPES = {
            JApplet.class,
            JButton.class,
            JCheckBox.class,
            JComboBox.class,
            JLabel.class,
            JList.class,
            JMenuItem.class,
            JProgressBar.class,
            JTextArea.class,
            JTextPane.class,
            JTextField.class,
            JToolBar.class,
            JTabbedPane.class,
            JTree.class,
            JTable.class,
    };
    public static void main(String[] args) {
        testSwingProperties();
        TestBean bean = new TestBean();
        Thread add = new Thread(new AddThread(bean));
        Thread remove = new Thread(new RemoveThread(bean));
        Thread prop = new Thread(new PropertyThread(bean));
        add.start();
        prop.start();
        remove.start();
    }
    private static void testSwingProperties() {
        long start = System.currentTimeMillis();
        for (Class type : TYPES) {
            try {
                Object bean = Beans.instantiate(type.getClassLoader(), type.getName());
                JComponent comp = (JComponent) bean;
                for (int k = 0; k < NUM_LISTENERS; k++) {
                    comp.addPropertyChangeListener(new PropertyListener());
                }
                for (PropertyDescriptor pd : getPropertyDescriptors(type)) {
                    if (pd.isBound()) {
                        if (DEBUG) {
                            System.out.println("Bound property found: " + pd.getName());
                        }
                        Method read = pd.getReadMethod();
                        Method write = pd.getWriteMethod();
                        try {
                            write.invoke(
                                    bean,
                                    getValue(
                                            pd.getPropertyType(),
                                            read.invoke(bean)));
                        } catch (Exception ex) {
                            if (DEBUG) {
                                System.out.println("Reflective method invocation Exception for " + type + " : " + ex.getMessage());
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                if (DEBUG) {
                    System.out.println("Exception for " + type.getName() +
                            " : " + ex.getMessage());
                }
            }
        }
        System.out.println("Exec time (ms): " + (System.currentTimeMillis() - start));
    }
    public static Object getValue(Class type, Object value) {
        if (String.class.equals(type)) {
            return "test string";
        }
        if (value instanceof Integer) {
            Integer i = (Integer) value;
            return Integer.valueOf(i + 1);
        }
        if (value instanceof Boolean) {
            Boolean b = (Boolean) value;
            return Boolean.valueOf(!b);
        }
        return null;
    }
    public static PropertyDescriptor[] getPropertyDescriptors(Class type) {
        try {
            return Introspector.getBeanInfo(type).getPropertyDescriptors();
        } catch (IntrospectionException exception) {
            throw new Error("unexpected exception", exception);
        }
    }
    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException exception) {
        }
    }
    private static class AddThread implements Runnable {
        private final TestBean bean;
        AddThread(TestBean bean) {
            this.bean = bean;
        }
        public void run() {
            for (int i = 0; i < NUM_LISTENERS; i++) {
                for (int j = 0; j < 10; j++) {
                    this.bean.addPropertyChangeListener(new PropertyListener());
                }
                if (DEBUG) {
                    System.out.println("10 listeners added");
                }
                sleep(25L);
            }
        }
    }
    private static class RemoveThread implements Runnable {
        private final TestBean bean;
        RemoveThread(TestBean bean) {
            this.bean = bean;
        }
        public void run() {
            for (int k = 0; k < NUM_LISTENERS; k++) {
                sleep(100L);
                PropertyChangeListener[] listeners = this.bean.getPropertyChangeListners();
                for (int i = listeners.length - 1; i >= 0; i--) {
                    this.bean.removePropertyChangeListener(listeners[i]);
                }
                if (DEBUG) {
                    System.out.println(listeners.length + " listeners removed");
                }
            }
        }
    }
    private static class PropertyThread implements Runnable {
        private final TestBean bean;
        PropertyThread(TestBean bean) {
            this.bean = bean;
        }
        public void run() {
            for (int i = 0; i < NUM_LISTENERS; i++) {
                boolean flag = this.bean.isFoo();
                this.bean.setFoo(!flag);
                this.bean.setBar(Boolean.toString(flag));
                if (DEBUG) {
                    System.out.println("Executed property changes");
                }
                sleep(40L);
            }
        }
    }
    private static class PropertyListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
        }
    }
    public static class TestBean {
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private boolean foo;
        private String bar;
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            this.pcs.addPropertyChangeListener(listener);
        }
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            this.pcs.removePropertyChangeListener(listener);
        }
        public PropertyChangeListener[] getPropertyChangeListners() {
            return this.pcs.getPropertyChangeListeners();
        }
        public boolean isFoo() {
            return this.foo;
        }
        public void setFoo(boolean foo) {
            boolean old = this.foo;
            this.foo = foo;
            this.pcs.firePropertyChange(FOO, old, foo);
        }
        public String getBar() {
            return this.bar;
        }
        public void setBar(String bar) {
            String old = this.bar;
            this.bar = bar;
            this.pcs.firePropertyChange(BAR, old, bar);
        }
    }
}
