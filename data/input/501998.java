public class SerializerException extends Exception {
    private static final long serialVersionUID = 7224994582791351605L;
    public SerializerException(){
        super();
    }
    public SerializerException(String message){
        super(message);
    }
    public SerializerException(Throwable t){
        super(t);
    }
}
