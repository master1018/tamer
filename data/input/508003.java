abstract public class ToStream extends SerializerBase
{
    private static final String COMMENT_BEGIN = "<!--";
    private static final String COMMENT_END = "-->";
    protected BoolStack m_disableOutputEscapingStates = new BoolStack();
    EncodingInfo m_encodingInfo = new EncodingInfo(null,null, '\u0000');
    protected BoolStack m_preserves = new BoolStack();
    protected boolean m_ispreserve = false;
    protected boolean m_isprevtext = false;
    private static final char[] s_systemLineSep;
    static {
        SecuritySupport ss = SecuritySupport.getInstance();
        s_systemLineSep = ss.getSystemProperty("line.separator").toCharArray();
    }
    protected char[] m_lineSep = s_systemLineSep;
    protected boolean m_lineSepUse = true;    
    protected int m_lineSepLen = m_lineSep.length;
    protected CharInfo m_charInfo;
    boolean m_shouldFlush = true;
    protected boolean m_spaceBeforeClose = false;
    boolean m_startNewLine;
    protected boolean m_inDoctype = false;
    boolean m_isUTF8 = false;
    protected boolean m_cdataStartCalled = false;
    private boolean m_expandDTDEntities = true;
    public ToStream()
    {
    }
    protected void closeCDATA() throws org.xml.sax.SAXException
    {
        try
        {
            m_writer.write(CDATA_DELIMITER_CLOSE);
            m_cdataTagOpen = false; 
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
    }
    public void serialize(Node node) throws IOException
    {
        try
        {
            TreeWalker walker =
                new TreeWalker(this);
            walker.traverse(node);
        }
        catch (org.xml.sax.SAXException se)
        {
            throw new WrappedRuntimeException(se);
        }
    }
    protected boolean m_escaping = true;
    protected final void flushWriter() throws org.xml.sax.SAXException
    {
        final java.io.Writer writer = m_writer;
        if (null != writer)
        {
            try
            {
                if (writer instanceof WriterToUTF8Buffered)
                {
                    if (m_shouldFlush)
                         ((WriterToUTF8Buffered) writer).flush();
                    else
                         ((WriterToUTF8Buffered) writer).flushBuffer();
                }
                if (writer instanceof WriterToASCI)
                {
                    if (m_shouldFlush)
                        writer.flush();
                }
                else
                {
                    writer.flush();
                }
            }
            catch (IOException ioe)
            {
                throw new org.xml.sax.SAXException(ioe);
            }
        }
    }
    OutputStream m_outputStream;
    public OutputStream getOutputStream()
    {
        return m_outputStream;
    }
    public void elementDecl(String name, String model) throws SAXException
    {
        if (m_inExternalDTD)
            return;
        try
        {
            final java.io.Writer writer = m_writer;
            DTDprolog();
            writer.write("<!ELEMENT ");
            writer.write(name);
            writer.write(' ');
            writer.write(model);
            writer.write('>');
            writer.write(m_lineSep, 0, m_lineSepLen);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
    }
    public void internalEntityDecl(String name, String value)
        throws SAXException
    {
        if (m_inExternalDTD)
            return;
        try
        {
            DTDprolog();
            outputEntityDecl(name, value);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
    }
    void outputEntityDecl(String name, String value) throws IOException
    {
        final java.io.Writer writer = m_writer;
        writer.write("<!ENTITY ");
        writer.write(name);
        writer.write(" \"");
        writer.write(value);
        writer.write("\">");
        writer.write(m_lineSep, 0, m_lineSepLen);
    }
    protected final void outputLineSep() throws IOException
    {
        m_writer.write(m_lineSep, 0, m_lineSepLen);
    }
    void setProp(String name, String val, boolean defaultVal) {
        if (val != null) {
            char first = getFirstCharLocName(name);
            switch (first) {
            case 'c':
                if (OutputKeys.CDATA_SECTION_ELEMENTS.equals(name)) {
                    String cdataSectionNames = val;
                    addCdataSectionElements(cdataSectionNames);
                }
                break;
            case 'd':
                if (OutputKeys.DOCTYPE_SYSTEM.equals(name)) {
                    this.m_doctypeSystem = val;
                } else if (OutputKeys.DOCTYPE_PUBLIC.equals(name)) {
                    this.m_doctypePublic = val;
                    if (val.startsWith("-
                        m_spaceBeforeClose = true;
                }
                break;
            case 'e':
                String newEncoding = val;
                if (OutputKeys.ENCODING.equals(name)) {
                    String possible_encoding = Encodings.getMimeEncoding(val);
                    if (possible_encoding != null) {
                        super.setProp("mime-name", possible_encoding,
                                defaultVal);
                    }
                    final String oldExplicitEncoding = getOutputPropertyNonDefault(OutputKeys.ENCODING);
                    final String oldDefaultEncoding  = getOutputPropertyDefault(OutputKeys.ENCODING);
                    if ( (defaultVal && ( oldDefaultEncoding == null || !oldDefaultEncoding.equalsIgnoreCase(newEncoding)))
                            || ( !defaultVal && (oldExplicitEncoding == null || !oldExplicitEncoding.equalsIgnoreCase(newEncoding) ))) {
                       EncodingInfo encodingInfo = Encodings.getEncodingInfo(newEncoding);
                       if (newEncoding != null && encodingInfo.name == null) {
                        final String msg = Utils.messages.createMessage(
                                MsgKey.ER_ENCODING_NOT_SUPPORTED,new Object[]{ newEncoding });
                        final String msg2 = 
                            "Warning: encoding \"" + newEncoding + "\" not supported, using "
                                   + Encodings.DEFAULT_MIME_ENCODING;
                        try {
                                final Transformer tran = super.getTransformer();
                                if (tran != null) {
                                    final ErrorListener errHandler = tran
                                            .getErrorListener();
                                    if (null != errHandler
                                            && m_sourceLocator != null) {
                                        errHandler
                                                .warning(new TransformerException(
                                                        msg, m_sourceLocator));
                                        errHandler
                                                .warning(new TransformerException(
                                                        msg2, m_sourceLocator));
                                    } else {
                                        System.out.println(msg);
                                        System.out.println(msg2);
                                    }
                                } else {
                                    System.out.println(msg);
                                    System.out.println(msg2);
                                }
                            } catch (Exception e) {
                            }
                            newEncoding = Encodings.DEFAULT_MIME_ENCODING;
                            val = Encodings.DEFAULT_MIME_ENCODING; 
                            encodingInfo = Encodings.getEncodingInfo(newEncoding);
                        } 
                       if (defaultVal == false || oldExplicitEncoding == null) {
                           m_encodingInfo = encodingInfo;
                           if (newEncoding != null)
                               m_isUTF8 = newEncoding.equals(Encodings.DEFAULT_MIME_ENCODING);
                           OutputStream os = getOutputStream();
                           if (os != null) {
                               Writer w = getWriter();
                               String oldEncoding = getOutputProperty(OutputKeys.ENCODING);
                               if ((w == null || !m_writer_set_by_user) 
                                       && !newEncoding.equalsIgnoreCase(oldEncoding)) {
                                   super.setProp(name, val, defaultVal);
                                   setOutputStreamInternal(os,false);
                               }
                           }
                       }
                    }
                }
                break;
            case 'i':
                if (OutputPropertiesFactory.S_KEY_INDENT_AMOUNT.equals(name)) {
                    setIndentAmount(Integer.parseInt(val));
                } else if (OutputKeys.INDENT.equals(name)) {
                    boolean b = "yes".equals(val) ? true : false;
                    m_doIndent = b;
                }
                break;
            case 'l':
                if (OutputPropertiesFactory.S_KEY_LINE_SEPARATOR.equals(name)) {
                    m_lineSep = val.toCharArray();
                    m_lineSepLen = m_lineSep.length;
                }
                break;
            case 'm':
                if (OutputKeys.MEDIA_TYPE.equals(name)) {
                    m_mediatype = val;
                }
                break;
            case 'o':
                if (OutputKeys.OMIT_XML_DECLARATION.equals(name)) {
                    boolean b = "yes".equals(val) ? true : false;
                    this.m_shouldNotWriteXMLHeader = b;
                }
                break;
            case 's':
                if (OutputKeys.STANDALONE.equals(name)) {
                    if (defaultVal) {
                        setStandaloneInternal(val);
                    } else {
                        m_standaloneWasSpecified = true;
                        setStandaloneInternal(val);
                    }
                }
                break;
            case 'v':
                if (OutputKeys.VERSION.equals(name)) {
                    m_version = val;
                }
                break;
            default:
                break;
            } 
            super.setProp(name, val, defaultVal);
        }
    }
    public void setOutputFormat(Properties format)
    {
        boolean shouldFlush = m_shouldFlush;
        if (format != null)
        {
            Enumeration propNames;
            propNames = format.propertyNames();
            while (propNames.hasMoreElements())
            {
                String key = (String) propNames.nextElement();
                String value = format.getProperty(key);
                String explicitValue = (String) format.get(key);
                if (explicitValue == null && value != null) {
                    this.setOutputPropertyDefault(key,value);
                }
                if (explicitValue != null) {
                    this.setOutputProperty(key,explicitValue);
                }
            } 
        }   
        String entitiesFileName =
            (String) format.get(OutputPropertiesFactory.S_KEY_ENTITIES);
        if (null != entitiesFileName)
        {
            String method = 
                (String) format.get(OutputKeys.METHOD);
            m_charInfo = CharInfo.getCharInfo(entitiesFileName, method);
        }
        m_shouldFlush = shouldFlush;
    }
    public Properties getOutputFormat() {
        Properties def = new Properties();
        {
            Set s = getOutputPropDefaultKeys();
            Iterator i = s.iterator();
            while (i.hasNext()) {
                String key = (String) i.next();
                String val = getOutputPropertyDefault(key);
                def.put(key, val);
            }
        }
        Properties props = new Properties(def);
        {
            Set s = getOutputPropKeys();
            Iterator i = s.iterator();
            while (i.hasNext()) {
                String key = (String) i.next();
                String val = getOutputPropertyNonDefault(key);
                if (val != null)
                    props.put(key, val);
            }
        }
        return props;
    }
    public void setWriter(Writer writer)
    {        
        setWriterInternal(writer, true);
    }
    private boolean m_writer_set_by_user;
    private void setWriterInternal(Writer writer, boolean setByUser) {
        m_writer_set_by_user = setByUser;
        m_writer = writer;
        if (m_tracer != null) {
            boolean noTracerYet = true;
            Writer w2 = m_writer;
            while (w2 instanceof WriterChain) {
                if (w2 instanceof SerializerTraceWriter) {
                    noTracerYet = false;
                    break;
                }
                w2 = ((WriterChain)w2).getWriter();
            }
            if (noTracerYet)
                m_writer = new SerializerTraceWriter(m_writer, m_tracer);
        }
    }
    public boolean setLineSepUse(boolean use_sytem_line_break)
    {
        boolean oldValue = m_lineSepUse;
        m_lineSepUse = use_sytem_line_break;
        return oldValue;
    }
    public void setOutputStream(OutputStream output)
    {
        setOutputStreamInternal(output, true);
    }
    private void setOutputStreamInternal(OutputStream output, boolean setByUser)
    {
        m_outputStream = output;
        String encoding = getOutputProperty(OutputKeys.ENCODING);        
        if (Encodings.DEFAULT_MIME_ENCODING.equalsIgnoreCase(encoding))
        {
            setWriterInternal(new WriterToUTF8Buffered(output), false);
        } else if (
                "WINDOWS-1250".equals(encoding)
                || "US-ASCII".equals(encoding)
                || "ASCII".equals(encoding))
        {
            setWriterInternal(new WriterToASCI(output), false);
        } else if (encoding != null) {
            Writer osw = null;
                try
                {
                    osw = Encodings.getWriter(output, encoding);
                }
                catch (UnsupportedEncodingException uee)
                {
                    osw = null;
                }
            if (osw == null) {
                System.out.println(
                    "Warning: encoding \""
                        + encoding
                        + "\" not supported"
                        + ", using "
                        + Encodings.DEFAULT_MIME_ENCODING);
                encoding = Encodings.DEFAULT_MIME_ENCODING;
                setEncoding(encoding);
                try {
                    osw = Encodings.getWriter(output, encoding);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            setWriterInternal(osw,false);
        }
        else {
            Writer osw = new OutputStreamWriter(output);
            setWriterInternal(osw,false);
        }
    }
    public boolean setEscaping(boolean escape)
    {
        final boolean temp = m_escaping;
        m_escaping = escape;
        return temp;
    }
    protected void indent(int depth) throws IOException
    {
        if (m_startNewLine)
            outputLineSep();
        if (m_indentAmount > 0)
            printSpace(depth * m_indentAmount);
    }
    protected void indent() throws IOException
    {
        indent(m_elemContext.m_currentElemDepth);
    }
    private void printSpace(int n) throws IOException
    {
        final java.io.Writer writer = m_writer;
        for (int i = 0; i < n; i++)
        {
            writer.write(' ');
        }
    }
    public void attributeDecl(
        String eName,
        String aName,
        String type,
        String valueDefault,
        String value)
        throws SAXException
    {
        if (m_inExternalDTD)
            return;
        try
        {
            final java.io.Writer writer = m_writer;
            DTDprolog();
            writer.write("<!ATTLIST ");
            writer.write(eName);
            writer.write(' ');
            writer.write(aName);
            writer.write(' ');
            writer.write(type);
            if (valueDefault != null)
            {
                writer.write(' ');
                writer.write(valueDefault);
            }
            writer.write('>');
            writer.write(m_lineSep, 0, m_lineSepLen);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
    }
    public Writer getWriter()
    {
        return m_writer;
    }
    public void externalEntityDecl(
        String name,
        String publicId,
        String systemId)
        throws SAXException
    {
        try {
            DTDprolog();
            m_writer.write("<!ENTITY ");            
            m_writer.write(name);
            if (publicId != null) {
                m_writer.write(" PUBLIC \"");
                m_writer.write(publicId);
            }
            else {
                m_writer.write(" SYSTEM \"");
                m_writer.write(systemId);
            }
            m_writer.write("\" >");
            m_writer.write(m_lineSep, 0, m_lineSepLen);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected boolean escapingNotNeeded(char ch)
    {
        final boolean ret;
        if (ch < 127)
        {
            if (ch >= CharInfo.S_SPACE || (CharInfo.S_LINEFEED == ch || 
                    CharInfo.S_CARRIAGERETURN == ch || CharInfo.S_HORIZONAL_TAB == ch))
                ret= true;
            else
                ret = false;
        }
        else {            
            ret = m_encodingInfo.isInEncoding(ch);
        }
        return ret;
    }
    protected int writeUTF16Surrogate(char c, char ch[], int i, int end)
        throws IOException
    {
        int codePoint = 0;
        if (i + 1 >= end)
        {
            throw new IOException(
                Utils.messages.createMessage(
                    MsgKey.ER_INVALID_UTF16_SURROGATE,
                    new Object[] { Integer.toHexString((int) c)}));
        }
        final char high = c;
        final char low = ch[i+1];
        if (!Encodings.isLowUTF16Surrogate(low)) {
            throw new IOException(
                Utils.messages.createMessage(
                    MsgKey.ER_INVALID_UTF16_SURROGATE,
                    new Object[] {
                        Integer.toHexString((int) c)
                            + " "
                            + Integer.toHexString(low)}));
        }
        final java.io.Writer writer = m_writer;
        if (m_encodingInfo.isInEncoding(c,low)) {
            writer.write(ch,i,2);
        }
        else {
            final String encoding = getEncoding();
            if (encoding != null) {
                codePoint = Encodings.toCodePoint(high, low);
                writer.write('&');
                writer.write('#');
                writer.write(Integer.toString(codePoint));
                writer.write(';');
            } else {
                writer.write(ch, i, 2);
            }
        }
        return codePoint;
    }
    int accumDefaultEntity(
        java.io.Writer writer,
        char ch,
        int i,
        char[] chars,
        int len,
        boolean fromTextNode,
        boolean escLF)
        throws IOException
    {
        if (!escLF && CharInfo.S_LINEFEED == ch)
        {
            writer.write(m_lineSep, 0, m_lineSepLen);
        }
        else
        {
            if ((fromTextNode && m_charInfo.shouldMapTextChar(ch)) || (!fromTextNode && m_charInfo.shouldMapAttrChar(ch)))
            {
                String outputStringForChar = m_charInfo.getOutputStringForChar(ch);
                if (null != outputStringForChar)
                {
                    writer.write(outputStringForChar);
                }
                else
                    return i;
            }
            else
                return i;
        }
        return i + 1;
    }
    void writeNormalizedChars(
        char ch[],
        int start,
        int length,
        boolean isCData,
        boolean useSystemLineSeparator)
        throws IOException, org.xml.sax.SAXException
    {
        final java.io.Writer writer = m_writer;
        int end = start + length;
        for (int i = start; i < end; i++)
        {
            char c = ch[i];
            if (CharInfo.S_LINEFEED == c && useSystemLineSeparator)
            {
                writer.write(m_lineSep, 0, m_lineSepLen);
            }
            else if (isCData && (!escapingNotNeeded(c)))
            {
                if (m_cdataTagOpen)
                    closeCDATA();
                if (Encodings.isHighUTF16Surrogate(c))
                {
                    writeUTF16Surrogate(c, ch, i, end);
                    i++ ; 
                }
                else
                {
                    writer.write("&#");
                    String intStr = Integer.toString((int) c);
                    writer.write(intStr);
                    writer.write(';');
                }
            }
            else if (
                isCData
                    && ((i < (end - 2))
                        && (']' == c)
                        && (']' == ch[i + 1])
                        && ('>' == ch[i + 2])))
            {
                writer.write(CDATA_CONTINUE);
                i += 2;
            }
            else
            {
                if (escapingNotNeeded(c))
                {
                    if (isCData && !m_cdataTagOpen)
                    {
                        writer.write(CDATA_DELIMITER_OPEN);
                        m_cdataTagOpen = true;
                    }
                    writer.write(c);
                }
                else if (Encodings.isHighUTF16Surrogate(c))
                {
                    if (m_cdataTagOpen)
                        closeCDATA();
                    writeUTF16Surrogate(c, ch, i, end);
                    i++; 
                }
                else
                {
                    if (m_cdataTagOpen)
                        closeCDATA();
                    writer.write("&#");
                    String intStr = Integer.toString((int) c);
                    writer.write(intStr);
                    writer.write(';');
                }
            }
        }
    }
    public void endNonEscaping() throws org.xml.sax.SAXException
    {
        m_disableOutputEscapingStates.pop();
    }
    public void startNonEscaping() throws org.xml.sax.SAXException
    {
        m_disableOutputEscapingStates.push(true);
    }
    protected void cdata(char ch[], int start, final int length)
        throws org.xml.sax.SAXException
    {
        try
        {
            final int old_start = start;
            if (m_elemContext.m_startTagOpen)
            {
                closeStartTag();
                m_elemContext.m_startTagOpen = false;
            }
            m_ispreserve = true;
            if (shouldIndent())
                indent();
            boolean writeCDataBrackets =
                (((length >= 1) && escapingNotNeeded(ch[start])));
            if (writeCDataBrackets && !m_cdataTagOpen)
            {
                m_writer.write(CDATA_DELIMITER_OPEN);
                m_cdataTagOpen = true;
            }
            if (isEscapingDisabled())
            {
                charactersRaw(ch, start, length);
            }
            else
                writeNormalizedChars(ch, start, length, true, m_lineSepUse);
            if (writeCDataBrackets)
            {
                if (ch[start + length - 1] == ']')
                    closeCDATA();
            }
            if (m_tracer != null)
                super.fireCDATAEvent(ch, old_start, length);
        }
        catch (IOException ioe)
        {
            throw new org.xml.sax.SAXException(
                Utils.messages.createMessage(
                    MsgKey.ER_OIERROR,
                    null),
                ioe);
        }
    }
    private boolean isEscapingDisabled()
    {
        return m_disableOutputEscapingStates.peekOrFalse();
    }
    protected void charactersRaw(char ch[], int start, int length)
        throws org.xml.sax.SAXException
    {
        if (m_inEntityRef)
            return;
        try
        {
            if (m_elemContext.m_startTagOpen)
            {
                closeStartTag();
                m_elemContext.m_startTagOpen = false;
            }
            m_ispreserve = true;
            m_writer.write(ch, start, length);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
    }
    public void characters(final char chars[], final int start, final int length)
        throws org.xml.sax.SAXException
    {
        if (length == 0 || (m_inEntityRef && !m_expandDTDEntities))
            return;
        m_docIsEmpty = false;
        if (m_elemContext.m_startTagOpen)
        {
            closeStartTag();
            m_elemContext.m_startTagOpen = false;
        }
        else if (m_needToCallStartDocument)
        {
            startDocumentInternal();
        }
        if (m_cdataStartCalled || m_elemContext.m_isCdataSection)
        {
            cdata(chars, start, length);
            return;
        }
        if (m_cdataTagOpen)
            closeCDATA();
        if (m_disableOutputEscapingStates.peekOrFalse() || (!m_escaping))
        {
            charactersRaw(chars, start, length);
            if (m_tracer != null)
                super.fireCharEvent(chars, start, length);
            return;
        }
        if (m_elemContext.m_startTagOpen)
        {
            closeStartTag();
            m_elemContext.m_startTagOpen = false;
        }
        try
        {
            int i;
            int startClean;
            final int end = start + length;
            int lastDirtyCharProcessed = start - 1; 
            final Writer writer = m_writer;
            boolean isAllWhitespace = true;
            i = start;
            while (i < end && isAllWhitespace) {
                char ch1 = chars[i];
                if (m_charInfo.shouldMapTextChar(ch1)) {
                    writeOutCleanChars(chars, i, lastDirtyCharProcessed);
                    String outputStringForChar = m_charInfo
                            .getOutputStringForChar(ch1);
                    writer.write(outputStringForChar);
                    isAllWhitespace = false;
                    lastDirtyCharProcessed = i; 
                    i++;
                } else {
                    switch (ch1) {
                    case CharInfo.S_SPACE:
                        i++;
                        break;
                    case CharInfo.S_LINEFEED:
                        lastDirtyCharProcessed = processLineFeed(chars, i,
                                lastDirtyCharProcessed, writer);
                        i++;
                        break;
                    case CharInfo.S_CARRIAGERETURN:
                        writeOutCleanChars(chars, i, lastDirtyCharProcessed);
                        writer.write("&#13;");
                        lastDirtyCharProcessed = i;
                        i++;
                        break;
                    case CharInfo.S_HORIZONAL_TAB:
                        i++;
                        break;
                    default:
                        isAllWhitespace = false;
                        break;
                    }
                }
            }
            if (i < end || !isAllWhitespace) 
                m_ispreserve = true;
            for (; i < end; i++)
            {
                char ch = chars[i];
                if (m_charInfo.shouldMapTextChar(ch)) {
                    writeOutCleanChars(chars, i, lastDirtyCharProcessed);
                    String outputStringForChar = m_charInfo.getOutputStringForChar(ch);
                    writer.write(outputStringForChar);
                    lastDirtyCharProcessed = i;
                }
                else {
                    if (ch <= 0x1F) {
                        switch (ch) {
                        case CharInfo.S_HORIZONAL_TAB:
                            break;
                        case CharInfo.S_LINEFEED:
                            lastDirtyCharProcessed = processLineFeed(chars, i, lastDirtyCharProcessed, writer);
                            break;
                        case CharInfo.S_CARRIAGERETURN:
                        	writeOutCleanChars(chars, i, lastDirtyCharProcessed);
                        	writer.write("&#13;");
                        	lastDirtyCharProcessed = i;
                            break;
                        default:
                            writeOutCleanChars(chars, i, lastDirtyCharProcessed);
                            writer.write("&#");
                            writer.write(Integer.toString(ch));
                            writer.write(';');
                            lastDirtyCharProcessed = i;
                            break;
                        }
                    }
                    else if (ch < 0x7F) {  
                    }
                    else if (ch <= 0x9F){
                        writeOutCleanChars(chars, i, lastDirtyCharProcessed);
                        writer.write("&#");
                        writer.write(Integer.toString(ch));
                        writer.write(';');
                        lastDirtyCharProcessed = i;
                    }
                    else if (ch == CharInfo.S_LINE_SEPARATOR) {
                        writeOutCleanChars(chars, i, lastDirtyCharProcessed);
                        writer.write("&#8232;");
                        lastDirtyCharProcessed = i;
                    }
                    else if (m_encodingInfo.isInEncoding(ch)) {
                    }
                    else {
                        writeOutCleanChars(chars, i, lastDirtyCharProcessed);
                        writer.write("&#");
                        writer.write(Integer.toString(ch));
                        writer.write(';');
                        lastDirtyCharProcessed = i;
                    }
                }
            }
            startClean = lastDirtyCharProcessed + 1;
            if (i > startClean)
            {
                int lengthClean = i - startClean;
                m_writer.write(chars, startClean, lengthClean);
            }
            m_isprevtext = true;
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
        if (m_tracer != null)
            super.fireCharEvent(chars, start, length);
    }
	private int processLineFeed(final char[] chars, int i, int lastProcessed, final Writer writer) throws IOException {
		if (!m_lineSepUse 
		|| (m_lineSepLen ==1 && m_lineSep[0] == CharInfo.S_LINEFEED)){
		}
		else {
		    writeOutCleanChars(chars, i, lastProcessed);
		    writer.write(m_lineSep, 0, m_lineSepLen);
		    lastProcessed = i;
		}
		return lastProcessed;
	}
    private void writeOutCleanChars(final char[] chars, int i, int lastProcessed) throws IOException {
        int startClean;
        startClean = lastProcessed + 1;
        if (startClean < i)
        {
            int lengthClean = i - startClean;
            m_writer.write(chars, startClean, lengthClean);
        }
    }     
    private static boolean isCharacterInC0orC1Range(char ch)
    {
        if(ch == 0x09 || ch == 0x0A || ch == 0x0D)
        	return false;
        else        	    	
        	return (ch >= 0x7F && ch <= 0x9F)|| (ch >= 0x01 && ch <= 0x1F);
    }
    private static boolean isNELorLSEPCharacter(char ch)
    {
        return (ch == 0x85 || ch == 0x2028);
    }
    private int processDirty(
        char[] chars, 
        int end,
        int i, 
        char ch,
        int lastDirty,
        boolean fromTextNode) throws IOException
    {
        int startClean = lastDirty + 1;
        if (i > startClean)
        {
            int lengthClean = i - startClean;
            m_writer.write(chars, startClean, lengthClean);
        }
        if (CharInfo.S_LINEFEED == ch && fromTextNode)
        {
            m_writer.write(m_lineSep, 0, m_lineSepLen);
        }
        else
        {
            startClean =
                accumDefaultEscape(
                    m_writer,
                    (char)ch,
                    i,
                    chars,
                    end,
                    fromTextNode,
                    false);
            i = startClean - 1;
        }
        return i;
    }
    public void characters(String s) throws org.xml.sax.SAXException
    {
        if (m_inEntityRef && !m_expandDTDEntities)
            return;
        final int length = s.length();
        if (length > m_charsBuff.length)
        {
            m_charsBuff = new char[length * 2 + 1];
        }
        s.getChars(0, length, m_charsBuff, 0);
        characters(m_charsBuff, 0, length);
    }
    private int accumDefaultEscape(
        Writer writer,
        char ch,
        int i,
        char[] chars,
        int len,
        boolean fromTextNode,
        boolean escLF)
        throws IOException
    {
        int pos = accumDefaultEntity(writer, ch, i, chars, len, fromTextNode, escLF);
        if (i == pos)
        {
            if (Encodings.isHighUTF16Surrogate(ch))
            {
                char next;
                int codePoint = 0;
                if (i + 1 >= len)
                {
                    throw new IOException(
                        Utils.messages.createMessage(
                            MsgKey.ER_INVALID_UTF16_SURROGATE,
                            new Object[] { Integer.toHexString(ch)}));
                }
                else
                {
                    next = chars[++i];
                    if (!(Encodings.isLowUTF16Surrogate(next)))
                        throw new IOException(
                            Utils.messages.createMessage(
                                MsgKey
                                    .ER_INVALID_UTF16_SURROGATE,
                                new Object[] {
                                    Integer.toHexString(ch)
                                        + " "
                                        + Integer.toHexString(next)}));
                    codePoint = Encodings.toCodePoint(ch,next);
                }
                writer.write("&#");
                writer.write(Integer.toString(codePoint));
                writer.write(';');
                pos += 2; 
            }
            else
            {
                if (isCharacterInC0orC1Range(ch) || isNELorLSEPCharacter(ch))
                {
                    writer.write("&#");
                    writer.write(Integer.toString(ch));
                    writer.write(';');
                }
                else if ((!escapingNotNeeded(ch) || 
                    (  (fromTextNode && m_charInfo.shouldMapTextChar(ch))
                     || (!fromTextNode && m_charInfo.shouldMapAttrChar(ch)))) 
                && m_elemContext.m_currentElemDepth > 0)
                {
                    writer.write("&#");
                    writer.write(Integer.toString(ch));
                    writer.write(';');
                }
                else
                {
                    writer.write(ch);
                }
                pos++;  
            }
        }
        return pos;
    }
    public void startElement(
        String namespaceURI,
        String localName,
        String name,
        Attributes atts)
        throws org.xml.sax.SAXException
    {
        if (m_inEntityRef)
            return;
        if (m_needToCallStartDocument)
        {
            startDocumentInternal();
            m_needToCallStartDocument = false;
            m_docIsEmpty = false;
        }
        else if (m_cdataTagOpen)
            closeCDATA();
        try
        {
            if (m_needToOutputDocTypeDecl) {
                if(null != getDoctypeSystem()) {
                    outputDocTypeDecl(name, true);
                }
                m_needToOutputDocTypeDecl = false;
            }
            if (m_elemContext.m_startTagOpen)
            {
                closeStartTag();
                m_elemContext.m_startTagOpen = false;
            }
            if (namespaceURI != null)
                ensurePrefixIsDeclared(namespaceURI, name);
            m_ispreserve = false;
            if (shouldIndent() && m_startNewLine)
            {
                indent();
            }
            m_startNewLine = true;
            final java.io.Writer writer = m_writer;
            writer.write('<');
            writer.write(name);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
        if (atts != null)
            addAttributes(atts);
        m_elemContext = m_elemContext.push(namespaceURI,localName,name);
        m_isprevtext = false;
        if (m_tracer != null)
            firePseudoAttributes();
    }
    public void startElement(
        String elementNamespaceURI,
        String elementLocalName,
        String elementName)
        throws SAXException
    {
        startElement(elementNamespaceURI, elementLocalName, elementName, null);
    }
    public void startElement(String elementName) throws SAXException
    {
        startElement(null, null, elementName, null);
    }
    void outputDocTypeDecl(String name, boolean closeDecl) throws SAXException
    {
        if (m_cdataTagOpen)
            closeCDATA();
        try
        {
            final java.io.Writer writer = m_writer;
            writer.write("<!DOCTYPE ");
            writer.write(name);
            String doctypePublic = getDoctypePublic();
            if (null != doctypePublic)
            {
                writer.write(" PUBLIC \"");
                writer.write(doctypePublic);
                writer.write('\"');
            }
            String doctypeSystem = getDoctypeSystem();
            if (null != doctypeSystem)
            {
                if (null == doctypePublic)
                    writer.write(" SYSTEM \"");
                else
                    writer.write(" \"");
                writer.write(doctypeSystem);
                if (closeDecl)
                {
                    writer.write("\">");
                    writer.write(m_lineSep, 0, m_lineSepLen);
                    closeDecl = false; 
                }
                else
                    writer.write('\"');
            }
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
    }
    public void processAttributes(java.io.Writer writer, int nAttrs) throws IOException, SAXException
    {
            String encoding = getEncoding();
            for (int i = 0; i < nAttrs; i++)
            {
                final String name = m_attributes.getQName(i);
                final String value = m_attributes.getValue(i);
                writer.write(' ');
                writer.write(name);
                writer.write("=\"");
                writeAttrString(writer, value, encoding);
                writer.write('\"');
            }
    }
    public void writeAttrString(
        Writer writer,
        String string,
        String encoding)
        throws IOException
    {
        final int len = string.length();
        if (len > m_attrBuff.length)
        {
           m_attrBuff = new char[len*2 + 1];             
        }
        string.getChars(0,len, m_attrBuff, 0);   
        final char[] stringChars = m_attrBuff;
        for (int i = 0; i < len; i++)
        {
            char ch = stringChars[i];
            if (m_charInfo.shouldMapAttrChar(ch)) {
                accumDefaultEscape(writer, ch, i, stringChars, len, false, true);
            }
            else {
                if (0x0 <= ch && ch <= 0x1F) {
                    switch (ch) {
                    case CharInfo.S_HORIZONAL_TAB:
                        writer.write("&#9;");
                        break;
                    case CharInfo.S_LINEFEED:
                        writer.write("&#10;");
                        break;
                    case CharInfo.S_CARRIAGERETURN:
                        writer.write("&#13;");
                        break;
                    default:
                        writer.write("&#");
                        writer.write(Integer.toString(ch));
                        writer.write(';');
                        break;
                    }
                }
                else if (ch < 0x7F) {   
                        writer.write(ch);
                }
                else if (ch <= 0x9F){
                    writer.write("&#");
                    writer.write(Integer.toString(ch));
                    writer.write(';');
                }
                else if (ch == CharInfo.S_LINE_SEPARATOR) {
                    writer.write("&#8232;");
                }
                else if (m_encodingInfo.isInEncoding(ch)) {
                    writer.write(ch);
                }
                else {
                    writer.write("&#");
                    writer.write(Integer.toString(ch));
                    writer.write(';');
                }
            }
        }
    }
    public void endElement(String namespaceURI, String localName, String name)
        throws org.xml.sax.SAXException
    {
        if (m_inEntityRef)
            return;
        m_prefixMap.popNamespaces(m_elemContext.m_currentElemDepth, null);
        try
        {
            final java.io.Writer writer = m_writer;
            if (m_elemContext.m_startTagOpen)
            {
                if (m_tracer != null)
                    super.fireStartElem(m_elemContext.m_elementName);
                int nAttrs = m_attributes.getLength();
                if (nAttrs > 0)
                {
                    processAttributes(m_writer, nAttrs);
                    m_attributes.clear();
                }
                if (m_spaceBeforeClose)
                    writer.write(" />");
                else
                    writer.write("/>");
            }
            else
            {
                if (m_cdataTagOpen)
                    closeCDATA();
                if (shouldIndent())
                    indent(m_elemContext.m_currentElemDepth - 1);
                writer.write('<');
                writer.write('/');
                writer.write(name);
                writer.write('>');
            }
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
        if (!m_elemContext.m_startTagOpen && m_doIndent)
        {
            m_ispreserve = m_preserves.isEmpty() ? false : m_preserves.pop();
        }
        m_isprevtext = false;
        if (m_tracer != null)
            super.fireEndElem(name);
        m_elemContext = m_elemContext.m_prev;
    }
    public void endElement(String name) throws org.xml.sax.SAXException
    {
        endElement(null, null, name);
    }
    public void startPrefixMapping(String prefix, String uri)
        throws org.xml.sax.SAXException
    {
        startPrefixMapping(prefix, uri, true);
    }
    public boolean startPrefixMapping(
        String prefix,
        String uri,
        boolean shouldFlush)
        throws org.xml.sax.SAXException
    {
        boolean pushed;
        int pushDepth;
        if (shouldFlush)
        {
            flushPending();
            pushDepth = m_elemContext.m_currentElemDepth + 1;
        }
        else
        {
            pushDepth = m_elemContext.m_currentElemDepth;
        }
        pushed = m_prefixMap.pushNamespace(prefix, uri, pushDepth);
        if (pushed)
        {
            String name;
            if (EMPTYSTRING.equals(prefix))
            {
                name = "xmlns";
                addAttributeAlways(XMLNS_URI, name, name, "CDATA", uri, false);
            }
            else
            {
                if (!EMPTYSTRING.equals(uri))
                { 
                    name = "xmlns:" + prefix;
                    addAttributeAlways(XMLNS_URI, prefix, name, "CDATA", uri, false);
                }
            }
        }
        return pushed;
    }
    public void comment(char ch[], int start, int length)
        throws org.xml.sax.SAXException
    {
        int start_old = start;
        if (m_inEntityRef)
            return;
        if (m_elemContext.m_startTagOpen)
        {
            closeStartTag();
            m_elemContext.m_startTagOpen = false;
        }
        else if (m_needToCallStartDocument)
        {
            startDocumentInternal();
            m_needToCallStartDocument = false;
        }
        try
        {
            final int limit = start + length;
            boolean wasDash = false;
            if (m_cdataTagOpen)
                closeCDATA();
            if (shouldIndent())
                indent();
            final java.io.Writer writer = m_writer;    
            writer.write(COMMENT_BEGIN);
            for (int i = start; i < limit; i++)
            {
                if (wasDash && ch[i] == '-')
                {
                    writer.write(ch, start, i - start);
                    writer.write(" -");
                    start = i + 1;
                }
                wasDash = (ch[i] == '-');
            }
            if (length > 0)
            {
                final int remainingChars = (limit - start);
                if (remainingChars > 0)
                    writer.write(ch, start, remainingChars);
                if (ch[limit - 1] == '-')
                    writer.write(' ');
            }
            writer.write(COMMENT_END);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
        m_startNewLine = true;
        if (m_tracer != null)
            super.fireCommentEvent(ch, start_old,length);
    }
    public void endCDATA() throws org.xml.sax.SAXException
    {
        if (m_cdataTagOpen)
            closeCDATA();
        m_cdataStartCalled = false;
    }
    public void endDTD() throws org.xml.sax.SAXException
    {
        try
        {
            if (m_needToOutputDocTypeDecl)
            {
                outputDocTypeDecl(m_elemContext.m_elementName, false);
                m_needToOutputDocTypeDecl = false;
            }
            final java.io.Writer writer = m_writer;
            if (!m_inDoctype)
                writer.write("]>");
            else
            {
                writer.write('>');
            }
            writer.write(m_lineSep, 0, m_lineSepLen);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
    }
    public void endPrefixMapping(String prefix) throws org.xml.sax.SAXException
    { 
    }
    public void ignorableWhitespace(char ch[], int start, int length)
        throws org.xml.sax.SAXException
    {
        if (0 == length)
            return;
        characters(ch, start, length);
    }
    public void skippedEntity(String name) throws org.xml.sax.SAXException
    { 
    }
    public void startCDATA() throws org.xml.sax.SAXException
    {
        m_cdataStartCalled = true;
    }
    public void startEntity(String name) throws org.xml.sax.SAXException
    {
        if (name.equals("[dtd]"))
            m_inExternalDTD = true;
        if (!m_expandDTDEntities && !m_inExternalDTD) {
            startNonEscaping();
            characters("&" + name + ';');
            endNonEscaping();
        }
        m_inEntityRef = true;
    }
    protected void closeStartTag() throws SAXException
    {
        if (m_elemContext.m_startTagOpen)
        {
            try
            {
                if (m_tracer != null)
                    super.fireStartElem(m_elemContext.m_elementName);
                int nAttrs = m_attributes.getLength();
                if (nAttrs > 0)
                {
                    processAttributes(m_writer, nAttrs);
                    m_attributes.clear();
                }
                m_writer.write('>');
            }
            catch (IOException e)
            {
                throw new SAXException(e);
            }
            if (m_CdataElems != null)
                m_elemContext.m_isCdataSection = isCdataSection();
            if (m_doIndent)
            {
                m_isprevtext = false;
                m_preserves.push(m_ispreserve);
            }
        }
    }
    public void startDTD(String name, String publicId, String systemId)
        throws org.xml.sax.SAXException
    {
        setDoctypeSystem(systemId);
        setDoctypePublic(publicId);
        m_elemContext.m_elementName = name;
        m_inDoctype = true;
    }
    public int getIndentAmount()
    {
        return m_indentAmount;
    }
    public void setIndentAmount(int m_indentAmount)
    {
        this.m_indentAmount = m_indentAmount;
    }
    protected boolean shouldIndent()
    {
        return m_doIndent && (!m_ispreserve && !m_isprevtext) && m_elemContext.m_currentElemDepth > 0;
    }
    private void setCdataSectionElements(String key, Properties props)
    {
        String s = props.getProperty(key);
        if (null != s)
        {
            Vector v = new Vector();
            int l = s.length();
            boolean inCurly = false;
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < l; i++)
            {
                char c = s.charAt(i);
                if (Character.isWhitespace(c))
                {
                    if (!inCurly)
                    {
                        if (buf.length() > 0)
                        {
                            addCdataSectionElement(buf.toString(), v);
                            buf.setLength(0);
                        }
                        continue;
                    }
                }
                else if ('{' == c)
                    inCurly = true;
                else if ('}' == c)
                    inCurly = false;
                buf.append(c);
            }
            if (buf.length() > 0)
            {
                addCdataSectionElement(buf.toString(), v);
                buf.setLength(0);
            }
            setCdataSectionElements(v);
        }
    }
    private void addCdataSectionElement(String URI_and_localName, Vector v)
    {
        StringTokenizer tokenizer =
            new StringTokenizer(URI_and_localName, "{}", false);
        String s1 = tokenizer.nextToken();
        String s2 = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;
        if (null == s2)
        {
            v.addElement(null);
            v.addElement(s1);
        }
        else
        {
            v.addElement(s1);
            v.addElement(s2);
        }
    }
    public void setCdataSectionElements(Vector URI_and_localNames)
    {
        if (URI_and_localNames != null)
        {
            final int len = URI_and_localNames.size() - 1;
            if (len > 0)
            {
                final StringBuffer sb = new StringBuffer();
                for (int i = 0; i < len; i += 2)
                {
                    if (i != 0)
                        sb.append(' ');
                    final String uri = (String) URI_and_localNames.elementAt(i);
                    final String localName =
                        (String) URI_and_localNames.elementAt(i + 1);
                    if (uri != null)
                    {
                        sb.append('{');
                        sb.append(uri);
                        sb.append('}');
                    }
                    sb.append(localName);
                }
                m_StringOfCDATASections = sb.toString();
            }
        }
        initCdataElems(m_StringOfCDATASections);
    }
    protected String ensureAttributesNamespaceIsDeclared(
        String ns,
        String localName,
        String rawName)
        throws org.xml.sax.SAXException
    {
        if (ns != null && ns.length() > 0)
        {
            int index = 0;
            String prefixFromRawName =
                (index = rawName.indexOf(":")) < 0
                    ? ""
                    : rawName.substring(0, index);
            if (index > 0)
            {
                String uri = m_prefixMap.lookupNamespace(prefixFromRawName);
                if (uri != null && uri.equals(ns))
                {
                    return null;
                }
                else
                {
                    this.startPrefixMapping(prefixFromRawName, ns, false);
                    this.addAttribute(
                        "http:
                        prefixFromRawName,
                        "xmlns:" + prefixFromRawName,
                        "CDATA",
                        ns, false);
                    return prefixFromRawName;
                }
            }
            else
            {
                String prefix = m_prefixMap.lookupPrefix(ns);
                if (prefix == null)
                {
                    prefix = m_prefixMap.generateNextPrefix();
                    this.startPrefixMapping(prefix, ns, false);
                    this.addAttribute(
                        "http:
                        prefix,
                        "xmlns:" + prefix,
                        "CDATA",
                        ns, false);
                }
                return prefix;
            }
        }
        return null;
    }
    void ensurePrefixIsDeclared(String ns, String rawName)
        throws org.xml.sax.SAXException
    {
        if (ns != null && ns.length() > 0)
        {
            int index;
            final boolean no_prefix = ((index = rawName.indexOf(":")) < 0);
            String prefix = (no_prefix) ? "" : rawName.substring(0, index);
            if (null != prefix)
            {
                String foundURI = m_prefixMap.lookupNamespace(prefix);
                if ((null == foundURI) || !foundURI.equals(ns))
                {
                    this.startPrefixMapping(prefix, ns);
                    this.addAttributeAlways(
                        "http:
                        no_prefix ? "xmlns" : prefix,  
                        no_prefix ? "xmlns" : ("xmlns:"+ prefix), 
                        "CDATA",
                        ns,
                        false);
                }
            }
        }
    }
    public void flushPending() throws SAXException
    {
            if (m_needToCallStartDocument)
            {
                startDocumentInternal();
                m_needToCallStartDocument = false;
            }
            if (m_elemContext.m_startTagOpen)
            {
                closeStartTag();
                m_elemContext.m_startTagOpen = false;
            }
            if (m_cdataTagOpen)
            {
                closeCDATA();
                m_cdataTagOpen = false;
            }
            if (m_writer != null) {
                try {
                    m_writer.flush();
                }
                catch(IOException e) {
                }
            }
    }
    public void setContentHandler(ContentHandler ch)
    {
    }
    public boolean addAttributeAlways(
        String uri,
        String localName,
        String rawName,
        String type,
        String value,
        boolean xslAttribute)
    {
        boolean was_added;
        int index;
        if (uri == null || localName == null || uri.length() == 0)
            index = m_attributes.getIndex(rawName);
        else {
            index = m_attributes.getIndex(uri, localName);
        }
        if (index >= 0)
        {
            String old_value = null;
            if (m_tracer != null)
            {
                old_value = m_attributes.getValue(index);
                if (value.equals(old_value))
                    old_value = null;
            }
            m_attributes.setValue(index, value);
            was_added = false;
            if (old_value != null)
                firePseudoAttributes();
        }
        else
        {
            if (xslAttribute)
            {
                final int colonIndex = rawName.indexOf(':');
                if (colonIndex > 0)
                {
                    String prefix = rawName.substring(0,colonIndex);
                    NamespaceMappings.MappingRecord existing_mapping = m_prefixMap.getMappingFromPrefix(prefix);
                    if (existing_mapping != null 
                    && existing_mapping.m_declarationDepth == m_elemContext.m_currentElemDepth
                    && !existing_mapping.m_uri.equals(uri))
                    {
                        prefix = m_prefixMap.lookupPrefix(uri);
                        if (prefix == null)
                        {
                            prefix = m_prefixMap.generateNextPrefix();
                        }
                        rawName = prefix + ':' + localName;
                    }
                }
                try
                {
                    String prefixUsed =
                        ensureAttributesNamespaceIsDeclared(
                            uri,
                            localName,
                            rawName);
                }
                catch (SAXException e)
                {
                    e.printStackTrace();
                }
            }
            m_attributes.addAttribute(uri, localName, rawName, type, value);
            was_added = true;
            if (m_tracer != null)
                firePseudoAttributes();
        }
        return was_added;
    }
    protected void firePseudoAttributes()
    {
        if (m_tracer != null)
        {
            try
            {
                m_writer.flush();
                StringBuffer sb = new StringBuffer();
                int nAttrs = m_attributes.getLength();
                if (nAttrs > 0)
                {
                    java.io.Writer writer =
                        new ToStream.WritertoStringBuffer(sb);
                    processAttributes(writer, nAttrs);
                }
                sb.append('>');  
                char ch[] = sb.toString().toCharArray();
                m_tracer.fireGenerateEvent(
                    SerializerTrace.EVENTTYPE_OUTPUT_PSEUDO_CHARACTERS,
                    ch,
                    0,
                    ch.length);                
            }
            catch (IOException ioe)
            {
            }
            catch (SAXException se)
            {
            }
        }
    }
    private class WritertoStringBuffer extends java.io.Writer
    {
        final private StringBuffer m_stringbuf;
        WritertoStringBuffer(StringBuffer sb)
        {
            m_stringbuf = sb;
        }
        public void write(char[] arg0, int arg1, int arg2) throws IOException
        {
            m_stringbuf.append(arg0, arg1, arg2);
        }
        public void flush() throws IOException
        {
        }
        public void close() throws IOException
        {
        }
        public void write(int i)
        {
            m_stringbuf.append((char) i);
        }
        public void write(String s)
        {
            m_stringbuf.append(s);
        }
    }
    public void setTransformer(Transformer transformer) {
        super.setTransformer(transformer);
        if (m_tracer != null
         && !(m_writer instanceof SerializerTraceWriter)  )
            setWriterInternal(new SerializerTraceWriter(m_writer, m_tracer), false);        
    }
    public boolean reset()
    {
        boolean wasReset = false;
        if (super.reset())
        {
            resetToStream();
            wasReset = true;
        }
        return wasReset;
    }
    private void resetToStream()
    {
         this.m_cdataStartCalled = false;
         this.m_disableOutputEscapingStates.clear();
         this.m_escaping = true;
         this.m_expandDTDEntities = true; 
         this.m_inDoctype = false;
         this.m_ispreserve = false;
         this.m_isprevtext = false;
         this.m_isUTF8 = false; 
         this.m_lineSep = s_systemLineSep;
         this.m_lineSepLen = s_systemLineSep.length;
         this.m_lineSepUse = true;
         this.m_preserves.clear();
         this.m_shouldFlush = true;
         this.m_spaceBeforeClose = false;
         this.m_startNewLine = false;
         this.m_writer_set_by_user = false;
    }        
     public void setEncoding(String encoding)
     {
         setOutputProperty(OutputKeys.ENCODING,encoding);
     }
    static final class BoolStack
    {
      private boolean m_values[];
      private int m_allocatedSize;
      private int m_index;
      public BoolStack()
      {
        this(32);
      }
      public BoolStack(int size)
      {
        m_allocatedSize = size;
        m_values = new boolean[size];
        m_index = -1;
      }
      public final int size()
      {
        return m_index + 1;
      }
      public final void clear()
      {
        m_index = -1;
      }
      public final boolean push(boolean val)
      {
        if (m_index == m_allocatedSize - 1)
          grow();
        return (m_values[++m_index] = val);
      }
      public final boolean pop()
      {
        return m_values[m_index--];
      }
      public final boolean popAndTop()
      {
        m_index--;
        return (m_index >= 0) ? m_values[m_index] : false;
      }
      public final void setTop(boolean b)
      {
        m_values[m_index] = b;
      }
      public final boolean peek()
      {
        return m_values[m_index];
      }
      public final boolean peekOrFalse()
      {
        return (m_index > -1) ? m_values[m_index] : false;
      }
      public final boolean peekOrTrue()
      {
        return (m_index > -1) ? m_values[m_index] : true;
      }
      public boolean isEmpty()
      {
        return (m_index == -1);
      }
      private void grow()
      {
        m_allocatedSize *= 2;
        boolean newVector[] = new boolean[m_allocatedSize];
        System.arraycopy(m_values, 0, newVector, 0, m_index + 1);
        m_values = newVector;
      }
    }
    public void notationDecl(String name, String pubID, String sysID) throws SAXException {
        try {
            DTDprolog();
            m_writer.write("<!NOTATION ");            
            m_writer.write(name);
            if (pubID != null) {
                m_writer.write(" PUBLIC \"");
                m_writer.write(pubID);
            }
            else {
                m_writer.write(" SYSTEM \"");
                m_writer.write(sysID);
            }
            m_writer.write("\" >");
            m_writer.write(m_lineSep, 0, m_lineSepLen);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void unparsedEntityDecl(String name, String pubID, String sysID, String notationName) throws SAXException {
        try {
            DTDprolog();       
            m_writer.write("<!ENTITY ");            
            m_writer.write(name);
            if (pubID != null) {
                m_writer.write(" PUBLIC \"");
                m_writer.write(pubID);
            }
            else {
                m_writer.write(" SYSTEM \"");
                m_writer.write(sysID);
            }
            m_writer.write("\" NDATA ");
            m_writer.write(notationName);
            m_writer.write(" >");
            m_writer.write(m_lineSep, 0, m_lineSepLen);
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }
    private void DTDprolog() throws SAXException, IOException {
        final java.io.Writer writer = m_writer;
        if (m_needToOutputDocTypeDecl)
        {
            outputDocTypeDecl(m_elemContext.m_elementName, false);
            m_needToOutputDocTypeDecl = false;
        }
        if (m_inDoctype)
        {
            writer.write(" [");
            writer.write(m_lineSep, 0, m_lineSepLen);
            m_inDoctype = false;
        }
    }
    public void setDTDEntityExpansion(boolean expand) { 
        m_expandDTDEntities = expand;     
    }
    public void setNewLine (char[] eolChars) {
        m_lineSep = eolChars;
        m_lineSepLen = eolChars.length;
    }
    public void addCdataSectionElements(String URI_and_localNames)
    {
        if (URI_and_localNames != null)
            initCdataElems(URI_and_localNames);
        if (m_StringOfCDATASections == null)
            m_StringOfCDATASections = URI_and_localNames;
        else
            m_StringOfCDATASections += (" " + URI_and_localNames);
    }
}
