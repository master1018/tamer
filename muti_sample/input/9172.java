public class ValidateISO4217 {
    static final int ALPHA_NUM = 26;
    static final byte UNDEFINED = 0;
    static final byte DEFINED = 1;
    static final byte SKIPPED = 2;
    static final String datafile = "tablea1.txt";
    static byte[] codes = new byte[ALPHA_NUM * ALPHA_NUM];
    static final String[][] additionalCodes = {
        {"AQ", "", "", "0"},    
        {"GS", "GBP", "826", "2"},      
        {"AX", "EUR", "978", "2"},      
        {"PS", "ILS", "376", "2"},      
        {"JE", "GBP", "826", "2"},      
        {"GG", "GBP", "826", "2"},      
        {"IM", "GBP", "826", "2"},      
        {"BL", "EUR", "978", "2"},      
        {"MF", "EUR", "978", "2"},      
    };
    static final String otherCodes =
        "ADP-AFA-ATS-AYM-BEF-BGL-BOV-BYB-CLF-CYP-DEM-ESP-FIM-FRF-GRD-GWP-IEP-ITL-LUF-MGF-MTL-MXV-NLG-PTE-RUR-SDD-SIT-SRG-TPE-TRL-VEF-USN-USS-XAG-XAU-XBA-XBB-XBC-XBD-XDR-XFO-XFU-XPD-XPT-XTS-XXX-YUM-ZWN";
    static boolean err = false;
    static Set<Currency> testCurrencies = new HashSet<Currency>();
    public static void main(String[] args) throws Exception {
        CheckDataVersion.check();
        test1();
        test2();
        getAvailableCurrenciesTest();
        if (err) {
            throw new RuntimeException("Failed: Validation ISO 4217 data");
        }
    }
    static void test1() throws Exception {
        try (FileReader fr = new FileReader(new File(System.getProperty("test.src", "."), datafile));
             BufferedReader in = new BufferedReader(fr))
        {
            String line;
            SimpleDateFormat format = null;
            while ((line = in.readLine()) != null) {
                if (line.length() == 0 || line.charAt(0) == '#') {
                    continue;
                }
                StringTokenizer tokens = new StringTokenizer(line, "\t");
                String country = tokens.nextToken();
                if (country.length() != 2) {
                    continue;
                }
                String currency;
                String numeric;
                String minorUnit;
                int tokensCount = tokens.countTokens();
                if (tokensCount < 3) {
                    currency = "";
                    numeric = "0";
                    minorUnit = "0";
                } else {
                    currency = tokens.nextToken();
                    numeric = tokens.nextToken();
                    minorUnit = tokens.nextToken();
                    testCurrencies.add(Currency.getInstance(currency));
                    if (tokensCount > 3) {
                        if (format == null) {
                            format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);
                            format.setTimeZone(TimeZone.getTimeZone("GMT"));
                            format.setLenient(false);
                        }
                        if (format.parse(tokens.nextToken()).getTime() <
                            System.currentTimeMillis()) {
                            currency = tokens.nextToken();
                            numeric = tokens.nextToken();
                            minorUnit = tokens.nextToken();
                            testCurrencies.add(Currency.getInstance(currency));
                        }
                    }
                }
                int index = toIndex(country);
                testCountryCurrency(country, currency, Integer.parseInt(numeric),
                    Integer.parseInt(minorUnit), index);
            }
        }
        for (int i = 0; i < additionalCodes.length; i++) {
            int index = toIndex(additionalCodes[i][0]);
            if (additionalCodes[i][1].length() != 0) {
                testCountryCurrency(additionalCodes[i][0], additionalCodes[i][1],
                    Integer.parseInt(additionalCodes[i][2]),
                    Integer.parseInt(additionalCodes[i][3]), index);
                testCurrencies.add(Currency.getInstance(additionalCodes[i][1]));
            } else {
                codes[index] = SKIPPED;
            }
        }
    }
    static int toIndex(String s) {
        return ((s.charAt(0) - 'A') * ALPHA_NUM + s.charAt(1) - 'A');
    }
    static void testCountryCurrency(String country, String currencyCode,
                                int numericCode, int digits, int index) {
        if (currencyCode.length() == 0) {
            return;
        }
        testCurrencyDefined(currencyCode, numericCode, digits);
        Locale loc = new Locale("", country);
        try {
            Currency currency = Currency.getInstance(loc);
            if (!currency.getCurrencyCode().equals(currencyCode)) {
                System.err.println("Error: [" + country + ":" +
                    loc.getDisplayCountry() + "] expected: " + currencyCode +
                    ", got: " + currency.getCurrencyCode());
                err = true;
            }
            if (codes[index] != UNDEFINED) {
                System.out.println("Warning: [" + country + ":" +
                    loc.getDisplayCountry() +
                    "] multiple definitions. currency code=" + currencyCode);
            }
            codes[index] = DEFINED;
        }
        catch (Exception e) {
            System.err.println("Error: " + e + ": Country=" + country);
            err = true;
        }
    }
    static void testCurrencyDefined(String currencyCode, int numericCode, int digits) {
        try {
            Currency currency = currency = Currency.getInstance(currencyCode);
            if (currency.getNumericCode() != numericCode) {
                System.err.println("Error: [" + currencyCode + "] expected: " +
                    numericCode + "; got: " + currency.getNumericCode());
                err = true;
            }
            if (currency.getDefaultFractionDigits() != digits) {
                System.err.println("Error: [" + currencyCode + "] expected: " +
                    digits + "; got: " + currency.getDefaultFractionDigits());
                err = true;
            }
        }
        catch (Exception e) {
            System.err.println("Error: " + e + ": Currency code=" +
                currencyCode);
            err = true;
        }
    }
    static void test2() {
        for (int i = 0; i < ALPHA_NUM; i++) {
            for (int j = 0; j < ALPHA_NUM; j++) {
                char[] code = new char[2];
                code[0] = (char)('A'+ i);
                code[1] = (char)('A'+ j);
                String country = new String(code);
                boolean ex;
                if (codes[toIndex(country)] == UNDEFINED) {
                    ex = false;
                    try {
                        Currency.getInstance(new Locale("", country));
                    }
                    catch (IllegalArgumentException e) {
                        ex = true;
                    }
                    if (!ex) {
                        System.err.println("Error: This should be an undefined code and throw IllegalArgumentException: " +
                            country);
                        err = true;
                    }
                } else if (codes[toIndex(country)] == SKIPPED) {
                    Currency cur = null;
                    try {
                        cur = Currency.getInstance(new Locale("", country));
                    }
                    catch (Exception e) {
                        System.err.println("Error: " + e + ": Country=" +
                            country);
                        err = true;
                    }
                    if (cur != null) {
                        System.err.println("Error: Currency.getInstance() for an this locale should return null: " +
                            country);
                        err = true;
                    }
                }
            }
        }
    }
    static void getAvailableCurrenciesTest() {
        Set<Currency> jreCurrencies = Currency.getAvailableCurrencies();
        StringTokenizer st = new StringTokenizer(otherCodes, "-");
        while (st.hasMoreTokens()) {
            testCurrencies.add(Currency.getInstance(st.nextToken()));
        }
        if (!testCurrencies.containsAll(jreCurrencies)) {
            System.err.print("Error: getAvailableCurrencies() returned extra currencies than expected: ");
            jreCurrencies.removeAll(testCurrencies);
            for (Currency c : jreCurrencies) {
                System.err.print(" "+c);
            }
            System.err.println();
            err = true;
        }
    }
}
