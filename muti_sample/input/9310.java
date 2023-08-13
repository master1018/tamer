class DTD implements DTDConstants {
    public String name;
    public Vector<Element> elements = new Vector<Element>();
    public Hashtable<String,Element> elementHash
        = new Hashtable<String,Element>();
    public Hashtable<Object,Entity> entityHash
        = new Hashtable<Object,Entity>();
    public final Element pcdata = getElement("#pcdata");
    public final Element html = getElement("html");
    public final Element meta = getElement("meta");
    public final Element base = getElement("base");
    public final Element isindex = getElement("isindex");
    public final Element head = getElement("head");
    public final Element body = getElement("body");
    public final Element applet = getElement("applet");
    public final Element param = getElement("param");
    public final Element p = getElement("p");
    public final Element title = getElement("title");
    final Element style = getElement("style");
    final Element link = getElement("link");
    final Element script = getElement("script");
    public static final int FILE_VERSION = 1;
    protected DTD(String name) {
        this.name = name;
        defEntity("#RE", GENERAL, '\r');
        defEntity("#RS", GENERAL, '\n');
        defEntity("#SPACE", GENERAL, ' ');
        defineElement("unknown", EMPTY, false, true, null, null, null, null);
    }
    public String getName() {
        return name;
    }
    public Entity getEntity(String name) {
        return entityHash.get(name);
    }
    public Entity getEntity(int ch) {
        return entityHash.get(Integer.valueOf(ch));
    }
    boolean elementExists(String name) {
        return !"unknown".equals(name) && (elementHash.get(name) != null);
    }
    public Element getElement(String name) {
        Element e = elementHash.get(name);
        if (e == null) {
            e = new Element(name, elements.size());
            elements.addElement(e);
            elementHash.put(name, e);
        }
        return e;
    }
    public Element getElement(int index) {
        return elements.elementAt(index);
    }
    public Entity defineEntity(String name, int type, char data[]) {
        Entity ent = entityHash.get(name);
        if (ent == null) {
            ent = new Entity(name, type, data);
            entityHash.put(name, ent);
            if (((type & GENERAL) != 0) && (data.length == 1)) {
                switch (type & ~GENERAL) {
                  case CDATA:
                  case SDATA:
                      entityHash.put(Integer.valueOf(data[0]), ent);
                    break;
                }
            }
        }
        return ent;
    }
    public Element defineElement(String name, int type,
                       boolean omitStart, boolean omitEnd, ContentModel content,
                       BitSet exclusions, BitSet inclusions, AttributeList atts) {
        Element e = getElement(name);
        e.type = type;
        e.oStart = omitStart;
        e.oEnd = omitEnd;
        e.content = content;
        e.exclusions = exclusions;
        e.inclusions = inclusions;
        e.atts = atts;
        return e;
    }
    public void defineAttributes(String name, AttributeList atts) {
        Element e = getElement(name);
        e.atts = atts;
    }
    public Entity defEntity(String name, int type, int ch) {
        char data[] = {(char)ch};
        return defineEntity(name, type, data);
    }
    protected Entity defEntity(String name, int type, String str) {
        int len = str.length();
        char data[] = new char[len];
        str.getChars(0, len, data, 0);
        return defineEntity(name, type, data);
    }
    protected Element defElement(String name, int type,
                       boolean omitStart, boolean omitEnd, ContentModel content,
                       String[] exclusions, String[] inclusions, AttributeList atts) {
        BitSet excl = null;
        if (exclusions != null && exclusions.length > 0) {
            excl = new BitSet();
            for (String str : exclusions) {
                if (str.length() > 0) {
                    excl.set(getElement(str).getIndex());
                }
            }
        }
        BitSet incl = null;
        if (inclusions != null && inclusions.length > 0) {
            incl = new BitSet();
            for (String str : inclusions) {
                if (str.length() > 0) {
                    incl.set(getElement(str).getIndex());
                }
            }
        }
        return defineElement(name, type, omitStart, omitEnd, content, excl, incl, atts);
    }
    protected AttributeList defAttributeList(String name, int type, int modifier, String value, String values, AttributeList atts) {
        Vector<String> vals = null;
        if (values != null) {
            vals = new Vector<String>();
            for (StringTokenizer s = new StringTokenizer(values, "|") ; s.hasMoreTokens() ;) {
                String str = s.nextToken();
                if (str.length() > 0) {
                    vals.addElement(str);
                }
            }
        }
        return new AttributeList(name, type, modifier, value, vals, atts);
    }
    protected ContentModel defContentModel(int type, Object obj, ContentModel next) {
        return new ContentModel(type, obj, next);
    }
    public String toString() {
        return name;
    }
    private static final Object DTD_HASH_KEY = new Object();
    public static void putDTDHash(String name, DTD dtd) {
        getDtdHash().put(name, dtd);
    }
    public static DTD getDTD(String name) throws IOException {
        name = name.toLowerCase();
        DTD dtd = getDtdHash().get(name);
        if (dtd == null)
          dtd = new DTD(name);
        return dtd;
    }
    private static Hashtable<String, DTD> getDtdHash() {
        AppContext appContext = AppContext.getAppContext();
        Hashtable<String, DTD> result = (Hashtable<String, DTD>) appContext.get(DTD_HASH_KEY);
        if (result == null) {
            result = new Hashtable<String, DTD>();
            appContext.put(DTD_HASH_KEY, result);
        }
        return result;
    }
    public void read(DataInputStream in) throws IOException {
        if (in.readInt() != FILE_VERSION) {
        }
        String[] names = new String[in.readShort()];
        for (int i = 0; i < names.length; i++) {
            names[i] = in.readUTF();
        }
        int num = in.readShort();
        for (int i = 0; i < num; i++) {
            short nameId = in.readShort();
            int type = in.readByte();
            String name = in.readUTF();
            defEntity(names[nameId], type | GENERAL, name);
        }
        num = in.readShort();
        for (int i = 0; i < num; i++) {
            short nameId = in.readShort();
            int type = in.readByte();
            byte flags = in.readByte();
            ContentModel m = readContentModel(in, names);
            String[] exclusions = readNameArray(in, names);
            String[] inclusions = readNameArray(in, names);
            AttributeList atts = readAttributeList(in, names);
            defElement(names[nameId], type,
                       ((flags & 0x01) != 0), ((flags & 0x02) != 0),
                       m, exclusions, inclusions, atts);
        }
    }
    private ContentModel readContentModel(DataInputStream in, String[] names)
                throws IOException {
        byte flag = in.readByte();
        switch(flag) {
            case 0:             
                return null;
            case 1: {           
                int type = in.readByte();
                ContentModel m = readContentModel(in, names);
                ContentModel next = readContentModel(in, names);
                return defContentModel(type, m, next);
            }
            case 2: {           
                int type = in.readByte();
                Element el = getElement(names[in.readShort()]);
                ContentModel next = readContentModel(in, names);
                return defContentModel(type, el, next);
            }
        default:
                throw new IOException("bad bdtd");
        }
    }
    private String[] readNameArray(DataInputStream in, String[] names)
                throws IOException {
        int num = in.readShort();
        if (num == 0) {
            return null;
        }
        String[] result = new String[num];
        for (int i = 0; i < num; i++) {
            result[i] = names[in.readShort()];
        }
        return result;
    }
    private AttributeList readAttributeList(DataInputStream in, String[] names)
                throws IOException  {
        AttributeList result = null;
        for (int num = in.readByte(); num > 0; --num) {
            short nameId = in.readShort();
            int type = in.readByte();
            int modifier = in.readByte();
            short valueId = in.readShort();
            String value = (valueId == -1) ? null : names[valueId];
            Vector<String> values = null;
            short numValues = in.readShort();
            if (numValues > 0) {
                values = new Vector<String>(numValues);
                for (int i = 0; i < numValues; i++) {
                    values.addElement(names[in.readShort()]);
                }
            }
result = new AttributeList(names[nameId], type, modifier, value,
                                       values, result);
        }
        return result;
    }
}
