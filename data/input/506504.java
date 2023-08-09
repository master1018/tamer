public class VCardEntry {
    private static final String LOG_TAG = "VCardEntry";
    private final static int DEFAULT_ORGANIZATION_TYPE = Organization.TYPE_WORK;
    private static final String ACCOUNT_TYPE_GOOGLE = "com.google";
    private static final String GOOGLE_MY_CONTACTS_GROUP = "System Group: My Contacts";
    private static final Map<String, Integer> sImMap = new HashMap<String, Integer>();
    static {
        sImMap.put(VCardConstants.PROPERTY_X_AIM, Im.PROTOCOL_AIM);
        sImMap.put(VCardConstants.PROPERTY_X_MSN, Im.PROTOCOL_MSN);
        sImMap.put(VCardConstants.PROPERTY_X_YAHOO, Im.PROTOCOL_YAHOO);
        sImMap.put(VCardConstants.PROPERTY_X_ICQ, Im.PROTOCOL_ICQ);
        sImMap.put(VCardConstants.PROPERTY_X_JABBER, Im.PROTOCOL_JABBER);
        sImMap.put(VCardConstants.PROPERTY_X_SKYPE_USERNAME, Im.PROTOCOL_SKYPE);
        sImMap.put(VCardConstants.PROPERTY_X_GOOGLE_TALK, Im.PROTOCOL_GOOGLE_TALK);
        sImMap.put(VCardConstants.ImportOnly.PROPERTY_X_GOOGLE_TALK_WITH_SPACE,
                Im.PROTOCOL_GOOGLE_TALK);
    }
    static public class PhoneData {
        public final int type;
        public final String data;
        public final String label;
        public boolean isPrimary;
        public PhoneData(int type, String data, String label, boolean isPrimary) {
            this.type = type;
            this.data = data;
            this.label = label;
            this.isPrimary = isPrimary;
        }
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof PhoneData)) {
                return false;
            }
            PhoneData phoneData = (PhoneData)obj;
            return (type == phoneData.type && data.equals(phoneData.data) &&
                    label.equals(phoneData.label) && isPrimary == phoneData.isPrimary);
        }
        @Override
        public String toString() {
            return String.format("type: %d, data: %s, label: %s, isPrimary: %s",
                    type, data, label, isPrimary);
        }
    }
    static public class EmailData {
        public final int type;
        public final String data;
        public final String label;
        public boolean isPrimary;
        public EmailData(int type, String data, String label, boolean isPrimary) {
            this.type = type;
            this.data = data;
            this.label = label;
            this.isPrimary = isPrimary;
        }
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof EmailData)) {
                return false;
            }
            EmailData emailData = (EmailData)obj;
            return (type == emailData.type && data.equals(emailData.data) &&
                    label.equals(emailData.label) && isPrimary == emailData.isPrimary);
        }
        @Override
        public String toString() {
            return String.format("type: %d, data: %s, label: %s, isPrimary: %s",
                    type, data, label, isPrimary);
        }
    }
    static public class PostalData {
        public static final int ADDR_MAX_DATA_SIZE = 7;
        private final String[] dataArray;
        public final String pobox;
        public final String extendedAddress;
        public final String street;
        public final String localty;
        public final String region;
        public final String postalCode;
        public final String country;
        public final int type;
        public final String label;
        public boolean isPrimary;
        public PostalData(final int type, final List<String> propValueList,
                final String label, boolean isPrimary) {
            this.type = type;
            dataArray = new String[ADDR_MAX_DATA_SIZE];
            int size = propValueList.size();
            if (size > ADDR_MAX_DATA_SIZE) {
                size = ADDR_MAX_DATA_SIZE;
            }
            int i = 0;
            for (String addressElement : propValueList) {
                dataArray[i] = addressElement;
                if (++i >= size) {
                    break;
                }
            }
            while (i < ADDR_MAX_DATA_SIZE) {
                dataArray[i++] = null;
            }
            this.pobox = dataArray[0];
            this.extendedAddress = dataArray[1];
            this.street = dataArray[2];
            this.localty = dataArray[3];
            this.region = dataArray[4];
            this.postalCode = dataArray[5];
            this.country = dataArray[6];
            this.label = label;
            this.isPrimary = isPrimary;
        }
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof PostalData)) {
                return false;
            }
            final PostalData postalData = (PostalData)obj;
            return (Arrays.equals(dataArray, postalData.dataArray) &&
                    (type == postalData.type &&
                            (type == StructuredPostal.TYPE_CUSTOM ?
                                    (label == postalData.label) : true)) &&
                    (isPrimary == postalData.isPrimary));
        }
        public String getFormattedAddress(final int vcardType) {
            StringBuilder builder = new StringBuilder();
            boolean empty = true;
            if (VCardConfig.isJapaneseDevice(vcardType)) {
                for (int i = ADDR_MAX_DATA_SIZE - 1; i >= 0; i--) {
                    String addressPart = dataArray[i];
                    if (!TextUtils.isEmpty(addressPart)) {
                        if (!empty) {
                            builder.append(' ');
                        } else {
                            empty = false;
                        }
                        builder.append(addressPart);
                    }
                }
            } else {
                for (int i = 0; i < ADDR_MAX_DATA_SIZE; i++) {
                    String addressPart = dataArray[i];
                    if (!TextUtils.isEmpty(addressPart)) {
                        if (!empty) {
                            builder.append(' ');
                        } else {
                            empty = false;
                        }
                        builder.append(addressPart);
                    }
                }
            }
            return builder.toString().trim();
        }
        @Override
        public String toString() {
            return String.format("type: %d, label: %s, isPrimary: %s",
                    type, label, isPrimary);
        }
    }
    static public class OrganizationData {
        public final int type;
        public String companyName;
        public String departmentName;
        public String titleName;
        public boolean isPrimary;
        public OrganizationData(int type,
                String companyName,
                String departmentName,
                String titleName,
                boolean isPrimary) {
            this.type = type;
            this.companyName = companyName;
            this.departmentName = departmentName;
            this.titleName = titleName;
            this.isPrimary = isPrimary;
        }
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof OrganizationData)) {
                return false;
            }
            OrganizationData organization = (OrganizationData)obj;
            return (type == organization.type &&
                    TextUtils.equals(companyName, organization.companyName) &&
                    TextUtils.equals(departmentName, organization.departmentName) &&
                    TextUtils.equals(titleName, organization.titleName) &&
                    isPrimary == organization.isPrimary);
        }
        public String getFormattedString() {
            final StringBuilder builder = new StringBuilder();
            if (!TextUtils.isEmpty(companyName)) {
                builder.append(companyName);
            }
            if (!TextUtils.isEmpty(departmentName)) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(departmentName);
            }
            if (!TextUtils.isEmpty(titleName)) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(titleName);
            }
            return builder.toString();
        }
        @Override
        public String toString() {
            return String.format(
                    "type: %d, company: %s, department: %s, title: %s, isPrimary: %s",
                    type, companyName, departmentName, titleName, isPrimary);
        }
    }
    static public class ImData {
        public final int protocol;
        public final String customProtocol;
        public final int type;
        public final String data;
        public final boolean isPrimary;
        public ImData(final int protocol, final String customProtocol, final int type,
                final String data, final boolean isPrimary) {
            this.protocol = protocol;
            this.customProtocol = customProtocol;
            this.type = type;
            this.data = data;
            this.isPrimary = isPrimary;
        }
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ImData)) {
                return false;
            }
            ImData imData = (ImData)obj;
            return (type == imData.type && protocol == imData.protocol
                    && (customProtocol != null ? customProtocol.equals(imData.customProtocol) :
                        (imData.customProtocol == null))
                    && (data != null ? data.equals(imData.data) : (imData.data == null))
                    && isPrimary == imData.isPrimary);
        }
        @Override
        public String toString() {
            return String.format(
                    "type: %d, protocol: %d, custom_protcol: %s, data: %s, isPrimary: %s",
                    type, protocol, customProtocol, data, isPrimary);
        }
    }
    public static class PhotoData {
        public static final String FORMAT_FLASH = "SWF";
        public final int type;
        public final String formatName;  
        public final byte[] photoBytes;
        public final boolean isPrimary;
        public PhotoData(int type, String formatName, byte[] photoBytes, boolean isPrimary) {
            this.type = type;
            this.formatName = formatName;
            this.photoBytes = photoBytes;
            this.isPrimary = isPrimary;
        }
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof PhotoData)) {
                return false;
            }
            PhotoData photoData = (PhotoData)obj;
            return (type == photoData.type &&
                    (formatName == null ? (photoData.formatName == null) :
                            formatName.equals(photoData.formatName)) &&
                    (Arrays.equals(photoBytes, photoData.photoBytes)) &&
                    (isPrimary == photoData.isPrimary));
        }
        @Override
        public String toString() {
            return String.format("type: %d, format: %s: size: %d, isPrimary: %s",
                    type, formatName, photoBytes.length, isPrimary);
        }
    }
     static class Property {
        private String mPropertyName;
        private Map<String, Collection<String>> mParameterMap =
            new HashMap<String, Collection<String>>();
        private List<String> mPropertyValueList = new ArrayList<String>();
        private byte[] mPropertyBytes;
        public void setPropertyName(final String propertyName) {
            mPropertyName = propertyName;
        }
        public void addParameter(final String paramName, final String paramValue) {
            Collection<String> values;
            if (!mParameterMap.containsKey(paramName)) {
                if (paramName.equals("TYPE")) {
                    values = new HashSet<String>();
                } else {
                    values = new ArrayList<String>();
                }
                mParameterMap.put(paramName, values);
            } else {
                values = mParameterMap.get(paramName);
            }
            values.add(paramValue);
        }
        public void addToPropertyValueList(final String propertyValue) {
            mPropertyValueList.add(propertyValue);
        }
        public void setPropertyBytes(final byte[] propertyBytes) {
            mPropertyBytes = propertyBytes;
        }
        public final Collection<String> getParameters(String type) {
            return mParameterMap.get(type);
        }
        public final List<String> getPropertyValueList() {
            return mPropertyValueList;
        }
        public void clear() {
            mPropertyName = null;
            mParameterMap.clear();
            mPropertyValueList.clear();
            mPropertyBytes = null;
        }
    }
    private String mFamilyName;
    private String mGivenName;
    private String mMiddleName;
    private String mPrefix;
    private String mSuffix;
    private String mFullName;
    private String mPhoneticFamilyName;
    private String mPhoneticGivenName;
    private String mPhoneticMiddleName;
    private String mPhoneticFullName;
    private List<String> mNickNameList;
    private String mDisplayName;
    private String mBirthday;
    private List<String> mNoteList;
    private List<PhoneData> mPhoneList;
    private List<EmailData> mEmailList;
    private List<PostalData> mPostalList;
    private List<OrganizationData> mOrganizationList;
    private List<ImData> mImList;
    private List<PhotoData> mPhotoList;
    private List<String> mWebsiteList;
    private List<List<String>> mAndroidCustomPropertyList;
    private final int mVCardType;
    private final Account mAccount;
    public VCardEntry() {
        this(VCardConfig.VCARD_TYPE_V21_GENERIC_UTF8);
    }
    public VCardEntry(int vcardType) {
        this(vcardType, null);
    }
    public VCardEntry(int vcardType, Account account) {
        mVCardType = vcardType;
        mAccount = account;
    }
    private void addPhone(int type, String data, String label, boolean isPrimary) {
        if (mPhoneList == null) {
            mPhoneList = new ArrayList<PhoneData>();
        }
        final StringBuilder builder = new StringBuilder();
        final String trimed = data.trim();
        final String formattedNumber;
        if (type == Phone.TYPE_PAGER) {
            formattedNumber = trimed;
        } else {
            final int length = trimed.length();
            for (int i = 0; i < length; i++) {
                char ch = trimed.charAt(i);
                if (('0' <= ch && ch <= '9') || (i == 0 && ch == '+')) {
                    builder.append(ch);
                }
            }
            final int formattingType = (VCardConfig.isJapaneseDevice(mVCardType) ?
                    PhoneNumberUtils.FORMAT_JAPAN : PhoneNumberUtils.FORMAT_NANP);
            formattedNumber = PhoneNumberUtils.formatNumber(builder.toString(), formattingType);
        }
        PhoneData phoneData = new PhoneData(type, formattedNumber, label, isPrimary);
        mPhoneList.add(phoneData);
    }
    private void addNickName(final String nickName) {
        if (mNickNameList == null) {
            mNickNameList = new ArrayList<String>();
        }
        mNickNameList.add(nickName);
    }
    private void addEmail(int type, String data, String label, boolean isPrimary){
        if (mEmailList == null) {
            mEmailList = new ArrayList<EmailData>();
        }
        mEmailList.add(new EmailData(type, data, label, isPrimary));
    }
    private void addPostal(int type, List<String> propValueList, String label, boolean isPrimary){
        if (mPostalList == null) {
            mPostalList = new ArrayList<PostalData>(0);
        }
        mPostalList.add(new PostalData(type, propValueList, label, isPrimary));
    }
    private void addNewOrganization(int type, final String companyName,
            final String departmentName,
            final String titleName, boolean isPrimary) {
        if (mOrganizationList == null) {
            mOrganizationList = new ArrayList<OrganizationData>();
        }
        mOrganizationList.add(new OrganizationData(type, companyName,
                departmentName, titleName, isPrimary));
    }
    private static final List<String> sEmptyList =
            Collections.unmodifiableList(new ArrayList<String>(0));
    private void handleOrgValue(final int type, List<String> orgList, boolean isPrimary) {
        if (orgList == null) {
            orgList = sEmptyList;
        }
        final String companyName;
        final String departmentName;
        final int size = orgList.size();
        switch (size) {
            case 0: {
                companyName = "";
                departmentName = null;
                break;
            }
            case 1: {
                companyName = orgList.get(0);
                departmentName = null;
                break;
            }
            default: {  
                companyName = orgList.get(0);
                StringBuilder builder = new StringBuilder();
                for (int i = 1; i < size; i++) {
                    if (i > 1) {
                        builder.append(' ');
                    }
                    builder.append(orgList.get(i));
                }
                departmentName = builder.toString();
            }
        }
        if (mOrganizationList == null) {
            addNewOrganization(type, companyName, departmentName, null, isPrimary);
            return;
        }
        for (OrganizationData organizationData : mOrganizationList) {
            if (organizationData.companyName == null &&
                    organizationData.departmentName == null) {
                organizationData.companyName = companyName;
                organizationData.departmentName = departmentName;
                organizationData.isPrimary = isPrimary;
                return;
            }
        }
        addNewOrganization(type, companyName, departmentName, null, isPrimary);
    }
    private void handleTitleValue(final String title) {
        if (mOrganizationList == null) {
            addNewOrganization(DEFAULT_ORGANIZATION_TYPE, null, null, title, false);
            return;
        }
        for (OrganizationData organizationData : mOrganizationList) {
            if (organizationData.titleName == null) {
                organizationData.titleName = title;
                return;
            }
        }
        addNewOrganization(DEFAULT_ORGANIZATION_TYPE, null, null, title, false);
    }
    private void addIm(int protocol, String customProtocol, int type,
            String propValue, boolean isPrimary) {
        if (mImList == null) {
            mImList = new ArrayList<ImData>();
        }
        mImList.add(new ImData(protocol, customProtocol, type, propValue, isPrimary));
    }
    private void addNote(final String note) {
        if (mNoteList == null) {
            mNoteList = new ArrayList<String>(1);
        }
        mNoteList.add(note);
    }
    private void addPhotoBytes(String formatName, byte[] photoBytes, boolean isPrimary) {
        if (mPhotoList == null) {
            mPhotoList = new ArrayList<PhotoData>(1);
        }
        final PhotoData photoData = new PhotoData(0, null, photoBytes, isPrimary);
        mPhotoList.add(photoData);
    }
    @SuppressWarnings("fallthrough")
    private void handleNProperty(List<String> elems) {
        int size;
        if (elems == null || (size = elems.size()) < 1) {
            return;
        }
        if (size > 5) {
            size = 5;
        }
        switch (size) {
            case 5: mSuffix = elems.get(4);
            case 4: mPrefix = elems.get(3);
            case 3: mMiddleName = elems.get(2);
            case 2: mGivenName = elems.get(1);
            default: mFamilyName = elems.get(0);
        }
    }
    @SuppressWarnings("fallthrough")
    private void handlePhoneticNameFromSound(List<String> elems) {
        if (!(TextUtils.isEmpty(mPhoneticFamilyName) &&
                TextUtils.isEmpty(mPhoneticMiddleName) &&
                TextUtils.isEmpty(mPhoneticGivenName))) {
            return;
        }
        int size;
        if (elems == null || (size = elems.size()) < 1) {
            return;
        }
        if (size > 3) {
            size = 3;
        }
        if (elems.get(0).length() > 0) {
            boolean onlyFirstElemIsNonEmpty = true;
            for (int i = 1; i < size; i++) {
                if (elems.get(i).length() > 0) {
                    onlyFirstElemIsNonEmpty = false;
                    break;
                }
            }
            if (onlyFirstElemIsNonEmpty) {
                final String[] namesArray = elems.get(0).split(" ");
                final int nameArrayLength = namesArray.length;
                if (nameArrayLength == 3) {
                    mPhoneticFamilyName = namesArray[0];
                    mPhoneticMiddleName = namesArray[1];
                    mPhoneticGivenName = namesArray[2];
                } else if (nameArrayLength == 2) {
                    mPhoneticFamilyName = namesArray[0];
                    mPhoneticGivenName = namesArray[1];
                } else {
                    mPhoneticFullName = elems.get(0);
                }
                return;
            }
        }
        switch (size) {
            case 3: mPhoneticMiddleName = elems.get(2);
            case 2: mPhoneticGivenName = elems.get(1);
            default: mPhoneticFamilyName = elems.get(0);
        }
    }
    public void addProperty(final Property property) {
        final String propName = property.mPropertyName;
        final Map<String, Collection<String>> paramMap = property.mParameterMap;
        final List<String> propValueList = property.mPropertyValueList;
        byte[] propBytes = property.mPropertyBytes;
        if (propValueList.size() == 0) {
            return;
        }
        final String propValue = listToString(propValueList).trim();
        if (propName.equals(VCardConstants.PROPERTY_VERSION)) {
        } else if (propName.equals(VCardConstants.PROPERTY_FN)) {
            mFullName = propValue;
        } else if (propName.equals(VCardConstants.PROPERTY_NAME) && mFullName == null) {
            mFullName = propValue;
        } else if (propName.equals(VCardConstants.PROPERTY_N)) {
            handleNProperty(propValueList);
        } else if (propName.equals(VCardConstants.PROPERTY_SORT_STRING)) {
            mPhoneticFullName = propValue;
        } else if (propName.equals(VCardConstants.PROPERTY_NICKNAME) ||
                propName.equals(VCardConstants.ImportOnly.PROPERTY_X_NICKNAME)) {
            addNickName(propValue);
        } else if (propName.equals(VCardConstants.PROPERTY_SOUND)) {
            Collection<String> typeCollection = paramMap.get(VCardConstants.PARAM_TYPE);
            if (typeCollection != null
                    && typeCollection.contains(VCardConstants.PARAM_TYPE_X_IRMC_N)) {
                final List<String> phoneticNameList =
                        VCardUtils.constructListFromValue(propValue,
                                VCardConfig.isV30(mVCardType));
                handlePhoneticNameFromSound(phoneticNameList);
            } else {
            }
        } else if (propName.equals(VCardConstants.PROPERTY_ADR)) {
            boolean valuesAreAllEmpty = true;
            for (String value : propValueList) {
                if (value.length() > 0) {
                    valuesAreAllEmpty = false;
                    break;
                }
            }
            if (valuesAreAllEmpty) {
                return;
            }
            int type = -1;
            String label = "";
            boolean isPrimary = false;
            Collection<String> typeCollection = paramMap.get(VCardConstants.PARAM_TYPE);
            if (typeCollection != null) {
                for (String typeString : typeCollection) {
                    typeString = typeString.toUpperCase();
                    if (typeString.equals(VCardConstants.PARAM_TYPE_PREF)) {
                        isPrimary = true;
                    } else if (typeString.equals(VCardConstants.PARAM_TYPE_HOME)) {
                        type = StructuredPostal.TYPE_HOME;
                        label = "";
                    } else if (typeString.equals(VCardConstants.PARAM_TYPE_WORK) ||
                            typeString.equalsIgnoreCase(VCardConstants.PARAM_EXTRA_TYPE_COMPANY)) {
                        type = StructuredPostal.TYPE_WORK;
                        label = "";
                    } else if (typeString.equals(VCardConstants.PARAM_ADR_TYPE_PARCEL) ||
                            typeString.equals(VCardConstants.PARAM_ADR_TYPE_DOM) ||
                            typeString.equals(VCardConstants.PARAM_ADR_TYPE_INTL)) {
                    } else {
                        if (typeString.startsWith("X-") && type < 0) {
                            typeString = typeString.substring(2);
                        }
                        type = StructuredPostal.TYPE_CUSTOM;
                        label = typeString;
                    }
                }
            }
            if (type < 0) {
                type = StructuredPostal.TYPE_HOME;
            }
            addPostal(type, propValueList, label, isPrimary);
        } else if (propName.equals(VCardConstants.PROPERTY_EMAIL)) {
            int type = -1;
            String label = null;
            boolean isPrimary = false;
            Collection<String> typeCollection = paramMap.get(VCardConstants.PARAM_TYPE);
            if (typeCollection != null) {
                for (String typeString : typeCollection) {
                    typeString = typeString.toUpperCase();
                    if (typeString.equals(VCardConstants.PARAM_TYPE_PREF)) {
                        isPrimary = true;
                    } else if (typeString.equals(VCardConstants.PARAM_TYPE_HOME)) {
                        type = Email.TYPE_HOME;
                    } else if (typeString.equals(VCardConstants.PARAM_TYPE_WORK)) {
                        type = Email.TYPE_WORK;
                    } else if (typeString.equals(VCardConstants.PARAM_TYPE_CELL)) {
                        type = Email.TYPE_MOBILE;
                    } else {
                        if (typeString.startsWith("X-") && type < 0) {
                            typeString = typeString.substring(2);
                        }
                        type = Email.TYPE_CUSTOM;
                        label = typeString;
                    }
                }
            }
            if (type < 0) {
                type = Email.TYPE_OTHER;
            }
            addEmail(type, propValue, label, isPrimary);
        } else if (propName.equals(VCardConstants.PROPERTY_ORG)) {
            final int type = Organization.TYPE_WORK;
            boolean isPrimary = false;
            Collection<String> typeCollection = paramMap.get(VCardConstants.PARAM_TYPE);
            if (typeCollection != null) {
                for (String typeString : typeCollection) {
                    if (typeString.equals(VCardConstants.PARAM_TYPE_PREF)) {
                        isPrimary = true;
                    }
                }
            }
            handleOrgValue(type, propValueList, isPrimary);
        } else if (propName.equals(VCardConstants.PROPERTY_TITLE)) {
            handleTitleValue(propValue);
        } else if (propName.equals(VCardConstants.PROPERTY_ROLE)) {
        } else if (propName.equals(VCardConstants.PROPERTY_PHOTO) ||
                propName.equals(VCardConstants.PROPERTY_LOGO)) {
            Collection<String> paramMapValue = paramMap.get("VALUE");
            if (paramMapValue != null && paramMapValue.contains("URL")) {
            } else {
                final Collection<String> typeCollection = paramMap.get("TYPE");
                String formatName = null;
                boolean isPrimary = false;
                if (typeCollection != null) {
                    for (String typeValue : typeCollection) {
                        if (VCardConstants.PARAM_TYPE_PREF.equals(typeValue)) {
                            isPrimary = true;
                        } else if (formatName == null){
                            formatName = typeValue;
                        }
                    }
                }
                addPhotoBytes(formatName, propBytes, isPrimary);
            }
        } else if (propName.equals(VCardConstants.PROPERTY_TEL)) {
            final Collection<String> typeCollection = paramMap.get(VCardConstants.PARAM_TYPE);
            final Object typeObject =
                VCardUtils.getPhoneTypeFromStrings(typeCollection, propValue);
            final int type;
            final String label;
            if (typeObject instanceof Integer) {
                type = (Integer)typeObject;
                label = null;
            } else {
                type = Phone.TYPE_CUSTOM;
                label = typeObject.toString();
            }
            final boolean isPrimary;
            if (typeCollection != null && typeCollection.contains(VCardConstants.PARAM_TYPE_PREF)) {
                isPrimary = true;
            } else {
                isPrimary = false;
            }
            addPhone(type, propValue, label, isPrimary);
        } else if (propName.equals(VCardConstants.PROPERTY_X_SKYPE_PSTNNUMBER)) {
            Collection<String> typeCollection = paramMap.get(VCardConstants.PARAM_TYPE);
            final int type = Phone.TYPE_OTHER;
            final boolean isPrimary;
            if (typeCollection != null && typeCollection.contains(VCardConstants.PARAM_TYPE_PREF)) {
                isPrimary = true;
            } else {
                isPrimary = false;
            }
            addPhone(type, propValue, null, isPrimary);
        } else if (sImMap.containsKey(propName)) {
            final int protocol = sImMap.get(propName);
            boolean isPrimary = false;
            int type = -1;
            final Collection<String> typeCollection = paramMap.get(VCardConstants.PARAM_TYPE);
            if (typeCollection != null) {
                for (String typeString : typeCollection) {
                    if (typeString.equals(VCardConstants.PARAM_TYPE_PREF)) {
                        isPrimary = true;
                    } else if (type < 0) {
                        if (typeString.equalsIgnoreCase(VCardConstants.PARAM_TYPE_HOME)) {
                            type = Im.TYPE_HOME;
                        } else if (typeString.equalsIgnoreCase(VCardConstants.PARAM_TYPE_WORK)) {
                            type = Im.TYPE_WORK;
                        }
                    }
                }
            }
            if (type < 0) {
                type = Phone.TYPE_HOME;
            }
            addIm(protocol, null, type, propValue, isPrimary);
        } else if (propName.equals(VCardConstants.PROPERTY_NOTE)) {
            addNote(propValue);
        } else if (propName.equals(VCardConstants.PROPERTY_URL)) {
            if (mWebsiteList == null) {
                mWebsiteList = new ArrayList<String>(1);
            }
            mWebsiteList.add(propValue);
        } else if (propName.equals(VCardConstants.PROPERTY_BDAY)) {
            mBirthday = propValue;
        } else if (propName.equals(VCardConstants.PROPERTY_X_PHONETIC_FIRST_NAME)) {
            mPhoneticGivenName = propValue;
        } else if (propName.equals(VCardConstants.PROPERTY_X_PHONETIC_MIDDLE_NAME)) {
            mPhoneticMiddleName = propValue;
        } else if (propName.equals(VCardConstants.PROPERTY_X_PHONETIC_LAST_NAME)) {
            mPhoneticFamilyName = propValue;
        } else if (propName.equals(VCardConstants.PROPERTY_X_ANDROID_CUSTOM)) {
            final List<String> customPropertyList =
                VCardUtils.constructListFromValue(propValue,
                        VCardConfig.isV30(mVCardType));
            handleAndroidCustomProperty(customPropertyList);
    private void constructDisplayName() {
        if (!TextUtils.isEmpty(mFullName)) {
            mDisplayName = mFullName;
        } else if (!(TextUtils.isEmpty(mFamilyName) && TextUtils.isEmpty(mGivenName))) {
            mDisplayName = VCardUtils.constructNameFromElements(mVCardType,
                    mFamilyName, mMiddleName, mGivenName, mPrefix, mSuffix);
        } else if (!(TextUtils.isEmpty(mPhoneticFamilyName) &&
                TextUtils.isEmpty(mPhoneticGivenName))) {
            mDisplayName = VCardUtils.constructNameFromElements(mVCardType,
                    mPhoneticFamilyName, mPhoneticMiddleName, mPhoneticGivenName);
        } else if (mEmailList != null && mEmailList.size() > 0) {
            mDisplayName = mEmailList.get(0).data;
        } else if (mPhoneList != null && mPhoneList.size() > 0) {
            mDisplayName = mPhoneList.get(0).data;
        } else if (mPostalList != null && mPostalList.size() > 0) {
            mDisplayName = mPostalList.get(0).getFormattedAddress(mVCardType);
        } else if (mOrganizationList != null && mOrganizationList.size() > 0) {
            mDisplayName = mOrganizationList.get(0).getFormattedString();
        }
        if (mDisplayName == null) {
            mDisplayName = "";
        }
    }
    public void consolidateFields() {
        constructDisplayName();
        if (mPhoneticFullName != null) {
            mPhoneticFullName = mPhoneticFullName.trim();
        }
    }
    public Uri pushIntoContentResolver(ContentResolver resolver) {
        ArrayList<ContentProviderOperation> operationList =
            new ArrayList<ContentProviderOperation>();
        ContentProviderOperation.Builder builder =
            ContentProviderOperation.newInsert(RawContacts.CONTENT_URI);
        String myGroupsId = null;
        if (mAccount != null) {
            builder.withValue(RawContacts.ACCOUNT_NAME, mAccount.name);
            builder.withValue(RawContacts.ACCOUNT_TYPE, mAccount.type);
            if (ACCOUNT_TYPE_GOOGLE.equals(mAccount.type)) {
                final Cursor cursor = resolver.query(Groups.CONTENT_URI, new String[] {
                        Groups.SOURCE_ID },
                        Groups.TITLE + "=?", new String[] {
                        GOOGLE_MY_CONTACTS_GROUP }, null);
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        myGroupsId = cursor.getString(0);
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        } else {
            builder.withValue(RawContacts.ACCOUNT_NAME, null);
            builder.withValue(RawContacts.ACCOUNT_TYPE, null);
        }
        operationList.add(builder.build());
        if (!nameFieldsAreEmpty()) {
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference(StructuredName.RAW_CONTACT_ID, 0);
            builder.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
            builder.withValue(StructuredName.GIVEN_NAME, mGivenName);
            builder.withValue(StructuredName.FAMILY_NAME, mFamilyName);
            builder.withValue(StructuredName.MIDDLE_NAME, mMiddleName);
            builder.withValue(StructuredName.PREFIX, mPrefix);
            builder.withValue(StructuredName.SUFFIX, mSuffix);
            if (!(TextUtils.isEmpty(mPhoneticGivenName)
                    && TextUtils.isEmpty(mPhoneticFamilyName)
                    && TextUtils.isEmpty(mPhoneticMiddleName))) {
                builder.withValue(StructuredName.PHONETIC_GIVEN_NAME, mPhoneticGivenName);
                builder.withValue(StructuredName.PHONETIC_FAMILY_NAME, mPhoneticFamilyName);
                builder.withValue(StructuredName.PHONETIC_MIDDLE_NAME, mPhoneticMiddleName);
            } else if (!TextUtils.isEmpty(mPhoneticFullName)) {
                builder.withValue(StructuredName.PHONETIC_GIVEN_NAME, mPhoneticFullName);
            }
            builder.withValue(StructuredName.DISPLAY_NAME, getDisplayName());
            operationList.add(builder.build());
        }
        if (mNickNameList != null && mNickNameList.size() > 0) {
            for (String nickName : mNickNameList) {
                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                builder.withValueBackReference(Nickname.RAW_CONTACT_ID, 0);
                builder.withValue(Data.MIMETYPE, Nickname.CONTENT_ITEM_TYPE);
                builder.withValue(Nickname.TYPE, Nickname.TYPE_DEFAULT);
                builder.withValue(Nickname.NAME, nickName);
                operationList.add(builder.build());
            }
        }
        if (mPhoneList != null) {
            for (PhoneData phoneData : mPhoneList) {
                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                builder.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
                builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                builder.withValue(Phone.TYPE, phoneData.type);
                if (phoneData.type == Phone.TYPE_CUSTOM) {
                    builder.withValue(Phone.LABEL, phoneData.label);
                }
                builder.withValue(Phone.NUMBER, phoneData.data);
                if (phoneData.isPrimary) {
                    builder.withValue(Phone.IS_PRIMARY, 1);
                }
                operationList.add(builder.build());
            }
        }
        if (mOrganizationList != null) {
            for (OrganizationData organizationData : mOrganizationList) {
                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                builder.withValueBackReference(Organization.RAW_CONTACT_ID, 0);
                builder.withValue(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE);
                builder.withValue(Organization.TYPE, organizationData.type);
                if (organizationData.companyName != null) {
                    builder.withValue(Organization.COMPANY, organizationData.companyName);
                }
                if (organizationData.departmentName != null) {
                    builder.withValue(Organization.DEPARTMENT, organizationData.departmentName);
                }
                if (organizationData.titleName != null) {
                    builder.withValue(Organization.TITLE, organizationData.titleName);
                }
                if (organizationData.isPrimary) {
                    builder.withValue(Organization.IS_PRIMARY, 1);
                }
                operationList.add(builder.build());
            }
        }
        if (mEmailList != null) {
            for (EmailData emailData : mEmailList) {
                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                builder.withValueBackReference(Email.RAW_CONTACT_ID, 0);
                builder.withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
                builder.withValue(Email.TYPE, emailData.type);
                if (emailData.type == Email.TYPE_CUSTOM) {
                    builder.withValue(Email.LABEL, emailData.label);
                }
                builder.withValue(Email.DATA, emailData.data);
                if (emailData.isPrimary) {
                    builder.withValue(Data.IS_PRIMARY, 1);
                }
                operationList.add(builder.build());
            }
        }
        if (mPostalList != null) {
            for (PostalData postalData : mPostalList) {
                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                VCardUtils.insertStructuredPostalDataUsingContactsStruct(
                        mVCardType, builder, postalData);
                operationList.add(builder.build());
            }
        }
        if (mImList != null) {
            for (ImData imData : mImList) {
                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                builder.withValueBackReference(Im.RAW_CONTACT_ID, 0);
                builder.withValue(Data.MIMETYPE, Im.CONTENT_ITEM_TYPE);
                builder.withValue(Im.TYPE, imData.type);
                builder.withValue(Im.PROTOCOL, imData.protocol);
                if (imData.protocol == Im.PROTOCOL_CUSTOM) {
                    builder.withValue(Im.CUSTOM_PROTOCOL, imData.customProtocol);
                }
                if (imData.isPrimary) {
                    builder.withValue(Data.IS_PRIMARY, 1);
                }
            }
        }
        if (mNoteList != null) {
            for (String note : mNoteList) {
                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                builder.withValueBackReference(Note.RAW_CONTACT_ID, 0);
                builder.withValue(Data.MIMETYPE, Note.CONTENT_ITEM_TYPE);
                builder.withValue(Note.NOTE, note);
                operationList.add(builder.build());
            }
        }
        if (mPhotoList != null) {
            for (PhotoData photoData : mPhotoList) {
                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                builder.withValueBackReference(Photo.RAW_CONTACT_ID, 0);
                builder.withValue(Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE);
                builder.withValue(Photo.PHOTO, photoData.photoBytes);
                if (photoData.isPrimary) {
                    builder.withValue(Photo.IS_PRIMARY, 1);
                }
                operationList.add(builder.build());
            }
        }
        if (mWebsiteList != null) {
            for (String website : mWebsiteList) {
                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                builder.withValueBackReference(Website.RAW_CONTACT_ID, 0);
                builder.withValue(Data.MIMETYPE, Website.CONTENT_ITEM_TYPE);
                builder.withValue(Website.URL, website);
                builder.withValue(Website.TYPE, Website.TYPE_HOMEPAGE);
                operationList.add(builder.build());
            }
        }
        if (!TextUtils.isEmpty(mBirthday)) {
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference(Event.RAW_CONTACT_ID, 0);
            builder.withValue(Data.MIMETYPE, Event.CONTENT_ITEM_TYPE);
            builder.withValue(Event.START_DATE, mBirthday);
            builder.withValue(Event.TYPE, Event.TYPE_BIRTHDAY);
            operationList.add(builder.build());
        }
        if (mAndroidCustomPropertyList != null) {
            for (List<String> customPropertyList : mAndroidCustomPropertyList) {
                int size = customPropertyList.size();
                if (size < 2 || TextUtils.isEmpty(customPropertyList.get(0))) {
                    continue;
                } else if (size > VCardConstants.MAX_DATA_COLUMN + 1) {
                    size = VCardConstants.MAX_DATA_COLUMN + 1;
                    customPropertyList =
                        customPropertyList.subList(0, VCardConstants.MAX_DATA_COLUMN + 2);
                }
                int i = 0;
                for (final String customPropertyValue : customPropertyList) {
                    if (i == 0) {
                        final String mimeType = customPropertyValue;
                        builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                        builder.withValueBackReference(GroupMembership.RAW_CONTACT_ID, 0);
                        builder.withValue(Data.MIMETYPE, mimeType);
                    } else {  
                        if (!TextUtils.isEmpty(customPropertyValue)) {
                            builder.withValue("data" + i, customPropertyValue);
                        }
                    }
                    i++;
                }
                operationList.add(builder.build());
            }
        }
        if (myGroupsId != null) {
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference(GroupMembership.RAW_CONTACT_ID, 0);
            builder.withValue(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
            builder.withValue(GroupMembership.GROUP_SOURCE_ID, myGroupsId);
            operationList.add(builder.build());
        }
        try {
            ContentProviderResult[] results = resolver.applyBatch(
                        ContactsContract.AUTHORITY, operationList);
            return (results == null || results.length == 0 || results[0] == null)
                ? null
                : results[0].uri;
        } catch (RemoteException e) {
            Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
            return null;
        } catch (OperationApplicationException e) {
            Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
            return null;
        }
    }
    public static VCardEntry buildFromResolver(ContentResolver resolver) {
        return buildFromResolver(resolver, Contacts.CONTENT_URI);
    }
    public static VCardEntry buildFromResolver(ContentResolver resolver, Uri uri) {
        return null;
    }
    private boolean nameFieldsAreEmpty() {
        return (TextUtils.isEmpty(mFamilyName)
                && TextUtils.isEmpty(mMiddleName)
                && TextUtils.isEmpty(mGivenName)
                && TextUtils.isEmpty(mPrefix)
                && TextUtils.isEmpty(mSuffix)
                && TextUtils.isEmpty(mFullName)
                && TextUtils.isEmpty(mPhoneticFamilyName)
                && TextUtils.isEmpty(mPhoneticMiddleName)
                && TextUtils.isEmpty(mPhoneticGivenName)
                && TextUtils.isEmpty(mPhoneticFullName));
    }
    public boolean isIgnorable() {
        return getDisplayName().length() == 0;
    }
    private String listToString(List<String> list){
        final int size = list.size();
        if (size > 1) {
            StringBuilder builder = new StringBuilder();
            int i = 0;
            for (String type : list) {
                builder.append(type);
                if (i < size - 1) {
                    builder.append(";");
                }
            }
            return builder.toString();
        } else if (size == 1) {
            return list.get(0);
        } else {
            return "";
        }
    }
    public String getFamilyName() {
        return mFamilyName;
    }
    public String getGivenName() {
        return mGivenName;
    }
    public String getMiddleName() {
        return mMiddleName;
    }
    public String getPrefix() {
        return mPrefix;
    }
    public String getSuffix() {
        return mSuffix;
    }
    public String getFullName() {
        return mFullName;
    }
    public String getPhoneticFamilyName() {
        return mPhoneticFamilyName;
    }
    public String getPhoneticGivenName() {
        return mPhoneticGivenName;
    }
    public String getPhoneticMiddleName() {
        return mPhoneticMiddleName;
    }
    public String getPhoneticFullName() {
        return mPhoneticFullName;
    }
    public final List<String> getNickNameList() {
        return mNickNameList;
    }
    public String getBirthday() {
        return mBirthday;
    }
    public final List<String> getNotes() {
        return mNoteList;
    }
    public final List<PhoneData> getPhoneList() {
        return mPhoneList;
    }
    public final List<EmailData> getEmailList() {
        return mEmailList;
    }
    public final List<PostalData> getPostalList() {
        return mPostalList;
    }
    public final List<OrganizationData> getOrganizationList() {
        return mOrganizationList;
    }
    public final List<ImData> getImList() {
        return mImList;
    }
    public final List<PhotoData> getPhotoList() {
        return mPhotoList;
    }
    public final List<String> getWebsiteList() {
        return mWebsiteList;
    }
    public String getDisplayName() {
        if (mDisplayName == null) {
            constructDisplayName();
        }
        return mDisplayName;
    }
}
