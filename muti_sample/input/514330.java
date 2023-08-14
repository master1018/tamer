class StubInitError {
    static short t = (short)(5 / 0);
    static Object value;
}
public class T_sput_object_13 {
    public void run() {
        StubInitError.value = this;
    }
}
