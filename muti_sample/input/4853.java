public class Bug4429024 {
    public static void main(String[] args) throws Exception {
        int errors=0;
        String [][] fiLocales = {
                    { "ar", "arabia" },
                    { "ba", "baski" },
                    { "bg", "bulgaria" },
                    { "ca", "katalaani" },
                    { "cs", "tsekki" },
                    { "da", "tanska" },
                    { "de", "saksa" },
                    { "el", "kreikka" },
                    { "en", "englanti" },
                    { "es", "espanja" },
                    { "fi", "suomi" },
                    { "fr", "ranska" },
                    { "he", "heprea" },
                    { "hi", "hindi" },
                    { "it", "italia" },
                    { "ja", "japani" },
                    { "lt", "liettua" },
                    { "lv", "latvia" },
                    { "nl", "hollanti" },
                    { "no", "norja" },
                    { "pl", "puola" },
                    { "pt", "portugali" },
                    { "ru", "ven\u00e4j\u00e4" },
                    { "sv", "ruotsi" },
                    { "th", "thai" },
                    { "tr", "turkki" },
                    { "zh", "kiina" }
        };
        String[][] fiCountries = {
                    { "BE", "Belgia" },
                    { "BR", "Brasilia" },
                    { "CA", "Kanada" },
                    { "CH", "Sveitsi" },
                    { "CN", "Kiina" },
                    { "CZ", "Tsekin tasavalta" },
                    { "DE", "Saksa" },
                    { "DK", "Tanska" },
                    { "ES", "Espanja" },
                    { "FI", "Suomi" },
                    { "FR", "Ranska" },
                    { "GB", "Iso-Britannia" },
                    { "GR", "Kreikka" },
                    { "IE", "Irlanti" },
                    { "IT", "Italia" },
                    { "JP", "Japani" },
                    { "KR", "Korea" },
                    { "NL", "Alankomaat" },
                    { "NO", "Norja" },
                    { "PL", "Puola" },
                    { "PT", "Portugali" },
                    { "RU", "Ven\u00e4j\u00e4" },
                    { "SE", "Ruotsi" },
                    { "TR", "Turkki" },
                    { "US", "Yhdysvallat" }
        };
        for (int i=0; i < fiLocales.length; i++) {
            errors += getLanguage(fiLocales[i][0], fiLocales[i][1]);
        }
        for (int i=0; i < fiCountries.length; i++) {
            errors += getCountry(fiCountries[i][0], fiCountries[i][1]);
        }
        if(errors > 0){
            throw new RuntimeException();
        }
    };
        static int getLanguage(String inLang, String localizedName){
            Locale fiLocale = new Locale("fi", "FI");
            Locale inLocale = new Locale (inLang, "");
            if (!inLocale.getDisplayLanguage(fiLocale).equals(localizedName)){
                System.out.println("Language " + inLang +" should be \"" + localizedName  + "\", not \"" + inLocale.getDisplayLanguage(fiLocale) + "\"");
                return 1;
            }
            else{
                return 0;
            }
        }
    static int getCountry(String inCountry, String localizedName){
            Locale fiLocale = new Locale("fi", "FI");
            Locale inLocale = new Locale ("", inCountry);
            if (!inLocale.getDisplayCountry(fiLocale).equals(localizedName)){
                System.out.println("Country " + inCountry + " should be \"" + localizedName + "\", not \"" + inLocale.getDisplayCountry(fiLocale) + "\"");
                return 1;
            }
            else{
                return 0;
            }
        }
}
