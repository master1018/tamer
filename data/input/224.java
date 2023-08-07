public class LineInfo {
    public LineInfo() {
    }
    public LineInfo(final byte token, final Object obj) {
        this.token = token;
        this.obj = obj;
    }
    private byte token;
    private Object obj;
    public byte getToken() {
        return token;
    }
    public void setToken(final byte token) {
        this.token = token;
    }
    public Object getObj() {
        return obj;
    }
    public void setObj(final Object obj) {
        this.obj = obj;
    }
}
