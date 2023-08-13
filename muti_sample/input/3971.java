public class SctpStandardSocketOptions {
    private SctpStandardSocketOptions() {}
    public static final SctpSocketOption<Boolean> SCTP_DISABLE_FRAGMENTS = new
        SctpStdSocketOption<Boolean>("SCTP_DISABLE_FRAGMENTS", Boolean.class,
        sun.nio.ch.SctpStdSocketOption.SCTP_DISABLE_FRAGMENTS);
    public static final SctpSocketOption<Boolean> SCTP_EXPLICIT_COMPLETE = new
        SctpStdSocketOption<Boolean>("SCTP_EXPLICIT_COMPLETE", Boolean.class,
        sun.nio.ch.SctpStdSocketOption.SCTP_EXPLICIT_COMPLETE);
    public static final SctpSocketOption<Integer> SCTP_FRAGMENT_INTERLEAVE =
            new SctpStdSocketOption<Integer>("SCTP_FRAGMENT_INTERLEAVE",
                  Integer.class,
                  sun.nio.ch.SctpStdSocketOption.SCTP_FRAGMENT_INTERLEAVE);
    public static final SctpSocketOption
        <SctpStandardSocketOptions.InitMaxStreams> SCTP_INIT_MAXSTREAMS =
        new SctpStdSocketOption<SctpStandardSocketOptions.InitMaxStreams>(
        "SCTP_INIT_MAXSTREAMS", SctpStandardSocketOptions.InitMaxStreams.class);
    public static final SctpSocketOption<Boolean> SCTP_NODELAY =
        new SctpStdSocketOption<Boolean>("SCTP_NODELAY", Boolean.class,
        sun.nio.ch.SctpStdSocketOption.SCTP_NODELAY);
     public static final SctpSocketOption<SocketAddress> SCTP_PRIMARY_ADDR =
             new SctpStdSocketOption<SocketAddress>
             ("SCTP_PRIMARY_ADDR", SocketAddress.class);
    public static final SctpSocketOption<SocketAddress> SCTP_SET_PEER_PRIMARY_ADDR =
            new SctpStdSocketOption<SocketAddress>
            ("SCTP_SET_PEER_PRIMARY_ADDR", SocketAddress.class);
    public static final SctpSocketOption<Integer> SO_SNDBUF =
        new SctpStdSocketOption<Integer>("SO_SNDBUF", Integer.class,
        sun.nio.ch.SctpStdSocketOption.SO_SNDBUF);
    public static final SctpSocketOption<Integer> SO_RCVBUF =
        new SctpStdSocketOption<Integer>("SO_RCVBUF", Integer.class,
        sun.nio.ch.SctpStdSocketOption.SO_RCVBUF);
    public static final SctpSocketOption<Integer> SO_LINGER =
        new SctpStdSocketOption<Integer>("SO_LINGER", Integer.class,
        sun.nio.ch.SctpStdSocketOption.SO_LINGER);
    public static class InitMaxStreams {
        private int maxInStreams;
        private int maxOutStreams;
        private InitMaxStreams(int maxInStreams, int maxOutStreams) {
           this.maxInStreams = maxInStreams;
           this.maxOutStreams = maxOutStreams;
        }
        public static InitMaxStreams create
              (int maxInStreams, int maxOutStreams) {
            if (maxOutStreams < 0 || maxOutStreams > 65535)
                throw new IllegalArgumentException(
                      "Invalid maxOutStreams value");
            if (maxInStreams < 0 || maxInStreams > 65535)
                throw new IllegalArgumentException(
                      "Invalid maxInStreams value");
            return new InitMaxStreams(maxInStreams, maxOutStreams);
        }
        public int maxInStreams() {
            return maxInStreams;
        }
        public int maxOutStreams() {
            return maxOutStreams;
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toString()).append(" [");
            sb.append("maxInStreams:").append(maxInStreams);
            sb.append("maxOutStreams:").append(maxOutStreams).append("]");
            return sb.toString();
        }
        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof InitMaxStreams) {
                InitMaxStreams that = (InitMaxStreams) obj;
                if (this.maxInStreams == that.maxInStreams &&
                    this.maxOutStreams == that.maxOutStreams)
                    return true;
            }
            return false;
        }
        @Override
        public int hashCode() {
            int hash = 7 ^ maxInStreams ^ maxOutStreams;
            return hash;
        }
    }
}
