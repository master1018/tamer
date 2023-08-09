public class BinaryExceptionHandler {
    public int startPC;
    public int endPC;
    public int handlerPC;
    public ClassDeclaration exceptionClass;
    BinaryExceptionHandler(int start, int end,
                           int handler, ClassDeclaration xclass) {
        startPC = start;
        endPC = end;
        handlerPC = handler;
        exceptionClass = xclass;
    }
}
