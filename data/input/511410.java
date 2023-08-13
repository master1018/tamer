public final class Encodings extends Object
{
    private static final String ENCODINGS_FILE = SerializerBase.PKG_PATH+"/Encodings.properties";
    static Writer getWriter(OutputStream output, String encoding)
        throws UnsupportedEncodingException
    {
        for (int i = 0; i < _encodings.length; ++i)
        {
            if (_encodings[i].name.equalsIgnoreCase(encoding))
            {
                try
                {
                    String javaName = _encodings[i].javaName;
                	OutputStreamWriter osw = new OutputStreamWriter(output,javaName);
                    return osw; 
                }
                catch (java.lang.IllegalArgumentException iae) 
                {
                }
                catch (UnsupportedEncodingException usee)
                {
                }
            }
        }
        try
        {
            return new OutputStreamWriter(output, encoding);
        }
        catch (java.lang.IllegalArgumentException iae) 
        {
            throw new UnsupportedEncodingException(encoding);
        }
    }
    static EncodingInfo getEncodingInfo(String encoding)
    {
        EncodingInfo ei;
        String normalizedEncoding = toUpperCaseFast(encoding);
        ei = (EncodingInfo) _encodingTableKeyJava.get(normalizedEncoding);
        if (ei == null)
            ei = (EncodingInfo) _encodingTableKeyMime.get(normalizedEncoding);
        if (ei == null) {
            ei = new EncodingInfo(null,null, '\u0000');
        }
        return ei;
    }
    public static boolean isRecognizedEncoding(String encoding)
    {
        EncodingInfo ei;
        String normalizedEncoding = encoding.toUpperCase();
        ei = (EncodingInfo) _encodingTableKeyJava.get(normalizedEncoding);
        if (ei == null)
            ei = (EncodingInfo) _encodingTableKeyMime.get(normalizedEncoding);
        if (ei != null)
            return true;
        return false;
    }
    static private String toUpperCaseFast(final String s) {
    	boolean different = false;
    	final int mx = s.length();
		char[] chars = new char[mx];
    	for (int i=0; i < mx; i++) {
    		char ch = s.charAt(i);
    		if ('a' <= ch && ch <= 'z') {
    			ch = (char) (ch + ('A' - 'a'));
    			different = true; 
    		}
    		chars[i] = ch;
    	}
    	final String upper;
    	if (different) 
    		upper = String.valueOf(chars);
    	else
    		upper = s;
    	return upper;
    }
    static final String DEFAULT_MIME_ENCODING = "UTF-8";
    static String getMimeEncoding(String encoding)
    {
        if (null == encoding)
        {
            try
            {
                encoding = System.getProperty("file.encoding", "UTF8");
                if (null != encoding)
                {
                    String jencoding =
                        (encoding.equalsIgnoreCase("Cp1252")
                            || encoding.equalsIgnoreCase("ISO8859_1")
                            || encoding.equalsIgnoreCase("8859_1")
                            || encoding.equalsIgnoreCase("UTF8"))
                            ? DEFAULT_MIME_ENCODING
                            : convertJava2MimeEncoding(encoding);
                    encoding =
                        (null != jencoding) ? jencoding : DEFAULT_MIME_ENCODING;
                }
                else
                {
                    encoding = DEFAULT_MIME_ENCODING;
                }
            }
            catch (SecurityException se)
            {
                encoding = DEFAULT_MIME_ENCODING;
            }
        }
        else
        {
            encoding = convertJava2MimeEncoding(encoding);
        }
        return encoding;
    }
    private static String convertJava2MimeEncoding(String encoding)
    {
        EncodingInfo enc =
            (EncodingInfo) _encodingTableKeyJava.get(toUpperCaseFast(encoding));
        if (null != enc)
            return enc.name;
        return encoding;
    }
    public static String convertMime2JavaEncoding(String encoding)
    {
        for (int i = 0; i < _encodings.length; ++i)
        {
            if (_encodings[i].name.equalsIgnoreCase(encoding))
            {
                return _encodings[i].javaName;
            }
        }
        return encoding;
    }
    private static EncodingInfo[] loadEncodingInfo()
    {
        try
        {
            final InputStream is; 
            SecuritySupport ss = SecuritySupport.getInstance();
            is = ss.getResourceAsStream(ObjectFactory.findClassLoader(),
                                            ENCODINGS_FILE);
            Properties props = new Properties();
            if (is != null) {
                props.load(is);
                is.close();
            } else {
            }
            int totalEntries = props.size();
            List encodingInfo_list = new ArrayList();
            Enumeration keys = props.keys();
            for (int i = 0; i < totalEntries; ++i)
            {
                String javaName = (String) keys.nextElement();
                String val = props.getProperty(javaName);
                int len = lengthOfMimeNames(val);
                String mimeName;
                char highChar;
                if (len == 0)
                {
                    mimeName = javaName;
                    highChar = '\u0000'; 
                }
                else
                {
                    try {
                        final String highVal = val.substring(len).trim();
                        highChar = (char) Integer.decode(highVal).intValue();
                    }
                    catch( NumberFormatException e) {
                        highChar = 0;
                    }
                    String mimeNames = val.substring(0, len);
                    StringTokenizer st =
                        new StringTokenizer(mimeNames, ",");
                    for (boolean first = true;
                        st.hasMoreTokens();
                        first = false)
                    {
                        mimeName = st.nextToken();
                        EncodingInfo ei = new EncodingInfo(mimeName, javaName, highChar);
                        encodingInfo_list.add(ei);
                        _encodingTableKeyMime.put(mimeName.toUpperCase(), ei);
                        if (first)
                            _encodingTableKeyJava.put(javaName.toUpperCase(), ei);
                    }
                }
            }
            EncodingInfo[] ret_ei = new EncodingInfo[encodingInfo_list.size()];
            encodingInfo_list.toArray(ret_ei);
            return ret_ei;
        }
        catch (java.net.MalformedURLException mue)
        {
            throw new org.apache.xml.serializer.utils.WrappedRuntimeException(mue);
        }
        catch (java.io.IOException ioe)
        {
            throw new org.apache.xml.serializer.utils.WrappedRuntimeException(ioe);
        }
    }
    private static int lengthOfMimeNames(String val) {
        int len = val.indexOf(' ');
        if (len < 0)  
            len = val.length();
        return len;
    }
    static boolean isHighUTF16Surrogate(char ch) {
        return ('\uD800' <= ch && ch <= '\uDBFF');
    }
    static boolean isLowUTF16Surrogate(char ch) {
        return ('\uDC00' <= ch && ch <= '\uDFFF');
    }
    static int toCodePoint(char highSurrogate, char lowSurrogate) {
        int codePoint =
            ((highSurrogate - 0xd800) << 10)
                + (lowSurrogate - 0xdc00)
                + 0x10000;
        return codePoint;
    }
    static int toCodePoint(char ch) {
        int codePoint = ch;
        return codePoint;
    }
    static public char getHighChar(String encoding)
    {
        final char highCodePoint;
        EncodingInfo ei;
        String normalizedEncoding = toUpperCaseFast(encoding);
        ei = (EncodingInfo) _encodingTableKeyJava.get(normalizedEncoding);
        if (ei == null)
            ei = (EncodingInfo) _encodingTableKeyMime.get(normalizedEncoding);
        if (ei != null)
            highCodePoint =  ei.getHighChar();
        else
            highCodePoint = 0;
        return highCodePoint;
    }
    private static final Hashtable _encodingTableKeyJava = new Hashtable();
    private static final Hashtable _encodingTableKeyMime = new Hashtable();
    private static final EncodingInfo[] _encodings = loadEncodingInfo();
}
