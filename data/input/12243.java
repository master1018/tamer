public final class Fidelity extends EnumSyntax
        implements PrintJobAttribute, PrintRequestAttribute {
    private static final long serialVersionUID = 6320827847329172308L;
    public static final Fidelity
        FIDELITY_TRUE = new Fidelity(0);
    public static final Fidelity
        FIDELITY_FALSE = new Fidelity(1);
    protected Fidelity(int value) {
        super (value);
    }
    private static final String[] myStringTable = {
        "true",
        "false"
    };
    private static final Fidelity[] myEnumValueTable = {
        FIDELITY_TRUE,
        FIDELITY_FALSE
    };
    protected String[] getStringTable() {
        return myStringTable;
    }
    protected EnumSyntax[] getEnumValueTable() {
        return myEnumValueTable;
    }   
    public final Class<? extends Attribute> getCategory() {
        return Fidelity.class;
    }
    public final String getName() {
        return "ipp-attribute-fidelity";
    }
}
