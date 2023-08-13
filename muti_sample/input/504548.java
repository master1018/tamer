public class GLES11Ext {
    public static final int GL_BLEND_EQUATION_RGB_OES                               = 0x8009;
    public static final int GL_BLEND_EQUATION_ALPHA_OES                             = 0x883D;
    public static final int GL_BLEND_DST_RGB_OES                                    = 0x80C8;
    public static final int GL_BLEND_SRC_RGB_OES                                    = 0x80C9;
    public static final int GL_BLEND_DST_ALPHA_OES                                  = 0x80CA;
    public static final int GL_BLEND_SRC_ALPHA_OES                                  = 0x80CB;
    public static final int GL_BLEND_EQUATION_OES                                   = 0x8009;
    public static final int GL_FUNC_ADD_OES                                         = 0x8006;
    public static final int GL_FUNC_SUBTRACT_OES                                    = 0x800A;
    public static final int GL_FUNC_REVERSE_SUBTRACT_OES                            = 0x800B;
    public static final int GL_ETC1_RGB8_OES                                        = 0x8D64;
    public static final int GL_DEPTH_COMPONENT24_OES                                = 0x81A6;
    public static final int GL_DEPTH_COMPONENT32_OES                                = 0x81A7;
    public static final int GL_TEXTURE_CROP_RECT_OES                                = 0x8B9D;
    public static final int GL_FIXED_OES                                            = 0x140C;
    public static final int GL_NONE_OES                                             = 0;
    public static final int GL_FRAMEBUFFER_OES                                      = 0x8D40;
    public static final int GL_RENDERBUFFER_OES                                     = 0x8D41;
    public static final int GL_RGBA4_OES                                            = 0x8056;
    public static final int GL_RGB5_A1_OES                                          = 0x8057;
    public static final int GL_RGB565_OES                                           = 0x8D62;
    public static final int GL_DEPTH_COMPONENT16_OES                                = 0x81A5;
    public static final int GL_RENDERBUFFER_WIDTH_OES                               = 0x8D42;
    public static final int GL_RENDERBUFFER_HEIGHT_OES                              = 0x8D43;
    public static final int GL_RENDERBUFFER_INTERNAL_FORMAT_OES                     = 0x8D44;
    public static final int GL_RENDERBUFFER_RED_SIZE_OES                            = 0x8D50;
    public static final int GL_RENDERBUFFER_GREEN_SIZE_OES                          = 0x8D51;
    public static final int GL_RENDERBUFFER_BLUE_SIZE_OES                           = 0x8D52;
    public static final int GL_RENDERBUFFER_ALPHA_SIZE_OES                          = 0x8D53;
    public static final int GL_RENDERBUFFER_DEPTH_SIZE_OES                          = 0x8D54;
    public static final int GL_RENDERBUFFER_STENCIL_SIZE_OES                        = 0x8D55;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE_OES               = 0x8CD0;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME_OES               = 0x8CD1;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL_OES             = 0x8CD2;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE_OES     = 0x8CD3;
    public static final int GL_COLOR_ATTACHMENT0_OES                                = 0x8CE0;
    public static final int GL_DEPTH_ATTACHMENT_OES                                 = 0x8D00;
    public static final int GL_STENCIL_ATTACHMENT_OES                               = 0x8D20;
    public static final int GL_FRAMEBUFFER_COMPLETE_OES                             = 0x8CD5;
    public static final int GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_OES                = 0x8CD6;
    public static final int GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_OES        = 0x8CD7;
    public static final int GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_OES                = 0x8CD9;
    public static final int GL_FRAMEBUFFER_INCOMPLETE_FORMATS_OES                   = 0x8CDA;
    public static final int GL_FRAMEBUFFER_UNSUPPORTED_OES                          = 0x8CDD;
    public static final int GL_FRAMEBUFFER_BINDING_OES                              = 0x8CA6;
    public static final int GL_RENDERBUFFER_BINDING_OES                             = 0x8CA7;
    public static final int GL_MAX_RENDERBUFFER_SIZE_OES                            = 0x84E8;
    public static final int GL_INVALID_FRAMEBUFFER_OPERATION_OES                    = 0x0506;
    public static final int GL_WRITE_ONLY_OES                                       = 0x88B9;
    public static final int GL_BUFFER_ACCESS_OES                                    = 0x88BB;
    public static final int GL_BUFFER_MAPPED_OES                                    = 0x88BC;
    public static final int GL_BUFFER_MAP_POINTER_OES                               = 0x88BD;
    public static final int GL_MODELVIEW_MATRIX_FLOAT_AS_INT_BITS_OES               = 0x898D;
    public static final int GL_PROJECTION_MATRIX_FLOAT_AS_INT_BITS_OES              = 0x898E;
    public static final int GL_TEXTURE_MATRIX_FLOAT_AS_INT_BITS_OES                 = 0x898F;
    public static final int GL_MAX_VERTEX_UNITS_OES                                 = 0x86A4;
    public static final int GL_MAX_PALETTE_MATRICES_OES                             = 0x8842;
    public static final int GL_MATRIX_PALETTE_OES                                   = 0x8840;
    public static final int GL_MATRIX_INDEX_ARRAY_OES                               = 0x8844;
    public static final int GL_WEIGHT_ARRAY_OES                                     = 0x86AD;
    public static final int GL_CURRENT_PALETTE_MATRIX_OES                           = 0x8843;
    public static final int GL_MATRIX_INDEX_ARRAY_SIZE_OES                          = 0x8846;
    public static final int GL_MATRIX_INDEX_ARRAY_TYPE_OES                          = 0x8847;
    public static final int GL_MATRIX_INDEX_ARRAY_STRIDE_OES                        = 0x8848;
    public static final int GL_MATRIX_INDEX_ARRAY_POINTER_OES                       = 0x8849;
    public static final int GL_MATRIX_INDEX_ARRAY_BUFFER_BINDING_OES                = 0x8B9E;
    public static final int GL_WEIGHT_ARRAY_SIZE_OES                                = 0x86AB;
    public static final int GL_WEIGHT_ARRAY_TYPE_OES                                = 0x86A9;
    public static final int GL_WEIGHT_ARRAY_STRIDE_OES                              = 0x86AA;
    public static final int GL_WEIGHT_ARRAY_POINTER_OES                             = 0x86AC;
    public static final int GL_WEIGHT_ARRAY_BUFFER_BINDING_OES                      = 0x889E;
    public static final int GL_DEPTH_STENCIL_OES                                    = 0x84F9;
    public static final int GL_UNSIGNED_INT_24_8_OES                                = 0x84FA;
    public static final int GL_DEPTH24_STENCIL8_OES                                 = 0x88F0;
    public static final int GL_RGB8_OES                                             = 0x8051;
    public static final int GL_RGBA8_OES                                            = 0x8058;
    public static final int GL_STENCIL_INDEX1_OES                                   = 0x8D46;
    public static final int GL_STENCIL_INDEX4_OES                                   = 0x8D47;
    public static final int GL_STENCIL_INDEX8_OES                                   = 0x8D48;
    public static final int GL_INCR_WRAP_OES                                        = 0x8507;
    public static final int GL_DECR_WRAP_OES                                        = 0x8508;
    public static final int GL_NORMAL_MAP_OES                                       = 0x8511;
    public static final int GL_REFLECTION_MAP_OES                                   = 0x8512;
    public static final int GL_TEXTURE_CUBE_MAP_OES                                 = 0x8513;
    public static final int GL_TEXTURE_BINDING_CUBE_MAP_OES                         = 0x8514;
    public static final int GL_TEXTURE_CUBE_MAP_POSITIVE_X_OES                      = 0x8515;
    public static final int GL_TEXTURE_CUBE_MAP_NEGATIVE_X_OES                      = 0x8516;
    public static final int GL_TEXTURE_CUBE_MAP_POSITIVE_Y_OES                      = 0x8517;
    public static final int GL_TEXTURE_CUBE_MAP_NEGATIVE_Y_OES                      = 0x8518;
    public static final int GL_TEXTURE_CUBE_MAP_POSITIVE_Z_OES                      = 0x8519;
    public static final int GL_TEXTURE_CUBE_MAP_NEGATIVE_Z_OES                      = 0x851A;
    public static final int GL_MAX_CUBE_MAP_TEXTURE_SIZE_OES                        = 0x851C;
    public static final int GL_TEXTURE_GEN_MODE_OES                                 = 0x2500;
    public static final int GL_TEXTURE_GEN_STR_OES                                  = 0x8D60;
    public static final int GL_MIRRORED_REPEAT_OES                                  = 0x8370;
    public static final int GL_3DC_X_AMD                                            = 0x87F9;
    public static final int GL_3DC_XY_AMD                                           = 0x87FA;
    public static final int GL_ATC_RGB_AMD                                          = 0x8C92;
    public static final int GL_ATC_RGBA_EXPLICIT_ALPHA_AMD                          = 0x8C93;
    public static final int GL_ATC_RGBA_INTERPOLATED_ALPHA_AMD                      = 0x87EE;
    public static final int GL_TEXTURE_MAX_ANISOTROPY_EXT                           = 0x84FE;
    public static final int GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT                       = 0x84FF;
    public static final int GL_BGRA                                                 = 0x80E1;
    native private static void _nativeClassInit();
    static {
	    _nativeClassInit();
    }
    private static final int GL_BYTE = GLES10.GL_BYTE;
    private static final int GL_FIXED = GLES10.GL_FIXED;
    private static final int GL_FLOAT = GLES10.GL_FLOAT;
    private static final int GL_SHORT = GLES10.GL_SHORT;
    private static Buffer _matrixIndexPointerOES;
    public static native void glBlendEquationSeparateOES(
        int modeRGB,
        int modeAlpha
    );
    public static native void glBlendFuncSeparateOES(
        int srcRGB,
        int dstRGB,
        int srcAlpha,
        int dstAlpha
    );
    public static native void glBlendEquationOES(
        int mode
    );
    public static native void glDrawTexsOES(
        short x,
        short y,
        short z,
        short width,
        short height
    );
    public static native void glDrawTexiOES(
        int x,
        int y,
        int z,
        int width,
        int height
    );
    public static native void glDrawTexxOES(
        int x,
        int y,
        int z,
        int width,
        int height
    );
    public static native void glDrawTexsvOES(
        short[] coords,
        int offset
    );
    public static native void glDrawTexsvOES(
        java.nio.ShortBuffer coords
    );
    public static native void glDrawTexivOES(
        int[] coords,
        int offset
    );
    public static native void glDrawTexivOES(
        java.nio.IntBuffer coords
    );
    public static native void glDrawTexxvOES(
        int[] coords,
        int offset
    );
    public static native void glDrawTexxvOES(
        java.nio.IntBuffer coords
    );
    public static native void glDrawTexfOES(
        float x,
        float y,
        float z,
        float width,
        float height
    );
    public static native void glDrawTexfvOES(
        float[] coords,
        int offset
    );
    public static native void glDrawTexfvOES(
        java.nio.FloatBuffer coords
    );
    public static native void glEGLImageTargetTexture2DOES(
        int target,
        java.nio.Buffer image
    );
    public static native void glEGLImageTargetRenderbufferStorageOES(
        int target,
        java.nio.Buffer image
    );
    public static native void glAlphaFuncxOES(
        int func,
        int ref
    );
    public static native void glClearColorxOES(
        int red,
        int green,
        int blue,
        int alpha
    );
    public static native void glClearDepthxOES(
        int depth
    );
    public static native void glClipPlanexOES(
        int plane,
        int[] equation,
        int offset
    );
    public static native void glClipPlanexOES(
        int plane,
        java.nio.IntBuffer equation
    );
    public static native void glColor4xOES(
        int red,
        int green,
        int blue,
        int alpha
    );
    public static native void glDepthRangexOES(
        int zNear,
        int zFar
    );
    public static native void glFogxOES(
        int pname,
        int param
    );
    public static native void glFogxvOES(
        int pname,
        int[] params,
        int offset
    );
    public static native void glFogxvOES(
        int pname,
        java.nio.IntBuffer params
    );
    public static native void glFrustumxOES(
        int left,
        int right,
        int bottom,
        int top,
        int zNear,
        int zFar
    );
    public static native void glGetClipPlanexOES(
        int pname,
        int[] eqn,
        int offset
    );
    public static native void glGetClipPlanexOES(
        int pname,
        java.nio.IntBuffer eqn
    );
    public static native void glGetFixedvOES(
        int pname,
        int[] params,
        int offset
    );
    public static native void glGetFixedvOES(
        int pname,
        java.nio.IntBuffer params
    );
    public static native void glGetLightxvOES(
        int light,
        int pname,
        int[] params,
        int offset
    );
    public static native void glGetLightxvOES(
        int light,
        int pname,
        java.nio.IntBuffer params
    );
    public static native void glGetMaterialxvOES(
        int face,
        int pname,
        int[] params,
        int offset
    );
    public static native void glGetMaterialxvOES(
        int face,
        int pname,
        java.nio.IntBuffer params
    );
    public static native void glGetTexEnvxvOES(
        int env,
        int pname,
        int[] params,
        int offset
    );
    public static native void glGetTexEnvxvOES(
        int env,
        int pname,
        java.nio.IntBuffer params
    );
    public static native void glGetTexParameterxvOES(
        int target,
        int pname,
        int[] params,
        int offset
    );
    public static native void glGetTexParameterxvOES(
        int target,
        int pname,
        java.nio.IntBuffer params
    );
    public static native void glLightModelxOES(
        int pname,
        int param
    );
    public static native void glLightModelxvOES(
        int pname,
        int[] params,
        int offset
    );
    public static native void glLightModelxvOES(
        int pname,
        java.nio.IntBuffer params
    );
    public static native void glLightxOES(
        int light,
        int pname,
        int param
    );
    public static native void glLightxvOES(
        int light,
        int pname,
        int[] params,
        int offset
    );
    public static native void glLightxvOES(
        int light,
        int pname,
        java.nio.IntBuffer params
    );
    public static native void glLineWidthxOES(
        int width
    );
    public static native void glLoadMatrixxOES(
        int[] m,
        int offset
    );
    public static native void glLoadMatrixxOES(
        java.nio.IntBuffer m
    );
    public static native void glMaterialxOES(
        int face,
        int pname,
        int param
    );
    public static native void glMaterialxvOES(
        int face,
        int pname,
        int[] params,
        int offset
    );
    public static native void glMaterialxvOES(
        int face,
        int pname,
        java.nio.IntBuffer params
    );
    public static native void glMultMatrixxOES(
        int[] m,
        int offset
    );
    public static native void glMultMatrixxOES(
        java.nio.IntBuffer m
    );
    public static native void glMultiTexCoord4xOES(
        int target,
        int s,
        int t,
        int r,
        int q
    );
    public static native void glNormal3xOES(
        int nx,
        int ny,
        int nz
    );
    public static native void glOrthoxOES(
        int left,
        int right,
        int bottom,
        int top,
        int zNear,
        int zFar
    );
    public static native void glPointParameterxOES(
        int pname,
        int param
    );
    public static native void glPointParameterxvOES(
        int pname,
        int[] params,
        int offset
    );
    public static native void glPointParameterxvOES(
        int pname,
        java.nio.IntBuffer params
    );
    public static native void glPointSizexOES(
        int size
    );
    public static native void glPolygonOffsetxOES(
        int factor,
        int units
    );
    public static native void glRotatexOES(
        int angle,
        int x,
        int y,
        int z
    );
    public static native void glSampleCoveragexOES(
        int value,
        boolean invert
    );
    public static native void glScalexOES(
        int x,
        int y,
        int z
    );
    public static native void glTexEnvxOES(
        int target,
        int pname,
        int param
    );
    public static native void glTexEnvxvOES(
        int target,
        int pname,
        int[] params,
        int offset
    );
    public static native void glTexEnvxvOES(
        int target,
        int pname,
        java.nio.IntBuffer params
    );
    public static native void glTexParameterxOES(
        int target,
        int pname,
        int param
    );
    public static native void glTexParameterxvOES(
        int target,
        int pname,
        int[] params,
        int offset
    );
    public static native void glTexParameterxvOES(
        int target,
        int pname,
        java.nio.IntBuffer params
    );
    public static native void glTranslatexOES(
        int x,
        int y,
        int z
    );
    public static native boolean glIsRenderbufferOES(
        int renderbuffer
    );
    public static native void glBindRenderbufferOES(
        int target,
        int renderbuffer
    );
    public static native void glDeleteRenderbuffersOES(
        int n,
        int[] renderbuffers,
        int offset
    );
    public static native void glDeleteRenderbuffersOES(
        int n,
        java.nio.IntBuffer renderbuffers
    );
    public static native void glGenRenderbuffersOES(
        int n,
        int[] renderbuffers,
        int offset
    );
    public static native void glGenRenderbuffersOES(
        int n,
        java.nio.IntBuffer renderbuffers
    );
    public static native void glRenderbufferStorageOES(
        int target,
        int internalformat,
        int width,
        int height
    );
    public static native void glGetRenderbufferParameterivOES(
        int target,
        int pname,
        int[] params,
        int offset
    );
    public static native void glGetRenderbufferParameterivOES(
        int target,
        int pname,
        java.nio.IntBuffer params
    );
    public static native boolean glIsFramebufferOES(
        int framebuffer
    );
    public static native void glBindFramebufferOES(
        int target,
        int framebuffer
    );
    public static native void glDeleteFramebuffersOES(
        int n,
        int[] framebuffers,
        int offset
    );
    public static native void glDeleteFramebuffersOES(
        int n,
        java.nio.IntBuffer framebuffers
    );
    public static native void glGenFramebuffersOES(
        int n,
        int[] framebuffers,
        int offset
    );
    public static native void glGenFramebuffersOES(
        int n,
        java.nio.IntBuffer framebuffers
    );
    public static native int glCheckFramebufferStatusOES(
        int target
    );
    public static native void glFramebufferRenderbufferOES(
        int target,
        int attachment,
        int renderbuffertarget,
        int renderbuffer
    );
    public static native void glFramebufferTexture2DOES(
        int target,
        int attachment,
        int textarget,
        int texture,
        int level
    );
    public static native void glGetFramebufferAttachmentParameterivOES(
        int target,
        int attachment,
        int pname,
        int[] params,
        int offset
    );
    public static native void glGetFramebufferAttachmentParameterivOES(
        int target,
        int attachment,
        int pname,
        java.nio.IntBuffer params
    );
    public static native void glGenerateMipmapOES(
        int target
    );
    public static native void glCurrentPaletteMatrixOES(
        int matrixpaletteindex
    );
    public static native void glLoadPaletteFromModelViewMatrixOES(
    );
    private static native void glMatrixIndexPointerOESBounds(
        int size,
        int type,
        int stride,
        java.nio.Buffer pointer,
        int remaining
    );
    public static void glMatrixIndexPointerOES(
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
    private static native void glWeightPointerOESBounds(
        int size,
        int type,
        int stride,
        java.nio.Buffer pointer,
        int remaining
    );
    public static void glWeightPointerOES(
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
    public static native void glDepthRangefOES(
        float zNear,
        float zFar
    );
    public static native void glFrustumfOES(
        float left,
        float right,
        float bottom,
        float top,
        float zNear,
        float zFar
    );
    public static native void glOrthofOES(
        float left,
        float right,
        float bottom,
        float top,
        float zNear,
        float zFar
    );
    public static native void glClipPlanefOES(
        int plane,
        float[] equation,
        int offset
    );
    public static native void glClipPlanefOES(
        int plane,
        java.nio.FloatBuffer equation
    );
    public static native void glGetClipPlanefOES(
        int pname,
        float[] eqn,
        int offset
    );
    public static native void glGetClipPlanefOES(
        int pname,
        java.nio.FloatBuffer eqn
    );
    public static native void glClearDepthfOES(
        float depth
    );
    public static native void glTexGenfOES(
        int coord,
        int pname,
        float param
    );
    public static native void glTexGenfvOES(
        int coord,
        int pname,
        float[] params,
        int offset
    );
    public static native void glTexGenfvOES(
        int coord,
        int pname,
        java.nio.FloatBuffer params
    );
    public static native void glTexGeniOES(
        int coord,
        int pname,
        int param
    );
    public static native void glTexGenivOES(
        int coord,
        int pname,
        int[] params,
        int offset
    );
    public static native void glTexGenivOES(
        int coord,
        int pname,
        java.nio.IntBuffer params
    );
    public static native void glTexGenxOES(
        int coord,
        int pname,
        int param
    );
    public static native void glTexGenxvOES(
        int coord,
        int pname,
        int[] params,
        int offset
    );
    public static native void glTexGenxvOES(
        int coord,
        int pname,
        java.nio.IntBuffer params
    );
    public static native void glGetTexGenfvOES(
        int coord,
        int pname,
        float[] params,
        int offset
    );
    public static native void glGetTexGenfvOES(
        int coord,
        int pname,
        java.nio.FloatBuffer params
    );
    public static native void glGetTexGenivOES(
        int coord,
        int pname,
        int[] params,
        int offset
    );
    public static native void glGetTexGenivOES(
        int coord,
        int pname,
        java.nio.IntBuffer params
    );
    public static native void glGetTexGenxvOES(
        int coord,
        int pname,
        int[] params,
        int offset
    );
    public static native void glGetTexGenxvOES(
        int coord,
        int pname,
        java.nio.IntBuffer params
    );
}
