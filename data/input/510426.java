class StubInitError {
    static char value = (char)(5 / 0); 
}
public class T_sput_char_13 {
    public void run() {
        StubInitError.value = 11;
    }
}
