public class HeaderGroup implements Cloneable {
    private List headers;
    public HeaderGroup() {
        this.headers = new ArrayList(16);
    }
    public void clear() {
        headers.clear();
    }
    public void addHeader(Header header) {
        if (header == null) {
            return;
        }
        headers.add(header);
    }
    public void removeHeader(Header header) {
        if (header == null) {
            return;
        }
        headers.remove(header);
    }
    public void updateHeader(Header header) {
        if (header == null) {
            return;
        }
        for (int i = 0; i < this.headers.size(); i++) {
            Header current = (Header) this.headers.get(i);
            if (current.getName().equalsIgnoreCase(header.getName())) {
                this.headers.set(i, header);
                return;
            }
        }
        this.headers.add(header);
    }
    public void setHeaders(Header[] headers) {
        clear();
        if (headers == null) {
            return;
        }
        for (int i = 0; i < headers.length; i++) {
            this.headers.add(headers[i]);
        }
    }
    public Header getCondensedHeader(String name) {
        Header[] headers = getHeaders(name);
        if (headers.length == 0) {
            return null;   
        } else if (headers.length == 1) {
            return headers[0];
        } else {
            CharArrayBuffer valueBuffer = new CharArrayBuffer(128);
            valueBuffer.append(headers[0].getValue());
            for (int i = 1; i < headers.length; i++) {
                valueBuffer.append(", ");
                valueBuffer.append(headers[i].getValue());
            }
            return new BasicHeader(name.toLowerCase(Locale.ENGLISH), valueBuffer.toString());
        }
    }
    public Header[] getHeaders(String name) {
        ArrayList headersFound = new ArrayList();
        for (int i = 0; i < headers.size(); i++) {
            Header header = (Header) headers.get(i);
            if (header.getName().equalsIgnoreCase(name)) {
                headersFound.add(header);
            }
        }
        return (Header[]) headersFound.toArray(new Header[headersFound.size()]);
    }
    public Header getFirstHeader(String name) {
        for (int i = 0; i < headers.size(); i++) {
            Header header = (Header) headers.get(i);
            if (header.getName().equalsIgnoreCase(name)) {
                return header;
            }
        }
        return null;                
    }
    public Header getLastHeader(String name) {
        for (int i = headers.size() - 1; i >= 0; i--) {
            Header header = (Header) headers.get(i);
            if (header.getName().equalsIgnoreCase(name)) {
                return header;
            }            
        }
        return null;        
    }
    public Header[] getAllHeaders() {
        return (Header[]) headers.toArray(new Header[headers.size()]);
    }
    public boolean containsHeader(String name) {
        for (int i = 0; i < headers.size(); i++) {
            Header header = (Header) headers.get(i);
            if (header.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
    public HeaderIterator iterator() {
        return new BasicListHeaderIterator(this.headers, null); 
    }
    public HeaderIterator iterator(final String name) {
        return new BasicListHeaderIterator(this.headers, name);
    }
    public HeaderGroup copy() {
        HeaderGroup clone = new HeaderGroup();
        clone.headers.addAll(this.headers);
        return clone;
    }
    public Object clone() throws CloneNotSupportedException {
        HeaderGroup clone = (HeaderGroup) super.clone();
        clone.headers = new ArrayList(this.headers);
        return clone;
    }
}
