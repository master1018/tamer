public class StringUIClientPropertyKey implements UIClientPropertyKey {
    private final String key;
    public StringUIClientPropertyKey(String key) {
        this.key = key;
    }
    public String toString() {
        return key;
    }
}
