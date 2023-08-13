public final class DialogTypeSelection extends EnumSyntax
        implements PrintRequestAttribute {
    private static final long serialVersionUID = 7518682952133256029L;
    public static final DialogTypeSelection
        NATIVE = new DialogTypeSelection(0);
    public static final DialogTypeSelection
        COMMON = new DialogTypeSelection(1);
    protected DialogTypeSelection(int value) {
                super(value);
    }
    private static final String[] myStringTable = {
        "native", "common"};
    private static final DialogTypeSelection[] myEnumValueTable = {
        NATIVE,
        COMMON
    };
    protected String[] getStringTable() {
        return myStringTable;
    }
    protected EnumSyntax[] getEnumValueTable() {
        return myEnumValueTable;
    }
    public final Class getCategory() {
        return DialogTypeSelection.class;
    }
    public final String getName() {
        return "dialog-type-selection";
    }
}
