public class Sphere extends Shape {
    public Sphere(boolean emitTextureCoordinates,
        boolean emitNormals, boolean emitColors) {
        super(GL10.GL_TRIANGLES, GL10.GL_UNSIGNED_SHORT,
              emitTextureCoordinates, emitNormals, emitColors);
    }
}
