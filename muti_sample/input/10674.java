public class TransferProtocolClient extends NetworkClient {
    static final boolean debug = false;
    protected Vector    serverResponse = new Vector(1);
    protected int       lastReplyCode;
    public int readServerResponse() throws IOException {
        StringBuffer    replyBuf = new StringBuffer(32);
        int             c;
        int             continuingCode = -1;
        int             code;
        String          response;
        serverResponse.setSize(0);
        while (true) {
            while ((c = serverInput.read()) != -1) {
                if (c == '\r') {
                    if ((c = serverInput.read()) != '\n')
                        replyBuf.append('\r');
                }
                replyBuf.append((char)c);
                if (c == '\n')
                    break;
            }
            response = replyBuf.toString();
            replyBuf.setLength(0);
            if (debug) {
                System.out.print(response);
            }
            if (response.length() == 0) {
                code = -1;
            } else {
                try {
                    code = Integer.parseInt(response.substring(0, 3));
                } catch (NumberFormatException e) {
                    code = -1;
                } catch (StringIndexOutOfBoundsException e) {
                    continue;
                }
            }
            serverResponse.addElement(response);
            if (continuingCode != -1) {
                if (code != continuingCode ||
                    (response.length() >= 4 && response.charAt(3) == '-')) {
                    continue;
                } else {
                    continuingCode = -1;
                    break;
                }
            } else if (response.length() >= 4 && response.charAt(3) == '-') {
                continuingCode = code;
                continue;
            } else {
                break;
            }
        }
        return lastReplyCode = code;
    }
    public void sendServer(String cmd) {
        serverOutput.print(cmd);
        if (debug) {
            System.out.print("Sending: " + cmd);
        }
    }
    public String getResponseString() {
        return (String) serverResponse.elementAt(0);
    }
    public Vector getResponseStrings() {
        return serverResponse;
    }
    public TransferProtocolClient(String host, int port) throws IOException {
        super(host, port);
    }
    public TransferProtocolClient() {}
}
