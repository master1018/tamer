public class Test4619536 {
    public static void main(String[] args) throws Exception {
        IndexedPropertyDescriptor ipd = BeanUtils.getIndexedPropertyDescriptor(A.class, "foo");
        if (!ipd.getIndexedPropertyType().equals(String.class)) {
            error(ipd, "A.foo should be String type");
        }
        PropertyDescriptor pd = BeanUtils.findPropertyDescriptor(B.class, "foo");
        if (pd instanceof IndexedPropertyDescriptor) {
            error(pd, "B.foo should not be an indexed property");
        }
        if (!pd.getPropertyType().equals(Date.class)) {
            error(pd, "B.foo should be Date type");
        }
        pd = BeanUtils.findPropertyDescriptor(Child.class, "foo");
        if (pd instanceof IndexedPropertyDescriptor) {
            error(pd, "Child.foo should not be an indexed property");
        }
        pd = BeanUtils.findPropertyDescriptor(Classic.class, "foo");
        if (pd instanceof IndexedPropertyDescriptor) {
            error(pd, "Classic.foo should not be an indexed property");
        }
        ipd = BeanUtils.getIndexedPropertyDescriptor(Index.class, "foo");
        if (!hasIPD(ipd)) {
            error(pd, "Index.foo should have ipd values");
        }
        if (hasPD(ipd)) {
            error(ipd, "Index.foo should not have pd values");
        }
        ipd = BeanUtils.getIndexedPropertyDescriptor(All.class, "foo");
        if (!hasPD(ipd) || !hasIPD(ipd)) {
            error(ipd, "All.foo should have all pd/ipd values");
        }
        if (!isValidType(ipd)) {
            error(ipd, "All.foo pdType should equal ipdType");
        }
        ipd = BeanUtils.getIndexedPropertyDescriptor(Getter.class, "foo");
        if (ipd.getReadMethod() == null || ipd.getWriteMethod() != null) {
            error(ipd, "Getter.foo classic methods incorrect");
        }
        if (!isValidType(ipd)) {
            error(ipd, "Getter.foo pdType should equal ipdType");
        }
        ipd = BeanUtils.getIndexedPropertyDescriptor(BadGetter.class, "foo");
        if (hasPD(ipd)) {
            error(ipd, "BadGetter.foo should not have classic methods");
        }
        ipd = BeanUtils.getIndexedPropertyDescriptor(Setter.class, "foo");
        if (ipd.getReadMethod() != null || ipd.getWriteMethod() == null) {
            error(ipd, "Setter.foo classic methods incorrect");
        }
        if (!isValidType(ipd)) {
            error(ipd, "Setter.foo pdType should equal ipdType");
        }
        ipd = BeanUtils.getIndexedPropertyDescriptor(BadSetter.class, "foo");
        if (hasPD(ipd)) {
            error(ipd, "BadSetter.foo should not have classic methods");
        }
    }
    public static boolean hasPD(PropertyDescriptor pd) {
        if (null == pd.getPropertyType()) {
            return false;
        }
        return (null != pd.getReadMethod())
            || (null != pd.getWriteMethod());
    }
    public static boolean hasIPD(IndexedPropertyDescriptor ipd) {
        if (null == ipd.getIndexedPropertyType()) {
            return false;
        }
        return (null != ipd.getIndexedReadMethod())
            || (null != ipd.getIndexedWriteMethod());
    }
    public static boolean isValidType(IndexedPropertyDescriptor ipd) {
        Class type = ipd.getPropertyType();
        return type.isArray() && type.getComponentType().equals(ipd.getIndexedPropertyType());
    }
    public static void error(PropertyDescriptor pd, String message) {
        BeanUtils.reportPropertyDescriptor(pd);
        throw new Error(message);
    }
    public static class A {
        public String getFoo(int x) {
            return null;
        }
    }
    public static class B extends A {
        public Date getFoo() {
            return null;
        }
    }
    public static class Parent {
        public void setFoo(String foo) {
        }
        public Child getFoo(int index) {
            return null;
        }
    }
    public static class Child extends Parent {
        public Child getFoo() {
            return null;
        }
    }
    public static class Classic {
        public String[] getFoo() {
            return null;
        }
        public void setFoo(String[] foo) {
        }
    }
    public static class Index {
        public String getFoo(int i) {
            return null;
        }
        public void setFoo(int i, String f) {
        }
    }
    public static class All extends Index {
        public String[] getFoo() {
            return null;
        }
        public void setFoo(String[] foo) {
        }
    }
    public static class Getter extends Index {
        public String[] getFoo() {
            return null;
        }
    }
    public static class BadGetter extends Index {
        public String getFoo() {
            return null;
        }
    }
    public static class Setter extends Index {
        public void setFoo(String[] f) {
        }
    }
    public static class BadSetter extends Index {
        public void setFoo(String f) {
        }
    }
}
