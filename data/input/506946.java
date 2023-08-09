public class Material {
    private Object3D parent;
    private String name;
    private String map_kd;
    private float[] ka = new float[4];
    private float[] kd = new float[4];
    private float[] ks = new float[4];
    private float ns;
    private int illum;
    private float d;
    private static float[] black = { 0.0f, 0.0f, 0.0f, 1.0f };
    public Material(Object3D parent) {
        this.parent = parent;
    }
    public String getName() {
        return name;
    }
    public String getMap_Kd() {
        return map_kd;
    }
    public void setMaterialParameters(GL10 gl) {
        gl.glMaterialfv(gl.GL_FRONT_AND_BACK, gl.GL_AMBIENT, kd, 0);
        gl.glMaterialfv(gl.GL_FRONT_AND_BACK, gl.GL_DIFFUSE, kd, 0);
        gl.glMaterialfv(gl.GL_FRONT_AND_BACK, gl.GL_SPECULAR, ks, 0);
        gl.glMaterialf(gl.GL_FRONT_AND_BACK, gl.GL_SHININESS,
                Math.min(Math.max(ns, 0), 128));
    }
    public void load(DataInputStream dis) throws IOException {
        dis.readInt(); 
        this.name = dis.readUTF();
        dis.readInt(); 
        this.map_kd = dis.readUTF();
        if (parent.hasTexcoords() && map_kd.length() > 0) {
            parent.loadTexture(map_kd);
        }
        this.ka[0] = dis.readFloat();
        this.ka[1] = dis.readFloat();
        this.ka[2] = dis.readFloat();
        this.ka[3] = dis.readFloat();
        this.kd[0] = dis.readFloat();
        this.kd[1] = dis.readFloat();
        this.kd[2] = dis.readFloat();
        this.kd[3] = dis.readFloat();
        this.ks[0] = dis.readFloat();
        this.ks[1] = dis.readFloat();
        this.ks[2] = dis.readFloat();
        this.ks[3] = dis.readFloat();
        this.ns = dis.readFloat();
        this.illum = dis.readInt();
        this.d = dis.readFloat();
    }
    public String toString() {
        return "Material[" +
        "name=\"" + name + "\"," +
        "ka={" + ka[0] + "," + ka[1] + "," + ka[2] + "}," +
        "kd={" + kd[0] + "," + kd[1] + "," + kd[2] + "}," +
        "ks={" + ks[0] + "," + ks[1] + "," + ks[2] + "}," +
        "ns=" + ns + "," +
        "map_kd=\"" + 
        (map_kd == null ? "" : map_kd) +
        "\"," +
        "illum=" + illum + "," +
        "d=" + d +
        "]";
    }
}
