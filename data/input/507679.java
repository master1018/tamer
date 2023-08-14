public class MalformedInputException extends CharacterCodingException {
    private static final long serialVersionUID = -3438823399834806194L;
    private int inputLength;
    public MalformedInputException(int length) {
        this.inputLength = length;
    }
    public int getInputLength() {
        return this.inputLength;
    }
    @Override
    public String getMessage() {
        return Messages.getString("niochar.05", this.inputLength); 
    }
}
