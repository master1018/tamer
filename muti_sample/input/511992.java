public class AVT implements java.io.Serializable, XSLTVisitable
{
    static final long serialVersionUID = 5167607155517042691L;
  private final static boolean USE_OBJECT_POOL = false; 
  private final static int INIT_BUFFER_CHUNK_BITS = 8; 
  private String m_simpleString = null;
  private Vector m_parts = null;
  private String m_rawName;
  public String getRawName()
  {
    return m_rawName;
  }
  public void setRawName(String rawName)
  {
    m_rawName = rawName;
  }
  private String m_name;
  public String getName()
  {
    return m_name;
  }
  public void setName(String name)
  {
    m_name = name;
  }
  private String m_uri;
  public String getURI()
  {
    return m_uri;
  }
  public void setURI(String uri)
  {
    m_uri = uri;
  }
  public AVT(StylesheetHandler handler, String uri, String name, 
             String rawName, String stringedValue,
             ElemTemplateElement owner)
          throws javax.xml.transform.TransformerException
  {
    m_uri = uri;
    m_name = name;
    m_rawName = rawName;
    StringTokenizer tokenizer = new StringTokenizer(stringedValue, "{}\"\'",
                                  true);
    int nTokens = tokenizer.countTokens();
    if (nTokens < 2)
    {
      m_simpleString = stringedValue;  
    }
    else
    {
      FastStringBuffer buffer = null;
      FastStringBuffer exprBuffer = null;
      if(USE_OBJECT_POOL){
        buffer = StringBufferPool.get(); 
        exprBuffer = StringBufferPool.get();
      }else{
        buffer = new FastStringBuffer(6);
        exprBuffer = new FastStringBuffer(6);
      }
      try
      {
        m_parts = new Vector(nTokens + 1);
        String t = null;  
        String lookahead = null;  
        String error = null;  
        while (tokenizer.hasMoreTokens())
        {
          if (lookahead != null)
          {
            t = lookahead;
            lookahead = null;
          }
          else
            t = tokenizer.nextToken();
          if (t.length() == 1)
          {
            switch (t.charAt(0))
            {
            case ('\"') :
            case ('\'') :
            {
              buffer.append(t);
              break;
            }
            case ('{') :
            {
              try
              {
                lookahead = tokenizer.nextToken();
                if (lookahead.equals("{"))
                {
                  buffer.append(lookahead);
                  lookahead = null;
                  break;  
                }
                else
                {
                  if (buffer.length() > 0)
                  {
                    m_parts.addElement(new AVTPartSimple(buffer.toString()));
                    buffer.setLength(0);
                  }
                  exprBuffer.setLength(0);
                  while (null != lookahead)
                  {
                    if (lookahead.length() == 1)
                    {
                      switch (lookahead.charAt(0))
                      {
                      case '\'' :
                      case '\"' :
                        {
                          exprBuffer.append(lookahead);
                          String quote = lookahead;
                          lookahead = tokenizer.nextToken();
                          while (!lookahead.equals(quote))
                          {
                            exprBuffer.append(lookahead);
                            lookahead = tokenizer.nextToken();
                          }
                          exprBuffer.append(lookahead);
                          lookahead = tokenizer.nextToken();
                          break;
                        }
                      case '{' :
                        {
                          error = XSLMessages.createMessage(
                                                            XSLTErrorResources.ER_NO_CURLYBRACE, null);  
                          lookahead = null;  
                          break;
                        }
                      case '}' :
                        {
                          buffer.setLength(0);
                          XPath xpath =
                                       handler.createXPath(exprBuffer.toString(), owner);
                          m_parts.addElement(new AVTPartXPath(xpath));
                          lookahead = null;  
                          break;
                        }
                      default :
                        {
                          exprBuffer.append(lookahead);
                          lookahead = tokenizer.nextToken();
                        }
                      }  
                    }  
                    else
                    {
                      exprBuffer.append(lookahead);
                      lookahead = tokenizer.nextToken();
                    }
                  }  
                  if (error != null)
                  {
                    break;  
                  }
                }
                break;
              }
              catch (java.util.NoSuchElementException ex)
              {
                error = XSLMessages.createMessage(XSLTErrorResources.ER_ILLEGAL_ATTRIBUTE_VALUE, new Object[]{ name, stringedValue }); 
                break;
              }
            }
            case ('}') :
            {
              lookahead = tokenizer.nextToken();
              if (lookahead.equals("}"))
              {
                buffer.append(lookahead);
                lookahead = null;  
              }
              else
              {
                try
                {
                  handler.warn(XSLTErrorResources.WG_FOUND_CURLYBRACE, null);  
                }
                catch (org.xml.sax.SAXException se)
                {
                  throw new TransformerException(se);
                }
                buffer.append("}");
              }
              break;
            }
            default :
            {
              buffer.append(t);
            }
            }  
          }  
          else
          {
            buffer.append(t);
          }
          if (null != error)
          {
            try
            {
              handler.warn(XSLTErrorResources.WG_ATTR_TEMPLATE,
                           new Object[]{ error });  
            }
            catch (org.xml.sax.SAXException se)
            {
              throw new TransformerException(se);
            }
            break;
          }
        }  
        if (buffer.length() > 0)
        {
          m_parts.addElement(new AVTPartSimple(buffer.toString()));
          buffer.setLength(0);
        }
      }
      finally
      {
        if(USE_OBJECT_POOL){
             StringBufferPool.free(buffer);
             StringBufferPool.free(exprBuffer);
         }else{
            buffer = null;
            exprBuffer = null;
         };
      }
    }  
    if (null == m_parts && (null == m_simpleString))
    {
      m_simpleString = "";
    }
  }
  public String getSimpleString()
  {
    if (null != m_simpleString){
      return m_simpleString;
    }
    else if (null != m_parts){
     final FastStringBuffer buf = getBuffer();
     String out = null;
    int n = m_parts.size();
    try{
      for (int i = 0; i < n; i++){
        AVTPart part = (AVTPart) m_parts.elementAt(i);
        buf.append(part.getSimpleString());
      }
      out = buf.toString();
    }finally{
      if(USE_OBJECT_POOL){
         StringBufferPool.free(buf);
     }else{
        buf.setLength(0); 
     };
    }
    return out;
  }else{
      return "";
  }
}
  public String evaluate(
          XPathContext xctxt, int context, org.apache.xml.utils.PrefixResolver nsNode)
            throws javax.xml.transform.TransformerException
  {
    if (null != m_simpleString){
        return m_simpleString;
    }else if (null != m_parts){
      final FastStringBuffer buf =getBuffer();
      String out = null;
      int n = m_parts.size();
      try{
        for (int i = 0; i < n; i++){
          AVTPart part = (AVTPart) m_parts.elementAt(i);  
          part.evaluate(xctxt, buf, context, nsNode);
        }
       out = buf.toString();
      }finally{
          if(USE_OBJECT_POOL){
             StringBufferPool.free(buf);
         }else{
           buf.setLength(0); 
         }
      }
     return out;
    }else{
      return "";
    }
  }
  public boolean isContextInsensitive()
  {
    return null != m_simpleString;
  }
  public boolean canTraverseOutsideSubtree()
  {
    if (null != m_parts)
    {
      int n = m_parts.size();
      for (int i = 0; i < n; i++)
      {
        AVTPart part = (AVTPart) m_parts.elementAt(i);
        if (part.canTraverseOutsideSubtree())
          return true;
      }
    }
    return false;
  }
  public void fixupVariables(java.util.Vector vars, int globalsSize)
  {
    if (null != m_parts)
    {
      int n = m_parts.size();
      for (int i = 0; i < n; i++)
      {
        AVTPart part = (AVTPart) m_parts.elementAt(i);
        part.fixupVariables(vars, globalsSize);
      }
    }
  }
  public void callVisitors(XSLTVisitor visitor)
  {
  	if(visitor.visitAVT(this) && (null != m_parts))
  	{
      int n = m_parts.size();
      for (int i = 0; i < n; i++)
      {
        AVTPart part = (AVTPart) m_parts.elementAt(i);
        part.callVisitors(visitor);
      }  		
  	}
  }
  public boolean isSimple() {
  	return m_simpleString != null;
  }
  private final FastStringBuffer getBuffer(){
    if(USE_OBJECT_POOL){
      return StringBufferPool.get();
    }else{
      return new FastStringBuffer(INIT_BUFFER_CHUNK_BITS);
    }
  }
}
