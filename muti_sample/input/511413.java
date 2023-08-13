public class PointCloud extends Shape {
    public PointCloud(int[] vertices) {
        this(vertices, 0, vertices.length);
    }
    public PointCloud(int[] vertices, int off, int len) {
        super(GL10.GL_POINTS, GL10.GL_UNSIGNED_SHORT,
              false, false, false);
        int numPoints = len / 3;
        short[] indices = new short[numPoints];
        for (int i = 0; i < numPoints; i++) {
            indices[i] = (short)i;
        }
        allocateBuffers(vertices, null, null, null, indices);
        this.mNumIndices = mIndexBuffer.capacity();
    }
    @Override public int getNumTriangles() {
        return mNumIndices * 2;
    }
}
