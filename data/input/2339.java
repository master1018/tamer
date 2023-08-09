class MultiplexConnectionInfo {
    int id;
    MultiplexInputStream in = null;
    MultiplexOutputStream out = null;
    boolean closed = false;
    MultiplexConnectionInfo(int id)
    {
        this.id  = id;
    }
}
