@TestTargetClass(MessageFormat.class) 
public class MessageFormatTest extends TestCase {
    private MessageFormat format1, format2, format3;
    private Locale defaultLocale;
    private void checkSerialization(MessageFormat format) {
        try {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(ba);
            out.writeObject(format);
            out.close();
            ObjectInputStream in = new ObjectInputStream(
                    new ByteArrayInputStream(ba.toByteArray()));
            MessageFormat read = (MessageFormat) in.readObject();
            assertTrue("Not equal: " + format.toPattern(), format.equals(read));
        } catch (IOException e) {
            fail("Format: " + format.toPattern() + " caused IOException: " + e);
        } catch (ClassNotFoundException e) {
            fail("Format: " + format.toPattern()
                    + " caused ClassNotFoundException: " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "IllegalArgumentException is not verified.",
        method = "MessageFormat",
        args = {java.lang.String.class, java.util.Locale.class}
    )
    public void test_ConstructorLjava_lang_StringLjava_util_Locale() {
        Locale mk = new Locale("mk", "MK");
        MessageFormat format = new MessageFormat(
                "Date: {0,date} Currency: {1, number, currency} Integer: {2, number, integer}",
                mk);
        assertTrue("Wrong locale1", format.getLocale().equals(mk));
        assertTrue("Wrong locale2", format.getFormats()[0].equals(DateFormat
                .getDateInstance(DateFormat.DEFAULT, mk)));
        assertTrue("Wrong locale3", format.getFormats()[1].equals(NumberFormat
                .getCurrencyInstance(mk)));
        assertTrue("Wrong locale4", format.getFormats()[2].equals(NumberFormat
                .getIntegerInstance(mk)));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "MessageFormat",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        MessageFormat format = new MessageFormat(
                "abc {4,time} def {3,date} ghi {2,number} jkl {1,choice,0#low|1#high} mnop {0}");
        assertTrue("Not a MessageFormat",
                format.getClass() == MessageFormat.class);
        Format[] formats = format.getFormats();
        assertNotNull("null formats", formats);
        assertTrue("Wrong format count: " + formats.length, formats.length >= 5);
        assertTrue("Wrong time format", formats[0].equals(DateFormat
                .getTimeInstance()));
        assertTrue("Wrong date format", formats[1].equals(DateFormat
                .getDateInstance()));
        assertTrue("Wrong number format", formats[2].equals(NumberFormat
                .getInstance()));
        assertTrue("Wrong choice format", formats[3].equals(new ChoiceFormat(
                "0.0#low|1.0#high")));
        assertNull("Wrong string format", formats[4]);
        Date date = new Date();
        FieldPosition pos = new FieldPosition(-1);
        StringBuffer buffer = new StringBuffer();
        format.format(new Object[] { "123", new Double(1.6), new Double(7.2),
                date, date }, buffer, pos);
        String result = buffer.toString();
        buffer.setLength(0);
        buffer.append("abc ");
        buffer.append(DateFormat.getTimeInstance().format(date));
        buffer.append(" def ");
        buffer.append(DateFormat.getDateInstance().format(date));
        buffer.append(" ghi ");
        buffer.append(NumberFormat.getInstance().format(new Double(7.2)));
        buffer.append(" jkl high mnop 123");
        assertTrue("Wrong answer:\n" + result + "\n" + buffer, result
                .equals(buffer.toString()));
        assertEquals("Simple string", "Test message", new MessageFormat(
                "Test message").format(new Object[0]));
        result = new MessageFormat("Don't").format(new Object[0]);
        assertTrue("Should not throw IllegalArgumentException: " + result,
                    "Dont".equals(result));
        try {
            new MessageFormat("Invalid {1,foobar} format descriptor!");
            fail("Expected test_ConstructorLjava_lang_String to throw IAE.");
        } catch (IllegalArgumentException ex) {
        }
        try {
            new MessageFormat(
                    "Invalid {1,date,invalid-spec} format descriptor!");
        } catch (IllegalArgumentException ex) {
        }
        checkSerialization(new MessageFormat(""));
        checkSerialization(new MessageFormat("noargs"));
        checkSerialization(new MessageFormat("{0}"));
        checkSerialization(new MessageFormat("a{0}"));
        checkSerialization(new MessageFormat("{0}b"));
        checkSerialization(new MessageFormat("a{0}b"));
        try {
            new MessageFormat("{0,number,integer");
            fail("Assert 0: Failed to detect unmatched brackets.");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "applyPattern",
        args = {java.lang.String.class}
    )
    public void test_applyPatternLjava_lang_String() {
        MessageFormat format = new MessageFormat("test");
        format.applyPattern("xx {0}");
        assertEquals("Invalid number", "xx 46", format
                .format(new Object[] { new Integer(46) }));
        Date date = new Date();
        String result = format.format(new Object[] { date });
        String expected = "xx " + DateFormat.getInstance().format(date);
        assertTrue("Invalid date:\n" + result + "\n" + expected, result
                .equals(expected));
        format = new MessageFormat("{0,date}{1,time}{2,number,integer}");
        format.applyPattern("nothing");
        assertEquals("Found formats", "nothing", format.toPattern());
        format.applyPattern("{0}");
        assertNull("Wrong format", format.getFormats()[0]);
        assertEquals("Wrong pattern", "{0}", format.toPattern());
        format.applyPattern("{0, \t\u001ftime }");
        assertTrue("Wrong time format", format.getFormats()[0]
                .equals(DateFormat.getTimeInstance()));
        assertEquals("Wrong time pattern", "{0,time}", format.toPattern());
        format.applyPattern("{0,Time, Short\n}");
        assertTrue("Wrong short time format", format.getFormats()[0]
                .equals(DateFormat.getTimeInstance(DateFormat.SHORT)));
        assertEquals("Wrong short time pattern", "{0,time,short}", format
                .toPattern());
        format.applyPattern("{0,TIME,\nmedium  }");
        assertTrue("Wrong medium time format", format.getFormats()[0]
                .equals(DateFormat.getTimeInstance(DateFormat.MEDIUM)));
        assertEquals("Wrong medium time pattern", "{0,time}", format
                .toPattern());
        format.applyPattern("{0,time,LONG}");
        assertTrue("Wrong long time format", format.getFormats()[0]
                .equals(DateFormat.getTimeInstance(DateFormat.LONG)));
        assertEquals("Wrong long time pattern", "{0,time,long}", format
                .toPattern());
        format.setLocale(Locale.FRENCH); 
        format.applyPattern("{0,time, Full}");
        assertTrue("Wrong full time format", format.getFormats()[0]
                .equals(DateFormat.getTimeInstance(DateFormat.FULL,
                        Locale.FRENCH)));
        format.setLocale(Locale.getDefault());
        format.applyPattern("{0, date}");
        assertTrue("Wrong date format", format.getFormats()[0]
                .equals(DateFormat.getDateInstance()));
        assertEquals("Wrong date pattern", "{0,date}", format.toPattern());
        format.applyPattern("{0, date, short}");
        assertTrue("Wrong short date format", format.getFormats()[0]
                .equals(DateFormat.getDateInstance(DateFormat.SHORT)));
        assertEquals("Wrong short date pattern", "{0,date,short}", format
                .toPattern());
        format.applyPattern("{0, date, medium}");
        assertTrue("Wrong medium date format", format.getFormats()[0]
                .equals(DateFormat.getDateInstance(DateFormat.MEDIUM)));
        assertEquals("Wrong medium date pattern", "{0,date}", format
                .toPattern());
        format.applyPattern("{0, date, long}");
        assertTrue("Wrong long date format", format.getFormats()[0]
                .equals(DateFormat.getDateInstance(DateFormat.LONG)));
        assertEquals("Wrong long date pattern", "{0,date,long}", format
                .toPattern());
        format.applyPattern("{0, date, full}");
        assertTrue("Wrong full date format", format.getFormats()[0]
                .equals(DateFormat.getDateInstance(DateFormat.FULL)));
        assertEquals("Wrong full date pattern", "{0,date,full}", format
                .toPattern());
        format.applyPattern("{0, date, MMM d {hh:mm:ss}}");
        assertEquals("Wrong time/date format", " MMM d {hh:mm:ss}",
                ((SimpleDateFormat) (format.getFormats()[0])).toPattern());
        assertEquals("Wrong time/date pattern", "{0,date, MMM d {hh:mm:ss}}",
                format.toPattern());
        format.applyPattern("{0, number}");
        assertTrue("Wrong number format", format.getFormats()[0]
                .equals(NumberFormat.getNumberInstance()));
        assertEquals("Wrong number pattern", "{0,number}", format.toPattern());
        format.applyPattern("{0, number, currency}");
        assertTrue("Wrong currency number format", format.getFormats()[0]
                .equals(NumberFormat.getCurrencyInstance()));
        assertEquals("Wrong currency number pattern", "{0,number,currency}",
                format.toPattern());
        format.applyPattern("{0, number, percent}");
        assertTrue("Wrong percent number format", format.getFormats()[0]
                .equals(NumberFormat.getPercentInstance()));
        assertEquals("Wrong percent number pattern", "{0,number,percent}",
                format.toPattern());
        format.applyPattern("{0, number, integer}");
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);
        nf.setParseIntegerOnly(true);
        assertTrue("Wrong integer number format", format.getFormats()[0]
                .equals(nf));
        assertEquals("Wrong integer number pattern", "{0,number,integer}",
                format.toPattern());
        format.applyPattern("{0, number, {'#'}##0.0E0}");
        format.applyPattern("{0, choice,0#no|1#one|2#{1,number}}");
        assertEquals("Wrong choice format",
        "0.0#no|1.0#one|2.0#{1,number}",
                ((ChoiceFormat) format.getFormats()[0]).toPattern());
        assertEquals("Wrong choice pattern",
                "{0,choice,0.0#no|1.0#one|2.0#{1,number}}", format.toPattern());
        assertEquals("Wrong formatted choice", "3.6", format
                .format(new Object[] { new Integer(2), new Float(3.6) }));
        try {
            format.applyPattern("WRONG MESSAGE FORMAT {0,number,{}");
            fail("Expected IllegalArgumentException for invalid pattern");
        } catch (IllegalArgumentException e) {
        }
        MessageFormat mf = new MessageFormat("{0,number,integer}");
        String badpattern = "{0,number,#";
        try {
            mf.applyPattern(badpattern);
            fail("Assert 0: Failed to detect unmatched brackets.");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "applyPattern",
        args = {java.lang.String.class}
    )
    public void test_applyPatternLjava_lang_String_AndroidFailure() {
        MessageFormat format = new MessageFormat("test");
        format.setLocale(Locale.FRENCH); 
        format.applyPattern("{0,time, Full}");
        assertEquals("Wrong full time pattern", "{0,time,full}", format
                .toPattern());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public void test_clone() {
        MessageFormat format = new MessageFormat("'{'choice'}'{0}");
        MessageFormat clone = (MessageFormat) format.clone();
        assertTrue("Clone not equal", format.equals(clone));
        assertEquals("Wrong answer", "{choice}{0}", format
                .format(new Object[] {}));
        clone.setFormat(0, DateFormat.getInstance());
        assertTrue("Clone shares format data", !format.equals(clone));
        format = (MessageFormat) clone.clone();
        Format[] formats = clone.getFormats();
        ((SimpleDateFormat) formats[0]).applyPattern("adk123");
        assertTrue("Clone shares format data", !format.equals(clone));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equalsLjava_lang_Object() {
        MessageFormat format1 = new MessageFormat("{0}");
        MessageFormat format2 = new MessageFormat("{1}");
        assertTrue("Should not be equal", !format1.equals(format2));
        format2.applyPattern("{0}");
        assertTrue("Should be equal", format1.equals(format2));
        SimpleDateFormat date = (SimpleDateFormat) DateFormat.getTimeInstance();
        format1.setFormat(0, DateFormat.getTimeInstance());
        format2.setFormat(0, new SimpleDateFormat(date.toPattern()));
        assertTrue("Should be equal2", format1.equals(format2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
        assertEquals("Should be equal", 3648, new MessageFormat("rr", null)
                .hashCode());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "formatToCharacterIterator",
        args = {java.lang.Object.class}
    )
    public void test_formatToCharacterIteratorLjava_lang_Object() {
        new Support_MessageFormat(
                "test_formatToCharacterIteratorLjava_lang_Object")
                .t_formatToCharacterIterator();
        try {
            new MessageFormat("{1, number}").formatToCharacterIterator(null);
            fail("NullPointerException was not thrown.");
        } catch(NullPointerException npe) {
        }
        try {
            new MessageFormat("{0, time}").formatToCharacterIterator(new Object[]{""});
            fail("IllegalArgumentException was not thrown.");
        } catch(IllegalArgumentException iae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "format",
        args = {java.lang.Object[].class, java.lang.StringBuffer.class, java.text.FieldPosition.class}
    )
    public void test_format$Ljava_lang_ObjectLjava_lang_StringBufferLjava_text_FieldPosition() {
        MessageFormat format = new MessageFormat("{1,number,integer}");
        StringBuffer buffer = new StringBuffer();
        format.format(new Object[] { "0", new Double(53.863) }, buffer,
                new FieldPosition(MessageFormat.Field.ARGUMENT));
        assertEquals("Wrong result", "54", buffer.toString());
        format.format(new Object[] { "0", new Double(53.863) }, buffer,
                new FieldPosition(MessageFormat.Field.ARGUMENT));
        assertEquals("Wrong result", "5454", buffer.toString());
        buffer = new StringBuffer();
        format
                .applyPattern("{0,choice,0#zero|1#one '{1,choice,2#two {2,time}}'}");
        Date date = new Date();
        String expected = "one two "
                + DateFormat.getTimeInstance().format(date);
        format.format(new Object[] { new Double(1.6),
                new Integer(3), date }, buffer, new FieldPosition(MessageFormat
                        .Field.ARGUMENT));
        assertEquals("Choice not recursive:\n" + expected + "\n" + buffer,
                expected, buffer.toString());
        StringBuffer str = format.format(new Object[] { new Double(0.6),
                new Integer(3)}, buffer, null);
        assertEquals(expected + "zero", str.toString());
        assertEquals(expected + "zero", buffer.toString());
        try {
            format.format(new Object[] { "0", new Double(1), "" }, buffer,
                    new FieldPosition(MessageFormat.Field.ARGUMENT));
            fail("IllegalArgumentException was not thrown.");
        } catch(IllegalArgumentException iae) {
        }
        try {
            format.format(new Object[] { "",  new Integer(3)}, buffer, 
                    new FieldPosition(MessageFormat.Field.ARGUMENT));
            fail("IllegalArgumentException was not thrown.");
        } catch(IllegalArgumentException iae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "format",
        args = {java.lang.Object.class, java.lang.StringBuffer.class, java.text.FieldPosition.class}
    )
    public void test_formatLjava_lang_ObjectLjava_lang_StringBufferLjava_text_FieldPosition() {
        new Support_MessageFormat(
                "test_formatLjava_lang_ObjectLjava_lang_StringBufferLjava_text_FieldPosition")
                .t_format_with_FieldPosition();
        String pattern = "On {4,date} at {3,time}, he ate {2,number, integer} " +
                "hamburger{2,choice,1#|1<s}.";
        MessageFormat format = new MessageFormat(pattern, Locale.US);
        Object[] objects = new Object[] { "", new Integer(3), 8, ""};
        try {
            format.format(objects, new StringBuffer(), 
                    new FieldPosition(DateFormat.Field.AM_PM));
            fail("IllegalArgumentException was not thrown.");
        } catch(IllegalArgumentException iae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "format",
        args = {java.lang.String.class, java.lang.Object[].class}
    )
    public void test_formatLjava_lang_StringLjava_lang_Object() {
        int iCurrency = 123;
        int iInteger  = Integer.MIN_VALUE;
        Date     date     = new Date(12345678);
        Object[] args     = { date, iCurrency, iInteger };
        String   resStr   = "Date: Jan 1, 1970 Currency: $" + iCurrency
                    + ".00 Integer: -2,147,483,648";
        String   pattern  = "Date: {0,date} Currency: {1, number, currency} Integer: {2, number, integer}";
        String   sFormat  = MessageFormat.format(pattern, (Object[]) args);
        assertEquals(
                "format(String, Object[]) with valid parameters returns incorrect string: case 1",
                sFormat, resStr);
        pattern = "abc {4, number, integer} def {3,date} ghi {2,number} jkl {1,choice,0#low|1#high} mnop {0}";
        resStr  = "abc -2,147,483,648 def Jan 1, 1970 ghi -2,147,483,648 jkl high mnop -2,147,483,648";
        Object[] args_ = { iInteger, 1, iInteger, date, iInteger };
        sFormat = MessageFormat.format(pattern, args_);
        assertEquals(
                "format(String, Object[]) with valid parameters returns incorrect string: case 1",
                sFormat, resStr);
        try {
            args = null;
            MessageFormat.format(null, args);
            fail("Doesn't throw IllegalArgumentException: null, null");
        } catch (Exception e) {
        }
        try {
            MessageFormat.format("Invalid {1,foobar} format descriptor!",
                    new Object[] {iInteger} );
            fail("Doesn't throw IllegalArgumentException with invalid pattern as a parameter: case 1");
        } catch (IllegalArgumentException ex) {
        }
        try {
            MessageFormat.format(
                    "Invalid {1,date,invalid-spec} format descriptor!", new Object[]{""});
            fail("Doesn't throw IllegalArgumentException with invalid pattern as a parameter: case 2");
        } catch (IllegalArgumentException ex) {
        }
        try {
            MessageFormat.format("{0,number,integer", new Object[] {iInteger});
            fail("Doesn't throw IllegalArgumentException, doesn't detect unmatched brackets");
        } catch (IllegalArgumentException ex) {
        }
        try {
            MessageFormat.format(
                    "Valid {1, date} format {0, number} descriptor!", new Object[]{ "" } );
            fail("Doesn't throw IllegalArgumentException with invalid Object array");
        } catch (IllegalArgumentException ex) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getFormats",
        args = {}
    )
    public void test_getFormats() {
        Format[] formats = format1.getFormats();
        Format[] correctFormats = new Format[] {
                NumberFormat.getCurrencyInstance(),
                DateFormat.getTimeInstance(),
                NumberFormat.getPercentInstance(), null,
                new ChoiceFormat("0#off|1#on"), DateFormat.getDateInstance(), };
        assertEquals("Test1:Returned wrong number of formats:",
                correctFormats.length, formats.length);
        for (int i = 0; i < correctFormats.length; i++) {
            assertEquals("Test1:wrong format for pattern index " + i + ":",
                    correctFormats[i], formats[i]);
        }
        formats = format2.getFormats();
        correctFormats = new Format[] { NumberFormat.getCurrencyInstance(),
                DateFormat.getTimeInstance(),
                NumberFormat.getPercentInstance(), null,
                new ChoiceFormat("0#off|1#on"), DateFormat.getDateInstance() };
        assertEquals("Test2:Returned wrong number of formats:",
                correctFormats.length, formats.length);
        for (int i = 0; i < correctFormats.length; i++) {
            assertEquals("Test2:wrong format for pattern index " + i + ":",
                    correctFormats[i], formats[i]);
        }
        formats = format3.getFormats();
        assertEquals("Test3: Returned wrong number of formats:", 0,
                formats.length);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getFormatsByArgumentIndex",
        args = {}
    )
    public void test_getFormatsByArgumentIndex() {
        Format[] formats = format1.getFormatsByArgumentIndex();
        Format[] correctFormats = new Format[] { DateFormat.getDateInstance(),
                new ChoiceFormat("0#off|1#on"), DateFormat.getTimeInstance(),
                NumberFormat.getCurrencyInstance(), null };
        assertEquals("Test1:Returned wrong number of formats:",
                correctFormats.length, formats.length);
        for (int i = 0; i < correctFormats.length; i++) {
            assertEquals("Test1:wrong format for argument index " + i + ":",
                    correctFormats[i], formats[i]);
        }
        formats = format2.getFormatsByArgumentIndex();
        correctFormats = new Format[] { DateFormat.getDateInstance(),
                new ChoiceFormat("0#off|1#on"), null,
                NumberFormat.getCurrencyInstance(), null, null, null, null,
                DateFormat.getTimeInstance() };
        assertEquals("Test2:Returned wrong number of formats:",
                correctFormats.length, formats.length);
        for (int i = 0; i < correctFormats.length; i++) {
            assertEquals("Test2:wrong format for argument index " + i + ":",
                    correctFormats[i], formats[i]);
        }
        formats = format3.getFormatsByArgumentIndex();
        assertEquals("Test3: Returned wrong number of formats:", 0,
                formats.length);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLocale",
        args = {}
    )
    public void test_getLocale() {
        try {
            Locale[] l = {
                    Locale.FRANCE,
                    Locale.KOREA,
                    new Locale(Locale.FRANCE.getCountry(), Locale.FRANCE
                            .getLanguage()), new Locale("mk"),
                    new Locale("mk", "MK"), Locale.US,
                    new Locale("#ru", "@31230") };
            String pattern = "getLocale test {0,number,#,####}";
            MessageFormat mf;
            for (int i = 0; i < 0; i++) {
                mf = new MessageFormat(pattern, l[i]);
                Locale result = mf.getLocale();
                assertEquals("Returned local: " + result + " instead of "
                        + l[i], l[i], result);
                assertEquals("Returned language: " + result.getLanguage()
                        + " instead of " + l[i].getLanguage(), l[i]
                        .getLanguage(), result.getLanguage());
                assertEquals("Returned country: " + result.getCountry()
                        + " instead of " + l[i].getCountry(),
                        l[i].getCountry(), result.getCountry());
            }
            mf = new MessageFormat(pattern);
            mf.setLocale(null);
            Locale result = mf.getLocale();
            assertEquals("Returned local: " + result + " instead of null",
                    null, result);
        } catch (Exception e) {
            fail("unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setFormat",
        args = {int.class, java.text.Format.class}
    )
    public void test_setFormatILjava_text_Format() {
        try {
            MessageFormat f1 = (MessageFormat) format1.clone();
            f1.setFormat(0, DateFormat.getTimeInstance());
            f1.setFormat(1, DateFormat.getTimeInstance());
            f1.setFormat(2, NumberFormat.getInstance());
            f1.setFormat(3, new ChoiceFormat("0#off|1#on"));
            f1.setFormat(4, new ChoiceFormat("1#few|2#ok|3#a lot"));
            f1.setFormat(5, DateFormat.getTimeInstance());
            Format[] formats = f1.getFormats();
            formats = f1.getFormats();
            Format[] correctFormats = new Format[] {
                    DateFormat.getTimeInstance(), DateFormat.getTimeInstance(),
                    NumberFormat.getInstance(), new ChoiceFormat("0#off|1#on"),
                    new ChoiceFormat("1#few|2#ok|3#a lot"),
                    DateFormat.getTimeInstance() };
            assertEquals("Test1A:Returned wrong number of formats:",
                    correctFormats.length, formats.length);
            for (int i = 0; i < correctFormats.length; i++) {
                assertEquals(
                        "Test1B:wrong format for pattern index " + i + ":",
                        correctFormats[i], formats[i]);
            }
            try {
                f1.setFormat(-1, DateFormat.getDateInstance());
                fail("Expected ArrayIndexOutOfBoundsException was not thrown");
                f1.setFormat(f1.getFormats().length, DateFormat
                        .getDateInstance());
                fail("Expected ArrayIndexOutOfBoundsException was not thrown");
            } catch (ArrayIndexOutOfBoundsException e) {
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setFormatByArgumentIndex",
        args = {int.class, java.text.Format.class}
    )
    public void test_setFormatByArgumentIndexILjava_text_Format() {
        MessageFormat f1 = (MessageFormat) format1.clone();
        f1.setFormatByArgumentIndex(0, DateFormat.getTimeInstance());
        f1.setFormatByArgumentIndex(4, new ChoiceFormat("1#few|2#ok|3#a lot"));
        Format[] formats = f1.getFormatsByArgumentIndex();
        Format[] correctFormats = new Format[] { DateFormat.getTimeInstance(),
                new ChoiceFormat("0#off|1#on"), DateFormat.getTimeInstance(),
                NumberFormat.getCurrencyInstance(),
                new ChoiceFormat("1#few|2#ok|3#a lot") };
        assertEquals("Test1A:Returned wrong number of formats:",
                correctFormats.length, formats.length);
        for (int i = 0; i < correctFormats.length; i++) {
            assertEquals("Test1B:wrong format for argument index " + i + ":",
                    correctFormats[i], formats[i]);
        }
        formats = f1.getFormats();
        correctFormats = new Format[] { NumberFormat.getCurrencyInstance(),
                DateFormat.getTimeInstance(), DateFormat.getTimeInstance(),
                new ChoiceFormat("1#few|2#ok|3#a lot"),
                new ChoiceFormat("0#off|1#on"), DateFormat.getTimeInstance(), };
        assertEquals("Test1C:Returned wrong number of formats:",
                correctFormats.length, formats.length);
        for (int i = 0; i < correctFormats.length; i++) {
            assertEquals("Test1D:wrong format for pattern index " + i + ":",
                    correctFormats[i], formats[i]);
        }
        MessageFormat f2 = (MessageFormat) format2.clone();
        f2.setFormatByArgumentIndex(2, NumberFormat.getPercentInstance());
        f2.setFormatByArgumentIndex(4, DateFormat.getTimeInstance());
        formats = f2.getFormatsByArgumentIndex();
        correctFormats = format2.getFormatsByArgumentIndex();
        assertEquals("Test2A:Returned wrong number of formats:",
                correctFormats.length, formats.length);
        for (int i = 0; i < correctFormats.length; i++) {
            assertEquals("Test2B:wrong format for argument index " + i + ":",
                    correctFormats[i], formats[i]);
        }
        formats = f2.getFormats();
        correctFormats = format2.getFormats();
        assertEquals("Test2C:Returned wrong number of formats:",
                correctFormats.length, formats.length);
        for (int i = 0; i < correctFormats.length; i++) {
            assertEquals("Test2D:wrong format for pattern index " + i + ":",
                    correctFormats[i], formats[i]);
        }
        MessageFormat f3 = (MessageFormat) format3.clone();
        f3.setFormatByArgumentIndex(1, NumberFormat.getCurrencyInstance());
        formats = f3.getFormatsByArgumentIndex();
        assertEquals("Test3A:Returned wrong number of formats:", 0,
                formats.length);
        formats = f3.getFormats();
        assertEquals("Test3B:Returned wrong number of formats:", 0,
                formats.length);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setFormats",
        args = {java.text.Format[].class}
    )
    public void test_setFormats$Ljava_text_Format() {
        try {
            MessageFormat f1 = (MessageFormat) format1.clone();
            Format[] correctFormats = new Format[] {
                    DateFormat.getTimeInstance(),
                    new ChoiceFormat("0#off|1#on"),
                    DateFormat.getTimeInstance(),
                    NumberFormat.getCurrencyInstance(),
                    new ChoiceFormat("1#few|2#ok|3#a lot") };
            f1.setFormats(correctFormats);
            Format[] formats = f1.getFormats();
            assertTrue("Test1A:Returned wrong number of formats:",
                    correctFormats.length <= formats.length);
            for (int i = 0; i < correctFormats.length; i++) {
                assertEquals("Test1B:wrong format for argument index " + i
                        + ":", correctFormats[i], formats[i]);
            }
            try {
                f1.setFormats(null);
                fail("Expected exception NullPointerException was not thrown");
            } catch (NullPointerException e) {
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setFormatsByArgumentIndex",
        args = {java.text.Format[].class}
    )
    public void test_setFormatsByArgumentIndex$Ljava_text_Format() {
        MessageFormat f1 = (MessageFormat) format1.clone();
        Format[] correctFormats = new Format[] { DateFormat.getTimeInstance(),
                new ChoiceFormat("0#off|1#on"), DateFormat.getTimeInstance(),
                NumberFormat.getCurrencyInstance(),
                new ChoiceFormat("1#few|2#ok|3#a lot") };
        f1.setFormatsByArgumentIndex(correctFormats);
        Format[] formats = f1.getFormatsByArgumentIndex();
        assertEquals("Test1A:Returned wrong number of formats:",
                correctFormats.length, formats.length);
        for (int i = 0; i < correctFormats.length; i++) {
            assertEquals("Test1B:wrong format for argument index " + i + ":",
                    correctFormats[i], formats[i]);
        }
        formats = f1.getFormats();
        correctFormats = new Format[] { NumberFormat.getCurrencyInstance(),
                DateFormat.getTimeInstance(), DateFormat.getTimeInstance(),
                new ChoiceFormat("1#few|2#ok|3#a lot"),
                new ChoiceFormat("0#off|1#on"), DateFormat.getTimeInstance(), };
        assertEquals("Test1C:Returned wrong number of formats:",
                correctFormats.length, formats.length);
        for (int i = 0; i < correctFormats.length; i++) {
            assertEquals("Test1D:wrong format for pattern index " + i + ":",
                    correctFormats[i], formats[i]);
        }
        MessageFormat f2 = (MessageFormat) format2.clone();
        Format[] inputFormats = new Format[] { DateFormat.getDateInstance(),
                new ChoiceFormat("0#off|1#on"),
                NumberFormat.getPercentInstance(),
                NumberFormat.getCurrencyInstance(),
                DateFormat.getTimeInstance(), null, null, null,
                DateFormat.getTimeInstance() };
        f2.setFormatsByArgumentIndex(inputFormats);
        formats = f2.getFormatsByArgumentIndex();
        correctFormats = format2.getFormatsByArgumentIndex();
        assertEquals("Test2A:Returned wrong number of formats:",
                correctFormats.length, formats.length);
        for (int i = 0; i < correctFormats.length; i++) {
            assertEquals("Test2B:wrong format for argument index " + i + ":",
                    correctFormats[i], formats[i]);
        }
        formats = f2.getFormats();
        correctFormats = new Format[] { NumberFormat.getCurrencyInstance(),
                DateFormat.getTimeInstance(), DateFormat.getDateInstance(),
                null, new ChoiceFormat("0#off|1#on"),
                DateFormat.getDateInstance() };
        assertEquals("Test2C:Returned wrong number of formats:",
                correctFormats.length, formats.length);
        for (int i = 0; i < correctFormats.length; i++) {
            assertEquals("Test2D:wrong format for pattern index " + i + ":",
                    correctFormats[i], formats[i]);
        }
        MessageFormat f3 = (MessageFormat) format3.clone();
        f3.setFormatsByArgumentIndex(inputFormats);
        formats = f3.getFormatsByArgumentIndex();
        assertEquals("Test3A:Returned wrong number of formats:", 0,
                formats.length);
        formats = f3.getFormats();
        assertEquals("Test3B:Returned wrong number of formats:", 0,
                formats.length);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "parse",
        args = {java.lang.String.class, java.text.ParsePosition.class}
    )
    public void test_parseLjava_lang_StringLjava_text_ParsePosition() {
        MessageFormat format = new MessageFormat("date is {0,date,MMM d, yyyy}");
        ParsePosition pos = new ParsePosition(2);
        Object[] result = (Object[]) format
                .parse("xxdate is Feb 28, 1999", pos);
        assertTrue("No result: " + result.length, result.length >= 1);
        assertTrue("Wrong answer", ((Date) result[0])
                .equals(new GregorianCalendar(1999, Calendar.FEBRUARY, 28)
                        .getTime()));
        MessageFormat mf = new MessageFormat("vm={0},{1},{2}");
        result = mf.parse("vm=win,foo,bar", new ParsePosition(0));
        assertTrue("Invalid parse", result[0].equals("win")
                && result[1].equals("foo") && result[2].equals("bar"));
        mf = new MessageFormat("{0}; {0}; {0}");
        String parse = "a; b; c";
        result = mf.parse(parse, new ParsePosition(0));
        assertEquals("Wrong variable result", "c", result[0]);
        try {
            mf.parse(parse, null);
            fail("NullPointerException was not thrown.");
        } catch(NullPointerException npe) {
        }
        try {
            mf.parse(null, pos);
        } catch(NullPointerException npe) {
            fail("NullPointerException was thrown.");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setLocale",
        args = {java.util.Locale.class}
    )
    public void test_setLocaleLjava_util_Locale() {
        MessageFormat format = new MessageFormat("date {0,date}");
        format.setLocale(Locale.CHINA);
        assertEquals("Wrong locale1", Locale.CHINA, format.getLocale());
        format.applyPattern("{1,date}");
        assertEquals("Wrong locale3", DateFormat.getDateInstance(
                DateFormat.DEFAULT, Locale.CHINA), format.getFormats()[0]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toPattern",
        args = {}
    )
    public void test_toPattern() {
        String pattern = "[{0}]";
        MessageFormat mf = new MessageFormat(pattern);
        assertTrue("Wrong pattern", mf.toPattern().equals(pattern));
        new MessageFormat("CHOICE {1,choice}").toPattern();
    }
    protected void setUp() {
        defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);
        String pattern = "A {3, number, currency} B {2, time} C {0, number, percent} D {4}  E {1,choice,0#off|1#on} F {0, date}";
        format1 = new MessageFormat(pattern);
        pattern = "A {3, number, currency} B {8, time} C {0, number, percent} D {6}  E {1,choice,0#off|1#on} F {0, date}";
        format2 = new MessageFormat(pattern);
        pattern = "A B C D E F";
        format3 = new MessageFormat(pattern);
    }
    protected void tearDown() {
        Locale.setDefault(defaultLocale);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IllegalArgumentException.",
        method = "MessageFormat",
        args = {java.lang.String.class, java.util.Locale.class}
    )
    public void test_ConstructorLjava_util_Locale() {
        try {
            new MessageFormat("{0,number,integer", Locale.US);
            fail("Assert 0: Failed to detect unmatched brackets.");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "parse",
            args = {java.lang.String.class}
    )
    public void test_parseLjava_lang_String() throws ParseException {
        String pattern = "A {3, number, currency} B {2, time} C {0, number, percent} D {4}  E {1,choice,0#off|1#on} F {0, date}";
        MessageFormat mf = new MessageFormat(pattern);
        String sToParse = "A $12,345.00 B 9:56:07 AM C 3,200% D 1/15/70 9:56 AM  E on F Jan 1, 1970";
        Object[] result;
        try {
            result = mf.parse(sToParse);
            assertTrue("No result: " + result.length, result.length == 5);
            assertTrue("Object 0 is not date", result[0] instanceof Date);
            assertEquals("Object 1 is not stringr", result[1].toString(), "1.0");
            assertTrue("Object 2 is not date", result[2] instanceof Date);
            assertEquals("Object 3 is not number", result[3].toString(),
                    "12345");
            assertEquals("Object 4 is not string", result[4].toString(),
                    "1/15/70 9:56 AM");
        } catch (java.text.ParseException pe) {
            fail("ParseException is thrown for incorrect string " + sToParse);
        }
        sToParse = "xxdate is Feb 28, 1999";
        try {
            result = format1.parse(sToParse);
            fail("ParseException is thrown for incorrect string " + sToParse);
        } catch (java.text.ParseException pe) {
        }
        sToParse = "vm=Test, @3 4 6, 3   ";
        mf = new MessageFormat("vm={0},{1},{2}");
        try {
            result = mf.parse(sToParse);
            assertTrue("No result: " + result.length, result.length == 3);
            assertEquals("Object 0 is not string", result[0].toString(), "Test");
            assertEquals("Object 1 is not string", result[1].toString(),
                    " @3 4 6");
            assertEquals("Object 2 is not string", result[2].toString(),
                    " 3   ");
        } catch (java.text.ParseException pe) {
            fail("ParseException is thrown for correct string " + sToParse);
        }
        try {
            result = mf.parse(null);
            fail("ParseException is not thrown for null " + sToParse);
        } catch (java.text.ParseException pe) {
        }
    }
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "parseObject",
            args = {java.lang.String.class, java.text.ParsePosition.class}
    )
    public void test_parseObjectLjava_lang_StringLjavajava_text_ParsePosition() {
        MessageFormat mf = new MessageFormat("{0,number,#.##}, {0,number,#.#}");
        try {
            Object[] objs = { new Double(3.1415) };
            String result = mf.format(objs);
            Object[] res = null;
            ParsePosition pp = new ParsePosition(0);
            int parseIndex = pp.getIndex();
            res = (Object[]) mf.parseObject(result, pp);
            assertTrue("Parse operation return null", res != null);
            assertTrue("parse operation return array with incorrect length",
                    1 == res.length);
            assertTrue("ParseIndex is incorrect", pp.getIndex() != parseIndex);
            assertTrue("Result object is incorrect", new Double(3.1)
                    .equals(res[0]));
            pp.setIndex(0);
            char[] cur = result.toCharArray();
            cur[cur.length / 2] = 'Z';
            String partialCorrect = new String(cur);
            res = (Object[]) mf.parseObject(partialCorrect, pp);
            assertTrue("Parse operation return null", res == null);
            assertTrue("ParseIndex is incorrect", pp.getIndex() == 0);
            assertTrue("ParseErrorIndex is incorrect",
                    pp.getErrorIndex() == cur.length / 2);
            try {
                mf.parseObject(result, null);
                fail("Expected NullPointerException was not thrown");
            } catch (NullPointerException e) {
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Regression test. Doesn't verifies exception.",
            method = "format",
            args = {java.lang.Object.class}
    )
    public void test_format_Object() {
        Locale.setDefault(Locale.CANADA);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        String pat = "text here {0,date,yyyyyyyyy} and here";
        Calendar c = Calendar.getInstance();
        String etalon = "text here 00000" + c.get(Calendar.YEAR) + " and here";
        MessageFormat obj = new MessageFormat(pat);
        assertEquals(etalon, obj.format(new Object[] { new Date() }));
    }
    @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "parse",
            args = {java.lang.String.class}
    )
    public void test_parse() throws ParseException {
        MessageFormat mf = new MessageFormat("{0,number,#,####}", Locale.US);
        Object[] res = mf.parse("1,00,00");
        assertEquals("Assert 0: incorrect size of parsed data ", 1, res.length);
        assertEquals("Assert 1: parsed value incorrectly", new Long(10000), (Long)res[0]);
    }
    @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "format",
            args = {java.lang.String.class, java.lang.Object[].class}
    )
    public void testHARMONY5323() {
        Object []messageArgs = new Object[11];
        for (int i = 0; i < messageArgs.length; i++)
            messageArgs[i] = "dumb"+i;
        String res = MessageFormat.format("bgcolor=\"{10}\"", messageArgs);
        assertEquals(res, "bgcolor=\"dumb10\"");
    }
}
