public class Misc implements Serializable {
    @Id
    @Column(name = "f_key")
    private String key;
    @Column(name = "f_value")
    private String value;
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
}
