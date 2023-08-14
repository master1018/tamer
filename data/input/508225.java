class StubInitError {
    static boolean value = 5 / 0 > 0 ? true : false; 
}
public class T_sput_boolean_13 {
    public void run() {
        StubInitError.value = true;
    }
}
