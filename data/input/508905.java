class StubInitError {
    static short value = (short)(5 / 0); 
}
public class T_sput_short_13 {
    public void run() {
        StubInitError.value = 11;
    }
}
