public class StringRefAddr extends RefAddr {
    private String contents;
    public StringRefAddr(String addrType, String addr) {
        super(addrType);
        contents = addr;
    }
    public Object getContent() {
        return contents;
    }
    private static final long serialVersionUID = -8913762495138505527L;
}
