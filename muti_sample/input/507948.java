public class SmtpSender extends Sender {
    Context mContext;
    private Transport mTransport;
    String mUsername;
    String mPassword;
    public static Sender newInstance(Context context, String uri) throws MessagingException {
        return new SmtpSender(context, uri);
    }
    private SmtpSender(Context context, String uriString) throws MessagingException {
        mContext = context;
        URI uri;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException use) {
            throw new MessagingException("Invalid SmtpTransport URI", use);
        }
        String scheme = uri.getScheme();
        if (scheme == null || !scheme.startsWith("smtp")) {
            throw new MessagingException("Unsupported protocol");
        }
        int connectionSecurity = Transport.CONNECTION_SECURITY_NONE;
        int defaultPort = 587;
        if (scheme.contains("+ssl")) {
            connectionSecurity = Transport.CONNECTION_SECURITY_SSL;
            defaultPort = 465;
        } else if (scheme.contains("+tls")) {
            connectionSecurity = Transport.CONNECTION_SECURITY_TLS;
        }
        boolean trustCertificates = scheme.contains("+trustallcerts");
        mTransport = new MailTransport("SMTP");
        mTransport.setUri(uri, defaultPort);
        mTransport.setSecurity(connectionSecurity, trustCertificates);
        String[] userInfoParts = mTransport.getUserInfoParts();
        if (userInfoParts != null) {
            mUsername = userInfoParts[0];
            if (userInfoParts.length > 1) {
                mPassword = userInfoParts[1];
            }
        }
    }
     void setTransport(Transport testTransport) {
        mTransport = testTransport;
    }
    @Override
    public void open() throws MessagingException {
        try {
            mTransport.open();
            executeSimpleCommand(null);
            String localHost = "localhost";
            try {
                InetAddress localAddress = InetAddress.getLocalHost();
                localHost = localAddress.getHostName();
            } catch (Exception e) {
                if (Config.LOGD && Email.DEBUG) {
                    Log.d(Email.LOG_TAG, "Unable to look up localhost");
                }
            }
            String result = executeSimpleCommand("EHLO " + localHost);
            if (mTransport.canTryTlsSecurity()) {
                if (result.contains("-STARTTLS") || result.contains(" STARTTLS")) {
                    executeSimpleCommand("STARTTLS");
                    mTransport.reopenTls();
                    result = executeSimpleCommand("EHLO " + localHost);
                } else {
                    if (Config.LOGD && Email.DEBUG) {
                        Log.d(Email.LOG_TAG, "TLS not supported but required");
                    }
                    throw new MessagingException(MessagingException.TLS_REQUIRED);
                }
            }
            boolean authLoginSupported = result.matches(".*AUTH.*LOGIN.*$");
            boolean authPlainSupported = result.matches(".*AUTH.*PLAIN.*$");
            if (mUsername != null && mUsername.length() > 0 && mPassword != null
                    && mPassword.length() > 0) {
                if (authPlainSupported) {
                    saslAuthPlain(mUsername, mPassword);
                }
                else if (authLoginSupported) {
                    saslAuthLogin(mUsername, mPassword);
                }
                else {
                    if (Config.LOGD && Email.DEBUG) {
                        Log.d(Email.LOG_TAG, "No valid authentication mechanism found.");
                    }
                    throw new MessagingException(MessagingException.AUTH_REQUIRED);
                }
            }
        } catch (SSLException e) {
            if (Config.LOGD && Email.DEBUG) {
                Log.d(Email.LOG_TAG, e.toString());
            }
            throw new CertificateValidationException(e.getMessage(), e);
        } catch (IOException ioe) {
            if (Config.LOGD && Email.DEBUG) {
                Log.d(Email.LOG_TAG, ioe.toString());
            }
            throw new MessagingException(MessagingException.IOERROR, ioe.toString());
        }
    }
    @Override
    public void sendMessage(long messageId) throws MessagingException {
        close();
        open();
        Message message = Message.restoreMessageWithId(mContext, messageId);
        if (message == null) {
            throw new MessagingException("Trying to send non-existent message id="
                    + Long.toString(messageId));
        }
        Address from = Address.unpackFirst(message.mFrom);
        Address[] to = Address.unpack(message.mTo);
        Address[] cc = Address.unpack(message.mCc);
        Address[] bcc = Address.unpack(message.mBcc);
        try {
            executeSimpleCommand("MAIL FROM: " + "<" + from.getAddress() + ">");
            for (Address address : to) {
                executeSimpleCommand("RCPT TO: " + "<" + address.getAddress() + ">");
            }
            for (Address address : cc) {
                executeSimpleCommand("RCPT TO: " + "<" + address.getAddress() + ">");
            }
            for (Address address : bcc) {
                executeSimpleCommand("RCPT TO: " + "<" + address.getAddress() + ">");
            }
            executeSimpleCommand("DATA");
            Rfc822Output.writeTo(mContext, messageId,
                    new EOLConvertingOutputStream(mTransport.getOutputStream()), true, false);
            executeSimpleCommand("\r\n.");
        } catch (IOException ioe) {
            throw new MessagingException("Unable to send message", ioe);
        }
    }
    @Override
    public void close() {
        mTransport.close();
    }
    private String executeSimpleCommand(String command) throws IOException, MessagingException {
        return executeSensitiveCommand(command, null);
    }
    private String executeSensitiveCommand(String command, String sensitiveReplacement)
            throws IOException, MessagingException {
        if (command != null) {
            mTransport.writeLine(command, sensitiveReplacement);
        }
        String line = mTransport.readLine();
        String result = line;
        while (line.length() >= 4 && line.charAt(3) == '-') {
            line = mTransport.readLine();
            result += line.substring(3);
        }
        if (result.length() > 0) {
            char c = result.charAt(0);
            if ((c == '4') || (c == '5')) {
                throw new MessagingException(result);
            }
        }
        return result;
    }
    private void saslAuthLogin(String username, String password) throws MessagingException,
        AuthenticationFailedException, IOException {
        try {
            executeSimpleCommand("AUTH LOGIN");
            executeSensitiveCommand(
                    Base64.encodeToString(username.getBytes(), Base64.NO_WRAP),
                    "/username redacted/");
            executeSensitiveCommand(
                    Base64.encodeToString(password.getBytes(), Base64.NO_WRAP),
                    "/password redacted/");
        }
        catch (MessagingException me) {
            if (me.getMessage().length() > 1 && me.getMessage().charAt(1) == '3') {
                throw new AuthenticationFailedException(me.getMessage());
            }
            throw me;
        }
    }
    private void saslAuthPlain(String username, String password) throws MessagingException,
            AuthenticationFailedException, IOException {
        byte[] data = ("\000" + username + "\000" + password).getBytes();
        data = Base64.encode(data, Base64.NO_WRAP);
        try {
            executeSensitiveCommand("AUTH PLAIN " + new String(data), "AUTH PLAIN /redacted/");
        }
        catch (MessagingException me) {
            if (me.getMessage().length() > 1 && me.getMessage().charAt(1) == '3') {
                throw new AuthenticationFailedException(me.getMessage());
            }
            throw me;
        }
    }
}
