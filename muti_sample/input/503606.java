abstract class GLWrapperBase
    implements GL, GL10, GL10Ext, GL11, GL11Ext {
    public GLWrapperBase(GL gl) {
        mgl = (GL10) gl;
        if (gl instanceof GL10Ext) {
            mgl10Ext = (GL10Ext) gl;
        }
        if (gl instanceof GL11) {
            mgl11 = (GL11) gl;
        }
        if (gl instanceof GL11Ext) {
            mgl11Ext = (GL11Ext) gl;
        }
        if (gl instanceof GL11ExtensionPack) {
            mgl11ExtensionPack = (GL11ExtensionPack) gl;
        }
    }
    protected GL10 mgl;
    protected GL10Ext mgl10Ext;
    protected GL11 mgl11;
    protected GL11Ext mgl11Ext;
    protected GL11ExtensionPack mgl11ExtensionPack;
}
