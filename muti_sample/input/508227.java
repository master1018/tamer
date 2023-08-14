public final class ImplForWildcard implements WildcardType {
    private final ListOfTypes extendsBound, superBound;
    public ImplForWildcard(ListOfTypes extendsBound, ListOfTypes superBound) {
        this.extendsBound = extendsBound;
        this.superBound = superBound;
    }
    public Type[] getLowerBounds() throws TypeNotPresentException, 
            MalformedParameterizedTypeException {
        return superBound.getResolvedTypes().clone();
    }
    public Type[] getUpperBounds() throws TypeNotPresentException, 
            MalformedParameterizedTypeException {
        return extendsBound.getResolvedTypes().clone();
    }
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof WildcardType)) {
            return false;
        }
        WildcardType that = (WildcardType) o;
        return Arrays.equals(getLowerBounds(), that.getLowerBounds()) && 
                Arrays.equals(getUpperBounds(), that.getUpperBounds());
    }
    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(getLowerBounds()) + 
                Arrays.hashCode(getUpperBounds());
    }
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("?");
        if (extendsBound.length() > 0) {
            sb.append(" extends ").append(extendsBound);
        } else if (superBound.length() > 0) {
            sb.append(" super ").append(superBound);
        }
        return sb.toString();
    }
}
