public class Matrix2f {
    public Matrix2f() {
        mMat = new float[4];
        loadIdentity();
    }
    public float get(int i, int j) {
        return mMat[i*2 + j];
    }
    public void set(int i, int j, float v) {
        mMat[i*2 + j] = v;
    }
    public void loadIdentity() {
        mMat[0] = 1;
        mMat[1] = 0;
        mMat[2] = 0;
        mMat[3] = 1;
    }
    public void load(Matrix2f src) {
        System.arraycopy(mMat, 0, src, 0, 4);
    }
    final float[] mMat;
}
