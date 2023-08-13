public class ICC_Profile implements Serializable {
    private static final long serialVersionUID = -3938515861990936766L;
    transient long ID;
    private transient ProfileDeferralInfo deferralInfo;
    private transient ProfileActivator profileActivator;
    private static ICC_Profile sRGBprofile;
    private static ICC_Profile XYZprofile;
    private static ICC_Profile PYCCprofile;
    private static ICC_Profile GRAYprofile;
    private static ICC_Profile LINEAR_RGBprofile;
    public static final int CLASS_INPUT = 0;
    public static final int CLASS_DISPLAY = 1;
    public static final int CLASS_OUTPUT = 2;
    public static final int CLASS_DEVICELINK = 3;
    public static final int CLASS_COLORSPACECONVERSION = 4;
    public static final int CLASS_ABSTRACT = 5;
    public static final int CLASS_NAMEDCOLOR = 6;
    public static final int icSigXYZData        = 0x58595A20;    
    public static final int icSigLabData        = 0x4C616220;    
    public static final int icSigLuvData        = 0x4C757620;    
    public static final int icSigYCbCrData        = 0x59436272;    
    public static final int icSigYxyData        = 0x59787920;    
    public static final int icSigRgbData        = 0x52474220;    
    public static final int icSigGrayData        = 0x47524159;    
    public static final int icSigHsvData        = 0x48535620;    
    public static final int icSigHlsData        = 0x484C5320;    
    public static final int icSigCmykData        = 0x434D594B;    
    public static final int icSigCmyData        = 0x434D5920;    
    public static final int icSigSpace2CLR        = 0x32434C52;    
    public static final int icSigSpace3CLR        = 0x33434C52;    
    public static final int icSigSpace4CLR        = 0x34434C52;    
    public static final int icSigSpace5CLR        = 0x35434C52;    
    public static final int icSigSpace6CLR        = 0x36434C52;    
    public static final int icSigSpace7CLR        = 0x37434C52;    
    public static final int icSigSpace8CLR        = 0x38434C52;    
    public static final int icSigSpace9CLR        = 0x39434C52;    
    public static final int icSigSpaceACLR        = 0x41434C52;    
    public static final int icSigSpaceBCLR        = 0x42434C52;    
    public static final int icSigSpaceCCLR        = 0x43434C52;    
    public static final int icSigSpaceDCLR        = 0x44434C52;    
    public static final int icSigSpaceECLR        = 0x45434C52;    
    public static final int icSigSpaceFCLR        = 0x46434C52;    
    public static final int icSigInputClass       = 0x73636E72;    
    public static final int icSigDisplayClass     = 0x6D6E7472;    
    public static final int icSigOutputClass      = 0x70727472;    
    public static final int icSigLinkClass        = 0x6C696E6B;    
    public static final int icSigAbstractClass    = 0x61627374;    
    public static final int icSigColorSpaceClass  = 0x73706163;    
    public static final int icSigNamedColorClass  = 0x6e6d636c;    
    public static final int icPerceptual            = 0;
    public static final int icRelativeColorimetric    = 1;
    public static final int icMediaRelativeColorimetric = 1;
    public static final int icSaturation            = 2;
    public static final int icAbsoluteColorimetric    = 3;
    public static final int icICCAbsoluteColorimetric = 3;
    public static final int icSigHead      = 0x68656164; 
    public static final int icSigAToB0Tag         = 0x41324230;    
    public static final int icSigAToB1Tag         = 0x41324231;    
    public static final int icSigAToB2Tag         = 0x41324232;    
    public static final int icSigBlueColorantTag  = 0x6258595A;    
    public static final int icSigBlueMatrixColumnTag = 0x6258595A; 
    public static final int icSigBlueTRCTag       = 0x62545243;    
    public static final int icSigBToA0Tag         = 0x42324130;    
    public static final int icSigBToA1Tag         = 0x42324131;    
    public static final int icSigBToA2Tag         = 0x42324132;    
    public static final int icSigCalibrationDateTimeTag = 0x63616C74;
    public static final int icSigCharTargetTag    = 0x74617267;    
    public static final int icSigCopyrightTag     = 0x63707274;    
    public static final int icSigCrdInfoTag       = 0x63726469;    
    public static final int icSigDeviceMfgDescTag = 0x646D6E64;    
    public static final int icSigDeviceModelDescTag = 0x646D6464;  
    public static final int icSigDeviceSettingsTag =  0x64657673;  
    public static final int icSigGamutTag         = 0x67616D74;    
    public static final int icSigGrayTRCTag       = 0x6b545243;    
    public static final int icSigGreenColorantTag = 0x6758595A;    
    public static final int icSigGreenMatrixColumnTag = 0x6758595A;
    public static final int icSigGreenTRCTag      = 0x67545243;    
    public static final int icSigLuminanceTag     = 0x6C756d69;    
    public static final int icSigMeasurementTag   = 0x6D656173;    
    public static final int icSigMediaBlackPointTag = 0x626B7074;  
    public static final int icSigMediaWhitePointTag = 0x77747074;  
    public static final int icSigNamedColor2Tag   = 0x6E636C32;    
    public static final int icSigOutputResponseTag = 0x72657370;   
    public static final int icSigPreview0Tag      = 0x70726530;    
    public static final int icSigPreview1Tag      = 0x70726531;    
    public static final int icSigPreview2Tag      = 0x70726532;    
    public static final int icSigProfileDescriptionTag = 0x64657363;
    public static final int icSigProfileSequenceDescTag = 0x70736571;
    public static final int icSigPs2CRD0Tag       = 0x70736430;    
    public static final int icSigPs2CRD1Tag       = 0x70736431;    
    public static final int icSigPs2CRD2Tag       = 0x70736432;    
    public static final int icSigPs2CRD3Tag       = 0x70736433;    
    public static final int icSigPs2CSATag        = 0x70733273;    
    public static final int icSigPs2RenderingIntentTag = 0x70733269;
    public static final int icSigRedColorantTag   = 0x7258595A;    
    public static final int icSigRedMatrixColumnTag = 0x7258595A;  
    public static final int icSigRedTRCTag        = 0x72545243;    
    public static final int icSigScreeningDescTag = 0x73637264;    
    public static final int icSigScreeningTag     = 0x7363726E;    
    public static final int icSigTechnologyTag    = 0x74656368;    
    public static final int icSigUcrBgTag         = 0x62666420;    
    public static final int icSigViewingCondDescTag = 0x76756564;  
    public static final int icSigViewingConditionsTag = 0x76696577;
    public static final int icSigChromaticityTag  = 0x6368726d;    
    public static final int icSigChromaticAdaptationTag = 0x63686164;
    public static final int icSigColorantOrderTag = 0x636C726F;    
    public static final int icSigColorantTableTag = 0x636C7274;    
    public static final int icHdrSize         = 0;  
    public static final int icHdrCmmId        = 4;  
    public static final int icHdrVersion      = 8;  
    public static final int icHdrDeviceClass  = 12; 
    public static final int icHdrColorSpace   = 16; 
    public static final int icHdrPcs          = 20; 
    public static final int icHdrDate       = 24; 
    public static final int icHdrMagic        = 36; 
    public static final int icHdrPlatform     = 40; 
    public static final int icHdrFlags        = 44; 
    public static final int icHdrManufacturer = 48; 
    public static final int icHdrModel        = 52; 
    public static final int icHdrAttributes   = 56; 
    public static final int icHdrRenderingIntent = 64; 
    public static final int icHdrIlluminant   = 68; 
    public static final int icHdrCreator      = 80; 
    public static final int icHdrProfileID = 84; 
    public static final int icTagType          = 0;    
    public static final int icTagReserved      = 4;    
    public static final int icCurveCount       = 8;    
    public static final int icCurveData        = 12;   
    public static final int icXYZNumberX       = 8;    
    ICC_Profile(long ID) {
        this.ID = ID;
    }
    ICC_Profile(ProfileDeferralInfo pdi) {
        this.deferralInfo = pdi;
        this.profileActivator = new ProfileActivator() {
            public void activate() throws ProfileDataException {
                activateDeferredProfile();
            }
        };
        ProfileDeferralMgr.registerDeferral(this.profileActivator);
    }
    protected void finalize () {
        if (ID != 0) {
            CMSManager.getModule().freeProfile(ID);
        } else if (profileActivator != null) {
            ProfileDeferralMgr.unregisterDeferral(profileActivator);
        }
    }
    public static ICC_Profile getInstance(byte[] data) {
    ICC_Profile thisProfile;
        long theID;
        if (ProfileDeferralMgr.deferring) {
            ProfileDeferralMgr.activateProfiles();
        }
        try {
            theID = CMSManager.getModule().loadProfile(data);
        } catch (CMMException c) {
            throw new IllegalArgumentException("Invalid ICC Profile Data");
        }
        try {
            if ((getColorSpaceType (theID) == ColorSpace.TYPE_GRAY) &&
                (getData (theID, icSigMediaWhitePointTag) != null) &&
                (getData (theID, icSigGrayTRCTag) != null)) {
                thisProfile = new ICC_ProfileGray (theID);
            }
            else if ((getColorSpaceType (theID) == ColorSpace.TYPE_RGB) &&
                (getData (theID, icSigMediaWhitePointTag) != null) &&
                (getData (theID, icSigRedColorantTag) != null) &&
                (getData (theID, icSigGreenColorantTag) != null) &&
                (getData (theID, icSigBlueColorantTag) != null) &&
                (getData (theID, icSigRedTRCTag) != null) &&
                (getData (theID, icSigGreenTRCTag) != null) &&
                (getData (theID, icSigBlueTRCTag) != null)) {
                thisProfile = new ICC_ProfileRGB (theID);
            }
            else {
                thisProfile = new ICC_Profile (theID);
            }
        } catch (CMMException c) {
            thisProfile = new ICC_Profile (theID);
        }
        return thisProfile;
    }
    public static ICC_Profile getInstance (int cspace) {
        ICC_Profile thisProfile = null;
        String fileName;
        switch (cspace) {
        case ColorSpace.CS_sRGB:
            synchronized(ICC_Profile.class) {
                if (sRGBprofile == null) {
                    ProfileDeferralInfo pInfo =
                        new ProfileDeferralInfo("sRGB.pf",
                                                ColorSpace.TYPE_RGB, 3,
                                                CLASS_DISPLAY);
                    sRGBprofile = getDeferredInstance(pInfo);
                }
                thisProfile = sRGBprofile;
            }
            break;
        case ColorSpace.CS_CIEXYZ:
            synchronized(ICC_Profile.class) {
                if (XYZprofile == null) {
                    ProfileDeferralInfo pInfo =
                        new ProfileDeferralInfo("CIEXYZ.pf",
                                                ColorSpace.TYPE_XYZ, 3,
                                                CLASS_DISPLAY);
                    XYZprofile = getDeferredInstance(pInfo);
                }
                thisProfile = XYZprofile;
            }
            break;
        case ColorSpace.CS_PYCC:
            synchronized(ICC_Profile.class) {
                if (PYCCprofile == null) {
                    if (standardProfileExists("PYCC.pf"))
                    {
                        ProfileDeferralInfo pInfo =
                            new ProfileDeferralInfo("PYCC.pf",
                                                    ColorSpace.TYPE_3CLR, 3,
                                                    CLASS_DISPLAY);
                        PYCCprofile = getDeferredInstance(pInfo);
                    } else {
                        throw new IllegalArgumentException(
                                "Can't load standard profile: PYCC.pf");
                    }
                }
                thisProfile = PYCCprofile;
            }
            break;
        case ColorSpace.CS_GRAY:
            synchronized(ICC_Profile.class) {
                if (GRAYprofile == null) {
                    ProfileDeferralInfo pInfo =
                        new ProfileDeferralInfo("GRAY.pf",
                                                ColorSpace.TYPE_GRAY, 1,
                                                CLASS_DISPLAY);
                    GRAYprofile = getDeferredInstance(pInfo);
                }
                thisProfile = GRAYprofile;
            }
            break;
        case ColorSpace.CS_LINEAR_RGB:
            synchronized(ICC_Profile.class) {
                if (LINEAR_RGBprofile == null) {
                    ProfileDeferralInfo pInfo =
                        new ProfileDeferralInfo("LINEAR_RGB.pf",
                                                ColorSpace.TYPE_RGB, 3,
                                                CLASS_DISPLAY);
                    LINEAR_RGBprofile = getDeferredInstance(pInfo);
                }
                thisProfile = LINEAR_RGBprofile;
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown color space");
        }
        return thisProfile;
    }
    private static ICC_Profile getStandardProfile(final String name) {
        return (ICC_Profile) AccessController.doPrivileged(
            new PrivilegedAction() {
                 public Object run() {
                     ICC_Profile p = null;
                     try {
                         p = getInstance (name);
                     } catch (IOException ex) {
                         throw new IllegalArgumentException(
                               "Can't load standard profile: " + name);
                     }
                     return p;
                 }
             });
    }
    public static ICC_Profile getInstance(String fileName) throws IOException {
        ICC_Profile thisProfile;
        FileInputStream fis = null;
        File f = getProfileFile(fileName);
        if (f != null) {
            fis = new FileInputStream(f);
        }
        if (fis == null) {
            throw new IOException("Cannot open file " + fileName);
        }
        thisProfile = getInstance(fis);
        fis.close();    
        return thisProfile;
    }
    public static ICC_Profile getInstance(InputStream s) throws IOException {
    byte profileData[];
        if (s instanceof ProfileDeferralInfo) {
            return getDeferredInstance((ProfileDeferralInfo) s);
        }
        if ((profileData = getProfileDataFromStream(s)) == null) {
            throw new IllegalArgumentException("Invalid ICC Profile Data");
        }
        return getInstance(profileData);
    }
    static byte[] getProfileDataFromStream(InputStream s) throws IOException {
    byte profileData[];
    int profileSize;
        byte header[] = new byte[128];
        int bytestoread = 128;
        int bytesread = 0;
        int n;
        while (bytestoread != 0) {
            if ((n = s.read(header, bytesread, bytestoread)) < 0) {
                return null;
            }
            bytesread += n;
            bytestoread -= n;
        }
        if (header[36] != 0x61 || header[37] != 0x63 ||
            header[38] != 0x73 || header[39] != 0x70) {
            return null;   
        }
        profileSize = ((header[0] & 0xff) << 24) |
                      ((header[1] & 0xff) << 16) |
                      ((header[2] & 0xff) <<  8) |
                       (header[3] & 0xff);
        profileData = new byte[profileSize];
        System.arraycopy(header, 0, profileData, 0, 128);
        bytestoread = profileSize - 128;
        bytesread = 128;
        while (bytestoread != 0) {
            if ((n = s.read(profileData, bytesread, bytestoread)) < 0) {
                return null;
            }
            bytesread += n;
            bytestoread -= n;
        }
        return profileData;
    }
    static ICC_Profile getDeferredInstance(ProfileDeferralInfo pdi) {
        if (!ProfileDeferralMgr.deferring) {
            return getStandardProfile(pdi.filename);
        }
        if (pdi.colorSpaceType == ColorSpace.TYPE_RGB) {
            return new ICC_ProfileRGB(pdi);
        } else if (pdi.colorSpaceType == ColorSpace.TYPE_GRAY) {
            return new ICC_ProfileGray(pdi);
        } else {
            return new ICC_Profile(pdi);
        }
    }
    void activateDeferredProfile() throws ProfileDataException {
        byte profileData[];
        FileInputStream fis;
        final String fileName = deferralInfo.filename;
        profileActivator = null;
        deferralInfo = null;
        PrivilegedAction<FileInputStream> pa = new PrivilegedAction<FileInputStream>() {
            public FileInputStream run() {
                File f = getStandardProfileFile(fileName);
                if (f != null) {
                    try {
                        return new FileInputStream(f);
                    } catch (FileNotFoundException e) {}
                }
                return null;
            }
        };
        if ((fis = AccessController.doPrivileged(pa)) == null) {
            throw new ProfileDataException("Cannot open file " + fileName);
        }
        try {
            profileData = getProfileDataFromStream(fis);
            fis.close();    
        }
        catch (IOException e) {
            ProfileDataException pde = new
                ProfileDataException("Invalid ICC Profile Data" + fileName);
            pde.initCause(e);
            throw pde;
        }
        if (profileData == null) {
            throw new ProfileDataException("Invalid ICC Profile Data" +
                fileName);
        }
        try {
            ID = CMSManager.getModule().loadProfile(profileData);
        } catch (CMMException c) {
            ProfileDataException pde = new
                ProfileDataException("Invalid ICC Profile Data" + fileName);
            pde.initCause(c);
            throw pde;
        }
    }
    public int getMajorVersion() {
    byte[] theHeader;
        theHeader = getData(icSigHead); 
        return (int) theHeader[8];
    }
    public int getMinorVersion() {
    byte[] theHeader;
        theHeader = getData(icSigHead); 
        return (int) theHeader[9];
    }
    public int getProfileClass() {
    byte[] theHeader;
    int theClassSig, theClass;
        if (deferralInfo != null) {
            return deferralInfo.profileClass; 
        }
        theHeader = getData(icSigHead);
        theClassSig = intFromBigEndian (theHeader, icHdrDeviceClass);
        switch (theClassSig) {
        case icSigInputClass:
            theClass = CLASS_INPUT;
            break;
        case icSigDisplayClass:
            theClass = CLASS_DISPLAY;
            break;
        case icSigOutputClass:
            theClass = CLASS_OUTPUT;
            break;
        case icSigLinkClass:
            theClass = CLASS_DEVICELINK;
            break;
        case icSigColorSpaceClass:
            theClass = CLASS_COLORSPACECONVERSION;
            break;
        case icSigAbstractClass:
            theClass = CLASS_ABSTRACT;
            break;
        case icSigNamedColorClass:
            theClass = CLASS_NAMEDCOLOR;
            break;
        default:
            throw new IllegalArgumentException("Unknown profile class");
        }
        return theClass;
    }
    public int getColorSpaceType() {
        if (deferralInfo != null) {
            return deferralInfo.colorSpaceType; 
        }
        return    getColorSpaceType(ID);
    }
    static int getColorSpaceType(long profileID) {
    byte[] theHeader;
    int theColorSpaceSig, theColorSpace;
        theHeader = getData(profileID, icSigHead);
        theColorSpaceSig = intFromBigEndian(theHeader, icHdrColorSpace);
        theColorSpace = iccCStoJCS (theColorSpaceSig);
        return theColorSpace;
    }
    public int getPCSType() {
        if (ProfileDeferralMgr.deferring) {
            ProfileDeferralMgr.activateProfiles();
        }
        return getPCSType(ID);
    }
    static int getPCSType(long profileID) {
    byte[] theHeader;
    int thePCSSig, thePCS;
        theHeader = getData(profileID, icSigHead);
        thePCSSig = intFromBigEndian(theHeader, icHdrPcs);
        thePCS = iccCStoJCS(thePCSSig);
        return thePCS;
    }
    public void write(String fileName) throws IOException {
    FileOutputStream outputFile;
    byte profileData[];
        profileData = getData(); 
        outputFile = new FileOutputStream(fileName);
        outputFile.write(profileData);
        outputFile.close ();
    }
    public void write(OutputStream s) throws IOException {
    byte profileData[];
        profileData = getData(); 
        s.write(profileData);
    }
    public byte[] getData() {
    int profileSize;
    byte[] profileData;
        if (ProfileDeferralMgr.deferring) {
            ProfileDeferralMgr.activateProfiles();
        }
        PCMM mdl = CMSManager.getModule();
        profileSize = mdl.getProfileSize(ID);
        profileData = new byte [profileSize];
        mdl.getProfileData(ID, profileData);
        return profileData;
    }
    public byte[] getData(int tagSignature) {
        if (ProfileDeferralMgr.deferring) {
            ProfileDeferralMgr.activateProfiles();
        }
        return getData(ID, tagSignature);
    }
    static byte[] getData(long profileID, int tagSignature) {
    int tagSize;
    byte[] tagData;
        try {
            PCMM mdl = CMSManager.getModule();
            tagSize = mdl.getTagSize(profileID, tagSignature);
            tagData = new byte[tagSize]; 
            mdl.getTagData(profileID, tagSignature, tagData);
        } catch(CMMException c) {
            tagData = null;
        }
        return tagData;
    }
    public void setData(int tagSignature, byte[] tagData) {
        if (ProfileDeferralMgr.deferring) {
            ProfileDeferralMgr.activateProfiles();
        }
        CMSManager.getModule().setTagData(ID, tagSignature, tagData);
    }
    void setRenderingIntent(int renderingIntent) {
        byte[] theHeader = getData(icSigHead);
        intToBigEndian (renderingIntent, theHeader, icHdrRenderingIntent);
        setData (icSigHead, theHeader);
    }
    int getRenderingIntent() {
        byte[] theHeader = getData(icSigHead);
        int renderingIntent = intFromBigEndian(theHeader, icHdrRenderingIntent);
        return renderingIntent;
    }
    public int getNumComponents() {
    byte[]    theHeader;
    int    theColorSpaceSig, theNumComponents;
        if (deferralInfo != null) {
            return deferralInfo.numComponents; 
        }
        theHeader = getData(icSigHead);
        theColorSpaceSig = intFromBigEndian (theHeader, icHdrColorSpace);
        switch (theColorSpaceSig) {
        case icSigGrayData:
            theNumComponents = 1;
            break;
        case icSigSpace2CLR:
            theNumComponents = 2;
            break;
        case icSigXYZData:
        case icSigLabData:
        case icSigLuvData:
        case icSigYCbCrData:
        case icSigYxyData:
        case icSigRgbData:
        case icSigHsvData:
        case icSigHlsData:
        case icSigCmyData:
        case icSigSpace3CLR:
            theNumComponents = 3;
            break;
        case icSigCmykData:
        case icSigSpace4CLR:
            theNumComponents = 4;
            break;
        case icSigSpace5CLR:
            theNumComponents = 5;
            break;
        case icSigSpace6CLR:
            theNumComponents = 6;
            break;
        case icSigSpace7CLR:
            theNumComponents = 7;
            break;
        case icSigSpace8CLR:
            theNumComponents = 8;
            break;
        case icSigSpace9CLR:
            theNumComponents = 9;
            break;
        case icSigSpaceACLR:
            theNumComponents = 10;
            break;
        case icSigSpaceBCLR:
            theNumComponents = 11;
            break;
        case icSigSpaceCCLR:
            theNumComponents = 12;
            break;
        case icSigSpaceDCLR:
            theNumComponents = 13;
            break;
        case icSigSpaceECLR:
            theNumComponents = 14;
            break;
        case icSigSpaceFCLR:
            theNumComponents = 15;
            break;
        default:
            throw new ProfileDataException ("invalid ICC color space");
        }
        return theNumComponents;
    }
    float[] getMediaWhitePoint() {
        return getXYZTag(icSigMediaWhitePointTag);
    }
    float[] getXYZTag(int theTagSignature) {
    byte[] theData;
    float[] theXYZNumber;
    int i1, i2, theS15Fixed16;
        theData = getData(theTagSignature); 
        theXYZNumber = new float [3];        
        for (i1 = 0, i2 = icXYZNumberX; i1 < 3; i1++, i2 += 4) {
            theS15Fixed16 = intFromBigEndian(theData, i2);
            theXYZNumber [i1] = ((float) theS15Fixed16) / 65536.0f;
        }
        return theXYZNumber;
    }
    float getGamma(int theTagSignature) {
    byte[] theTRCData;
    float theGamma;
    int theU8Fixed8;
        theTRCData = getData(theTagSignature); 
        if (intFromBigEndian (theTRCData, icCurveCount) != 1) {
            throw new ProfileDataException ("TRC is not a gamma");
        }
        theU8Fixed8 = (shortFromBigEndian(theTRCData, icCurveData)) & 0xffff;
        theGamma = ((float) theU8Fixed8) / 256.0f;
        return theGamma;
    }
    short[] getTRC(int theTagSignature) {
    byte[] theTRCData;
    short[] theTRC;
    int i1, i2, nElements, theU8Fixed8;
        theTRCData = getData(theTagSignature); 
        nElements = intFromBigEndian(theTRCData, icCurveCount);
        if (nElements == 1) {
            throw new ProfileDataException("TRC is not a table");
        }
        theTRC = new short [nElements];
        for (i1 = 0, i2 = icCurveData; i1 < nElements; i1++, i2 += 2) {
            theTRC[i1] = shortFromBigEndian(theTRCData, i2);
        }
        return theTRC;
    }
    static int iccCStoJCS(int theColorSpaceSig) {
    int theColorSpace;
        switch (theColorSpaceSig) {
        case icSigXYZData:
            theColorSpace = ColorSpace.TYPE_XYZ;
            break;
        case icSigLabData:
            theColorSpace = ColorSpace.TYPE_Lab;
            break;
        case icSigLuvData:
            theColorSpace = ColorSpace.TYPE_Luv;
            break;
        case icSigYCbCrData:
            theColorSpace = ColorSpace.TYPE_YCbCr;
            break;
        case icSigYxyData:
            theColorSpace = ColorSpace.TYPE_Yxy;
            break;
        case icSigRgbData:
            theColorSpace = ColorSpace.TYPE_RGB;
            break;
        case icSigGrayData:
            theColorSpace = ColorSpace.TYPE_GRAY;
            break;
        case icSigHsvData:
            theColorSpace = ColorSpace.TYPE_HSV;
            break;
        case icSigHlsData:
            theColorSpace = ColorSpace.TYPE_HLS;
            break;
        case icSigCmykData:
            theColorSpace = ColorSpace.TYPE_CMYK;
            break;
        case icSigCmyData:
            theColorSpace = ColorSpace.TYPE_CMY;
            break;
        case icSigSpace2CLR:
            theColorSpace = ColorSpace.TYPE_2CLR;
            break;
        case icSigSpace3CLR:
            theColorSpace = ColorSpace.TYPE_3CLR;
            break;
        case icSigSpace4CLR:
            theColorSpace = ColorSpace.TYPE_4CLR;
            break;
        case icSigSpace5CLR:
            theColorSpace = ColorSpace.TYPE_5CLR;
            break;
        case icSigSpace6CLR:
            theColorSpace = ColorSpace.TYPE_6CLR;
            break;
        case icSigSpace7CLR:
            theColorSpace = ColorSpace.TYPE_7CLR;
            break;
        case icSigSpace8CLR:
            theColorSpace = ColorSpace.TYPE_8CLR;
            break;
        case icSigSpace9CLR:
            theColorSpace = ColorSpace.TYPE_9CLR;
            break;
        case icSigSpaceACLR:
            theColorSpace = ColorSpace.TYPE_ACLR;
            break;
        case icSigSpaceBCLR:
            theColorSpace = ColorSpace.TYPE_BCLR;
            break;
        case icSigSpaceCCLR:
            theColorSpace = ColorSpace.TYPE_CCLR;
            break;
        case icSigSpaceDCLR:
            theColorSpace = ColorSpace.TYPE_DCLR;
            break;
        case icSigSpaceECLR:
            theColorSpace = ColorSpace.TYPE_ECLR;
            break;
        case icSigSpaceFCLR:
            theColorSpace = ColorSpace.TYPE_FCLR;
            break;
        default:
            throw new IllegalArgumentException ("Unknown color space");
        }
        return theColorSpace;
    }
    static int intFromBigEndian(byte[] array, int index) {
        return (((array[index]   & 0xff) << 24) |
                ((array[index+1] & 0xff) << 16) |
                ((array[index+2] & 0xff) <<  8) |
                 (array[index+3] & 0xff));
    }
    static void intToBigEndian(int value, byte[] array, int index) {
            array[index]   = (byte) (value >> 24);
            array[index+1] = (byte) (value >> 16);
            array[index+2] = (byte) (value >>  8);
            array[index+3] = (byte) (value);
    }
    static short shortFromBigEndian(byte[] array, int index) {
        return (short) (((array[index]   & 0xff) << 8) |
                         (array[index+1] & 0xff));
    }
    static void shortToBigEndian(short value, byte[] array, int index) {
            array[index]   = (byte) (value >> 8);
            array[index+1] = (byte) (value);
    }
    private static File getProfileFile(String fileName) {
        String path, dir, fullPath;
        File f = new File(fileName); 
        if (f.isAbsolute()) {
            return f.isFile() ? f : null;
        }
        if ((!f.isFile()) &&
                ((path = System.getProperty("java.iccprofile.path")) != null)){
                StringTokenizer st =
                    new StringTokenizer(path, File.pathSeparator);
                while (st.hasMoreTokens() && ((f == null) || (!f.isFile()))) {
                    dir = st.nextToken();
                        fullPath = dir + File.separatorChar + fileName;
                    f = new File(fullPath);
                    if (!isChildOf(f, dir)) {
                        f = null;
                    }
                }
            }
        if (((f == null) || (!f.isFile())) &&
                ((path = System.getProperty("java.class.path")) != null)) {
                StringTokenizer st =
                    new StringTokenizer(path, File.pathSeparator);
                while (st.hasMoreTokens() && ((f == null) || (!f.isFile()))) {
                    dir = st.nextToken();
                        fullPath = dir + File.separatorChar + fileName;
                    f = new File(fullPath);
                }
            }
        if ((f == null) || (!f.isFile())) {
            f = getStandardProfileFile(fileName);
        }
        if (f != null && f.isFile()) {
            return f;
        }
        return null;
    }
    private static File getStandardProfileFile(String fileName) {
        String dir = System.getProperty("java.home") +
            File.separatorChar + "lib" + File.separatorChar + "cmm";
        String fullPath = dir + File.separatorChar + fileName;
        File f = new File(fullPath);
        return (f.isFile() && isChildOf(f, dir)) ? f : null;
    }
    private static boolean isChildOf(File f, String dirName) {
        try {
            File dir = new File(dirName);
            String canonicalDirName = dir.getCanonicalPath();
            if (!canonicalDirName.endsWith(File.separator)) {
                canonicalDirName += File.separator;
            }
            String canonicalFileName = f.getCanonicalPath();
            return canonicalFileName.startsWith(canonicalDirName);
        } catch (IOException e) {
            return false;
        }
    }
    private static boolean standardProfileExists(final String fileName) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    return getStandardProfileFile(fileName) != null;
                }
            });
    }
    private int iccProfileSerializedDataVersion = 1;
    private void writeObject(ObjectOutputStream s)
      throws IOException
    {
        s.defaultWriteObject();
        String csName = null;
        if (this == sRGBprofile) {
            csName = "CS_sRGB";
        } else if (this == XYZprofile) {
            csName = "CS_CIEXYZ";
        } else if (this == PYCCprofile) {
            csName = "CS_PYCC";
        } else if (this == GRAYprofile) {
            csName = "CS_GRAY";
        } else if (this == LINEAR_RGBprofile) {
            csName = "CS_LINEAR_RGB";
        }
        byte[] data = null;
        if (csName == null) {
            data = getData();
        }
        s.writeObject(csName);
        s.writeObject(data);
    }
    private transient ICC_Profile resolvedDeserializedProfile;
    private void readObject(ObjectInputStream s)
      throws IOException, ClassNotFoundException
    {
        s.defaultReadObject();
        String csName = (String)s.readObject();
        byte[] data = (byte[])s.readObject();
        int cspace = 0;         
        boolean isKnownPredefinedCS = false;
        if (csName != null) {
            isKnownPredefinedCS = true;
            if (csName.equals("CS_sRGB")) {
                cspace = ColorSpace.CS_sRGB;
            } else if (csName.equals("CS_CIEXYZ")) {
                cspace = ColorSpace.CS_CIEXYZ;
            } else if (csName.equals("CS_PYCC")) {
                cspace = ColorSpace.CS_PYCC;
            } else if (csName.equals("CS_GRAY")) {
                cspace = ColorSpace.CS_GRAY;
            } else if (csName.equals("CS_LINEAR_RGB")) {
                cspace = ColorSpace.CS_LINEAR_RGB;
            } else {
                isKnownPredefinedCS = false;
            }
        }
        if (isKnownPredefinedCS) {
            resolvedDeserializedProfile = getInstance(cspace);
        } else {
            resolvedDeserializedProfile = getInstance(data);
        }
    }
    protected Object readResolve() throws ObjectStreamException {
        return resolvedDeserializedProfile;
    }
}
