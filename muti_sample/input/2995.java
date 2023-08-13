class DTDBuilder extends DTD {
    static PublicMapping mapping = null;
    private Hashtable namesHash = new Hashtable();
    private Vector namesVector = new Vector();
    protected DTDBuilder(String name) {
        super(name);
    }
    void save(DataOutputStream out, String className) throws IOException {
        out.writeInt(DTD.FILE_VERSION);
        buildNamesTable();
        int numNames = namesVector.size();
        out.writeShort((short) (namesVector.size()));
        for (int i = 0; i < namesVector.size(); i++) {
            String nm = (String) namesVector.elementAt(i);
            out.writeUTF(nm);
        }
        saveEntities(out);
        out.writeShort((short) (elements.size()));
        for (Enumeration e = elements.elements() ; e.hasMoreElements() ; ) {
            saveElement(out, (Element)e.nextElement());
        }
        if (namesVector.size() != numNames) {
            System.err.println("!!! ERROR!  Names were added to the list!");
            Thread.dumpStack();
            System.exit(1);
        }
    }
    private void buildNamesTable() {
        for (Enumeration e = entityHash.elements() ; e.hasMoreElements() ; ) {
            Entity ent = (Entity) e.nextElement();
            getNameId(ent.getName());
        }
        for (Enumeration e = elements.elements() ; e.hasMoreElements() ; ) {
            Element el = (Element) e.nextElement();
            getNameId(el.getName());
            for (AttributeList atts = el.getAttributes() ; atts != null ; atts = atts.getNext()) {
                getNameId(atts.getName());
                if (atts.getValue() != null) {
                    getNameId(atts.getValue());
                }
                Enumeration vals = atts.getValues();
                while (vals != null && vals.hasMoreElements()) {
                    String s = (String) vals.nextElement();
                    getNameId(s);
                }
            }
        }
    }
    private short getNameId(String name)  {
        Object o = namesHash.get(name);
        if (o != null) {
            return (short) ((Integer) o).intValue();
        }
        int i = namesVector.size();
        namesVector.addElement(name);
        namesHash.put(name, new Integer(i));
        return (short) i;
    }
    void saveEntities(DataOutputStream out) throws IOException {
        int num = 0;
        for (Enumeration e = entityHash.elements() ; e.hasMoreElements() ; ) {
            Entity ent = (Entity) e.nextElement();
            if (ent.isGeneral()) {
                num++;
            }
        }
        out.writeShort((short) num);
        for (Enumeration e = entityHash.elements() ; e.hasMoreElements() ; ) {
            Entity ent = (Entity) e.nextElement();
            if (ent.isGeneral()) {
                out.writeShort(getNameId(ent.getName()));
                out.writeByte(ent.getType() & ~GENERAL);
                out.writeUTF(ent.getString());
            }
        }
    }
    public void saveElement(DataOutputStream out, Element elem) throws IOException {
        out.writeShort(getNameId(elem.getName()));
        out.writeByte(elem.getType());
        byte flags = 0;
        if (elem.omitStart()) {
            flags |= 0x01;
        }
        if (elem.omitEnd()) {
            flags |= 0x02;
        }
        out.writeByte(flags);
        saveContentModel(out, elem.getContent());
        if (elem.exclusions == null) {
            out.writeShort(0);
        } else {
            short num = 0;
            for (int i = 0 ; i < elem.exclusions.size() ; i++) {
                if (elem.exclusions.get(i)) {
                    num++;
                }
            }
            out.writeShort(num);
            for (int i = 0 ; i < elem.exclusions.size() ; i++) {
                if (elem.exclusions.get(i)) {
                    out.writeShort(getNameId(getElement(i).getName()));
                }
            }
        }
        if (elem.inclusions == null) {
            out.writeShort(0);
        } else {
            short num = 0;
            for (int i = 0 ; i < elem.inclusions.size() ; i++) {
                if (elem.inclusions.get(i)) {
                    num++;
                }
            }
            out.writeShort(num);
            for (int i = 0 ; i < elem.inclusions.size() ; i++) {
                if (elem.inclusions.get(i)) {
                    out.writeShort(getNameId(getElement(i).getName()));
                }
            }
        }
        {
            short numAtts = 0;
            for (AttributeList atts = elem.getAttributes() ; atts != null ; atts = atts.getNext()) {
                numAtts++;
            }
            out.writeByte(numAtts);
            for (AttributeList atts = elem.getAttributes() ; atts != null ; atts = atts.getNext()) {
                out.writeShort(getNameId(atts.getName()));
                out.writeByte(atts.getType());
                out.writeByte(atts.getModifier());
                if (atts.getValue() == null) {
                    out.writeShort(-1);
                } else {
                    out.writeShort(getNameId(atts.getValue()));
                }
                if (atts.values == null) {
                    out.writeShort(0);
                } else {
                    out.writeShort((short) atts.values.size());
                    for (int i = 0; i < atts.values.size(); i++) {
                        String s = (String) atts.values.elementAt(i);
                        out.writeShort(getNameId(s));
                    }
                }
            }
        }
    }
    public void saveContentModel(DataOutputStream out, ContentModel model) throws IOException {
        if (model == null) {
            out.writeByte(0);
        } else if (model.content instanceof ContentModel) {
            out.writeByte(1);
            out.writeByte(model.type);
            saveContentModel(out, (ContentModel)model.content);
            saveContentModel(out, model.next);
        } else if (model.content instanceof Element) {
            out.writeByte(2);
            out.writeByte(model.type);
            out.writeShort(getNameId(((Element) model.content).getName()));
            saveContentModel(out, model.next);
        }
    }
    public static void main(String argv[]) {
        String dtd_home = System.getProperty("dtd_home") + File.separator;
        if (dtd_home == null) {
            System.err.println("Must set property 'dtd_home'");
            return;
        }
        DTDBuilder dtd = null;
        try {
            dtd = new DTDBuilder(argv[0]);
            mapping = new PublicMapping(dtd_home, "public.map");
            String path = mapping.get(argv[0]);
            new DTDParser().parse(new FileInputStream(path), dtd);
        } catch (IOException e) {
            System.err.println("Could not open DTD file "+argv[0]);
            e.printStackTrace(System.err);
            System.exit(1);
        }
        try {
            DataOutputStream str = new DataOutputStream(System.out);
            dtd.save(str, argv[0]);
            str.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
