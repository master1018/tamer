public class GenerateCurrencyData {
    private static DataOutputStream out;
    private static Properties currencyData;
    private static String formatVersion;
    private static String dataVersion;
    private static String validCurrencyCodes;
    private static String currenciesWith0MinorUnitDecimals;
    private static String currenciesWith1MinorUnitDecimal;
    private static String currenciesWith3MinorUnitDecimal;
    private static String currenciesWithMinorUnitsUndefined;
    private static final int MAGIC_NUMBER = 0x43757244;
    private static final int A_TO_Z = ('Z' - 'A') + 1;
    private static final int INVALID_COUNTRY_ENTRY = 0x007F;
    private static final int COUNTRY_WITHOUT_CURRENCY_ENTRY = 0x0080;
    private static final int SIMPLE_CASE_COUNTRY_MASK = 0x0000;
    private static final int SIMPLE_CASE_COUNTRY_FINAL_CHAR_MASK = 0x001F;
    private static final int SIMPLE_CASE_COUNTRY_DEFAULT_DIGITS_MASK = 0x0060;
    private static final int SIMPLE_CASE_COUNTRY_DEFAULT_DIGITS_SHIFT = 5;
    private static final int SPECIAL_CASE_COUNTRY_MASK = 0x0080;
    private static final int SPECIAL_CASE_COUNTRY_INDEX_MASK = 0x001F;
    private static final int SPECIAL_CASE_COUNTRY_INDEX_DELTA = 1;
    private static final int COUNTRY_TYPE_MASK = SIMPLE_CASE_COUNTRY_MASK | SPECIAL_CASE_COUNTRY_MASK;
    private static final int NUMERIC_CODE_MASK = 0x0003FF00;
    private static final int NUMERIC_CODE_SHIFT = 8;
    private static int[] mainTable = new int[A_TO_Z * A_TO_Z];
    private static final int maxSpecialCases = 30;
    private static int specialCaseCount = 0;
    private static long[] specialCaseCutOverTimes = new long[maxSpecialCases];
    private static String[] specialCaseOldCurrencies = new String[maxSpecialCases];
    private static String[] specialCaseNewCurrencies = new String[maxSpecialCases];
    private static int[] specialCaseOldCurrenciesDefaultFractionDigits = new int[maxSpecialCases];
    private static int[] specialCaseNewCurrenciesDefaultFractionDigits = new int[maxSpecialCases];
    private static int[] specialCaseOldCurrenciesNumericCode = new int[maxSpecialCases];
    private static int[] specialCaseNewCurrenciesNumericCode = new int[maxSpecialCases];
    private static final int maxOtherCurrencies = 65;
    private static int otherCurrenciesCount = 0;
    private static StringBuffer otherCurrencies = new StringBuffer();
    private static int[] otherCurrenciesDefaultFractionDigits = new int[maxOtherCurrencies];
    private static int[] otherCurrenciesNumericCode= new int[maxOtherCurrencies];
    private static SimpleDateFormat format;
    public static void main(String[] args) {
        if ( args.length == 2 && args[0].equals("-o") ) {
            try {
                out = new DataOutputStream(new FileOutputStream(args[1]));
            } catch ( FileNotFoundException e ) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace(System.err);
                System.exit(1);
            }
        } else {
            System.err.println("Error: Illegal arg count");
            System.exit(1);
        }
        format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        format.setLenient(false);
        try {
            readInput();
            buildMainAndSpecialCaseTables();
            buildOtherTables();
            writeOutput();
            out.flush();
            out.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
    private static void readInput() throws IOException {
        currencyData = new Properties();
        currencyData.load(System.in);
        formatVersion = (String) currencyData.get("formatVersion");
        dataVersion = (String) currencyData.get("dataVersion");
        validCurrencyCodes = (String) currencyData.get("all");
        currenciesWith0MinorUnitDecimals  = (String) currencyData.get("minor0");
        currenciesWith1MinorUnitDecimal  = (String) currencyData.get("minor1");
        currenciesWith3MinorUnitDecimal  = (String) currencyData.get("minor3");
        currenciesWithMinorUnitsUndefined  = (String) currencyData.get("minorUndefined");
        if (formatVersion == null ||
                dataVersion == null ||
                validCurrencyCodes == null ||
                currenciesWith0MinorUnitDecimals == null ||
                currenciesWith1MinorUnitDecimal == null ||
                currenciesWith3MinorUnitDecimal == null ||
                currenciesWithMinorUnitsUndefined == null) {
            throw new NullPointerException("not all required data is defined in input");
        }
    }
    private static void buildMainAndSpecialCaseTables() throws Exception {
        for (int first = 0; first < A_TO_Z; first++) {
            for (int second = 0; second < A_TO_Z; second++) {
                char firstChar = (char) ('A' + first);
                char secondChar = (char) ('A' + second);
                String countryCode = (new StringBuffer()).append(firstChar).append(secondChar).toString();
                String currencyInfo = (String) currencyData.get(countryCode);
                int tableEntry = 0;
                if (currencyInfo == null) {
                    tableEntry = INVALID_COUNTRY_ENTRY;
                } else {
                    int length = currencyInfo.length();
                    if (length == 0) {
                       tableEntry = COUNTRY_WITHOUT_CURRENCY_ENTRY;
                    } else if (length == 3) {
                        if (currencyInfo.charAt(0) == firstChar && currencyInfo.charAt(1) == secondChar) {
                            checkCurrencyCode(currencyInfo);
                            int digits = getDefaultFractionDigits(currencyInfo);
                            if (digits < 0 || digits > 3) {
                                throw new RuntimeException("fraction digits out of range for " + currencyInfo);
                            }
                            int numericCode= getNumericCode(currencyInfo);
                            if (numericCode < 0 || numericCode >= 1000 ) {
                                throw new RuntimeException("numeric code out of range for " + currencyInfo);
                            }
                            tableEntry = SIMPLE_CASE_COUNTRY_MASK
                                    | (currencyInfo.charAt(2) - 'A')
                                    | (digits << SIMPLE_CASE_COUNTRY_DEFAULT_DIGITS_SHIFT)
                                    | (numericCode << NUMERIC_CODE_SHIFT);
                        } else {
                            tableEntry = SPECIAL_CASE_COUNTRY_MASK | (makeSpecialCaseEntry(currencyInfo) + SPECIAL_CASE_COUNTRY_INDEX_DELTA);
                        }
                    } else {
                        tableEntry = SPECIAL_CASE_COUNTRY_MASK | (makeSpecialCaseEntry(currencyInfo) + SPECIAL_CASE_COUNTRY_INDEX_DELTA);
                    }
                }
                mainTable[first * A_TO_Z + second] = tableEntry;
            }
        }
    }
    private static int getDefaultFractionDigits(String currencyCode) {
        if (currenciesWith0MinorUnitDecimals.indexOf(currencyCode) != -1) {
            return 0;
        } else if (currenciesWith1MinorUnitDecimal.indexOf(currencyCode) != -1) {
            return 1;
        } else if (currenciesWith3MinorUnitDecimal.indexOf(currencyCode) != -1) {
            return 3;
        } else if (currenciesWithMinorUnitsUndefined.indexOf(currencyCode) != -1) {
            return -1;
        } else {
            return 2;
        }
    }
    private static int getNumericCode(String currencyCode) {
        int index = validCurrencyCodes.indexOf(currencyCode);
        String numericCode = validCurrencyCodes.substring(index + 3, index + 6);
        return Integer.parseInt(numericCode);
    }
    static HashMap specialCaseMap = new HashMap();
    private static int makeSpecialCaseEntry(String currencyInfo) throws Exception {
        Integer oldEntry = (Integer) specialCaseMap.get(currencyInfo);
        if (oldEntry != null) {
            return oldEntry.intValue();
        }
        if (specialCaseCount == maxSpecialCases) {
            throw new RuntimeException("too many special cases");
        }
        if (currencyInfo.length() == 3) {
            checkCurrencyCode(currencyInfo);
            specialCaseCutOverTimes[specialCaseCount] = Long.MAX_VALUE;
            specialCaseOldCurrencies[specialCaseCount] = currencyInfo;
            specialCaseOldCurrenciesDefaultFractionDigits[specialCaseCount] = getDefaultFractionDigits(currencyInfo);
            specialCaseOldCurrenciesNumericCode[specialCaseCount] = getNumericCode(currencyInfo);
            specialCaseNewCurrencies[specialCaseCount] = null;
            specialCaseNewCurrenciesDefaultFractionDigits[specialCaseCount] = 0;
            specialCaseNewCurrenciesNumericCode[specialCaseCount] = 0;
        } else {
            int length = currencyInfo.length();
            if (currencyInfo.charAt(3) != ';' ||
                    currencyInfo.charAt(length - 4) != ';') {
                throw new RuntimeException("invalid currency info: " + currencyInfo);
            }
            String oldCurrency = currencyInfo.substring(0, 3);
            String newCurrency = currencyInfo.substring(length - 3, length);
            checkCurrencyCode(oldCurrency);
            checkCurrencyCode(newCurrency);
            String timeString = currencyInfo.substring(4, length - 4);
            long time = format.parse(timeString).getTime();
            if (Math.abs(time - System.currentTimeMillis()) > ((long) 10) * 365 * 24 * 60 * 60 * 1000) {
                throw new RuntimeException("time is more than 10 years from present: " + time);
            }
            specialCaseCutOverTimes[specialCaseCount] = time;
            specialCaseOldCurrencies[specialCaseCount] = oldCurrency;
            specialCaseOldCurrenciesDefaultFractionDigits[specialCaseCount] = getDefaultFractionDigits(oldCurrency);
            specialCaseOldCurrenciesNumericCode[specialCaseCount] = getNumericCode(oldCurrency);
            specialCaseNewCurrencies[specialCaseCount] = newCurrency;
            specialCaseNewCurrenciesDefaultFractionDigits[specialCaseCount] = getDefaultFractionDigits(newCurrency);
            specialCaseNewCurrenciesNumericCode[specialCaseCount] = getNumericCode(newCurrency);
        }
        specialCaseMap.put(currencyInfo, new Integer(specialCaseCount));
        return specialCaseCount++;
    }
    private static void buildOtherTables() {
        if (validCurrencyCodes.length() % 7 != 6) {
            throw new RuntimeException("\"all\" entry has incorrect size");
        }
        for (int i = 0; i < (validCurrencyCodes.length() + 1) / 7; i++) {
            if (i > 0 && validCurrencyCodes.charAt(i * 7 - 1) != '-') {
                throw new RuntimeException("incorrect separator in \"all\" entry");
            }
            String currencyCode = validCurrencyCodes.substring(i * 7, i * 7 + 3);
            int numericCode = Integer.parseInt(
                validCurrencyCodes.substring(i * 7 + 3, i * 7 + 6));
            checkCurrencyCode(currencyCode);
            int tableEntry = mainTable[(currencyCode.charAt(0) - 'A') * A_TO_Z + (currencyCode.charAt(1) - 'A')];
            if (tableEntry == INVALID_COUNTRY_ENTRY ||
                    (tableEntry & SPECIAL_CASE_COUNTRY_MASK) != 0 ||
                    (tableEntry & SIMPLE_CASE_COUNTRY_FINAL_CHAR_MASK) != (currencyCode.charAt(2) - 'A')) {
                if (otherCurrenciesCount == maxOtherCurrencies) {
                    throw new RuntimeException("too many other currencies");
                }
                if (otherCurrencies.length() > 0) {
                    otherCurrencies.append('-');
                }
                otherCurrencies.append(currencyCode);
                otherCurrenciesDefaultFractionDigits[otherCurrenciesCount] = getDefaultFractionDigits(currencyCode);
                otherCurrenciesNumericCode[otherCurrenciesCount] = getNumericCode(currencyCode);
                otherCurrenciesCount++;
            }
        }
    }
    private static void checkCurrencyCode(String currencyCode) {
        if (currencyCode.length() != 3) {
            throw new RuntimeException("illegal length for currency code: " + currencyCode);
        }
        for (int i = 0; i < 3; i++) {
            char aChar = currencyCode.charAt(i);
            if ((aChar < 'A' || aChar > 'Z') && !currencyCode.equals("XB5")) {
                throw new RuntimeException("currency code contains illegal character: " + currencyCode);
            }
        }
        if (validCurrencyCodes.indexOf(currencyCode) == -1) {
            throw new RuntimeException("currency code not listed as valid: " + currencyCode);
        }
    }
    private static void writeOutput() throws IOException {
        out.writeInt(MAGIC_NUMBER);
        out.writeInt(Integer.parseInt(formatVersion));
        out.writeInt(Integer.parseInt(dataVersion));
        writeIntArray(mainTable, mainTable.length);
        out.writeInt(specialCaseCount);
        writeLongArray(specialCaseCutOverTimes, specialCaseCount);
        writeStringArray(specialCaseOldCurrencies, specialCaseCount);
        writeStringArray(specialCaseNewCurrencies, specialCaseCount);
        writeIntArray(specialCaseOldCurrenciesDefaultFractionDigits, specialCaseCount);
        writeIntArray(specialCaseNewCurrenciesDefaultFractionDigits, specialCaseCount);
        writeIntArray(specialCaseOldCurrenciesNumericCode, specialCaseCount);
        writeIntArray(specialCaseNewCurrenciesNumericCode, specialCaseCount);
        out.writeInt(otherCurrenciesCount);
        out.writeUTF(otherCurrencies.toString());
        writeIntArray(otherCurrenciesDefaultFractionDigits, otherCurrenciesCount);
        writeIntArray(otherCurrenciesNumericCode, otherCurrenciesCount);
    }
    private static void writeIntArray(int[] ia, int count) throws IOException {
        for (int i = 0; i < count; i ++) {
            out.writeInt(ia[i]);
        }
    }
    private static void writeLongArray(long[] la, int count) throws IOException  {
        for (int i = 0; i < count; i ++) {
            out.writeLong(la[i]);
        }
    }
    private static void writeStringArray(String[] sa, int count) throws IOException  {
        for (int i = 0; i < count; i ++) {
            String str = (sa[i] != null) ? sa[i] : "";
            out.writeUTF(str);
        }
    }
}
