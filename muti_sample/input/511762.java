public class LatLongSphere extends Sphere {
    public LatLongSphere(float centerX, float centerY, float centerZ,
        float radius, int lats, int longs,
        float minLongitude, float maxLongitude,
        boolean emitTextureCoordinates,
        boolean emitNormals,
        boolean emitColors,
        boolean flatten) {
        super(emitTextureCoordinates, emitNormals, emitColors);
        int tris = 2 * (lats - 1) * (longs - 1);
        int[] vertices = new int[3 * lats * longs];
        int[] texcoords = new int[2 * lats * longs];
        int[] colors = new int[4 * lats * longs];
        int[] normals = new int[3 * lats * longs];
        short[] indices = new short[3 * tris];
        int vidx = 0;
        int tidx = 0;
        int nidx = 0;
        int cidx = 0;
        int iidx = 0;
        minLongitude *= DEGREES_TO_RADIANS;
        maxLongitude *= DEGREES_TO_RADIANS;
        for (int i = 0; i < longs; i++) {
            float fi = (float) i / (longs - 1);
            float theta =
                (maxLongitude - minLongitude) * (1.0f - fi) + minLongitude;
            float sinTheta = (float) Math.sin(theta);
            float cosTheta = (float) Math.cos(theta);
            for (int j = 0; j < lats; j++) {
                float fj = (float) j / (lats - 1);
                float phi = PI * fj;
                float sinPhi = (float) Math.sin(phi);
                float cosPhi = (float) Math.cos(phi);
                float x = cosTheta * sinPhi;
                float y = cosPhi;
                float z = sinTheta * sinPhi;
                if (flatten) {
                    vertices[vidx++] = toFixed(2.0f * fi - 1.0f);
                    vertices[vidx++] = toFixed(0.5f - fj);
                    vertices[vidx++] = toFixed(0.0f);
                } else {
                    vertices[vidx++] = toFixed(x * radius + centerX);
                    vertices[vidx++] = toFixed(y * radius + centerY);
                    vertices[vidx++] = toFixed(z * radius + centerZ);
                }
                if (emitTextureCoordinates) {
                    texcoords[tidx++] = toFixed(1.0f - (theta / (TWO_PI)));
                    texcoords[tidx++] = toFixed(fj);
                }
                if (emitNormals) {
                    float norm = 1.0f / Shape.length(x, y, z);
                    normals[nidx++] = toFixed(x * norm);
                    normals[nidx++] = toFixed(y * norm);
                    normals[nidx++] = toFixed(z * norm);
                }
                if (emitColors) {
                    colors[cidx++] = (i % 2) * 65536;
                    colors[cidx++] = 0;
                    colors[cidx++] = (j % 2) * 65536;
                    colors[cidx++] = 65536;
                }
            }
        }
        for (int i = 0; i < longs - 1; i++) {
            for (int j = 0; j < lats - 1; j++) {
                int base = i * lats + j;
                indices[iidx++] = (short) (base);
                indices[iidx++] = (short) (base + 1);
                indices[iidx++] = (short) (base + lats + 1);
                indices[iidx++] = (short) (base + lats);
                indices[iidx++] = (short) (base);
                indices[iidx++] = (short) (base + lats + 1);
            }
        }
        allocateBuffers(vertices, texcoords, normals, colors, indices);
    }
}
