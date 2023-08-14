public class AddressUnitTests extends AndroidTestCase {
    private static final String MULTI_ADDRESSES_LIST = 
            "noname1@dom1.com, "
            + "<noname2@dom2.com>, "
            + "simple name <address3@dom3.org>, "
            + "\"name,4\" <address4@dom4.org>,"
            + "\"big \\\"G\\\"\" <bigG@dom5.net>,"
            + "\u65E5\u672C\u8A9E <address6@co.jp>,"
            + "\"\u65E5\u672C\u8A9E\" <address7@co.jp>,"
            + "\uD834\uDF01\uD834\uDF46 <address8@ne.jp>,"
            + "\"\uD834\uDF01\uD834\uDF46\" <address9@ne.jp>";
    private static final int MULTI_ADDRESSES_COUNT = 9;
    private static final Address PACK_ADDR_1 = new Address("john@gmail.com", "John Doe");
    private static final Address PACK_ADDR_2 = new Address("foo@bar.com", null);
    private static final Address PACK_ADDR_3 = new Address("mar.y+test@gmail.com", "Mar-y, B; B*arr");
    private static final Address[][] PACK_CASES = {
        {PACK_ADDR_2}, {PACK_ADDR_1}, 
        {PACK_ADDR_1, PACK_ADDR_2}, {PACK_ADDR_2, PACK_ADDR_1}, 
        {PACK_ADDR_1, PACK_ADDR_3}, {PACK_ADDR_2, PACK_ADDR_2}, 
        {PACK_ADDR_1, PACK_ADDR_2, PACK_ADDR_3}, {PACK_ADDR_3, PACK_ADDR_1, PACK_ADDR_2}
    };
    Address mAddress1;
    Address mAddress2;
    Address mAddress3;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mAddress1 = new Address("address1", "personal1");
        mAddress2 = new Address("address2", "");
        mAddress3 = new Address("address3", null);
    }
    private String padEncoded(String s) {
        return "=?UTF-8?B?" + s + "?=";
    }
    public void testBase64Decode() {
        String testString = "xyza\0\"";
        String base64Encoded[] = {"", "eA==", "eHk=", "eHl6", "eHl6YQ==", "eHl6YQA=", "eHl6YQAi"};
        int len = testString.length();
        for (int i = 1; i <= len; ++i) {
            String encoded = padEncoded(base64Encoded[i]);
            String decoded = DecoderUtil.decodeEncodedWords(encoded);
            String prefix = testString.substring(0, i);
            assertEquals(""+i, prefix, decoded);
        }
    }
    public void testSetAddress() {
        String bareAddress = "user1@dom1.com";
        String bracketAddress = "<user2@dom2.com>";
        Address address = new Address(bareAddress);
        assertEquals("bare address", "user1@dom1.com", address.getAddress());
        address.setAddress(bracketAddress);
        assertEquals("bracket address", "user2@dom2.com", address.getAddress());
    }
    public void testNullPersonal() {
        Address address = new Address("user1@dom1.org");
        assertNull("no name", address.getPersonal());
        address.setPersonal(null);
        assertNull("null name", address.getPersonal());
        address.setPersonal("");
        assertNull("empty name", address.getPersonal());
        address.setPersonal("\"\"");
        assertNull("quoted empty address", address.getPersonal());
    }
    public void testSetPersonal() {
        Address address = new Address("user1@dom1.net", "simple name");
        assertEquals("simple name", "simple name", address.getPersonal());
        address.setPersonal("big \\\"G\\\"");
        assertEquals("quoted name", "big \"G\"", address.getPersonal());
        address.setPersonal("=?UTF-8?Q?big \"G\"?=");
        assertEquals("quoted printable name", "big \"G\"", address.getPersonal());
        address.setPersonal("=?UTF-8?B?YmlnICJHIg==?=");
        assertEquals("base64 encoded name", "big \"G\"", address.getPersonal());
    }
    public void testSetPersonalMultipleEncodings() {
        Address address = new Address("user1@dom1.co.jp", "=?UTF-8?B?5bK45pys?=");
        assertEquals("base64 utf-16 name", "\u5CB8\u672C", address.getPersonal());
        address.setPersonal("\"=?UTF-8?Q?=E5=B2=B8=E6=9C=AC?=\"");
        assertEquals("quoted printable utf-16 name", "\u5CB8\u672C", address.getPersonal());
        address.setPersonal("=?ISO-2022-JP?B?GyRCNF9LXBsoQg==?=");
        assertEquals("base64 jis encoded name", "\u5CB8\u672C", address.getPersonal());
        address.setPersonal("\"=?UTF-8?B?8J2MgfCdjYY=?=\"");
        assertEquals("base64 utf-32 name", "\uD834\uDF01\uD834\uDF46", address.getPersonal());
        address.setPersonal("=?UTF-8?Q?=F0=9D=8C=81=F0=9D=8D=86?=");
        assertEquals("quoted printable utf-32 name",
                "\uD834\uDF01\uD834\uDF46", address.getPersonal());
    }
    public void testEmptyParse() {
        Address[] result;
        result = Address.parse(null);
        assertTrue("parsing null address", result != null && result.length == 0);
        result = Address.parse("");
        assertTrue("parsing zero-length", result != null && result.length == 0);
        result = Address.parse("   ");
        assertTrue("parsing spaces", result != null && result.length == 0);
        result = Address.parse("  ,  ");
        assertTrue("parsing spaces with comma", result != null && result.length == 0);
    }
    public void testSingleParse() {
        Address[] address1 = Address.parse("address1@dom1.com");
        assertEquals("bare address count", 1, address1.length);
        assertEquals("bare address", "address1@dom1.com", address1[0].getAddress());
        assertNull("name of bare address", address1[0].getPersonal());
        Address[] address2 = Address.parse("<address2@dom2.com>");
        assertEquals("bracket address count", 1, address2.length);
        assertEquals("bracket address", "address2@dom2.com", address2[0].getAddress());
        assertNull("name of bracket address", address2[0].getPersonal());
        Address[] address3 = Address.parse("first last <address3@dom3.org>");
        assertEquals("address with name count", 1, address3.length);
        assertEquals("address with name", "address3@dom3.org", address3[0].getAddress());
        assertEquals("name of address with name", "first last", address3[0].getPersonal());
        Address[] address4 = Address.parse("\"first,last\" <address4@dom4.org>");
        assertEquals("address with quoted name count", 1, address4.length);
        assertEquals("address with quoted name", "address4@dom4.org", address4[0].getAddress());
        assertEquals("name of address with quoted name", "first,last", address4[0].getPersonal());
    }
    public void testIllegalParse() {
        Address[] address1 = Address.parse("address1");
        assertEquals("no atmark", 0, address1.length);
        Address[] address2 = Address.parse("address2@");
        assertEquals("no domain", 0, address2.length);
        Address[] address3 = Address.parse("@dom3.com");
        assertEquals("no local part", 0, address3.length);
        Address[] address4 = Address.parse("address4@sub@dom4.org");
        assertEquals("more than one atmark", 0, address4.length);
        Address[] address5 = Address.parse("address5@dom5");
        assertEquals("not dot in domain part", 0, address5.length);
        Address[] address6 = Address.parse("address6@dom6.com.");
        assertEquals("domain ends with dot", 0, address6.length);
        Address[] address7 = Address.parse("address7@.dom7.org");
        assertEquals("domain starts with dot", 0, address7.length);
    }
    public void testParsingAddress() {
        Address[] addresses = Address.parse("address1@dom1.net, <address2@dom2.com>");
        assertEquals("address count", 2, addresses.length);
        assertEquals("bare address", "address1@dom1.net", addresses[0].getAddress());
        assertNull("bare address name", addresses[0].getPersonal());
        assertEquals("bracket address", "address2@dom2.com", addresses[1].getAddress());
        assertNull("bracket address name", addresses[1].getPersonal());
    }
    public void testParsingSimpleName() {
        Address[] addresses = Address.parse(
                "name 1 <address1@dom1.net>, " +
                "\"name,2\" <address2@dom2.org>");
        assertEquals("address count", 2, addresses.length);
        assertEquals("bare name address", "address1@dom1.net", addresses[0].getAddress());
        assertEquals("bare name", "name 1", addresses[0].getPersonal());
        assertEquals("double quoted name address", "address2@dom2.org", addresses[1].getAddress());
        assertEquals("double quoted name", "name,2", addresses[1].getPersonal());
    }
    public void testParsingUtf16Name() {
        Address[] addresses = Address.parse(
                "\u3042\u3044\u3046 \u3048\u304A <address1@dom1.jp>, " +
                "\"\u3042\u3044\u3046,\u3048\u304A\" <address2@dom2.jp>");
        assertEquals("address count", 2, addresses.length);
        assertEquals("bare utf-16 name address", "address1@dom1.jp", addresses[0].getAddress());
        assertEquals("bare utf-16 name",
                "\u3042\u3044\u3046 \u3048\u304A", addresses[0].getPersonal());
        assertEquals("double quoted utf-16 name address",
                "address2@dom2.jp", addresses[1].getAddress());
        assertEquals("double quoted utf-16 name",
                "\u3042\u3044\u3046,\u3048\u304A", addresses[1].getPersonal());
    }
    public void testParsingUtf32Name() {
        Address[] addresses = Address.parse(
                "\uD834\uDF01\uD834\uDF46 \uD834\uDF22 <address1@dom1.net>, " +
                "\"\uD834\uDF01\uD834\uDF46,\uD834\uDF22\" <address2@dom2.com>");
        assertEquals("address count", 2, addresses.length);
        assertEquals("bare utf-32 name address", "address1@dom1.net", addresses[0].getAddress());
        assertEquals("bare utf-32 name",
                "\uD834\uDF01\uD834\uDF46 \uD834\uDF22", addresses[0].getPersonal());
        assertEquals("double quoted utf-32 name address",
                "address2@dom2.com", addresses[1].getAddress());
        assertEquals("double quoted utf-32 name",
                "\uD834\uDF01\uD834\uDF46,\uD834\uDF22", addresses[1].getPersonal());
    }
    public void testParseMulti() {
        Address[] addresses = Address.parse(MULTI_ADDRESSES_LIST);
        assertEquals("multi addrsses count", MULTI_ADDRESSES_COUNT, addresses.length);
        assertEquals("no name 1 address", "noname1@dom1.com", addresses[0].getAddress());
        assertNull("no name 1 name", addresses[0].getPersonal());
        assertEquals("no name 2 address", "noname2@dom2.com", addresses[1].getAddress());
        assertNull("no name 2 name", addresses[1].getPersonal());
        assertEquals("simple name address", "address3@dom3.org", addresses[2].getAddress());
        assertEquals("simple name name", "simple name", addresses[2].getPersonal());
        assertEquals("double quoted name address", "address4@dom4.org", addresses[3].getAddress());
        assertEquals("double quoted name name", "name,4", addresses[3].getPersonal());
        assertEquals("quoted name address", "bigG@dom5.net", addresses[4].getAddress());
        assertEquals("quoted name name", "big \"G\"", addresses[4].getPersonal());
        assertEquals("utf-16 name address", "address6@co.jp", addresses[5].getAddress());       
        assertEquals("utf-16 name name", "\u65E5\u672C\u8A9E", addresses[5].getPersonal());       
        assertEquals("utf-16 quoted name address", "address7@co.jp", addresses[6].getAddress());       
        assertEquals("utf-16 quoted name name", "\u65E5\u672C\u8A9E",
                addresses[6].getPersonal());       
        assertEquals("utf-32 name address", "address8@ne.jp", addresses[7].getAddress());       
        assertEquals("utf-32 name name", "\uD834\uDF01\uD834\uDF46", addresses[7].getPersonal());       
        assertEquals("utf-32 quoted name address", "address9@ne.jp", addresses[8].getAddress());       
        assertEquals("utf-32 quoted name name", "\uD834\uDF01\uD834\uDF46",
                addresses[8].getPersonal());       
    }
    public void testToStringSingle() {
        Address[] addresses = Address.parse(MULTI_ADDRESSES_LIST);
        assertEquals("multi addrsses count", MULTI_ADDRESSES_COUNT, addresses.length);
        assertEquals("no name 1", "noname1@dom1.com", addresses[0].toString());
        assertEquals("no name 2", "noname2@dom2.com", addresses[1].toString());
        assertEquals("simple name", "simple name <address3@dom3.org>", addresses[2].toString());
        assertEquals("double quoted name", "\"name,4\" <address4@dom4.org>", addresses[3].toString());
        assertEquals("quoted name", "\"big \"G\"\" <bigG@dom5.net>", addresses[4].toString());
        assertEquals("utf-16 name", "\u65E5\u672C\u8A9E <address6@co.jp>",
                addresses[5].toString());       
        assertEquals("utf-16 quoted name", "\u65E5\u672C\u8A9E <address7@co.jp>",
                addresses[6].toString());       
        assertEquals("utf-32 name", "\uD834\uDF01\uD834\uDF46 <address8@ne.jp>",
                addresses[7].toString());       
        assertEquals("utf-32 quoted name", "\uD834\uDF01\uD834\uDF46 <address9@ne.jp>",
                addresses[8].toString());       
    }
    public void testToStringMulti() {
        Address[] addresses = Address.parse(MULTI_ADDRESSES_LIST);
        assertEquals("multi addrsses count", MULTI_ADDRESSES_COUNT, addresses.length);
        String line = Address.toString(addresses);
        assertEquals("toString multi", 
                "noname1@dom1.com,"
                + "noname2@dom2.com,"
                + "simple name <address3@dom3.org>,"
                + "\"name,4\" <address4@dom4.org>,"
                + "\"big \"G\"\" <bigG@dom5.net>,"
                + "\u65E5\u672C\u8A9E <address6@co.jp>,"
                + "\u65E5\u672C\u8A9E <address7@co.jp>,"
                + "\uD834\uDF01\uD834\uDF46 <address8@ne.jp>,"
                + "\uD834\uDF01\uD834\uDF46 <address9@ne.jp>",
                line);
    }
    public void testParsingQuotedEncodedName() {
        Address[] addresses = Address.parse(
                "\"big \\\"G\\\"\" <bigG@dom1.com>, =?UTF-8?B?5pel5pys6Kqe?= <address2@co.jp>");
        assertEquals("address count", 2, addresses.length);
        assertEquals("quoted name address", "bigG@dom1.com", addresses[0].getAddress());
        assertEquals("quoted name", "big \"G\"", addresses[0].getPersonal());
        assertEquals("encoded name address", "address2@co.jp", addresses[1].getAddress());
        assertEquals("encoded name", "\u65E5\u672C\u8A9E", addresses[1].getPersonal());
    }
    public void testToHeaderSingle() {
        Address noName1 = new Address("noname1@dom1.com");
        Address noName2 = new Address("<noname2@dom2.com>", "");
        Address simpleName = new Address("address3@dom3.org", "simple name");
        Address dquoteName = new Address("address4@dom4.org", "name,4");
        Address quotedName = new Address("bigG@dom5.net", "big \"G\"");
        Address utf16Name = new Address("<address6@co.jp>", "\"\u65E5\u672C\u8A9E\"");
        Address utf32Name = new Address("<address8@ne.jp>", "\uD834\uDF01\uD834\uDF46");
        assertEquals("no name 1 address", "noname1@dom1.com", noName1.getAddress());
        assertNull("no name 1 name", noName1.getPersonal());
        assertEquals("no name 2 address", "noname2@dom2.com", noName2.getAddress());
        assertNull("no name 2 name", noName2.getPersonal());
        assertEquals("simple name address", "address3@dom3.org", simpleName.getAddress());
        assertEquals("simple name name", "simple name", simpleName.getPersonal());
        assertEquals("double quoted name address", "address4@dom4.org", dquoteName.getAddress());
        assertEquals("double quoted name name", "name,4", dquoteName.getPersonal());
        assertEquals("quoted name address", "bigG@dom5.net", quotedName.getAddress());
        assertEquals("quoted name name", "big \"G\"", quotedName.getPersonal());
        assertEquals("utf-16 name address", "address6@co.jp", utf16Name.getAddress());       
        assertEquals("utf-16 name name", "\u65E5\u672C\u8A9E", utf16Name.getPersonal());       
        assertEquals("utf-32 name address", "address8@ne.jp", utf32Name.getAddress());       
        assertEquals("utf-32 name name", "\uD834\uDF01\uD834\uDF46", utf32Name.getPersonal());       
        assertEquals("no name 1", "noname1@dom1.com", noName1.toHeader());
        assertEquals("no name 2", "noname2@dom2.com", noName2.toHeader());
        assertEquals("simple name", "simple name <address3@dom3.org>", simpleName.toHeader());
        assertEquals("double quoted name", "\"name,4\" <address4@dom4.org>", dquoteName.toHeader());
        assertEquals("quoted name", "\"big \\\"G\\\"\" <bigG@dom5.net>", quotedName.toHeader());
        assertEquals("utf-16 name", "=?UTF-8?B?5pel5pys6Kqe?= <address6@co.jp>",
                utf16Name.toHeader());       
        assertEquals("utf-32 name", "=?UTF-8?B?8J2MgfCdjYY=?= <address8@ne.jp>",
                utf32Name.toHeader());       
    }
    public void testToHeaderMulti() {
        Address noName1 = new Address("noname1@dom1.com");
        Address noName2 = new Address("<noname2@dom2.com>", "");
        Address simpleName = new Address("address3@dom3.org", "simple name");
        Address dquoteName = new Address("address4@dom4.org", "name,4");
        Address quotedName = new Address("bigG@dom5.net", "big \"G\"");
        Address utf16Name = new Address("<address6@co.jp>", "\"\u65E5\u672C\u8A9E\"");
        Address utf32Name = new Address("<address8@ne.jp>", "\uD834\uDF01\uD834\uDF46");
        assertEquals("no name 1 address", "noname1@dom1.com", noName1.getAddress());
        assertNull("no name 1 name", noName1.getPersonal());
        assertEquals("no name 2 address", "noname2@dom2.com", noName2.getAddress());
        assertNull("no name 2 name", noName2.getPersonal());
        assertEquals("simple name address", "address3@dom3.org", simpleName.getAddress());
        assertEquals("simple name name", "simple name", simpleName.getPersonal());
        assertEquals("double quoted name address", "address4@dom4.org", dquoteName.getAddress());
        assertEquals("double quoted name name", "name,4", dquoteName.getPersonal());
        assertEquals("quoted name address", "bigG@dom5.net", quotedName.getAddress());
        assertEquals("quoted name name", "big \"G\"", quotedName.getPersonal());
        assertEquals("utf-16 name address", "address6@co.jp", utf16Name.getAddress());       
        assertEquals("utf-16 name name", "\u65E5\u672C\u8A9E", utf16Name.getPersonal());       
        assertEquals("utf-32 name address", "address8@ne.jp", utf32Name.getAddress());       
        assertEquals("utf-32 name name", "\uD834\uDF01\uD834\uDF46", utf32Name.getPersonal());       
        Address[] addresses = new Address[] {
                noName1, noName2, simpleName, dquoteName, quotedName, utf16Name, utf32Name,
        };
        String line = Address.toHeader(addresses);
        assertEquals("toHeader() multi",
                "noname1@dom1.com, "
                + "noname2@dom2.com, "
                + "simple name <address3@dom3.org>, "
                + "\"name,4\" <address4@dom4.org>, "
                + "\"big \\\"G\\\"\" <bigG@dom5.net>, "
                + "=?UTF-8?B?5pel5pys6Kqe?= <address6@co.jp>, "
                + "=?UTF-8?B?8J2MgfCdjYY=?= <address8@ne.jp>",
                line);
    }
    public void testToFriendlySingle() {        
        assertEquals("personal1", mAddress1.toFriendly());
        assertEquals("address2", mAddress2.toFriendly());
        assertEquals("address3", mAddress3.toFriendly());
    }
    public void testToFriendlyArray() {        
        Address[] list1 = null;
        Address[] list2 = new Address[0];
        Address[] list3 = new Address[] { mAddress1 };
        Address[] list4 = new Address[] { mAddress1, mAddress2, mAddress3 };
        assertEquals(null, Address.toFriendly(list1));
        assertEquals(null, Address.toFriendly(list2));
        assertEquals("personal1", Address.toFriendly(list3));
        assertEquals("personal1,address2,address3", Address.toFriendly(list4));
    }
    public void testEmptyPack() {
        String result;
        result = Address.pack(null);
        assertNull("packing null", result);
        result = Address.pack(new Address[] { });
        assertEquals("packing empty array", "", result);
    }
    public void testEmptyUnpack() {
        Address[] result;
        result = Address.unpack(null);
        assertTrue("unpacking null address", result != null && result.length == 0);
        result = Address.unpack("");
        assertTrue("unpacking zero-length", result != null && result.length == 0);
    }
    private static boolean addressEquals(Address a1, Address a2) {
        if (!a1.equals(a2)) {
            return false;
        }
        final String displayName1 = a1.getPersonal();
        final String displayName2 = a2.getPersonal();
        if (displayName1 == null) {
            return displayName2 == null;
        } else {
            return displayName1.equals(displayName2);
        }
    }
    private static boolean addressArrayEquals(Address[] array1, Address[] array2) {
        if (array1.length != array2.length) {
            return false;
        }
        for (int i = array1.length - 1; i >= 0; --i) {
            if (!addressEquals(array1[i], array2[i])) {
                return false;
            }
        }
        return true;
    }
    public void testPackUnpack() {
        for (Address[] list : PACK_CASES) {
            String packed = Address.pack(list);
            assertTrue(packed, addressArrayEquals(list, Address.unpack(packed)));
        }
    }
    public void testLegacyPackUnpack() {
        for (Address[] list : PACK_CASES) {
            String packed = legacyPack(list);
            assertTrue(packed, addressArrayEquals(list, Address.legacyUnpack(packed)));
        }
    }
    public void testUnpackToString() {
        assertNull(Address.unpackToString(null));
        assertNull(Address.unpackToString(""));
        for (Address[] list : PACK_CASES) {
            String packed = Address.pack(list);
            String s1 = Address.unpackToString(packed);
            String s2 = Address.toString(Address.unpack(packed));
            assertEquals(s2, s2, s1);
        }
    }
    public void testParseAndPack() {
        String s1 = Address.parseAndPack(MULTI_ADDRESSES_LIST);
        String s2 = Address.pack(Address.parse(MULTI_ADDRESSES_LIST));
        assertEquals(s2, s1);
    }
    public void testSinglePack() {
        Address[] addrArray = new Address[1];
        for (Address address : new Address[]{PACK_ADDR_1, PACK_ADDR_2, PACK_ADDR_3}) {
            String packed1 = address.pack();
            addrArray[0] = address;
            String packed2 = Address.pack(addrArray);
            assertEquals(packed1, packed2);
        }
    }
    public void testUnpackFirst() {
        assertNull(Address.unpackFirst(null));
        assertNull(Address.unpackFirst(""));
        for (Address[] list : PACK_CASES) {
            String packed = Address.pack(list);
            Address[] array = Address.unpack(packed);
            Address first = Address.unpackFirst(packed);
            assertTrue(packed, addressEquals(array[0], first));
        }
    }
    public void testIsValidAddress() {
        String notValid[] = {"", "foo", "john@", "x@y", "x@y.", "foo.com"};
        String valid[] = {"x@y.z", "john@gmail.com", "a@b.c.d"};
        for (String address : notValid) {
            assertTrue(address, !Address.isValidAddress(address));
        }
        for (String address : valid) {
            assertTrue(address, Address.isValidAddress(address));
        }
        assertTrue("Empty address list is valid", Address.isAllValid(""));
    }
    private static String legacyPack(Address[] addresses) {
        if (addresses == null) {
            return null;
        } else if (addresses.length == 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0, count = addresses.length; i < count; i++) {
            Address address = addresses[i];
            try {
                sb.append(URLEncoder.encode(address.getAddress(), "UTF-8"));
                if (address.getPersonal() != null) {
                    sb.append(';');
                    sb.append(URLEncoder.encode(address.getPersonal(), "UTF-8"));
                }
                if (i < count - 1) {
                    sb.append(',');
                }
            }
            catch (UnsupportedEncodingException uee) {
                return null;
            }
        }
        return sb.toString();
    }
}
