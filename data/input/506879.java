public class MatrixPaletteRenderer implements GLSurfaceView.Renderer{
    private Context mContext;
    private Grid mGrid;
    private int mTextureID;
    private static class Grid {
        final static int FLOAT_SIZE = 4;
        final static int CHAR_SIZE = 2;
        final static int VERTEX_SIZE = 8 * FLOAT_SIZE;
        final static int VERTEX_TEXTURE_BUFFER_INDEX_OFFSET = 3;
        final static int VERTEX_WEIGHT_BUFFER_INDEX_OFFSET = 5;
        final static int VERTEX_PALETTE_INDEX_OFFSET = 7 * FLOAT_SIZE;
        private int mVertexBufferObjectId;
        private int mElementBufferObjectId;
        private ByteBuffer mVertexByteBuffer;
        private FloatBuffer mVertexBuffer;
        private CharBuffer mIndexBuffer;
        private int mW;
        private int mH;
        private int mIndexCount;
        public Grid(int w, int h) {
            if (w < 0 || w >= 65536) {
                throw new IllegalArgumentException("w");
            }
            if (h < 0 || h >= 65536) {
                throw new IllegalArgumentException("h");
            }
            if (w * h >= 65536) {
                throw new IllegalArgumentException("w * h >= 65536");
            }
            mW = w;
            mH = h;
            int size = w * h;
            mVertexByteBuffer = ByteBuffer.allocateDirect(VERTEX_SIZE * size)
                .order(ByteOrder.nativeOrder());
            mVertexBuffer = mVertexByteBuffer.asFloatBuffer();
            int quadW = mW - 1;
            int quadH = mH - 1;
            int quadCount = quadW * quadH;
            int indexCount = quadCount * 6;
            mIndexCount = indexCount;
            mIndexBuffer = ByteBuffer.allocateDirect(CHAR_SIZE * indexCount)
                .order(ByteOrder.nativeOrder()).asCharBuffer();
            {
                int i = 0;
                for (int y = 0; y < quadH; y++) {
                    for (int x = 0; x < quadW; x++) {
                        char a = (char) (y * mW + x);
                        char b = (char) (y * mW + x + 1);
                        char c = (char) ((y + 1) * mW + x);
                        char d = (char) ((y + 1) * mW + x + 1);
                        mIndexBuffer.put(i++, a);
                        mIndexBuffer.put(i++, c);
                        mIndexBuffer.put(i++, b);
                        mIndexBuffer.put(i++, b);
                        mIndexBuffer.put(i++, c);
                        mIndexBuffer.put(i++, d);
                    }
                }
            }
        }
        public void set(int i, int j, float x, float y, float z,
                float u, float v,
                float w0, float w1,
                int p0, int p1) {
            if (i < 0 || i >= mW) {
                throw new IllegalArgumentException("i");
            }
            if (j < 0 || j >= mH) {
                throw new IllegalArgumentException("j");
            }
            if (w0 + w1 != 1.0f) {
                throw new IllegalArgumentException("Weights must add up to 1.0f");
            }
            int index = mW * j + i;
            mVertexBuffer.position(index * VERTEX_SIZE / FLOAT_SIZE);
            mVertexBuffer.put(x);
            mVertexBuffer.put(y);
            mVertexBuffer.put(z);
            mVertexBuffer.put(u);
            mVertexBuffer.put(v);
            mVertexBuffer.put(w0);
            mVertexBuffer.put(w1);
            mVertexByteBuffer.position(index * VERTEX_SIZE + VERTEX_PALETTE_INDEX_OFFSET);
            mVertexByteBuffer.put((byte) p0);
            mVertexByteBuffer.put((byte) p1);
        }
        public void createBufferObjects(GL gl) {
            int[] vboIds = new int[2];
            GL11 gl11 = (GL11) gl;
            gl11.glGenBuffers(2, vboIds, 0);
            mVertexBufferObjectId = vboIds[0];
            mElementBufferObjectId = vboIds[1];
            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertexBufferObjectId);
            mVertexByteBuffer.position(0);
            gl11.glBufferData(GL11.GL_ARRAY_BUFFER, mVertexByteBuffer.capacity(), mVertexByteBuffer, GL11.GL_STATIC_DRAW);
            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mElementBufferObjectId);
            mIndexBuffer.position(0);
            gl11.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, mIndexBuffer.capacity() * CHAR_SIZE, mIndexBuffer, GL11.GL_STATIC_DRAW);
            mVertexBuffer = null;
            mVertexByteBuffer = null;
            mIndexBuffer = null;
        }
        public void draw(GL10 gl) {
            GL11 gl11 = (GL11) gl;
            GL11Ext gl11Ext = (GL11Ext) gl;
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertexBufferObjectId);
            gl11.glVertexPointer(3, GL10.GL_FLOAT, VERTEX_SIZE, 0);
            gl11.glTexCoordPointer(2, GL10.GL_FLOAT, VERTEX_SIZE, VERTEX_TEXTURE_BUFFER_INDEX_OFFSET * FLOAT_SIZE);
            gl.glEnableClientState(GL11Ext.GL_MATRIX_INDEX_ARRAY_OES);
            gl.glEnableClientState(GL11Ext.GL_WEIGHT_ARRAY_OES);
            gl11Ext.glWeightPointerOES(2, GL10.GL_FLOAT, VERTEX_SIZE, VERTEX_WEIGHT_BUFFER_INDEX_OFFSET  * FLOAT_SIZE);
            gl11Ext.glMatrixIndexPointerOES(2, GL10.GL_UNSIGNED_BYTE, VERTEX_SIZE, VERTEX_PALETTE_INDEX_OFFSET );
            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mElementBufferObjectId);
            gl11.glDrawElements(GL10.GL_TRIANGLES, mIndexCount, GL10.GL_UNSIGNED_SHORT, 0);
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL11Ext.GL_MATRIX_INDEX_ARRAY_OES);
            gl.glDisableClientState(GL11Ext.GL_WEIGHT_ARRAY_OES);
            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }
    public MatrixPaletteRenderer(Context context) {
        mContext = context;
    }
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glDisable(GL10.GL_DITHER);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                GL10.GL_FASTEST);
        gl.glClearColor(.5f, .5f, .5f, 1);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glEnable(GL10.GL_TEXTURE_2D);
        int[] textures = new int[1];
        gl.glGenTextures(1, textures, 0);
        mTextureID = textures[0];
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
                GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D,
                GL10.GL_TEXTURE_MAG_FILTER,
                GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
                GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
                GL10.GL_CLAMP_TO_EDGE);
        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
                GL10.GL_REPLACE);
        InputStream is = mContext.getResources()
                .openRawResource(R.raw.robot);
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(is);
        } finally {
            try {
                is.close();
            } catch(IOException e) {
            }
        }
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
        mGrid = generateWeightedGrid(gl);
    }
    public void onDrawFrame(GL10 gl) {
        gl.glDisable(GL10.GL_DITHER);
        gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
                GL10.GL_MODULATE);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        GLU.gluLookAt(gl, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glActiveTexture(GL10.GL_TEXTURE0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
                GL10.GL_REPEAT);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
                GL10.GL_REPEAT);
        long time = SystemClock.uptimeMillis() % 4000L;
        double animationUnit = ((double) time) / 4000;
        float unitAngle = (float) Math.cos(animationUnit * 2 * Math.PI);
        float angle = unitAngle * 135f;
        gl.glEnable(GL11Ext.GL_MATRIX_PALETTE_OES);
        gl.glMatrixMode(GL11Ext.GL_MATRIX_PALETTE_OES);
        GL11Ext gl11Ext = (GL11Ext) gl;
        gl11Ext.glCurrentPaletteMatrixOES(0);
        gl11Ext.glLoadPaletteFromModelViewMatrixOES();
        gl.glRotatef(angle, 0, 0, 1.0f);
        gl11Ext.glCurrentPaletteMatrixOES(1);
        gl11Ext.glLoadPaletteFromModelViewMatrixOES();
        mGrid.draw(gl);
        gl.glDisable(GL11Ext.GL_MATRIX_PALETTE_OES);
    }
    public void onSurfaceChanged(GL10 gl, int w, int h) {
        gl.glViewport(0, 0, w, h);
        float ratio = (float) w / h;
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-ratio, ratio, -1, 1, 3, 7);
    }
    private Grid generateWeightedGrid(GL gl) {
        final int uSteps = 20;
        final int vSteps = 20;
        float radius = 0.25f;
        float height = 2.0f;
        Grid grid = new Grid(uSteps + 1, vSteps + 1);
        for (int j = 0; j <= vSteps; j++) {
            for (int i = 0; i <= uSteps; i++) {
                double angle = Math.PI * 2 * i / uSteps;
                float x = radius * (float) Math.cos(angle);
                float y = height * ((float) j / vSteps - 0.5f);
                float z = radius * (float) Math.sin(angle);
                float u = -4.0f * (float) i / uSteps;
                float v = -4.0f * (float) j / vSteps;
                float w0 = (float) j / vSteps;
                float w1 = 1.0f - w0;
                grid.set(i, j, x, y, z, u, v, w0, w1, 0, 1);
            }
        }
        grid.createBufferObjects(gl);
        return grid;
    }
}
