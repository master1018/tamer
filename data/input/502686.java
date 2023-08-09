public class LanguageSwitcher {
    private Locale[] mLocales;
    private LatinIME mIme;
    private String[] mSelectedLanguageArray;
    private String   mSelectedLanguages;
    private int      mCurrentIndex = 0;
    private String   mDefaultInputLanguage;
    private Locale   mDefaultInputLocale;
    private Locale   mSystemLocale;
    public LanguageSwitcher(LatinIME ime) {
        mIme = ime;
        mLocales = new Locale[0];
    }
    public Locale[]  getLocales() {
        return mLocales;
    }
    public int getLocaleCount() {
        return mLocales.length;
    }
    public boolean loadLocales(SharedPreferences sp) {
        String selectedLanguages = sp.getString(LatinIME.PREF_SELECTED_LANGUAGES, null);
        String currentLanguage   = sp.getString(LatinIME.PREF_INPUT_LANGUAGE, null);
        if (selectedLanguages == null || selectedLanguages.length() < 1) {
            loadDefaults();
            if (mLocales.length == 0) {
                return false;
            }
            mLocales = new Locale[0];
            return true;
        }
        if (selectedLanguages.equals(mSelectedLanguages)) {
            return false;
        }
        mSelectedLanguageArray = selectedLanguages.split(",");
        mSelectedLanguages = selectedLanguages; 
        constructLocales();
        mCurrentIndex = 0;
        if (currentLanguage != null) {
            mCurrentIndex = 0;
            for (int i = 0; i < mLocales.length; i++) {
                if (mSelectedLanguageArray[i].equals(currentLanguage)) {
                    mCurrentIndex = i;
                    break;
                }
            }
        }
        return true;
    }
    private void loadDefaults() {
        mDefaultInputLocale = mIme.getResources().getConfiguration().locale;
        String country = mDefaultInputLocale.getCountry();
        mDefaultInputLanguage = mDefaultInputLocale.getLanguage() +
                (TextUtils.isEmpty(country) ? "" : "_" + country);
    }
    private void constructLocales() {
        mLocales = new Locale[mSelectedLanguageArray.length];
        for (int i = 0; i < mLocales.length; i++) {
            final String lang = mSelectedLanguageArray[i];
            mLocales[i] = new Locale(lang.substring(0, 2),
                    lang.length() > 4 ? lang.substring(3, 5) : "");
        }
    }
    public String getInputLanguage() {
        if (getLocaleCount() == 0) return mDefaultInputLanguage;
        return mSelectedLanguageArray[mCurrentIndex];
    }
    public String[] getEnabledLanguages() {
        return mSelectedLanguageArray;
    }
    public Locale getInputLocale() {
        if (getLocaleCount() == 0) return mDefaultInputLocale;
        return mLocales[mCurrentIndex];
    }
    public Locale getNextInputLocale() {
        if (getLocaleCount() == 0) return mDefaultInputLocale;
        return mLocales[(mCurrentIndex + 1) % mLocales.length];
    }
    public void setSystemLocale(Locale locale) {
        mSystemLocale = locale;
    }
    public Locale getSystemLocale() {
        return mSystemLocale;
    }
    public Locale getPrevInputLocale() {
        if (getLocaleCount() == 0) return mDefaultInputLocale;
        return mLocales[(mCurrentIndex - 1 + mLocales.length) % mLocales.length];
    }
    public void reset() {
        mCurrentIndex = 0;
    }
    public void next() {
        mCurrentIndex++;
        if (mCurrentIndex >= mLocales.length) mCurrentIndex = 0; 
    }
    public void prev() {
        mCurrentIndex--;
        if (mCurrentIndex < 0) mCurrentIndex = mLocales.length - 1; 
    }
    public void persist() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mIme);
        Editor editor = sp.edit();
        editor.putString(LatinIME.PREF_INPUT_LANGUAGE, getInputLanguage());
        editor.commit();
    }
    static String toTitleCase(String s) {
        if (s.length() == 0) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
