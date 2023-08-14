public abstract class Media extends EnumSyntax
    implements DocAttribute, PrintRequestAttribute, PrintJobAttribute {
    private static final long serialVersionUID = -2823970704630722439L;
    protected Media(int value) {
           super (value);
    }
    public boolean equals(Object object) {
        return(object != null && object instanceof Media &&
               object.getClass() == this.getClass() &&
               ((Media)object).getValue() == this.getValue());
    }
    public final Class<? extends Attribute> getCategory() {
        return Media.class;
    }
    public final String getName() {
        return "media";
    }
}
