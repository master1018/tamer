public class Test6194788 {
    public static void main(String[] args) throws IntrospectionException {
        test(Grand.class, new PropertyDescriptor("index", Grand.class));
        test(Grand.class, new IndexedPropertyDescriptor("name", Grand.class, null, null, "getName", "setName"));
        test(Parent.class, new PropertyDescriptor("parentIndex", Parent.class));
        test(Parent.class, new IndexedPropertyDescriptor("parentName", Parent.class));
        test(Child.class, new PropertyDescriptor("childIndex", Child.class));
        test(Child.class, new IndexedPropertyDescriptor("childName", Child.class));
    }
    private static void test(Class type, PropertyDescriptor property) {
        for (PropertyDescriptor pd : BeanUtils.getPropertyDescriptors(type)) {
            boolean forward = pd.equals(property);
            boolean backward = property.equals(pd);
            if (forward || backward) {
                if (forward && backward)
                    return;
                throw new Error("illegal comparison of properties");
            }
        }
        throw new Error("could not find property: " + property.getName());
    }
    public static class Grand {
        public int getIndex() {
            return 0;
        }
        public void setIndex(int index) {
        }
        public String getName(int index) {
            return null;
        }
        public void setName(int index, String name) {
        }
    }
    public static class Parent {
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
        public int getParentIndex() {
            return 0;
        }
        public void setParentIndex(int index) {
        }
        public String[] getParentName() {
            return null;
        }
        public String getParentName(int index) {
            return null;
        }
        public void setParentName(String[] names) {
        }
        public void setParentName(int index, String name) {
        }
    }
    public static class Child {
        public int getChildIndex() {
            return 0;
        }
        public void setChildIndex(int index) {
        }
        public String[] getChildName() {
            return null;
        }
        public String getChildName(int index) {
            return null;
        }
        public void setChildName(String[] names) {
        }
        public void setChildName(int index, String name) {
        }
    }
}
