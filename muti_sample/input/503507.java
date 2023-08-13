class StubInitError {
    static long value = (long)(5 / 0); 
}
public class T_sput_wide_13 {
    public void run() {
        StubInitError.value = 11;
    }
}
