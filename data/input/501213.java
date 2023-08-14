public class Jsr239CodeEmitter extends JniCodeEmitter implements CodeEmitter {
    PrintStream mJava10InterfaceStream;
    PrintStream mJava10ExtInterfaceStream;
    PrintStream mJava11InterfaceStream;
    PrintStream mJava11ExtInterfaceStream;
    PrintStream mJava11ExtPackInterfaceStream;
    PrintStream mJavaImplStream;
    PrintStream mCStream;
    PrintStream mJavaInterfaceStream;
    public Jsr239CodeEmitter(String classPathName,
                          ParameterChecker checker,
                          PrintStream java10InterfaceStream,
                          PrintStream java10ExtInterfaceStream,
                          PrintStream java11InterfaceStream,
                          PrintStream java11ExtInterfaceStream,
                          PrintStream java11ExtPackInterfaceStream,
                          PrintStream javaImplStream,
                          PrintStream cStream,
                          boolean useContextPointer) {
        mClassPathName = classPathName;
        mChecker = checker;
        mJava10InterfaceStream = java10InterfaceStream;
        mJava10ExtInterfaceStream = java10ExtInterfaceStream;
        mJava11InterfaceStream = java11InterfaceStream;
        mJava11ExtInterfaceStream = java11ExtInterfaceStream;
        mJava11ExtPackInterfaceStream = java11ExtPackInterfaceStream;
        mJavaImplStream = javaImplStream;
        mCStream = cStream;
        mUseContextPointer = useContextPointer;
    }
    public void setVersion(int version, boolean ext, boolean pack) {
        if (version == 0) {
            mJavaInterfaceStream = ext ? mJava10ExtInterfaceStream :
                mJava10InterfaceStream;
        } else if (version == 1) {
            mJavaInterfaceStream = ext ?
                (pack ? mJava11ExtPackInterfaceStream :
                 mJava11ExtInterfaceStream) :
                mJava11InterfaceStream;
        } else {
            throw new RuntimeException("Bad version: " + version);
        }
    }
    public void emitCode(CFunc cfunc, String original) {
        emitCode(cfunc, original, mJavaInterfaceStream, mJavaImplStream, mCStream);
    }
    public void emitNativeRegistration() {
        emitNativeRegistration("register_com_google_android_gles_jni_GLImpl", mCStream);
    }
}
