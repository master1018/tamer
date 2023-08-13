public class ModifierPermutation {
    static boolean failed = false;
    final static int BUTTONSNUMBER = MouseInfo.getNumberOfButtons();
    static SunToolkit st = (SunToolkit)(Toolkit.getDefaultToolkit());
    static int [] mouseButtons = new int [BUTTONSNUMBER]; 
    static int [] mouseButtonsDown = new int [BUTTONSNUMBER]; 
    static int [] affectedButtonsToPressRelease;
    static Robot robot;
    static CheckingAdapter adapterTest1;
    static Frame f;
    static {
        for (int i = 0; i < BUTTONSNUMBER; i++){
            mouseButtons[i] = InputEvent.getMaskForButton(i+1); 
            mouseButtonsDown[i] = InputEvent.getMaskForButton(i+1);
        }
    }
    public static void main(String s[]){
        init();
        try {
            robot = new Robot();
        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Test failed.", e);
        }
        robot.delay(500);
        robot.mouseMove(f.getLocationOnScreen().x + f.getWidth()/2, f.getLocationOnScreen().y + f.getHeight()/2);
        robot.delay(500);
        for (int k = 0; k < factorial(mouseButtons.length)-1; k++){
            for (int buttonsToPressNumber = 2; buttonsToPressNumber <= BUTTONSNUMBER; buttonsToPressNumber++ ){
                System.out.println(">>>");
                affectedButtonsToPressRelease = Arrays.copyOf(mouseButtons, buttonsToPressNumber);
                dumpArray("Affected Buttons ", affectedButtonsToPressRelease);
                pressAllButtons(affectedButtonsToPressRelease);
                releaseAllButtonsForwardOrder(affectedButtonsToPressRelease);
                System.out.println("<<<");
            }
            nextPermutation(k, mouseButtons);
            dumpArray("mouseButtons (step="+k+")", mouseButtons);
        }
    }
    private static void init(){
        adapterTest1 = new CheckingAdapter();
        f = new Frame("Robot presses mouse here");
        f.setSize(300, 300);
        f.setVisible(true);
        f.addMouseListener(adapterTest1);
    }
    public static int factorial(int t){
        if (t <=1 ) {
            return 1;
        } else {
            return t*factorial(t-1);
        }
    }
    static volatile int currentButtonIndexUnderAction;
    public static void pressAllButtons(int []array){
        for (int i = 0; i <array.length; i ++){
            if (failed) {
                throw new RuntimeException("PRESSED_EVENT is not filled with correct values. Review messaage above.");
            }
            System.out.println("Pressing button = " + array[i]);
            currentButtonIndexUnderAction = i;
            robot.mousePress(array[i]);
            System.out.println("currentButtonIndexUnderAction ="+currentButtonIndexUnderAction);
            st.realSync();
        }
    }
    public static void releaseAllButtonsForwardOrder(int []array){
        for (int i = 0; i <array.length; i ++){
            System.out.println("Releasing button = " + array[i]);
            currentButtonIndexUnderAction = i;
            robot.mouseRelease(array[i]);
            System.out.println("currentButtonIndexUnderAction ="+currentButtonIndexUnderAction);
            st.realSync();
        }
    }
    public static void checkModifiersOnPress(MouseEvent e){
        System.out.println("checkModifiers. currentButtonIndexUnderAction ="+currentButtonIndexUnderAction);
        for (int i = 0; i<= currentButtonIndexUnderAction; i++){
            if ((e.getModifiersEx() & affectedButtonsToPressRelease[i]) == 0){
                System.out.println("ERROR["+i+"]: PRESSED_EVENT is not filled with correct values. affectedButtonsToPressRelease[i]= "+affectedButtonsToPressRelease[i] +" Event = "+e);
                ModifierPermutation.failed = true;
            } else {
                System.out.println("CORRECT["+i+"]: affectedButtonsToPressRelease[i]= "+affectedButtonsToPressRelease[i]+ " Event = "+e);
            }
        }
    }
    public static void dumpValues(int button, int modifiers, int modifiersStandard, int modifiersEx, int modifiersExStandard){
        System.out.println("Button = "+button + "Modifiers = "+ modifiers + " standard = "+ modifiersStandard);
        System.out.println("                   ModifiersEx = "+ modifiersEx + " standardEx = "+ modifiersExStandard);
    }
    public static void dumpArray(String id, int [] array){
        System.out.print(id);
        for (int i = 0; i < array.length; i++){
            System.out.print(array[i]+" ");
        }
        System.out.println();
    }
    public static void nextPermutation(int step, int []array){
        int i;
        int leftEl = 0;
        int rightEl = 0;
        i = array.length - 2;
        while (i>=0) {
            if (array[i] < array[i+1]){
                leftEl = i;
                break;
            }
            i--;
        }
        i = array.length - 1;
        while (i>=0) {
            if (array[i] > array[leftEl]) {
                rightEl = i;
                break;
            }
            i--;
        }
        swapElements(array, leftEl, rightEl);
        if (leftEl + 2 <  array.length){
            Arrays.sort(array, leftEl + 1 , array.length);
        }
    }
    public static void swapElements(int [] array, int leftEl, int rightEl){
        int tmp = array[leftEl];
        array[leftEl] = array[rightEl];
        array[rightEl] = tmp;
    }
    public static void checkModifiersOnRelease(MouseEvent e){
        System.out.println("CheckModifiersOnRelease. currentButtonIndexUnderAction ="+currentButtonIndexUnderAction);
        for (int i = currentButtonIndexUnderAction+1; i<affectedButtonsToPressRelease.length; i++){
            if ((e.getModifiersEx() & affectedButtonsToPressRelease[i]) == 0){
                System.out.println("ERROR["+i+"]: RELEASED_EVENT is not filled with correct values. affectedButtonsToPressRelease[i]= "+affectedButtonsToPressRelease[i] +" Event = "+e);
                ModifierPermutation.failed = true;
            } else {
                System.out.println("CORRECT["+i+"]: affectedButtonsToPressRelease[i]= "+affectedButtonsToPressRelease[i]+ " Event = "+e);
            }
        }
    }
    public static void checkModifiersOnClick(MouseEvent e){
        System.out.println("CheckModifiersOnClick. currentButtonIndexUnderAction ="+currentButtonIndexUnderAction);
        for (int i = currentButtonIndexUnderAction+1; i<affectedButtonsToPressRelease.length; i++){
            if ((e.getModifiersEx() & affectedButtonsToPressRelease[i]) == 0){
                System.out.println("ERROR["+i+"]: CLICK_EVENT is not filled with correct values. affectedButtonsToPressRelease[i]= "+affectedButtonsToPressRelease[i] +" Event = "+e);
                ModifierPermutation.failed = true;
            } else {
                System.out.println("CORRECT["+i+"]: affectedButtonsToPressRelease[i]= "+affectedButtonsToPressRelease[i]+ " Event = "+e);
            }
        }
    }
}
class CheckingAdapter extends MouseAdapter{
    public CheckingAdapter(){}
    public void mousePressed(MouseEvent e) {
        System.out.println("PRESSED "+e);
        ModifierPermutation.checkModifiersOnPress(e);
    }
    public void mouseReleased(MouseEvent e) {
        System.out.println("RELEASED "+e);
        ModifierPermutation.checkModifiersOnRelease(e);
    }
    public void mouseClicked(MouseEvent e) {
        System.out.println("CLICKED "+e);
        ModifierPermutation.checkModifiersOnClick(e);
    }
}
