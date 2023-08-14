class DTDParser implements DTDConstants {
    DTDBuilder dtd;
    DTDInputStream in;
    int ch;
    char str[] = new char[128];
    int strpos = 0;
    int nerrors = 0;
    void error(String err, String arg1, String arg2, String arg3) {
        nerrors++;
        String msgParams[] = {arg1, arg2, arg3};
        String str = getSubstProp("dtderr." + err, msgParams);
        if (str == null) {
            str = err + "[" + arg1 + "," + arg2 + "," + arg3 + "]";
        }
        System.err.println("line " + in.ln + ", dtd " + dtd + ": " + str);
    }
    void error(String err, String arg1, String arg2) {
        error(err, arg1, arg2, "?");
    }
    void error(String err, String arg1) {
        error(err, arg1, "?", "?");
    }
    void error(String err) {
        error(err, "?", "?", "?");
    }
    private String getSubstProp(String propName, String args[]) {
        String prop = System.getProperty(propName);
        if (prop == null) {
            return null;
        }
        return MessageFormat.format(prop, args);
    }
    boolean expect(int c) throws IOException {
        if (ch != c) {
            char str[] = {(char)c};
            error("expected", "'" + new String(str) + "'");
            return false;
        }
        ch = in.read();
        return true;
    }
    void addString(int c) {
        if (strpos == str.length) {
            char newstr[] = new char[str.length * 2];
            System.arraycopy(str, 0, newstr, 0, str.length);
            str = newstr;
        }
        str[strpos++] = (char)c;
    }
    String getString(int pos) {
        char newstr[] = new char[strpos - pos];
        System.arraycopy(str, pos, newstr, 0, strpos - pos);
        strpos = pos;
        return new String(newstr);
    }
    char[] getChars(int pos) {
        char newstr[] = new char[strpos - pos];
        System.arraycopy(str, pos, newstr, 0, strpos - pos);
        strpos = pos;
        return newstr;
    }
    void skipSpace() throws IOException {
        while (true) {
            switch (ch) {
              case '\n':
              case ' ':
              case '\t':
                ch = in.read();
                break;
              default:
                return;
            }
        }
    }
    void skipParameterSpace() throws IOException {
        while (true) {
            switch (ch) {
              case '\n':
              case ' ':
              case '\t':
                ch = in.read();
                break;
              case '-':
                if ((ch = in.read()) != '-') {
                    in.push(ch);
                    ch = '-';
                    return;
                }
                in.replace++;
                while (true) {
                    switch (ch = in.read()) {
                      case '-':
                        if ((ch = in.read()) == '-') {
                            ch = in.read();
                            in.replace--;
                            skipParameterSpace();
                            return;
                        }
                        break;
                      case -1:
                        error("eof.arg", "comment");
                        in.replace--;
                        return;
                    }
                }
              default:
                return;
            }
        }
    }
    boolean parseIdentifier(boolean lower) throws IOException {
        switch (ch) {
          case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
          case 'G': case 'H': case 'I': case 'J': case 'K': case 'L':
          case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R':
          case 'S': case 'T': case 'U': case 'V': case 'W': case 'X':
          case 'Y': case 'Z':
            if (lower) {
                ch = 'a' + (ch - 'A');
            }
          case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
          case 'g': case 'h': case 'i': case 'j': case 'k': case 'l':
          case 'm': case 'n': case 'o': case 'p': case 'q': case 'r':
          case 's': case 't': case 'u': case 'v': case 'w': case 'x':
          case 'y': case 'z':
            break;
          default:
            return false;
        }
        addString(ch);
        ch = in.read();
        parseNameToken(lower);
        return true;
    }
    boolean parseNameToken(boolean lower) throws IOException {
        boolean first = true;
        while (true) {
            switch (ch) {
              case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
              case 'G': case 'H': case 'I': case 'J': case 'K': case 'L':
              case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R':
              case 'S': case 'T': case 'U': case 'V': case 'W': case 'X':
              case 'Y': case 'Z':
                if (lower) {
                    ch = 'a' + (ch - 'A');
                }
              case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
              case 'g': case 'h': case 'i': case 'j': case 'k': case 'l':
              case 'm': case 'n': case 'o': case 'p': case 'q': case 'r':
              case 's': case 't': case 'u': case 'v': case 'w': case 'x':
              case 'y': case 'z':
              case '0': case '1': case '2': case '3': case '4':
              case '5': case '6': case '7': case '8': case '9':
              case '.': case '-':
                addString(ch);
                ch = in.read();
                first = false;
                break;
              default:
                return !first;
            }
        }
    }
    Vector parseIdentifierList(boolean lower) throws IOException {
        Vector elems = new Vector();
        skipSpace();
        switch (ch) {
          case '(':
            ch = in.read();
            skipParameterSpace();
            while (parseNameToken(lower)) {
                elems.addElement(getString(0));
                skipParameterSpace();
                if (ch == '|') {
                    ch = in.read();
                    skipParameterSpace();
                }
            }
            expect(')');
            skipParameterSpace();
            break;
          default:
            if (!parseIdentifier(lower)) {
                error("expected", "identifier");
                break;
            }
            elems.addElement(getString(0));
            skipParameterSpace();
            break;
        }
        return elems;
    }
    private void parseEntityReference() throws IOException {
        int pos = strpos;
        if ((ch = in.read()) == '#') {
            int n = 0;
            ch = in.read();
            if (((ch >= 'a') && (ch <= 'z')) || ((ch >= 'A') && (ch <= 'Z'))) {
                addString('#');
            } else {
                while ((ch >= '0') && (ch <= '9')) {
                    n = (n * 10) + ch - '0';
                    ch = in.read();
                }
                if ((ch == ';') || (ch == '\n')) {
                    ch = in.read();
                }
                addString(n);
                return;
            }
        }
        while (true) {
            switch (ch) {
              case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
              case 'G': case 'H': case 'I': case 'J': case 'K': case 'L':
              case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R':
              case 'S': case 'T': case 'U': case 'V': case 'W': case 'X':
              case 'Y': case 'Z':
              case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
              case 'g': case 'h': case 'i': case 'j': case 'k': case 'l':
              case 'm': case 'n': case 'o': case 'p': case 'q': case 'r':
              case 's': case 't': case 'u': case 'v': case 'w': case 'x':
              case 'y': case 'z':
              case '0': case '1': case '2': case '3': case '4':
              case '5': case '6': case '7': case '8': case '9':
              case '.': case '-':
                addString(ch);
                ch = in.read();
                break;
              default:
                if (strpos == pos) {
                    addString('&');
                    return;
                }
                String nm = getString(pos);
                Entity ent = dtd.getEntity(nm);
                if (ent == null) {
                    error("undef.entref" + nm);
                    return;
                }
                if ((ch == ';') || (ch == '\n')) {
                    ch = in.read();
                }
                char data[] = ent.getData();
                for (int i = 0 ; i < data.length ; i++) {
                    addString(data[i]);
                }
                return;
            }
        }
    }
    private void parseEntityDeclaration() throws IOException {
        int type = GENERAL;
        skipSpace();
        if (ch == '%') {
            ch = in.read();
            type = PARAMETER;
            skipSpace();
        }
        if (ch == '#') {
            addString('#');
            ch = in.read();
        }
        if (!parseIdentifier(false)) {
            error("expected", "identifier");
            return;
        }
        String nm = getString(0);
        skipParameterSpace();
        if (parseIdentifier(false)) {
            String tnm = getString(0);
            int t = Entity.name2type(tnm);
            if (t == 0) {
                error("invalid.arg", "entity type", tnm);
            } else {
                type |= t;
            }
            skipParameterSpace();
        }
        if ((ch != '"') && (ch != '\'')) {
            error("expected", "entity value");
            skipParameterSpace();
            if (ch == '>') {
                ch = in.read();
            }
            return;
        }
        int term = ch;
        ch = in.read();
        while ((ch != -1) && (ch != term)) {
            if (ch == '&') {
                parseEntityReference();
            } else {
                addString(ch & 0xFF);
                ch = in.read();
            }
        }
        if (ch == term) {
            ch = in.read();
        }
        if (in.replace == 0) {
            char data[] = getChars(0);
            dtd.defineEntity(nm, type, data);
        } else {
            strpos = 0;
        }
        skipParameterSpace();
        expect('>');
    }
    ContentModel parseContentModel() throws IOException {
        ContentModel m = null;
        switch (ch) {
          case '(':
            ch = in.read();
            skipParameterSpace();
            ContentModel e = parseContentModel();
            if (ch != ')') {
                m = new ContentModel(ch, e);
                do {
                    ch = in.read();
                    skipParameterSpace();
                    e.next = parseContentModel();
                    if (e.next.type == m.type) {
                        e.next = (ContentModel)e.next.content;
                    }
                    for (; e.next != null ; e = e.next);
                } while (ch == m.type);
            } else {
                m = new ContentModel(',', e);
            }
            expect(')');
            break;
          case '#':
            ch = in.read();
            if (parseIdentifier(true)) {
                m = new ContentModel('*', new ContentModel(dtd.getElement("#" + getString(0))));
            } else {
                error("invalid", "content model");
            }
            break;
          default:
            if (parseIdentifier(true)) {
                m = new ContentModel(dtd.getElement(getString(0)));
            } else {
                error("invalid", "content model");
            }
            break;
        }
        switch (ch) {
          case '?':
          case '*':
          case '+':
            m = new ContentModel(ch, m);
            ch = in.read();
            break;
        }
        skipParameterSpace();
        return m;
    }
    void parseElementDeclaration() throws IOException {
        Vector elems = parseIdentifierList(true);
        BitSet inclusions = null;
        BitSet exclusions = null;
        boolean omitStart = false;
        boolean omitEnd = false;
        if ((ch == '-') || (ch == 'O')) {
            omitStart = ch == 'O';
            ch = in.read();
            skipParameterSpace();
            if ((ch == '-') || (ch == 'O')) {
                omitEnd = ch == 'O';
                ch = in.read();
                skipParameterSpace();
            } else {
                expect('-');
            }
        }
        int type = MODEL;
        ContentModel content = null;
        if (parseIdentifier(false)) {
            String nm = getString(0);
            type = Element.name2type(nm);
            if (type == 0) {
                error("invalid.arg", "content type", nm);
                type = EMPTY;
            }
            skipParameterSpace();
        } else {
            content = parseContentModel();
        }
        if ((type == MODEL) || (type == ANY)) {
            if (ch == '-') {
                ch = in.read();
                Vector v = parseIdentifierList(true);
                exclusions = new BitSet();
                for (Enumeration e = v.elements() ; e.hasMoreElements() ;) {
                    exclusions.set(dtd.getElement((String)e.nextElement()).getIndex());
                }
            }
            if (ch == '+') {
                ch = in.read();
                Vector v = parseIdentifierList(true);
                inclusions = new BitSet();
                for (Enumeration e = v.elements() ; e.hasMoreElements() ;) {
                    inclusions.set(dtd.getElement((String)e.nextElement()).getIndex());
                }
            }
        }
        expect('>');
        if (in.replace == 0) {
            for (Enumeration e = elems.elements() ; e.hasMoreElements() ;) {
                dtd.defineElement((String)e.nextElement(), type, omitStart, omitEnd, content, exclusions, inclusions, null);
            }
        }
    }
    void parseAttributeDeclaredValue(AttributeList atts) throws IOException {
        if (ch == '(') {
            atts.values = parseIdentifierList(true);
            atts.type = NMTOKEN;
            return;
        }
        if (!parseIdentifier(false)) {
            error("invalid", "attribute value");
            return;
        }
        atts.type = atts.name2type(getString(0));
        skipParameterSpace();
        if (atts.type == NOTATION) {
            atts.values = parseIdentifierList(true);
        }
    }
    String parseAttributeValueSpecification() throws IOException {
        int delim = -1;
        switch (ch) {
          case '\'':
          case '"':
            delim = ch;
            ch = in.read();
        }
        while (true) {
            switch (ch) {
              case -1:
                error("eof.arg", "attribute value");
                return getString(0);
              case '&':
                parseEntityReference();
                break;
              case ' ':
              case '\t':
              case '\n':
                if (delim == -1) {
                    return getString(0);
                }
                addString(' ');
                ch = in.read();
                break;
              case '\'':
              case '"':
                if (delim == ch) {
                    ch = in.read();
                    return getString(0);
                }
              default:
                addString(ch & 0xFF);
                ch = in.read();
                break;
            }
        }
    }
    void parseAttributeDefaultValue(AttributeList atts) throws IOException {
        if (ch == '#') {
            ch = in.read();
            if (!parseIdentifier(true)) {
                error("invalid", "attribute value");
                return;
            }
            skipParameterSpace();
            atts.modifier = atts.name2type(getString(0));
            if (atts.modifier != FIXED) {
                return;
            }
        }
        atts.value = parseAttributeValueSpecification();
        skipParameterSpace();
    }
    void parseAttlistDeclaration() throws IOException {
        Vector elems = parseIdentifierList(true);
        AttributeList attlist = null, atts = null;
        while (parseIdentifier(true)) {
            if (atts == null) {
                attlist = atts = new AttributeList(getString(0));
            } else {
                atts.next = new AttributeList(getString(0));
                atts = atts.next;
            }
            skipParameterSpace();
            parseAttributeDeclaredValue(atts);
            parseAttributeDefaultValue(atts);
            if ((atts.modifier == IMPLIED) && (atts.values != null) && (atts.values.size() == 1)) {
                atts.value = (String)atts.values.elementAt(0);
            }
        }
        expect('>');
        if (in.replace == 0) {
            for (Enumeration e = elems.elements() ; e.hasMoreElements() ;) {
                dtd.defineAttributes((String)e.nextElement(), attlist);
            }
        }
    }
    void parseIgnoredSection() throws IOException {
        int depth = 1;
        in.replace++;
        while (true) {
            switch (ch) {
              case '<':
                if ((ch = in.read()) == '!') {
                    if ((ch = in.read()) == '[') {
                        ch = in.read();
                        depth++;
                    }
                }
                break;
              case ']':
                if ((ch = in.read()) == ']') {
                    if ((ch = in.read()) == '>') {
                        ch = in.read();
                        if (--depth == 0) {
                            in.replace--;
                            return;
                        }
                    }
                }
                break;
              case -1:
                error("eof");
                in.replace--;
                return;
              default:
                ch = in.read();
                break;
            }
        }
    }
    void parseMarkedSectionDeclaration() throws IOException {
        ch = in.read();
        skipSpace();
        if (!parseIdentifier(true)) {
            error("expected", "section status keyword");
            return;
        }
        String str = getString(0);
        skipSpace();
        expect('[');
        if ("ignore".equals(str)) {
            parseIgnoredSection();
        } else {
            if (!"include".equals(str)) {
                error("invalid.arg", "section status keyword", str);
            }
            parseSection();
            expect(']');
            expect(']');
            expect('>');
        }
    }
    void parseExternalIdentifier() throws IOException {
        if (parseIdentifier(false)) {
            String id = getString(0);
            skipParameterSpace();
            if (id.equals("PUBLIC")) {
                if ((ch == '\'') || (ch == '"')) {
                    parseAttributeValueSpecification();
                } else {
                    error("expected", "public identifier");
                }
                skipParameterSpace();
            } else if (!id.equals("SYSTEM")) {
                error("invalid", "external identifier");
            }
            if ((ch == '\'') || (ch == '"')) {
                parseAttributeValueSpecification();
            }
            skipParameterSpace();
        }
    }
    void parseDocumentTypeDeclaration() throws IOException {
        skipParameterSpace();
        if (!parseIdentifier(true)) {
            error("expected", "identifier");
        } else {
            skipParameterSpace();
        }
        strpos = 0;
        parseExternalIdentifier();
        if (ch == '[') {
            ch = in.read();
            parseSection();
            expect(']');
            skipParameterSpace();
        }
        expect('>');
    }
    void parseSection() throws IOException {
        while (true) {
            switch (ch) {
              case ']':
                return;
              case '<':
                switch (ch = in.read()) {
                  case '!':
                    switch (ch = in.read()) {
                      case '[':
                        parseMarkedSectionDeclaration();
                        break;
                      case '-':
                        skipParameterSpace();
                        expect('>');
                        break;
                      default:
                        if (parseIdentifier(true)) {
                            String str = getString(0);
                            if (str.equals("element")) {
                                parseElementDeclaration();
                            } else if (str.equals("entity")) {
                                parseEntityDeclaration();
                            } else if (str.equals("attlist")) {
                                parseAttlistDeclaration();
                            } else if (str.equals("doctype")) {
                                parseDocumentTypeDeclaration();
                            } else if (str.equals("usemap")) {
                                error("ignoring", "usemap");
                                while ((ch != -1) && (ch != '>')) {
                                    ch = in.read();
                                }
                                expect('>');
                            } else if (str.equals("shortref")) {
                                error("ignoring", "shortref");
                                while ((ch != -1) && (ch != '>')) {
                                    ch = in.read();
                                }
                                expect('>');
                            } else if (str.equals("notation")) {
                                error("ignoring", "notation");
                                while ((ch != -1) && (ch != '>')) {
                                    ch = in.read();
                                }
                                expect('>');
                            } else {
                                error("markup");
                            }
                        } else {
                            error("markup");
                            while ((ch != -1) && (ch != '>')) {
                                ch = in.read();
                            }
                            expect('>');
                        }
                    }
                }
                break;
              case -1:
                return;
              default:
                char str[] = {(char)ch};
                error("invalid.arg", "character", "'" + new String(str) + "' / " + ch);
              case ' ':
              case '\t':
              case '\n':
                ch = in.read();
                break;
            }
        }
    }
    DTD parse(InputStream in, DTDBuilder dtd) {
        try {
            this.dtd = dtd;
            this.in = new DTDInputStream(in, dtd);
            long tm = System.currentTimeMillis();
            ch = this.in.read();
            parseSection();
            if (ch != -1) {
                error("premature");
            }
            tm = System.currentTimeMillis() - tm;
            System.err.println("[Parsed DTD " + dtd + " in " + tm + "ms]");
        } catch (IOException e) {
            error("ioexception");
        } catch (Exception e) {
            error("exception", e.getClass().getName(), e.getMessage());
            e.printStackTrace();
        } catch (ThreadDeath e) {
            error("terminated");
        }
        return (nerrors > 0) ? null : dtd;
    }
}
