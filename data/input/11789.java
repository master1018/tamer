public class BidiConformance {
    private static boolean error = false;
    private static boolean verbose = false;
    private static boolean abort = false;
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-verbose")) {
                verbose = true;
            } else if (arg.equals("-abort")) {
                abort = true;
            }
        }
        BidiConformance bc = new BidiConformance();
        bc.test();
        if (error) {
            throw new RuntimeException("Failed.");
        } else {
            System.out.println("Passed.");
        }
    }
    private void test() {
        testConstants();
        testConstructors();
        testMethods();
        testMethods4Constructor1();  
        testMethods4Constructor2();  
        testMethods4Constructor3();  
    }
    private void testConstants() {
        System.out.println("*** Test constants");
        checkResult("Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT",
                     -2, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        checkResult("Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT",
                     -1, Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT);
        checkResult("Bidi.DIRECTION_LEFT_TO_RIGHT",
                     0, Bidi.DIRECTION_LEFT_TO_RIGHT);
        checkResult("Bidi.DIRECTION_RIGHT_TO_LEFT",
                     1, Bidi.DIRECTION_RIGHT_TO_LEFT);
    }
    private void testConstructors() {
        System.out.println("*** Test constructors");
        testConstructor1();  
        testConstructor2();  
        testConstructor3();  
    }
    private void testMethods() {
        System.out.println("*** Test methods");
        testMethod_createLineBidi1();
        testMethod_createLineBidi2();
        testMethod_getLevelAt();
        testMethod_getRunLevel();
        testMethod_getRunLimit();
        testMethod_getRunStart();
        testMethod_reorderVisually1();
        testMethod_reorderVisually2();
        testMethod_requiresBidi();
    }
    private void testMethods4Constructor1() {
        System.out.println("*** Test methods for constructor 1");
        String paragraph;
        Bidi bidi;
        NumericShaper ns = NumericShaper.getShaper(NumericShaper.ARABIC);
        for (int textNo = 0; textNo < data4Constructor1.length; textNo++) {
            paragraph = data4Constructor1[textNo][0];
            int start = paragraph.indexOf('<')+1;
            int limit = paragraph.indexOf('>');
            int testNo;
            System.out.println("*** Test textNo=" + textNo +
                ": Bidi(AttributedCharacterIterator\"" +
                toReadableString(paragraph) + "\") " +
                "  start=" + start + ", limit=" + limit);
            testNo = 0;
            System.out.println(" Test#" + testNo +": RUN_DIRECTION_LTR");
            AttributedString astr = new AttributedString(paragraph);
            astr.addAttribute(TextAttribute.RUN_DIRECTION,
                              TextAttribute.RUN_DIRECTION_LTR);
            bidi = new Bidi(astr.getIterator());
            callTestEachMethod4Constructor1(textNo, testNo, bidi);
            ++testNo;
            System.out.println(" Test#" + testNo +
                ": RUN_DIRECTION_LTR, BIDI_EMBEDDING(1)");
            astr = new AttributedString(paragraph);
            astr.addAttribute(TextAttribute.RUN_DIRECTION,
                              TextAttribute.RUN_DIRECTION_LTR);
            astr.addAttribute(TextAttribute.BIDI_EMBEDDING, new Integer(1),
                              start, limit);
            bidi = new Bidi(astr.getIterator());
            callTestEachMethod4Constructor1(textNo, testNo, bidi);
            ++testNo;
            System.out.println(" Test#" + testNo +
                ": RUN_DIERCTION_LTR, BIDI_EMBEDDING(2)");
            astr = new AttributedString(paragraph);
            astr.addAttribute(TextAttribute.RUN_DIRECTION,
                              TextAttribute.RUN_DIRECTION_LTR);
            astr.addAttribute(TextAttribute.BIDI_EMBEDDING, new Integer(2),
                              start, limit);
            bidi = new Bidi(astr.getIterator());
            callTestEachMethod4Constructor1(textNo, testNo, bidi);
            ++testNo;
            System.out.println(" Test#" + testNo +
                ": RUN_DIRECTIOIN_LTR, BIDI_EMBEDDING(-3)");
            astr = new AttributedString(paragraph);
            astr.addAttribute(TextAttribute.RUN_DIRECTION,
                              TextAttribute.RUN_DIRECTION_LTR);
            astr.addAttribute(TextAttribute.BIDI_EMBEDDING, new Integer(-3),
                              start, limit);
            bidi = new Bidi(astr.getIterator());
            callTestEachMethod4Constructor1(textNo, testNo, bidi);
            ++testNo;
            System.out.println(" Test#" + testNo +
                ": RUN_DIRECTION_LTR, BIDI_EMBEDDING(-4)");
            astr = new AttributedString(paragraph);
            astr.addAttribute(TextAttribute.RUN_DIRECTION,
                              TextAttribute.RUN_DIRECTION_LTR);
            astr.addAttribute(TextAttribute.BIDI_EMBEDDING, new Integer(-4),
                              start, limit);
            bidi = new Bidi(astr.getIterator());
            callTestEachMethod4Constructor1(textNo, testNo, bidi);
            ++testNo;
            System.out.println(" Test#" + testNo + ": RUN_DIRECTION_RTL");
            astr = new AttributedString(paragraph);
            astr.addAttribute(TextAttribute.RUN_DIRECTION,
                              TextAttribute.RUN_DIRECTION_RTL);
            bidi = new Bidi(astr.getIterator());
            callTestEachMethod4Constructor1(textNo, testNo, bidi);
            ++testNo;
            System.out.println(" Test#" + testNo +
                ": RUN_DIRECTION_RTL, BIDI_EMBEDDING(1)");
            astr = new AttributedString(paragraph);
            astr.addAttribute(TextAttribute.RUN_DIRECTION,
                              TextAttribute.RUN_DIRECTION_RTL);
            astr.addAttribute(TextAttribute.BIDI_EMBEDDING, new Integer(1),
                              start, limit);
            try {
                bidi = new Bidi(astr.getIterator());
                callTestEachMethod4Constructor1(textNo, testNo, bidi);
            }
            catch (IllegalArgumentException e) {
                errorHandling("  Unexpected exception: " + e);
            }
            ++testNo;
            System.out.println(" Test#" + testNo +
                ": RUN_DIRECTION_RTL, BIDI_EMBEDDING(2)");
            astr = new AttributedString(paragraph);
            astr.addAttribute(TextAttribute.RUN_DIRECTION,
                              TextAttribute.RUN_DIRECTION_RTL);
            astr.addAttribute(TextAttribute.BIDI_EMBEDDING, new Integer(2),
                              start, limit);
            try {
                bidi = new Bidi(astr.getIterator());
                callTestEachMethod4Constructor1(textNo, testNo, bidi);
            }
            catch (IllegalArgumentException e) {
                errorHandling("  Unexpected exception: " + e);
            }
            ++testNo;
            System.out.println(" Test#" + testNo +
                ": RUN_DIRECTION_RTL, BIDI_EMBEDDING(-3)");
            astr = new AttributedString(paragraph);
            astr.addAttribute(TextAttribute.RUN_DIRECTION,
                              TextAttribute.RUN_DIRECTION_RTL);
            astr.addAttribute(TextAttribute.BIDI_EMBEDDING, new Integer(-3),
                              start, limit);
            try {
                bidi = new Bidi(astr.getIterator());
                callTestEachMethod4Constructor1(textNo, testNo, bidi);
            }
            catch (IllegalArgumentException e) {
                errorHandling("  Unexpected exception: " + e);
            }
            ++testNo;
            System.out.println(" Test#" + testNo +
                ": RUN_DIRECTION_RTL, BIDI_EMBEDDING(-4)");
            astr = new AttributedString(paragraph);
            astr.addAttribute(TextAttribute.RUN_DIRECTION,
                              TextAttribute.RUN_DIRECTION_RTL);
            astr.addAttribute(TextAttribute.BIDI_EMBEDDING, new Integer(-4),
                              start, limit);
            try {
                bidi = new Bidi(astr.getIterator());
                callTestEachMethod4Constructor1(textNo, testNo, bidi);
            }
            catch (IllegalArgumentException e) {
                errorHandling("  Unexpected exception: " + e);
            }
            ++testNo;
            System.out.println(" Test#" + testNo +
                ": TextAttribute not specified");
            astr = new AttributedString(paragraph);
            bidi = new Bidi(astr.getIterator());
            callTestEachMethod4Constructor1(textNo, testNo, bidi);
            ++testNo;
            System.out.println(" Test#" + testNo +
                ": RUN_DIRECTION_LTR, NUMERIC_SHAPING(ARABIC)");
            astr = new AttributedString(paragraph);
            astr.addAttribute(TextAttribute.RUN_DIRECTION,
                              TextAttribute.RUN_DIRECTION_LTR);
            astr.addAttribute(TextAttribute.NUMERIC_SHAPING, ns);
            bidi = new Bidi(astr.getIterator());
            callTestEachMethod4Constructor1(textNo, testNo, bidi);
            ++testNo;
            System.out.println(" Test#" + testNo +
                 ": RUN_DIRECTION_RTL, NUMERIC_SHAPING(ARABIC)");
            astr = new AttributedString(paragraph);
            astr.addAttribute(TextAttribute.RUN_DIRECTION,
                              TextAttribute.RUN_DIRECTION_RTL);
            astr.addAttribute(TextAttribute.NUMERIC_SHAPING, ns);
            bidi = new Bidi(astr.getIterator());
            callTestEachMethod4Constructor1(textNo, testNo, bidi);
        }
    }
    private void testMethods4Constructor2() {
        System.out.println("*** Test methods for constructor 2");
        String paragraph;
        Bidi bidi;
        for (int textNo = 0; textNo < data4Constructor2.length; textNo++) {
            paragraph = data4Constructor2[textNo][0];
            for (int flagNo = 0; flagNo < FLAGS.length; flagNo++) {
                int flag = FLAGS[flagNo];
                System.out.println("*** Test textNo=" + textNo +
                    ": Bidi(\"" + toReadableString(paragraph) +
                    "\", " + getFlagName(flag) + ")");
                bidi = new Bidi(paragraph, flag);
                callTestEachMethod4Constructor2(textNo, flagNo, bidi);
            }
        }
    }
    private void testMethods4Constructor3() {
        System.out.println("*** Test methods for constructor 3");
        String paragraph;
        Bidi bidi;
        for (int textNo = 0; textNo < data4Constructor3.length; textNo++) {
            paragraph = data4Constructor3[textNo][0];
            char[] c = paragraph.toCharArray();
            int start = paragraph.indexOf('<')+1;
            byte[][] embeddings = (c.length < emb4Constructor3[1][0].length) ?
                                  emb4Constructor3[0] : emb4Constructor3[1];
            for (int flagNo = 0; flagNo < FLAGS.length; flagNo++) {
                int flag = FLAGS[flagNo];
                for (int embNo = 0; embNo < embeddings.length; embNo++) {
                    int dataNo = flagNo * FLAGS.length + embNo;
                    System.out.println("*** Test textNo=" + textNo +
                        ": Bidi(char[]\"" + toReadableString(paragraph) +
                        "\", 0, embeddings={" + toString(embeddings[embNo]) +
                        "}, " + c.length + ", " +
                       getFlagName(flag) + ")" + "  dataNo=" + dataNo);
                    try {
                        bidi = new Bidi(c, 0, embeddings[embNo], 0,
                                        c.length, flag);
                        callTestEachMethod4Constructor3(textNo, dataNo, bidi);
                    }
                    catch (Exception e) {
                        errorHandling("  Unexpected exception: " + e);
                    }
                }
            }
        }
    }
    private void testConstructor1() {
        Bidi bidi;
        try {
            bidi = new Bidi(null);
            errorHandling("Bidi((AttributedCharacterIterator)null) " +
                "should throw an IAE.");
        }
        catch (IllegalArgumentException e) {
        }
        catch (NullPointerException e) {
            errorHandling("Bidi((AttributedCharacterIterator)null) " +
                "should not throw an NPE but an IAE.");
        }
        String paragraph = data4Constructor1[1][0];
        int start = paragraph.indexOf('<')+1;
        int limit = paragraph.indexOf('>');
        AttributedString astr = new AttributedString(paragraph);
        astr.addAttribute(TextAttribute.RUN_DIRECTION,
                          TextAttribute.RUN_DIRECTION_RTL);
        astr.addAttribute(TextAttribute.BIDI_EMBEDDING, new Integer(-61),
                          start, limit);
        try {
            bidi = new Bidi(astr.getIterator());
            for (int i = start; i < limit; i++) {
                if (bidi.getLevelAt(i) != 61) {
                    errorHandling("Bidi(AttributedCharacterIterator).getLevelAt(" +
                        i + ") should not be " + bidi.getLevelAt(i) +
                        " but 60 when BIDI_EMBEDDING is -61.");
                }
            }
        }
        catch (Exception e) {
            errorHandling("  Unexpected exception: " + e);
        }
        astr = new AttributedString(paragraph);
        astr.addAttribute(TextAttribute.RUN_DIRECTION,
                          TextAttribute.RUN_DIRECTION_RTL);
        astr.addAttribute(TextAttribute.BIDI_EMBEDDING, new Integer(-62),
                          start, limit);
        try {
            bidi = new Bidi(astr.getIterator());
            for (int i = start; i < limit; i++) {
                if (bidi.getLevelAt(i) != 1) {
                    errorHandling("Bidi(AttributedCharacterIterator).getLevelAt() " +
                        "should be 1 when BIDI_EMBEDDING is -62.");
                }
            }
        }
        catch (Exception e) {
            errorHandling("  Unexpected exception: " + e);
        }
        astr = new AttributedString(paragraph);
        astr.addAttribute(TextAttribute.RUN_DIRECTION,
                          TextAttribute.RUN_DIRECTION_RTL);
        astr.addAttribute(TextAttribute.BIDI_EMBEDDING, new Integer(60),
                          start, limit);
        try {
            bidi = new Bidi(astr.getIterator());
            for (int i = start; i < limit; i++) {
                if (bidi.getLevelAt(i) != 61) {
                    errorHandling("Bidi(AttributedCharacterIterator).getLevelAt() " +
                        "should be 61 when BIDI_EMBEDDING is 60.");
                }
            }
        }
        catch (Exception e) {
            errorHandling("  Unexpected exception: " + e);
        }
        astr = new AttributedString(paragraph);
        astr.addAttribute(TextAttribute.RUN_DIRECTION,
                          TextAttribute.RUN_DIRECTION_RTL);
        astr.addAttribute(TextAttribute.BIDI_EMBEDDING, new Integer(61),
                          start, limit);
        try {
            bidi = new Bidi(astr.getIterator());
            for (int i = start; i < limit; i++) {
                if (bidi.getLevelAt(i) != 61) {
                    errorHandling("Bidi(AttributedCharacterIterator).getLevelAt(" +
                        i + ") should not be " + bidi.getLevelAt(i) +
                        " but 61 when BIDI_EMBEDDING is 61.");
                }
            }
        }
        catch (Exception e) {
            errorHandling("  Unexpected exception: " + e);
        }
        astr = new AttributedString(paragraph);
        astr.addAttribute(TextAttribute.RUN_DIRECTION,
                          TextAttribute.RUN_DIRECTION_RTL);
        astr.addAttribute(TextAttribute.BIDI_EMBEDDING, new Integer(62),
                          start, limit);
        try {
            bidi = new Bidi(astr.getIterator());
            for (int i = start; i < limit; i++) {
                if (bidi.getLevelAt(i) != 1) {
                    errorHandling("Bidi(AttributedCharacterIterator).getLevelAt()" +
                        " should not be " + bidi.getLevelAt(i) +
                        " but 1 when BIDI_EMBEDDING is 62.");
                }
            }
        }
        catch (Exception e) {
            errorHandling("  Unexpected exception: " + e);
        }
    }
    private void testConstructor2() {
        Bidi bidi;
        try {
            bidi = new Bidi(null, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
            errorHandling("Bidi((String)null, DIRECTION_DEFAULT_LEFT_TO_RIGHT)" +
                " should throw an IAE.");
        }
        catch (IllegalArgumentException e) {
        }
        catch (NullPointerException e) {
            errorHandling("Bidi((String)null, DIRECTION_DEFAULT_LEFT_TO_RIGHT) " +
                "should not throw an NPE but an IAE.");
        }
        try {
            bidi = new Bidi("abc", -3);
        }
        catch (Exception e) {
            errorHandling("Bidi(\"abc\", -3) should not throw an exception: " +
                e);
        }
        try {
            bidi = new Bidi("abc", 2);
        }
        catch (Exception e) {
            errorHandling("Bidi(\"abc\", 2) should not throw an exception: " +
                e);
        }
    }
    private void testConstructor3() {
        char[] text = {'a', 'b', 'c', 'd', 'e'};
        byte[] embeddings = {0, 0, 0, 0, 0};
        Bidi bidi;
        try {
            bidi = new Bidi(null, 0, embeddings, 0, 5,
                            Bidi.DIRECTION_LEFT_TO_RIGHT);
            errorHandling("Bidi(char[], ...) should throw an IAE " +
                "when text=null.");
        }
        catch (IllegalArgumentException e) {
        }
        catch (NullPointerException e) {
            errorHandling("Bidi(char[], ...) should not throw an NPE " +
                "but an IAE when text=null.");
        }
        try {
            bidi = new Bidi(text, -1, embeddings, 0, 5,
                            Bidi.DIRECTION_LEFT_TO_RIGHT);
            errorHandling("Bidi(char[], ...) should throw an IAE " +
                "when textStart is incorrect(-1: too small).");
        }
        catch (IllegalArgumentException e) {
        }
        catch (ArrayIndexOutOfBoundsException e) {
            errorHandling("Bidi(char[], ...) should not throw an NPE " +
                "but an IAE when textStart is incorrect(-1: too small).");
        }
        try {
            bidi = new Bidi(text, 4, embeddings, 0, 2,
                            Bidi.DIRECTION_LEFT_TO_RIGHT);
            errorHandling("Bidi(char[], ...) should throw an IAE " +
                "when textStart is incorrect(4: too large).");
        }
        catch (IllegalArgumentException e) {
        }
        catch (ArrayIndexOutOfBoundsException e) {
            errorHandling("Bidi(char[], ...) should not throw an NPE " +
                "but an IAE when textStart is incorrect(4: too large).");
        }
        byte[] actualLevels = new byte[text.length];
        byte[] validEmbeddings1 = {0, -61, -60, -2, -1};
        byte[] expectedLevels1  = {0,  61,  60,  2,  1};
        try {
            bidi = new Bidi(text, 0, validEmbeddings1, 0, 5,
                            Bidi.DIRECTION_LEFT_TO_RIGHT);
            for (int i = 0; i < text.length; i++) {
                actualLevels[i] = (byte)bidi.getLevelAt(i);
            }
            if (!Arrays.equals(expectedLevels1, actualLevels)) {
                errorHandling("Bidi(char[], ...).getLevelAt()" +
                    " should be {" + toString(actualLevels) +
                    "} when embeddings are {" +
                    toString(expectedLevels1) + "}.");
            }
        }
        catch (Exception e) {
            errorHandling("Bidi(char[], ...) should not throw an exception " +
                "when embeddings is valid(-61).");
        }
        byte[] validEmbeddings2 = {0,  61,  60,  2,  1};
        byte[] expectedLevels2  = {0,  62,  60,  2,  2};
        try {
            bidi = new Bidi(text, 0, validEmbeddings2, 0, 5,
                            Bidi.DIRECTION_LEFT_TO_RIGHT);
            for (int i = 0; i < text.length; i++) {
                actualLevels[i] = (byte)bidi.getLevelAt(i);
            }
            if (!Arrays.equals(expectedLevels2, actualLevels)) {
                errorHandling("Bidi(char[], ...).getLevelAt()" +
                    " should be {" + toString(actualLevels) +
                    "} when embeddings are {" +
                    toString(expectedLevels2) + "}.");
            }
        }
        catch (Exception e) {
            errorHandling("Bidi(char[], ...) should not throw an exception " +
                "when embeddings is valid(61).");
        }
        byte[] invalidEmbeddings1 = {0, -62, 0, 0, 0};
        try {
            bidi = new Bidi(text, 0, invalidEmbeddings1, 0, 5,
                            Bidi.DIRECTION_LEFT_TO_RIGHT);
            if (bidi.getLevelAt(1) != 0) {
                errorHandling("Bidi(char[], ...).getLevelAt(1) should be 0 " +
                    "when embeddings[1] is -62.");
            }
        }
        catch (Exception e) {
            errorHandling("Bidi(char[], ...) should not throw an exception " +
                "even when embeddings includes -62.");
        }
        byte[] invalidEmbeddings2 = {0, 62, 0, 0, 0};
        try {
            bidi = new Bidi(text, 0, invalidEmbeddings2, 0, 5,
                            Bidi.DIRECTION_LEFT_TO_RIGHT);
            if (bidi.getLevelAt(1) != 0) {
                errorHandling("Bidi(char[], ...).getLevelAt(1) should be 0 " +
                    "when embeddings[1] is 62.");
            }
        }
        catch (Exception e) {
            errorHandling("Bidi(char[], ...) should not throw an exception " +
                "even when embeddings includes 62.");
        }
        try {
            bidi = new Bidi(text, 0, embeddings, 0, -1,
                            Bidi.DIRECTION_LEFT_TO_RIGHT);
            errorHandling("Bidi(char[], ...) should throw an IAE " +
                "when paragraphLength=-1(too small).");
        }
        catch (IllegalArgumentException e) {
        }
        catch (NegativeArraySizeException e) {
            errorHandling("Bidi(char[], ...) should not throw an NASE " +
                "but an IAE when paragraphLength=-1(too small).");
        }
        try {
            bidi = new Bidi(text, 0, embeddings, 0, 6,
                            Bidi.DIRECTION_LEFT_TO_RIGHT);
            errorHandling("Bidi(char[], ...) should throw an IAE " +
                "when paragraphLength=6(too large).");
        }
        catch (IllegalArgumentException e) {
        }
        catch (ArrayIndexOutOfBoundsException e) {
            errorHandling("Bidi(char[], ...) should not throw an AIOoBE " +
                "but an IAE when paragraphLength=6(too large).");
        }
        try {
            bidi = new Bidi(text, 0, embeddings, 0, 4, -3);
        }
        catch (Exception e) {
            errorHandling("Bidi(char[], ...) should not throw an exception " +
                "even when flag=-3(too small).");
        }
        try {
            bidi = new Bidi(text, 0, embeddings, 0, 5, 2);
        }
        catch (Exception e) {
            errorHandling("Bidi(char[], ...) should not throw an exception " +
                "even when flag=2(too large).");
        }
    }
    private void callTestEachMethod4Constructor1(int textNo,
                                                 int testNo,
                                                 Bidi bidi) {
        testEachMethod(bidi,
                       data4Constructor1[textNo][0],
                       data4Constructor1[textNo][testNo+1],
                       baseIsLTR4Constructor1[textNo][testNo],
                       isLTR_isRTL4Constructor1[textNo][0][testNo],
                       isLTR_isRTL4Constructor1[textNo][1][testNo]);
System.out.println(bidi.toString());
    }
    private void callTestEachMethod4Constructor2(int textNo,
                                                 int flagNo,
                                                 Bidi bidi) {
        testEachMethod(bidi,
                       data4Constructor2[textNo][0],
                       data4Constructor2[textNo][flagNo+1],
                       baseIsLTR4Constructor2[textNo][flagNo],
                       isLTR_isRTL4Constructor2[textNo][0][flagNo],
                       isLTR_isRTL4Constructor2[textNo][1][flagNo]);
System.out.println(bidi.toString());
    }
    private void callTestEachMethod4Constructor3(int textNo,
                                                 int dataNo,
                                                 Bidi bidi) {
        testEachMethod(bidi,
                       data4Constructor3[textNo][0],
                       data4Constructor3[textNo][dataNo+1],
                       baseIsLTR4Constructor3[textNo][dataNo],
                       isLTR_isRTL4Constructor3[textNo][0][dataNo],
                       isLTR_isRTL4Constructor3[textNo][1][dataNo]);
System.out.println(bidi.toString());
    }
    private StringBuilder sb = new StringBuilder();
    private void testEachMethod(Bidi bidi,
                                String text,
                                String expectedLevels,
                                boolean expectedBaseIsLTR,
                                boolean expectedIsLTR,
                                boolean expectedIsRTL
                               ) {
        boolean actualBoolean = bidi.baseIsLeftToRight();
        checkResult("baseIsLeftToRight()", expectedBaseIsLTR, actualBoolean);
        int expectedInt = (expectedBaseIsLTR) ? 0 : 1;
        int actualInt = bidi.getBaseLevel();
        checkResult("getBaseLevel()", expectedInt, actualInt);
        expectedInt = text.length();
        actualInt = bidi.getLength();
        checkResult("getLength()", expectedInt, actualInt);
        sb.setLength(0);
        for (int i = 0; i < text.length(); i++) {
            sb.append(bidi.getLevelAt(i));
        }
        checkResult("getLevelAt()", expectedLevels, sb.toString());
        expectedInt = getRunCount(expectedLevels);
        actualInt = bidi.getRunCount();
        checkResult("getRunCount()", expectedInt, actualInt);
        if (expectedInt == actualInt) {
            int runCount = expectedInt;
            int[] expectedRunLevels = getRunLevels_int(runCount, expectedLevels);
            int[] expectedRunLimits = getRunLimits(runCount, expectedLevels);
            int[] expectedRunStarts = getRunStarts(runCount, expectedLevels);
            int[] actualRunLevels = new int[runCount];
            int[] actualRunLimits = new int[runCount];
            int[] actualRunStarts = new int[runCount];
            for (int k = 0; k < runCount; k++) {
                actualRunLevels[k] = bidi.getRunLevel(k);
                actualRunLimits[k] = bidi.getRunLimit(k);
                actualRunStarts[k] = bidi.getRunStart(k);
            }
            checkResult("getRunLevel()", expectedRunLevels, actualRunLevels);
            checkResult("getRunStart()", expectedRunStarts, actualRunStarts);
            checkResult("getRunLimit()", expectedRunLimits, actualRunLimits);
        }
        boolean expectedBoolean = expectedIsLTR;
        actualBoolean = bidi.isLeftToRight();
        checkResult("isLeftToRight()", expectedBoolean, actualBoolean);
        expectedBoolean = !(expectedIsLTR || expectedIsRTL);
        actualBoolean = bidi.isMixed();
        checkResult("isMixed()", expectedBoolean, actualBoolean);
        expectedBoolean = expectedIsRTL;
        actualBoolean = bidi.isRightToLeft();
        checkResult("isRightToLeft()", expectedBoolean, actualBoolean);
    }
    private int getRunCount(String levels) {
        int len = levels.length();
        char c = levels.charAt(0);
        int runCount = 1;
        for (int index = 1; index < len; index++) {
            if (levels.charAt(index) != c) {
                runCount++;
                c = levels.charAt(index);
            }
        }
        return runCount;
    }
    private int[] getRunLevels_int(int runCount, String levels) {
        int[] array = new int[runCount];
        int len = levels.length();
        char c = levels.charAt(0);
        int i = 0;
        array[i++] = c - '0';
        for (int index = 1; index < len; index++) {
            if (levels.charAt(index) != c) {
                c = levels.charAt(index);
                array[i++] = c - '0';
            }
        }
        return array;
    }
    private byte[] getRunLevels_byte(int runCount, String levels) {
        byte[] array = new byte[runCount];
        int len = levels.length();
        char c = levels.charAt(0);
        int i = 0;
        array[i++] = (byte)(c - '0');
        for (int index = 1; index < len; index++) {
            if (levels.charAt(index) != c) {
                c = levels.charAt(index);
                array[i++] = (byte)(c - '0');
            }
        }
        return array;
    }
    private int[] getRunLimits(int runCount, String levels) {
        int[] array = new int[runCount];
        int len = levels.length();
        char c = levels.charAt(0);
        int i = 0;
        for (int index = 1; index < len; index++) {
            if (levels.charAt(index) != c) {
                c = levels.charAt(index);
                array[i++] = index;
            }
        }
        array[i] = len;
        return array;
    }
    private int[] getRunStarts(int runCount, String levels) {
        int[] array = new int[runCount];
        int len = levels.length();
        char c = levels.charAt(0);
        int i = 1;
        for (int index = 1; index < len; index++) {
            if (levels.charAt(index) != c) {
                c = levels.charAt(index);
                array[i++] = index;
            }
        }
        return array;
    }
    private String[] getObjects(int runCount, String text, String levels) {
        String[] array = new String[runCount];
        int[] runLimits = getRunLimits(runCount, levels);
        int runStart = 0;
        for (int i = 0; i < runCount; i++) {
            array[i] = text.substring(runStart, runLimits[i]);
            runStart = runLimits[i];
        }
        return array;
    }
    private void testMethod_createLineBidi1() {
        System.out.println("*** Test createLineBidi() 1");
        String str = " ABC 123. " + HebrewABC + " " + NKo123 + ". ABC 123";
        int lineStart = str.indexOf('.') + 2;
        int lineLimit = str.lastIndexOf('.') + 2;
        Bidi bidi = new Bidi(str, FLAGS[0]);
        Bidi lineBidi = bidi.createLineBidi(lineStart, lineLimit);
        checkResult("getBaseLevel()",
            bidi.getBaseLevel(), lineBidi.getBaseLevel());
        checkResult("getLevelAt(5)",
            bidi.getLevelAt(lineStart+5), lineBidi.getLevelAt(5));
    }
    private void testMethod_createLineBidi2() {
        System.out.println("*** Test createLineBidi() 2");
        Bidi bidi = new Bidi(data4Constructor1[0][0], FLAGS[0]);
        int len = data4Constructor1[0][0].length();
        try {
            Bidi lineBidi = bidi.createLineBidi(0, len);
        }
        catch (Exception e) {
            errorHandling("createLineBidi(0, textLength)" +
                " should not throw an exception.");
        }
        try {
            Bidi lineBidi = bidi.createLineBidi(-1, len);
            errorHandling("createLineBidi(-1, textLength)" +
                " should throw an IAE.");
        }
        catch (IllegalArgumentException e) {
        }
        try {
            Bidi lineBidi = bidi.createLineBidi(0, len+1);
            errorHandling("createLineBidi(0, textLength+1)" +
                " should throw an IAE.");
        }
        catch (IllegalArgumentException e) {
        }
    }
    private void testMethod_getLevelAt() {
        System.out.println("*** Test getLevelAt()");
        Bidi bidi = new Bidi(data4Constructor1[1][0], FLAGS[0]);
        int len = data4Constructor1[1][0].length();
        try {
            int level = bidi.getLevelAt(-1);
            if (level != bidi.getBaseLevel()) {
                errorHandling("getLevelAt(-1) returned a wrong level." +
                    " Expected=" + bidi.getBaseLevel() + ", got=" + level);
            }
        }
        catch (Exception e) {
            errorHandling("getLevelAt(-1) should not throw an exception.");
        }
        try {
            int level = bidi.getLevelAt(len+1);
            if (level != bidi.getBaseLevel()) {
                errorHandling("getLevelAt(textLength+1)" +
                    " returned a wrong level." +
                    " Expected=" + bidi.getBaseLevel() + ", got=" + level);
            }
        }
        catch (Exception e) {
            errorHandling("getLevelAt(-1) should not throw an exception.");
        }
    }
    private void testMethod_getRunLevel() {
        System.out.println("*** Test getRunLevel()");
        String str = "ABC 123";
        int length = str.length();
        Bidi bidi = new Bidi(str, Bidi.DIRECTION_LEFT_TO_RIGHT);
        try {
            if (bidi.getRunLevel(-1) != 0 ||  
                bidi.getRunLevel(0) != 0 ||   
                bidi.getRunLevel(1) != 0 ||   
                bidi.getRunLevel(2) != 0) {   
                errorHandling("getRunLevel() should return 0" +
                    " when getRunCount() is 1.");
            }
        }
        catch (Exception e) {
            errorHandling("getRunLevel() should not throw an exception " +
                "when getRunCount() is 1.");
        }
        str = "ABC " + HebrewABC + " 123";
        length = str.length();
        bidi = new Bidi(str, Bidi.DIRECTION_LEFT_TO_RIGHT);
        try {
            bidi.getRunLevel(-1);
            errorHandling("getRunLevel() should throw an AIOoBE " +
                "when run is -1(too small).");
        }
        catch (ArrayIndexOutOfBoundsException e) {
        }
        catch (IllegalArgumentException e) {
            errorHandling("getRunLevel() should not throw an IAE " +
                "but an AIOoBE when run is -1(too small).");
        }
        try {
            bidi.getRunLevel(0);
            bidi.getRunLevel(1);
            bidi.getRunLevel(2);
        }
        catch (Exception e) {
            errorHandling("getRunLevel() should not throw an exception" +
                " when run is from 0 to 2(runCount-1).");
        }
        try {
            bidi.getRunLevel(3);
            errorHandling("getRunLevel() should throw an AIOoBE" +
                " when run is 3(same as runCount).");
        }
        catch (ArrayIndexOutOfBoundsException e) {
        }
        catch (IllegalArgumentException e) {
            errorHandling("getRunLevel() should not throw an IAE " +
                "but an AIOoBE when run is 3(same as runCount).");
        }
    }
    private void testMethod_getRunLimit() {
        System.out.println("*** Test getRunLimit()");
        String str = "ABC 123";
        int length = str.length();
        Bidi bidi = new Bidi(str, Bidi.DIRECTION_LEFT_TO_RIGHT);
        try {
            if (bidi.getRunLimit(-1) != length ||  
                bidi.getRunLimit(0) != length ||   
                bidi.getRunLimit(1) != length ||   
                bidi.getRunLimit(2) != length) {   
                errorHandling("getRunLimit() should return " + length +
                    " when getRunCount() is 1.");
            }
        }
        catch (Exception e) {
            errorHandling("getRunLimit() should not throw an exception " +
                "when getRunCount() is 1.");
        }
        str = "ABC " + ArabicABC + " 123";
        length = str.length();
        bidi = new Bidi(str, Bidi.DIRECTION_LEFT_TO_RIGHT);
        try {
            bidi.getRunLimit(-1);
            errorHandling("getRunLimit() should throw an AIOoBE " +
                "when run is -1(too small).");
        }
        catch (ArrayIndexOutOfBoundsException e) {
        }
        catch (IllegalArgumentException e) {
            errorHandling("getRunLimit() should not throw an IAE " +
                "but an AIOoBE when run is -1(too small).");
        }
        try {
            bidi.getRunLimit(0);
            bidi.getRunLimit(1);
            bidi.getRunLimit(2);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            errorHandling("getRunLimit() should not throw an AIOOBE " +
                "when run is from 0 to 2(runCount-1).");
        }
        try {
            bidi.getRunLimit(3);
            errorHandling("getRunLimit() should throw an AIOoBE " +
                "when run is 3(same as runCount).");
        }
        catch (ArrayIndexOutOfBoundsException e) {
        }
        catch (IllegalArgumentException e) {
            errorHandling("getRunLimit() should not throw an IAE " +
                "but an AIOoBE when run is 3(same as runCount).");
        }
    }
    private void testMethod_getRunStart() {
        System.out.println("*** Test getRunStart()");
        String str = "ABC 123";
        int length = str.length();
        Bidi bidi = new Bidi(str, Bidi.DIRECTION_LEFT_TO_RIGHT);
        try {
            if (bidi.getRunStart(-1) != 0 ||  
                bidi.getRunStart(0) != 0 ||   
                bidi.getRunStart(1) != 0 ||   
                bidi.getRunStart(2) != 0) {   
                errorHandling("getRunStart() should return 0" +
                    " when getRunCount() is 1.");
            }
        }
        catch (Exception e) {
            errorHandling("getRunLimit() should not throw an exception" +
                " when getRunCount() is 1.");
        }
        str = "ABC " + NKoABC + " 123";
        length = str.length();
        bidi = new Bidi(str, Bidi.DIRECTION_LEFT_TO_RIGHT);
        try {
            bidi.getRunStart(-1);
            errorHandling("getRunStart() should throw an AIOoBE" +
                " when run is -1(too small).");
        }
        catch (ArrayIndexOutOfBoundsException e) {
        }
        catch (IllegalArgumentException e) {
            errorHandling("getRunStart() should not throw an IAE " +
                "but an AIOoBE when run is -1(too small).");
        }
        try {
            bidi.getRunStart(0);
            bidi.getRunStart(1);
            bidi.getRunStart(2);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            errorHandling("getRunStart() should not throw an AIOOBE " +
                "when run is from 0 to 2(runCount-1).");
        }
        try {
            if (bidi.getRunStart(3) != length) {
                errorHandling("getRunStart() should return " + length +
                    " when run is 3(same as runCount).");
            }
        }
        catch (Exception e) {
            errorHandling("getRunStart() should not throw an exception " +
                "when run is 3(same as runCount).");
        }
        try {
            bidi.getRunStart(4);
            errorHandling("getRunStart() should throw an AIOoBE " +
                "when run is runCount+1(too large).");
        }
        catch (ArrayIndexOutOfBoundsException e) {
        }
        catch (IllegalArgumentException e) {
            errorHandling("getRunStart() should not throw an IAE " +
                "but an AIOoBE when run is runCount+1(too large).");
        }
    }
    private void testMethod_reorderVisually1() {
        System.out.println("*** Test reorderVisually() 1");
        for (int textNo = 0; textNo < data4reorderVisually.length; textNo++) {
            Object[] objects = data4reorderVisually[textNo][0];
            byte[] levels = getLevels(data4reorderVisually[textNo]);
            Object[] expectedObjects = data4reorderVisually[textNo][2];
            Bidi.reorderVisually(levels, 0, objects, 0, objects.length);
            checkResult("textNo=" + textNo + ": reorderVisually(levels=[" +
                toString(levels) + "], objects=[" + toString(objects) + "])",
                expectedObjects, objects);
        }
    }
    private void testMethod_reorderVisually2() {
        System.out.println("*** Test reorderVisually() 2");
        Object[] objects = data4reorderVisually[0][0];
        byte[] levels = getLevels(data4reorderVisually[0]);
        int count = objects.length;
        int llen = levels.length;
        int olen = objects.length;
        try {
            Bidi.reorderVisually(null, 0, objects, 0, count);
            errorHandling("reorderVisually() should throw a NPE " +
                "when levels is null.");
        }
        catch (NullPointerException e) {
        }
        try {
            Bidi.reorderVisually(levels, -1, objects, 0, count);
            errorHandling("reorderVisually() should throw an IAE " +
                "when levelStart is -1.");
        }
        catch (IllegalArgumentException e) {
        }
        catch (ArrayIndexOutOfBoundsException e) {
            errorHandling("reorderVisually() should not throw an AIOoBE " +
                "but an IAE when levelStart is -1.");
        }
        try {
            Bidi.reorderVisually(levels, llen, objects, 0, count);
            errorHandling("reorderVisually() should throw an IAE " +
                "when levelStart is 6(levels.length).");
        }
        catch (IllegalArgumentException e) {
        }
        catch (ArrayIndexOutOfBoundsException e) {
            errorHandling("reorderVisually() should not throw an AIOoBE " +
                "but an IAE when levelStart is 6(levels.length).");
        }
        try {
            Bidi.reorderVisually(levels, 0, null, 0, count);
            errorHandling("reorderVisually() should throw a NPE" +
                " when objects is null.");
        }
        catch (NullPointerException e) {
        }
        try {
            Bidi.reorderVisually(levels, 0, objects, -1, count);
            errorHandling("reorderVisually() should throw an IAE" +
                " when objectStart is -1.");
        }
        catch (IllegalArgumentException e) {
        }
        catch (ArrayIndexOutOfBoundsException e) {
            errorHandling("reorderVisually() should not throw an AIOoBE " +
                "but an IAE when objectStart is -1.");
        }
        try {
            Bidi.reorderVisually(levels, 0, objects, 6, objects.length);
            errorHandling("reorderVisually() should throw an IAE " +
                "when objectStart is 6(objects.length).");
        }
        catch (IllegalArgumentException e) {
        }
        try {
            Bidi.reorderVisually(levels, 0, objects, 0, -1);
            errorHandling("reorderVisually() should throw an IAE " +
                "when count is -1.");
        }
        catch (IllegalArgumentException e) {
        }
        catch (NegativeArraySizeException e) {
            errorHandling("reorderVisually() should not throw an NASE " +
                "but an IAE when count is -1.");
        }
        try {
            Bidi.reorderVisually(levels, 0, objects, 0, count+1);
            errorHandling("reorderVisually() should throw an IAE " +
                "when count is 7(objects.length+1).");
        }
        catch (IllegalArgumentException e) {
        }
        catch (ArrayIndexOutOfBoundsException e) {
            errorHandling("reorderVisually() should not throw an AIOoBE " +
                "but an IAE when count is 7(objects.length+1).");
        }
        try {
            Bidi.reorderVisually(levels, 0, objects, 0, 0);
            checkResult("reorderVisually(count=0)",
                data4reorderVisually[0][0], objects);
        }
        catch (Exception e) {
            errorHandling("reorderVisually() should not throw an exception" +
                " when count is 0.");
        }
    }
    private void testMethod_requiresBidi() {
        System.out.println("*** Test requiresBidi()");
        String paragraph;
        char[] text;
        Bidi bidi;
        for (int textNo = 0; textNo < data4Constructor2.length; textNo++) {
            paragraph = data4Constructor2[textNo][0];
            text = paragraph.toCharArray();
            boolean rBidi = Bidi.requiresBidi(text, 0, text.length);
            if (rBidi != requiresBidi4Constructor2[textNo]) {
                error = true;
                System.err.println("Unexpected requiresBidi() value" +
                    " for requiresBidi(\"" + paragraph + "\", " + 0 + ", " +
                    text.length + ")." +
                    "\n    Expected: " + requiresBidi4Constructor2[textNo] +
                    "\n    Got     : " + rBidi);
            } else if (verbose) {
                System.out.println("  Okay : requiresBidi() for" +
                    " requiresBidi(\"" + paragraph + "\", " + 0 + ", " +
                    text.length + ")  Got: " + rBidi);
            }
        }
        char[] txt = {'A', 'B', 'C', 'D', 'E'};
        int textLength = txt.length;
        try {
            Bidi.requiresBidi(txt, -1, textLength);
            errorHandling("requiresBidi() should throw an IAE" +
                " when start is -1(too small).");
        }
        catch (IllegalArgumentException e) {
        }
        catch (ArrayIndexOutOfBoundsException e) {
            errorHandling("requiresBidi() should not throw an AIOoBE " +
                "but an IAE when start is -1(too small).");
        }
        try {
            Bidi.requiresBidi(txt, textLength, textLength);
        }
        catch (Exception e) {
            errorHandling("requiresBidi() should not throw an exception " +
                "when start is textLength.");
        }
        try {
            Bidi.requiresBidi(txt, textLength+1, textLength);
            errorHandling("requiresBidi() should throw an IAE" +
                " when start is textLength+1(too large).");
        }
        catch (IllegalArgumentException e) {
        }
        try {
            Bidi.requiresBidi(txt, 0, -1);
            errorHandling("requiresBidi() should throw an IAE" +
                " when limit is -1(too small).");
        }
        catch (IllegalArgumentException e) {
        }
        try {
            Bidi.requiresBidi(txt, 0, textLength+1);
            errorHandling("requiresBidi() should throw an IAE" +
                " when limit is textLength+1(too large).");
        }
        catch (IllegalArgumentException e) {
        }
        catch (ArrayIndexOutOfBoundsException e) {
            errorHandling("requiresBidi() should not throw an AIOoBE " +
                "but an IAE when limit is textLength+1(too large).");
        }
    }
    private void checkResult(String name,
                             int expectedValue,
                             int actualValue) {
        if (expectedValue != actualValue) {
            errorHandling("Unexpected " + name + " value." +
                " Expected: " + expectedValue + " Got: " + actualValue);
        } else if (verbose) {
            System.out.println("  Okay : " + name + " = " + actualValue);
        }
    }
    private void checkResult(String name,
                             boolean expectedValue,
                             boolean actualValue) {
        if (expectedValue != actualValue) {
            errorHandling("Unexpected " + name + " value." +
                " Expected: " + expectedValue + " Got: " + actualValue);
        } else if (verbose) {
            System.out.println("  Okay : " + name + " = " + actualValue);
        }
    }
    private void checkResult(String name,
                             String expectedValue,
                             String actualValue) {
        if (!expectedValue.equals(actualValue)) {
            errorHandling("Unexpected " + name + " value." +
                "\n\tExpected: \"" + expectedValue + "\"" +
                "\n\tGot:      \"" + actualValue + "\"");
        } else if (verbose) {
            System.out.println("  Okay : " + name + " = \"" +
                actualValue + "\"");
        }
    }
    private void checkResult(String name,
                             int[] expectedValues,
                             int[] actualValues) {
        if (!Arrays.equals(expectedValues, actualValues)) {
            errorHandling("Unexpected " + name + " value." +
                "\n\tExpected: " + toString(expectedValues) + "" +
                "\n\tGot:      " + toString(actualValues) + "");
        } else if (verbose) {
            System.out.println("  Okay : " + name + " = " +
                toString(actualValues));
        }
    }
    private void checkResult(String name,
                             Object[] expectedValues,
                             Object[] actualValues) {
        if (!Arrays.equals(expectedValues, actualValues)) {
            errorHandling("Unexpected " + name + " value." +
                "\n\tExpected: [" + toString(expectedValues) +
                "]\n\tGot:      [" + toString(actualValues) + "]");
        } else if (verbose) {
            System.out.println("  Okay : " + name + " Reordered objects = [" +
                toString(actualValues) + "]");
        }
    }
    private void errorHandling(String msg) {
        if (abort) {
            throw new RuntimeException("Error: " + msg);
        } else {
            error = true;
            System.err.println("**Error:" + msg);
        }
    }
    private String toString(int[] values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length-1; i++) {
            sb.append((int)values[i]);
            sb.append(' ');
        }
        sb.append((int)values[values.length-1]);
        return sb.toString();
    }
    private String toString(byte[] values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length-1; i++) {
            sb.append((byte)values[i]);
            sb.append(' ');
        }
        sb.append((byte)values[values.length-1]);
        return sb.toString();
    }
    private String toString(Object[] values) {
        StringBuilder sb = new StringBuilder();
        String name;
        for (int i = 0; i < values.length-1; i++) {
            if ((name = getStringName((String)values[i])) != null) {
                sb.append(name);
                sb.append(", ");
            } else {
                sb.append('"');
                sb.append((String)values[i]);
                sb.append("\", ");
            }
        }
        if ((name = getStringName((String)values[values.length-1])) != null) {
            sb.append(name);
        } else {
            sb.append('"');
            sb.append((String)values[values.length-1]);
            sb.append('\"');
        }
        return sb.toString();
    }
    private String getStringName(String str) {
        if (ArabicABC.equals(str)) return "ArabicABC";
        else if (Arabic123.equals(str)) return "Arabic123";
        else if (PArabicABC.equals(str)) return "ArabicABC(Presentation form)";
        else if (HebrewABC.equals(str)) return "HebrewABC";
        else if (KharoshthiABC.equals(str)) return "KharoshthiABC(RTL)";
        else if (Kharoshthi123.equals(str)) return "Kharoshthi123(RTL)";
        else if (NKoABC.equals(str)) return "NKoABC(RTL)";
        else if (NKo123.equals(str)) return "NKo123(RTL)";
        else if (OsmanyaABC.equals(str)) return "OsmanyaABC(LTR)";
        else if (Osmanya123.equals(str)) return "Osmanya123(LTR)";
        else return null;
    }
    private String getFlagName(int flag) {
        if (flag == -2 || flag == 0x7e) return FLAGNAMES[0];
        else if (flag == -1 || flag == 0x7f) return FLAGNAMES[1];
        else if (flag == 0) return FLAGNAMES[2];
        else if (flag == 1) return FLAGNAMES[3];
        else return "Unknown(0x" + Integer.toHexString(flag) + ")";
    }
    private String toReadableString(String str) {
         String s = str;
         s = s.replaceAll(ArabicABC, "ArabicABC");
         s = s.replaceAll(Arabic123, "Arabic123");
         s = s.replaceAll(PArabicABC, "ArabicABC(Presentation form)");
         s = s.replaceAll(HebrewABC, "HebrewABC");
         s = s.replaceAll(KharoshthiABC, "KharoshthiABC");
         s = s.replaceAll(Kharoshthi123, "Kharoshthi123");
         s = s.replaceAll(NKoABC, "NKoABC");
         s = s.replaceAll(NKo123, "NKo123");
         s = s.replaceAll(OsmanyaABC, "OsmanyaABC");
         s = s.replaceAll(Osmanya123, "Osmanya123");
         return s;
    }
    private  byte[] getLevels(Object[][] data) {
        int levelLength = data[0].length;
        byte[] array = new byte[levelLength];
        int textIndex = 0;
        for (int i = 0; i < levelLength; i++) {
            array[i] = (byte)(((String)data[1][0]).charAt(textIndex) - '0');
            textIndex += ((String)data[0][i]).length();
        }
        return array;
    }
    private static final int[] FLAGS = {
        Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT,  
        Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT,  
        Bidi.DIRECTION_LEFT_TO_RIGHT,          
        Bidi.DIRECTION_RIGHT_TO_LEFT           
    };
    private static final String[] FLAGNAMES = {
        "DIRECTION_DEFAULT_LEFT_TO_RIGHT",  
        "DIRECTION_DEFAULT_RIGHT_TO_LEFT",  
        "DIRECTION_LEFT_TO_RIGHT",          
        "DIRECTION_RIGHT_TO_LEFT",          
    };
    private static final char L   = '\u200E';
    private static final char R   = '\u202F';
    private static final char LRE = '\u202A';
    private static final char RLE = '\u202B';
    private static final char PDF = '\u202C';
    private static final char LRO = '\u202D';
    private static final char RLO = '\u202E';
    private static String ArabicABC = "\u0627\u0628\u0629";
    private static String Arabic123 = "\u0661\u0662\u0663";
    private static String PArabicABC = "\uFE97\uFE92\uFE8E";
    private static String HebrewABC = "\u05D0\u05D1\u05D2";
    private static String KharoshthiABC =
        new String(Character.toChars(0x10A10)) +
        new String(Character.toChars(0x10A11)) +
        new String(Character.toChars(0x10A12));
    private static String Kharoshthi123 =
        new String(Character.toChars(0x10A40)) +
        new String(Character.toChars(0x10A41)) +
        new String(Character.toChars(0x10A42));
    private static String NKoABC = "\u07CA\u07CB\u07CC";
    private static String NKo123 = "\u07C1\u07C2\u07C3";
    private static String OsmanyaABC =
        new String(Character.toChars(0x10480)) +
        new String(Character.toChars(0x10481)) +
        new String(Character.toChars(0x10482));
    private static String Osmanya123 =
        new String(Character.toChars(0x104A0)) +
        new String(Character.toChars(0x104A1)) +
        new String(Character.toChars(0x104A2));
    private static String[][] data4Constructor1 = {
        {"abc <ABC XYZ> xyz.",
             "000000000000000000", "000002222222000000", "000000000000000000",
             "000003333333000000", "000000000000000000",
             "222222222222222221", "222222222222222221", "222222222222222221",
             "222113333333112221", "222224444444222221",
             "000000000000000000", "000000000000000000", "222222222222222221"},
        {"ABC <" + HebrewABC + " " + NKo123 + "> XYZ.",
             "000001111111000000", "000001111111000000", "000003333333000000",
             "000003333333000000", "000000000000000000",
             "222111111111112221", "222111111111112221", "222223333333222221",
             "222113333333112221", "222224444444222221",
             "000001111111000000", "000001111111000000", "222111111111112221"},
        {NKoABC + " <ABC XYZ> " + NKo123 + ".",
             "111000000000001110", "111112222222111110", "111002222222001110",
             "111113333333111110", "111004444444001110",
             "111112222222111111", "111112222222111111", "111112222222111111",
             "111111111111111111", "111114444444111111",
             "111112222222111111", "111000000000001110", "111112222222111111"},
        {HebrewABC + " <" + ArabicABC + " " + Arabic123 + "> " + NKo123 + ".",
             "111111111222111110", "111111111222111110", "111003333444001110",
             "111113333333111110", "111004444444001110",
             "111111111222111111", "111111111222111111", "111113333444111111",
             "111111111111111111", "111114444444111111",
             "111111111222111111", "111111111222111110", "111111111222111111"},
        {"abc <" + NKoABC + " 123> xyz.",
             "000001111222000000", "000001111222000000", "000003333444000000",
             "000003333333000000", "000000000000000000",
             "222111111222112221", "222111111222112221", "222223333444222221",
             "222113333333112221", "222224444444222221",
             "000001111222000000", "000001111222000000", "222111111222112221"},
        {"abc <ABC " + NKo123 + "> xyz.",
             "000000000111000000", "000002221111000000", "000002222333000000",
             "000003333333000000", "000000000000000000",
             "222222221111112221", "222222221111112221", "222222222333222221",
             "222113333333112221", "222224444444222221",
             "000000000111000000", "000000000111000000", "222222221111112221"},
        {ArabicABC + " <" + NKoABC + " 123" + "> " + Arabic123 + ".",
             "111111111222112220", "111111111222112220", "111003333444002220",
             "111113333333112220", "111004444444002220",
             "111111111222112221", "111111111222112221", "111113333444112221",
             "111113333333112221", "111114444444112221",
             "111111111222112221", "111111111222112220", "111111111222112221"},
        {ArabicABC + " <XYZ " + NKoABC + "> " + Arabic123 + ".",
             "111000000111112220", "111112221111112220", "111002222333002220",
             "111113333333112220", "111004444444002220",
             "111112221111112221", "111112221111112221", "111112222333112221",
             "111113333333112221", "111114444444112221",
             "111112221111112221", "111000000111112220", "111112221111112221"},
        {OsmanyaABC + " <" + KharoshthiABC + " " + Kharoshthi123 + "> " +
         Osmanya123 + ".",
             "000000001111111111111000000000", "000000001111111111111000000000",
             "000000003333333333333000000000", "000000003333333333333000000000",
             "000000000000000000000000000000",
             "222222111111111111111112222221", "222222111111111111111112222221",
             "222222223333333333333222222221", "222222113333333333333112222221",
             "222222224444444444444222222221",
             "000000001111111111111000000000", "000000001111111111111000000000",
             "222222111111111111111112222221"},
        {KharoshthiABC + " <" + OsmanyaABC + " " + Osmanya123 + "> " +
         Kharoshthi123 + ".",
             "111111000000000000000001111110", "111111112222222222222111111110",
             "111111002222222222222001111110", "111111113333333333333111111110",
             "111111004444444444444001111110",
             "111111112222222222222111111111", "111111112222222222222111111111",
             "111111112222222222222111111111", "111111111111111111111111111111",
             "111111114444444444444111111111",
             "111111112222222222222111111111", "111111000000000000000001111110",
             "111111112222222222222111111111"},
    };
    private static boolean[][] baseIsLTR4Constructor1 = {
        {true,  true,  true,  true,  true,
         false, false, false, false, false,
         true,  true,  false},
        {true,  true,  true,  true,  true,
         false, false, false, false, false,
         true,  true,  false},
        {true,  true,  true,  true,  true,
         false, false, false, false, false,
         false, true,  false},
        {true,  true,  true,  true,  true,
         false, false, false, false, false,
         false, true,  false},
        {true,  true,  true,  true,  true,
         false, false, false, false, false,
         true,  true,  false},
        {true,  true,  true,  true,  true,
         false, false, false, false, false,
         true,  true,  false},
        {true,  true,  true,  true,  true,
         false, false, false, false, false,
         false, true,  false},
        {true,  true,  true,  true,  true,
         false, false, false, false, false,
         false, true,  false},
        {true,  true,  true,  true,  true,
         false, false, false, false, false,
         true,  true,  false},
        {true,  true,  true,  true,  true,
         false, false, false, false, false,
         false, true,  false},
    };
    private static boolean[][][] isLTR_isRTL4Constructor1 = {
        {{true,  false, true,  false, true,
          false, false, false, false, false,
          true,  true,  false},
         {false, false, false, false, false,
          false, false, false, false, false,
          false, false, false}},
        {{false, false, false, false, true,
          false, false, false, false, false,
          false, false, false},
         {false, false, false, false, false,
          false, false, false, false, false,
          false, false, false}},
        {{false, false, false, false, false,
          false, false, false, false, false,
          false, false, false},
         {false, false, false, false, false,
          false, false, false, true,  false,
          false, false, false}},
        {{false, false, false, false, false,
          false, false, false, false, false,
          false, false, false},
         {false, false, false, false, false,
          false, false, false, true,  false,
          false, false, false}},
        {{false, false, false, false, true,
          false, false, false, false, false,
          false, false, false},
         {false, false, false, false, false,
          false, false, false, false, false,
          false, false, false}},
        {{false, false, false, false, true,
          false, false, false, false, false,
          false, false, false},
         {false, false, false, false, false,
          false, false, false, false, false,
          false, false, false}},
        {{false, false, false, false, false,
          false, false, false, false, false,
          false, false, false},
         {false, false, false, false, false,
          false, false, false, false, false,
          false, false, false}},
        {{false, false, false, false, false,
          false, false, false, false, false,
          false, false, false},
         {false, false, false, false, false,
          false, false, false, false, false,
          false, false, false}},
        {{false, false, false, false, true,
          false, false, false, false, false,
          false, false, false},
         {false, false, false, false, false,
          false, false, false, false, false,
          false, false, false}},
        {{false, false, false, false, false,
          false, false, false, false, false,
          false, false, false},
         {false, false, false, false, false,
          false, false, false, true,  false,
          false, false, false}},
    };
    private static String[][] data4Constructor2 = {
        {" ABC 123.",
             "000000000", "000000000", "000000000", "122222221"},
        {" ABC " + HebrewABC + " " + NKo123 + " 123.",
             "00000111111112220", "00000111111112220", "00000111111112220",
             "12221111111112221"},
        {" ABC " + ArabicABC + " " + Arabic123 + " 123.",
             "00000111122212220", "00000111122212220", "00000111122212220",
             "12221111122212221"},
        {" " + NKoABC + " ABC 123 " + NKo123 + ".",
             "11111222222211111", "11111222222211111", "01110000000001110",
             "11111222222211111"},
        {" " + ArabicABC + " ABC 123 " + Arabic123 + ".",
             "11111222222212221", "11111222222212221", "01110000000002220",
             "11111222222212221"},
        {" " + HebrewABC + " " + NKo123 + ".",
             "111111111", "111111111", "011111110", "111111111"},
        {" " + ArabicABC + " " + Arabic123 + ".",
             "111112221", "111112221", "011112220", "111112221"},
        {" " + KharoshthiABC + " " + Kharoshthi123 + ".",
             "111111111111111", "111111111111111", "011111111111110",
             "111111111111111"},
        {L + HebrewABC + " " + NKo123 + ".",
             "011111110", "011111110", "011111110", "211111111"},
        {R + "ABC " + Osmanya123 + ".",
             "000000000000", "000000000000", "000000000000", "122222222221"},
        {"ABC " + PArabicABC + " " + PArabicABC + " 123",
             "000011111111222", "000011111111222", "000011111111222",
             "222111111111222"},
        {RLE + "ABC " + HebrewABC + " " + NKo123 + "." + PDF,
             "22221111111110", "22221111111110", "22221111111110",
             "44443333333331"},
        {"He said \"" + RLE + "ABC " + HebrewABC + " " + NKo123 + PDF + ".\"",
             "000000000222211111111000", "000000000222211111111000",
             "000000000222211111111000", "222222211444433333333111"},
        {LRO + "He said \"" + RLE + "ABC " + NKoABC + " " + NKo123 + PDF +
         ".\"" + PDF,
             "22222222224444333333332220", "22222222224444333333332220",
             "22222222224444333333332220", "22222222224444333333332221"},
        {LRO + "He said \"" + RLE + "ABC " + HebrewABC + " " + NKo123 + PDF +
         ".\"",  
             "2222222222444433333333222", "2222222222444433333333222",
             "2222222222444433333333222", "2222222222444433333333222"},
        {"Did you say '" + LRE + "he said \"" + RLE + "ABC " + HebrewABC +
         " " + NKo123 + PDF + "\"" + PDF + "'?",
             "0000000000000222222222244443333333322000",
             "0000000000000222222222244443333333322000",
             "0000000000000222222222244443333333322000",
             "2222222222222222222222244443333333322111"},
        {RLO + "Did you say '" + LRE + "he said \"" + RLE + "ABC " +
         HebrewABC + " " + NKo123 + PDF + "\"" + PDF + "'?" + PDF,
             "111111111111112222222222444433333333221110",
             "111111111111112222222222444433333333221110",
             "111111111111112222222222444433333333221110",
             "333333333333334444444444666655555555443331"},
        {RLO + "Did you say '" + LRE + "he said \"" + RLE + "ABC " +
         HebrewABC + " " + NKo123 + PDF + "\"" + PDF + "'?",  
             "11111111111111222222222244443333333322111",
             "11111111111111222222222244443333333322111",
             "11111111111111222222222244443333333322111",
             "33333333333333444444444466665555555544333"},
        {" ABC (" + ArabicABC + " " + Arabic123 + ") 123.",
             "0000001111222112220", "0000001111222112220",
             "0000001111222112220", "1222111111222112221"},
        {" " + HebrewABC + " (ABC 123) " + NKo123 + ".",
             "1111112222222111111", "1111112222222111111",
             "0111000000000001110", "1111112222222111111"},
        {" He said \"" + RLE + "ABC " + NKoABC + " " + NKo123 + PDF + ".\" ",
             "00000000002222111111110000", "00000000002222111111110000",
             "00000000002222111111110000", "12222222114444333333331111"},
        {" Did you say '" + LRE + "he said \"" + RLE + "ABC " + HebrewABC +
         " " + NKo123 + PDF + "\"" + PDF + "'? ",
             "000000000000002222222222444433333333220000",
             "000000000000002222222222444433333333220000",
             "000000000000002222222222444433333333220000",
             "122222222222222222222222444433333333221111"},
        {RLE + OsmanyaABC + " " + KharoshthiABC + " " + Kharoshthi123 + "." +
         PDF,
             "22222221111111111111110", "22222221111111111111110",
             "22222221111111111111110", "44444443333333333333331"},
    };
    private static boolean[][] baseIsLTR4Constructor2 = {
        {true,  true,  true,  false},
        {true,  true,  true,  false},
        {true,  true,  true,  false},
        {false, false, true,  false},
        {false, false, true,  false},
        {false, false, true,  false},
        {false, false, true,  false},
        {false, false, true,  false},
        {true,  true,  true,  false},
        {true,  true,  true,  false},
        {true,  true,  true,  false},
        {true,  true,  true,  false},
        {true,  true,  true,  false},
        {true,  true,  true,  false},
        {true,  true,  true,  false},
        {true,  true,  true,  false},
        {true,  true,  true,  false},
        {true,  true,  true,  false},
        {true,  true,  true,  false},
        {false, false, true,  false},
        {true,  true,  true,  false},
        {true,  true,  true,  false},
        {true,  true,  true,  false},
    };
    private static boolean[][][] isLTR_isRTL4Constructor2 = {
        {{true,  true,  true,  false}, {false, false, false, false}},
        {{false, false, false, false}, {false, false, false, false}},
        {{false, false, false, false}, {false, false, false, false}},
        {{false, false, false, false}, {false, false, false, false}},
        {{false, false, false, false}, {false, false, false, false}},
        {{false, false, false, false}, {true,  true,  false, true }},
        {{false, false, false, false}, {false, false, false, false}},
        {{false, false, false, false}, {true,  true,  false, true }},
        {{false, false, false, false}, {false, false, false, false}},
        {{true,  true,  true,  false}, {false, false, false, false}},
        {{false, false, false, false}, {false, false, false, false}},
        {{false, false, false, false}, {false, false, false, false}},
        {{false, false, false, false}, {false, false, false, false}},
        {{false, false, false, false}, {false, false, false, false}},
        {{false, false, false, false}, {false, false, false, false}},
        {{false, false, false, false}, {false, false, false, false}},
        {{false, false, false, false}, {false, false, false, false}},
        {{false, false, false, false}, {false, false, false, false}},
        {{false, false, false, false}, {false, false, false, false}},
        {{false, false, false, false}, {false, false, false, false}},
        {{false, false, false, false}, {false, false, false, false}},
        {{false, false, false, false}, {false, false, false, false}},
        {{false, false, false, false}, {false, false, false, false}},
    };
    private static boolean[] requiresBidi4Constructor2 = {
        false, true,  true,  true,  true,
        true,  true,  true,  true,  false,
        true,  true,  true,  true,  true,
        true,  true,  true,  true,  true,
        true,  true,  true,
    };
    private static byte[][][] emb4Constructor3 = {
        {{0, 0, 0, 0, 0,  1,  1,  1,  1,  1,  1,  1, 0, 0, 0, 0, 0, 0},
         {0, 0, 0, 0, 0,  2,  2,  2,  2,  2,  2,  2, 0, 0, 0, 0, 0, 0},
         {0, 0, 0, 0, 0, -3, -3, -3, -3, -3, -3, -3, 0, 0, 0, 0, 0, 0},
         {0, 0, 0, 0, 0, -4, -4, -4, -4, -4, -4, -4, 0, 0, 0, 0, 0, 0}},
        {{ 0,  0,  0,  0,  0,  0,  0,  0,
           1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,
           0,  0,  0,  0,  0,  0,  0,  0,  0},
         { 0,  0,  0,  0,  0,  0,  0,  0,
           2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,
           0,  0,  0,  0,  0,  0,  0,  0,  0},
         { 0,  0,  0,  0,  0,  0,  0,  0,
          -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
           0,  0,  0,  0,  0,  0,  0,  0,  0},
         { 0,  0,  0,  0,  0,  0,  0,  0,
          -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4,
           0,  0,  0,  0,  0,  0,  0,  0,  0}},
    };
    private static String[][] data4Constructor3 = {
        {"abc <ABC XYZ> xyz.",
             "000002222222000000", "000000000000000000",
             "000003333333000000", "000000000000000000",
             "222222222222222221", "222222222222222221",
             "222113333333112221", "222224444444222221",
             "000002222222000000", "000000000000000000",
             "000003333333000000", "000000000000000000",
             "222222222222222221", "222222222222222221",
             "222113333333112221", "222224444444222221"},
        {"ABC <" + HebrewABC + " " + NKo123 + "> XYZ.",
             "000001111111000000", "000003333333000000",
             "000003333333000000", "000000000000000000",
             "222111111111112221", "222223333333222221",
             "222113333333112221", "222224444444222221",
             "000001111111000000", "000003333333000000",
             "000003333333000000", "000000000000000000",
             "222111111111112221", "222223333333222221",
             "222113333333112221", "222224444444222221"},
        {NKoABC + " <ABC XYZ> " + NKo123 + ".",
             "111112222222111111", "111112222222111111",
             "111111111111111111", "111114444444111111",
             "111112222222111111", "111112222222111111",
             "111111111111111111", "111114444444111111",
             "111112222222111110", "111002222222001110",
             "111113333333111110", "111004444444001110",
             "111112222222111111", "111112222222111111",
             "111111111111111111", "111114444444111111"},
        {HebrewABC + " <" + ArabicABC + " " + Arabic123 + "> " + NKo123 + ".",
             "111111111222111111", "111113333444111111",
             "111111111111111111", "111114444444111111",
             "111111111222111111", "111113333444111111",
             "111111111111111111", "111114444444111111",
             "111111111222111110", "111003333444001110",
             "111113333333111110", "111004444444001110",
             "111111111222111111", "111113333444111111",
             "111111111111111111", "111114444444111111"},
        {"abc <123 456> xyz.",
             "000002221222000000", "000000000000000000",
             "000003333333000000", "000000000000000000",
             "222222222222222221", "222222222222222221",
             "222113333333112221", "222224444444222221",
             "000002221222000000", "000000000000000000",
             "000003333333000000", "000000000000000000",
             "222222222222222221", "222222222222222221",
             "222113333333112221", "222224444444222221"},
        {OsmanyaABC + " <" + KharoshthiABC + " " + Kharoshthi123 + "> " +
         Osmanya123 + ".",
             "000000001111111111111000000000", "000000003333333333333000000000",
             "000000003333333333333000000000", "000000000000000000000000000000",
             "222222111111111111111112222221", "222222223333333333333222222221",
             "222222113333333333333112222221", "222222224444444444444222222221",
             "000000001111111111111000000000", "000000003333333333333000000000",
             "000000003333333333333000000000", "000000000000000000000000000000",
             "222222111111111111111112222221", "222222223333333333333222222221",
             "222222113333333333333112222221", "222222224444444444444222222221"},
        {KharoshthiABC + " <" + OsmanyaABC + " " + Osmanya123 + "> " +
         Kharoshthi123 + ".",
             "111111112222222222222111111111", "111111112222222222222111111111",
             "111111111111111111111111111111", "111111114444444444444111111111",
             "111111112222222222222111111111", "111111112222222222222111111111",
             "111111111111111111111111111111", "111111114444444444444111111111",
             "111111112222222222222111111110", "111111002222222222222001111110",
             "111111113333333333333111111110", "111111004444444444444001111110",
             "111111112222222222222111111111", "111111112222222222222111111111",
             "111111111111111111111111111111", "111111114444444444444111111111"},
    };
    private static boolean[][] baseIsLTR4Constructor3 = {
        {true,  true,  true,  true,    
         true,  true,  true,  true,    
         true,  true,  true,  true,    
         false, false, false, false},  
        {true,  true,  true,  true,
         true,  true,  true,  true,
         true,  true,  true,  true,
         false, false, false, false},
        {false, false, false, false,
         false, false, false, false,
         true,  true,  true,  true,
         false, false, false, false},
        {false, false, false, false,
         false, false, false, false,
         true,  true,  true,  true,
         false, false, false, false},
        {true,  true,  true,  true,
         true,  true,  true,  true,
         true,  true,  true,  true,
         false, false, false, false},
        {true,  true,  true,  true,
         true,  true,  true,  true,
         true,  true,  true,  true,
         false, false, false, false},
        {false, false, false, false,
         false, false, false, false,
         true,  true,  true,  true,
         false, false, false, false},
    };
    private static boolean[][][] isLTR_isRTL4Constructor3 = {
        {{false, true,  false, true,     
          false, false, false, false,    
          false, true,  false, true,     
          false, false, false, false},   
         {false, false, false, false,    
          false, false, false, false,    
          false, false, false, false,    
          false, false, false, false}},  
        {{false, false, false, true,
          false, false, false, false,
          false, false, false, true,
          false, false, false, false},
         {false, false, false, false,
          false, false, false, false,
          false, false, false, false,
          false, false, false, false}},
        {{false, false, false, false,
          false, false, false, false,
          false, false, false, false,
          false, false, false, false},
         {false, false, true,  false,
          false, false, true,  false,
          false, false, false, false,
          false, false, true,  false}},
        {{false, false, false, false,
          false, false, false, false,
          false, false, false, false,
          false, false, false, false},
         {false, false, true,  false,
          false, false, true,  false,
          false, false, false, false,
          false, false, true,  false}},
        {{false, true,  false, true,
          false, false, false, false,
          false, true,  false, true,
          false, false, false, false },
         {false, false, false, false,
          false, false, false, false,
          false, false, false, false,
          false, false, false, false}},
        {{false, false, false, true,
          false, false, false, false,
          false, false, false, true,
          false, false, false, false},
         {false, false, false, false,
          false, false, false, false,
          false, false, false, false,
          false, false, false, false}},
        {{false, false, false, false,
          false, false, false, false,
          false, false, false, false,
          false, false, false, false},
         {false, false, true,  false,
          false, false, true,  false,
          false, false, false, false,
          false, false, true,  false}},
    };
    private static Object[][][] data4reorderVisually = {
        {{"ABC", " ", "abc", " ", ArabicABC, "."},   
         {"000000001110"},                           
         {"ABC", " ", "abc", " ", ArabicABC, "."}},  
        {{"ABC", " ", HebrewABC, " ", NKoABC, "."},
         {"222111111111"},
         {".", NKoABC, " ", HebrewABC, " ", "ABC"}},
        {{OsmanyaABC, " ", HebrewABC, " ", KharoshthiABC, "."},
         {"222222111111111111"},
         {".", KharoshthiABC, " ", HebrewABC, " ", OsmanyaABC,}},
        {{"ABC", " ", Osmanya123, " ", "\"", OsmanyaABC, " ", Kharoshthi123,
          " ", KharoshthiABC, ".", "\""},
         {"0000000000002222221111111111111100"},
         {"ABC", " ", Osmanya123, " ", "\"", KharoshthiABC, " ", Kharoshthi123,
          " ", OsmanyaABC, ".", "\""}},
    };
}
