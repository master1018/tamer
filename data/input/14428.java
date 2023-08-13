class IdentifierToken {
    long where;
    int modifiers;
    Identifier id;
    public IdentifierToken(long where, Identifier id) {
        this.where = where;
        this.id = id;
    }
    public IdentifierToken(Identifier id) {
        this.where = 0;
        this.id = id;
    }
    public IdentifierToken(long where, Identifier id, int modifiers) {
        this.where = where;
        this.id = id;
        this.modifiers = modifiers;
    }
    public long getWhere() {
        return where;
    }
    public Identifier getName() {
        return id;
    }
    public int getModifiers() {
        return modifiers;
    }
    public String toString() {
        return id.toString();
    }
    public static long getWhere(IdentifierToken id, long defaultWhere) {
        return (id != null && id.where != 0) ? id.where : defaultWhere;
    }
}
