public final class OverrideMethod {
    private static HashMap<String, MethodListener> sMethods = new HashMap<String, MethodListener>();
    private static MethodListener sDefaultListener = null;
    public static void setDefaultListener(MethodListener listener) {
        sDefaultListener = listener;
    }
    public static void setMethodListener(String signature, MethodListener listener) {
        if (listener == null) {
            sMethods.remove(signature);
        } else {
            sMethods.put(signature, listener);
        }
    }
    public static void invokeV(String signature, boolean isNative, Object caller) {
        MethodListener i = sMethods.get(signature);
        if (i != null) {
            i.onInvokeV(signature, isNative, caller);
        } else if (sDefaultListener != null) {
            sDefaultListener.onInvokeV(signature, isNative, caller);
        }
    }
    public static int invokeI(String signature, boolean isNative, Object caller) {
        MethodListener i = sMethods.get(signature);
        if (i != null) {
            return i.onInvokeI(signature, isNative, caller);
        } else if (sDefaultListener != null) {
            return sDefaultListener.onInvokeI(signature, isNative, caller);
        }
        return 0;
    }
    public static long invokeL(String signature, boolean isNative, Object caller) {
        MethodListener i = sMethods.get(signature);
        if (i != null) {
            return i.onInvokeL(signature, isNative, caller);
        } else if (sDefaultListener != null) {
            return sDefaultListener.onInvokeL(signature, isNative, caller);
        }
        return 0;
    }
    public static float invokeF(String signature, boolean isNative, Object caller) {
        MethodListener i = sMethods.get(signature);
        if (i != null) {
            return i.onInvokeF(signature, isNative, caller);
        } else if (sDefaultListener != null) {
            return sDefaultListener.onInvokeF(signature, isNative, caller);
        }
        return 0;
    }
    public static double invokeD(String signature, boolean isNative, Object caller) {
        MethodListener i = sMethods.get(signature);
        if (i != null) {
            return i.onInvokeD(signature, isNative, caller);
        } else if (sDefaultListener != null) {
            return sDefaultListener.onInvokeD(signature, isNative, caller);
        }
        return 0;
    }
    public static Object invokeA(String signature, boolean isNative, Object caller) {
        MethodListener i = sMethods.get(signature);
        if (i != null) {
            return i.onInvokeA(signature, isNative, caller);
        } else if (sDefaultListener != null) {
            return sDefaultListener.onInvokeA(signature, isNative, caller);
        }
        return null;
    }
}
