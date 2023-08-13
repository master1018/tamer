class RuleBasedBreakIteratorBuilder {
    protected static final byte IGNORE = -1;
    private CompactByteArray charCategoryTable = null;
    private SupplementaryCharacterData supplementaryCharCategoryTable = null;
    private short[] stateTable = null;
    private short[] backwardsStateTable = null;
    private boolean[] endStates = null;
    private boolean[] lookaheadStates = null;
    private byte[] additionalData = null;
    private int numCategories;
    protected Vector categories = null;
    protected Hashtable expressions = null;
    protected CharSet ignoreChars = null;
    protected Vector tempStateTable = null;
    protected Vector decisionPointList = null;
    protected Stack decisionPointStack = null;
    protected Vector loopingStates = null;
    protected Vector statesToBackfill = null;
    protected Vector mergeList = null;
    protected boolean clearLoopingStates = false;
    protected static final int END_STATE_FLAG = 0x8000;
    protected static final int DONT_LOOP_FLAG = 0x4000;
    protected static final int LOOKAHEAD_STATE_FLAG = 0x2000;
    protected static final int ALL_FLAGS = END_STATE_FLAG
                                         | LOOKAHEAD_STATE_FLAG
                                         | DONT_LOOP_FLAG;
    public RuleBasedBreakIteratorBuilder(String description) {
        Vector tempRuleList = buildRuleList(description);
        buildCharCategories(tempRuleList);
        buildStateTable(tempRuleList);
        buildBackwardsStateTable(tempRuleList);
    }
    private Vector buildRuleList(String description) {
        Vector tempRuleList = new Vector();
        Stack parenStack = new Stack();
        int p = 0;
        int ruleStart = 0;
        int c = '\u0000';
        int lastC = '\u0000';
        int lastOpen = '\u0000';
        boolean haveEquals = false;
        boolean havePipe = false;
        boolean sawVarName = false;
        final String charsThatCantPrecedeAsterisk = "=/{(|}*;\u0000";
        if (description.length() != 0 &&
            description.codePointAt(description.length() - 1) != ';') {
            description = description + ";";
        }
        while (p < description.length()) {
            c = description.codePointAt(p);
            switch (c) {
                case '\\':
                    ++p;
                    break;
                case '{':
                case '<':
                case '[':
                case '(':
                    if (lastOpen == '<') {
                        error("Can't nest brackets inside <>", p, description);
                    }
                    if (lastOpen == '[' && c != '[') {
                        error("Can't nest anything in [] but []", p, description);
                    }
                    if (c == '<' && (haveEquals || havePipe)) {
                        error("Unknown variable name", p, description);
                    }
                    lastOpen = c;
                    parenStack.push(new Character((char)c));
                    if (c == '<') {
                        sawVarName = true;
                    }
                    break;
                case '}':
                case '>':
                case ']':
                case ')':
                    char expectedClose = '\u0000';
                    switch (lastOpen) {
                        case '{':
                            expectedClose = '}';
                            break;
                        case '[':
                            expectedClose = ']';
                            break;
                        case '(':
                            expectedClose = ')';
                            break;
                        case '<':
                            expectedClose = '>';
                            break;
                    }
                    if (c != expectedClose) {
                        error("Unbalanced parentheses", p, description);
                    }
                    if (lastC == lastOpen) {
                        error("Parens don't contain anything", p, description);
                    }
                    parenStack.pop();
                    if (!parenStack.empty()) {
                        lastOpen = ((Character)(parenStack.peek())).charValue();
                    }
                    else {
                        lastOpen = '\u0000';
                    }
                    break;
                case '*':
                    if (charsThatCantPrecedeAsterisk.indexOf(lastC) != -1) {
                        error("Misplaced asterisk", p, description);
                    }
                    break;
                case '?':
                    if (lastC != '*') {
                        error("Misplaced ?", p, description);
                    }
                    break;
                case '=':
                    if (haveEquals || havePipe) {
                        error("More than one = or / in rule", p, description);
                    }
                    haveEquals = true;
                    break;
                case '/':
                    if (haveEquals || havePipe) {
                        error("More than one = or / in rule", p, description);
                    }
                    if (sawVarName) {
                        error("Unknown variable name", p, description);
                    }
                    havePipe = true;
                    break;
                case '!':
                    if (lastC != ';' && lastC != '\u0000') {
                        error("! can only occur at the beginning of a rule", p, description);
                    }
                    break;
                case '.':
                    break;
                case '^':
                case '-':
                case ':':
                    if (lastOpen != '[' && lastOpen != '<') {
                        error("Illegal character", p, description);
                    }
                    break;
                case ';':
                    if (lastC == ';' || lastC == '\u0000') {
                        error("Empty rule", p, description);
                    }
                    if (!parenStack.empty()) {
                        error("Unbalanced parenheses", p, description);
                    }
                    if (parenStack.empty()) {
                        if (haveEquals) {
                            description = processSubstitution(description.substring(ruleStart,
                                            p), description, p + 1);
                        }
                        else {
                            if (sawVarName) {
                                error("Unknown variable name", p, description);
                            }
                            tempRuleList.addElement(description.substring(ruleStart, p));
                        }
                        ruleStart = p + 1;
                        haveEquals = havePipe = sawVarName = false;
                    }
                    break;
                case '|':
                    if (lastC == '|') {
                        error("Empty alternative", p, description);
                    }
                    if (parenStack.empty() || lastOpen != '(') {
                        error("Misplaced |", p, description);
                    }
                    break;
                default:
                    if (c >= ' ' && c < '\u007f' && !Character.isLetter((char)c)
                        && !Character.isDigit((char)c)) {
                        error("Illegal character", p, description);
                    }
                    if (c >= Character.MIN_SUPPLEMENTARY_CODE_POINT) {
                        ++p;
                    }
                    break;
            }
            lastC = c;
            ++p;
        }
        if (tempRuleList.size() == 0) {
            error("No valid rules in description", p, description);
        }
        return tempRuleList;
    }
    protected String processSubstitution(String substitutionRule, String description,
                    int startPos) {
        String replace;
        String replaceWith;
        int equalPos = substitutionRule.indexOf('=');
        replace = substitutionRule.substring(0, equalPos);
        replaceWith = substitutionRule.substring(equalPos + 1);
        handleSpecialSubstitution(replace, replaceWith, startPos, description);
        if (replaceWith.length() == 0) {
            error("Nothing on right-hand side of =", startPos, description);
        }
        if (replace.length() == 0) {
            error("Nothing on left-hand side of =", startPos, description);
        }
        if (replace.length() == 2 && replace.charAt(0) != '\\') {
            error("Illegal left-hand side for =", startPos, description);
        }
        if (replace.length() >= 3 && replace.charAt(0) != '<' &&
            replace.codePointBefore(equalPos) != '>') {
            error("Illegal left-hand side for =", startPos, description);
        }
        if (!(replaceWith.charAt(0) == '[' &&
              replaceWith.charAt(replaceWith.length() - 1) == ']') &&
            !(replaceWith.charAt(0) == '(' &&
              replaceWith.charAt(replaceWith.length() - 1) == ')')) {
            error("Illegal right-hand side for =", startPos, description);
        }
        StringBuffer result = new StringBuffer();
        result.append(description.substring(0, startPos));
        int lastPos = startPos;
        int pos = description.indexOf(replace, startPos);
        while (pos != -1) {
            result.append(description.substring(lastPos, pos));
            result.append(replaceWith);
            lastPos = pos + replace.length();
            pos = description.indexOf(replace, lastPos);
        }
        result.append(description.substring(lastPos));
        return result.toString();
    }
    protected void handleSpecialSubstitution(String replace, String replaceWith,
                int startPos, String description) {
        if (replace.equals("<ignore>")) {
            if (replaceWith.charAt(0) == '(') {
                error("Ignore group can't be enclosed in (", startPos, description);
            }
            ignoreChars = CharSet.parseString(replaceWith);
        }
    }
    protected void buildCharCategories(Vector tempRuleList) {
        int bracketLevel = 0;
        int p = 0;
        int lineNum = 0;
        expressions = new Hashtable();
        while (lineNum < tempRuleList.size()) {
            String line = (String)(tempRuleList.elementAt(lineNum));
            p = 0;
            while (p < line.length()) {
                int c = line.codePointAt(p);
                switch (c) {
                    case '{': case '}': case '(': case ')': case '*': case '.':
                    case '/': case '|': case ';': case '?': case '!':
                        break;
                    case '[':
                        int q = p + 1;
                        ++bracketLevel;
                        while (q < line.length() && bracketLevel != 0) {
                            c = line.codePointAt(q);
                            switch (c) {
                            case '\\':
                                q++;
                                break;
                            case '[':
                                ++bracketLevel;
                                break;
                            case ']':
                                --bracketLevel;
                                break;
                            }
                            q = q + Character.charCount(c);
                        }
                        if (expressions.get(line.substring(p, q)) == null) {
                            expressions.put(line.substring(p, q), CharSet.parseString(line.substring(p, q)));
                        }
                        p = q - 1;
                        break;
                    case '\\':
                        ++p;
                        c = line.codePointAt(p);
                    default:
                        expressions.put(line.substring(p, p + 1), CharSet.parseString(line.substring(p, p + 1)));
                        break;
                }
                p += Character.charCount(line.codePointAt(p));
            }
            ++lineNum;
        }
        CharSet.releaseExpressionCache();
        categories = new Vector();
        if (ignoreChars != null) {
            categories.addElement(ignoreChars);
        }
        else {
            categories.addElement(new CharSet());
        }
        ignoreChars = null;
        mungeExpressionList(expressions);
        Enumeration iter = expressions.elements();
        while (iter.hasMoreElements()) {
            CharSet e = (CharSet)iter.nextElement();
            for (int j = categories.size() - 1; !e.empty() && j > 0; j--) {
                CharSet that = (CharSet)(categories.elementAt(j));
                if (!that.intersection(e).empty()) {
                    CharSet temp = that.difference(e);
                    if (!temp.empty()) {
                        categories.addElement(temp);
                    }
                    temp = e.intersection(that);
                    e = e.difference(that);
                    if (!temp.equals(that)) {
                        categories.setElementAt(temp, j);
                    }
                }
            }
            if (!e.empty()) {
                categories.addElement(e);
            }
        }
        CharSet allChars = new CharSet();
        for (int i = 1; i < categories.size(); i++) {
            allChars = allChars.union((CharSet)(categories.elementAt(i)));
        }
        CharSet ignoreChars = (CharSet)(categories.elementAt(0));
        ignoreChars = ignoreChars.difference(allChars);
        categories.setElementAt(ignoreChars, 0);
        iter = expressions.keys();
        while (iter.hasMoreElements()) {
            String key = (String)iter.nextElement();
            CharSet cs = (CharSet)expressions.get(key);
            StringBuffer cats = new StringBuffer();
            for (int j = 0; j < categories.size(); j++) {
                CharSet temp = cs.intersection((CharSet)(categories.elementAt(j)));
                if (!temp.empty()) {
                    cats.append((char)(0x100 + j));
                    if (temp.equals(cs)) {
                        break;
                    }
                }
            }
            expressions.put(key, cats.toString());
        }
        charCategoryTable = new CompactByteArray((byte)0);
        supplementaryCharCategoryTable = new SupplementaryCharacterData((byte)0);
        for (int i = 0; i < categories.size(); i++) {
            CharSet chars = (CharSet)(categories.elementAt(i));
            Enumeration enum_ = chars.getChars();
            while (enum_.hasMoreElements()) {
                int[] range = (int[])(enum_.nextElement());
                if (i != 0) {
                    if (range[0] < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
                        if (range[1] < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
                            charCategoryTable.setElementAt((char)range[0], (char)range[1], (byte)i);
                        } else {
                            charCategoryTable.setElementAt((char)range[0], (char)0xFFFF, (byte)i);
                            supplementaryCharCategoryTable.appendElement(Character.MIN_SUPPLEMENTARY_CODE_POINT, range[1], (byte)i);
                        }
                    } else {
                        supplementaryCharCategoryTable.appendElement(range[0], range[1], (byte)i);
                    }
                }
                else {
                    if (range[0] < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
                        if (range[1] < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
                            charCategoryTable.setElementAt((char)range[0], (char)range[1], IGNORE);
                        } else {
                            charCategoryTable.setElementAt((char)range[0], (char)0xFFFF, IGNORE);
                            supplementaryCharCategoryTable.appendElement(Character.MIN_SUPPLEMENTARY_CODE_POINT, range[1], IGNORE);
                        }
                    } else {
                        supplementaryCharCategoryTable.appendElement(range[0], range[1], IGNORE);
                    }
                }
            }
        }
        charCategoryTable.compact();
        supplementaryCharCategoryTable.complete();
        numCategories = categories.size();
    }
    protected void mungeExpressionList(Hashtable expressions) {
    }
    private void buildStateTable(Vector tempRuleList) {
        tempStateTable = new Vector();
        tempStateTable.addElement(new short[numCategories + 1]);
        tempStateTable.addElement(new short[numCategories + 1]);
        for (int i = 0; i < tempRuleList.size(); i++) {
            String rule = (String)tempRuleList.elementAt(i);
            if (rule.charAt(0) != '!') {
                parseRule(rule, true);
            }
        }
        finishBuildingStateTable(true);
    }
    private void parseRule(String rule, boolean forward) {
        int p = 0;
        int currentState = 1;   
        int lastState = currentState;
        String pendingChars = "";
        decisionPointStack = new Stack();
        decisionPointList = new Vector();
        loopingStates = new Vector();
        statesToBackfill = new Vector();
        short[] state;
        boolean sawEarlyBreak = false;
        if (!forward) {
            loopingStates.addElement(new Integer(1));
        }
        decisionPointList.addElement(new Integer(currentState)); 
        currentState = tempStateTable.size() - 1;   
        while (p < rule.length()) {
            int c = rule.codePointAt(p);
            clearLoopingStates = false;
            if (c == '['
                || c == '\\'
                || Character.isLetter(c)
                || Character.isDigit(c)
                || c < ' '
                || c == '.'
                || c >= '\u007f') {
                if (c != '.') {
                    int q = p;
                    if (c == '\\') {
                        q = p + 2;
                        ++p;
                    }
                    else if (c == '[') {
                        int bracketLevel = 1;
                        q += Character.charCount(rule.codePointAt(q));
                        while (bracketLevel > 0) {
                            c = rule.codePointAt(q);
                            if (c == '[') {
                                ++bracketLevel;
                            }
                            else if (c == ']') {
                                --bracketLevel;
                            }
                            else if (c == '\\') {
                                c = rule.codePointAt(++q);
                            }
                            q += Character.charCount(c);
                        }
                    }
                    else {
                        q = p + Character.charCount(c);
                    }
                    pendingChars = (String)expressions.get(rule.substring(p, q));
                    p = q - Character.charCount(rule.codePointBefore(q));
                }
                else {
                    int rowNum = ((Integer)decisionPointList.lastElement()).intValue();
                    state = (short[])tempStateTable.elementAt(rowNum);
                    if (p + 1 < rule.length() && rule.charAt(p + 1) == '*' && state[0] != 0) {
                        decisionPointList.addElement(new Integer(state[0]));
                        pendingChars = "";
                        ++p;
                    }
                    else {
                        StringBuffer temp = new StringBuffer();
                        for (int i = 0; i < numCategories; i++)
                            temp.append((char)(i + 0x100));
                        pendingChars = temp.toString();
                    }
                }
                if (pendingChars.length() != 0) {
                    if (p + 1 < rule.length() && rule.charAt(p + 1) == '*') {
                        decisionPointStack.push(decisionPointList.clone());
                    }
                    int newState = tempStateTable.size();
                    if (loopingStates.size() != 0) {
                        statesToBackfill.addElement(new Integer(newState));
                    }
                    state = new short[numCategories + 1];
                    if (sawEarlyBreak) {
                        state[numCategories] = DONT_LOOP_FLAG;
                    }
                    tempStateTable.addElement(state);
                    updateStateTable(decisionPointList, pendingChars, (short)newState);
                    decisionPointList.removeAllElements();
                    lastState = currentState;
                    do {
                        ++currentState;
                        decisionPointList.addElement(new Integer(currentState));
                    } while (currentState + 1 < tempStateTable.size());
                }
            }
            else if (c == '{') {
                decisionPointStack.push(decisionPointList.clone());
            }
            else if (c == '}' || c == '*') {
                if (c == '*') {
                    for (int i = lastState + 1; i < tempStateTable.size(); i++) {
                        Vector temp = new Vector();
                        temp.addElement(new Integer(i));
                        updateStateTable(temp, pendingChars, (short)(lastState + 1));
                    }
                }
                Vector temp = (Vector)decisionPointStack.pop();
                for (int i = 0; i < decisionPointList.size(); i++)
                    temp.addElement(decisionPointList.elementAt(i));
                decisionPointList = temp;
            }
            else if (c == '?') {
                setLoopingStates(decisionPointList, decisionPointList);
            }
            else if (c == '(') {
                tempStateTable.addElement(new short[numCategories + 1]);
                lastState = currentState;
                ++currentState;
                decisionPointList.insertElementAt(new Integer(currentState), 0);
                decisionPointStack.push(decisionPointList.clone());
                decisionPointStack.push(new Vector());
            }
            else if (c == '|') {
                Vector oneDown = (Vector)decisionPointStack.pop();
                Vector twoDown = (Vector)decisionPointStack.peek();
                decisionPointStack.push(oneDown);
                for (int i = 0; i < decisionPointList.size(); i++)
                    oneDown.addElement(decisionPointList.elementAt(i));
                decisionPointList = (Vector)twoDown.clone();
            }
            else if (c == ')') {
                Vector exitPoints = (Vector)decisionPointStack.pop();
                for (int i = 0; i < decisionPointList.size(); i++)
                    exitPoints.addElement(decisionPointList.elementAt(i));
                decisionPointList = exitPoints;
                if (p + 1 >= rule.length() || rule.charAt(p + 1) != '*') {
                    decisionPointStack.pop();
                }
                else {
                    exitPoints = (Vector)decisionPointList.clone();
                    Vector temp = (Vector)decisionPointStack.pop();
                    int tempStateNum = ((Integer)temp.firstElement()).intValue();
                    short[] tempState = (short[])tempStateTable.elementAt(tempStateNum);
                    for (int i = 0; i < decisionPointList.size(); i++)
                        temp.addElement(decisionPointList.elementAt(i));
                    decisionPointList = temp;
                    for (int i = 0; i < tempState.length; i++) {
                        if (tempState[i] > tempStateNum) {
                            updateStateTable(exitPoints,
                                             new Character((char)(i + 0x100)).toString(),
                                             tempState[i]);
                        }
                    }
                    lastState = currentState;
                    currentState = tempStateTable.size() - 1;
                    ++p;
                }
            }
            else if (c == '/') {
                sawEarlyBreak = true;
                for (int i = 0; i < decisionPointList.size(); i++) {
                    state = (short[])tempStateTable.elementAt(((Integer)decisionPointList.
                                    elementAt(i)).intValue());
                    state[numCategories] |= LOOKAHEAD_STATE_FLAG;
                }
            }
            if (clearLoopingStates) {
                setLoopingStates(null, decisionPointList);
            }
            p += Character.charCount(c);
        }
        setLoopingStates(null, decisionPointList);
        for (int i = 0; i < decisionPointList.size(); i++) {
            int rowNum = ((Integer)decisionPointList.elementAt(i)).intValue();
            state = (short[])tempStateTable.elementAt(rowNum);
            state[numCategories] |= END_STATE_FLAG;
            if (sawEarlyBreak) {
                state[numCategories] |= LOOKAHEAD_STATE_FLAG;
            }
        }
    }
    private void updateStateTable(Vector rows,
                                  String pendingChars,
                                  short newValue) {
        short[] newValues = new short[numCategories + 1];
        for (int i = 0; i < pendingChars.length(); i++)
            newValues[(int)(pendingChars.charAt(i)) - 0x100] = newValue;
        for (int i = 0; i < rows.size(); i++) {
            mergeStates(((Integer)rows.elementAt(i)).intValue(), newValues, rows);
        }
    }
    private void mergeStates(int rowNum,
                             short[] newValues,
                             Vector rowsBeingUpdated) {
        short[] oldValues = (short[])(tempStateTable.elementAt(rowNum));
        boolean isLoopingState = loopingStates.contains(new Integer(rowNum));
        for (int i = 0; i < oldValues.length; i++) {
            if (oldValues[i] == newValues[i]) {
                continue;
            }
            else if (isLoopingState && loopingStates.contains(new Integer(oldValues[i]))) {
                if (newValues[i] != 0) {
                    if (oldValues[i] == 0) {
                        clearLoopingStates = true;
                    }
                    oldValues[i] = newValues[i];
                }
            }
            else if (oldValues[i] == 0) {
                oldValues[i] = newValues[i];
            }
            else if (i == numCategories) {
                oldValues[i] = (short)((newValues[i] & ALL_FLAGS) | oldValues[i]);
            }
            else if (oldValues[i] != 0 && newValues[i] != 0) {
                int combinedRowNum = searchMergeList(oldValues[i], newValues[i]);
                if (combinedRowNum != 0) {
                    oldValues[i] = (short)combinedRowNum;
                }
                else {
                    int oldRowNum = oldValues[i];
                    int newRowNum = newValues[i];
                    combinedRowNum = tempStateTable.size();
                    if (mergeList == null) {
                        mergeList = new Vector();
                    }
                    mergeList.addElement(new int[] { oldRowNum, newRowNum, combinedRowNum });
                    short[] newRow = new short[numCategories + 1];
                    short[] oldRow = (short[])(tempStateTable.elementAt(oldRowNum));
                    System.arraycopy(oldRow, 0, newRow, 0, numCategories + 1);
                    tempStateTable.addElement(newRow);
                    oldValues[i] = (short)combinedRowNum;
                    if ((decisionPointList.contains(new Integer(oldRowNum))
                            || decisionPointList.contains(new Integer(newRowNum)))
                        && !decisionPointList.contains(new Integer(combinedRowNum))
                    ) {
                        decisionPointList.addElement(new Integer(combinedRowNum));
                    }
                    if ((rowsBeingUpdated.contains(new Integer(oldRowNum))
                            || rowsBeingUpdated.contains(new Integer(newRowNum)))
                        && !rowsBeingUpdated.contains(new Integer(combinedRowNum))
                    ) {
                        decisionPointList.addElement(new Integer(combinedRowNum));
                    }
                    for (int k = 0; k < decisionPointStack.size(); k++) {
                        Vector dpl = (Vector)decisionPointStack.elementAt(k);
                        if ((dpl.contains(new Integer(oldRowNum))
                                || dpl.contains(new Integer(newRowNum)))
                            && !dpl.contains(new Integer(combinedRowNum))
                        ) {
                            dpl.addElement(new Integer(combinedRowNum));
                        }
                    }
                    mergeStates(combinedRowNum, (short[])(tempStateTable.elementAt(
                                    newValues[i])), rowsBeingUpdated);
                }
            }
        }
        return;
    }
    private int searchMergeList(int a, int b) {
        if (mergeList == null) {
            return 0;
        }
        else {
            int[] entry;
            for (int i = 0; i < mergeList.size(); i++) {
                entry = (int[])(mergeList.elementAt(i));
                if ((entry[0] == a && entry[1] == b) || (entry[0] == b && entry[1] == a)) {
                    return entry[2];
                }
                if ((entry[2] == a && (entry[0] == b || entry[1] == b))) {
                    return entry[2];
                }
                if ((entry[2] == b && (entry[0] == a || entry[1] == a))) {
                    return entry[2];
                }
            }
            return 0;
        }
    }
    private void setLoopingStates(Vector newLoopingStates, Vector endStates) {
        if (!loopingStates.isEmpty()) {
            int loopingState = ((Integer)loopingStates.lastElement()).intValue();
            int rowNum;
            for (int i = 0; i < endStates.size(); i++) {
                eliminateBackfillStates(((Integer)endStates.elementAt(i)).intValue());
            }
            for (int i = 0; i < statesToBackfill.size(); i++) {
                rowNum = ((Integer)statesToBackfill.elementAt(i)).intValue();
                short[] state = (short[])tempStateTable.elementAt(rowNum);
                state[numCategories] =
                    (short)((state[numCategories] & ALL_FLAGS) | loopingState);
            }
            statesToBackfill.removeAllElements();
            loopingStates.removeAllElements();
        }
        if (newLoopingStates != null) {
            loopingStates = (Vector)newLoopingStates.clone();
        }
    }
    private void eliminateBackfillStates(int baseState) {
        if (statesToBackfill.contains(new Integer(baseState))) {
            statesToBackfill.removeElement(new Integer(baseState));
            short[] state = (short[])tempStateTable.elementAt(baseState);
            for (int i = 0; i < numCategories; i++) {
                if (state[i] != 0) {
                    eliminateBackfillStates(state[i]);
                }
            }
        }
    }
    private void backfillLoopingStates() {
        short[] state;
        short[] loopingState = null;
        int loopingStateRowNum = 0;
        int fromState;
        for (int i = 0; i < tempStateTable.size(); i++) {
            state = (short[])tempStateTable.elementAt(i);
            fromState = state[numCategories] & ~ALL_FLAGS;
            if (fromState > 0) {
                if (fromState != loopingStateRowNum) {
                    loopingStateRowNum = fromState;
                    loopingState = (short[])tempStateTable.elementAt(loopingStateRowNum);
                }
                state[numCategories] &= ALL_FLAGS;
                for (int j = 0; j < state.length; j++) {
                    if (state[j] == 0) {
                        state[j] = loopingState[j];
                    }
                    else if (state[j] == DONT_LOOP_FLAG) {
                        state[j] = 0;
                    }
                }
            }
        }
    }
    private void finishBuildingStateTable(boolean forward) {
        backfillLoopingStates();
        int[] rowNumMap = new int[tempStateTable.size()];
        Stack rowsToFollow = new Stack();
        rowsToFollow.push(new Integer(1));
        rowNumMap[1] = 1;
        while (rowsToFollow.size() != 0) {
            int rowNum = ((Integer)rowsToFollow.pop()).intValue();
            short[] row = (short[])(tempStateTable.elementAt(rowNum));
            for (int i = 0; i < numCategories; i++) {
                if (row[i] != 0) {
                    if (rowNumMap[row[i]] == 0) {
                        rowNumMap[row[i]] = row[i];
                        rowsToFollow.push(new Integer(row[i]));
                    }
                }
            }
        }
        boolean madeChange;
        int newRowNum;
        int[] stateClasses = new int[tempStateTable.size()];
        int nextClass = numCategories + 1;
        short[] state1, state2;
        for (int i = 1; i < stateClasses.length; i++) {
            if (rowNumMap[i] == 0) {
                continue;
            }
            state1 = (short[])tempStateTable.elementAt(i);
            for (int j = 0; j < numCategories; j++) {
                if (state1[j] != 0) {
                    ++stateClasses[i];
                }
            }
            if (stateClasses[i] == 0) {
                stateClasses[i] = nextClass;
            }
        }
        ++nextClass;
        int currentClass;
        int lastClass;
        boolean split;
        do {
            currentClass = 1;
            lastClass = nextClass;
            while (currentClass < nextClass) {
                split = false;
                state1 = state2 = null;
                for (int i = 0; i < stateClasses.length; i++) {
                    if (stateClasses[i] == currentClass) {
                        if (state1 == null) {
                            state1 = (short[])tempStateTable.elementAt(i);
                        }
                        else {
                            state2 = (short[])tempStateTable.elementAt(i);
                            for (int j = 0; j < state2.length; j++) {
                                if ((j == numCategories && state1[j] != state2[j] && forward)
                                        || (j != numCategories && stateClasses[state1[j]]
                                        != stateClasses[state2[j]])) {
                                    stateClasses[i] = nextClass;
                                    split = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (split) {
                    ++nextClass;
                }
                ++currentClass;
            }
        } while (lastClass != nextClass);
        int[] representatives = new int[nextClass];
        for (int i = 1; i < stateClasses.length; i++)
            if (representatives[stateClasses[i]] == 0) {
                representatives[stateClasses[i]] = i;
            }
            else {
                rowNumMap[i] = representatives[stateClasses[i]];
            }
        for (int i = 1; i < rowNumMap.length; i++) {
            if (rowNumMap[i] != i) {
                tempStateTable.setElementAt(null, i);
            }
        }
        newRowNum = 1;
        for (int i = 1; i < rowNumMap.length; i++) {
            if (tempStateTable.elementAt(i) != null) {
                rowNumMap[i] = newRowNum++;
            }
        }
        for (int i = 1; i < rowNumMap.length; i++) {
            if (tempStateTable.elementAt(i) == null) {
                rowNumMap[i] = rowNumMap[rowNumMap[i]];
            }
        }
        if (forward) {
            endStates = new boolean[newRowNum];
            lookaheadStates = new boolean[newRowNum];
            stateTable = new short[newRowNum * numCategories];
            int p = 0;
            int p2 = 0;
            for (int i = 0; i < tempStateTable.size(); i++) {
                short[] row = (short[])(tempStateTable.elementAt(i));
                if (row == null) {
                    continue;
                }
                for (int j = 0; j < numCategories; j++) {
                    stateTable[p] = (short)(rowNumMap[row[j]]);
                    ++p;
                }
                endStates[p2] = ((row[numCategories] & END_STATE_FLAG) != 0);
                lookaheadStates[p2] = ((row[numCategories] & LOOKAHEAD_STATE_FLAG) != 0);
                ++p2;
            }
        }
        else {
            backwardsStateTable = new short[newRowNum * numCategories];
            int p = 0;
            for (int i = 0; i < tempStateTable.size(); i++) {
                short[] row = (short[])(tempStateTable.elementAt(i));
                if (row == null) {
                    continue;
                }
                for (int j = 0; j < numCategories; j++) {
                    backwardsStateTable[p] = (short)(rowNumMap[row[j]]);
                    ++p;
                }
            }
        }
    }
    private void buildBackwardsStateTable(Vector tempRuleList) {
        tempStateTable = new Vector();
        tempStateTable.addElement(new short[numCategories + 1]);
        tempStateTable.addElement(new short[numCategories + 1]);
        for (int i = 0; i < tempRuleList.size(); i++) {
            String rule = (String)tempRuleList.elementAt(i);
            if (rule.charAt(0) == '!') {
                parseRule(rule.substring(1), false);
            }
        }
        backfillLoopingStates();
        int backTableOffset = tempStateTable.size();
        if (backTableOffset > 2) {
            ++backTableOffset;
        }
        for (int i = 0; i < numCategories + 1; i++)
            tempStateTable.addElement(new short[numCategories + 1]);
        short[] state = (short[])tempStateTable.elementAt(backTableOffset - 1);
        for (int i = 0; i < numCategories; i++)
            state[i] = (short)(i + backTableOffset);
        int numRows = stateTable.length / numCategories;
        for (int column = 0; column < numCategories; column++) {
            for (int row = 0; row < numRows; row++) {
                int nextRow = lookupState(row, column);
                if (nextRow != 0) {
                    for (int nextColumn = 0; nextColumn < numCategories; nextColumn++) {
                        int cellValue = lookupState(nextRow, nextColumn);
                        if (cellValue != 0) {
                            state = (short[])tempStateTable.elementAt(nextColumn +
                                            backTableOffset);
                            state[column] = (short)(column + backTableOffset);
                        }
                    }
                }
            }
        }
        if (backTableOffset > 1) {
            state = (short[])tempStateTable.elementAt(1);
            for (int i = backTableOffset - 1; i < tempStateTable.size(); i++) {
                short[] state2 = (short[])tempStateTable.elementAt(i);
                for (int j = 0; j < numCategories; j++) {
                    if (state[j] != 0 && state2[j] != 0) {
                        state2[j] = state[j];
                    }
                }
            }
            state = (short[])tempStateTable.elementAt(backTableOffset - 1);
            for (int i = 1; i < backTableOffset - 1; i++) {
                short[] state2 = (short[])tempStateTable.elementAt(i);
                if ((state2[numCategories] & END_STATE_FLAG) == 0) {
                    for (int j = 0; j < numCategories; j++) {
                        if (state2[j] == 0) {
                            state2[j] = state[j];
                        }
                    }
                }
            }
        }
        finishBuildingStateTable(false);
    }
    protected int lookupState(int state, int category) {
        return stateTable[state * numCategories + category];
    }
    protected void error(String message, int position, String context) {
        throw new IllegalArgumentException("Parse error at position (" + position + "): " + message + "\n" +
                context.substring(0, position) + " -here- " + context.substring(position));
    }
    void makeFile(String filename) {
        writeTables(filename);
    }
    private static final byte[] LABEL = {
        (byte)'B', (byte)'I', (byte)'d', (byte)'a', (byte)'t', (byte)'a',
        (byte)'\0'
    };
    private static final byte[] supportedVersion = { (byte)1 };
     private static final int HEADER_LENGTH = 36;
     private static final int BMP_INDICES_LENGTH = 512;
    protected void writeTables(String datafile) {
        final String filename;
        final String outputDir;
        String tmpbuf = GenerateBreakIteratorData.getOutputDirectory();
        if (tmpbuf.equals("")) {
            filename = datafile;
            outputDir = "";
        } else {
            char sep = File.separatorChar;
            if (sep == '/') {
                outputDir = tmpbuf;
            } else if (sep == '\\') {
                outputDir = tmpbuf.replaceAll("/", "\\\\");
            } else {
                outputDir = tmpbuf.replaceAll("/", String.valueOf(sep));
            }
            filename = outputDir + sep + datafile;
        }
        try {
            if (!outputDir.equals("")) {
                new File(outputDir).mkdirs();
            }
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename));
            byte[] BMPdata = charCategoryTable.getStringArray();
            short[] BMPindices = charCategoryTable.getIndexArray();
            int[] nonBMPdata = supplementaryCharCategoryTable.getArray();
            if (BMPdata.length <= 0) {
                throw new InternalError("Wrong BMP data length(" + BMPdata.length + ")");
            }
            if (BMPindices.length != BMP_INDICES_LENGTH) {
                throw new InternalError("Wrong BMP indices length(" + BMPindices.length + ")");
            }
            if (nonBMPdata.length <= 0) {
                throw new InternalError("Wrong non-BMP data length(" + nonBMPdata.length + ")");
            }
            int len;
            CRC32 crc32 = new CRC32();
            len = stateTable.length;
            for (int i = 0; i < len; i++) {
                crc32.update(stateTable[i]);
            }
            len = backwardsStateTable.length;
            for (int i = 0; i < len; i++) {
                crc32.update(backwardsStateTable[i]);
            }
            crc32.update(toByteArray(endStates));
            crc32.update(toByteArray(lookaheadStates));
            for (int i = 0; i < BMP_INDICES_LENGTH; i++) {
                crc32.update(BMPindices[i]);
            }
            crc32.update(BMPdata);
            len = nonBMPdata.length;
            for (int i = 0; i < len; i++) {
                crc32.update(nonBMPdata[i]);
            }
            if (additionalData != null) {
                len = additionalData.length;
                for (int i = 0; i < len; i++) {
                    crc32.update(additionalData[i]);
                }
            }
            len = HEADER_LENGTH +
                  (stateTable.length + backwardsStateTable.length) * 2 +
                  endStates.length + lookaheadStates.length + 1024 +
                  BMPdata.length + nonBMPdata.length * 4 +
                  ((additionalData == null) ? 0 : additionalData.length);
            out.write(LABEL);
            out.write(supportedVersion);
            out.write(toByteArray(len));
            out.write(toByteArray(stateTable.length));
            out.write(toByteArray(backwardsStateTable.length));
            out.write(toByteArray(endStates.length));
            out.write(toByteArray(lookaheadStates.length));
            out.write(toByteArray(BMPdata.length));
            out.write(toByteArray(nonBMPdata.length));
            if (additionalData == null) {
                out.write(toByteArray(0));
            } else {
                out.write(toByteArray(additionalData.length));
            }
            out.write(toByteArray(crc32.getValue()));
            len = stateTable.length;
            for (int i = 0; i < len; i++) {
                out.write(toByteArray(stateTable[i]));
            }
            len = backwardsStateTable.length;
            for (int i = 0; i < len; i++) {
                out.write(toByteArray(backwardsStateTable[i]));
            }
            out.write(toByteArray(endStates));
            out.write(toByteArray(lookaheadStates));
            for (int i = 0; i < BMP_INDICES_LENGTH; i++) {
                out.write(toByteArray(BMPindices[i]));
            }
            BMPindices = null;
            out.write(BMPdata);
            BMPdata = null;
            len = nonBMPdata.length;
            for (int i = 0; i < len; i++) {
                out.write(toByteArray(nonBMPdata[i]));
            }
            nonBMPdata = null;
            if (additionalData != null) {
                out.write(additionalData);
            }
            out.close();
        }
        catch (Exception e) {
            throw new InternalError(e.toString());
        }
    }
    byte[] toByteArray(short val) {
        byte[] buf = new byte[2];
        buf[0] = (byte)((val>>>8) & 0xFF);
        buf[1] = (byte)(val & 0xFF);
        return buf;
    }
    byte[] toByteArray(int val) {
        byte[] buf = new byte[4];
        buf[0] = (byte)((val>>>24) & 0xFF);
        buf[1] = (byte)((val>>>16) & 0xFF);
        buf[2] = (byte)((val>>>8) & 0xFF);
        buf[3] = (byte)(val & 0xFF);
        return buf;
    }
    byte[] toByteArray(long val) {
        byte[] buf = new byte[8];
        buf[0] = (byte)((val>>>56) & 0xff);
        buf[1] = (byte)((val>>>48) & 0xff);
        buf[2] = (byte)((val>>>40) & 0xff);
        buf[3] = (byte)((val>>>32) & 0xff);
        buf[4] = (byte)((val>>>24) & 0xff);
        buf[5] = (byte)((val>>>16) & 0xff);
        buf[6] = (byte)((val>>>8) & 0xff);
        buf[7] = (byte)(val & 0xff);
        return buf;
    }
    byte[] toByteArray(boolean[] data) {
        byte[] buf = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            buf[i] = data[i] ? (byte)1 : (byte)0;
        }
        return buf;
    }
    void setAdditionalData(byte[] data) {
        additionalData = data;
    }
}
