public final class EriManager {
    class EriFile {
        public int mVersionNumber;                      
        public int mNumberOfEriEntries;                 
        public int mEriFileType;                        
        public String[] mCallPromptId;                  
        public HashMap<Integer, EriInfo> mRoamIndTable; 
        public EriFile() {
            this.mVersionNumber = -1;
            this.mNumberOfEriEntries = 0;
            this.mEriFileType = -1;
            this.mCallPromptId = new String[] { "", "", "" };
            this.mRoamIndTable = new HashMap<Integer, EriInfo>();
        }
    }
    class EriDisplayInformation {
        public int mEriIconIndex;
        public int mEriIconMode;
        public String mEriIconText;
        public EriDisplayInformation(int eriIconIndex, int eriIconMode, String eriIconText) {
            mEriIconIndex = eriIconIndex;
            mEriIconMode = eriIconMode;
            mEriIconText = eriIconText;
        }
        @Override
        public String toString() {
            return "EriDisplayInformation: {" + " IconIndex: " + mEriIconIndex + " EriIconMode: "
                    + mEriIconMode + " EriIconText: " + mEriIconText + " }";
        }
    }
    private static final String LOG_TAG = "CDMA";
    private static final boolean DBG = true;
    private static final boolean VDBG = false;
    public static final int ERI_FROM_XML          = 0;
    public static final int ERI_FROM_FILE_SYSTEM  = 1;
    public static final int ERI_FROM_MODEM        = 2;
    private PhoneBase mPhone;
    private Context mContext;
    private int mEriFileSource = ERI_FROM_XML;
    private boolean isEriFileLoaded;
    private EriFile mEriFile;
    public EriManager(PhoneBase phone, Context context, int eriFileSource) {
        this.mPhone = phone;
        this.mContext = context;
        this.mEriFileSource = eriFileSource;
        this.mEriFile = new EriFile();
    }
    public void dispose() {
        mEriFile = new EriFile();
        isEriFileLoaded = false;
    }
    public void loadEriFile() {
        switch (mEriFileSource) {
        case ERI_FROM_MODEM:
            loadEriFileFromModem();
            break;
        case ERI_FROM_FILE_SYSTEM:
            loadEriFileFromFileSystem();
            break;
        case ERI_FROM_XML:
        default:
            loadEriFileFromXml();
            break;
        }
    }
    private void loadEriFileFromModem() {
    }
    private void loadEriFileFromFileSystem() {
    }
    private void loadEriFileFromXml() {
        XmlPullParser parser = null;
        FileInputStream stream = null;
        Resources r = mContext.getResources();
        try {
            if (DBG) Log.d(LOG_TAG, "loadEriFileFromXml: check for alternate file");
            stream = new FileInputStream(
                            r.getString(com.android.internal.R.string.alternate_eri_file));
            parser = Xml.newPullParser();
            parser.setInput(stream, null);
            if (DBG) Log.d(LOG_TAG, "loadEriFileFromXml: opened alternate file");
        } catch (FileNotFoundException e) {
            if (DBG) Log.d(LOG_TAG, "loadEriFileFromXml: no alternate file");
            parser = null;
        } catch (XmlPullParserException e) {
            if (DBG) Log.d(LOG_TAG, "loadEriFileFromXml: no parser for alternate file");
            parser = null;
        }
        if (parser == null) {
            if (DBG) Log.d(LOG_TAG, "loadEriFileFromXml: open normal file");
            parser = r.getXml(com.android.internal.R.xml.eri);
        }
        try {
            XmlUtils.beginDocument(parser, "EriFile");
            mEriFile.mVersionNumber = Integer.parseInt(
                    parser.getAttributeValue(null, "VersionNumber"));
            mEriFile.mNumberOfEriEntries = Integer.parseInt(
                    parser.getAttributeValue(null, "NumberOfEriEntries"));
            mEriFile.mEriFileType = Integer.parseInt(
                    parser.getAttributeValue(null, "EriFileType"));
            int parsedEriEntries = 0;
            while(true) {
                XmlUtils.nextElement(parser);
                String name = parser.getName();
                if (name == null) {
                    if (parsedEriEntries != mEriFile.mNumberOfEriEntries)
                        Log.e(LOG_TAG, "Error Parsing ERI file: " +  mEriFile.mNumberOfEriEntries
                                + " defined, " + parsedEriEntries + " parsed!");
                    break;
                } else if (name.equals("CallPromptId")) {
                    int id = Integer.parseInt(parser.getAttributeValue(null, "Id"));
                    String text = parser.getAttributeValue(null, "CallPromptText");
                    if (id >= 0 && id <= 2) {
                        mEriFile.mCallPromptId[id] = text;
                    } else {
                        Log.e(LOG_TAG, "Error Parsing ERI file: found" + id + " CallPromptId");
                    }
                } else if (name.equals("EriInfo")) {
                    int roamingIndicator = Integer.parseInt(
                            parser.getAttributeValue(null, "RoamingIndicator"));
                    int iconIndex = Integer.parseInt(parser.getAttributeValue(null, "IconIndex"));
                    int iconMode = Integer.parseInt(parser.getAttributeValue(null, "IconMode"));
                    String eriText = parser.getAttributeValue(null, "EriText");
                    int callPromptId = Integer.parseInt(
                            parser.getAttributeValue(null, "CallPromptId"));
                    int alertId = Integer.parseInt(parser.getAttributeValue(null, "AlertId"));
                    parsedEriEntries++;
                    mEriFile.mRoamIndTable.put(roamingIndicator, new EriInfo (roamingIndicator,
                            iconIndex, iconMode, eriText, callPromptId, alertId));
                }
            }
            if (DBG) Log.d(LOG_TAG, "loadEriFileFromXml: eri parsing successful, file loaded");
            isEriFileLoaded = true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Got exception while loading ERI file.", e);
        } finally {
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser)parser).close();
            }
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
            }
        }
    }
    public int getEriFileVersion() {
        return mEriFile.mVersionNumber;
    }
    public int getEriNumberOfEntries() {
        return mEriFile.mNumberOfEriEntries;
    }
    public int getEriFileType() {
        return mEriFile.mEriFileType;
    }
    public boolean isEriFileLoaded() {
        return isEriFileLoaded;
    }
    private EriInfo getEriInfo(int roamingIndicator) {
        if (mEriFile.mRoamIndTable.containsKey(roamingIndicator)) {
            return mEriFile.mRoamIndTable.get(roamingIndicator);
        } else {
            return null;
        }
    }
    private EriDisplayInformation getEriDisplayInformation(int roamInd, int defRoamInd){
        EriDisplayInformation ret;
        if (isEriFileLoaded) {
            EriInfo eriInfo = getEriInfo(roamInd);
            if (eriInfo != null) {
                if (VDBG) Log.v(LOG_TAG, "ERI roamInd " + roamInd + " found in ERI file");
                ret = new EriDisplayInformation(
                        eriInfo.mIconIndex,
                        eriInfo.mIconMode,
                        eriInfo.mEriText);
                return ret;
            }
        }
        switch (roamInd) {
        case EriInfo.ROAMING_INDICATOR_ON:
            ret = new EriDisplayInformation(
                    EriInfo.ROAMING_INDICATOR_ON,
                    EriInfo.ROAMING_ICON_MODE_NORMAL,
                    mContext.getText(com.android.internal.R.string.roamingText0).toString());
            break;
        case EriInfo.ROAMING_INDICATOR_OFF:
            ret = new EriDisplayInformation(
                    EriInfo.ROAMING_INDICATOR_OFF,
                    EriInfo.ROAMING_ICON_MODE_NORMAL,
                    mContext.getText(com.android.internal.R.string.roamingText1).toString());
            break;
        case EriInfo.ROAMING_INDICATOR_FLASH:
            ret = new EriDisplayInformation(
                    EriInfo.ROAMING_INDICATOR_FLASH,
                    EriInfo.ROAMING_ICON_MODE_FLASH,
                    mContext.getText(com.android.internal.R.string.roamingText2).toString());
            break;
        case 3:
            ret = new EriDisplayInformation(
                    roamInd,
                    EriInfo.ROAMING_ICON_MODE_NORMAL,
                    mContext.getText(com.android.internal.R.string.roamingText3).toString());
            break;
        case 4:
            ret = new EriDisplayInformation(
                    roamInd,
                    EriInfo.ROAMING_ICON_MODE_NORMAL,
                    mContext.getText(com.android.internal.R.string.roamingText4).toString());
            break;
        case 5:
            ret = new EriDisplayInformation(
                    roamInd,
                    EriInfo.ROAMING_ICON_MODE_NORMAL,
                    mContext.getText(com.android.internal.R.string.roamingText5).toString());
            break;
        case 6:
            ret = new EriDisplayInformation(
                    roamInd,
                    EriInfo.ROAMING_ICON_MODE_NORMAL,
                    mContext.getText(com.android.internal.R.string.roamingText6).toString());
            break;
        case 7:
            ret = new EriDisplayInformation(
                    roamInd,
                    EriInfo.ROAMING_ICON_MODE_NORMAL,
                    mContext.getText(com.android.internal.R.string.roamingText7).toString());
            break;
        case 8:
            ret = new EriDisplayInformation(
                    roamInd,
                    EriInfo.ROAMING_ICON_MODE_NORMAL,
                    mContext.getText(com.android.internal.R.string.roamingText8).toString());
            break;
        case 9:
            ret = new EriDisplayInformation(
                    roamInd,
                    EriInfo.ROAMING_ICON_MODE_NORMAL,
                    mContext.getText(com.android.internal.R.string.roamingText9).toString());
            break;
        case 10:
            ret = new EriDisplayInformation(
                    roamInd,
                    EriInfo.ROAMING_ICON_MODE_NORMAL,
                    mContext.getText(com.android.internal.R.string.roamingText10).toString());
            break;
        case 11:
            ret = new EriDisplayInformation(
                    roamInd,
                    EriInfo.ROAMING_ICON_MODE_NORMAL,
                    mContext.getText(com.android.internal.R.string.roamingText11).toString());
            break;
        case 12:
            ret = new EriDisplayInformation(
                    roamInd,
                    EriInfo.ROAMING_ICON_MODE_NORMAL,
                    mContext.getText(com.android.internal.R.string.roamingText12).toString());
            break;
        default:
            if (!isEriFileLoaded) {
                if (DBG) Log.d(LOG_TAG, "ERI File not loaded");
                if(defRoamInd > 2) {
                    if (VDBG) Log.v(LOG_TAG, "ERI defRoamInd > 2 ...flashing");
                    ret = new EriDisplayInformation(
                            EriInfo.ROAMING_INDICATOR_FLASH,
                            EriInfo.ROAMING_ICON_MODE_FLASH,
                            mContext.getText(com.android.internal
                                                            .R.string.roamingText2).toString());
                } else {
                    if (VDBG) Log.v(LOG_TAG, "ERI defRoamInd <= 2");
                    switch (defRoamInd) {
                    case EriInfo.ROAMING_INDICATOR_ON:
                        ret = new EriDisplayInformation(
                                EriInfo.ROAMING_INDICATOR_ON,
                                EriInfo.ROAMING_ICON_MODE_NORMAL,
                                mContext.getText(com.android.internal
                                                            .R.string.roamingText0).toString());
                        break;
                    case EriInfo.ROAMING_INDICATOR_OFF:
                        ret = new EriDisplayInformation(
                                EriInfo.ROAMING_INDICATOR_OFF,
                                EriInfo.ROAMING_ICON_MODE_NORMAL,
                                mContext.getText(com.android.internal
                                                            .R.string.roamingText1).toString());
                        break;
                    case EriInfo.ROAMING_INDICATOR_FLASH:
                        ret = new EriDisplayInformation(
                                EriInfo.ROAMING_INDICATOR_FLASH,
                                EriInfo.ROAMING_ICON_MODE_FLASH,
                                mContext.getText(com.android.internal
                                                            .R.string.roamingText2).toString());
                        break;
                    default:
                        ret = new EriDisplayInformation(-1, -1, "ERI text");
                    }
                }
            } else {
                EriInfo eriInfo = getEriInfo(roamInd);
                EriInfo defEriInfo = getEriInfo(defRoamInd);
                if (eriInfo == null) {
                    if (VDBG) {
                        Log.v(LOG_TAG, "ERI roamInd " + roamInd
                            + " not found in ERI file ...using defRoamInd " + defRoamInd);
                    }
                    if(defEriInfo == null) {
                        Log.e(LOG_TAG, "ERI defRoamInd " + defRoamInd
                                + " not found in ERI file ...on");
                        ret = new EriDisplayInformation(
                                EriInfo.ROAMING_INDICATOR_ON,
                                EriInfo.ROAMING_ICON_MODE_NORMAL,
                                mContext.getText(com.android.internal
                                                             .R.string.roamingText0).toString());
                    } else {
                        if (VDBG) {
                            Log.v(LOG_TAG, "ERI defRoamInd " + defRoamInd + " found in ERI file");
                        }
                        ret = new EriDisplayInformation(
                                defEriInfo.mIconIndex,
                                defEriInfo.mIconMode,
                                defEriInfo.mEriText);
                    }
                } else {
                    if (VDBG) Log.v(LOG_TAG, "ERI roamInd " + roamInd + " found in ERI file");
                    ret = new EriDisplayInformation(
                            eriInfo.mIconIndex,
                            eriInfo.mIconMode,
                            eriInfo.mEriText);
                }
            }
            break;
        }
        if (VDBG) Log.v(LOG_TAG, "Displaying ERI " + ret.toString());
        return ret;
    }
    public int getCdmaEriIconIndex(int roamInd, int defRoamInd){
        return getEriDisplayInformation(roamInd, defRoamInd).mEriIconIndex;
    }
    public int getCdmaEriIconMode(int roamInd, int defRoamInd){
        return getEriDisplayInformation(roamInd, defRoamInd).mEriIconMode;
    }
    public String getCdmaEriText(int roamInd, int defRoamInd){
        return getEriDisplayInformation(roamInd, defRoamInd).mEriIconText;
    }
}
