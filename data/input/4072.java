public class LinkOutputImpl implements LinkOutput {
    public StringBuffer output;
    public LinkOutputImpl() {
        output = new StringBuffer();
    }
    public void append(Object o) {
        output.append(o instanceof String ?
            (String) o : ((LinkOutputImpl)o).toString());
    }
    public String toString() {
        return output.toString();
    }
}
