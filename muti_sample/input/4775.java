class PipeImpl
    extends Pipe
{
    private final SourceChannel source;
    private final SinkChannel sink;
    PipeImpl(SelectorProvider sp) {
        long pipeFds = IOUtil.makePipe(true);
        int readFd = (int) (pipeFds >>> 32);
        int writeFd = (int) pipeFds;
        FileDescriptor sourcefd = new FileDescriptor();
        IOUtil.setfdVal(sourcefd, readFd);
        source = new SourceChannelImpl(sp, sourcefd);
        FileDescriptor sinkfd = new FileDescriptor();
        IOUtil.setfdVal(sinkfd, writeFd);
        sink = new SinkChannelImpl(sp, sinkfd);
    }
    public SourceChannel source() {
        return source;
    }
    public SinkChannel sink() {
        return sink;
    }
}
