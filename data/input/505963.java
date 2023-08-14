public class URISyntaxException extends Exception {
    private static final long serialVersionUID = 2137979680897488891L;
    private String input;
    private int index;
    public URISyntaxException(String input, String reason, int index) {
        super(reason);
        if (input == null || reason == null) {
            throw new NullPointerException();
        }
        if (index < -1) {
            throw new IllegalArgumentException();
        }
        this.input = input;
        this.index = index;
    }
    public URISyntaxException(String input, String reason) {
        super(reason);
        if (input == null || reason == null) {
            throw new NullPointerException();
        }
        this.input = input;
        index = -1;
    }
    public int getIndex() {
        return index;
    }
    public String getReason() {
        return super.getMessage();
    }
    public String getInput() {
        return input;
    }
    @Override
    public String getMessage() {
        String reason = super.getMessage();
        if (index != -1) {
            return Msg.getString("K0326", 
                    new String[] { reason, Integer.toString(index), input });
        }
        return Msg.getString("K0327", 
                new String[] { reason, input });
    }
}
