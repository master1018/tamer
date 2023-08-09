public class LaxContentLengthStrategy implements ContentLengthStrategy {
    public LaxContentLengthStrategy() {
        super();
    }
    public long determineLength(final HttpMessage message) throws HttpException {
        if (message == null) {
            throw new IllegalArgumentException("HTTP message may not be null");
        }
        HttpParams params = message.getParams(); 
        boolean strict = params.isParameterTrue(CoreProtocolPNames.STRICT_TRANSFER_ENCODING);
        Header transferEncodingHeader = message.getFirstHeader(HTTP.TRANSFER_ENCODING);
        Header contentLengthHeader = message.getFirstHeader(HTTP.CONTENT_LEN);
        if (transferEncodingHeader != null) {
            HeaderElement[] encodings = null;
            try {
                encodings = transferEncodingHeader.getElements();
            } catch (ParseException px) {
                throw new ProtocolException
                    ("Invalid Transfer-Encoding header value: " +
                     transferEncodingHeader, px);
            }
            if (strict) {
                for (int i = 0; i < encodings.length; i++) {
                    String encoding = encodings[i].getName();
                    if (encoding != null && encoding.length() > 0 
                        && !encoding.equalsIgnoreCase(HTTP.CHUNK_CODING)
                        && !encoding.equalsIgnoreCase(HTTP.IDENTITY_CODING)) {
                        throw new ProtocolException("Unsupported transfer encoding: " + encoding);
                    }
                }
            }
            int len = encodings.length;
            if (HTTP.IDENTITY_CODING.equalsIgnoreCase(transferEncodingHeader.getValue())) {
                return IDENTITY;                            
            } else if ((len > 0) && (HTTP.CHUNK_CODING.equalsIgnoreCase(
                    encodings[len - 1].getName()))) { 
                return CHUNKED;
            } else {
                if (strict) {
                    throw new ProtocolException("Chunk-encoding must be the last one applied");
                }
                return IDENTITY;                            
            }
        } else if (contentLengthHeader != null) {
            long contentlen = -1;
            Header[] headers = message.getHeaders(HTTP.CONTENT_LEN);
            if (strict && headers.length > 1) {
                throw new ProtocolException("Multiple content length headers");
            }
            for (int i = headers.length - 1; i >= 0; i--) {
                Header header = headers[i];
                try {
                    contentlen = Long.parseLong(header.getValue());
                    break;
                } catch (NumberFormatException e) {
                    if (strict) {
                        throw new ProtocolException("Invalid content length: " + header.getValue());
                    }
                }
            }
            if (contentlen >= 0) {
                return contentlen;
            } else {
                return IDENTITY;
            }
        } else {
            return IDENTITY;
        }
    }
}
