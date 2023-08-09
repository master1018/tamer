public class InvalidPropertiesFormatException extends IOException {
    private static final long serialVersionUID = 7763056076009360219L;
    public InvalidPropertiesFormatException(String m) {
        super(m);
    }
    public InvalidPropertiesFormatException(Throwable c) {
        initCause(c);
    }
    private void writeObject(ObjectOutputStream out) 
            throws NotSerializableException{
        throw new NotSerializableException();        
    }
    private void readObject(ObjectInputStream in) 
            throws NotSerializableException{
        throw new NotSerializableException();        
    }
}
