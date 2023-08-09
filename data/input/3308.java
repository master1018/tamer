public final class DirectoryIteratorException
    extends ConcurrentModificationException
{
    private static final long serialVersionUID = -6012699886086212874L;
    public DirectoryIteratorException(IOException cause) {
        super(Objects.requireNonNull(cause));
    }
    @Override
    public IOException getCause() {
        return (IOException)super.getCause();
    }
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException
    {
        s.defaultReadObject();
        Throwable cause = super.getCause();
        if (!(cause instanceof IOException))
            throw new InvalidObjectException("Cause must be an IOException");
    }
}
