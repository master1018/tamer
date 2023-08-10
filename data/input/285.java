public class ConstructorConverter extends BaseConverter implements Converter {
    private Constructor constructor;
    public ConstructorConverter(Class inputType, Constructor c) {
        super(inputType);
        this.constructor = c;
    }
    protected Object doConversion(Object obj) {
        Object[] args = new Object[] { obj };
        try {
            return constructor.newInstance(args);
        } catch (Exception e) {
            return null;
        }
    }
}
