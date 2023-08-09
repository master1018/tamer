class StubInitError {
    static byte value = (byte)(5 / 0); 
}
public class T_sput_byte_13 {
    public void run() {
        StubInitError.value = 11;
    }
}
