public class Visibility {
    public static native int visibilityTest(float[] ws, int wsOffset,
            float[] positions, int positionsOffset, char[] indices,
            int indicesOffset, int indexCount);
    public static native int frustumCullSpheres(float[] mvp, int mvpOffset,
            float[] spheres, int spheresOffset, int spheresCount,
            int[] results, int resultsOffset, int resultsCapacity);
    public static native void computeBoundingSphere(float[] positions,
            int positionsOffset, int positionsCount, float[] sphere,
            int sphereOffset);
}
