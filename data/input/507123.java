public class Matrix3f {
    public Matrix3f() {
        mMat = new float[9];
        loadIdentity();
    }
    public float get(int i, int j) {
        return mMat[i*3 + j];
    }
    public void set(int i, int j, float v) {
        mMat[i*3 + j] = v;
    }
    public void loadIdentity() {
        mMat[0] = 1;
        mMat[1] = 0;
        mMat[2] = 0;
        mMat[3] = 0;
        mMat[4] = 1;
        mMat[5] = 0;
        mMat[6] = 0;
        mMat[7] = 0;
        mMat[8] = 1;
    }
    public void load(Matrix3f src) {
        System.arraycopy(mMat, 0, src, 0, 9);
    }
    final float[] mMat;
}
