public class ClassNotLoadedException extends Exception
{
    private String className;
    public ClassNotLoadedException(String className) {
        super();
        this.className = className;
    }
    public ClassNotLoadedException(String className, String message) {
        super(message);
        this.className = className;
    }
    public String className() {
        return className;
    }
}
