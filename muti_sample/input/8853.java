public class Bug4512215 {
    public static void main(String[] args) throws Exception {
        testCurrencyDefined("XBD", -1);
        testCountryCurrency("TJ", "TJS", 2);
        testCountryCurrency("FO", "DKK", 2);
        testCountryCurrency("FK", "FKP", 2);
        testCountryCurrency("AF", "AFN", 2);    
        testCountryCurrency("TL", "USD", 2);    
        testCountryCurrency("CS", "CSD", 2);    
    }
    private static void testCountryCurrency(String country, String currencyCode,
            int digits) {
        testCurrencyDefined(currencyCode, digits);
        Currency currency = Currency.getInstance(new Locale("", country));
        if (!currency.getCurrencyCode().equals(currencyCode)) {
            throw new RuntimeException("[" + country
                    + "] expected: " + currencyCode
                    + "; got: " + currency.getCurrencyCode());
        }
    }
    private static void testCurrencyDefined(String currencyCode, int digits) {
        Currency currency = Currency.getInstance(currencyCode);
        if (currency.getDefaultFractionDigits() != digits) {
            throw new RuntimeException("[" + currencyCode
                    + "] expected: " + digits
                    + "; got: " + currency.getDefaultFractionDigits());
        }
    }
}
