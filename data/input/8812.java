public class SocketImplTest extends Applet {
    static public void main(String[] args) {
        System.setSecurityManager(new SecurityManager());
        SocketImplTest s = new SocketImplTest();
        s.init();
        s.start();
    }
    class MySocketImpl extends SocketImpl {
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
        protected void connect(SocketAddress a, int t) throws IOException {
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
    class MyDatagramSocketImpl extends DatagramSocketImpl {
        protected void create() throws SocketException {
        }
        protected void bind(int lport, InetAddress laddr) throws SocketException {
        }
        protected void send(DatagramPacket p) throws IOException {
        }
        protected int peek(InetAddress i) throws IOException {
            return 0;
        }
        protected int peekData(DatagramPacket p) throws IOException {
            return 0;
        }
        protected void receive(DatagramPacket p) throws IOException {
        }
        protected void setTTL(byte ttl) throws IOException {
        }
        protected byte getTTL() throws IOException {
            return 0;
        }
        protected void setTimeToLive(int ttl) throws IOException {
        }
        protected int getTimeToLive() throws IOException {
            return 0;
        }
        protected void join(InetAddress inetaddr) throws IOException {
        }
        protected void leave(InetAddress inetaddr) throws IOException {
        }
        protected void joinGroup(SocketAddress mcastaddr, NetworkInterface netIf)
            throws IOException {
        }
        protected void leaveGroup(SocketAddress mcastaddr, NetworkInterface netIf)
            throws IOException {
        }
        protected void close() {
        }
        public Object getOption(int optID){
            return null;
        }
        public void setOption(int optID, Object value){
        }
    }
    class MySocket extends Socket {
        public MySocket(SocketImpl impl) throws IOException {
            super(impl);
        }
    }
    class MyDatagramSocket extends DatagramSocket {
        public MyDatagramSocket(DatagramSocketImpl impl) {
            super(impl);
        }
    }
    public void init(){
        MySocketImpl socketImpl = new MySocketImpl();
        MyDatagramSocketImpl dgramSocketImpl = new MyDatagramSocketImpl();
        try{
            MySocket socko = new MySocket(socketImpl);
            MyDatagramSocket dsock = new MyDatagramSocket(dgramSocketImpl);
        } catch(IOException ioex){
            System.err.println(ioex);
        } catch(SecurityException sec) {
            throw new RuntimeException("Failed. Creation of socket throwing SecurityException: ");
        }
    }
}
