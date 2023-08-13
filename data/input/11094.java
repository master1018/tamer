public class Test4619792 {
    public static void main(String[] args) throws IntrospectionException {
        Class[] types = {
                Component.class,
                Container.class,
                JComponent.class,
                AbstractButton.class,
                JButton.class,
                JToggleButton.class,
        };
        String[] names = {
                "enabled",
                "name",
                "focusable",
        };
        for (String name : names) {
            for (Class type : types) {
                BeanUtils.getPropertyDescriptor(type, name);
            }
        }
    }
}
