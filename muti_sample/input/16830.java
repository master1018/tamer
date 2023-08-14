public class Test4653179 {
    public static void main(String[] args) throws Exception {
        String [] array = {"first string", "second one"};
        Object valueInt = testInt(array, "get", 0);
        if (!valueInt.equals(array[0]))
            throw new Error("unexpected value: " + valueInt);
        Object valueNew = testNew(array, "get", 1);
        if (!valueNew.equals(array[1]))
            throw new Error("unexpected value: " + valueNew);
        valueInt = testInt(Class.class, "forName", "javax.swing.JButton");
        if (!valueInt.equals(JButton.class))
            throw new Error("unexpected value: " + valueInt);
        valueNew = testNew(Class.class, "forName", "javax.swing.JButton");
        if (!valueNew.equals(JButton.class))
            throw new Error("unexpected value: " + valueNew);
        testInt(JButton.class, "new");
        testNew(JButton.class, "new");
    }
    private static Object testInt(Object target, String name, Object... args) throws Exception {
        return new Expression(target, name, args).getValue();
    }
    private static Object testNew(Object target, String name, Object... args) throws Exception {
        return testInt(target, new String(name), args); 
    }
}
