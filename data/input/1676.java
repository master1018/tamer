public abstract class Process {
    abstract public OutputStream getOutputStream();
    abstract public InputStream getInputStream();
    abstract public InputStream getErrorStream();
    abstract public int waitFor() throws InterruptedException;
    abstract public int exitValue();
    abstract public void destroy();
}
