public class GLESCodeEmitter extends JniCodeEmitter {
    PrintStream mJavaImplStream;
    PrintStream mCStream;
    PrintStream mJavaInterfaceStream;
    public GLESCodeEmitter(String classPathName,
                          ParameterChecker checker,
                          PrintStream javaImplStream,
                          PrintStream cStream) {
        mClassPathName = classPathName;
        mChecker = checker;
        mJavaImplStream = javaImplStream;
        mCStream = cStream;
        mUseContextPointer = false;
        mUseStaticMethods = true;
    }
    public void emitCode(CFunc cfunc, String original) {
        emitCode(cfunc, original, null, mJavaImplStream,
                mCStream);
    }
    public void emitNativeRegistration(String nativeRegistrationName) {
        emitNativeRegistration(nativeRegistrationName, mCStream);
    }
}
