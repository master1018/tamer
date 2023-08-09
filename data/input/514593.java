public class SocketOutputBuffer extends AbstractSessionOutputBuffer {
    public SocketOutputBuffer(
            final Socket socket, 
            int buffersize,
            final HttpParams params) throws IOException {
        super();
        if (socket == null) {
            throw new IllegalArgumentException("Socket may not be null");
        }
        if (buffersize < 0) {
            buffersize = socket.getReceiveBufferSize();
            if (buffersize > 8096) {
                buffersize = 8096;
            }
        }
        if (buffersize < 1024) {
            buffersize = 1024;
        }
        socket.setSendBufferSize(buffersize * 3);
        init(socket.getOutputStream(), buffersize, params);
    }
}
