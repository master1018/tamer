public final class Destination extends URISyntax
        implements PrintJobAttribute, PrintRequestAttribute {
    private static final long serialVersionUID = 6776739171700415321L;
    public Destination(URI uri) {
        super (uri);
    }
    public boolean equals(Object object) {
        return (super.equals(object) &&
                object instanceof Destination);
    }
    public final Class<? extends Attribute> getCategory() {
        return Destination.class;
    }
    public final String getName() {
        return "spool-data-destination";
    }
}
