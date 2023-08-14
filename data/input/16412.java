class MimeType implements Serializable, Cloneable {
    private static final long serialVersionUID = -2785720609362367683L;
    private String[] myPieces;
    private transient String myStringValue = null;
    private transient ParameterMapEntrySet myEntrySet = null;
    private transient ParameterMap myParameterMap = null;
    private class ParameterMapEntry implements Map.Entry {
        private int myIndex;
        public ParameterMapEntry(int theIndex) {
            myIndex = theIndex;
        }
        public Object getKey(){
            return myPieces[myIndex];
        }
        public Object getValue(){
            return myPieces[myIndex+1];
        }
        public Object setValue (Object value) {
            throw new UnsupportedOperationException();
        }
        public boolean equals(Object o) {
            return (o != null &&
                    o instanceof Map.Entry &&
                    getKey().equals (((Map.Entry) o).getKey()) &&
                    getValue().equals(((Map.Entry) o).getValue()));
        }
        public int hashCode() {
            return getKey().hashCode() ^ getValue().hashCode();
        }
    }
    private class ParameterMapEntrySetIterator implements Iterator {
        private int myIndex = 2;
        public boolean hasNext() {
            return myIndex < myPieces.length;
        }
        public Object next() {
            if (hasNext()) {
                ParameterMapEntry result = new ParameterMapEntry (myIndex);
                myIndex += 2;
                return result;
            } else {
                throw new NoSuchElementException();
            }
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    private class ParameterMapEntrySet extends AbstractSet {
        public Iterator iterator() {
            return new ParameterMapEntrySetIterator();
        }
        public int size() {
            return (myPieces.length - 2) / 2;
        }
    }
    private class ParameterMap extends AbstractMap {
        public Set entrySet() {
            if (myEntrySet == null) {
                myEntrySet = new ParameterMapEntrySet();
            }
            return myEntrySet;
        }
    }
    public MimeType(String s) {
        parse (s);
    }
    public String getMimeType() {
        return getStringValue();
    }
    public String getMediaType() {
        return myPieces[0];
    }
    public String getMediaSubtype() {
        return myPieces[1];
    }
    public Map getParameterMap() {
        if (myParameterMap == null) {
            myParameterMap = new ParameterMap();
        }
        return myParameterMap;
    }
    public String toString() {
        return getStringValue();
    }
    public int hashCode() {
        return getStringValue().hashCode();
    }
    public boolean equals (Object obj) {
        return(obj != null &&
               obj instanceof MimeType &&
               getStringValue().equals(((MimeType) obj).getStringValue()));
    }
    private String getStringValue() {
        if (myStringValue == null) {
            StringBuffer result = new StringBuffer();
            result.append (myPieces[0]);
            result.append ('/');
            result.append (myPieces[1]);
            int n = myPieces.length;
            for (int i = 2; i < n; i += 2) {
                result.append(';');
                result.append(' ');
                result.append(myPieces[i]);
                result.append('=');
                result.append(addQuotes (myPieces[i+1]));
            }
            myStringValue = result.toString();
        }
        return myStringValue;
    }
    private static final int TOKEN_LEXEME         = 0;
    private static final int QUOTED_STRING_LEXEME = 1;
    private static final int TSPECIAL_LEXEME      = 2;
    private static final int EOF_LEXEME           = 3;
    private static final int ILLEGAL_LEXEME       = 4;
    private static class LexicalAnalyzer {
        protected String mySource;
        protected int mySourceLength;
        protected int myCurrentIndex;
        protected int myLexemeType;
        protected int myLexemeBeginIndex;
        protected int myLexemeEndIndex;
        public LexicalAnalyzer(String theSource) {
            mySource = theSource;
            mySourceLength = theSource.length();
            myCurrentIndex = 0;
            nextLexeme();
        }
        public int getLexemeType() {
            return myLexemeType;
        }
        public String getLexeme() {
            return(myLexemeBeginIndex >= mySourceLength ?
                   null :
                   mySource.substring(myLexemeBeginIndex, myLexemeEndIndex));
        }
        public char getLexemeFirstCharacter() {
            return(myLexemeBeginIndex >= mySourceLength ?
                   '\u0000' :
                   mySource.charAt(myLexemeBeginIndex));
        }
        public void nextLexeme() {
            int state = 0;
            int commentLevel = 0;
            char c;
            while (state >= 0) {
                switch (state) {
                case 0:
                    if (myCurrentIndex >= mySourceLength) {
                        myLexemeType = EOF_LEXEME;
                        myLexemeBeginIndex = mySourceLength;
                        myLexemeEndIndex = mySourceLength;
                        state = -1;
                    } else if (Character.isWhitespace
                               (c = mySource.charAt (myCurrentIndex ++))) {
                        state = 0;
                    } else if (c == '\"') {
                        myLexemeType = QUOTED_STRING_LEXEME;
                        myLexemeBeginIndex = myCurrentIndex;
                        state = 1;
                    } else if (c == '(') {
                        ++ commentLevel;
                        state = 3;
                    } else if (c == '/'  || c == ';' || c == '=' ||
                               c == ')'  || c == '<' || c == '>' ||
                               c == '@'  || c == ',' || c == ':' ||
                               c == '\\' || c == '[' || c == ']' ||
                               c == '?') {
                        myLexemeType = TSPECIAL_LEXEME;
                        myLexemeBeginIndex = myCurrentIndex - 1;
                        myLexemeEndIndex = myCurrentIndex;
                        state = -1;
                    } else {
                        myLexemeType = TOKEN_LEXEME;
                        myLexemeBeginIndex = myCurrentIndex - 1;
                        state = 5;
                    }
                    break;
                case 1:
                    if (myCurrentIndex >= mySourceLength) {
                        myLexemeType = ILLEGAL_LEXEME;
                        myLexemeBeginIndex = mySourceLength;
                        myLexemeEndIndex = mySourceLength;
                        state = -1;
                    } else if ((c = mySource.charAt (myCurrentIndex ++)) == '\"') {
                        myLexemeEndIndex = myCurrentIndex - 1;
                        state = -1;
                    } else if (c == '\\') {
                        state = 2;
                    } else {
                        state = 1;
                    }
                    break;
                case 2:
                    if (myCurrentIndex >= mySourceLength) {
                        myLexemeType = ILLEGAL_LEXEME;
                        myLexemeBeginIndex = mySourceLength;
                        myLexemeEndIndex = mySourceLength;
                        state = -1;
                    } else {
                        ++ myCurrentIndex;
                        state = 1;
                    } break;
                case 3: if (myCurrentIndex >= mySourceLength) {
                    myLexemeType = ILLEGAL_LEXEME;
                    myLexemeBeginIndex = mySourceLength;
                    myLexemeEndIndex = mySourceLength;
                    state = -1;
                } else if ((c = mySource.charAt (myCurrentIndex ++)) == '(') {
                    ++ commentLevel;
                    state = 3;
                } else if (c == ')') {
                    -- commentLevel;
                    state = commentLevel == 0 ? 0 : 3;
                } else if (c == '\\') {
                    state = 4;
                } else { state = 3;
                }
                break;
                case 4:
                    if (myCurrentIndex >= mySourceLength) {
                        myLexemeType = ILLEGAL_LEXEME;
                        myLexemeBeginIndex = mySourceLength;
                        myLexemeEndIndex = mySourceLength;
                        state = -1;
                    } else {
                        ++ myCurrentIndex;
                        state = 3;
                    }
                    break;
                case 5:
                    if (myCurrentIndex >= mySourceLength) {
                        myLexemeEndIndex = myCurrentIndex;
                        state = -1;
                    } else if (Character.isWhitespace
                               (c = mySource.charAt (myCurrentIndex ++))) {
                        myLexemeEndIndex = myCurrentIndex - 1;
                        state = -1;
                    } else if (c == '\"' || c == '(' || c == '/' ||
                               c == ';'  || c == '=' || c == ')' ||
                               c == '<' || c == '>'  || c == '@' ||
                               c == ',' || c == ':' || c == '\\' ||
                               c == '[' || c == ']' || c == '?') {
                        -- myCurrentIndex;
                        myLexemeEndIndex = myCurrentIndex;
                        state = -1;
                    } else {
                        state = 5;
                    }
                    break;
                }
            }
        }
    }
    private static String toUnicodeLowerCase(String s) {
        int n = s.length();
        char[] result = new char [n];
        for (int i = 0; i < n; ++ i) {
            result[i] = Character.toLowerCase (s.charAt (i));
        }
        return new String (result);
    }
    private static String removeBackslashes(String s) {
        int n = s.length();
        char[] result = new char [n];
        int i;
        int j = 0;
        char c;
        for (i = 0; i < n; ++ i) {
            c = s.charAt (i);
            if (c == '\\') {
                c = s.charAt (++ i);
            }
            result[j++] = c;
        }
        return new String (result, 0, j);
    }
    private static String addQuotes(String s) {
        int n = s.length();
        int i;
        char c;
        StringBuffer result = new StringBuffer (n+2);
        result.append ('\"');
        for (i = 0; i < n; ++ i) {
            c = s.charAt (i);
            if (c == '\"') {
                result.append ('\\');
            }
            result.append (c);
        }
        result.append ('\"');
        return result.toString();
    }
    private void parse(String s) {
        if (s == null) {
            throw new NullPointerException();
        }
        LexicalAnalyzer theLexer = new LexicalAnalyzer (s);
        int theLexemeType;
        Vector thePieces = new Vector();
        boolean mediaTypeIsText = false;
        boolean parameterNameIsCharset = false;
        if (theLexer.getLexemeType() == TOKEN_LEXEME) {
            String mt = toUnicodeLowerCase (theLexer.getLexeme());
            thePieces.add (mt);
            theLexer.nextLexeme();
            mediaTypeIsText = mt.equals ("text");
        } else {
            throw new IllegalArgumentException();
        }
        if (theLexer.getLexemeType() == TSPECIAL_LEXEME &&
              theLexer.getLexemeFirstCharacter() == '/') {
            theLexer.nextLexeme();
        } else {
            throw new IllegalArgumentException();
        }
        if (theLexer.getLexemeType() == TOKEN_LEXEME) {
            thePieces.add (toUnicodeLowerCase (theLexer.getLexeme()));
            theLexer.nextLexeme();
        } else {
            throw new IllegalArgumentException();
        }
        while (theLexer.getLexemeType() == TSPECIAL_LEXEME &&
               theLexer.getLexemeFirstCharacter() == ';') {
            theLexer.nextLexeme();
            if (theLexer.getLexemeType() == TOKEN_LEXEME) {
                String pn = toUnicodeLowerCase (theLexer.getLexeme());
                thePieces.add (pn);
                theLexer.nextLexeme();
                parameterNameIsCharset = pn.equals ("charset");
            } else {
                throw new IllegalArgumentException();
            }
            if (theLexer.getLexemeType() == TSPECIAL_LEXEME &&
                theLexer.getLexemeFirstCharacter() == '=') {
                theLexer.nextLexeme();
            } else {
                throw new IllegalArgumentException();
            }
            if (theLexer.getLexemeType() == TOKEN_LEXEME) {
                String pv = theLexer.getLexeme();
                thePieces.add(mediaTypeIsText && parameterNameIsCharset ?
                              toUnicodeLowerCase (pv) :
                              pv);
                theLexer.nextLexeme();
            } else if (theLexer.getLexemeType() == QUOTED_STRING_LEXEME) {
                String pv = removeBackslashes (theLexer.getLexeme());
                thePieces.add(mediaTypeIsText && parameterNameIsCharset ?
                              toUnicodeLowerCase (pv) :
                              pv);
                theLexer.nextLexeme();
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (theLexer.getLexemeType() != EOF_LEXEME) {
            throw new IllegalArgumentException();
        }
        int n = thePieces.size();
        myPieces = (String[]) thePieces.toArray (new String [n]);
        int i, j;
        String temp;
        for (i = 4; i < n; i += 2) {
            j = 2;
            while (j < i && myPieces[j].compareTo (myPieces[i]) <= 0) {
                j += 2;
            }
            while (j < i) {
                temp = myPieces[j];
                myPieces[j] = myPieces[i];
                myPieces[i] = temp;
                temp = myPieces[j+1];
                myPieces[j+1] = myPieces[i+1];
                myPieces[i+1] = temp;
                j += 2;
            }
        }
    }
}
