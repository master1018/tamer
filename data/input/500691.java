public class Switch {
    private static void testSwitch() {
        System.out.println("Switch.testSwitch");
        int a = 1;
        switch (a) {
            case -1: assert(false); break;
            case 0: assert(false); break;
            case 1:  break;
            case 2: assert(false); break;
            case 3: assert(false); break;
            case 4: assert(false); break;
            default: assert(false); break;
        }
        switch (a) {
            case 3: assert(false); break;
            case 4: assert(false); break;
            default:  break;
        }
        a = 0x12345678;
        switch (a) {
            case 0x12345678:  break;
            case 0x12345679: assert(false); break;
            default: assert(false); break;
        }
        switch (a) {
            case 57: assert(false); break;
            case -6: assert(false); break;
            case 0x12345678:  break;
            case 22: assert(false); break;
            case 3: assert(false); break;
            default: assert(false); break;
        }
        switch (a) {
            case -6: assert(false); break;
            case 3: assert(false); break;
            default:  break;
        }
        a = -5;
        switch (a) {
            case 12: assert(false); break;
            case -5:  break;
            case 0: assert(false); break;
            default: assert(false); break;
        }
        switch (a) {
            default:  break;
        }
    }
    public static void run() {
        testSwitch();
    }
}
