public class Test6707234 {
    public static void main(String[] args) {
        if (null == BeanUtils.getPropertyDescriptor(C.class, "number").getWriteMethod()) {
            throw new Error("no write method");
        }
    }
    public interface I {
        void setNumber(Object number);
        Number getNumber();
    }
    public class C implements I {
        public void setNumber(Object value) {
        }
        public void setNumber(Long value) {
        }
        public Long getNumber() {
            return null;
        }
    }
}
