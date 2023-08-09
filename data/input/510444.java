final class GridQuadMesh {
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTexCoordBuffer;
    private CharBuffer mIndexBuffer;
    private int mW;
    private int mH;
    private int mIndexCount;
    private int mVertBufferIndex;
    private int mIndexBufferIndex;
    private int mTextureCoordBufferIndex;
    public GridQuadMesh(int vertsAcross, int vertsDown) {
        if (vertsAcross < 0 || vertsAcross >= 65536) {
            throw new IllegalArgumentException("vertsAcross");
        }
        if (vertsDown < 0 || vertsDown >= 65536) {
            throw new IllegalArgumentException("vertsDown");
        }
        if (vertsAcross * vertsDown >= 65536) {
            throw new IllegalArgumentException("vertsAcross * vertsDown >= 65536");
        }
        mW = vertsAcross;
        mH = vertsDown;
        int size = vertsAcross * vertsDown;
        final int FLOAT_SIZE = 4;
        final int CHAR_SIZE = 2;
        mVertexBuffer = ByteBuffer.allocateDirect(FLOAT_SIZE * size * 3).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTexCoordBuffer = ByteBuffer.allocateDirect(FLOAT_SIZE * size * 2).order(ByteOrder.nativeOrder()).asFloatBuffer();
        int quadW = mW - 1;
        int quadH = mH - 1;
        int quadCount = quadW * quadH;
        int indexCount = quadCount * 6;
        mIndexCount = indexCount;
        mIndexBuffer = ByteBuffer.allocateDirect(CHAR_SIZE * indexCount).order(ByteOrder.nativeOrder()).asCharBuffer();
        {
            int i = 0;
            for (int y = 0; y < quadH; y++) {
                for (int x = 0; x < quadW; x++) {
                    char a = (char) (y * mW + x);
                    char b = (char) (y * mW + x + 1);
                    char c = (char) ((y + 1) * mW + x);
                    char d = (char) ((y + 1) * mW + x + 1);
                    mIndexBuffer.put(i++, a);
                    mIndexBuffer.put(i++, b);
                    mIndexBuffer.put(i++, c);
                    mIndexBuffer.put(i++, b);
                    mIndexBuffer.put(i++, c);
                    mIndexBuffer.put(i++, d);
                }
            }
        }
        mVertBufferIndex = 0;
    }
    void set(int i, int j, float x, float y, float z, float u, float v) {
        if (i < 0 || i >= mW) {
            throw new IllegalArgumentException("i");
        }
        if (j < 0 || j >= mH) {
            throw new IllegalArgumentException("j");
        }
        int index = mW * j + i;
        int posIndex = index * 3;
        mVertexBuffer.put(posIndex, x);
        mVertexBuffer.put(posIndex + 1, y);
        mVertexBuffer.put(posIndex + 2, z);
        int texIndex = index * 2;
        mTexCoordBuffer.put(texIndex, u);
        mTexCoordBuffer.put(texIndex + 1, v);
    }
    public void draw(GL10 gl, boolean useTexture) {
        if (mVertBufferIndex == 0) {
            gl.glVertexPointer(3, GL11.GL_FLOAT, 0, mVertexBuffer);
            if (useTexture) {
                gl.glTexCoordPointer(2, GL11.GL_FLOAT, 0, mTexCoordBuffer);
            }
            gl.glDrawElements(GL11.GL_TRIANGLES, mIndexCount, GL11.GL_UNSIGNED_SHORT, mIndexBuffer);
        } else {
            GL11 gl11 = (GL11) gl;
            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertBufferIndex);
            gl11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mTextureCoordBufferIndex);
            gl11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mIndexBufferIndex);
            gl11.glDrawElements(GL11.GL_TRIANGLES, mIndexCount, GL11.GL_UNSIGNED_SHORT, 0);
            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }
    public boolean usingHardwareBuffers() {
        return mVertBufferIndex != 0;
    }
    public void forgetHardwareBuffers() {
        mVertBufferIndex = 0;
        mIndexBufferIndex = 0;
        mTextureCoordBufferIndex = 0;
    }
    public void freeHardwareBuffers(GL10 gl) {
        if (mVertBufferIndex != 0) {
            if (gl instanceof GL11) {
                GL11 gl11 = (GL11) gl;
                int[] buffer = new int[1];
                buffer[0] = mVertBufferIndex;
                gl11.glDeleteBuffers(1, buffer, 0);
                buffer[0] = mTextureCoordBufferIndex;
                gl11.glDeleteBuffers(1, buffer, 0);
                buffer[0] = mIndexBufferIndex;
                gl11.glDeleteBuffers(1, buffer, 0);
            }
            forgetHardwareBuffers();
        }
    }
    public void generateHardwareBuffers(GL10 gl) {
        if (mVertBufferIndex == 0) {
            if (gl instanceof GL11) {
                GL11 gl11 = (GL11) gl;
                int[] buffer = new int[1];
                gl11.glGenBuffers(1, buffer, 0);
                mVertBufferIndex = buffer[0];
                gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertBufferIndex);
                final int vertexSize = mVertexBuffer.capacity() * 4;
                mVertexBuffer.position(0);
                gl11.glBufferData(GL11.GL_ARRAY_BUFFER, vertexSize, mVertexBuffer, GL11.GL_STATIC_DRAW);
                gl11.glGenBuffers(1, buffer, 0);
                mTextureCoordBufferIndex = buffer[0];
                gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mTextureCoordBufferIndex);
                final int texCoordSize = mTexCoordBuffer.capacity() * 4;
                mTexCoordBuffer.position(0);
                gl11.glBufferData(GL11.GL_ARRAY_BUFFER, texCoordSize, mTexCoordBuffer, GL11.GL_STATIC_DRAW);
                gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
                gl11.glGenBuffers(1, buffer, 0);
                mIndexBufferIndex = buffer[0];
                gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mIndexBufferIndex);
                final int indexSize = mIndexBuffer.capacity() * 2;
                mIndexBuffer.position(0);
                gl11.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, indexSize, mIndexBuffer, GL11.GL_STATIC_DRAW);
                gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
            }
        }
    }
}
