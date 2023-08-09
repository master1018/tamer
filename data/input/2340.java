class LogHandler {
    public LogHandler() {}
    public abstract
    Object initialSnapshot() throws Exception;
    public
    void snapshot(OutputStream out, Object value) throws Exception {
        MarshalOutputStream s = new MarshalOutputStream(out);
        s.writeObject(value);
        s.flush();
    }
    public
    Object recover(InputStream in) throws Exception {
        MarshalInputStream s = new MarshalInputStream(in);
        return s.readObject();
    }
    public
    void writeUpdate(LogOutputStream out, Object value) throws Exception {
        MarshalOutputStream s = new MarshalOutputStream(out);
        s.writeObject(value);
        s.flush();
    }
    public
    Object readUpdate(LogInputStream in, Object state) throws Exception {
        MarshalInputStream  s = new MarshalInputStream(in);
        return applyUpdate(s.readObject(), state);
    }
    public abstract
    Object applyUpdate(Object update, Object state) throws Exception;
}
