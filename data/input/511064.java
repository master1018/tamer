class CertificateChainValidator {
    private static final CertificateChainValidator sInstance
            = new CertificateChainValidator();
    public static CertificateChainValidator getInstance() {
        return sInstance;
    }
    private CertificateChainValidator() {}
    public SslError doHandshakeAndValidateServerCertificates(
            HttpsConnection connection, SSLSocket sslSocket, String domain)
            throws IOException {
        X509Certificate[] serverCertificates = null;
        try {
            sslSocket.setUseClientMode(true);
            sslSocket.startHandshake();
        } catch (IOException e) {
            closeSocketThrowException(
                sslSocket, e.getMessage(),
                "failed to perform SSL handshake");
        }
        Certificate[] peerCertificates =
            sslSocket.getSession().getPeerCertificates();
        if (peerCertificates == null || peerCertificates.length <= 0) {
            closeSocketThrowException(
                sslSocket, "failed to retrieve peer certificates");
        } else {
            serverCertificates =
                new X509Certificate[peerCertificates.length];
            for (int i = 0; i < peerCertificates.length; ++i) {
                serverCertificates[i] =
                    (X509Certificate)(peerCertificates[i]);
            }
            if (connection != null) {
                if (serverCertificates[0] != null) {
                    connection.setCertificate(
                        new SslCertificate(serverCertificates[0]));
                }
            }
        }
        X509Certificate currCertificate = serverCertificates[0];
        if (currCertificate == null) {
            closeSocketThrowException(
                sslSocket, "certificate for this site is null");
        } else {
            if (!DomainNameValidator.match(currCertificate, domain)) {
                String errorMessage = "certificate not for this host: " + domain;
                if (HttpLog.LOGV) {
                    HttpLog.v(errorMessage);
                }
                sslSocket.getSession().invalidate();
                return new SslError(
                    SslError.SSL_IDMISMATCH, currCertificate);
            }
        }
        int chainLength = serverCertificates.length;
        if (serverCertificates.length > 1) {
          int currIndex;
          for (currIndex = 0; currIndex < serverCertificates.length; ++currIndex) {
            boolean foundNext = false;
            for (int nextIndex = currIndex + 1;
                 nextIndex < serverCertificates.length;
                 ++nextIndex) {
              if (serverCertificates[currIndex].getIssuerDN().equals(
                  serverCertificates[nextIndex].getSubjectDN())) {
                foundNext = true;
                if (nextIndex != currIndex + 1) {
                  X509Certificate tempCertificate = serverCertificates[nextIndex];
                  serverCertificates[nextIndex] = serverCertificates[currIndex + 1];
                  serverCertificates[currIndex + 1] = tempCertificate;
                }
                break;
              }
            }
            if (!foundNext) break;
          }
          chainLength = currIndex + 1;
          X509Certificate lastCertificate = serverCertificates[chainLength - 1];
          Date now = new Date();
          if (lastCertificate.getSubjectDN().equals(lastCertificate.getIssuerDN())
              && now.after(lastCertificate.getNotAfter())) {
            --chainLength;
          }
        }
        X509Certificate[] newServerCertificates = null;
        newServerCertificates = new X509Certificate[chainLength];
        for (int i = 0; i < chainLength; ++i) {
          newServerCertificates[i] = serverCertificates[i];
        }
        try {
            SSLParameters.getDefaultTrustManager().checkServerTrusted(
                newServerCertificates, "RSA");
            return null;
        } catch (CertificateException e) {
            sslSocket.getSession().invalidate();
            if (HttpLog.LOGV) {
                HttpLog.v(
                    "failed to pre-validate the certificate chain, error: " +
                    e.getMessage());
            }
            return new SslError(
                SslError.SSL_UNTRUSTED, currCertificate);
        }
    }
    private void closeSocketThrowException(
            SSLSocket socket, String errorMessage, String defaultErrorMessage)
            throws IOException {
        closeSocketThrowException(
            socket, errorMessage != null ? errorMessage : defaultErrorMessage);
    }
    private void closeSocketThrowException(SSLSocket socket,
            String errorMessage) throws IOException {
        if (HttpLog.LOGV) {
            HttpLog.v("validation error: " + errorMessage);
        }
        if (socket != null) {
            SSLSession session = socket.getSession();
            if (session != null) {
                session.invalidate();
            }
            socket.close();
        }
        throw new SSLHandshakeException(errorMessage);
    }
}
