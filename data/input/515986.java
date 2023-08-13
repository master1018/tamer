public class GLDebugHelper {
    public static final int CONFIG_CHECK_GL_ERROR = (1 << 0);
    public static final int CONFIG_CHECK_THREAD = (1 << 1);
    public static final int CONFIG_LOG_ARGUMENT_NAMES = (1 << 2);
    public static final int ERROR_WRONG_THREAD = 0x7000;
    public static GL wrap(GL gl, int configFlags, Writer log) {
        if ( configFlags != 0 ) {
            gl = new GLErrorWrapper(gl, configFlags);
        }
        if ( log != null ) {
            boolean logArgumentNames =
                (CONFIG_LOG_ARGUMENT_NAMES & configFlags) != 0;
            gl = new GLLogWrapper(gl, log, logArgumentNames);
        }
        return gl;
    }
    public static EGL wrap(EGL egl, int configFlags, Writer log) {
        if (log != null) {
            egl = new EGLLogWrapper(egl, configFlags, log);
        }
        return egl;
    }
}
