public class NameCallback implements Callback, java.io.Serializable {
    private static final long serialVersionUID = 3770938795909392253L;
    private String prompt;
    private String defaultName;
    private String inputName;
    public NameCallback(String prompt) {
        if (prompt == null || prompt.length() == 0)
            throw new IllegalArgumentException();
        this.prompt = prompt;
    }
    public NameCallback(String prompt, String defaultName) {
        if (prompt == null || prompt.length() == 0 ||
            defaultName == null || defaultName.length() == 0)
            throw new IllegalArgumentException();
        this.prompt = prompt;
        this.defaultName = defaultName;
    }
    public String getPrompt() {
        return prompt;
    }
    public String getDefaultName() {
        return defaultName;
    }
    public void setName(String name) {
        this.inputName = name;
    }
    public String getName() {
        return inputName;
    }
}
