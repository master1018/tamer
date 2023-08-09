class RuntimeExceptionParameter implements Serializable {
    private void writeObject(ObjectOutputStream out)
        throws IOException
    {
        throw new RuntimeException("wrote a parameter whos writeObject " +
                                   "method always throws a RuntimeException"
        );
    }
}
