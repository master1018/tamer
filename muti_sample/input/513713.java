class MaterialIndices {
    private Material material = null;
    private ShortBuffer indexBuffer = null;
    public MaterialIndices(Material material, ShortBuffer indexBuffer) {
        this.material = material;
        this.indexBuffer = indexBuffer;
    }
    public Material getMaterial() {
        return material;
    }
    public ShortBuffer getIndexBuffer() {
        return indexBuffer;
    }
}
public class Group {
    private Object3D parent;
    private String name;
    private List<MaterialIndices> materialIndices =
        new ArrayList<MaterialIndices>();
    public Group(Object3D parent) {
        this.parent = parent;
    }
    public String getName() {
        return name;
    }
    public void load(DataInputStream dis) throws IOException {
        dis.readInt(); 
        this.name = dis.readUTF();
        int numMaterials = dis.readInt();
        for (int i = 0; i < numMaterials; i++) {
            dis.readInt(); 
            String matName = dis.readUTF();
            Material material = parent.getMaterial(matName);
            int numIndices = dis.readInt();
            byte[] indicesBytes = new byte[numIndices * 2];
            dis.readFully(indicesBytes);
            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                int idx = 0;
                for (int j = 0; j < numIndices; j++) {
                    byte b0 = indicesBytes[idx];
                    byte b1 = indicesBytes[idx + 1];
                    indicesBytes[idx] = b1;
                    indicesBytes[idx + 1] = b0;
                    idx += 2;
                }
            }
            ByteBuffer ibb = ByteBuffer.allocateDirect(2*numIndices);
            ibb.order(ByteOrder.nativeOrder());
            ibb.put(indicesBytes);
            ibb.position(0);
            ShortBuffer sb = ibb.asShortBuffer();
            materialIndices.add(new MaterialIndices(material, sb));
        }
    }
    public int getNumTriangles() {
        int numTriangles = 0;
        Iterator<MaterialIndices> iter = materialIndices.iterator();
        while (iter.hasNext()) {
            MaterialIndices matIdx = iter.next();
            ShortBuffer indexBuffer = matIdx.getIndexBuffer();
            numTriangles += indexBuffer.capacity()/3;
        }
        return numTriangles;
    }
    public void draw(GL10 gl) {
        gl.glDisableClientState(gl.GL_COLOR_ARRAY);
        gl.glVertexPointer(3, gl.GL_FIXED, 0, parent.getVertexBuffer());
        gl.glEnableClientState(gl.GL_VERTEX_ARRAY);
        gl.glNormalPointer(gl.GL_FIXED, 0, parent.getNormalBuffer());
        gl.glEnableClientState(gl.GL_NORMAL_ARRAY);
        if (parent.hasTexcoords()) {
            gl.glTexCoordPointer(2, gl.GL_FIXED, 0, parent.getTexcoordBuffer());
            gl.glEnableClientState(gl.GL_TEXTURE_COORD_ARRAY);
            gl.glEnable(gl.GL_TEXTURE_2D);
        } else {
            gl.glDisable(gl.GL_TEXTURE_2D);
        }
        Iterator<MaterialIndices> iter = materialIndices.iterator();
        while (iter.hasNext()) {
            MaterialIndices matIdx = iter.next();
            ShortBuffer indexBuffer = matIdx.getIndexBuffer();
            Material mat = matIdx.getMaterial();
            mat.setMaterialParameters(gl);
            if (parent.hasTexcoords() && mat.getMap_Kd().length() > 0) {
                Texture texture = parent.getTexture(mat.getMap_Kd());
                texture.setTextureParameters(gl);
            }
            gl.glDrawElements(gl.GL_TRIANGLES,
                    indexBuffer.capacity(),
                    gl.GL_UNSIGNED_SHORT,
                    indexBuffer);
        }
    }
    public String toString() {
        return "Group[" +
        "name=" + name +
        "]";
    }
}
