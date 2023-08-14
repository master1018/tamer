public final class MccTable
{
    private static final String[] TZ_STRINGS = {
        "",
        "Africa/Johannesburg",
        "Asia/Beijing",
        "Asia/Singapore",
        "Asia/Tokyo",
        "Australia/Sydney",
        "Europe/Amsterdam",
        "Europe/Berlin",
        "Europe/Dublin",
        "Europe/London",
        "Europe/Madrid",
        "Europe/Paris",
        "Europe/Prague",
        "Europe/Rome",
        "Europe/Vienna",
        "Europe/Warsaw",
        "Europe/Zurich",
        "Pacific/Auckland"
    };
    private static final String[] LANG_STRINGS = {
        "", "cs", "de", "en", "es", "fr", "it", "ja", "nl", "zh"
    };
    private static final short[] MCC_CODES = {
        0x00ca, 0x00cc, 0x00ce, 0x00d0, 0x00d4, 0x00d5, 0x00d6, 0x00d8, 0x00da, 0x00db,
        0x00dc, 0x00de, 0x00e1, 0x00e2, 0x00e4, 0x00e6, 0x00e7, 0x00e8, 0x00ea, 0x00eb,
        0x00ee, 0x00f0, 0x00f2, 0x00f4, 0x00f6, 0x00f7, 0x00f8, 0x00fa, 0x00ff, 0x0101,
        0x0103, 0x0104, 0x0106, 0x010a, 0x010c, 0x010e, 0x0110, 0x0112, 0x0114, 0x0116,
        0x0118, 0x011a, 0x011b, 0x011c, 0x011e, 0x0120, 0x0121, 0x0122, 0x0124, 0x0125,
        0x0126, 0x0127, 0x0129, 0x012e, 0x0134, 0x0136, 0x0137, 0x0138, 0x0139, 0x013a,
        0x013b, 0x013c, 0x014a, 0x014c, 0x014e, 0x0152, 0x0154, 0x0156, 0x0158, 0x015a,
        0x015c, 0x015e, 0x0160, 0x0162, 0x0164, 0x0166, 0x0168, 0x016a, 0x016b, 0x016c,
        0x016d, 0x016e, 0x0170, 0x0172, 0x0174, 0x0176, 0x0178, 0x0190, 0x0191, 0x0192,
        0x0194, 0x0195, 0x019a, 0x019c, 0x019d, 0x019e, 0x019f, 0x01a0, 0x01a1, 0x01a2,
        0x01a3, 0x01a4, 0x01a5, 0x01a6, 0x01a7, 0x01a8, 0x01a9, 0x01aa, 0x01ab, 0x01ac,
        0x01ad, 0x01ae, 0x01af, 0x01b0, 0x01b2, 0x01b4, 0x01b5, 0x01b6, 0x01b8, 0x01b9,
        0x01c2, 0x01c4, 0x01c6, 0x01c7, 0x01c8, 0x01c9, 0x01cc, 0x01cd, 0x01d2, 0x01d3,
        0x01d6, 0x01d8, 0x01f6, 0x01f9, 0x01fe, 0x0202, 0x0203, 0x0208, 0x020d, 0x0210,
        0x0212, 0x0216, 0x0217, 0x0218, 0x0219, 0x021b, 0x021c, 0x021d, 0x021e, 0x021f,
        0x0220, 0x0221, 0x0222, 0x0223, 0x0224, 0x0225, 0x0226, 0x0227, 0x0228, 0x025a,
        0x025b, 0x025c, 0x025d, 0x025e, 0x025f, 0x0260, 0x0261, 0x0262, 0x0263, 0x0264,
        0x0265, 0x0266, 0x0267, 0x0268, 0x0269, 0x026a, 0x026b, 0x026c, 0x026d, 0x026e,
        0x026f, 0x0270, 0x0271, 0x0272, 0x0273, 0x0274, 0x0275, 0x0276, 0x0277, 0x0278,
        0x0279, 0x027a, 0x027b, 0x027c, 0x027d, 0x027e, 0x027f, 0x0280, 0x0281, 0x0282,
        0x0283, 0x0285, 0x0286, 0x0287, 0x0288, 0x0289, 0x028a, 0x028b, 0x028c, 0x028d,
        0x028e, 0x028f, 0x0291, 0x02be, 0x02c0, 0x02c2, 0x02c4, 0x02c6, 0x02c8, 0x02ca,
        0x02cc, 0x02d2, 0x02d4, 0x02da, 0x02dc, 0x02de, 0x02e0, 0x02e2, 0x02e4, 0x02e6,
        0x02e8, 0x02ea, 0x02ec, 0x02ee
    };
    private static final int[] IND_CODES = {
        0x67720400, 0x6e6c6c68, 0x62650400, 0x667204b5, 0x6d630400, 0x61640400,
        0x657304a4, 0x68750400, 0x62610400, 0x68720400, 0x72730400, 0x697404d6,
        0x766104d6, 0x726f0400, 0x63680502, 0x637a6cc1, 0x736b0400, 0x61746ce2,
        0x67626c93, 0x67626c93, 0x646b0400, 0x73650400, 0x6e6f0400, 0x66690400,
        0x6c740400, 0x6c760400, 0x65650400, 0x72750400, 0x75610400, 0x62790400,
        0x6d640400, 0x706c04f0, 0x64656c72, 0x67690400, 0x70740400, 0x6c750400,
        0x69650483, 0x69730400, 0x616c0400, 0x6d740400, 0x63790400, 0x67650400,
        0x616d0400, 0x62670400, 0x74720400, 0x666f0400, 0x67650400, 0x676c0400,
        0x736d0400, 0x736c0400, 0x6d6b0400, 0x6c690400, 0x6d650400, 0x63615e00,
        0x706d0400, 0x75735e03, 0x75735e03, 0x75735e03, 0x75735e03, 0x75735e03,
        0x75735e03, 0x75735e03, 0x70720400, 0x76690400, 0x6d780600, 0x6a6d0600,
        0x67700400, 0x62620600, 0x61670600, 0x6b790600, 0x76670600, 0x626d0400,
        0x67640400, 0x6d730400, 0x6b6e0400, 0x6c630400, 0x76630400, 0x6e6c0400,
        0x61770400, 0x62730400, 0x61690600, 0x646d0400, 0x63750400, 0x646f0400,
        0x68740400, 0x74740400, 0x74630400, 0x617a0400, 0x6b7a0400, 0x62740400,
        0x696e0400, 0x696e0400, 0x706b0400, 0x61660400, 0x6c6b0400, 0x6d6d0400,
        0x6c620400, 0x6a6f0400, 0x73790400, 0x69710400, 0x6b770400, 0x73610400,
        0x79650400, 0x6f6d0400, 0x70730400, 0x61650400, 0x696c0400, 0x62680400,
        0x71610400, 0x6d6e0400, 0x6e700400, 0x61650400, 0x61650400, 0x69720400,
        0x757a0400, 0x746a0400, 0x6b670400, 0x746d0400, 0x6a707447, 0x6a707447,
        0x6b720400, 0x766e0400, 0x686b0400, 0x6d6f0400, 0x6b680400, 0x6c610400,
        0x636e6c29, 0x636e6c29, 0x74770400, 0x6b700400, 0x62640400, 0x6d760400,
        0x6d790400, 0x61755c53, 0x69640400, 0x746c0400, 0x70680400, 0x74680400,
        0x73675c33, 0x626e0400, 0x6e7a0513, 0x6d700400, 0x67750400, 0x6e720400,
        0x70670400, 0x746f0400, 0x73620400, 0x76750400, 0x666a0400, 0x77660400,
        0x61730400, 0x6b690400, 0x6e630400, 0x70660400, 0x636b0400, 0x77730400,
        0x666d0400, 0x6d680400, 0x70770400, 0x65670400, 0x647a0400, 0x6d610400,
        0x746e0400, 0x6c790400, 0x676d0400, 0x736e0400, 0x6d720400, 0x6d6c0400,
        0x676e0400, 0x63690400, 0x62660400, 0x6e650400, 0x74670400, 0x626a0400,
        0x6d750400, 0x6c720400, 0x736c0400, 0x67680400, 0x6e670400, 0x74640400,
        0x63660400, 0x636d0400, 0x63760400, 0x73740400, 0x67710400, 0x67610400,
        0x63670400, 0x63670400, 0x616f0400, 0x67770400, 0x73630400, 0x73640400,
        0x72770400, 0x65740400, 0x736f0400, 0x646a0400, 0x6b650400, 0x747a0400,
        0x75670400, 0x62690400, 0x6d7a0400, 0x7a6d0400, 0x6d670400, 0x72650400,
        0x7a770400, 0x6e610400, 0x6d770400, 0x6c730400, 0x62770400, 0x737a0400,
        0x6b6d0400, 0x7a610413, 0x65720400, 0x627a0400, 0x67740400, 0x73760400,
        0x686e0600, 0x6e690400, 0x63720400, 0x70610400, 0x70650400, 0x61720600,
        0x62720400, 0x636c0400, 0x636f0600, 0x76650400, 0x626f0400, 0x67790400,
        0x65630400, 0x67660400, 0x70790400, 0x73720400, 0x75790400, 0x666b0400
    };
    static final String LOG_TAG = "MccTable";
    public static String defaultTimeZoneForMcc(int mcc) {
        int index = Arrays.binarySearch(MCC_CODES, (short)mcc);
        if (index < 0) {
            return null;
        }
        int indCode = IND_CODES[index];
        int tzInd = (indCode >>> 4) & 0x001F;
        String tz = TZ_STRINGS[tzInd];
        if (tz == "") {
            return null;
        }
        return tz;
    }
    public static String countryCodeForMcc(int mcc) {
        int index = Arrays.binarySearch(MCC_CODES, (short)mcc);
        if (index < 0) {
            return "";
        }
        int indCode = IND_CODES[index];
        byte[] iso = {(byte)((indCode >>> 24) & 0x00FF), (byte)((indCode >>> 16) & 0x00FF)};
        return new String(iso);
    }
    public static String defaultLanguageForMcc(int mcc) {
        int index = Arrays.binarySearch(MCC_CODES, (short)mcc);
        if (index < 0) {
            return null;
        }
        int indCode = IND_CODES[index];
        int langInd = indCode & 0x000F;
        String lang = LANG_STRINGS[langInd];
        if (lang == "") {
            return null;
        }
        return lang;
    }
    public static int smallestDigitsMccForMnc(int mcc) {
        int index = Arrays.binarySearch(MCC_CODES, (short)mcc);
        if (index < 0) {
            return 2;
        }
        int indCode = IND_CODES[index];
        int smDig = (indCode >>> 9) & 0x0003;
        return smDig;
    }
    public static int wifiChannelsForMcc(int mcc) {
        int index = Arrays.binarySearch(MCC_CODES, (short)mcc);
        if (index < 0) {
            return 0;
        }
        int indCode = IND_CODES[index];
        int wifi = (indCode >>> 11) & 0x000F;
        return wifi;
    }
    public static void updateMccMncConfiguration(PhoneBase phone, String mccmnc) {
        if (!TextUtils.isEmpty(mccmnc)) {
            int mcc, mnc;
            try {
                mcc = Integer.parseInt(mccmnc.substring(0,3));
                mnc = Integer.parseInt(mccmnc.substring(3));
            } catch (NumberFormatException e) {
                Log.e(LOG_TAG, "Error parsing IMSI");
                return;
            }
            Log.d(LOG_TAG, "updateMccMncConfiguration: mcc=" + mcc + ", mnc=" + mnc);
            if (mcc != 0) {
                setTimezoneFromMccIfNeeded(phone, mcc);
                setLocaleFromMccIfNeeded(phone, mcc);
                setWifiChannelsFromMcc(phone, mcc);
            }
            try {
                Configuration config = ActivityManagerNative.getDefault().getConfiguration();
                if (mcc != 0) {
                    config.mcc = mcc;
                }
                if (mnc != 0) {
                    config.mnc = mnc;
                }
                ActivityManagerNative.getDefault().updateConfiguration(config);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "Can't update configuration", e);
            }
        }
    }
    private static void setTimezoneFromMccIfNeeded(PhoneBase phone, int mcc) {
        String timezone = SystemProperties.get(ServiceStateTracker.TIMEZONE_PROPERTY);
        if (timezone == null || timezone.length() == 0) {
            String zoneId = defaultTimeZoneForMcc(mcc);
            if (zoneId != null && zoneId.length() > 0) {
                Context context = phone.getContext();
                AlarmManager alarm =
                        (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarm.setTimeZone(zoneId);
                Log.d(LOG_TAG, "timezone set to "+zoneId);
            }
        }
    }
    private static void setLocaleFromMccIfNeeded(PhoneBase phone, int mcc) {
        String language = MccTable.defaultLanguageForMcc(mcc);
        String country = MccTable.countryCodeForMcc(mcc);
        Log.d(LOG_TAG, "locale set to "+language+"_"+country);
        phone.setSystemLocale(language, country);
    }
    private static void setWifiChannelsFromMcc(PhoneBase phone, int mcc) {
        int wifiChannels = MccTable.wifiChannelsForMcc(mcc);
        if (wifiChannels != 0) {
            Context context = phone.getContext();
            Log.d(LOG_TAG, "WIFI_NUM_ALLOWED_CHANNELS set to " + wifiChannels);
            WifiManager wM = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            wM.setNumAllowedChannels(wifiChannels, true);
        }
    }
}
