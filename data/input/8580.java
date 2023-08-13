public class PositionDataInputStream extends DataInputStream {
    public PositionDataInputStream(InputStream in) {
        super(in instanceof PositionInputStream?
              in : new PositionInputStream(in));
    }
    public boolean markSupported() {
        return false;
    }
    public void mark(int readLimit) {
        throw new UnsupportedOperationException("mark");
    }
    public void reset() {
        throw new UnsupportedOperationException("reset");
    }
    public long position() {
        return ((PositionInputStream)in).position();
    }
}
