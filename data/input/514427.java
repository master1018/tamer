public class Annulus extends Shape {
    public Annulus(float centerX, float centerY, float Z,
        float innerRadius, float outerRadius,
        float rInner, float gInner, float bInner, float aInner,
        float rOuter, float gOuter, float bOuter, float aOuter,
        int sectors) {
        super(GL10.GL_TRIANGLES, GL10.GL_UNSIGNED_SHORT,
              false, false, true);
        int radii = sectors + 1;
        int[] vertices = new int[2 * 3 * radii];
        int[] colors = new int[2 * 4 * radii];
        short[] indices = new short[2 * 3 * radii];
        int vidx = 0;
        int cidx = 0;
        int iidx = 0;
        for (int i = 0; i < radii; i++) {
            float theta = (i * TWO_PI) / (radii - 1);
            float cosTheta = (float) Math.cos(theta);
            float sinTheta = (float) Math.sin(theta);
            vertices[vidx++] = toFixed(centerX + innerRadius * cosTheta);
            vertices[vidx++] = toFixed(centerY + innerRadius * sinTheta);
            vertices[vidx++] = toFixed(Z);
            vertices[vidx++] = toFixed(centerX + outerRadius * cosTheta);
            vertices[vidx++] = toFixed(centerY + outerRadius * sinTheta);
            vertices[vidx++] = toFixed(Z);
            colors[cidx++] = toFixed(rInner);
            colors[cidx++] = toFixed(gInner);
            colors[cidx++] = toFixed(bInner);
            colors[cidx++] = toFixed(aInner);
            colors[cidx++] = toFixed(rOuter);
            colors[cidx++] = toFixed(gOuter);
            colors[cidx++] = toFixed(bOuter);
            colors[cidx++] = toFixed(aOuter);
        }
        for (int i = 0; i < sectors; i++) {
            indices[iidx++] = (short) (2 * i);
            indices[iidx++] = (short) (2 * i + 1);
            indices[iidx++] = (short) (2 * i + 2);
            indices[iidx++] = (short) (2 * i + 1);
            indices[iidx++] = (short) (2 * i + 3);
            indices[iidx++] = (short) (2 * i + 2);
        }
        allocateBuffers(vertices, null, null, colors, indices);
    }
}
