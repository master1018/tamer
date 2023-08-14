public class PublicTestClass implements TestInterface, Serializable, Cloneable {
    private static final long serialVersionUID = 1L;
    public static String TEST_FIELD = "test field"; 
    Object clazz;
    public PublicTestClass() {
        class LocalClass { }
        clazz = new LocalClass();
    }
    public Object getLocalClass() {
        class LocalClass {}
        Object returnedObject = new LocalClass();
        return returnedObject;
    }
    int count = 0; 
    public int getCount() {
        return count;
    }
    public void setCount(int value) {
        count = value;        
    }
    private class PrivateClass1 {
        public String toString() {
            return "PrivateClass0";
        }
    }
    public class PrivateClass2 {
        public String toString() {
            return "PrivateClass1";
        }
    }    
}
