public class ClassCastException extends RuntimeException {
    private static final long serialVersionUID = -9223365651070458532L;
    public ClassCastException() {
        super();
    }
    public ClassCastException(String detailMessage) {
        super(detailMessage);
    }
    ClassCastException(Class<?> instanceClass, Class<?> castClass) {
        super(Msg.getString("K0340", instanceClass.getName(), castClass 
                .getName()));
    }
}
