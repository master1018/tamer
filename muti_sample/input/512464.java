class StubInitError {
    static int value = 5 / 0; 
}
public class T_sput_13 {
    public void run() {
        StubInitError.value = 12;
    }
}
