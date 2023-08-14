public class MethodAdapter implements MethodListener {
    public void onInvokeV(String signature, boolean isNative, Object caller) {
    }
    public int onInvokeI(String signature, boolean isNative, Object caller) {
        onInvokeV(signature, isNative, caller);
        return 0;
    }
    public long onInvokeL(String signature, boolean isNative, Object caller) {
        onInvokeV(signature, isNative, caller);
        return 0;
    }
    public float onInvokeF(String signature, boolean isNative, Object caller) {
        onInvokeV(signature, isNative, caller);
        return 0;
    }
    public double onInvokeD(String signature, boolean isNative, Object caller) {
        onInvokeV(signature, isNative, caller);
        return 0;
    }
    public Object onInvokeA(String signature, boolean isNative, Object caller) {
        onInvokeV(signature, isNative, caller);
        return null;
    }
}
