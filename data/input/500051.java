public class ECGenParameterSpec implements AlgorithmParameterSpec {
    private final String name;
    public ECGenParameterSpec(String name) {
        this.name = name;
        if (this.name == null) {
            throw new NullPointerException(Messages.getString("security.83", "name")); 
        }
    }
    public String getName() {
        return name;
    }
}
