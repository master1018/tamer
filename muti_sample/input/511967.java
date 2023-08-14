public class UnmappableCharacterException extends CharacterCodingException {
    private static final long serialVersionUID = -7026962371537706123L;
    private int inputLength;
    public UnmappableCharacterException(int length) {
        this.inputLength = length;
    }
    public int getInputLength() {
        return this.inputLength;
    }
    @Override
    public String getMessage() {
        return Messages.getString("niochar.0A", this.inputLength); 
    }
}
