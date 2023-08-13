final class CharInfo
{
    private HashMap m_charToString;
    public static final String HTML_ENTITIES_RESOURCE = 
                SerializerBase.PKG_NAME+".HTMLEntities";
    public static final String XML_ENTITIES_RESOURCE = 
                SerializerBase.PKG_NAME+".XMLEntities";
    static final char S_HORIZONAL_TAB = 0x09;
    static final char S_LINEFEED = 0x0A;
    static final char S_CARRIAGERETURN = 0x0D;
    static final char S_SPACE = 0x20;
    static final char S_QUOTE = 0x22;
    static final char S_LT = 0x3C;
    static final char S_GT = 0x3E;
    static final char S_NEL = 0x85;    
    static final char S_LINE_SEPARATOR = 0x2028;
    boolean onlyQuotAmpLtGt;
    static final int ASCII_MAX = 128;
    private final boolean[] shouldMapAttrChar_ASCII;
    private final boolean[] shouldMapTextChar_ASCII;
    private final int array_of_bits[];
    private static final int SHIFT_PER_WORD = 5;
    private static final int LOW_ORDER_BITMASK = 0x1f;
    private int firstWordNotUsed;
    private CharInfo() 
    {
    	this.array_of_bits = createEmptySetOfIntegers(65535);
    	this.firstWordNotUsed = 0;
    	this.shouldMapAttrChar_ASCII = new boolean[ASCII_MAX];
    	this.shouldMapTextChar_ASCII = new boolean[ASCII_MAX];
    	this.m_charKey = new CharKey();
    	this.onlyQuotAmpLtGt = true;
    	return;
    }
    private CharInfo(String entitiesResource, String method, boolean internal)
    {
    	this();
    	m_charToString = new HashMap();
        ResourceBundle entities = null;
        boolean noExtraEntities = true;
        if (internal) { 
            try {
                entities = PropertyResourceBundle.getBundle(entitiesResource);
            } catch (Exception e) {}
        }
        if (entities != null) {
            Enumeration keys = entities.getKeys();
            while (keys.hasMoreElements()){
                String name = (String) keys.nextElement();
                String value = entities.getString(name);
                int code = Integer.parseInt(value);
                boolean extra = defineEntity(name, (char) code);
                if (extra)
                    noExtraEntities = false;
            }
        } else {
            InputStream is = null;
            try {
                if (internal) {
                    is = CharInfo.class.getResourceAsStream(entitiesResource);
                } else {
                    ClassLoader cl = ObjectFactory.findClassLoader();
                    if (cl == null) {
                        is = ClassLoader.getSystemResourceAsStream(entitiesResource);
                    } else {
                        is = cl.getResourceAsStream(entitiesResource);
                    }
                    if (is == null) {
                        try {
                            URL url = new URL(entitiesResource);
                            is = url.openStream();
                        } catch (Exception e) {}
                    }
                }
                if (is == null) {
                    throw new RuntimeException(
                        Utils.messages.createMessage(
                            MsgKey.ER_RESOURCE_COULD_NOT_FIND,
                            new Object[] {entitiesResource, entitiesResource}));
                }
                BufferedReader reader;
                try {
                    reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    reader = new BufferedReader(new InputStreamReader(is));
                }
                String line = reader.readLine();
                while (line != null) {
                    if (line.length() == 0 || line.charAt(0) == '#') {
                        line = reader.readLine();
                        continue;
                    }
                    int index = line.indexOf(' ');
                    if (index > 1) {
                        String name = line.substring(0, index);
                        ++index;
                        if (index < line.length()) {
                            String value = line.substring(index);
                            index = value.indexOf(' ');
                            if (index > 0) {
                                value = value.substring(0, index);
                            }
                            int code = Integer.parseInt(value);
                            boolean extra = defineEntity(name, (char) code);
                            if (extra)
                                noExtraEntities = false;
                        }
                    }
                    line = reader.readLine();
                }
                is.close();
            } catch (Exception e) {
                throw new RuntimeException(
                    Utils.messages.createMessage(
                        MsgKey.ER_RESOURCE_COULD_NOT_LOAD,
                        new Object[] { entitiesResource,
                                       e.toString(),
                                       entitiesResource,
                                       e.toString()}));
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception except) {}
                }
            }
        }
        onlyQuotAmpLtGt = noExtraEntities;
        if (Method.XML.equals(method)) 
        {       
            shouldMapTextChar_ASCII[S_QUOTE] = false;
        }
        if (Method.HTML.equals(method)) {
        	shouldMapAttrChar_ASCII['<'] = false;    
            shouldMapTextChar_ASCII[S_QUOTE] = false;
        }
    }
    private boolean defineEntity(String name, char value)
    {
        StringBuffer sb = new StringBuffer("&");
        sb.append(name);
        sb.append(';');
        String entityString = sb.toString();
        boolean extra = defineChar2StringMapping(entityString, value);
        return extra;
    }
    private final CharKey m_charKey;
    String getOutputStringForChar(char value)
    {
        m_charKey.setChar(value);
        return (String) m_charToString.get(m_charKey);
    }
    final boolean shouldMapAttrChar(int value)
    {
        if (value < ASCII_MAX)
            return shouldMapAttrChar_ASCII[value];
        return get(value);
    }    
    final boolean shouldMapTextChar(int value)
    {
        if (value < ASCII_MAX)
            return shouldMapTextChar_ASCII[value];
        return get(value);
    }
    private static CharInfo getCharInfoBasedOnPrivilege(
        final String entitiesFileName, final String method, 
        final boolean internal){
            return (CharInfo) AccessController.doPrivileged(
                new PrivilegedAction() {
                        public Object run() {
                            return new CharInfo(entitiesFileName, 
                              method, internal);}
            });            
    }
    static CharInfo getCharInfo(String entitiesFileName, String method)
    {
        CharInfo charInfo = (CharInfo) m_getCharInfoCache.get(entitiesFileName);
        if (charInfo != null) {
        	return mutableCopyOf(charInfo);
        }
        try {
            charInfo = getCharInfoBasedOnPrivilege(entitiesFileName, 
                                        method, true);
            m_getCharInfoCache.put(entitiesFileName, charInfo);
            return mutableCopyOf(charInfo);
        } catch (Exception e) {}
        try {
            return getCharInfoBasedOnPrivilege(entitiesFileName, 
                                method, false);
        } catch (Exception e) {}
        String absoluteEntitiesFileName;
        if (entitiesFileName.indexOf(':') < 0) {
            absoluteEntitiesFileName =
                SystemIDResolver.getAbsoluteURIFromRelative(entitiesFileName);
        } else {
            try {
                absoluteEntitiesFileName =
                    SystemIDResolver.getAbsoluteURI(entitiesFileName, null);
            } catch (TransformerException te) {
                throw new WrappedRuntimeException(te);
            }
        }
        return getCharInfoBasedOnPrivilege(entitiesFileName, 
                                method, false);
    }
    private static CharInfo mutableCopyOf(CharInfo charInfo) {
    	CharInfo copy = new CharInfo();
    	int max = charInfo.array_of_bits.length;
    	System.arraycopy(charInfo.array_of_bits,0,copy.array_of_bits,0,max);
    	copy.firstWordNotUsed = charInfo.firstWordNotUsed;
    	max = charInfo.shouldMapAttrChar_ASCII.length;
    	System.arraycopy(charInfo.shouldMapAttrChar_ASCII,0,copy.shouldMapAttrChar_ASCII,0,max);
    	max = charInfo.shouldMapTextChar_ASCII.length;
    	System.arraycopy(charInfo.shouldMapTextChar_ASCII,0,copy.shouldMapTextChar_ASCII,0,max);
    	copy.m_charToString = (HashMap) charInfo.m_charToString.clone();
    	copy.onlyQuotAmpLtGt = charInfo.onlyQuotAmpLtGt;
		return copy;
	}
    private static Hashtable m_getCharInfoCache = new Hashtable();
    private static int arrayIndex(int i) {
        return (i >> SHIFT_PER_WORD);
    }
    private static int bit(int i) {
        int ret = (1 << (i & LOW_ORDER_BITMASK));
        return ret;
    }
    private int[] createEmptySetOfIntegers(int max) {
        firstWordNotUsed = 0; 
        int[] arr = new int[arrayIndex(max - 1) + 1];
            return arr;
    }
    private final void set(int i) {   
        setASCIItextDirty(i);
        setASCIIattrDirty(i); 
        int j = (i >> SHIFT_PER_WORD); 
        int k = j + 1;       
        if(firstWordNotUsed < k) 
            firstWordNotUsed = k;
        array_of_bits[j] |= (1 << (i & LOW_ORDER_BITMASK));
    }
    private final boolean get(int i) {
        boolean in_the_set = false;
        int j = (i >> SHIFT_PER_WORD); 
        if(j < firstWordNotUsed)
            in_the_set = (array_of_bits[j] & 
                          (1 << (i & LOW_ORDER_BITMASK))
            ) != 0;  
        return in_the_set;
    }
    private boolean extraEntity(String outputString, int charToMap)
    {
        boolean extra = false;
        if (charToMap < ASCII_MAX)
        {
            switch (charToMap)
            {
                case '"' : 
                	if (!outputString.equals("&quot;"))
                		extra = true;  
                	break;
                case '&' : 
                	if (!outputString.equals("&amp;"))
                		extra = true;
                	break;
                case '<' : 
                	if (!outputString.equals("&lt;"))
                		extra = true;
                	break;
                case '>' : 
                	if (!outputString.equals("&gt;"))
                		extra = true;
                    break;
                default : 
                    extra = true;
            }
        }
        return extra;
    }    
    private void setASCIItextDirty(int j) 
    {
        if (0 <= j && j < ASCII_MAX) 
        {
            shouldMapTextChar_ASCII[j] = true;
        } 
    }
    private void setASCIIattrDirty(int j) 
    {
        if (0 <= j && j < ASCII_MAX) 
        {
            shouldMapAttrChar_ASCII[j] = true;
        } 
    }
    boolean defineChar2StringMapping(String outputString, char inputChar) 
    {
        CharKey character = new CharKey(inputChar);
        m_charToString.put(character, outputString);
        set(inputChar);  
        boolean extraMapping = extraEntity(outputString, inputChar);
        return extraMapping;
    }
    private static class CharKey extends Object
    {
      private char m_char;
      public CharKey(char key)
      {
        m_char = key;
      }
      public CharKey()
      {
      }
      public final void setChar(char c)
      {
        m_char = c;
      }
      public final int hashCode()
      {
        return (int)m_char;
      }
      public final boolean equals(Object obj)
      {
        return ((CharKey)obj).m_char == m_char;
      }
    }
}
