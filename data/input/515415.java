abstract class Strings
{
    public static final String WHITE_SPACE = " \t\r\n";
    public static String toListForm (final String [] strings, final char delimiter)
    {
        if (strings == null) return null;
        if (strings.length == 0) return "";
        final StringBuffer s = new StringBuffer ();
        for (int i = 0, iLimit = strings.length; i < iLimit; ++ i)
        {
            if (i != 0) s.append (delimiter);
            s.append (strings [i]);
        }
        return s.toString ();
    }
    public static String [] removeDuplicates (final String [] strings, final boolean removeNull)
    {
        if (strings == null) return strings;
        final int length = strings.length;
        if (length == 0) return strings;
        final Set  _strings = new HashSet (length);
        final List  _result = new ArrayList (length);
        for (int i = 0; i < length; ++ i)
        {
            final String s = strings [i];
            if (removeNull && (s == null)) continue;
            if (_strings.add (s)) _result.add (s);
        }
        final int resultLength = _result.size (); 
        if (resultLength == length)
            return strings;
        else
        {
            final String [] result = new String [resultLength];
            _result.toArray (result);
            return result; 
        }
    }
    public static String [] merge (final String [] strings, final String delimiters, final boolean removeNull)
    {
        if (strings == null) return strings;
        final int length = strings.length;
        if (length == 0) return strings;
        if ((delimiters == null) || (delimiters.length () == 0))
            throw new IllegalArgumentException ("null/empty input: delimiters");
        final Set  _strings = new HashSet (length);
        final List  _result = new ArrayList (length);
        for (int i = 0; i < length; ++ i)
        {
            final String s = strings [i];
            if (removeNull && (s == null)) continue;
            final StringTokenizer tokenizer = new StringTokenizer (s, delimiters);
            while (tokenizer.hasMoreTokens ())
            {
                final String ss = tokenizer.nextToken ();
                if (_strings.add (ss)) _result.add (ss);
            }
        }
        final String [] result = new String [_result.size ()];
        _result.toArray (result);
        return result; 
    }
    public static String [] mergeAT (final String [] strings, final String delimiters, final boolean processAtFiles)
        throws IOException
    {
        if (! processAtFiles)
            return merge (strings, delimiters, true);
        else
        {
            if (strings == null) return strings;
            final int length = strings.length;
            if (length == 0) return strings;
            if ((delimiters == null) || (delimiters.length () == 0))
                throw new IllegalArgumentException ("null/empty input: delimiters");
            final Set  _strings = new HashSet (length);
            final List  _result = new ArrayList (length);
            for (int i = 0; i < length; ++ i)
            {
                final String s = strings [i];
                if (s == null) continue;
                final StringTokenizer tokenizer = new StringTokenizer (s, delimiters);
                while (tokenizer.hasMoreTokens ())
                {
                    final String ss = tokenizer.nextToken ();
                    if (ss.startsWith ("@"))
                    {
                        final String [] fileList = Files.readFileList (new File (ss.substring (1)));
                        for (int j = 0; j < fileList.length; ++ j)
                        {
                            final String sss = fileList [j];
                            if (_strings.add (sss)) _result.add (sss);
                        }
                    }
                    else if (_strings.add (ss)) _result.add (ss);
                }
            }
            final String [] result = new String [_result.size ()];
            _result.toArray (result);
            return result;
        }
    }
    public static void HTMLEscape (final String s, final StringBuffer append)
    {
        if (s == null) throw new IllegalArgumentException ("null input: s");
        if (append == null) throw new IllegalArgumentException ("null input: append");
        final char [] chars;
        if (USE_GET_CHARS) chars = s.toCharArray (); 
        for (int i = 0, iLimit = s.length (); i < iLimit; ++ i)
        {
            final char c = USE_GET_CHARS ? chars [i] : s.charAt (i);
            switch (c)
            {
                case '<':
                    append.append ("&lt;");
                    break;
                case '>':
                    append.append ("&gt;");
                    break;
                case '"':
                    append.append ("&#34;");
                    break;
                case '&':
                    append.append ("&amp;");
                    break;
                default:
                    append.append (c);    
            } 
        }
    }
    public static void HTMLEscapeNB (final String s, final StringBuffer append)
    {
        if (s == null) throw new IllegalArgumentException ("null input: s");
        if (append == null) throw new IllegalArgumentException ("null input: append");
        final char [] chars;
        if (USE_GET_CHARS) chars = s.toCharArray (); 
        for (int i = 0, iLimit = s.length (); i < iLimit; ++ i)
        {
            final char c = USE_GET_CHARS ? chars [i] : s.charAt (i);
            switch (c)
            {
                case ' ':
                    append.append ('\u00A0'); 
                    break;
                case '\t':
                    append.append ("\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0"); 
                    break;
                case '<':
                    append.append ("&lt;");
                    break;
                case '>':
                    append.append ("&gt;");
                    break;
                case '"':
                    append.append ("&#34;");
                    break;
                case '&':
                    append.append ("&amp;");
                    break;
                default:
                    append.append (c);    
            } 
        }
    }
    public static String HTMLEscape (final String s)
    {
        final StringBuffer buf = new StringBuffer ();
        HTMLEscape (s, buf);
        return buf.toString ();
    }
    public static String HTMLEscapeSP (final String s)
    {
        final StringBuffer buf = new StringBuffer ();
        HTMLEscapeNB (s, buf);
        return buf.toString ();
    }
    private Strings () {} 
    private static final boolean USE_GET_CHARS = true;
} 
