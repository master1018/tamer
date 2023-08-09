public class BasicHeaderValueFormatter implements HeaderValueFormatter {
    public final static
        BasicHeaderValueFormatter DEFAULT = new BasicHeaderValueFormatter();
    public final static String SEPARATORS = " ;,:@()<>\\\"/[]?={}\t";
    public final static String UNSAFE_CHARS = "\"\\";
    public final static
        String formatElements(final HeaderElement[] elems,
                              final boolean quote,
                              HeaderValueFormatter formatter) {
        if (formatter == null)
            formatter = BasicHeaderValueFormatter.DEFAULT;
        return formatter.formatElements(null, elems, quote).toString();
    }
    public CharArrayBuffer formatElements(CharArrayBuffer buffer,
                                          final HeaderElement[] elems,
                                          final boolean quote) {
        if (elems == null) {
            throw new IllegalArgumentException
                ("Header element array must not be null.");
        }
        int len = estimateElementsLen(elems);
        if (buffer == null) {
            buffer = new CharArrayBuffer(len);
        } else {
            buffer.ensureCapacity(len);
        }
        for (int i=0; i<elems.length; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            formatHeaderElement(buffer, elems[i], quote);
        }
        return buffer;
    }
    protected int estimateElementsLen(final HeaderElement[] elems) {
        if ((elems == null) || (elems.length < 1))
            return 0;
        int result = (elems.length-1) * 2; 
        for (int i=0; i<elems.length; i++) {
            result += estimateHeaderElementLen(elems[i]);
        }
        return result;
    }
    public final static
        String formatHeaderElement(final HeaderElement elem,
                                   boolean quote,
                                   HeaderValueFormatter formatter) {
        if (formatter == null)
            formatter = BasicHeaderValueFormatter.DEFAULT;
        return formatter.formatHeaderElement(null, elem, quote).toString();
    }
    public CharArrayBuffer formatHeaderElement(CharArrayBuffer buffer,
                                               final HeaderElement elem,
                                               final boolean quote) {
        if (elem == null) {
            throw new IllegalArgumentException
                ("Header element must not be null.");
        }
        int len = estimateHeaderElementLen(elem);
        if (buffer == null) {
            buffer = new CharArrayBuffer(len);
        } else {
            buffer.ensureCapacity(len);
        }
        buffer.append(elem.getName());
        final String value = elem.getValue();
        if (value != null) {
            buffer.append('=');
            doFormatValue(buffer, value, quote);
        }
        final int parcnt = elem.getParameterCount();
        if (parcnt > 0) {
            for (int i=0; i<parcnt; i++) {
                buffer.append("; ");
                formatNameValuePair(buffer, elem.getParameter(i), quote);
            }
        }
        return buffer;
    }
    protected int estimateHeaderElementLen(final HeaderElement elem) {
        if (elem == null)
            return 0;
        int result = elem.getName().length(); 
        final String value = elem.getValue();
        if (value != null) {
            result += 3 + value.length(); 
        }
        final int parcnt = elem.getParameterCount();
        if (parcnt > 0) {
            for (int i=0; i<parcnt; i++) {
                result += 2 +                   
                    estimateNameValuePairLen(elem.getParameter(i));
            }
        }
        return result;
    }
    public final static
        String formatParameters(final NameValuePair[] nvps,
                                final boolean quote,
                                HeaderValueFormatter formatter) {
        if (formatter == null)
            formatter = BasicHeaderValueFormatter.DEFAULT;
        return formatter.formatParameters(null, nvps, quote).toString();
    }
    public CharArrayBuffer formatParameters(CharArrayBuffer buffer,
                                            NameValuePair[] nvps,
                                            boolean quote) {
        if (nvps == null) {
            throw new IllegalArgumentException
                ("Parameters must not be null.");
        }
        int len = estimateParametersLen(nvps);
        if (buffer == null) {
            buffer = new CharArrayBuffer(len);
        } else {
            buffer.ensureCapacity(len);
        }
        for (int i = 0; i < nvps.length; i++) {
            if (i > 0) {
                buffer.append("; ");
            }
            formatNameValuePair(buffer, nvps[i], quote);
        }
        return buffer;
    }
    protected int estimateParametersLen(final NameValuePair[] nvps) {
        if ((nvps == null) || (nvps.length < 1))
            return 0;
        int result = (nvps.length-1) * 2; 
        for (int i=0; i<nvps.length; i++) {
            result += estimateNameValuePairLen(nvps[i]);
        }
        return result;
    }
    public final static
        String formatNameValuePair(final NameValuePair nvp,
                                   final boolean quote,
                                   HeaderValueFormatter formatter) {
        if (formatter == null)
            formatter = BasicHeaderValueFormatter.DEFAULT;
        return formatter.formatNameValuePair(null, nvp, quote).toString();
    }
    public CharArrayBuffer formatNameValuePair(CharArrayBuffer buffer,
                                               final NameValuePair nvp,
                                               final boolean quote) {
        if (nvp == null) {
            throw new IllegalArgumentException
                ("NameValuePair must not be null.");
        }
        int len = estimateNameValuePairLen(nvp);
        if (buffer == null) {
            buffer = new CharArrayBuffer(len);
        } else {
            buffer.ensureCapacity(len);
        }
        buffer.append(nvp.getName());
        final String value = nvp.getValue();
        if (value != null) {
            buffer.append('=');
            doFormatValue(buffer, value, quote);
        }
        return buffer;
    }
    protected int estimateNameValuePairLen(final NameValuePair nvp) {
        if (nvp == null)
            return 0;
        int result = nvp.getName().length(); 
        final String value = nvp.getValue();
        if (value != null) {
            result += 3 + value.length(); 
        }
        return result;
    }
    protected void doFormatValue(final CharArrayBuffer buffer,
                                 final String value,
                                 boolean quote) {
        if (!quote) {
            for (int i = 0; (i < value.length()) && !quote; i++) {
                quote = isSeparator(value.charAt(i));
            }
        }
        if (quote) {
            buffer.append('"');
        }
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (isUnsafe(ch)) {
                buffer.append('\\');
            }
            buffer.append(ch);
        }
        if (quote) {
            buffer.append('"');
        }
    }
    protected boolean isSeparator(char ch) {
        return SEPARATORS.indexOf(ch) >= 0;
    }
    protected boolean isUnsafe(char ch) {
        return UNSAFE_CHARS.indexOf(ch) >= 0;
    }
} 
