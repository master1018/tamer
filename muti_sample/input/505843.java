public class VCardJapanizationTests extends VCardTestsBase {
    private void testNameUtf8Common(int vcardType) {
        mVerifier.initForExportTest(vcardType);
        ContactEntry entry = mVerifier.addInputEntry();
        entry.addContentValues(StructuredName.CONTENT_ITEM_TYPE)
                .put(StructuredName.FAMILY_NAME, "\u3075\u308B\u3069")
                .put(StructuredName.GIVEN_NAME, "\u3091\u308A\u304B")
                .put(StructuredName.MIDDLE_NAME, "B")
                .put(StructuredName.PREFIX, "Dr.")
                .put(StructuredName.SUFFIX, "Ph.D");
        ContentValues contentValues =
            (VCardConfig.isV30(vcardType) ? null : mContentValuesForQPAndUtf8);
        mVerifier.addPropertyNodesVerifierElem()
                .addExpectedNode("FN", "Dr. \u3075\u308B\u3069 B \u3091\u308A\u304B Ph.D",
                        contentValues)
                .addExpectedNode("N", "\u3075\u308B\u3069;\u3091\u308A\u304B;B;Dr.;Ph.D",
                        Arrays.asList(
                                "\u3075\u308B\u3069", "\u3091\u308A\u304B", "B", "Dr.", "Ph.D"),
                                null, contentValues, null, null);
    }
    public void testNameUtf8V21() {
        testNameUtf8Common(VCardConfig.VCARD_TYPE_V21_JAPANESE_UTF8);
    }
    public void testNameUtf8V30() {
        testNameUtf8Common(VCardConfig.VCARD_TYPE_V30_JAPANESE_UTF8);
    }
    public void testNameShiftJis() {
        mVerifier.initForExportTest(VCardConfig.VCARD_TYPE_V30_JAPANESE_SJIS);
        ContactEntry entry = mVerifier.addInputEntry();
        entry.addContentValues(StructuredName.CONTENT_ITEM_TYPE)
                .put(StructuredName.FAMILY_NAME, "\u3075\u308B\u3069")
                .put(StructuredName.GIVEN_NAME, "\u3091\u308A\u304B")
                .put(StructuredName.MIDDLE_NAME, "B")
                .put(StructuredName.PREFIX, "Dr.")
                .put(StructuredName.SUFFIX, "Ph.D");
        mVerifier.addPropertyNodesVerifierElem()
                .addExpectedNode("FN", "Dr. \u3075\u308B\u3069 B \u3091\u308A\u304B Ph.D",
                        mContentValuesForSJis)
                .addExpectedNode("N", "\u3075\u308B\u3069;\u3091\u308A\u304B;B;Dr.;Ph.D",
                        Arrays.asList(
                                "\u3075\u308B\u3069", "\u3091\u308A\u304B", "B", "Dr.", "Ph.D"),
                                null, mContentValuesForSJis, null, null);
    }
    public void testNameDoCoMo() {
        mVerifier.initForExportTest(VCardConfig.VCARD_TYPE_DOCOMO);
        ContactEntry entry = mVerifier.addInputEntry();
        entry.addContentValues(StructuredName.CONTENT_ITEM_TYPE)
                .put(StructuredName.FAMILY_NAME, "\u3075\u308B\u3069")
                .put(StructuredName.GIVEN_NAME, "\u3091\u308A\u304B")
                .put(StructuredName.MIDDLE_NAME, "B")
                .put(StructuredName.PREFIX, "Dr.")
                .put(StructuredName.SUFFIX, "Ph.D");
        final String fullName = "Dr. \u3075\u308B\u3069 B \u3091\u308A\u304B Ph.D";
        mVerifier.addPropertyNodesVerifierElem()
                .addExpectedNode("N", fullName + ";;;;",
                        Arrays.asList(fullName, "", "", "", ""),
                        null, mContentValuesForSJis, null, null)
                .addExpectedNode("FN", fullName, mContentValuesForSJis)
                .addExpectedNode("SOUND", ";;;;", new TypeSet("X-IRMC-N"))
                .addExpectedNode("TEL", "", new TypeSet("HOME"))
                .addExpectedNode("EMAIL", "", new TypeSet("HOME"))
                .addExpectedNode("ADR", "", new TypeSet("HOME"))
                .addExpectedNode("X-CLASS", "PUBLIC")
                .addExpectedNode("X-REDUCTION", "")
                .addExpectedNode("X-NO", "")
                .addExpectedNode("X-DCM-HMN-MODE", "");
    }
    private void testPhoneticNameCommon(int vcardType) {
        mVerifier.initForExportTest(vcardType);
        ContactEntry entry = mVerifier.addInputEntry();
        entry.addContentValues(StructuredName.CONTENT_ITEM_TYPE)
                .put(StructuredName.PHONETIC_FAMILY_NAME, "\u3084\u307E\u3060")
                .put(StructuredName.PHONETIC_MIDDLE_NAME, "\u30DF\u30C9\u30EB\u30CD\u30FC\u30E0")
                .put(StructuredName.PHONETIC_GIVEN_NAME, "\u305F\u308D\u3046");
        final ContentValues contentValues =
            (VCardConfig.usesShiftJis(vcardType) ?
                    (VCardConfig.isV30(vcardType) ? mContentValuesForSJis :
                            mContentValuesForQPAndSJis) :
                    (VCardConfig.isV30(vcardType) ? null : mContentValuesForQPAndUtf8));
        PropertyNodesVerifierElem elem = mVerifier.addPropertyNodesVerifierElemWithEmptyName();
        elem.addExpectedNode("X-PHONETIC-LAST-NAME", "\u3084\u307E\u3060",
                        contentValues)
                .addExpectedNode("X-PHONETIC-MIDDLE-NAME",
                        "\u30DF\u30C9\u30EB\u30CD\u30FC\u30E0",
                        contentValues)
                .addExpectedNode("X-PHONETIC-FIRST-NAME", "\u305F\u308D\u3046",
                        contentValues);
        if (VCardConfig.isV30(vcardType)) {
            elem.addExpectedNode("SORT-STRING",
                    "\u3084\u307E\u3060 \u30DF\u30C9\u30EB\u30CD\u30FC\u30E0 \u305F\u308D\u3046",
                    contentValues);
        }
        ContentValuesBuilder builder = mVerifier.addContentValuesVerifierElem()
                .addExpected(StructuredName.CONTENT_ITEM_TYPE);
        builder.put(StructuredName.PHONETIC_FAMILY_NAME, "\u3084\u307E\u3060")
                .put(StructuredName.PHONETIC_MIDDLE_NAME, "\u30DF\u30C9\u30EB\u30CD\u30FC\u30E0")
                .put(StructuredName.PHONETIC_GIVEN_NAME, "\u305F\u308D\u3046")
                .put(StructuredName.DISPLAY_NAME,
                        "\u3084\u307E\u3060 \u30DF\u30C9\u30EB\u30CD\u30FC\u30E0 " +
                        "\u305F\u308D\u3046");
    }
    public void testPhoneticNameForJapaneseV21Utf8() {
        testPhoneticNameCommon(VCardConfig.VCARD_TYPE_V21_JAPANESE_UTF8);
    }
    public void testPhoneticNameForJapaneseV21Sjis() {
        testPhoneticNameCommon(VCardConfig.VCARD_TYPE_V21_JAPANESE_SJIS);
    }
    public void testPhoneticNameForJapaneseV30Utf8() {
        testPhoneticNameCommon(VCardConfig.VCARD_TYPE_V30_JAPANESE_UTF8);
    }
    public void testPhoneticNameForJapaneseV30SJis() {
        testPhoneticNameCommon(VCardConfig.VCARD_TYPE_V30_JAPANESE_SJIS);
    }
    public void testPhoneticNameForMobileV21_1() {
        mVerifier.initForExportTest(VCardConfig.VCARD_TYPE_V21_JAPANESE_MOBILE);
        ContactEntry entry = mVerifier.addInputEntry();
        entry.addContentValues(StructuredName.CONTENT_ITEM_TYPE)
                .put(StructuredName.PHONETIC_FAMILY_NAME, "\u3084\u307E\u3060")
                .put(StructuredName.PHONETIC_MIDDLE_NAME, "\u30DF\u30C9\u30EB\u30CD\u30FC\u30E0")
                .put(StructuredName.PHONETIC_GIVEN_NAME, "\u305F\u308D\u3046");
        mVerifier.addPropertyNodesVerifierElem()
                .addExpectedNode("SOUND",
                        "\uFF94\uFF8F\uFF80\uFF9E \uFF90\uFF84\uFF9E\uFF99\uFF88\uFF70\uFF91 " +
                        "\uFF80\uFF9B\uFF73;;;;",
                        mContentValuesForSJis, new TypeSet("X-IRMC-N"));
        ContentValuesBuilder builder = mVerifier.addContentValuesVerifierElem()
                .addExpected(StructuredName.CONTENT_ITEM_TYPE);
        builder.put(StructuredName.PHONETIC_FAMILY_NAME, "\uFF94\uFF8F\uFF80\uFF9E")
                .put(StructuredName.PHONETIC_MIDDLE_NAME,
                        "\uFF90\uFF84\uFF9E\uFF99\uFF88\uFF70\uFF91")
                .put(StructuredName.PHONETIC_GIVEN_NAME, "\uFF80\uFF9B\uFF73")
                .put(StructuredName.DISPLAY_NAME,
                        "\uFF94\uFF8F\uFF80\uFF9E \uFF90\uFF84\uFF9E\uFF99\uFF88\uFF70\uFF91 " +
                        "\uFF80\uFF9B\uFF73");
    }
    public void testPhoneticNameForMobileV21_2() {
        mVerifier.initForExportTest(VCardConfig.VCARD_TYPE_V21_JAPANESE_MOBILE);
        ContactEntry entry = mVerifier.addInputEntry();
        entry.addContentValues(StructuredName.CONTENT_ITEM_TYPE)
                .put(StructuredName.PHONETIC_FAMILY_NAME, "\u3084\u307E\u3060")
                .put(StructuredName.PHONETIC_GIVEN_NAME, "\u305F\u308D\u3046");
        mVerifier.addPropertyNodesVerifierElem()
                .addExpectedNode("SOUND", "\uFF94\uFF8F\uFF80\uFF9E \uFF80\uFF9B\uFF73;;;;",
                                mContentValuesForSJis, new TypeSet("X-IRMC-N"));
        ContentValuesBuilder builder = mVerifier.addContentValuesVerifierElem()
                .addExpected(StructuredName.CONTENT_ITEM_TYPE);
        builder.put(StructuredName.PHONETIC_FAMILY_NAME, "\uFF94\uFF8F\uFF80\uFF9E")
                .put(StructuredName.PHONETIC_GIVEN_NAME, "\uFF80\uFF9B\uFF73")
                .put(StructuredName.DISPLAY_NAME, "\uFF94\uFF8F\uFF80\uFF9E \uFF80\uFF9B\uFF73");
    }
    private void testPostalAddressWithJapaneseCommon(int vcardType) {
        mVerifier.initForExportTest(vcardType);
        ContactEntry entry = mVerifier.addInputEntry();
        entry.addContentValues(StructuredPostal.CONTENT_ITEM_TYPE)
                .put(StructuredPostal.POBOX, "\u79C1\u66F8\u7BB107")
                .put(StructuredPostal.STREET, "\u96DB\u898B\u6CA2\u6751")
                .put(StructuredPostal.CITY, "\u9E7F\u9AA8\u5E02")
                .put(StructuredPostal.REGION, "\u00D7\u00D7\u770C")
                .put(StructuredPostal.POSTCODE, "494-1313")
                .put(StructuredPostal.COUNTRY, "\u65E5\u672C")
                .put(StructuredPostal.FORMATTED_ADDRESS,
                        "\u3053\u3093\u306A\u3068\u3053\u308D\u3092\u898B"
                        + "\u308B\u306A\u3093\u3066\u6687\u4EBA\u3067\u3059\u304B\uFF1F")
                .put(StructuredPostal.TYPE, StructuredPostal.TYPE_CUSTOM)
                .put(StructuredPostal.LABEL, "\u304A\u3082\u3061\u304B\u3048\u308A");
        ContentValues contentValues = (VCardConfig.usesShiftJis(vcardType) ?
                (VCardConfig.isV30(vcardType) ? mContentValuesForSJis :
                    mContentValuesForQPAndSJis) :
                (VCardConfig.isV30(vcardType) ? mContentValuesForUtf8 :
                    mContentValuesForQPAndUtf8));
        PropertyNodesVerifierElem elem = mVerifier.addPropertyNodesVerifierElemWithEmptyName();
        elem.addExpectedNode("ADR", Arrays.asList("\u79C1\u66F8\u7BB107",
                "", "\u96DB\u898B\u6CA2\u6751", "\u9E7F\u9AA8\u5E02", "\u00D7\u00D7\u770C",
                "494-1313", "\u65E5\u672C"),
                contentValues);
        mVerifier.addContentValuesVerifierElem().addExpected(StructuredPostal.CONTENT_ITEM_TYPE)
                .put(StructuredPostal.POBOX, "\u79C1\u66F8\u7BB107")
                .put(StructuredPostal.STREET, "\u96DB\u898B\u6CA2\u6751")
                .put(StructuredPostal.CITY, "\u9E7F\u9AA8\u5E02")
                .put(StructuredPostal.REGION, "\u00D7\u00D7\u770C")
                .put(StructuredPostal.POSTCODE, "494-1313")
                .put(StructuredPostal.COUNTRY, "\u65E5\u672C")
                .put(StructuredPostal.FORMATTED_ADDRESS,
                        "\u65E5\u672C 494-1313 \u00D7\u00D7\u770C \u9E7F\u9AA8\u5E02 " +
                        "\u96DB\u898B\u6CA2\u6751 " + "\u79C1\u66F8\u7BB107")
                .put(StructuredPostal.TYPE, StructuredPostal.TYPE_HOME);
    }
    public void testPostalAddresswithJapaneseV21() {
        testPostalAddressWithJapaneseCommon(VCardConfig.VCARD_TYPE_V21_JAPANESE_SJIS);
    }
    public void testPostalAdrressForDoCoMo_1() {
        mVerifier.initForExportTest(VCardConfig.VCARD_TYPE_DOCOMO);
        ContactEntry entry = mVerifier.addInputEntry();
        entry.addContentValues(StructuredPostal.CONTENT_ITEM_TYPE)
                .put(StructuredPostal.TYPE, StructuredPostal.TYPE_WORK)
                .put(StructuredPostal.POBOX, "1");
        entry.addContentValues(StructuredPostal.CONTENT_ITEM_TYPE)
                .put(StructuredPostal.TYPE, StructuredPostal.TYPE_OTHER)
                .put(StructuredPostal.POBOX, "2");
        entry.addContentValues(StructuredPostal.CONTENT_ITEM_TYPE)
                .put(StructuredPostal.TYPE, StructuredPostal.TYPE_HOME)
                .put(StructuredPostal.POBOX, "3");
        entry.addContentValues(StructuredPostal.CONTENT_ITEM_TYPE)
                .put(StructuredPostal.TYPE, StructuredPostal.TYPE_CUSTOM)
                .put(StructuredPostal.LABEL, "custom")
                .put(StructuredPostal.POBOX, "4");
        mVerifier.addPropertyNodesVerifierElemWithEmptyName()
                .addExpectedNode("TEL", "", new TypeSet("HOME"))
                .addExpectedNode("EMAIL", "", new TypeSet("HOME"))
                .addExpectedNode("X-CLASS", "PUBLIC")
                .addExpectedNode("X-REDUCTION", "")
                .addExpectedNode("X-NO", "")
                .addExpectedNode("X-DCM-HMN-MODE", "")
                .addExpectedNode("ADR",
                        Arrays.asList("3", "", "", "", "", "", ""), new TypeSet("HOME"));
    }
    public void testPostalAdrressForDoCoMo_2() {
        mVerifier.initForExportTest(VCardConfig.VCARD_TYPE_DOCOMO);
        ContactEntry entry = mVerifier.addInputEntry();
        entry.addContentValues(StructuredPostal.CONTENT_ITEM_TYPE)
                .put(StructuredPostal.TYPE, StructuredPostal.TYPE_OTHER)
                .put(StructuredPostal.POBOX, "1");
        entry.addContentValues(StructuredPostal.CONTENT_ITEM_TYPE)
                .put(StructuredPostal.TYPE, StructuredPostal.TYPE_WORK)
                .put(StructuredPostal.POBOX, "2");
        entry.addContentValues(StructuredPostal.CONTENT_ITEM_TYPE)
                .put(StructuredPostal.TYPE, StructuredPostal.TYPE_CUSTOM)
                .put(StructuredPostal.LABEL, "custom")
                .put(StructuredPostal.POBOX, "3");
        mVerifier.addPropertyNodesVerifierElemWithEmptyName()
                .addExpectedNode("TEL", "", new TypeSet("HOME"))
                .addExpectedNode("EMAIL", "", new TypeSet("HOME"))
                .addExpectedNode("X-CLASS", "PUBLIC")
                .addExpectedNode("X-REDUCTION", "")
                .addExpectedNode("X-NO", "")
                .addExpectedNode("X-DCM-HMN-MODE", "")
                .addExpectedNode("ADR",
                        Arrays.asList("2", "", "", "", "", "", ""), new TypeSet("WORK"));
    }
    public void testPostalAdrressForDoCoMo_3() {
        mVerifier.initForExportTest(VCardConfig.VCARD_TYPE_DOCOMO);
        ContactEntry entry = mVerifier.addInputEntry();
        entry.addContentValues(StructuredPostal.CONTENT_ITEM_TYPE)
                .put(StructuredPostal.TYPE, StructuredPostal.TYPE_CUSTOM)
                .put(StructuredPostal.LABEL, "custom1")
                .put(StructuredPostal.POBOX, "1");
        entry.addContentValues(StructuredPostal.CONTENT_ITEM_TYPE)
                .put(StructuredPostal.TYPE, StructuredPostal.TYPE_OTHER)
                .put(StructuredPostal.POBOX, "2");
        entry.addContentValues(StructuredPostal.CONTENT_ITEM_TYPE)
                .put(StructuredPostal.TYPE, StructuredPostal.TYPE_CUSTOM)
                .put(StructuredPostal.LABEL, "custom2")
                .put(StructuredPostal.POBOX, "3");
        mVerifier.addPropertyNodesVerifierElemWithEmptyName()
                .addExpectedNode("TEL", "", new TypeSet("HOME"))
                .addExpectedNode("EMAIL", "", new TypeSet("HOME"))
                .addExpectedNode("X-CLASS", "PUBLIC")
                .addExpectedNode("X-REDUCTION", "")
                .addExpectedNode("X-NO", "")
                .addExpectedNode("X-DCM-HMN-MODE", "")
                .addExpectedNode("ADR", Arrays.asList("2", "", "", "", "", "", ""));
    }
    public void testPostalAdrressForDoCoMo_4() {
        mVerifier.initForExportTest(VCardConfig.VCARD_TYPE_DOCOMO);
        ContactEntry entry = mVerifier.addInputEntry();
        entry.addContentValues(StructuredPostal.CONTENT_ITEM_TYPE)
                .put(StructuredPostal.POBOX, "1");
        entry.addContentValues(StructuredPostal.CONTENT_ITEM_TYPE)
                .put(StructuredPostal.TYPE, StructuredPostal.TYPE_OTHER)
                .put(StructuredPostal.POBOX, "2");
        entry.addContentValues(StructuredPostal.CONTENT_ITEM_TYPE)
                .put(StructuredPostal.TYPE, StructuredPostal.TYPE_HOME)
                .put(StructuredPostal.POBOX, "3");
        entry.addContentValues(StructuredPostal.CONTENT_ITEM_TYPE)
                .put(StructuredPostal.TYPE, StructuredPostal.TYPE_WORK)
                .put(StructuredPostal.POBOX, "4");
        entry.addContentValues(StructuredPostal.CONTENT_ITEM_TYPE)
                .put(StructuredPostal.POBOX, "5");
        mVerifier.addPropertyNodesVerifierElemWithEmptyName()
                .addExpectedNode("TEL", "", new TypeSet("HOME"))
                .addExpectedNode("EMAIL", "", new TypeSet("HOME"))
                .addExpectedNode("X-CLASS", "PUBLIC")
                .addExpectedNode("X-REDUCTION", "")
                .addExpectedNode("X-NO", "")
                .addExpectedNode("X-DCM-HMN-MODE", "")
                .addExpectedNode("ADR",
                        Arrays.asList("3", "", "", "", "", "", ""), new TypeSet("HOME"));
    }
    private void testJapanesePhoneNumberCommon(int vcardType) {
        mVerifier.initForExportTest(vcardType);
        ContactEntry entry = mVerifier.addInputEntry();
        entry.addContentValues(Phone.CONTENT_ITEM_TYPE)
                .put(Phone.NUMBER, "0312341234")
                .put(Phone.TYPE, Phone.TYPE_HOME);
        entry.addContentValues(Phone.CONTENT_ITEM_TYPE)
                .put(Phone.NUMBER, "09012341234")
                .put(Phone.TYPE, Phone.TYPE_MOBILE);
        mVerifier.addPropertyNodesVerifierElemWithEmptyName()
                .addExpectedNode("TEL", "03-1234-1234", new TypeSet("HOME"))
                .addExpectedNode("TEL", "090-1234-1234", new TypeSet("CELL"));
    }
    public void testJapanesePhoneNumberV21_1() {
        testJapanesePhoneNumberCommon(VCardConfig.VCARD_TYPE_V21_JAPANESE_UTF8);
    }
    public void testJapanesePhoneNumberV30() {
        testJapanesePhoneNumberCommon(VCardConfig.VCARD_TYPE_V30_JAPANESE_UTF8);
    }
    public void testJapanesePhoneNumberDoCoMo() {
        mVerifier.initForExportTest(VCardConfig.VCARD_TYPE_DOCOMO);
        ContactEntry entry = mVerifier.addInputEntry();
        entry.addContentValues(Phone.CONTENT_ITEM_TYPE)
                .put(Phone.NUMBER, "0312341234")
                .put(Phone.TYPE, Phone.TYPE_HOME);
        entry.addContentValues(Phone.CONTENT_ITEM_TYPE)
                .put(Phone.NUMBER, "09012341234")
                .put(Phone.TYPE, Phone.TYPE_MOBILE);
        mVerifier.addPropertyNodesVerifierElemWithEmptyName()
                .addExpectedNode("EMAIL", "", new TypeSet("HOME"))
                .addExpectedNode("X-CLASS", "PUBLIC")
                .addExpectedNode("X-REDUCTION", "")
                .addExpectedNode("X-NO", "")
                .addExpectedNode("X-DCM-HMN-MODE", "")
                .addExpectedNode("ADR", "", new TypeSet("HOME"))
                .addExpectedNode("TEL", "03-1234-1234", new TypeSet("HOME"))
                .addExpectedNode("TEL", "090-1234-1234", new TypeSet("CELL"));
    }
    public void testNoteDoCoMo() {
        mVerifier.initForExportTest(VCardConfig.VCARD_TYPE_DOCOMO);
        ContactEntry entry = mVerifier.addInputEntry();
        entry.addContentValues(Note.CONTENT_ITEM_TYPE)
                .put(Note.NOTE, "note1");
        entry.addContentValues(Note.CONTENT_ITEM_TYPE)
                .put(Note.NOTE, "note2");
        entry.addContentValues(Note.CONTENT_ITEM_TYPE)
                .put(Note.NOTE, "note3");
        mVerifier.addPropertyNodesVerifierElemWithEmptyName()
                .addExpectedNode("TEL", "", new TypeSet("HOME"))
                .addExpectedNode("EMAIL", "", new TypeSet("HOME"))
                .addExpectedNode("X-CLASS", "PUBLIC")
                .addExpectedNode("X-REDUCTION", "")
                .addExpectedNode("X-NO", "")
                .addExpectedNode("X-DCM-HMN-MODE", "")
                .addExpectedNode("ADR", "", new TypeSet("HOME"))
                .addExpectedNode("NOTE", "note1\nnote2\nnote3", mContentValuesForQP);
    }
    public void testAndroidCustomV21() {
        mVerifier.initForExportTest(VCardConfig.VCARD_TYPE_V21_GENERIC_UTF8);
        mVerifier.addInputEntry().addContentValues(Nickname.CONTENT_ITEM_TYPE)
                .put(Nickname.NAME, "\u304D\u3083\u30FC\u30A8\u30C3\u30C1\u30FC");
        mVerifier.addPropertyNodesVerifierElemWithEmptyName()
                .addExpectedNode("X-ANDROID-CUSTOM",
                        Arrays.asList(Nickname.CONTENT_ITEM_TYPE,
                                "\u304D\u3083\u30FC\u30A8\u30C3\u30C1\u30FC",
                                "", "", "", "", "", "", "", "", "", "", "", "", "", ""),
                        mContentValuesForQPAndUtf8);
    }
}
