class BadPacketException extends RuntimeException {
    public BadPacketException()
    {
        super();
    }
    public BadPacketException(String msg)
    {
        super(msg);
    }
}
