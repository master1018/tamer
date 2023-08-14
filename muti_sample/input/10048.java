public class ConfirmationCallback implements Callback, java.io.Serializable {
    private static final long serialVersionUID = -9095656433782481624L;
    public static final int UNSPECIFIED_OPTION          = -1;
    public static final int YES_NO_OPTION               = 0;
    public static final int YES_NO_CANCEL_OPTION        = 1;
    public static final int OK_CANCEL_OPTION            = 2;
    public static final int YES                         = 0;
    public static final int NO                          = 1;
    public static final int CANCEL                      = 2;
    public static final int OK                          = 3;
    public static final int INFORMATION                 = 0;
    public static final int WARNING                     = 1;
    public static final int ERROR                       = 2;
    private String prompt;
    private int messageType;
    private int optionType = UNSPECIFIED_OPTION;
    private int defaultOption;
    private String[] options;
    private int selection;
    public ConfirmationCallback(int messageType,
                int optionType, int defaultOption) {
        if (messageType < INFORMATION || messageType > ERROR ||
            optionType < YES_NO_OPTION || optionType > OK_CANCEL_OPTION)
            throw new IllegalArgumentException();
        switch (optionType) {
        case YES_NO_OPTION:
            if (defaultOption != YES && defaultOption != NO)
                throw new IllegalArgumentException();
            break;
        case YES_NO_CANCEL_OPTION:
            if (defaultOption != YES && defaultOption != NO &&
                defaultOption != CANCEL)
                throw new IllegalArgumentException();
            break;
        case OK_CANCEL_OPTION:
            if (defaultOption != OK && defaultOption != CANCEL)
                throw new IllegalArgumentException();
            break;
        }
        this.messageType = messageType;
        this.optionType = optionType;
        this.defaultOption = defaultOption;
    }
    public ConfirmationCallback(int messageType,
                String[] options, int defaultOption) {
        if (messageType < INFORMATION || messageType > ERROR ||
            options == null || options.length == 0 ||
            defaultOption < 0 || defaultOption >= options.length)
            throw new IllegalArgumentException();
        for (int i = 0; i < options.length; i++) {
            if (options[i] == null || options[i].length() == 0)
                throw new IllegalArgumentException();
        }
        this.messageType = messageType;
        this.options = options;
        this.defaultOption = defaultOption;
    }
    public ConfirmationCallback(String prompt, int messageType,
                int optionType, int defaultOption) {
        if (prompt == null || prompt.length() == 0 ||
            messageType < INFORMATION || messageType > ERROR ||
            optionType < YES_NO_OPTION || optionType > OK_CANCEL_OPTION)
            throw new IllegalArgumentException();
        switch (optionType) {
        case YES_NO_OPTION:
            if (defaultOption != YES && defaultOption != NO)
                throw new IllegalArgumentException();
            break;
        case YES_NO_CANCEL_OPTION:
            if (defaultOption != YES && defaultOption != NO &&
                defaultOption != CANCEL)
                throw new IllegalArgumentException();
            break;
        case OK_CANCEL_OPTION:
            if (defaultOption != OK && defaultOption != CANCEL)
                throw new IllegalArgumentException();
            break;
        }
        this.prompt = prompt;
        this.messageType = messageType;
        this.optionType = optionType;
        this.defaultOption = defaultOption;
    }
    public ConfirmationCallback(String prompt, int messageType,
                String[] options, int defaultOption) {
        if (prompt == null || prompt.length() == 0 ||
            messageType < INFORMATION || messageType > ERROR ||
            options == null || options.length == 0 ||
            defaultOption < 0 || defaultOption >= options.length)
            throw new IllegalArgumentException();
        for (int i = 0; i < options.length; i++) {
            if (options[i] == null || options[i].length() == 0)
                throw new IllegalArgumentException();
        }
        this.prompt = prompt;
        this.messageType = messageType;
        this.options = options;
        this.defaultOption = defaultOption;
    }
    public String getPrompt() {
        return prompt;
    }
    public int getMessageType() {
        return messageType;
    }
    public int getOptionType() {
        return optionType;
    }
    public String[] getOptions() {
        return options;
    }
    public int getDefaultOption() {
        return defaultOption;
    }
    public void setSelectedIndex(int selection) {
        this.selection = selection;
    }
    public int getSelectedIndex() {
        return selection;
    }
}
