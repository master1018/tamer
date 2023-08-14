public class ParserException extends Exception {
    private static final long serialVersionUID = -6202498304209638323L;
    public ParserException(){
        super();
    }
    public ParserException(String message){
        super(message);
    }
    public ParserException(Throwable t){
        super(t);
    }
}
