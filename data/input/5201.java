class MalformedRequestException extends Exception {
    MalformedRequestException() { }
    MalformedRequestException(String msg) {
        super(msg);
    }
    MalformedRequestException(Exception x) {
        super(x);
    }
}
