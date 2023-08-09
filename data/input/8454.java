public class GenerateCharacter {
    final static boolean DEBUG = false;
    final static String commandMarker = "$$";
    static String ROOT                        = "";
    static String DefaultUnicodeSpecFileName  = ROOT + "UnicodeData.txt";
    static String DefaultSpecialCasingFileName = ROOT + "SpecialCasing.txt";
    static String DefaultPropListFileName     = ROOT + "PropList.txt";
    static String DefaultJavaTemplateFileName = ROOT + "Character.java.template";
    static String DefaultJavaOutputFileName   = ROOT + "Character.java";
    static String DefaultCTemplateFileName    = ROOT + "Character.c.template";
    static String DefaultCOutputFileName      = ROOT + "Character.c";
    static int plane = 0;
    private static final int
        shiftType           = 0,        maskType            =       0x001F,
        shiftDigitOffset    = 5,        maskDigitOffset     =       0x03E0,
        shiftNumericType    = 10,       maskNumericType     =       0x0C00,
        shiftIdentifierInfo = 12,       maskIdentifierInfo  =       0x7000,
                                        maskUnicodePart     =       0x1000,
        shiftCaseInfo       = 15,       maskCaseInfo        =      0x38000,
                                        maskLowerCase       =      0x20000,
                                        maskUpperCase       =      0x10000,
                                        maskTitleCase       =      0x08000,
        shiftCaseOffset     = 18,       maskCaseOffset      =   0x07FC0000,
        shiftCaseOffsetSign = 5,
                                        maskDigit               =   0x001F,
                                        maskCase                =   0x01FF,
        shiftBidi           = 27,       maskBidi              = 0x78000000,
        shiftMirrored       = 31,       
        shiftPlane          = 16,       maskPlane = 0xFF0000;
    private static final long maskMirrored          = 0x80000000L;
    private static final long
        maskOtherLowercase  = 0x100000000L,
        maskOtherUppercase  = 0x200000000L,
        maskOtherAlphabetic = 0x400000000L,
        maskOtherMath       = 0x800000000L,
        maskIdeographic     = 0x1000000000L,
        maskNoncharacterCP  = 0x2000000000L;
    public static int
        valueNotNumeric             = 0x0000,
        valueDigit                  = 0x0400,
        valueStrangeNumeric         = 0x0800,
        valueJavaSupradecimal       = 0x0C00,
        valueIgnorable              = 0x1000,
        valueJavaOnlyPart           = 0x2000,
        valueJavaUnicodePart        = 0x3000,
        valueJavaWhitespace         = 0x4000,
        valueJavaStartUnicodePart   = 0x5000,
        valueJavaOnlyStart          = 0x6000,
        valueJavaUnicodeStart       = 0x7000,
        lowJavaStart                = 0x5000,
        nonzeroJavaPart             = 0x3000,
        valueUnicodeStart           = 0x7000;
    private static final int
        bitJavaStart            = 0x02,
        bitJavaPart             = 0x01,
        maskIsJavaIdentifierPart = bitJavaPart,
        maskIsJavaIdentifierStart = bitJavaStart;
    static int maxOffset = maskCase/2 ;
    static int minOffset = -maxOffset;
    static String hex(long n) { return Long.toHexString(n).toUpperCase(); }
    static String hex2(long n) {
        String q = Long.toHexString(n & 0xFF).toUpperCase();
        return "00".substring(Math.min(2, q.length())) + q;
    }
    static String hex4(long n) {
        String q = Long.toHexString(n & 0xFFFF).toUpperCase();
        return "0000".substring(Math.min(4, q.length())) + q;
    }
    static String hex8(long n) {
        String q = Long.toHexString(n & 0xFFFFFFFFL).toUpperCase();
        return "00000000".substring(Math.min(8, q.length())) + q;
    }
    static String hex16(long n) {
        String q = Long.toHexString(n).toUpperCase();
        return "0000000000000000".substring(Math.min(16, q.length())) + q;
    }
    static String dec3(long n) {
        String q = Long.toString(n);
        return "   ".substring(Math.min(3, q.length())) + q;
    }
    static String dec5(long n) {
        String q = Long.toString(n);
        return "     ".substring(Math.min(5, q.length())) + q;
    }
    static void FAIL(String s) {
        System.out.println("** " + s);
    }
    static long[] buildMap(UnicodeSpec[] data, SpecialCaseMap[] specialMaps, PropList propList)
    {
        long[] result;
        if (bLatin1 == true) {
            result = new long[256];
        } else {
            result = new long[1<<16];
        }
        int k=0;
        int codePoint = plane<<16;
        UnicodeSpec nonCharSpec = new UnicodeSpec();
        for (int j = 0; j < data.length && k < result.length; j++) {
            if (data[j].codePoint == codePoint) {
                result[k] = buildOne(codePoint, data[j], specialMaps);
                ++k;
                ++codePoint;
            }
            else if(data[j].codePoint > codePoint) {
                if (data[j].name.endsWith("Last>")) {
                    while (codePoint < data[j].codePoint && k < result.length) {
                        result[k] = buildOne(codePoint, data[j], specialMaps);
                        ++k;
                        ++codePoint;
                    }
                }
                else {
                    while (codePoint < data[j].codePoint && k < result.length) {
                        result[k] = buildOne(codePoint, nonCharSpec, specialMaps);
                        ++k;
                        ++codePoint;
                    }
                }
                k = data[j].codePoint & 0xFFFF;
                codePoint = data[j].codePoint;
                result[k] = buildOne(codePoint, data[j], specialMaps);
                ++k;
                ++codePoint;
            }
            else {
                System.out.println("An error has occured during spec mapping.");
                System.exit(0);
            }
        }
        codePoint = (plane<<16) | k;
        while (k < result.length) {
            result[k] = buildOne(codePoint, nonCharSpec, specialMaps);
            ++k;
            ++codePoint;
        }
        addExProp(result, propList, "Other_Lowercase", maskOtherLowercase);
        addExProp(result, propList, "Other_Uppercase", maskOtherUppercase);
        addExProp(result, propList, "Other_Alphabetic", maskOtherAlphabetic);
        addExProp(result, propList, "Ideographic", maskIdeographic);
        return result;
    }
    static int maxOffsetSeen = 0;
    static int minOffsetSeen = 0;
    static boolean isInvalidJavaWhiteSpace(int c) {
        int[] exceptions = {0x00A0, 0x2007, 0x202F, 0xFEFF};
        boolean retValue = false;
        for(int x=0;x<exceptions.length;x++) {
            if(c == exceptions[x]) {
                retValue = true;
                break;
            }
        }
        return retValue;
    }
    static long buildOne(int c, UnicodeSpec us, SpecialCaseMap[] specialMaps) {
        long resultA = 0;
        resultA |= us.generalCategory;
        NUMERIC: {
        STRANGE: {
            int val = 0;
            if ((c >= 0x0041) && (c <= 0x005A)) {
                val = c - 0x0041;
                resultA |= valueJavaSupradecimal;
            } else if ((c >= 0x0061) && (c <= 0x007A)) {
                val = c - 0x0061;
                resultA |= valueJavaSupradecimal;
            } else if ((c >= 0xFF21) && (c <= 0xFF3A)) {
                val = c - 0xFF21;
                resultA |= valueJavaSupradecimal;
            } else if ((c >= 0xFF41) && (c <= 0xFF5A)) {
                val = c - 0xFF41;
                resultA |= valueJavaSupradecimal;
            } else if (us.isDecimalValue()) {
                val = us.decimalValue;
                resultA |= valueDigit;
            } else if (us.isDigitValue()) {
                val = us.digitValue;
                resultA |= valueDigit;
            } else {
                if (us.numericValue.length() == 0) {
                    break NUMERIC;                      
                } else {
                    try {
                        val = Integer.parseInt(us.numericValue);
                        if (val >= 32 || val < 0) break STRANGE;
                        if (c == 0x215F) break STRANGE;
                    } catch(NumberFormatException e) {
                        break STRANGE;
                    }
                    resultA |= valueDigit;
                }
            }
            if (val >= 32 || val < 0) break STRANGE;
            resultA |= ((val - c & maskDigit) << shiftDigitOffset);
            break NUMERIC;
        } 
        resultA |= valueStrangeNumeric;
        } 
        int offset = 0;
        int specialMap = SpecialCaseMap.find(c, specialCaseMaps);
        boolean bHasUpper = (us.hasUpperMap()) || (specialMap != -1);
        if (bHasUpper) {
            resultA |= maskUpperCase;
        }
        if (specialMap != -1) {
            offset = -1;
        }
        else if (us.hasUpperMap())  {
            offset = c - us.upperMap;
        }
        if (us.hasLowerMap()) {
            resultA |= maskLowerCase;
            if (offset == 0)
                offset = us.lowerMap - c;
            else if (offset != (us.lowerMap - c)) {
                if (DEBUG) {
                FAIL("Character " + hex(c) +
                " has incompatible lowercase and uppercase mappings");
                }
            }
        }
        if ((us.hasTitleMap() && us.titleMap != us.upperMap) ||
            (bHasUpper && us.hasLowerMap())) {
            resultA |= maskTitleCase;
        }
        if (bHasUpper && !us.hasLowerMap() && !us.hasTitleMap() && verbose) {
            System.out.println("Warning: Character " + hex4(c) + " has upper but " +
                               "no title case; Java won't know this");
        }
        if (offset < minOffsetSeen) minOffsetSeen = offset;
        if (offset > maxOffsetSeen) maxOffsetSeen = offset;
        if (offset > maxOffset || offset < minOffset) {
            if (DEBUG) {
            FAIL("Case offset " + offset + " for character " + hex4(c) + " must be handled as a special case");
            }
            offset = maskCase;
        }
        resultA |= ((offset & maskCase) << shiftCaseOffset);
        if (us.generalCategory == UnicodeSpec.LOWERCASE_LETTER
                || us.generalCategory == UnicodeSpec.UPPERCASE_LETTER
                || us.generalCategory == UnicodeSpec.TITLECASE_LETTER
                || us.generalCategory == UnicodeSpec.MODIFIER_LETTER
                || us.generalCategory == UnicodeSpec.OTHER_LETTER
                || us.generalCategory == UnicodeSpec.LETTER_NUMBER) {
            resultA |= valueJavaUnicodeStart;
        }
        else if (us.generalCategory == UnicodeSpec.COMBINING_SPACING_MARK
                || us.generalCategory == UnicodeSpec.NON_SPACING_MARK
                || us.generalCategory == UnicodeSpec.DECIMAL_DIGIT_NUMBER) {
            resultA |= valueJavaUnicodePart;
        }
        else if (us.generalCategory == UnicodeSpec.CONNECTOR_PUNCTUATION) {
            resultA |= valueJavaStartUnicodePart;
        }
        else if (us.generalCategory == UnicodeSpec.CURRENCY_SYMBOL) {
            resultA |= valueJavaOnlyStart;
        }
        else if (((c >= 0x0000) && (c <= 0x0008))
                || ((c >= 0x000E) && (c <= 0x001B))
                || ((c >= 0x007F) && (c <= 0x009F))
                || us.generalCategory == UnicodeSpec.FORMAT) {
            resultA |= valueIgnorable;
        }
        else if (us.generalCategory == UnicodeSpec.SPACE_SEPARATOR
                || us.generalCategory == UnicodeSpec.LINE_SEPARATOR
                || us.generalCategory == UnicodeSpec.PARAGRAPH_SEPARATOR) {
            if (!isInvalidJavaWhiteSpace(c)) resultA |= valueJavaWhitespace;
        }
        else if (((c >= 0x0009) && (c <= 0x000D))
                || ((c >= 0x001C) && (c <= 0x001F))) {
            resultA |= valueJavaWhitespace;
        }
        if (!nobidi) {
            int tmpBidi =
                (us.bidiCategory > UnicodeSpec.DIRECTIONALITY_OTHER_NEUTRALS ||
                    us.bidiCategory == -1) ? maskBidi : (us.bidiCategory << shiftBidi);
            resultA |= tmpBidi;
        }
        if (!nomirror) {
            resultA |= us.mirrored ? maskMirrored : 0;
        }
        if (identifiers) {
            long replacement = 0;
            if ((resultA & maskIdentifierInfo) >= lowJavaStart) {
                replacement |= bitJavaStart;
            }
            if ( ((resultA & nonzeroJavaPart) != 0)
                    && ((resultA & maskIdentifierInfo) != valueIgnorable)) {
                replacement |= bitJavaPart;
            }
            resultA = replacement;
        }
        return resultA;
    }
    static void addExProp(long[] map, PropList propList, String prop, long mask) {
        List<Integer> cps = propList.codepoints(prop);
        if (cps != null) {
            for (Integer cp : cps) {
                if (cp < map.length)
                    map[cp] |= mask;
            }
        }
    }
    static long[][] buildTable(long[] map, int size) {
        int n = map.length;
        if (((n >> size) << size) != n) {
            FAIL("Length " + n + " is not a multiple of " + (1 << size));
        }
        int m = 1 << size;
        long[] newmap = new long[n >> size];
        long[] buffer = new long[n];
        int ptr = 0;
OUTER:  for (int i = 0; i < n; i += m) {
    MIDDLE: for (int j = 0; j < ptr; j += m) {
                for (int k = 0; k < m; k++) {
                    if (buffer[j+k] != map[i+k])
                        continue MIDDLE;
                }
                newmap[i >> size] = (j >> size);
                continue OUTER;
            } 
            for (int k = 0; k < m; k++) {
                buffer[ptr+k] = map[i+k];
            }
            newmap[i >> size] = (ptr >> size);
            ptr += m;
        } 
        long[] newdata = new long[ptr];
        for (int j = 0; j < ptr; j++) {
            newdata[j] = buffer[j];
        }
        long[][] result = { newmap, newdata };
        return result;
    }
    static void generateCharacterClass(String theTemplateFileName,
                                       String theOutputFileName)
        throws FileNotFoundException, IOException {
        BufferedReader in = new BufferedReader(new FileReader(theTemplateFileName));
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(theOutputFileName)));
        out.println(commentStart +
            " This file was generated AUTOMATICALLY from a template file " +
            new java.util.Date() + commentEnd);
        int marklen = commandMarker.length();
        LOOP: while(true) {
            try {
                String line = in.readLine();
                if (line == null) break LOOP;
                int pos = 0;
                int depth = 0;
                while ((pos = line.indexOf(commandMarker, pos)) >= 0) {
                    int newpos = pos + marklen;
                    char ch = 'x';
                    SCAN: while (newpos < line.length() &&
                            (Character.isJavaIdentifierStart(ch = line.charAt(newpos))
                            || ch == '(' || (ch == ')' && depth > 0))) {
                        ++newpos;
                        if (ch == '(') {
                            ++depth;
                        }
                        else if (ch == ')') {
                            --depth;
                            if (depth == 0)
                                break SCAN;
                        }
                    }
                    String replacement = replaceCommand(line.substring(pos + marklen, newpos));
                    line = line.substring(0, pos) + replacement + line.substring(newpos);
                    pos += replacement.length();
                }
                out.println(line);
            }
            catch (IOException e) {
                break LOOP;
            }
        }
        in.close();
        out.close();
    }
    static String replaceCommand(String x) {
        if (x.equals("Tables")) return genTables();
        if (x.equals("Initializers")) return genInitializers();
        if (x.length() >= 9 && x.substring(0, 7).equals("Lookup(") &&
                x.substring(x.length()-1).equals(")") )
            return genAccess("A", x.substring(7, x.length()-1), (identifiers ? 2 : 32));
        if (x.length() >= 11 && x.substring(0, 9).equals("LookupEx(") &&
                x.substring(x.length()-1).equals(")") )
            return genAccess("B", x.substring(9, x.length()-1), 16);
        if (x.equals("shiftType")) return Long.toString(shiftType);
        if (x.equals("shiftIdentifierInfo")) return Long.toString(shiftIdentifierInfo);
        if (x.equals("maskIdentifierInfo")) return "0x" + hex8(maskIdentifierInfo);
        if (x.equals("maskUnicodePart")) return "0x" + hex8(maskUnicodePart);
        if (x.equals("shiftCaseOffset")) return Long.toString(shiftCaseOffset);
        if (x.equals("shiftCaseInfo")) return Long.toString(shiftCaseInfo);
        if (x.equals("shiftCaseOffsetSign")) return Long.toString(shiftCaseOffsetSign);
        if (x.equals("maskCase")) return "0x" + hex8(maskCase);
        if (x.equals("maskCaseOffset")) return "0x" + hex8(maskCaseOffset);
        if (x.equals("maskLowerCase")) return "0x" + hex8(maskLowerCase);
        if (x.equals("maskUpperCase")) return "0x" + hex8(maskUpperCase);
        if (x.equals("maskTitleCase")) return "0x" + hex8(maskTitleCase);
        if (x.equals("maskOtherLowercase")) return "0x" + hex4(maskOtherLowercase >> 32);
        if (x.equals("maskOtherUppercase")) return "0x" + hex4(maskOtherUppercase >> 32);
        if (x.equals("maskOtherAlphabetic")) return "0x" + hex4(maskOtherAlphabetic >> 32);
        if (x.equals("maskIdeographic")) return "0x" + hex4(maskIdeographic >> 32);
        if (x.equals("valueIgnorable")) return "0x" + hex8(valueIgnorable);
        if (x.equals("valueJavaUnicodeStart")) return "0x" + hex8(valueJavaUnicodeStart);
        if (x.equals("valueJavaOnlyStart")) return "0x" + hex8(valueJavaOnlyStart);
        if (x.equals("valueJavaUnicodePart")) return "0x" + hex8(valueJavaUnicodePart);
        if (x.equals("valueJavaOnlyPart")) return "0x" + hex8(valueJavaOnlyPart);
        if (x.equals("valueJavaWhitespace")) return "0x" + hex8(valueJavaWhitespace);
        if (x.equals("lowJavaStart")) return "0x" + hex8(lowJavaStart);
        if (x.equals("nonzeroJavaPart")) return "0x" + hex8(nonzeroJavaPart);
        if (x.equals("bitJavaStart")) return "0x" + hex8(bitJavaStart);
        if (x.equals("bitJavaPart")) return Long.toString(bitJavaPart);
        if (x.equals("valueUnicodeStart")) return "0x" + hex8(valueUnicodeStart);
        if (x.equals("maskIsJavaIdentifierStart")) return "0x" + hex(maskIsJavaIdentifierStart);
        if (x.equals("maskIsJavaIdentifierPart")) return "0x" + hex(maskIsJavaIdentifierPart);
        if (x.equals("shiftDigitOffset")) return Long.toString(shiftDigitOffset);
        if (x.equals("maskDigitOffset")) return "0x" + hex(maskDigitOffset);
        if (x.equals("maskDigit")) return "0x" + hex(maskDigit);
        if (x.equals("shiftNumericType")) return Long.toString(shiftNumericType);
        if (x.equals("maskNumericType")) return "0x" + hex(maskNumericType);
        if (x.equals("valueNotNumeric")) return "0x" + hex8(valueNotNumeric);
        if (x.equals("valueDigit")) return "0x" + hex8(valueDigit);
        if (x.equals("valueStrangeNumeric")) return "0x" + hex8(valueStrangeNumeric);
        if (x.equals("valueJavaSupradecimal")) return "0x" + hex8(valueJavaSupradecimal);
        if (x.equals("valueDigit")) return "0x" + hex8(valueDigit);
        if (x.equals("valueStrangeNumeric")) return "0x" + hex8(valueStrangeNumeric);
        if (x.equals("maskType")) return "0x" + hex(maskType);
        if (x.equals("shiftBidi")) return Long.toString(shiftBidi);
        if (x.equals("maskBidi")) return "0x" + hex(maskBidi);
        if (x.equals("maskMirrored")) return "0x" + hex8(maskMirrored);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.UNASSIGNED][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.UNASSIGNED);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.UPPERCASE_LETTER][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.UPPERCASE_LETTER);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.LOWERCASE_LETTER][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.LOWERCASE_LETTER);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.TITLECASE_LETTER][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.TITLECASE_LETTER);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.MODIFIER_LETTER][UnicodeSpec.LONG]))
             return Integer.toString(UnicodeSpec.MODIFIER_LETTER);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.OTHER_LETTER][UnicodeSpec.LONG]))
             return Integer.toString(UnicodeSpec.OTHER_LETTER);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.NON_SPACING_MARK][UnicodeSpec.LONG]))
             return Integer.toString(UnicodeSpec.NON_SPACING_MARK);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.ENCLOSING_MARK][UnicodeSpec.LONG]))
             return Integer.toString(UnicodeSpec.ENCLOSING_MARK);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.COMBINING_SPACING_MARK][UnicodeSpec.LONG]))
             return Integer.toString(UnicodeSpec.COMBINING_SPACING_MARK);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.DECIMAL_DIGIT_NUMBER][UnicodeSpec.LONG]))
             return Integer.toString(UnicodeSpec.DECIMAL_DIGIT_NUMBER);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.OTHER_NUMBER][UnicodeSpec.LONG]))
             return Integer.toString(UnicodeSpec.OTHER_NUMBER);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.SPACE_SEPARATOR][UnicodeSpec.LONG]))
             return Integer.toString(UnicodeSpec.SPACE_SEPARATOR);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.LINE_SEPARATOR][UnicodeSpec.LONG]))
             return Integer.toString(UnicodeSpec.LINE_SEPARATOR);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.PARAGRAPH_SEPARATOR][UnicodeSpec.LONG]))
             return Integer.toString(UnicodeSpec.PARAGRAPH_SEPARATOR);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.CONTROL][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.CONTROL);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.FORMAT][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.FORMAT);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.PRIVATE_USE][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.PRIVATE_USE);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.SURROGATE][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.SURROGATE);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.DASH_PUNCTUATION][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.DASH_PUNCTUATION);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.START_PUNCTUATION][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.START_PUNCTUATION);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.END_PUNCTUATION][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.END_PUNCTUATION);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.INITIAL_QUOTE_PUNCTUATION][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.INITIAL_QUOTE_PUNCTUATION);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.FINAL_QUOTE_PUNCTUATION][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.FINAL_QUOTE_PUNCTUATION);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.CONNECTOR_PUNCTUATION][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.CONNECTOR_PUNCTUATION);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.OTHER_PUNCTUATION][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.OTHER_PUNCTUATION);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.LETTER_NUMBER][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.LETTER_NUMBER);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.MATH_SYMBOL][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.MATH_SYMBOL);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.CURRENCY_SYMBOL][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.CURRENCY_SYMBOL);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.MODIFIER_SYMBOL][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.MODIFIER_SYMBOL);
        if (x.equals(UnicodeSpec.generalCategoryList[UnicodeSpec.OTHER_SYMBOL][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.OTHER_SYMBOL);
        if (x.equals(UnicodeSpec.bidiCategoryList[UnicodeSpec.DIRECTIONALITY_LEFT_TO_RIGHT][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.DIRECTIONALITY_LEFT_TO_RIGHT);
        if (x.equals(UnicodeSpec.bidiCategoryList[UnicodeSpec.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING);
        if (x.equals(UnicodeSpec.bidiCategoryList[UnicodeSpec.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE);
        if (x.equals(UnicodeSpec.bidiCategoryList[UnicodeSpec.DIRECTIONALITY_RIGHT_TO_LEFT][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.DIRECTIONALITY_RIGHT_TO_LEFT);
        if (x.equals(UnicodeSpec.bidiCategoryList[UnicodeSpec.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC);
        if (x.equals(UnicodeSpec.bidiCategoryList[UnicodeSpec.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING);
        if (x.equals(UnicodeSpec.bidiCategoryList[UnicodeSpec.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE);
        if (x.equals(UnicodeSpec.bidiCategoryList[UnicodeSpec.DIRECTIONALITY_POP_DIRECTIONAL_FORMAT][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.DIRECTIONALITY_POP_DIRECTIONAL_FORMAT);
        if (x.equals(UnicodeSpec.bidiCategoryList[UnicodeSpec.DIRECTIONALITY_EUROPEAN_NUMBER][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.DIRECTIONALITY_EUROPEAN_NUMBER);
        if (x.equals(UnicodeSpec.bidiCategoryList[UnicodeSpec.DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR);
        if (x.equals(UnicodeSpec.bidiCategoryList[UnicodeSpec.DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR);
        if (x.equals(UnicodeSpec.bidiCategoryList[UnicodeSpec.DIRECTIONALITY_ARABIC_NUMBER][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.DIRECTIONALITY_ARABIC_NUMBER);
        if (x.equals(UnicodeSpec.bidiCategoryList[UnicodeSpec.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR);
        if (x.equals(UnicodeSpec.bidiCategoryList[UnicodeSpec.DIRECTIONALITY_NONSPACING_MARK][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.DIRECTIONALITY_NONSPACING_MARK);
         if (x.equals(UnicodeSpec.bidiCategoryList[UnicodeSpec.DIRECTIONALITY_BOUNDARY_NEUTRAL][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.DIRECTIONALITY_BOUNDARY_NEUTRAL);
        if (x.equals(UnicodeSpec.bidiCategoryList[UnicodeSpec.DIRECTIONALITY_PARAGRAPH_SEPARATOR][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.DIRECTIONALITY_PARAGRAPH_SEPARATOR);
        if (x.equals(UnicodeSpec.bidiCategoryList[UnicodeSpec.DIRECTIONALITY_SEGMENT_SEPARATOR][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.DIRECTIONALITY_SEGMENT_SEPARATOR);
        if (x.equals(UnicodeSpec.bidiCategoryList[UnicodeSpec.DIRECTIONALITY_WHITESPACE][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.DIRECTIONALITY_WHITESPACE);
        if (x.equals(UnicodeSpec.bidiCategoryList[UnicodeSpec.DIRECTIONALITY_OTHER_NEUTRALS][UnicodeSpec.LONG]))
            return Integer.toString(UnicodeSpec.DIRECTIONALITY_OTHER_NEUTRALS);
        FAIL("Unknown text substitution marker " + commandMarker + x);
        return commandMarker + x;
    }
    static String genTables() {
        int n = sizes.length;
        StringBuffer result = new StringBuffer();
        result.append(commentStart + " The following tables and code generated using:" +
                  commentEnd + "\n  ");
        result.append(commentStart + ' ' + commandLineDescription + commentEnd + "\n  ");
                if (plane == 0 && bLatin1 == false) {
            genCaseMapTableDeclaration(result);
            genCaseMapTable(initializers, specialCaseMaps);
                }
        int totalBytes = 0;
        for (int k = 0; k < n - 1; k++) {
            genTable(result, tableNames[k], tables[k], 0, bytes[k]<<3, sizes[k], preshifted[k],
                sizes[k+1], false, false, k==0);
            int s = bytes[k];
            if (s == 1 && useCharForByte) {
                s = 2;
            }
            totalBytes += tables[k].length * s;
        }
        genTable(result, "A", tables[n - 1], 0, (identifiers ? 2 : 32),
            sizes[n - 1], false, 0, true, !(identifiers), false);
        genTable(result, "B", tables[n - 1], 32, 16, sizes[n - 1], false, 0, true, true, false);
        totalBytes += ((((tables[n - 1].length * (identifiers ? 2 : 32)) + 31) >> 5) << 2);
        result.append(commentStart);
        result.append(" In all, the character property tables require ");
        result.append(totalBytes).append(" bytes.").append(commentEnd);
        if (verbose) {
            System.out.println("The character property tables require "
                 + totalBytes + " bytes.");
        }
        return result.toString();
    }
    static String genInitializers() {
        return initializers.toString();
    }
    static int getTotalBytes() {
        int n = sizes.length;
        int totalBytes = 0;
        for (int k = 0; k < n - 1; k++) {
            totalBytes += tables[k].length * bytes[k];
        }
        totalBytes += ((((tables[n - 1].length * (identifiers ? 2 : 32))
                         + 31) >> 5) << 2);
        return totalBytes;
    }
    static void appendEscapedStringFragment(StringBuffer result,
                                            char[] line,
                                            int length,
                                            boolean lastFragment) {
        result.append("    \"");
        for (int k=0; k<length; ++k) {
            result.append("\\u");
            result.append(hex4(line[k]));
        }
        result.append("\"");
        result.append(lastFragment ? ";" : "+");
        result.append("\n");
    }
    static String SMALL_INITIALIZER =
        "        { 
        "            int len = $$name_DATA.length();\n"+
        "            int j=0;\n"+
        "            for (int i=0; i<len; ++i) {\n"+
        "                int c = $$name_DATA.charAt(i);\n"+
        "                for (int k=0; k<$$entriesPerChar; ++k) {\n"+
        "                    $$name[j++] = ($$type)c;\n"+
        "                    c >>= $$bits;\n"+
        "                }\n"+
        "            }\n"+
        "            assert (j == $$size);\n"+
        "        }\n";
    static String SAME_SIZE_INITIALIZER =
        "        { 
        "            assert ($$name_DATA.length() == $$size);\n"+
        "            for (int i=0; i<$$size; ++i)\n"+
        "                $$name[i] = ($$type)$$name_DATA.charAt(i);\n"+
        "        }\n";
    static String BIG_INITIALIZER =
        "        { 
        "            int len = $$name_DATA.length();\n"+
        "            int j=0;\n"+
        "            int charsInEntry=0;\n"+
        "            $$type entry=0;\n"+
        "            for (int i=0; i<len; ++i) {\n"+
        "                entry |= $$name_DATA.charAt(i);\n"+
        "                if (++charsInEntry == $$charsPerEntry) {\n"+
        "                    $$name[j++] = entry;\n"+
        "                    entry = 0;\n"+
        "                    charsInEntry = 0;\n"+
        "                }\n"+
        "                else {\n"+
        "                    entry <<= 16;\n"+
        "                }\n"+
        "            }\n"+
        "            assert (j == $$size);\n"+
        "        }\n";
    static String INT32_INITIALIZER =
        "        { 
        "            char[] data = $$name_DATA.toCharArray();\n"+
        "            assert (data.length == ($$size * 2));\n"+
        "            int i = 0, j = 0;\n"+
        "            while (i < ($$size * 2)) {\n"+
        "                int entry = data[i++] << 16;\n"+
        "                $$name[j++] = entry | data[i++];\n"+
        "            }\n"+
        "        }\n";
    static void addInitializer(String name, String type, int entriesPerChar,
                               int bits, int size) {
        String template = (entriesPerChar == 1) ? SAME_SIZE_INITIALIZER :
                          ((entriesPerChar > 0) ? SMALL_INITIALIZER : BIG_INITIALIZER);
        if (entriesPerChar == -2) {
            template = INT32_INITIALIZER;
        }
        int marklen = commandMarker.length();
        int pos = 0;
        while ((pos = template.indexOf(commandMarker, pos)) >= 0) {
            int newpos = pos + marklen;
            char ch = 'x';
            while (newpos < template.length() &&
                   Character.isJavaIdentifierStart(ch = template.charAt(newpos)) &&
                   ch != '_') 
                ++newpos;
            String token = template.substring(pos+marklen, newpos);
            String replacement = "ERROR";
            if (token.equals("name")) replacement = name;
            else if (token.equals("type")) replacement = type;
            else if (token.equals("bits")) replacement = ""+bits;
            else if (token.equals("size")) replacement = ""+size;
            else if (token.equals("entriesPerChar")) replacement = ""+entriesPerChar;
            else if (token.equals("charsPerEntry")) replacement = ""+(-entriesPerChar);
            else FAIL("Unrecognized token: " + token);
            template = template.substring(0, pos) + replacement + template.substring(newpos);
            pos += replacement.length();
        }
        initializers.append(template);
    }
    static void genTable(StringBuffer result, String name,
                         long[] table, int extract, int bits, int size,
                         boolean preshifted, int shift, boolean hexFormat,
                         boolean properties, boolean hexComment) {
        String atype = bits == 1 ? (Csyntax ? "unsigned long" : "int") :
            bits == 2 ? (Csyntax ? "unsigned long" : "int") :
            bits == 4 ? (Csyntax ? "unsigned long" : "int") :
            bits == 8 ? (Csyntax ? "unsigned char" : "byte") :
            bits == 16 ? (Csyntax ? "unsigned short" : "char") :
            bits == 32 ? (Csyntax ? "unsigned long" : "int") :
            (Csyntax ? "int64" : "long");
        long maxPosEntry = bits == 1 ? Integer.MAX_VALUE : 
            bits == 2 ? Integer.MAX_VALUE :
            bits == 4 ? Integer.MAX_VALUE :
            bits == 8 ? Byte.MAX_VALUE :
            bits == 16 ? Short.MAX_VALUE :
            bits == 32 ? Integer.MAX_VALUE :
            Long.MAX_VALUE;
        int entriesPerChar = bits <= 16 ? (16 / bits) : -(bits / 16);
        boolean shiftEntries = preshifted && shift != 0;
        if (bits == 8 && tableAsString && useCharForByte) {
            atype = "char";
            maxPosEntry = Character.MAX_VALUE;
            entriesPerChar = 1;
        }
        boolean noConversion = atype.equals("char");
        result.append(commentStart);
        result.append(" The ").append(name).append(" table has ").append(table.length);
        result.append(" entries for a total of ");
        int sizeOfTable = ((table.length * bits + 31) >> 5) << 2;
        if (bits == 8 && useCharForByte) {
            sizeOfTable *= 2;
        }
        result.append(sizeOfTable);
        result.append(" bytes.").append(commentEnd).append("\n\n");
        if (Csyntax)
            result.append("  static ");
        else
            result.append("  static final ");
        result.append(atype);
        result.append(" ").append(name).append("[");
        if (Csyntax)
            result.append(table.length >> (bits == 1 ? 5 : bits == 2 ? 4 : bits == 4 ? 3 : 0));
        if (tableAsString) {
            if (noConversion) {
                result.append("] = (\n");
            } else {
                result.append("] = new ").append(atype).append("["+table.length+"];\n  ");
                result.append("static final String ").append(name).append("_DATA =\n");
            }
            int CHARS_PER_LINE = 8;
            StringBuffer theString = new StringBuffer();
            int entriesInCharSoFar = 0;
            char ch = '\u0000';
            int charsPerEntry = -entriesPerChar;
            for (int j=0; j<table.length; ++j) {
                long entry;
                if ("A".equals(name))
                    entry = (table[j] & 0xffffffffL) >> extract;
                else
                    entry = (table[j] >> extract);
                if (shiftEntries) entry <<= shift;
                if (entry >= (1L << bits)) {
                    FAIL("Entry too big");
                }
                if (entriesPerChar > 0) {
                    ch = (char)(((int)ch >> bits) | (entry << (entriesPerChar-1)*bits));
                    ++entriesInCharSoFar;
                    if (entriesInCharSoFar == entriesPerChar) {
                        theString.append(ch);
                        entriesInCharSoFar = 0;
                        ch = '\u0000';
                    }
                }
                else {
                    for (int k=0; k<charsPerEntry; ++k) {
                        ch = (char)(entry >> ((charsPerEntry-1)*16));
                        entry <<= 16;
                        theString.append(ch);
                    }
                }
            }
            if (entriesInCharSoFar > 0) {
                while (entriesInCharSoFar < entriesPerChar) {
                    ch = (char)((int)ch >> bits);
                    ++entriesInCharSoFar;
                }
                theString.append(ch);
                entriesInCharSoFar = 0;
            }
            result.append(Utility.formatForSource(theString.toString(), "    "));
            if (noConversion) {
                result.append(").toCharArray()");
            }
            result.append(";\n\n  ");
            if (!noConversion) {
                addInitializer(name, atype, entriesPerChar, bits, table.length);
            }
        }
        else {
            result.append("] = {");
            boolean castEntries = shiftEntries && (bits < 32);
            int printPerLine = hexFormat ? (bits == 1 ? 32*4 :
                bits == 2 ? 16*4 :
                bits == 4 ? 8*4 :
                bits == 8 ? 8 :
                bits == 16 ? 8 :
                bits == 32 ? 4 : 2) :
                (bits == 8 ? 8 :
                bits == 16 ? 8 : 4);
            int printMask = properties ? 0 :
            Math.min(1 << size,
                printPerLine >> (castEntries ? (Csyntax ? 2 : 1) : 0)) - 1;
            int commentShift = ((1 << size) == table.length) ? 0 : size;
            int commentMask = ((1 << size) == table.length) ? printMask : (1 << size) - 1;
            long val = 0;
            for (int j = 0; j < table.length; j++) {
                if ((j & printMask) == 0) {
                    while (result.charAt(result.length() - 1) == ' ')
                        result.setLength(result.length() - 1);
                    result.append("\n    ");
                }
        PRINT:  {
                if (castEntries)
                    result.append("(").append(atype).append(")(");
                long entry = table[j] >> extract;
                int packMask = ((1 << (bits == 1 ? 5 : bits == 2 ? 4 : bits == 4 ? 3 : 2)) - 1);
                int k = j & packMask;
                if (bits >= 8)
                    val = entry;
                else if (k == 0) {
                    val = entry;
                    break PRINT;
                }
                else {
                    val |= (entry << (k*bits));
                    if (k != packMask)
                        break PRINT;
                }
                if (val > maxPosEntry && !Csyntax) { 
                    result.append('-');
                    val = maxPosEntry + maxPosEntry + 2 - val;
                }
                if (hexFormat) {
                    result.append("0x");
                    if (bits == 8)
                        result.append(hex2((byte)val));
                    else if (bits == 16)
                        result.append(hex4((short)val));
                    else if (bits == 32 || bits < 8)
                        result.append(hex8((int)val));
                    else {
                        result.append(hex16((long)val));
                        if (!Csyntax)
                            result.append("L");
                    }
                }
                else {
                    if (bits == 8)
                        result.append(dec3(val));
                    else if (bits == 64) {
                        result.append(dec5(val));
                        if (!Csyntax)
                            result.append("L");
                    }
                    else
                        result.append(dec5(val));
                }
                if (shiftEntries)
                    result.append("<<").append(shift);
                if (castEntries) result.append(")");
                if (j < (table.length - 1))
                    result.append(", ");
                else
                    result.append("  ");
                if ((j & printMask) == printMask) {
                    result.append(" ").append(commentStart).append(" ");
                    if (hexComment)
                        result.append("0x").append(hex4((j & ~commentMask) << (16 - size)));
                    else
                        result.append(dec3((j & ~commentMask) >> commentShift));
                    if (properties) propertiesComments(result, val);
                    result.append(commentEnd);
                }
                } 
            }
            result.append("\n  };\n\n  ");
        }
    }
    static void genCaseMapTableDeclaration(StringBuffer result) {
        String myTab = "    ";
        result.append(myTab + "static final char[][][] charMap;\n");
    }
    static void genCaseMapTable(StringBuffer result, SpecialCaseMap[] specialCaseMaps){
        String myTab = "    ";
        int ch;
        char[] map;
        result.append(myTab + "charMap = new char[][][] {\n");
        for (int x = 0; x < specialCaseMaps.length; x++) {
            ch = specialCaseMaps[x].getCharSource();
            map = specialCaseMaps[x].getUpperCaseMap();
            result.append(myTab + myTab);
            result.append("{ ");
            result.append("{\'\\u"+hex4(ch)+"\'}, {");
            for (int y = 0; y < map.length; y++) {
                result.append("\'\\u"+hex4(map[y])+"\', ");
            }
            result.append("} },\n");
        }
        result.append(myTab + "};\n");
    }
    static void propertiesComments(StringBuffer result, long val) {
        result.append("   ");
        switch ((int)(val & maskType)) {
            case UnicodeSpec.CONTROL:
                result.append("Cc");
                break;
            case UnicodeSpec.FORMAT:
                result.append("Cf");
                break;
            case UnicodeSpec.PRIVATE_USE:
                result.append("Co");
                break;
            case UnicodeSpec.SURROGATE:
                result.append("Cs");
                break;
            case UnicodeSpec.LOWERCASE_LETTER:
                result.append("Ll");
                break;
            case UnicodeSpec.MODIFIER_LETTER:
                result.append("Lm");
                break;
            case UnicodeSpec.OTHER_LETTER:
                result.append("Lo");
                break;
            case UnicodeSpec.TITLECASE_LETTER:
                result.append("Lt");
                break;
            case UnicodeSpec.UPPERCASE_LETTER:
                result.append("Lu");
                break;
            case UnicodeSpec.COMBINING_SPACING_MARK:
                result.append("Mc");
                break;
            case UnicodeSpec.ENCLOSING_MARK:
                result.append("Me");
                break;
            case UnicodeSpec.NON_SPACING_MARK:
                result.append("Mn");
                break;
            case UnicodeSpec.DECIMAL_DIGIT_NUMBER:
                result.append("Nd");
                break;
            case UnicodeSpec.LETTER_NUMBER:
                result.append("Nl");
                break;
            case UnicodeSpec.OTHER_NUMBER:
                result.append("No");
                break;
            case UnicodeSpec.CONNECTOR_PUNCTUATION:
                result.append("Pc");
                break;
            case UnicodeSpec.DASH_PUNCTUATION:
                result.append("Pd");
                break;
            case UnicodeSpec.END_PUNCTUATION:
                result.append("Pe");
                break;
            case UnicodeSpec.OTHER_PUNCTUATION:
                result.append("Po");
                break;
            case UnicodeSpec.START_PUNCTUATION:
                result.append("Ps");
                break;
            case UnicodeSpec.CURRENCY_SYMBOL:
                result.append("Sc");
                break;
            case UnicodeSpec.MODIFIER_SYMBOL:
                result.append("Sk");
                break;
            case UnicodeSpec.MATH_SYMBOL:
                result.append("Sm");
                break;
            case UnicodeSpec.OTHER_SYMBOL:
                result.append("So");
                break;
            case UnicodeSpec.LINE_SEPARATOR:
                result.append("Zl"); break;
            case UnicodeSpec.PARAGRAPH_SEPARATOR:
                result.append("Zp");
                break;
            case UnicodeSpec.SPACE_SEPARATOR:
                result.append("Zs");
                break;
            case UnicodeSpec.UNASSIGNED:
                result.append("unassigned");
                break;
        }
        switch ((int)((val & maskBidi) >> shiftBidi)) {
            case UnicodeSpec.DIRECTIONALITY_LEFT_TO_RIGHT:
                result.append(", L");
                break;
            case UnicodeSpec.DIRECTIONALITY_RIGHT_TO_LEFT:
                result.append(", R");
                break;
            case UnicodeSpec.DIRECTIONALITY_EUROPEAN_NUMBER:
                result.append(", EN");
                break;
            case UnicodeSpec.DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR:
                result.append(", ES");
                break;
            case UnicodeSpec.DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR:
                result.append(", ET");
                break;
            case UnicodeSpec.DIRECTIONALITY_ARABIC_NUMBER:
                result.append(", AN");
                break;
            case UnicodeSpec.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR:
                result.append(", CS");
                break;
            case UnicodeSpec.DIRECTIONALITY_PARAGRAPH_SEPARATOR:
                result.append(", B");
                break;
            case UnicodeSpec.DIRECTIONALITY_SEGMENT_SEPARATOR:
                result.append(", S");
                break;
            case UnicodeSpec.DIRECTIONALITY_WHITESPACE:
                result.append(", WS");
                break;
            case UnicodeSpec.DIRECTIONALITY_OTHER_NEUTRALS:
                result.append(", ON");
                break;
        }
        if ((val & maskUpperCase) != 0) {
            result.append(", hasUpper (subtract ");
            result.append((val & maskCaseOffset) >> shiftCaseOffset).append(")");
        }
        if ((val & maskLowerCase) != 0) {
            result.append(", hasLower (add ");
            result.append((val & maskCaseOffset) >> shiftCaseOffset).append(")");
        }
        if ((val & maskTitleCase) != 0) {
            result.append(", hasTitle");
        }
        if ((val & maskIdentifierInfo) == valueIgnorable) {
            result.append(", ignorable");
        }
        if ((val & maskIdentifierInfo) == valueJavaUnicodePart) {
            result.append(", identifier part");
        }
        if ((val & maskIdentifierInfo) == valueJavaStartUnicodePart) {
            result.append(", underscore");
        }
        if ((val & maskIdentifierInfo) == valueJavaWhitespace) {
            result.append(", whitespace");
        }
        if ((val & maskIdentifierInfo) == valueJavaOnlyStart) {
            result.append(", currency");
        }
        if ((val & maskIdentifierInfo) == valueJavaUnicodeStart) {
            result.append(", identifier start");
        }
        if ((val & maskNumericType) == valueDigit) {
            result.append(", decimal ");
            result.append((val & maskDigitOffset) >> shiftDigitOffset);
        }
        if ((val & maskNumericType) == valueStrangeNumeric) {
            result.append(", strange");
        }
        if ((val & maskNumericType) == valueJavaSupradecimal) {
            result.append(", supradecimal ");
            result.append((val & maskDigitOffset) >> shiftDigitOffset);
        }
    }
    static String[] tableNames = { "X", "Y", "Z", "P", "Q", "R", "S", "T", "U", "V", "W" };
    static String tableName(int j) { return tableNames[j]; }
    static String genAccess(String tbl, String var, int bits) {
        String access = null;
        int bitoffset = bits == 1 ? 5 : bits == 2 ? 4 : bits == 4 ? 3 : 0;
        for (int k = 0; k < sizes.length; k++) {
            int offset = ((k < sizes.length - 1) ? 0 : bitoffset);
            int shift = shifts[k] + offset;
            String shifted = (shift == 0) ? var : "(" + var + ">>" + shift + ")";
            int mask = (1 << (sizes[k] - offset)) - 1;
            String masked = (k == 0) ? shifted :
              "(" + shifted + "&0x" + hex(mask) + ")";
            String index = (k == 0) ? masked :
             (mask == 0) ? access : "(" + access + "|" + masked + ")";
            String indexNoParens = (index.charAt(0) != '(') ? index :
                 index.substring(1, index.length() - 1);
            String tblname = (k == sizes.length - 1) ? tbl : tableName(k);
            String fetched = tblname + "[" + indexNoParens + "]";
            String zeroextended = (zeroextend[k] == 0) ? fetched :
                "(" + fetched + "&0x" + hex(zeroextend[k]) + ")";
            int adjustment = preshifted[k] ? 0 :
               sizes[k+1] - ((k == sizes.length - 2) ? bitoffset : 0);
            String adjusted = (preshifted[k] || adjustment == 0) ? zeroextended :
                "(" + zeroextended + "<<" + adjustment + ")";
            String bitshift = (bits == 1) ? "(" + var + "&0x1F)" :
                (bits == 2) ? "((" + var + "&0xF)<<1)" :
                (bits == 4) ? "((" + var + "&7)<<2)" : null;
            String extracted = ((k < sizes.length - 1) || (bits >= 8)) ? adjusted :
                "((" + adjusted + ">>" + bitshift + ")&" +
                (bits == 4 ? "0xF" : "" + ((1 << bits) - 1)) + ")";
            access = extracted;
        }
        return access;
    }
    static boolean verbose = false;
    static boolean nobidi = false;
    static boolean nomirror = false;
    static boolean identifiers = false;
    static boolean Csyntax = false;
    static String TemplateFileName = null;
    static String OutputFileName = null;
    static String UnicodeSpecFileName = null; 
    static String SpecialCasingFileName = null;
    static String PropListFileName = null;
    static boolean useCharForByte = false;
    static int[] sizes;
    static int bins = 0; 
    static boolean tableAsString = false;
    static boolean bLatin1 = false;
    static String commandLineDescription;
    static int[] shifts;
    static int[] zeroextend;
    static int[] bytes;
    static boolean[] preshifted;
    static long[][] tables;
    static String commentStart;
    static String commentEnd;
    static StringBuffer initializers = new StringBuffer();
    static SpecialCaseMap[] specialCaseMaps;
    static void processArgs(String[] args) {
        StringBuffer desc = new StringBuffer("java GenerateCharacter");
        for (int j=0; j<args.length; ++j) {
            desc.append(" " + args[j]);
        }
        for (int j = 0; j < args.length; j++) {
            if (args[j].equals("-verbose") || args[j].equals("-v"))
                verbose = true;
            else if (args[j].equals("-nobidi"))
                nobidi = true;
            else if (args[j].equals("-nomirror"))
                nomirror = true;
            else if (args[j].equals("-identifiers"))
                identifiers = true;
            else if (args[j].equals("-c"))
                Csyntax = true;
            else if (args[j].equals("-string"))
                tableAsString = true;
            else if (args[j].equals("-o")) {
                if (j == args.length - 1) {
                    FAIL("File name missing after -o");
                }
                else {
                    OutputFileName = args[++j];
                }
            }
            else if (args[j].equals("-search")) {
                if (j == args.length - 1)
                    FAIL("Bin count missing after -search");
                else {
                    bins = Integer.parseInt(args[++j]);
                    if (bins < 1 || bins > 10)
                        FAIL("Bin count must be >= 1 and <= 10");
                }
            }
            else if (args[j].equals("-template")) {
                if (j == args.length - 1)
                    FAIL("File name missing after -template");
                else
                    TemplateFileName = args[++j];
            }
            else if (args[j].equals("-spec")) { 
                if (j == args.length - 1) {
                    FAIL("File name missing after -spec");
                }
                else {
                    UnicodeSpecFileName = args[++j];
                }
            }
            else if (args[j].equals("-specialcasing")) {
                if (j == args.length -1) {
                    FAIL("File name missing after -specialcasing");
                }
                else {
                    SpecialCasingFileName = args[++j];
                }
            }
            else if (args[j].equals("-proplist")) {
                if (j == args.length -1) {
                    FAIL("File name missing after -proplist");
                }
                else {
                    PropListFileName = args[++j];
                }
            }
            else if (args[j].equals("-plane")) {
                if (j == args.length -1) {
                    FAIL("Plane number missing after -plane");
                }
                else {
                    plane = Integer.parseInt(args[++j]);
                }
                if (plane > 0) {
                    bLatin1 = false;
                }
            }
            else if ("-usecharforbyte".equals(args[j])) {
                useCharForByte = true;
            }
            else if (args[j].equals("-latin1")) {
                bLatin1 = true;
                plane = 0;
            }
            else {
                try {
                    int val = Integer.parseInt(args[j]);
                    if (val < 0 || val > 32) FAIL("Incorrect bit field width: " + args[j]);
                    if (sizes == null)
                        sizes = new int[1];
                    else {
                        int[] newsizes = new int[sizes.length + 1];
                        System.arraycopy(sizes, 0, newsizes, 0, sizes.length);
                        sizes = newsizes;
                    }
                    sizes[sizes.length - 1] = val;
                }
                catch(NumberFormatException e) {
                    FAIL("Unknown switch: " + args[j]);
                }
            }
        }
        if (Csyntax && tableAsString) {
            FAIL("Can't specify table as string with C syntax");
        }
        if (sizes == null) {
            desc.append(" [");
            if (identifiers) {
                int[] newsizes = { 8, 4, 4 };           
                desc.append("8 4 4]");
                sizes = newsizes;
            }
            else {
                int[] newsizes = { 10, 5, 1 }; 
                desc.append("10 5 1]");
                sizes = newsizes;
            }
        }
        if (UnicodeSpecFileName == null) { 
            UnicodeSpecFileName = DefaultUnicodeSpecFileName;
            desc.append(" [-spec " + UnicodeSpecFileName + ']');
        }
        if (SpecialCasingFileName == null) {
            SpecialCasingFileName = DefaultSpecialCasingFileName;
            desc.append(" [-specialcasing " + SpecialCasingFileName + ']');
        }
        if (PropListFileName == null) {
            PropListFileName = DefaultPropListFileName;
            desc.append(" [-proplist " + PropListFileName + ']');
        }
        if (TemplateFileName == null) {
            TemplateFileName = (Csyntax ? DefaultCTemplateFileName
                  : DefaultJavaTemplateFileName);
            desc.append(" [-template " + TemplateFileName + ']');
        }
        if (OutputFileName == null) {
            OutputFileName = (Csyntax ? DefaultCOutputFileName
                    : DefaultJavaOutputFileName);
            desc.append(" [-o " + OutputFileName + ']');
        }
        commentStart = (Csyntax ? "" : "");
        commandLineDescription = desc.toString();
    }
    private static void searchBins(long[] map, int binsOccupied) throws Exception {
        int bitsFree = 16;
        for (int i=0; i<binsOccupied; ++i) bitsFree -= sizes[i];
        if (binsOccupied == (bins-1)) {
            sizes[binsOccupied] = bitsFree;
            generateForSizes(map);
        }
        else {
            for (int i=1; i<bitsFree; ++i) { 
                sizes[binsOccupied] = i;
                searchBins(map, binsOccupied+1);
            }
        }
    }
    private static void generateForSizes(long[] map) throws Exception {
        int sum = 0;
        shifts = new int[sizes.length];
        for (int k = sizes.length - 1; k >= 0; k--) {
            shifts[k] = sum;
            sum += sizes[k];
        }
        if ((1 << sum) < map.length || (1 << (sum - 1)) >= map.length) {
            FAIL("Bit field widths total to " + sum +
             ": wrong total for map of size " + map.length);
        }
        tables = new long[sizes.length][];
        tables[sizes.length - 1] = map;
        for (int j = sizes.length - 1; j > 0; j--) {
            if (verbose && bins==0)
                System.err.println("Building map " + (j+1) + " of bit width " + sizes[j]);
            long[][] temp = buildTable(tables[j], sizes[j]);
            tables[j-1] = temp[0];
            tables[j] = temp[1];
        }
        preshifted = new boolean[sizes.length];
        zeroextend = new int[sizes.length];
        bytes = new int[sizes.length];
        for (int j = 0; j < sizes.length - 1; j++) {
            int len = tables[j+1].length;
            int size = sizes[j+1];
            if (len > 0x100 && (len >> size) <= 0x100) {
                len >>= size;
                preshifted[j] = false;
            }
            else if (len > 0x10000 && (len >> size) <= 0x10000) {
                len >>= size;
                preshifted[j] = false;
            }
            else preshifted[j] = true;
            if (Csyntax)
                zeroextend[j] = 0;
            else if (len > 0x7F && len <= 0xFF) {
                if (!useCharForByte) {
                    zeroextend[j] = 0xFF;
                }
            } else if (len > 0x7FFF && len <= 0xFFFF)
                zeroextend[j] = 0xFFFF;
            else zeroextend[j] = 0;
            if (len <= 0x100) bytes[j] = 1;
            else if (len <= 0x10000) bytes[j] = 2;
            else bytes[j] = 4;
        }
        preshifted[sizes.length - 1] = true;
        zeroextend[sizes.length - 1] = 0;
        bytes[sizes.length - 1] = 0;
        if (bins > 0) {
            int totalBytes = getTotalBytes();
            String access = genAccess("A", "ch", (identifiers ? 2 : 32));
            int accessComplexity = 0;
            for (int j=0; j<access.length(); ++j) {
                char ch = access.charAt(j);
                if ("[&|><".indexOf(ch) >= 0) ++accessComplexity;
                if (ch == '<' || ch == '>') ++j;
            }
            System.out.print("(");
            for (int j=0; j<sizes.length; ++j) System.out.print(" " + sizes[j]);
            System.out.println(" ) " + totalBytes + " " + accessComplexity + " " + access);
            return;
        }
        if (verbose) {
            System.out.println("    n\t size\tlength\tshift\tzeroext\tbytes\tpreshifted");
            for (int j = 0; j < sizes.length; j++) {
                System.out.println(dec5(j) + "\t" +
                    dec5(sizes[j]) + "\t" +
                    dec5(tables[j].length) + "\t" +
                    dec5(shifts[j]) + "\t" +
                    dec5(zeroextend[j]) + "\t" +
                    dec5(bytes[j]) + "\t " +
                    preshifted[j]);
            }
        }
        if (verbose) {
            System.out.println("Generating source code for class Character");
            System.out.println("A table access looks like " +
                         genAccess("A", "ch", (identifiers ? 2 : 32)));
        }
        generateCharacterClass(TemplateFileName, OutputFileName);
    }
    public static void main(String[] args) {
        processArgs(args);
        try {
            UnicodeSpec[] data = UnicodeSpec.readSpecFile(new File(UnicodeSpecFileName), plane);
            specialCaseMaps = SpecialCaseMap.readSpecFile(new File(SpecialCasingFileName), plane);
            PropList propList = PropList.readSpecFile(new File(PropListFileName), plane);
            if (verbose) {
                System.out.println(data.length + " items read from Unicode spec file " + UnicodeSpecFileName); 
            }
            long[] map = buildMap(data, specialCaseMaps, propList);
            if (verbose) {
                System.err.println("Completed building of initial map");
            }
            if (bins == 0) {
                generateForSizes(map);
            }
            else {
                while (bins > 0) {
                    sizes = new int[bins];
                    searchBins(map, 0);
                    --bins;
                }
            }
            if (verbose && false) {
                System.out.println("Offset range seen: -" + hex8(-minOffsetSeen) + "..+" +
                             hex8(maxOffsetSeen));
                System.out.println("          allowed: -" + hex8(-minOffset) + "..+" +
                             hex8(maxOffset));
            }
        }
        catch (FileNotFoundException e) { FAIL(e.toString()); }
        catch (IOException e) { FAIL(e.toString()); }
        catch (Throwable e) {
            System.out.println("Unexpected exception:");
            e.printStackTrace();
            FAIL("Unexpected exception!");
        }
        if (verbose) { System.out.println("Done!");}
    }
}   
