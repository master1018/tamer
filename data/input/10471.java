public class VMStartException extends Exception
{
    Process process;
    public VMStartException(Process process) {
        super();
        this.process = process;
    }
    public VMStartException(String message,
                            Process process) {
        super(message);
        this.process = process;
    }
    public Process process() {
        return process;
    }
}
