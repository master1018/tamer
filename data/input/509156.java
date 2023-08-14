public class DriverPropertyInfo {
    public String[] choices;
    public String description;
    public String name;
    public boolean required;
    public String value;
    public DriverPropertyInfo(String name, String value) {
        this.name = name;
        this.value = value;
        this.choices = null;
        this.description = null;
        this.required = false;
    }
}
