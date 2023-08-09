public class ButtonArraysEquality {
    static int [] eventDownMask = new int []{InputEvent.BUTTON1_DOWN_MASK, InputEvent.BUTTON2_DOWN_MASK, InputEvent.BUTTON3_DOWN_MASK};
    public static void main(String []s){
        int [] buttonDownMasksAPI = new int [MouseInfo.getNumberOfButtons()];
        for (int i = 0; i < MouseInfo.getNumberOfButtons(); i++){
            buttonDownMasksAPI[i] = InputEvent.getMaskForButton(i+1);
            System.out.println("TEST: "+buttonDownMasksAPI[i]);
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
        int [] buttonDownMasks = new int [Array.getLength(obj)];
        checkNullAndPutValuesToArray(buttonDownMasks, obj);
        if (buttonDownMasks.length < buttonDownMasksAPI.length){
            throw new RuntimeException("Test failed. The lengths array is less then the number of buttons");
        }
        for (int i = 0; i < 3; i++) {
            if (eventDownMask[i] != buttonDownMasks[i])
            {
                System.out.println("Test : "+ i + " | " + " | " +eventDownMask[i] + " | "+ buttonDownMasks[i]);
                throw new RuntimeException("Failure: masks are not correct for standard buttons");
            }
        }
        for (int i = 3; i < MouseInfo.getNumberOfButtons(); i++) {
            if (buttonDownMasksAPI[i] != buttonDownMasks[i]) {
                throw new RuntimeException("Failure: masks are not the same for extra buttons");
            }
        }
        System.out.println("Test passed.");
    }
    public static void checkNullAndPutValuesToArray(int [] array, Object obj){
        if (obj == null){
            throw new RuntimeException("Test failed. The array obtained via reflection is "+obj);
        }
        for (int i = 0; i < Array.getLength(obj); i++){
            System.out.println("Test (Reflection): "+ Array.getInt(obj, i));
            array[i] = Array.getInt(obj, i);
        }
    }
}
