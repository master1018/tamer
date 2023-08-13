public class CollectionCertStoreParameters implements CertStoreParameters {
    private static final Collection<?> defaultCollection = Collections.EMPTY_SET;
    private final Collection<?> collection;
    public CollectionCertStoreParameters() {
        this.collection = defaultCollection;
    }
    public CollectionCertStoreParameters(Collection<?> collection) {
        this.collection = collection;
        if (this.collection == null) {
            throw new NullPointerException();
        }
    }
    public Object clone() {
        try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
    }
    public Collection<?> getCollection() {
        return collection;
    }
    public String toString() {
        StringBuilder sb =
            new StringBuilder("CollectionCertStoreParameters: [\ncollection: "); 
        sb.append(getCollection().toString());
        sb.append("\n]"); 
        return sb.toString();
    }
}
