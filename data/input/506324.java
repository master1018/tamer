public class GLImpl implements GL10, GL10Ext, GL11, GL11Ext, GL11ExtensionPack {
    native private static void _nativeClassInit();
    static {
	_nativeClassInit();
    }
    Buffer _colorPointer = null;
    Buffer _normalPointer = null;
    Buffer _texCoordPointer = null;
    Buffer _vertexPointer = null;
    Buffer _pointSizePointerOES = null;
    Buffer _matrixIndexPointerOES = null;
    Buffer _weightPointerOES = null;
    private boolean haveCheckedExtensions;
    private boolean have_OES_blend_equation_separate;
    private boolean have_OES_blend_subtract;
    private boolean have_OES_framebuffer_object;
    private boolean have_OES_texture_cube_map;
    public GLImpl() {
    }
    public void glGetPointerv(int pname, java.nio.Buffer[] params) {
        throw new UnsupportedOperationException("glGetPointerv");
    }
    private static boolean allowIndirectBuffers(String appName) {
        boolean result = false;
        int version = 0;
        IPackageManager pm = ActivityThread.getPackageManager();
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(appName, 0);
            if (applicationInfo != null) {
                version = applicationInfo.targetSdkVersion;
            }
        } catch (android.os.RemoteException e) {
        }
        Log.e("OpenGLES", String.format(
            "Application %s (SDK target %d) called a GL11 Pointer method with an indirect Buffer.",
            appName, version));
        if (version <= Build.VERSION_CODES.CUPCAKE) {
            result = true;
        }
        return result;
    }
    public native void glActiveTexture(
        int texture
    );
    public native void glAlphaFunc(
        int func,
        float ref
    );
    public native void glAlphaFuncx(
        int func,
        int ref
    );
    public native void glBindTexture(
        int target,
        int texture
    );
    public native void glBlendFunc(
        int sfactor,
        int dfactor
    );
    public native void glClear(
        int mask
    );
    public native void glClearColor(
        float red,
        float green,
        float blue,
        float alpha
    );
    public native void glClearColorx(
        int red,
        int green,
        int blue,
        int alpha
    );
    public native void glClearDepthf(
        float depth
    );
    public native void glClearDepthx(
        int depth
    );
    public native void glClearStencil(
        int s
    );
    public native void glClientActiveTexture(
        int texture
    );
    public native void glColor4f(
        float red,
        float green,
        float blue,
        float alpha
    );
    public native void glColor4x(
        int red,
        int green,
        int blue,
        int alpha
    );
    public native void glColorMask(
        boolean red,
        boolean green,
        boolean blue,
        boolean alpha
    );
    private native void glColorPointerBounds(
        int size,
        int type,
        int stride,
        java.nio.Buffer pointer,
        int remaining
    );
    public void glColorPointer(
        int size,
        int type,
        int stride,
        java.nio.Buffer pointer
    ) {
        glColorPointerBounds(
            size,
            type,
            stride,
            pointer,
            pointer.remaining()
        );
        if ((size == 4) &&
            ((type == GL_FLOAT) ||
             (type == GL_UNSIGNED_BYTE) ||
             (type == GL_FIXED)) &&
            (stride >= 0)) {
            _colorPointer = pointer;
        }
    }
    public native void glCompressedTexImage2D(
        int target,
        int level,
        int internalformat,
        int width,
        int height,
        int border,
        int imageSize,
        java.nio.Buffer data
    );
    public native void glCompressedTexSubImage2D(
        int target,
        int level,
        int xoffset,
        int yoffset,
        int width,
        int height,
        int format,
        int imageSize,
        java.nio.Buffer data
    );
    public native void glCopyTexImage2D(
        int target,
        int level,
        int internalformat,
        int x,
        int y,
        int width,
        int height,
        int border
    );
    public native void glCopyTexSubImage2D(
        int target,
        int level,
        int xoffset,
        int yoffset,
        int x,
        int y,
        int width,
        int height
    );
    public native void glCullFace(
        int mode
    );
    public native void glDeleteTextures(
        int n,
        int[] textures,
        int offset
    );
    public native void glDeleteTextures(
        int n,
        java.nio.IntBuffer textures
    );
    public native void glDepthFunc(
        int func
    );
    public native void glDepthMask(
        boolean flag
    );
    public native void glDepthRangef(
        float zNear,
        float zFar
    );
    public native void glDepthRangex(
        int zNear,
        int zFar
    );
    public native void glDisable(
        int cap
    );
    public native void glDisableClientState(
        int array
    );
    public native void glDrawArrays(
        int mode,
        int first,
        int count
    );
    public native void glDrawElements(
        int mode,
        int count,
        int type,
        java.nio.Buffer indices
    );
    public native void glEnable(
        int cap
    );
    public native void glEnableClientState(
        int array
    );
    public native void glFinish(
    );
    public native void glFlush(
    );
    public native void glFogf(
        int pname,
        float param
    );
    public native void glFogfv(
        int pname,
        float[] params,
        int offset
    );
    public native void glFogfv(
        int pname,
        java.nio.FloatBuffer params
    );
    public native void glFogx(
        int pname,
        int param
    );
    public native void glFogxv(
        int pname,
        int[] params,
        int offset
    );
    public native void glFogxv(
        int pname,
        java.nio.IntBuffer params
    );
    public native void glFrontFace(
        int mode
    );
    public native void glFrustumf(
        float left,
        float right,
        float bottom,
        float top,
        float zNear,
        float zFar
    );
    public native void glFrustumx(
        int left,
        int right,
        int bottom,
        int top,
        int zNear,
        int zFar
    );
    public native void glGenTextures(
        int n,
        int[] textures,
        int offset
    );
    public native void glGenTextures(
        int n,
        java.nio.IntBuffer textures
    );
    public native int glGetError(
    );
    public native void glGetIntegerv(
        int pname,
        int[] params,
        int offset
    );
    public native void glGetIntegerv(
        int pname,
        java.nio.IntBuffer params
    );
    public native String _glGetString(
        int name
    );
    public String glGetString(
        int name
    ) {
        String returnValue;
        returnValue = _glGetString(
            name
        );
        return returnValue;
    }
    public native void glHint(
        int target,
        int mode
    );
    public native void glLightModelf(
        int pname,
        float param
    );
    public native void glLightModelfv(
        int pname,
        float[] params,
        int offset
    );
    public native void glLightModelfv(
        int pname,
        java.nio.FloatBuffer params
    );
    public native void glLightModelx(
        int pname,
        int param
    );
    public native void glLightModelxv(
        int pname,
        int[] params,
        int offset
    );
    public native void glLightModelxv(
        int pname,
        java.nio.IntBuffer params
    );
    public native void glLightf(
        int light,
        int pname,
        float param
    );
    public native void glLightfv(
        int light,
        int pname,
        float[] params,
        int offset
    );
    public native void glLightfv(
        int light,
        int pname,
        java.nio.FloatBuffer params
    );
    public native void glLightx(
        int light,
        int pname,
        int param
    );
    public native void glLightxv(
        int light,
        int pname,
        int[] params,
        int offset
    );
    public native void glLightxv(
        int light,
        int pname,
        java.nio.IntBuffer params
    );
    public native void glLineWidth(
        float width
    );
    public native void glLineWidthx(
        int width
    );
    public native void glLoadIdentity(
    );
    public native void glLoadMatrixf(
        float[] m,
        int offset
    );
    public native void glLoadMatrixf(
        java.nio.FloatBuffer m
    );
    public native void glLoadMatrixx(
        int[] m,
        int offset
    );
    public native void glLoadMatrixx(
        java.nio.IntBuffer m
    );
    public native void glLogicOp(
        int opcode
    );
    public native void glMaterialf(
        int face,
        int pname,
        float param
    );
    public native void glMaterialfv(
        int face,
        int pname,
        float[] params,
        int offset
    );
    public native void glMaterialfv(
        int face,
        int pname,
        java.nio.FloatBuffer params
    );
    public native void glMaterialx(
        int face,
        int pname,
        int param
    );
    public native void glMaterialxv(
        int face,
        int pname,
        int[] params,
        int offset
    );
    public native void glMaterialxv(
        int face,
        int pname,
        java.nio.IntBuffer params
    );
    public native void glMatrixMode(
        int mode
    );
    public native void glMultMatrixf(
        float[] m,
        int offset
    );
    public native void glMultMatrixf(
        java.nio.FloatBuffer m
    );
    public native void glMultMatrixx(
        int[] m,
        int offset
    );
    public native void glMultMatrixx(
        java.nio.IntBuffer m
    );
    public native void glMultiTexCoord4f(
        int target,
        float s,
        float t,
        float r,
        float q
    );
    public native void glMultiTexCoord4x(
        int target,
        int s,
        int t,
        int r,
        int q
    );
    public native void glNormal3f(
        float nx,
        float ny,
        float nz
    );
    public native void glNormal3x(
        int nx,
        int ny,
        int nz
    );
    private native void glNormalPointerBounds(
        int type,
        int stride,
        java.nio.Buffer pointer,
        int remaining
    );
    public void glNormalPointer(
        int type,
        int stride,
        java.nio.Buffer pointer
    ) {
        glNormalPointerBounds(
            type,
            stride,
            pointer,
            pointer.remaining()
        );
        if (((type == GL_FLOAT) ||
             (type == GL_BYTE) ||
             (type == GL_SHORT) ||
             (type == GL_FIXED)) &&
            (stride >= 0)) {
            _normalPointer = pointer;
        }
    }
    public native void glOrthof(
        float left,
        float right,
        float bottom,
        float top,
        float zNear,
        float zFar
    );
    public native void glOrthox(
        int left,
        int right,
        int bottom,
        int top,
        int zNear,
        int zFar
    );
    public native void glPixelStorei(
        int pname,
        int param
    );
    public native void glPointSize(
        float size
    );
    public native void glPointSizex(
        int size
    );
    public native void glPolygonOffset(
        float factor,
        float units
    );
    public native void glPolygonOffsetx(
        int factor,
        int units
    );
    public native void glPopMatrix(
    );
    public native void glPushMatrix(
    );
    public native void glReadPixels(
        int x,
        int y,
        int width,
        int height,
        int format,
        int type,
        java.nio.Buffer pixels
    );
    public native void glRotatef(
        float angle,
        float x,
        float y,
        float z
    );
    public native void glRotatex(
        int angle,
        int x,
        int y,
        int z
    );
    public native void glSampleCoverage(
        float value,
        boolean invert
    );
    public native void glSampleCoveragex(
        int value,
        boolean invert
    );
    public native void glScalef(
        float x,
        float y,
        float z
    );
    public native void glScalex(
        int x,
        int y,
        int z
    );
    public native void glScissor(
        int x,
        int y,
        int width,
        int height
    );
    public native void glShadeModel(
        int mode
    );
    public native void glStencilFunc(
        int func,
        int ref,
        int mask
    );
    public native void glStencilMask(
        int mask
    );
    public native void glStencilOp(
        int fail,
        int zfail,
        int zpass
    );
    private native void glTexCoordPointerBounds(
        int size,
        int type,
        int stride,
        java.nio.Buffer pointer,
        int remaining
    );
    public void glTexCoordPointer(
        int size,
        int type,
        int stride,
        java.nio.Buffer pointer
    ) {
        glTexCoordPointerBounds(
            size,
            type,
            stride,
            pointer,
            pointer.remaining()
        );
        if (((size == 2) ||
             (size == 3) ||
             (size == 4)) &&
            ((type == GL_FLOAT) ||
             (type == GL_BYTE) ||
             (type == GL_SHORT) ||
             (type == GL_FIXED)) &&
            (stride >= 0)) {
            _texCoordPointer = pointer;
        }
    }
    public native void glTexEnvf(
        int target,
        int pname,
        float param
    );
    public native void glTexEnvfv(
        int target,
        int pname,
        float[] params,
        int offset
    );
    public native void glTexEnvfv(
        int target,
        int pname,
        java.nio.FloatBuffer params
    );
    public native void glTexEnvx(
        int target,
        int pname,
        int param
    );
    public native void glTexEnvxv(
        int target,
        int pname,
        int[] params,
        int offset
    );
    public native void glTexEnvxv(
        int target,
        int pname,
        java.nio.IntBuffer params
    );
    public native void glTexImage2D(
        int target,
        int level,
        int internalformat,
        int width,
        int height,
        int border,
        int format,
        int type,
        java.nio.Buffer pixels
    );
    public native void glTexParameterf(
        int target,
        int pname,
        float param
    );
    public native void glTexParameterx(
        int target,
        int pname,
        int param
    );
    public native void glTexSubImage2D(
        int target,
        int level,
        int xoffset,
        int yoffset,
        int width,
        int height,
        int format,
        int type,
        java.nio.Buffer pixels
    );
    public native void glTranslatef(
        float x,
        float y,
        float z
    );
    public native void glTranslatex(
        int x,
        int y,
        int z
    );
    private native void glVertexPointerBounds(
        int size,
        int type,
        int stride,
        java.nio.Buffer pointer,
        int remaining
    );
    public void glVertexPointer(
        int size,
        int type,
        int stride,
        java.nio.Buffer pointer
    ) {
        glVertexPointerBounds(
            size,
            type,
            stride,
            pointer,
            pointer.remaining()
        );
        if (((size == 2) ||
             (size == 3) ||
             (size == 4)) &&
            ((type == GL_FLOAT) ||
             (type == GL_BYTE) ||
             (type == GL_SHORT) ||
             (type == GL_FIXED)) &&
            (stride >= 0)) {
            _vertexPointer = pointer;
        }
    }
    public native void glViewport(
        int x,
        int y,
        int width,
        int height
    );
    public native int glQueryMatrixxOES(
        int[] mantissa,
        int mantissaOffset,
        int[] exponent,
        int exponentOffset
    );
    public native int glQueryMatrixxOES(
        java.nio.IntBuffer mantissa,
        java.nio.IntBuffer exponent
    );
    public native void glBindBuffer(
        int target,
        int buffer
    );
    public native void glBufferData(
        int target,
        int size,
        java.nio.Buffer data,
        int usage
    );
    public native void glBufferSubData(
        int target,
        int offset,
        int size,
        java.nio.Buffer data
    );
    public native void glClipPlanef(
        int plane,
        float[] equation,
        int offset
    );
    public native void glClipPlanef(
        int plane,
        java.nio.FloatBuffer equation
    );
    public native void glClipPlanex(
        int plane,
        int[] equation,
        int offset
    );
    public native void glClipPlanex(
        int plane,
        java.nio.IntBuffer equation
    );
    public native void glColor4ub(
        byte red,
        byte green,
        byte blue,
        byte alpha
    );
    public native void glColorPointer(
        int size,
        int type,
        int stride,
        int offset
    );
    public native void glDeleteBuffers(
        int n,
        int[] buffers,
        int offset
    );
    public native void glDeleteBuffers(
        int n,
        java.nio.IntBuffer buffers
    );
    public native void glDrawElements(
        int mode,
        int count,
        int type,
        int offset
    );
    public native void glGenBuffers(
        int n,
        int[] buffers,
        int offset
    );
    public native void glGenBuffers(
        int n,
        java.nio.IntBuffer buffers
    );
    public native void glGetBooleanv(
        int pname,
        boolean[] params,
        int offset
    );
    public native void glGetBooleanv(
        int pname,
        java.nio.IntBuffer params
    );
    public native void glGetBufferParameteriv(
        int target,
        int pname,
        int[] params,
        int offset
    );
    public native void glGetBufferParameteriv(
        int target,
        int pname,
        java.nio.IntBuffer params
    );
    public native void glGetClipPlanef(
        int pname,
        float[] eqn,
        int offset
    );
    public native void glGetClipPlanef(
        int pname,
        java.nio.FloatBuffer eqn
    );
    public native void glGetClipPlanex(
        int pname,
        int[] eqn,
        int offset
    );
    public native void glGetClipPlanex(
        int pname,
        java.nio.IntBuffer eqn
    );
    public native void glGetFixedv(
        int pname,
        int[] params,
        int offset
    );
    public native void glGetFixedv(
        int pname,
        java.nio.IntBuffer params
    );
    public native void glGetFloatv(
        int pname,
        float[] params,
        int offset
    );
    public native void glGetFloatv(
        int pname,
        java.nio.FloatBuffer params
    );
    public native void glGetLightfv(
        int light,
        int pname,
        float[] params,
        int offset
    );
    public native void glGetLightfv(
        int light,
        int pname,
        java.nio.FloatBuffer params
    );
    public native void glGetLightxv(
        int light,
        int pname,
        int[] params,
        int offset
    );
    public native void glGetLightxv(
        int light,
        int pname,
        java.nio.IntBuffer params
    );
    public native void glGetMaterialfv(
        int face,
        int pname,
        float[] params,
        int offset
    );
    public native void glGetMaterialfv(
        int face,
        int pname,
        java.nio.FloatBuffer params
    );
    public native void glGetMaterialxv(
        int face,
        int pname,
        int[] params,
        int offset
    );
    public native void glGetMaterialxv(
        int face,
        int pname,
        java.nio.IntBuffer params
    );
    public native void glGetTexEnviv(
        int env,
        int pname,
        int[] params,
        int offset
    );
    public native void glGetTexEnviv(
        int env,
        int pname,
        java.nio.IntBuffer params
    );
    public native void glGetTexEnvxv(
        int env,
        int pname,
        int[] params,
        int offset
    );
    public native void glGetTexEnvxv(
        int env,
        int pname,
        java.nio.IntBuffer params
    );
    public native void glGetTexParameterfv(
        int target,
        int pname,
        float[] params,
        int offset
    );
    public native void glGetTexParameterfv(
        int target,
        int pname,
        java.nio.FloatBuffer params
    );
    public native void glGetTexParameteriv(
        int target,
        int pname,
        int[] params,
        int offset
    );
    public native void glGetTexParameteriv(
        int target,
        int pname,
        java.nio.IntBuffer params
    );
    public native void glGetTexParameterxv(
        int target,
        int pname,
        int[] params,
        int offset
    );
    public native void glGetTexParameterxv(
        int target,
        int pname,
        java.nio.IntBuffer params
    );
    public native boolean glIsBuffer(
        int buffer
    );
    public native boolean glIsEnabled(
        int cap
    );
    public native boolean glIsTexture(
        int texture
    );
    public native void glNormalPointer(
        int type,
        int stride,
        int offset
    );
    public native void glPointParameterf(
        int pname,
        float param
    );
    public native void glPointParameterfv(
        int pname,
        float[] params,
        int offset
    );
    public native void glPointParameterfv(
        int pname,
        java.nio.FloatBuffer params
    );
    public native void glPointParameterx(
        int pname,
        int param
    );
    public native void glPointParameterxv(
        int pname,
        int[] params,
        int offset
    );
    public native void glPointParameterxv(
        int pname,
        java.nio.IntBuffer params
    );
    private native void glPointSizePointerOESBounds(
        int type,
        int stride,
        java.nio.Buffer pointer,
        int remaining
    );
    public void glPointSizePointerOES(
        int type,
        int stride,
        java.nio.Buffer pointer
    ) {
        glPointSizePointerOESBounds(
            type,
            stride,
            pointer,
            pointer.remaining()
        );
        if (((type == GL_FLOAT) ||
             (type == GL_FIXED)) &&
            (stride >= 0)) {
            _pointSizePointerOES = pointer;
        }
    }
    public native void glTexCoordPointer(
        int size,
        int type,
        int stride,
        int offset
    );
    public native void glTexEnvi(
        int target,
        int pname,
        int param
    );
    public native void glTexEnviv(
        int target,
        int pname,
        int[] params,
        int offset
    );
    public native void glTexEnviv(
        int target,
        int pname,
        java.nio.IntBuffer params
    );
    public native void glTexParameterfv(
        int target,
        int pname,
        float[] params,
        int offset
    );
    public native void glTexParameterfv(
        int target,
        int pname,
        java.nio.FloatBuffer params
    );
    public native void glTexParameteri(
        int target,
        int pname,
        int param
    );
    public native void glTexParameteriv(
        int target,
        int pname,
        int[] params,
        int offset
    );
    public native void glTexParameteriv(
        int target,
        int pname,
        java.nio.IntBuffer params
    );
    public native void glTexParameterxv(
        int target,
        int pname,
        int[] params,
        int offset
    );
    public native void glTexParameterxv(
        int target,
        int pname,
        java.nio.IntBuffer params
    );
    public native void glVertexPointer(
        int size,
        int type,
        int stride,
        int offset
    );
    public native void glCurrentPaletteMatrixOES(
        int matrixpaletteindex
    );
    public native void glDrawTexfOES(
        float x,
        float y,
        float z,
        float width,
        float height
    );
    public native void glDrawTexfvOES(
        float[] coords,
        int offset
    );
    public native void glDrawTexfvOES(
        java.nio.FloatBuffer coords
    );
    public native void glDrawTexiOES(
        int x,
        int y,
        int z,
        int width,
        int height
    );
    public native void glDrawTexivOES(
        int[] coords,
        int offset
    );
    public native void glDrawTexivOES(
        java.nio.IntBuffer coords
    );
    public native void glDrawTexsOES(
        short x,
        short y,
        short z,
        short width,
        short height
    );
    public native void glDrawTexsvOES(
        short[] coords,
        int offset
    );
    public native void glDrawTexsvOES(
        java.nio.ShortBuffer coords
    );
    public native void glDrawTexxOES(
        int x,
        int y,
        int z,
        int width,
        int height
    );
    public native void glDrawTexxvOES(
        int[] coords,
        int offset
    );
    public native void glDrawTexxvOES(
        java.nio.IntBuffer coords
    );
    public native void glLoadPaletteFromModelViewMatrixOES(
    );
    private native void glMatrixIndexPointerOESBounds(
        int size,
        int type,
        int stride,
        java.nio.Buffer pointer,
        int remaining
    );
    public void glMatrixIndexPointerOES(
        int size,
        int type,
        int stride,
        java.nio.Buffer pointer
    ) {
        glMatrixIndexPointerOESBounds(
            size,
            type,
            stride,
            pointer,
            pointer.remaining()
        );
        if (((size == 2) ||
             (size == 3) ||
             (size == 4)) &&
            ((type == GL_FLOAT) ||
             (type == GL_BYTE) ||
             (type == GL_SHORT) ||
             (type == GL_FIXED)) &&
            (stride >= 0)) {
            _matrixIndexPointerOES = pointer;
        }
    }
    public native void glMatrixIndexPointerOES(
        int size,
        int type,
        int stride,
        int offset
    );
    private native void glWeightPointerOESBounds(
        int size,
        int type,
        int stride,
        java.nio.Buffer pointer,
        int remaining
    );
    public void glWeightPointerOES(
        int size,
        int type,
        int stride,
        java.nio.Buffer pointer
    ) {
        glWeightPointerOESBounds(
            size,
            type,
            stride,
            pointer,
            pointer.remaining()
        );
    }
    public native void glWeightPointerOES(
        int size,
        int type,
        int stride,
        int offset
    );
    public native void glBindFramebufferOES(
        int target,
        int framebuffer
    );
    public native void glBindRenderbufferOES(
        int target,
        int renderbuffer
    );
    public native void glBlendEquation(
        int mode
    );
    public native void glBlendEquationSeparate(
        int modeRGB,
        int modeAlpha
    );
    public native void glBlendFuncSeparate(
        int srcRGB,
        int dstRGB,
        int srcAlpha,
        int dstAlpha
    );
    public native int glCheckFramebufferStatusOES(
        int target
    );
    public native void glDeleteFramebuffersOES(
        int n,
        int[] framebuffers,
        int offset
    );
    public native void glDeleteFramebuffersOES(
        int n,
        java.nio.IntBuffer framebuffers
    );
    public native void glDeleteRenderbuffersOES(
        int n,
        int[] renderbuffers,
        int offset
    );
    public native void glDeleteRenderbuffersOES(
        int n,
        java.nio.IntBuffer renderbuffers
    );
    public native void glFramebufferRenderbufferOES(
        int target,
        int attachment,
        int renderbuffertarget,
        int renderbuffer
    );
    public native void glFramebufferTexture2DOES(
        int target,
        int attachment,
        int textarget,
        int texture,
        int level
    );
    public native void glGenerateMipmapOES(
        int target
    );
    public native void glGenFramebuffersOES(
        int n,
        int[] framebuffers,
        int offset
    );
    public native void glGenFramebuffersOES(
        int n,
        java.nio.IntBuffer framebuffers
    );
    public native void glGenRenderbuffersOES(
        int n,
        int[] renderbuffers,
        int offset
    );
    public native void glGenRenderbuffersOES(
        int n,
        java.nio.IntBuffer renderbuffers
    );
    public native void glGetFramebufferAttachmentParameterivOES(
        int target,
        int attachment,
        int pname,
        int[] params,
        int offset
    );
    public native void glGetFramebufferAttachmentParameterivOES(
        int target,
        int attachment,
        int pname,
        java.nio.IntBuffer params
    );
    public native void glGetRenderbufferParameterivOES(
        int target,
        int pname,
        int[] params,
        int offset
    );
    public native void glGetRenderbufferParameterivOES(
        int target,
        int pname,
        java.nio.IntBuffer params
    );
    public native void glGetTexGenfv(
        int coord,
        int pname,
        float[] params,
        int offset
    );
    public native void glGetTexGenfv(
        int coord,
        int pname,
        java.nio.FloatBuffer params
    );
    public native void glGetTexGeniv(
        int coord,
        int pname,
        int[] params,
        int offset
    );
    public native void glGetTexGeniv(
        int coord,
        int pname,
        java.nio.IntBuffer params
    );
    public native void glGetTexGenxv(
        int coord,
        int pname,
        int[] params,
        int offset
    );
    public native void glGetTexGenxv(
        int coord,
        int pname,
        java.nio.IntBuffer params
    );
    public native boolean glIsFramebufferOES(
        int framebuffer
    );
    public native boolean glIsRenderbufferOES(
        int renderbuffer
    );
    public native void glRenderbufferStorageOES(
        int target,
        int internalformat,
        int width,
        int height
    );
    public native void glTexGenf(
        int coord,
        int pname,
        float param
    );
    public native void glTexGenfv(
        int coord,
        int pname,
        float[] params,
        int offset
    );
    public native void glTexGenfv(
        int coord,
        int pname,
        java.nio.FloatBuffer params
    );
    public native void glTexGeni(
        int coord,
        int pname,
        int param
    );
    public native void glTexGeniv(
        int coord,
        int pname,
        int[] params,
        int offset
    );
    public native void glTexGeniv(
        int coord,
        int pname,
        java.nio.IntBuffer params
    );
    public native void glTexGenx(
        int coord,
        int pname,
        int param
    );
    public native void glTexGenxv(
        int coord,
        int pname,
        int[] params,
        int offset
    );
    public native void glTexGenxv(
        int coord,
        int pname,
        java.nio.IntBuffer params
    );
}
