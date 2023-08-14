public class OldImpl {
    static class FunkySocketImpl extends SocketImpl {
        protected void accept(SocketImpl impl) throws IOException {
        }
        protected int available(){
            return 0;
        }
        protected void bind(InetAddress host, int port){
        }
        protected void close(){
        }
        protected void connect(InetAddress address, int port){
        }
        protected void connect(String host, int port){
        }
        protected void connect(SocketAddress a,int b){
        }
        protected void create(boolean stream){
        }
        protected InputStream getInputStream(){
            return null;
        }
        protected OutputStream getOutputStream(){
            return null;
        }
        protected void listen(int backlog){
        }
        public Object getOption(int optID){
            return null;
        }
        public void setOption(int optID, Object value){
        }
        protected void sendUrgentData(int i){
        }
    }
    static class FunkyWunkySocketImpl extends FunkySocketImpl {}
    static class FunkySocket extends Socket {
        public FunkySocket(SocketImpl impl) throws IOException {
            super(impl);
        }
    }
    public static void main(String args[]) throws Exception {
        FunkyWunkySocketImpl socketImpl = new FunkyWunkySocketImpl();
        FunkySocket socko = new FunkySocket(socketImpl);
        if (socko.isBound()) {
            throw new RuntimeException ("socket is not really bound");
        }
    }
}
