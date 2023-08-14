public final class OCSP {
    private static final Debug debug = Debug.getInstance("certpath");
    private static final int CONNECT_TIMEOUT = 15000; 
    private OCSP() {}
    public static RevocationStatus check(X509Certificate cert,
        X509Certificate issuerCert)
        throws IOException, CertPathValidatorException {
        CertId certId = null;
        URI responderURI = null;
        try {
            X509CertImpl certImpl = X509CertImpl.toImpl(cert);
            responderURI = getResponderURI(certImpl);
            if (responderURI == null) {
                throw new CertPathValidatorException
                    ("No OCSP Responder URI in certificate");
            }
            certId = new CertId(issuerCert, certImpl.getSerialNumberObject());
        } catch (CertificateException ce) {
            throw new CertPathValidatorException
                ("Exception while encoding OCSPRequest", ce);
        } catch (IOException ioe) {
            throw new CertPathValidatorException
                ("Exception while encoding OCSPRequest", ioe);
        }
        OCSPResponse ocspResponse = check(Collections.singletonList(certId),
            responderURI, issuerCert, null);
        return (RevocationStatus) ocspResponse.getSingleResponse(certId);
    }
    public static RevocationStatus check(X509Certificate cert,
        X509Certificate issuerCert, URI responderURI, X509Certificate
        responderCert, Date date)
        throws IOException, CertPathValidatorException {
        CertId certId = null;
        try {
            X509CertImpl certImpl = X509CertImpl.toImpl(cert);
            certId = new CertId(issuerCert, certImpl.getSerialNumberObject());
        } catch (CertificateException ce) {
            throw new CertPathValidatorException
                ("Exception while encoding OCSPRequest", ce);
        } catch (IOException ioe) {
            throw new CertPathValidatorException
                ("Exception while encoding OCSPRequest", ioe);
        }
        OCSPResponse ocspResponse = check(Collections.singletonList(certId),
            responderURI, responderCert, date);
        return (RevocationStatus) ocspResponse.getSingleResponse(certId);
    }
    static OCSPResponse check(List<CertId> certIds, URI responderURI,
        X509Certificate responderCert, Date date)
        throws IOException, CertPathValidatorException {
        byte[] bytes = null;
        try {
            OCSPRequest request = new OCSPRequest(certIds);
            bytes = request.encodeBytes();
        } catch (IOException ioe) {
            throw new CertPathValidatorException
                ("Exception while encoding OCSPRequest", ioe);
        }
        InputStream in = null;
        OutputStream out = null;
        byte[] response = null;
        try {
            URL url = responderURI.toURL();
            if (debug != null) {
                debug.println("connecting to OCSP service at: " + url);
            }
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setConnectTimeout(CONNECT_TIMEOUT);
            con.setReadTimeout(CONNECT_TIMEOUT);
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty
                ("Content-type", "application/ocsp-request");
            con.setRequestProperty
                ("Content-length", String.valueOf(bytes.length));
            out = con.getOutputStream();
            out.write(bytes);
            out.flush();
            if (debug != null &&
                con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                debug.println("Received HTTP error: " + con.getResponseCode()
                    + " - " + con.getResponseMessage());
            }
            in = con.getInputStream();
            int contentLength = con.getContentLength();
            if (contentLength == -1) {
                contentLength = Integer.MAX_VALUE;
            }
            response = new byte[contentLength > 2048 ? 2048 : contentLength];
            int total = 0;
            while (total < contentLength) {
                int count = in.read(response, total, response.length - total);
                if (count < 0)
                    break;
                total += count;
                if (total >= response.length && total < contentLength) {
                    response = Arrays.copyOf(response, total * 2);
                }
            }
            response = Arrays.copyOf(response, total);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    throw ioe;
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ioe) {
                    throw ioe;
                }
            }
        }
        OCSPResponse ocspResponse = null;
        try {
            ocspResponse = new OCSPResponse(response, date, responderCert);
        } catch (IOException ioe) {
            throw new CertPathValidatorException(ioe);
        }
        if (ocspResponse.getResponseStatus() != ResponseStatus.SUCCESSFUL) {
            throw new CertPathValidatorException
                ("OCSP response error: " + ocspResponse.getResponseStatus());
        }
        for (CertId certId : certIds) {
            SingleResponse sr = ocspResponse.getSingleResponse(certId);
            if (sr == null) {
                if (debug != null) {
                    debug.println("No response found for CertId: " + certId);
                }
                throw new CertPathValidatorException(
                    "OCSP response does not include a response for a " +
                    "certificate supplied in the OCSP request");
            }
            if (debug != null) {
                debug.println("Status of certificate (with serial number " +
                    certId.getSerialNumber() + ") is: " + sr.getCertStatus());
            }
        }
        return ocspResponse;
    }
    public static URI getResponderURI(X509Certificate cert) {
        try {
            return getResponderURI(X509CertImpl.toImpl(cert));
        } catch (CertificateException ce) {
            return null;
        }
    }
    static URI getResponderURI(X509CertImpl certImpl) {
        AuthorityInfoAccessExtension aia =
            certImpl.getAuthorityInfoAccessExtension();
        if (aia == null) {
            return null;
        }
        List<AccessDescription> descriptions = aia.getAccessDescriptions();
        for (AccessDescription description : descriptions) {
            if (description.getAccessMethod().equals(
                AccessDescription.Ad_OCSP_Id)) {
                GeneralName generalName = description.getAccessLocation();
                if (generalName.getType() == GeneralNameInterface.NAME_URI) {
                    URIName uri = (URIName) generalName.getName();
                    return uri.getURI();
                }
            }
        }
        return null;
    }
    public static interface RevocationStatus {
        public enum CertStatus { GOOD, REVOKED, UNKNOWN };
        CertStatus getCertStatus();
        Date getRevocationTime();
        CRLReason getRevocationReason();
        Map<String, Extension> getSingleExtensions();
    }
}
