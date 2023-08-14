public class EqualExceptionTest {
    public static void main(String[] args) throws Exception {
        System.out.println("<<< Test whether an null descriptor will cause an NullPointerException.");
        MBeanInfo mbi1 = new MBeanInfo("MyClass","",null,null,null,null);
        MBeanInfo mbi2 = new MBeanInfo("MyClass",null,null,null,null,null);
        System.out.println("<<< mbi1.equals(mbi2) = "+mbi1.equals(mbi2));
        System.out.println("<<< Test whether an null class name will cause an NullPointerException.");
        MBeanInfo mbi3 = new MBeanInfo("MyClass","",null,null,null,null);
        MBeanInfo mbi4 = new MBeanInfo(null,null,null,null,null,null);
        System.out.println("<<< mbi3.equals(mbi4) = "+mbi3.equals(mbi4));
    }
}
