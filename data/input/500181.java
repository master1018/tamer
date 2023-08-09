public final class ClassDefinition {
    private Class<?> definitionClass;
    private byte[] definitionClassFile;
    public ClassDefinition(Class<?> theClass, byte[] theClassFile) {
        if (theClass == null) {
            throw new NullPointerException(Messages.getString("instrument.1")); 
        }
        if (theClassFile == null) {
            throw new NullPointerException(Messages.getString("instrument.2")); 
        }
        this.definitionClass = theClass;
        this.definitionClassFile = theClassFile;
    }
    public Class<?> getDefinitionClass() {
        return this.definitionClass;
    }
    public byte[] getDefinitionClassFile() {
        return this.definitionClassFile;
    }
}
