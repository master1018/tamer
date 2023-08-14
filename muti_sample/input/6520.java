public class CheckGetMaskForButton{
    static Robot robot;
    public static void main(String []s){
        System.out.println("Number Of Buttons = "+ MouseInfo.getNumberOfButtons());
        CheckGetMaskForButton f = new CheckGetMaskForButton();
        int [] buttonMasksViaAPI = new int[MouseInfo.getNumberOfButtons()];
        for (int i = 0; i < MouseInfo.getNumberOfButtons(); i++){
            buttonMasksViaAPI[i] = InputEvent.getMaskForButton(i+1);
            System.out.println("Test (API): "+ buttonMasksViaAPI[i]);
        }
        Object obj = AccessController.doPrivileged(
                new PrivilegedAction() {
            public Object run() {
                try {
                    Class clazz = Class.forName("java.awt.event.InputEvent");
                    Method method  = clazz.getDeclaredMethod("getButtonDownMasks",new Class [] {});
                    if (method != null) {
                        method.setAccessible(true);
                        return method.invoke(null, (Object[])null);
                    }
                }catch (Exception e){
                    throw new RuntimeException("Test failed. Exception occured:", e);
                }
                return null;
            }
        });
        if (obj == null){
            throw new RuntimeException("Test failed. The value obtained via reflection is "+obj);
        }
        int [] buttonDownMasksViaReflection = new int [Array.getLength(obj)];
        if (Array.getLength(obj) < buttonMasksViaAPI.length){
            throw new RuntimeException("Test failed. The length of API array greater or equals then the length of  Reflect array.");
        }
        for (int i = 0; i < MouseInfo.getNumberOfButtons(); i++){
            System.out.println("Test (Reflection): "+ Array.getInt(obj, i));
            if (buttonMasksViaAPI[i] != Array.getInt(obj, i)){
                throw new RuntimeException("Test failed. Values of InputEvent array are different for API and Reflection invocations");
            }
        }
        System.out.println("Test passed.");
    }
}
