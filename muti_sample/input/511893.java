abstract  class MemberListParser {
    private final DirectClassFile cf;
    private final CstType definer;
    private final int offset;
    private final AttributeFactory attributeFactory;
    private int endOffset;
    private ParseObserver observer;
    public MemberListParser(DirectClassFile cf, CstType definer,
            int offset, AttributeFactory attributeFactory) {
        if (cf == null) {
            throw new NullPointerException("cf == null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }
        if (attributeFactory == null) {
            throw new NullPointerException("attributeFactory == null");
        }
        this.cf = cf;
        this.definer = definer;
        this.offset = offset;
        this.attributeFactory = attributeFactory;
        this.endOffset = -1;
    }
    public int getEndOffset() {
        parseIfNecessary();
        return endOffset;
    }
    public final void setObserver(ParseObserver observer) {
        this.observer = observer;
    }
    protected final void parseIfNecessary() {
        if (endOffset < 0) {
            parse();
        }
    }
    protected final int getCount() {
        ByteArray bytes = cf.getBytes();
        return bytes.getUnsignedShort(offset);
    }
    protected final CstType getDefiner() {
        return definer;
    }
    protected abstract String humanName();
    protected abstract String humanAccessFlags(int accessFlags);
    protected abstract int getAttributeContext();
    protected abstract Member set(int n, int accessFlags, CstNat nat,
            AttributeList attributes);
    private void parse() {
        int attributeContext = getAttributeContext();
        int count = getCount();
        int at = offset + 2; 
        ByteArray bytes = cf.getBytes();
        ConstantPool pool = cf.getConstantPool();
        if (observer != null) {
            observer.parsed(bytes, offset, 2,
                            humanName() + "s_count: " + Hex.u2(count));
        }
        for (int i = 0; i < count; i++) {
            try {
                int accessFlags = bytes.getUnsignedShort(at);
                int nameIdx = bytes.getUnsignedShort(at + 2);
                int descIdx = bytes.getUnsignedShort(at + 4);
                CstUtf8 name = (CstUtf8) pool.get(nameIdx);
                CstUtf8 desc = (CstUtf8) pool.get(descIdx);
                if (observer != null) {
                    observer.startParsingMember(bytes, at, name.getString(),
                                                desc.getString());
                    observer.parsed(bytes, at, 0, "\n" + humanName() +
                                    "s[" + i + "]:\n");
                    observer.changeIndent(1);
                    observer.parsed(bytes, at, 2,
                                    "access_flags: " +
                                    humanAccessFlags(accessFlags));
                    observer.parsed(bytes, at + 2, 2,
                                    "name: " + name.toHuman());
                    observer.parsed(bytes, at + 4, 2,
                                    "descriptor: " + desc.toHuman());
                }
                at += 6;
                AttributeListParser parser =
                    new AttributeListParser(cf, attributeContext, at,
                                            attributeFactory);
                parser.setObserver(observer);
                at = parser.getEndOffset();
                StdAttributeList attributes = parser.getList();
                attributes.setImmutable();
                CstNat nat = new CstNat(name, desc);
                Member member = set(i, accessFlags, nat, attributes);
                if (observer != null) {
                    observer.changeIndent(-1);
                    observer.parsed(bytes, at, 0, "end " + humanName() +
                                    "s[" + i + "]\n");
                    observer.endParsingMember(bytes, at, name.getString(),
                                              desc.getString(), member);
                }
            } catch (ParseException ex) {
                ex.addContext("...while parsing " + humanName() + "s[" + i +
                              "]");
                throw ex;
            } catch (RuntimeException ex) {
                ParseException pe = new ParseException(ex);
                pe.addContext("...while parsing " + humanName() + "s[" + i +
                              "]");
                throw pe;
            }
        }
        endOffset = at;
    }
}
