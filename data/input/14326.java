class PackageWriter extends BandStructure {
    Package pkg;
    OutputStream finalOut;
    PackageWriter(Package pkg, OutputStream out) throws IOException {
        this.pkg = pkg;
        this.finalOut = out;
        initPackageMajver(pkg.package_majver);
    }
    void write() throws IOException {
        boolean ok = false;
        try {
            if (verbose > 0) {
                Utils.log.info("Setting up constant pool...");
            }
            setup();
            if (verbose > 0) {
                Utils.log.info("Packing...");
            }
            writeConstantPool();
            writeFiles();
            writeAttrDefs();
            writeInnerClasses();
            writeClassesAndByteCodes();
            writeAttrCounts();
            if (verbose > 1)  printCodeHist();
            if (verbose > 0) {
                Utils.log.info("Coding...");
            }
            all_bands.chooseBandCodings();
            writeFileHeader();
            writeAllBandsTo(finalOut);
            ok = true;
        } catch (Exception ee) {
            Utils.log.warning("Error on output: "+ee, ee);
            if (verbose > 0)  finalOut.close();
            if (ee instanceof IOException)  throw (IOException)ee;
            if (ee instanceof RuntimeException)  throw (RuntimeException)ee;
            throw new Error("error packing", ee);
        }
    }
    Set<Entry>                       requiredEntries;  
    Map<Attribute.Layout, int[]>     backCountTable;   
    int[][]     attrCounts;       
    void setup() {
        requiredEntries = new HashSet<>();
        setArchiveOptions();
        trimClassAttributes();
        collectAttributeLayouts();
        pkg.buildGlobalConstantPool(requiredEntries);
        setBandIndexes();
        makeNewAttributeBands();
        collectInnerClasses();
    }
    void setArchiveOptions() {
        int minModtime = pkg.default_modtime;
        int maxModtime = pkg.default_modtime;
        int minOptions = -1;
        int maxOptions = 0;
        archiveOptions |= pkg.default_options;
        for (File file : pkg.files) {
            int modtime = file.modtime;
            int options = file.options;
            if (minModtime == NO_MODTIME) {
                minModtime = maxModtime = modtime;
            } else {
                if (minModtime > modtime)  minModtime = modtime;
                if (maxModtime < modtime)  maxModtime = modtime;
            }
            minOptions &= options;
            maxOptions |= options;
        }
        if (pkg.default_modtime == NO_MODTIME) {
            pkg.default_modtime = minModtime;
        }
        if (minModtime != NO_MODTIME && minModtime != maxModtime) {
            archiveOptions |= AO_HAVE_FILE_MODTIME;
        }
        if (!testBit(archiveOptions,AO_DEFLATE_HINT) && minOptions != -1) {
            if (testBit(minOptions, FO_DEFLATE_HINT)) {
                archiveOptions |= AO_DEFLATE_HINT;
                minOptions -= FO_DEFLATE_HINT;
                maxOptions -= FO_DEFLATE_HINT;
            }
            pkg.default_options |= minOptions;
            if (minOptions != maxOptions
                || minOptions != pkg.default_options) {
                archiveOptions |= AO_HAVE_FILE_OPTIONS;
            }
        }
        Map<Integer, int[]> verCounts = new HashMap<>();
        int bestCount = 0;
        int bestVersion = -1;
        for (Class cls : pkg.classes) {
            int version = cls.getVersion();
            int[] var = verCounts.get(version);
            if (var == null) {
                var = new int[1];
                verCounts.put(version, var);
            }
            int count = (var[0] += 1);
            if (bestCount < count) {
                bestCount = count;
                bestVersion = version;
            }
        }
        verCounts.clear();
        if (bestVersion == -1)  bestVersion = 0;  
        int bestMajver = (char)(bestVersion >>> 16);
        int bestMinver = (char)(bestVersion);
        pkg.default_class_majver = (short) bestMajver;
        pkg.default_class_minver = (short) bestMinver;
        String bestVerStr = Package.versionStringOf(bestMajver, bestMinver);
        if (verbose > 0)
           Utils.log.info("Consensus version number in segment is "+bestVerStr);
        if (verbose > 0)
            Utils.log.info("Highest version number in segment is "+
                           Package.versionStringOf(pkg.getHighestClassVersion()));
        for (Class cls : pkg.classes) {
            if (cls.getVersion() != bestVersion) {
                Attribute a = makeClassFileVersionAttr(cls.minver, cls.majver);
                if (verbose > 1) {
                    String clsVer = cls.getVersionString();
                    String pkgVer = bestVerStr;
                    Utils.log.fine("Version "+clsVer+" of "+cls
                                     +" doesn't match package version "
                                     +pkgVer);
                }
                cls.addAttribute(a);
            }
        }
        for (File file : pkg.files) {
            long len = file.getFileLength();
            if (len != (int)len) {
                archiveOptions |= AO_HAVE_FILE_SIZE_HI;
                if (verbose > 0)
                   Utils.log.info("Note: Huge resource file "+file.getFileName()+" forces 64-bit sizing");
                break;
            }
        }
        int cost0 = 0;
        int cost1 = 0;
        for (Class cls : pkg.classes) {
            for (Class.Method m : cls.getMethods()) {
                if (m.code != null) {
                    if (m.code.attributeSize() == 0) {
                        cost1 += 1;
                    } else if (shortCodeHeader(m.code) != LONG_CODE_HEADER) {
                        cost0 += 3;
                    }
                }
            }
        }
        if (cost0 > cost1) {
            archiveOptions |= AO_HAVE_ALL_CODE_FLAGS;
        }
        if (verbose > 0)
            Utils.log.info("archiveOptions = "
                             +"0b"+Integer.toBinaryString(archiveOptions));
    }
    void writeFileHeader() throws IOException {
        pkg.checkVersion();
        writeArchiveMagic();
        writeArchiveHeader();
    }
    private void putMagicInt32(int val) throws IOException {
        int res = val;
        for (int i = 0; i < 4; i++) {
            archive_magic.putByte(0xFF & (res >>> 24));
            res <<= 8;
        }
    }
    void writeArchiveMagic() throws IOException {
        putMagicInt32(pkg.magic);
    }
    void writeArchiveHeader() throws IOException {
        int headerDiscountForDebug = 0;
        boolean haveSpecial = testBit(archiveOptions, AO_HAVE_SPECIAL_FORMATS);
        if (!haveSpecial) {
            haveSpecial |= (band_headers.length() != 0);
            haveSpecial |= (attrDefsWritten.length != 0);
            if (haveSpecial)
                archiveOptions |= AO_HAVE_SPECIAL_FORMATS;
        }
        if (!haveSpecial)
            headerDiscountForDebug += AH_SPECIAL_FORMAT_LEN;
        boolean haveFiles = testBit(archiveOptions, AO_HAVE_FILE_HEADERS);
        if (!haveFiles) {
            haveFiles |= (archiveNextCount > 0);
            haveFiles |= (pkg.default_modtime != NO_MODTIME);
            if (haveFiles)
                archiveOptions |= AO_HAVE_FILE_HEADERS;
        }
        if (!haveFiles)
            headerDiscountForDebug += AH_FILE_HEADER_LEN;
        boolean haveNumbers = testBit(archiveOptions, AO_HAVE_CP_NUMBERS);
        if (!haveNumbers) {
            haveNumbers |= pkg.cp.haveNumbers();
            if (haveNumbers)
                archiveOptions |= AO_HAVE_CP_NUMBERS;
        }
        if (!haveNumbers)
            headerDiscountForDebug += AH_CP_NUMBER_LEN;
        assert(pkg.package_majver > 0);  
        archive_header_0.putInt(pkg.package_minver);
        archive_header_0.putInt(pkg.package_majver);
        if (verbose > 0)
            Utils.log.info("Package Version for this segment:"+
                           Package.versionStringOf(pkg.getPackageVersion()));
        archive_header_0.putInt(archiveOptions); 
        assert(archive_header_0.length() == AH_LENGTH_0);
        final int DUMMY = 0;
        if (haveFiles) {
            assert(archive_header_S.length() == AH_ARCHIVE_SIZE_HI);
            archive_header_S.putInt(DUMMY); 
            assert(archive_header_S.length() == AH_ARCHIVE_SIZE_LO);
            archive_header_S.putInt(DUMMY); 
            assert(archive_header_S.length() == AH_LENGTH_S);
        }
        if (haveFiles) {
            archive_header_1.putInt(archiveNextCount);  
            archive_header_1.putInt(pkg.default_modtime);
            archive_header_1.putInt(pkg.files.size());
        } else {
            assert(pkg.files.isEmpty());
        }
        if (haveSpecial) {
            archive_header_1.putInt(band_headers.length());
            archive_header_1.putInt(attrDefsWritten.length);
        } else {
            assert(band_headers.length() == 0);
            assert(attrDefsWritten.length == 0);
        }
        writeConstantPoolCounts(haveNumbers);
        archive_header_1.putInt(pkg.getAllInnerClasses().size());
        archive_header_1.putInt(pkg.default_class_minver);
        archive_header_1.putInt(pkg.default_class_majver);
        archive_header_1.putInt(pkg.classes.size());
        assert(archive_header_0.length() +
               archive_header_S.length() +
               archive_header_1.length()
               == AH_LENGTH - headerDiscountForDebug);
        archiveSize0 = 0;
        archiveSize1 = all_bands.outputSize();
        archiveSize0 += archive_magic.outputSize();
        archiveSize0 += archive_header_0.outputSize();
        archiveSize0 += archive_header_S.outputSize();
        archiveSize1 -= archiveSize0;
        if (haveFiles) {
            int archiveSizeHi = (int)(archiveSize1 >>> 32);
            int archiveSizeLo = (int)(archiveSize1 >>> 0);
            archive_header_S.patchValue(AH_ARCHIVE_SIZE_HI, archiveSizeHi);
            archive_header_S.patchValue(AH_ARCHIVE_SIZE_LO, archiveSizeLo);
            int zeroLen = UNSIGNED5.getLength(DUMMY);
            archiveSize0 += UNSIGNED5.getLength(archiveSizeHi) - zeroLen;
            archiveSize0 += UNSIGNED5.getLength(archiveSizeLo) - zeroLen;
        }
        if (verbose > 1)
            Utils.log.fine("archive sizes: "+
                             archiveSize0+"+"+archiveSize1);
        assert(all_bands.outputSize() == archiveSize0+archiveSize1);
    }
    void writeConstantPoolCounts(boolean haveNumbers) throws IOException {
        for (int k = 0; k < ConstantPool.TAGS_IN_ORDER.length; k++) {
            byte tag = ConstantPool.TAGS_IN_ORDER[k];
            int count = pkg.cp.getIndexByTag(tag).size();
            switch (tag) {
            case CONSTANT_Utf8:
                if (count > 0)
                    assert(pkg.cp.getIndexByTag(tag).get(0)
                           == ConstantPool.getUtf8Entry(""));
                break;
            case CONSTANT_Integer:
            case CONSTANT_Float:
            case CONSTANT_Long:
            case CONSTANT_Double:
                if (!haveNumbers) {
                    assert(count == 0);
                    continue;
                }
                break;
            }
            archive_header_1.putInt(count);
        }
    }
    protected Index getCPIndex(byte tag) {
        return pkg.cp.getIndexByTag(tag);
    }
    void writeConstantPool() throws IOException {
        IndexGroup cp = pkg.cp;
        if (verbose > 0)  Utils.log.info("Writing CP");
        for (int k = 0; k < ConstantPool.TAGS_IN_ORDER.length; k++) {
            byte  tag   = ConstantPool.TAGS_IN_ORDER[k];
            Index index = cp.getIndexByTag(tag);
            Entry[] cpMap = index.cpMap;
            if (verbose > 0)
                Utils.log.info("Writing "+cpMap.length+" "+ConstantPool.tagName(tag)+" entries...");
            if (optDumpBands) {
                try (PrintStream ps = new PrintStream(getDumpStream(index, ".idx"))) {
                    printArrayTo(ps, cpMap, 0, cpMap.length);
                }
            }
            switch (tag) {
            case CONSTANT_Utf8:
                writeUtf8Bands(cpMap);
                break;
            case CONSTANT_Integer:
                for (int i = 0; i < cpMap.length; i++) {
                    NumberEntry e = (NumberEntry) cpMap[i];
                    int x = ((Integer)e.numberValue()).intValue();
                    cp_Int.putInt(x);
                }
                break;
            case CONSTANT_Float:
                for (int i = 0; i < cpMap.length; i++) {
                    NumberEntry e = (NumberEntry) cpMap[i];
                    float fx = ((Float)e.numberValue()).floatValue();
                    int x = Float.floatToIntBits(fx);
                    cp_Float.putInt(x);
                }
                break;
            case CONSTANT_Long:
                for (int i = 0; i < cpMap.length; i++) {
                    NumberEntry e = (NumberEntry) cpMap[i];
                    long x = ((Long)e.numberValue()).longValue();
                    cp_Long_hi.putInt((int)(x >>> 32));
                    cp_Long_lo.putInt((int)(x >>> 0));
                }
                break;
            case CONSTANT_Double:
                for (int i = 0; i < cpMap.length; i++) {
                    NumberEntry e = (NumberEntry) cpMap[i];
                    double dx = ((Double)e.numberValue()).doubleValue();
                    long x = Double.doubleToLongBits(dx);
                    cp_Double_hi.putInt((int)(x >>> 32));
                    cp_Double_lo.putInt((int)(x >>> 0));
                }
                break;
            case CONSTANT_String:
                for (int i = 0; i < cpMap.length; i++) {
                    StringEntry e = (StringEntry) cpMap[i];
                    cp_String.putRef(e.ref);
                }
                break;
            case CONSTANT_Class:
                for (int i = 0; i < cpMap.length; i++) {
                    ClassEntry e = (ClassEntry) cpMap[i];
                    cp_Class.putRef(e.ref);
                }
                break;
            case CONSTANT_Signature:
                writeSignatureBands(cpMap);
                break;
            case CONSTANT_NameandType:
                for (int i = 0; i < cpMap.length; i++) {
                    DescriptorEntry e = (DescriptorEntry) cpMap[i];
                    cp_Descr_name.putRef(e.nameRef);
                    cp_Descr_type.putRef(e.typeRef);
                }
                break;
            case CONSTANT_Fieldref:
                writeMemberRefs(tag, cpMap, cp_Field_class, cp_Field_desc);
                break;
            case CONSTANT_Methodref:
                writeMemberRefs(tag, cpMap, cp_Method_class, cp_Method_desc);
                break;
            case CONSTANT_InterfaceMethodref:
                writeMemberRefs(tag, cpMap, cp_Imethod_class, cp_Imethod_desc);
                break;
            default:
                assert(false);
            }
        }
    }
    void writeUtf8Bands(Entry[] cpMap) throws IOException {
        if (cpMap.length == 0)
            return;  
        assert(cpMap[0].stringValue().equals(""));
        final int SUFFIX_SKIP_1 = 1;
        final int PREFIX_SKIP_2 = 2;
        char[][] chars = new char[cpMap.length][];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = cpMap[i].stringValue().toCharArray();
        }
        int[] prefixes = new int[cpMap.length];  
        char[] prevChars = {};
        for (int i = 0; i < chars.length; i++) {
            int prefix = 0;
            char[] curChars = chars[i];
            int limit = Math.min(curChars.length, prevChars.length);
            while (prefix < limit && curChars[prefix] == prevChars[prefix])
                prefix++;
            prefixes[i] = prefix;
            if (i >= PREFIX_SKIP_2)
                cp_Utf8_prefix.putInt(prefix);
            else
                assert(prefix == 0);
            prevChars = curChars;
        }
        for (int i = 0; i < chars.length; i++) {
            char[] str = chars[i];
            int prefix = prefixes[i];
            int suffix = str.length - prefixes[i];
            boolean isPacked = false;
            if (suffix == 0) {
                isPacked = (i >= SUFFIX_SKIP_1);
            } else if (optBigStrings && effort > 1 && suffix > 100) {
                int numWide = 0;
                for (int n = 0; n < suffix; n++) {
                    if (str[prefix+n] > 127) {
                        numWide++;
                    }
                }
                if (numWide > 100) {
                    isPacked = tryAlternateEncoding(i, numWide, str, prefix);
                }
            }
            if (i < SUFFIX_SKIP_1) {
                assert(!isPacked);
                assert(suffix == 0);
            } else if (isPacked) {
                cp_Utf8_suffix.putInt(0);
                cp_Utf8_big_suffix.putInt(suffix);
            } else {
                assert(suffix != 0);  
                cp_Utf8_suffix.putInt(suffix);
                for (int n = 0; n < suffix; n++) {
                    int ch = str[prefix+n];
                    cp_Utf8_chars.putInt(ch);
                }
            }
        }
        if (verbose > 0) {
            int normCharCount = cp_Utf8_chars.length();
            int packCharCount = cp_Utf8_big_chars.length();
            int charCount = normCharCount + packCharCount;
            Utils.log.info("Utf8string #CHARS="+charCount+" #PACKEDCHARS="+packCharCount);
        }
    }
    private boolean tryAlternateEncoding(int i, int numWide,
                                         char[] str, int prefix) {
        int suffix = str.length - prefix;
        int[] cvals = new int[suffix];
        for (int n = 0; n < suffix; n++) {
            cvals[n] = str[prefix+n];
        }
        CodingChooser cc = getCodingChooser();
        Coding bigRegular = cp_Utf8_big_chars.regularCoding;
        String bandName = "(Utf8_big_"+i+")";
        int[] sizes = { 0, 0 };
        final int BYTE_SIZE = CodingChooser.BYTE_SIZE;
        final int ZIP_SIZE = CodingChooser.ZIP_SIZE;
        if (verbose > 1 || cc.verbose > 1) {
            Utils.log.fine("--- chooseCoding "+bandName);
        }
        CodingMethod special = cc.choose(cvals, bigRegular, sizes);
        Coding charRegular = cp_Utf8_chars.regularCoding;
        if (verbose > 1)
            Utils.log.fine("big string["+i+"] len="+suffix+" #wide="+numWide+" size="+sizes[BYTE_SIZE]+"/z="+sizes[ZIP_SIZE]+" coding "+special);
        if (special != charRegular) {
            int specialZipSize = sizes[ZIP_SIZE];
            int[] normalSizes = cc.computeSize(charRegular, cvals);
            int normalZipSize = normalSizes[ZIP_SIZE];
            int minWin = Math.max(5, normalZipSize/1000);
            if (verbose > 1)
                Utils.log.fine("big string["+i+"] normalSize="+normalSizes[BYTE_SIZE]+"/z="+normalSizes[ZIP_SIZE]+" win="+(specialZipSize<normalZipSize-minWin));
            if (specialZipSize < normalZipSize-minWin) {
                IntBand big = cp_Utf8_big_chars.newIntBand(bandName);
                big.initializeValues(cvals);
                return true;
            }
        }
        return false;
    }
    void writeSignatureBands(Entry[] cpMap) throws IOException {
        for (int i = 0; i < cpMap.length; i++) {
            SignatureEntry e = (SignatureEntry) cpMap[i];
            cp_Signature_form.putRef(e.formRef);
            for (int j = 0; j < e.classRefs.length; j++) {
                cp_Signature_classes.putRef(e.classRefs[j]);
            }
        }
    }
    void writeMemberRefs(byte tag, Entry[] cpMap, CPRefBand cp_class, CPRefBand cp_desc) throws IOException {
        for (int i = 0; i < cpMap.length; i++) {
            MemberEntry e = (MemberEntry) cpMap[i];
            cp_class.putRef(e.classRef);
            cp_desc.putRef(e.descRef);
        }
    }
    void writeFiles() throws IOException {
        int numFiles = pkg.files.size();
        if (numFiles == 0)  return;
        int options = archiveOptions;
        boolean haveSizeHi  = testBit(options, AO_HAVE_FILE_SIZE_HI);
        boolean haveModtime = testBit(options, AO_HAVE_FILE_MODTIME);
        boolean haveOptions = testBit(options, AO_HAVE_FILE_OPTIONS);
        if (!haveOptions) {
            for (File file : pkg.files) {
                if (file.isClassStub()) {
                    haveOptions = true;
                    options |= AO_HAVE_FILE_OPTIONS;
                    archiveOptions = options;
                    break;
                }
            }
        }
        if (haveSizeHi || haveModtime || haveOptions || !pkg.files.isEmpty()) {
            options |= AO_HAVE_FILE_HEADERS;
            archiveOptions = options;
        }
        for (File file : pkg.files) {
            file_name.putRef(file.name);
            long len = file.getFileLength();
            file_size_lo.putInt((int)len);
            if (haveSizeHi)
                file_size_hi.putInt((int)(len >>> 32));
            if (haveModtime)
                file_modtime.putInt(file.modtime - pkg.default_modtime);
            if (haveOptions)
                file_options.putInt(file.options);
            file.writeTo(file_bits.collectorStream());
            if (verbose > 1)
                Utils.log.fine("Wrote "+len+" bytes of "+file.name.stringValue());
        }
        if (verbose > 0)
            Utils.log.info("Wrote "+numFiles+" resource files");
    }
    @SuppressWarnings("unchecked")
    void collectAttributeLayouts() {
        maxFlags = new int[ATTR_CONTEXT_LIMIT];
        allLayouts = new FixedList<>(ATTR_CONTEXT_LIMIT);
        for (int i = 0; i < ATTR_CONTEXT_LIMIT; i++) {
            allLayouts.set(i, new HashMap<Attribute.Layout, int[]>());
        }
        for (Class cls : pkg.classes) {
            visitAttributeLayoutsIn(ATTR_CONTEXT_CLASS, cls);
            for (Class.Field f : cls.getFields()) {
                visitAttributeLayoutsIn(ATTR_CONTEXT_FIELD, f);
            }
            for (Class.Method m : cls.getMethods()) {
                visitAttributeLayoutsIn(ATTR_CONTEXT_METHOD, m);
                if (m.code != null) {
                    visitAttributeLayoutsIn(ATTR_CONTEXT_CODE, m.code);
                }
            }
        }
        for (int i = 0; i < ATTR_CONTEXT_LIMIT; i++) {
            int nl = allLayouts.get(i).size();
            boolean haveLongFlags = haveFlagsHi(i);
            final int TOO_MANY_ATTRS = 32 
                - 12 
                + 4  ;
            if (nl >= TOO_MANY_ATTRS) {  
                int mask = 1<<(LG_AO_HAVE_XXX_FLAGS_HI+i);
                archiveOptions |= mask;
                haveLongFlags = true;
                if (verbose > 0)
                   Utils.log.info("Note: Many "+Attribute.contextName(i)+" attributes forces 63-bit flags");
            }
            if (verbose > 1) {
                Utils.log.fine(Attribute.contextName(i)+".maxFlags = 0x"+Integer.toHexString(maxFlags[i]));
                Utils.log.fine(Attribute.contextName(i)+".#layouts = "+nl);
            }
            assert(haveFlagsHi(i) == haveLongFlags);
        }
        initAttrIndexLimit();
        for (int i = 0; i < ATTR_CONTEXT_LIMIT; i++) {
            assert((attrFlagMask[i] & maxFlags[i]) == 0);
        }
        backCountTable = new HashMap<>();
        attrCounts = new int[ATTR_CONTEXT_LIMIT][];
        for (int i = 0; i < ATTR_CONTEXT_LIMIT; i++) {
            long avHiBits = ~(maxFlags[i] | attrFlagMask[i]);
            assert(attrIndexLimit[i] > 0);
            assert(attrIndexLimit[i] < 64);  
            avHiBits &= (1L<<attrIndexLimit[i])-1;
            int nextLoBit = 0;
            Map<Attribute.Layout, int[]> defMap = allLayouts.get(i);
            Map.Entry[] layoutsAndCounts = new Map.Entry[defMap.size()];
            defMap.entrySet().toArray(layoutsAndCounts);
            Arrays.sort(layoutsAndCounts, new Comparator<Object>() {
                public int compare(Object o0, Object o1) {
                    Map.Entry e0 = (Map.Entry) o0;
                    Map.Entry e1 = (Map.Entry) o1;
                    int r = - ( ((int[])e0.getValue())[0]
                              - ((int[])e1.getValue())[0] );
                    if (r != 0)  return r;
                    return ((Comparable)e0.getKey()).compareTo(e1.getKey());
                }
            });
            attrCounts[i] = new int[attrIndexLimit[i]+layoutsAndCounts.length];
            for (int j = 0; j < layoutsAndCounts.length; j++) {
                Map.Entry e = layoutsAndCounts[j];
                Attribute.Layout def = (Attribute.Layout) e.getKey();
                int count = ((int[])e.getValue())[0];
                int index;
                Integer predefIndex = attrIndexTable.get(def);
                if (predefIndex != null) {
                    index = predefIndex.intValue();
                } else if (avHiBits != 0) {
                    while ((avHiBits & 1) == 0) {
                        avHiBits >>>= 1;
                        nextLoBit += 1;
                    }
                    avHiBits -= 1;  
                    index = setAttributeLayoutIndex(def, nextLoBit);
                } else {
                    index = setAttributeLayoutIndex(def, ATTR_INDEX_OVERFLOW);
                }
                attrCounts[i][index] = count;
                Attribute.Layout.Element[] cbles = def.getCallables();
                final int[] bc = new int[cbles.length];
                for (int k = 0; k < cbles.length; k++) {
                    assert(cbles[k].kind == Attribute.EK_CBLE);
                    if (!cbles[k].flagTest(Attribute.EF_BACK)) {
                        bc[k] = -1;  
                    }
                }
                backCountTable.put(def, bc);
                if (predefIndex == null) {
                    Entry ne = ConstantPool.getUtf8Entry(def.name());
                    String layout = def.layoutForPackageMajver(getPackageMajver());
                    Entry le = ConstantPool.getUtf8Entry(layout);
                    requiredEntries.add(ne);
                    requiredEntries.add(le);
                    if (verbose > 0) {
                        if (index < attrIndexLimit[i])
                           Utils.log.info("Using free flag bit 1<<"+index+" for "+count+" occurrences of "+def);
                        else
                            Utils.log.info("Using overflow index "+index+" for "+count+" occurrences of "+def);
                    }
                }
            }
        }
        maxFlags = null;
        allLayouts = null;
    }
    int[] maxFlags;
    List<Map<Attribute.Layout, int[]>> allLayouts;
    void visitAttributeLayoutsIn(int ctype, Attribute.Holder h) {
        maxFlags[ctype] |= h.flags;
        for (Attribute a : h.getAttributes()) {
            Attribute.Layout def = a.layout();
            Map<Attribute.Layout, int[]> defMap = allLayouts.get(ctype);
            int[] count = defMap.get(def);
            if (count == null) {
                defMap.put(def, count = new int[1]);
            }
            if (count[0] < Integer.MAX_VALUE) {
                count[0] += 1;
            }
        }
    }
    Attribute.Layout[] attrDefsWritten;
    @SuppressWarnings("unchecked")
    void writeAttrDefs() throws IOException {
        List<Object[]> defList = new ArrayList<>();
        for (int i = 0; i < ATTR_CONTEXT_LIMIT; i++) {
            int limit = attrDefs.get(i).size();
            for (int j = 0; j < limit; j++) {
                int header = i;  
                if (j < attrIndexLimit[i]) {
                    header |= ((j + ADH_BIT_IS_LSB) << ADH_BIT_SHIFT);
                    assert(header < 0x100);  
                    if (!testBit(attrDefSeen[i], 1L<<j)) {
                        continue;
                    }
                }
                Attribute.Layout def = attrDefs.get(i).get(j);
                defList.add(new Object[]{ Integer.valueOf(header), def });
                assert(Integer.valueOf(j).equals(attrIndexTable.get(def)));
            }
        }
        int numAttrDefs = defList.size();
        Object[][] defs = new Object[numAttrDefs][];
        defList.toArray(defs);
        Arrays.sort(defs, new Comparator() {
            public int compare(Object o0, Object o1) {
                Object[] a0 = (Object[]) o0;
                Object[] a1 = (Object[]) o1;
                int r = ((Comparable)a0[0]).compareTo(a1[0]);
                if (r != 0)  return r;
                Object ind0 = attrIndexTable.get(a0[1]);
                Object ind1 = attrIndexTable.get(a1[1]);
                assert(ind0 != null);
                assert(ind1 != null);
                return ((Comparable)ind0).compareTo(ind1);
            }
        });
        attrDefsWritten = new Attribute.Layout[numAttrDefs];
        try (PrintStream dump = !optDumpBands ? null
                 : new PrintStream(getDumpStream(attr_definition_headers, ".def")))
        {
            int[] indexForDebug = Arrays.copyOf(attrIndexLimit, ATTR_CONTEXT_LIMIT);
            for (int i = 0; i < defs.length; i++) {
                int header = ((Integer)defs[i][0]).intValue();
                Attribute.Layout def = (Attribute.Layout) defs[i][1];
                attrDefsWritten[i] = def;
                assert((header & ADH_CONTEXT_MASK) == def.ctype());
                attr_definition_headers.putByte(header);
                attr_definition_name.putRef(ConstantPool.getUtf8Entry(def.name()));
                String layout = def.layoutForPackageMajver(getPackageMajver());
                attr_definition_layout.putRef(ConstantPool.getUtf8Entry(layout));
                boolean debug = false;
                assert(debug = true);
                if (debug) {
                    int hdrIndex = (header >> ADH_BIT_SHIFT) - ADH_BIT_IS_LSB;
                    if (hdrIndex < 0)  hdrIndex = indexForDebug[def.ctype()]++;
                    int realIndex = (attrIndexTable.get(def)).intValue();
                    assert(hdrIndex == realIndex);
                }
                if (dump != null) {
                    int index = (header >> ADH_BIT_SHIFT) - ADH_BIT_IS_LSB;
                    dump.println(index+" "+def);
                }
            }
        }
    }
    void writeAttrCounts() throws IOException {
        for (int ctype = 0; ctype < ATTR_CONTEXT_LIMIT; ctype++) {
            MultiBand xxx_attr_bands = attrBands[ctype];
            IntBand xxx_attr_calls = getAttrBand(xxx_attr_bands, AB_ATTR_CALLS);
            Attribute.Layout[] defs = new Attribute.Layout[attrDefs.get(ctype).size()];
            attrDefs.get(ctype).toArray(defs);
            for (boolean predef = true; ; predef = false) {
                for (int ai = 0; ai < defs.length; ai++) {
                    Attribute.Layout def = defs[ai];
                    if (def == null)  continue;  
                    if (predef != isPredefinedAttr(ctype, ai))
                        continue;  
                    int totalCount = attrCounts[ctype][ai];
                    if (totalCount == 0)
                        continue;  
                    int[] bc = backCountTable.get(def);
                    for (int j = 0; j < bc.length; j++) {
                        if (bc[j] >= 0) {
                            int backCount = bc[j];
                            bc[j] = -1;  
                            xxx_attr_calls.putInt(backCount);
                            assert(def.getCallables()[j].flagTest(Attribute.EF_BACK));
                        } else {
                            assert(!def.getCallables()[j].flagTest(Attribute.EF_BACK));
                        }
                    }
                }
                if (!predef)  break;
            }
        }
    }
    void trimClassAttributes() {
        for (Class cls : pkg.classes) {
            cls.minimizeSourceFile();
        }
    }
    void collectInnerClasses() {
        Map<ClassEntry, InnerClass> allICMap = new HashMap<>();
        for (Class cls : pkg.classes) {
            if (!cls.hasInnerClasses())  continue;
            for (InnerClass ic : cls.getInnerClasses()) {
                InnerClass pic = allICMap.put(ic.thisClass, ic);
                if (pic != null && !pic.equals(ic) && pic.predictable) {
                    allICMap.put(pic.thisClass, pic);
                }
            }
        }
        InnerClass[] allICs = new InnerClass[allICMap.size()];
        allICMap.values().toArray(allICs);
        allICMap = null;  
        Arrays.sort(allICs);  
        pkg.setAllInnerClasses(Arrays.asList(allICs));
        for (Class cls : pkg.classes) {
            cls.minimizeLocalICs();
        }
    }
    void writeInnerClasses() throws IOException {
        for (InnerClass ic : pkg.getAllInnerClasses()) {
            int flags = ic.flags;
            assert((flags & ACC_IC_LONG_FORM) == 0);
            if (!ic.predictable) {
                flags |= ACC_IC_LONG_FORM;
            }
            ic_this_class.putRef(ic.thisClass);
            ic_flags.putInt(flags);
            if (!ic.predictable) {
                ic_outer_class.putRef(ic.outerClass);
                ic_name.putRef(ic.name);
            }
        }
    }
    void writeLocalInnerClasses(Class cls) throws IOException {
        List<InnerClass> localICs = cls.getInnerClasses();
        class_InnerClasses_N.putInt(localICs.size());
        for(InnerClass ic : localICs) {
            class_InnerClasses_RC.putRef(ic.thisClass);
            if (ic.equals(pkg.getGlobalInnerClass(ic.thisClass))) {
                class_InnerClasses_F.putInt(0);
            } else {
                int flags = ic.flags;
                if (flags == 0)
                    flags = ACC_IC_LONG_FORM;  
                class_InnerClasses_F.putInt(flags);
                class_InnerClasses_outer_RCN.putRef(ic.outerClass);
                class_InnerClasses_name_RUN.putRef(ic.name);
            }
        }
    }
    void writeClassesAndByteCodes() throws IOException {
        Class[] classes = new Class[pkg.classes.size()];
        pkg.classes.toArray(classes);
        if (verbose > 0)
            Utils.log.info("  ...scanning "+classes.length+" classes...");
        int nwritten = 0;
        for (int i = 0; i < classes.length; i++) {
            Class cls = classes[i];
            if (verbose > 1)
                Utils.log.fine("Scanning "+cls);
            ClassEntry   thisClass  = cls.thisClass;
            ClassEntry   superClass = cls.superClass;
            ClassEntry[] interfaces = cls.interfaces;
            assert(superClass != thisClass);  
            if (superClass == null)  superClass = thisClass;
            class_this.putRef(thisClass);
            class_super.putRef(superClass);
            class_interface_count.putInt(cls.interfaces.length);
            for (int j = 0; j < interfaces.length; j++) {
                class_interface.putRef(interfaces[j]);
            }
            writeMembers(cls);
            writeAttrs(ATTR_CONTEXT_CLASS, cls, cls);
            nwritten++;
            if (verbose > 0 && (nwritten % 1000) == 0)
                Utils.log.info("Have scanned "+nwritten+" classes...");
        }
    }
    void writeMembers(Class cls) throws IOException {
        List<Class.Field> fields = cls.getFields();
        class_field_count.putInt(fields.size());
        for (Class.Field f : fields) {
            field_descr.putRef(f.getDescriptor());
            writeAttrs(ATTR_CONTEXT_FIELD, f, cls);
        }
        List<Class.Method> methods = cls.getMethods();
        class_method_count.putInt(methods.size());
        for (Class.Method m : methods) {
            method_descr.putRef(m.getDescriptor());
            writeAttrs(ATTR_CONTEXT_METHOD, m, cls);
            assert((m.code != null) == (m.getAttribute(attrCodeEmpty) != null));
            if (m.code != null) {
                writeCodeHeader(m.code);
                writeByteCodes(m.code);
            }
        }
    }
    void writeCodeHeader(Code c) throws IOException {
        boolean attrsOK = testBit(archiveOptions, AO_HAVE_ALL_CODE_FLAGS);
        int na = c.attributeSize();
        int sc = shortCodeHeader(c);
        if (!attrsOK && na > 0)
            sc = LONG_CODE_HEADER;
        if (verbose > 2) {
            int siglen = c.getMethod().getArgumentSize();
            Utils.log.fine("Code sizes info "+c.max_stack+" "+c.max_locals+" "+c.getHandlerCount()+" "+siglen+" "+na+(sc > 0 ? " SHORT="+sc : ""));
        }
        code_headers.putByte(sc);
        if (sc == LONG_CODE_HEADER) {
            code_max_stack.putInt(c.getMaxStack());
            code_max_na_locals.putInt(c.getMaxNALocals());
            code_handler_count.putInt(c.getHandlerCount());
        } else {
            assert(attrsOK || na == 0);
            assert(c.getHandlerCount() < shortCodeHeader_h_limit);
        }
        writeCodeHandlers(c);
        if (sc == LONG_CODE_HEADER || attrsOK)
            writeAttrs(ATTR_CONTEXT_CODE, c, c.thisClass());
    }
    void writeCodeHandlers(Code c) throws IOException {
        int sum, del;
        for (int j = 0, jmax = c.getHandlerCount(); j < jmax; j++) {
            code_handler_class_RCN.putRef(c.handler_class[j]); 
            sum = c.encodeBCI(c.handler_start[j]);
            code_handler_start_P.putInt(sum);
            del = c.encodeBCI(c.handler_end[j]) - sum;
            code_handler_end_PO.putInt(del);
            sum += del;
            del = c.encodeBCI(c.handler_catch[j]) - sum;
            code_handler_catch_PO.putInt(del);
        }
    }
    void writeAttrs(int ctype,
                    final Attribute.Holder h,
                    Class cls) throws IOException {
        MultiBand xxx_attr_bands = attrBands[ctype];
        IntBand xxx_flags_hi = getAttrBand(xxx_attr_bands, AB_FLAGS_HI);
        IntBand xxx_flags_lo = getAttrBand(xxx_attr_bands, AB_FLAGS_LO);
        boolean haveLongFlags = haveFlagsHi(ctype);
        assert(attrIndexLimit[ctype] == (haveLongFlags? 63: 32));
        if (h.attributes == null) {
            xxx_flags_lo.putInt(h.flags);  
            if (haveLongFlags)
                xxx_flags_hi.putInt(0);
            return;
        }
        if (verbose > 3)
            Utils.log.fine("Transmitting attrs for "+h+" flags="+Integer.toHexString(h.flags));
        long flagMask = attrFlagMask[ctype];  
        long flagsToAdd = 0;
        int overflowCount = 0;
        for (Attribute a : h.attributes) {
            Attribute.Layout def = a.layout();
            int index = (attrIndexTable.get(def)).intValue();
            assert(attrDefs.get(ctype).get(index) == def);
            if (verbose > 3)
                Utils.log.fine("add attr @"+index+" "+a+" in "+h);
            if (index < attrIndexLimit[ctype] && testBit(flagMask, 1L<<index)) {
                if (verbose > 3)
                    Utils.log.fine("Adding flag bit 1<<"+index+" in "+Long.toHexString(flagMask));
                assert(!testBit(h.flags, 1L<<index));
                flagsToAdd |= (1L<<index);
                flagMask -= (1L<<index);  
            } else {
                flagsToAdd |= (1L<<X_ATTR_OVERFLOW);
                overflowCount += 1;
                if (verbose > 3)
                    Utils.log.fine("Adding overflow attr #"+overflowCount);
                IntBand xxx_attr_indexes = getAttrBand(xxx_attr_bands, AB_ATTR_INDEXES);
                xxx_attr_indexes.putInt(index);
            }
            if (def.bandCount == 0) {
                if (def == attrInnerClassesEmpty) {
                    writeLocalInnerClasses((Class) h);
                    continue;
                }
                continue;
            }
            assert(a.fixups == null);
            final Band[] ab = attrBandTable.get(def);
            assert(ab != null);
            assert(ab.length == def.bandCount);
            final int[] bc = backCountTable.get(def);
            assert(bc != null);
            assert(bc.length == def.getCallables().length);
            if (verbose > 2)  Utils.log.fine("writing "+a+" in "+h);
            boolean isCV = (ctype == ATTR_CONTEXT_FIELD && def == attrConstantValue);
            if (isCV)  setConstantValueIndex((Class.Field)h);
            a.parse(cls, a.bytes(), 0, a.size(),
                      new Attribute.ValueStream() {
                public void putInt(int bandIndex, int value) {
                    ((IntBand) ab[bandIndex]).putInt(value);
                }
                public void putRef(int bandIndex, Entry ref) {
                    ((CPRefBand) ab[bandIndex]).putRef(ref);
                }
                public int encodeBCI(int bci) {
                    Code code = (Code) h;
                    return code.encodeBCI(bci);
                }
                public void noteBackCall(int whichCallable) {
                    assert(bc[whichCallable] >= 0);
                    bc[whichCallable] += 1;
                }
            });
            if (isCV)  setConstantValueIndex(null);  
        }
        if (overflowCount > 0) {
            IntBand xxx_attr_count = getAttrBand(xxx_attr_bands, AB_ATTR_COUNT);
            xxx_attr_count.putInt(overflowCount);
        }
        xxx_flags_lo.putInt(h.flags | (int)flagsToAdd);
        if (haveLongFlags)
            xxx_flags_hi.putInt((int)(flagsToAdd >>> 32));
        else
            assert((flagsToAdd >>> 32) == 0);
        assert((h.flags & flagsToAdd) == 0)
            : (h+".flags="
                +Integer.toHexString(h.flags)+"^"
                +Long.toHexString(flagsToAdd));
    }
    private Code                 curCode;
    private Class                curClass;
    private Entry[] curCPMap;
    private void beginCode(Code c) {
        assert(curCode == null);
        curCode = c;
        curClass = c.m.thisClass();
        curCPMap = c.getCPMap();
    }
    private void endCode() {
        curCode = null;
        curClass = null;
        curCPMap = null;
    }
    private int initOpVariant(Instruction i, Entry newClass) {
        if (i.getBC() != _invokespecial)  return -1;
        MemberEntry ref = (MemberEntry) i.getCPRef(curCPMap);
        if ("<init>".equals(ref.descRef.nameRef.stringValue()) == false)
            return -1;
        ClassEntry refClass = ref.classRef;
        if (refClass == curClass.thisClass)
            return _invokeinit_op+_invokeinit_self_option;
        if (refClass == curClass.superClass)
            return _invokeinit_op+_invokeinit_super_option;
        if (refClass == newClass)
            return _invokeinit_op+_invokeinit_new_option;
        return -1;
    }
    private int selfOpVariant(Instruction i) {
        int bc = i.getBC();
        if (!(bc >= _first_linker_op && bc <= _last_linker_op))  return -1;
        MemberEntry ref = (MemberEntry) i.getCPRef(curCPMap);
        ClassEntry refClass = ref.classRef;
        int self_bc = _self_linker_op + (bc - _first_linker_op);
        if (refClass == curClass.thisClass)
            return self_bc;
        if (refClass == curClass.superClass)
            return self_bc + _self_linker_super_flag;
        return -1;
    }
    void writeByteCodes(Code code) throws IOException {
        beginCode(code);
        IndexGroup cp = pkg.cp;
        boolean prevAload = false;
        Entry newClass = null;
        for (Instruction i = code.instructionAt(0); i != null; i = i.next()) {
            if (verbose > 3)  Utils.log.fine(i.toString());
            if (i.isNonstandard()
                && (!p200.getBoolean(Utils.COM_PREFIX+"invokedynamic")
                    || i.getBC() != _xxxunusedxxx)) {
                String complaint = code.getMethod()
                    +" contains an unrecognized bytecode "+i
                    +"; please use the pass-file option on this class.";
                Utils.log.warning(complaint);
                throw new IOException(complaint);
            }
            if (i.isWide()) {
                if (verbose > 1) {
                    Utils.log.fine("_wide opcode in "+code);
                    Utils.log.fine(i.toString());
                }
                bc_codes.putByte(_wide);
                codeHist[_wide]++;
            }
            int bc = i.getBC();
            if (bc == _aload_0) {
                Instruction ni = code.instructionAt(i.getNextPC());
                if (selfOpVariant(ni) >= 0) {
                    prevAload = true;
                    continue;
                }
            }
            int init_bc = initOpVariant(i, newClass);
            if (init_bc >= 0) {
                if (prevAload) {
                    bc_codes.putByte(_aload_0);
                    codeHist[_aload_0]++;
                    prevAload = false;  
                }
                bc_codes.putByte(init_bc);
                codeHist[init_bc]++;
                MemberEntry ref = (MemberEntry) i.getCPRef(curCPMap);
                int coding = cp.getOverloadingIndex(ref);
                bc_initref.putInt(coding);
                continue;
            }
            int self_bc = selfOpVariant(i);
            if (self_bc >= 0) {
                boolean isField = Instruction.isFieldOp(bc);
                boolean isSuper = (self_bc >= _self_linker_op+_self_linker_super_flag);
                boolean isAload = prevAload;
                prevAload = false;  
                if (isAload)
                    self_bc += _self_linker_aload_flag;
                bc_codes.putByte(self_bc);
                codeHist[self_bc]++;
                MemberEntry ref = (MemberEntry) i.getCPRef(curCPMap);
                CPRefBand bc_which = selfOpRefBand(self_bc);
                Index which_ix = cp.getMemberIndex(ref.tag, ref.classRef);
                bc_which.putRef(ref, which_ix);
                continue;
            }
            assert(!prevAload);
            codeHist[bc]++;
            switch (bc) {
            case _tableswitch: 
            case _lookupswitch: 
                bc_codes.putByte(bc);
                Instruction.Switch isw = (Instruction.Switch) i;
                int apc = isw.getAlignedPC();
                int npc = isw.getNextPC();
                int caseCount = isw.getCaseCount();
                bc_case_count.putInt(caseCount);
                putLabel(bc_label, code, i.getPC(), isw.getDefaultLabel());
                for (int j = 0; j < caseCount; j++) {
                    putLabel(bc_label, code, i.getPC(), isw.getCaseLabel(j));
                }
                if (bc == _tableswitch) {
                    bc_case_value.putInt(isw.getCaseValue(0));
                } else {
                    for (int j = 0; j < caseCount; j++) {
                        bc_case_value.putInt(isw.getCaseValue(j));
                    }
                }
                continue;
            }
            switch (bc) {
            case _xxxunusedxxx:  
                {
                    i.setNonstandardLength(3);
                    int refx = i.getShortAt(1);
                    Entry ref = (refx == 0)? null: curCPMap[refx];
                    bc_codes.putByte(_byte_escape);
                    bc_escsize.putInt(1);     
                    bc_escbyte.putByte(bc);   
                    bc_codes.putByte(_ref_escape);
                    bc_escrefsize.putInt(2);  
                    bc_escref.putRef(ref);    
                    continue;
                }
            }
            int branch = i.getBranchLabel();
            if (branch >= 0) {
                bc_codes.putByte(bc);
                putLabel(bc_label, code, i.getPC(), branch);
                continue;
            }
            Entry ref = i.getCPRef(curCPMap);
            if (ref != null) {
                if (bc == _new)  newClass = ref;
                if (bc == _ldc)  ldcHist[ref.tag]++;
                CPRefBand bc_which;
                int vbc = bc;
                switch (i.getCPTag()) {
                case CONSTANT_Literal:
                    switch (ref.tag) {
                    case CONSTANT_Integer:
                        bc_which = bc_intref;
                        switch (bc) {
                        case _ldc:    vbc = _ildc; break;
                        case _ldc_w:  vbc = _ildc_w; break;
                        default:      assert(false);
                        }
                        break;
                    case CONSTANT_Float:
                        bc_which = bc_floatref;
                        switch (bc) {
                        case _ldc:    vbc = _fldc; break;
                        case _ldc_w:  vbc = _fldc_w; break;
                        default:      assert(false);
                        }
                        break;
                    case CONSTANT_Long:
                        bc_which = bc_longref;
                        assert(bc == _ldc2_w);
                        vbc = _lldc2_w;
                        break;
                    case CONSTANT_Double:
                        bc_which = bc_doubleref;
                        assert(bc == _ldc2_w);
                        vbc = _dldc2_w;
                        break;
                    case CONSTANT_String:
                        bc_which = bc_stringref;
                        switch (bc) {
                        case _ldc:    vbc = _aldc; break;
                        case _ldc_w:  vbc = _aldc_w; break;
                        default:      assert(false);
                        }
                        break;
                    case CONSTANT_Class:
                        bc_which = bc_classref;
                        switch (bc) {
                        case _ldc:    vbc = _cldc; break;
                        case _ldc_w:  vbc = _cldc_w; break;
                        default:      assert(false);
                        }
                        break;
                    default:
                        bc_which = null;
                        assert(false);
                    }
                    break;
                case CONSTANT_Class:
                    if (ref == curClass.thisClass)  ref = null;
                    bc_which = bc_classref; break;
                case CONSTANT_Fieldref:
                    bc_which = bc_fieldref; break;
                case CONSTANT_Methodref:
                    bc_which = bc_methodref; break;
                case CONSTANT_InterfaceMethodref:
                    bc_which = bc_imethodref; break;
                default:
                    bc_which = null;
                    assert(false);
                }
                bc_codes.putByte(vbc);
                bc_which.putRef(ref);
                if (bc == _multianewarray) {
                    assert(i.getConstant() == code.getByte(i.getPC()+3));
                    bc_byte.putByte(0xFF & i.getConstant());
                } else if (bc == _invokeinterface) {
                    assert(i.getLength() == 5);
                    assert(i.getConstant() == (1+((MemberEntry)ref).descRef.typeRef.computeSize(true)) << 8);
                } else {
                    assert(i.getLength() == ((bc == _ldc)?2:3));
                }
                continue;
            }
            int slot = i.getLocalSlot();
            if (slot >= 0) {
                bc_codes.putByte(bc);
                bc_local.putInt(slot);
                int con = i.getConstant();
                if (bc == _iinc) {
                    if (!i.isWide()) {
                        bc_byte.putByte(0xFF & con);
                    } else {
                        bc_short.putInt(0xFFFF & con);
                    }
                } else {
                    assert(con == 0);
                }
                continue;
            }
            bc_codes.putByte(bc);
            int pc = i.getPC()+1;
            int npc = i.getNextPC();
            if (pc < npc) {
                switch (bc) {
                case _sipush:
                    bc_short.putInt(0xFFFF & i.getConstant());
                    break;
                case _bipush:
                    bc_byte.putByte(0xFF & i.getConstant());
                    break;
                case _newarray:
                    bc_byte.putByte(0xFF & i.getConstant());
                    break;
                default:
                    assert(false);  
                }
            }
        }
        bc_codes.putByte(_end_marker);
        bc_codes.elementCountForDebug++;
        codeHist[_end_marker]++;
        endCode();
    }
    int[] codeHist = new int[1<<8];
    int[] ldcHist  = new int[20];
    void printCodeHist() {
        assert(verbose > 0);
        String[] hist = new String[codeHist.length];
        int totalBytes = 0;
        for (int bc = 0; bc < codeHist.length; bc++) {
            totalBytes += codeHist[bc];
        }
        for (int bc = 0; bc < codeHist.length; bc++) {
            if (codeHist[bc] == 0) { hist[bc] = ""; continue; }
            String iname = Instruction.byteName(bc);
            String count = "" + codeHist[bc];
            count = "         ".substring(count.length()) + count;
            String pct = "" + (codeHist[bc] * 10000 / totalBytes);
            while (pct.length() < 4) {
                pct = "0" + pct;
            }
            pct = pct.substring(0, pct.length()-2) + "." + pct.substring(pct.length()-2);
            hist[bc] = count + "  " + pct + "%  " + iname;
        }
        Arrays.sort(hist);
        System.out.println("Bytecode histogram ["+totalBytes+"]");
        for (int i = hist.length; --i >= 0; ) {
            if ("".equals(hist[i]))  continue;
            System.out.println(hist[i]);
        }
        for (int tag = 0; tag < ldcHist.length; tag++) {
            int count = ldcHist[tag];
            if (count == 0)  continue;
            System.out.println("ldc "+ConstantPool.tagName(tag)+" "+count);
        }
    }
}
