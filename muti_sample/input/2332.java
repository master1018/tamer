class NameValueNode extends NameNode {
    String val;
    NameValueNode(String name, String val) {
        super(name);
        this.val = val;
    }
    NameValueNode(String name, int ival) {
        super(name);
        this.val = Integer.toString(ival);
    }
    String value() {
        return val;
    }
    public String toString() {
        return name + "=" + val;
    }
}
