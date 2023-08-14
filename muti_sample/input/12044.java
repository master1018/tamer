public class LinkRef extends Reference {
    static final String linkClassName = LinkRef.class.getName();
    static final String linkAddrType = "LinkAddress";
    public LinkRef(Name linkName) {
        super(linkClassName, new StringRefAddr(linkAddrType, linkName.toString()));
    }
    public LinkRef(String linkName) {
        super(linkClassName, new StringRefAddr(linkAddrType, linkName));
    }
    public String getLinkName() throws NamingException {
        if (className != null && className.equals(linkClassName)) {
            RefAddr addr = get(linkAddrType);
            if (addr != null && addr instanceof StringRefAddr) {
                return (String)((StringRefAddr)addr).getContent();
            }
        }
        throw new MalformedLinkException();
    }
    private static final long serialVersionUID = -5386290613498931298L;
}
