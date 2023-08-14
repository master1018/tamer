class Entity implements DTDConstants {
    public String name;
    public int type;
    public char data[];
    public Entity(String name, int type, char data[]) {
        this.name = name;
        this.type = type;
        this.data = data;
    }
    public String getName() {
        return name;
    }
    public int getType() {
        return type & 0xFFFF;
    }
    public boolean isParameter() {
        return (type & PARAMETER) != 0;
    }
    public boolean isGeneral() {
        return (type & GENERAL) != 0;
    }
    public char getData()[] {
        return data;
    }
    public String getString() {
        return new String(data, 0, data.length);
    }
    static Hashtable<String, Integer> entityTypes = new Hashtable<String, Integer>();
    static {
        entityTypes.put("PUBLIC", Integer.valueOf(PUBLIC));
        entityTypes.put("CDATA", Integer.valueOf(CDATA));
        entityTypes.put("SDATA", Integer.valueOf(SDATA));
        entityTypes.put("PI", Integer.valueOf(PI));
        entityTypes.put("STARTTAG", Integer.valueOf(STARTTAG));
        entityTypes.put("ENDTAG", Integer.valueOf(ENDTAG));
        entityTypes.put("MS", Integer.valueOf(MS));
        entityTypes.put("MD", Integer.valueOf(MD));
        entityTypes.put("SYSTEM", Integer.valueOf(SYSTEM));
    }
    public static int name2type(String nm) {
        Integer i = entityTypes.get(nm);
        return (i == null) ? CDATA : i.intValue();
    }
}
