public class CommandRecognizerEngine extends RecognizerEngine {
    private static final String OPEN_ENTRIES = "openentries.txt";
    public static final String PHONE_TYPE_EXTRA = "phone_type";
    private static final int MINIMUM_CONFIDENCE = 100;
    private File mContactsFile;
    private boolean mMinimizeResults;
    private boolean mAllowOpenEntries;
    private HashMap<String,String> mOpenEntries;
    public CommandRecognizerEngine() {
        mContactsFile = null;
        mMinimizeResults = false;
        mAllowOpenEntries = true;
    }
    public void setContactsFile(File contactsFile) {
        if (contactsFile != mContactsFile) {
            mContactsFile = contactsFile;
            if (mSrecGrammar != null) {
                mSrecGrammar.destroy();
                mSrecGrammar = null;
                mOpenEntries = null;
            }
        }
    }
    public void setMinimizeResults(boolean minimizeResults) {
        mMinimizeResults = minimizeResults;
    }
    public void setAllowOpenEntries(boolean allowOpenEntries) {
        if (mAllowOpenEntries != allowOpenEntries) {
            if (mSrecGrammar != null) {
                mSrecGrammar.destroy();
                mSrecGrammar = null;
                mOpenEntries = null;
            }
        }
        mAllowOpenEntries = allowOpenEntries;
    }
    protected void setupGrammar() throws IOException, InterruptedException {
        if (Config.LOGD) Log.d(TAG, "start getVoiceContacts");
        if (Config.LOGD) Log.d(TAG, "contactsFile is " + (mContactsFile == null ?
            "null" : "not null"));
        List<VoiceContact> contacts = mContactsFile != null ?
                VoiceContact.getVoiceContactsFromFile(mContactsFile) :
                VoiceContact.getVoiceContacts(mActivity);
        if (mLogger != null) mLogger.logContacts(contacts);
        File g2g = mActivity.getFileStreamPath("voicedialer." +
                Integer.toHexString(contacts.hashCode()) + ".g2g");
        if (!g2g.exists()) {
            deleteAllG2GFiles(mActivity);
            if (mSrecGrammar != null) {
                mSrecGrammar.destroy();
                mSrecGrammar = null;
            }
            if (Config.LOGD) Log.d(TAG, "start new Grammar");
            mSrecGrammar = mSrec.new Grammar(SREC_DIR + "/grammars/VoiceDialer.g2g");
            mSrecGrammar.setupRecognizer();
            if (Config.LOGD) Log.d(TAG, "start grammar.resetAllSlots");
            mSrecGrammar.resetAllSlots();
            addNameEntriesToGrammar(contacts);
            if (mAllowOpenEntries) {
                addOpenEntriesToGrammar();
            }
            if (Config.LOGD) Log.d(TAG, "start grammar.compile");
            mSrecGrammar.compile();
            if (Config.LOGD) Log.d(TAG, "start grammar.save " + g2g.getPath());
            g2g.getParentFile().mkdirs();
            mSrecGrammar.save(g2g.getPath());
        }
        else if (mSrecGrammar == null) {
            if (Config.LOGD) Log.d(TAG, "start new Grammar loading " + g2g);
            mSrecGrammar = mSrec.new Grammar(g2g.getPath());
            mSrecGrammar.setupRecognizer();
        }
        if (mOpenEntries == null && mAllowOpenEntries) {
            loadOpenEntriesTable();
        }
    }
    private void addNameEntriesToGrammar(List<VoiceContact> contacts)
            throws InterruptedException {
        if (Config.LOGD) Log.d(TAG, "addNameEntriesToGrammar " + contacts.size());
        HashSet<String> entries = new HashSet<String>();
        StringBuffer sb = new StringBuffer();
        int count = 0;
        for (VoiceContact contact : contacts) {
            if (Thread.interrupted()) throw new InterruptedException();
            String name = scrubName(contact.mName);
            if (name.length() == 0 || !entries.add(name)) continue;
            sb.setLength(0);
            sb.append("V='");
            sb.append(contact.mContactId).append(' ');
            sb.append(contact.mPrimaryId).append(' ');
            sb.append(contact.mHomeId).append(' ');
            sb.append(contact.mMobileId).append(' ');
            sb.append(contact.mWorkId).append(' ');
            sb.append(contact.mOtherId);
            sb.append("'");
            try {
                mSrecGrammar.addWordToSlot("@Names", name, null, 1, sb.toString());
            } catch (Exception e) {
                Log.e(TAG, "Cannot load all contacts to voice recognizer, loaded " +
                        count, e);
                break;
            }
            count++;
        }
    }
    private void loadOpenEntriesTable() throws InterruptedException, IOException {
        if (Config.LOGD) Log.d(TAG, "addOpenEntriesToGrammar");
        File oe = mActivity.getFileStreamPath(OPEN_ENTRIES);
        if (!oe.exists()) {
            mOpenEntries = new HashMap<String, String>();
            PackageManager pm = mActivity.getPackageManager();
            List<ResolveInfo> riList = pm.queryIntentActivities(
                            new Intent(Intent.ACTION_MAIN).
                            addCategory("android.intent.category.VOICE_LAUNCH"),
                            PackageManager.GET_ACTIVITIES);
            if (Thread.interrupted()) throw new InterruptedException();
            riList.addAll(pm.queryIntentActivities(
                            new Intent(Intent.ACTION_MAIN).
                            addCategory("android.intent.category.LAUNCHER"),
                            PackageManager.GET_ACTIVITIES));
            String voiceDialerClassName = mActivity.getComponentName().getClassName();
            for (ResolveInfo ri : riList) {
                if (Thread.interrupted()) throw new InterruptedException();
                if (voiceDialerClassName.equals(ri.activityInfo.name)) continue;
                String label = scrubName(ri.loadLabel(pm).toString());
                if (label.length() == 0) continue;
                addClassName(mOpenEntries, label,
                        ri.activityInfo.packageName, ri.activityInfo.name);
                String[] words = label.split(" ");
                if (words.length > 1) {
                    for (String word : words) {
                        word = word.trim();
                        int len = word.length();
                        if (len <= 1) continue;
                        if (len == 2 && !(Character.isUpperCase(word.charAt(0)) &&
                                        Character.isUpperCase(word.charAt(1)))) continue;
                        if ("and".equalsIgnoreCase(word) ||
                                "the".equalsIgnoreCase(word)) continue;
                        addClassName(mOpenEntries, word,
                                ri.activityInfo.packageName, ri.activityInfo.name);
                    }
                }
            }
            if (Config.LOGD) Log.d(TAG, "addOpenEntriesToGrammar writing " + oe);
            try {
                 FileOutputStream fos = new FileOutputStream(oe);
                 try {
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(mOpenEntries);
                    oos.close();
                } finally {
                    fos.close();
                }
            } catch (IOException ioe) {
                deleteCachedGrammarFiles(mActivity);
                throw ioe;
            }
        }
        else {
            if (Config.LOGD) Log.d(TAG, "addOpenEntriesToGrammar reading " + oe);
            try {
                FileInputStream fis = new FileInputStream(oe);
                try {
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    mOpenEntries = (HashMap<String, String>)ois.readObject();
                    ois.close();
                } finally {
                    fis.close();
                }
            } catch (Exception e) {
                deleteCachedGrammarFiles(mActivity);
                throw new IOException(e.toString());
            }
        }
    }
    private void addOpenEntriesToGrammar() throws InterruptedException, IOException {
        loadOpenEntriesTable();
        for (String label : mOpenEntries.keySet()) {
            if (Thread.interrupted()) throw new InterruptedException();
            String entry = mOpenEntries.get(label);
            int count = 0;
            for (int i = 0; 0 != (i = entry.indexOf(' ', i) + 1); count++) ;
            if (count > RESULT_LIMIT) continue;
            mSrecGrammar.addWordToSlot("@Opens", label, null, 1, "V='" + label + "'");
        }
    }
    private static void addClassName(HashMap<String,String> openEntries,
            String label, String packageName, String className) {
        String component = packageName + "/" + className;
        String labelLowerCase = label.toLowerCase();
        String classList = openEntries.get(labelLowerCase);
        if (classList == null) {
            openEntries.put(labelLowerCase, component);
            return;
        }
        int index = classList.indexOf(component);
        int after = index + component.length();
        if (index != -1 && (index == 0 || classList.charAt(index - 1) == ' ') &&
                (after == classList.length() || classList.charAt(after) == ' ')) return;
        openEntries.put(labelLowerCase, classList + ' ' + component);
    }
    private final static char[] mLatin1Letters =
            "AAAAAAACEEEEIIIIDNOOOOO OUUUUYDsaaaaaaaceeeeiiiidnooooo ouuuuydy".
            toCharArray();
    private final static int mLatin1Base = 0x00c0;
    private static String scrubName(String name) {
        name = name.replace("&", " and ");
        name = name.replace("@", " at ");
        while (true) {
            int i = name.indexOf('(');
            if (i == -1) break;
            int j = name.indexOf(')', i);
            if (j == -1) break;
            name = name.substring(0, i) + " " + name.substring(j + 1);
        }
        char[] nm = null;
        for (int i = name.length() - 1; i >= 0; i--) {
            char ch = name.charAt(i);
            if (ch < ' ' || '~' < ch) {
                if (nm == null) nm = name.toCharArray();
                nm[i] = mLatin1Base <= ch && ch < mLatin1Base + mLatin1Letters.length ?
                    mLatin1Letters[ch - mLatin1Base] : ' ';
            }
        }
        if (nm != null) {
            name = new String(nm);
        }
        while (true) {
            int i = name.indexOf('.');
            if (i == -1 ||
                    i + 1 >= name.length() ||
                    !Character.isLetterOrDigit(name.charAt(i + 1))) break;
            name = name.substring(0, i) + " dot " + name.substring(i + 1);
        }
        name = name.trim();
        for (int i = name.length() - 1; true; i--) {
            if (i < 0) return "";
            char ch = name.charAt(i);
            if (('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z') || ('0' <= ch && ch <= '9')) {
                break;
            }
        }
        return name;
    }
    private static void deleteAllG2GFiles(Context context) {
        FileFilter ff = new FileFilter() {
            public boolean accept(File f) {
                String name = f.getName();
                return name.endsWith(".g2g");
            }
        };
        File[] files = context.getFilesDir().listFiles(ff);
        if (files != null) {
            for (File file : files) {
                if (Config.LOGD) Log.d(TAG, "deleteAllG2GFiles " + file);
                file.delete();
            }
        }
    }
    public static void deleteCachedGrammarFiles(Context context) {
        deleteAllG2GFiles(context);
        File oe = context.getFileStreamPath(OPEN_ENTRIES);
        if (Config.LOGD) Log.v(TAG, "deleteCachedGrammarFiles " + oe);
        if (oe.exists()) oe.delete();
    }
    private final static String mNanpFormats =
        "xxx xxx xxxx\n" +
        "xxx xxxx\n" +
        "x11\n";
    private final static String mPlusFormats =
        "+1 xxx xxx xxxx\n" +         
        "+20 x xxx xxxx\n" +          
        "+20 1x xxx xxxx\n" +         
        "+20 xx xxx xxxx\n" +         
        "+20 xxx xxx xxxx\n" +        
        "+212 xxxx xxxx\n" +          
        "+213 xx xx xx xx\n" +        
        "+213 xx xxx xxxx\n" +        
        "+216 xx xxx xxx\n" +         
        "+218 xx xxx xxx\n" +         
        "+22x \n" +
        "+23x \n" +
        "+24x \n" +
        "+25x \n" +
        "+26x \n" +
        "+27 xx xxx xxxx\n" +         
        "+290 x xxx\n" +              
        "+291 x xxx xxx\n" +          
        "+297 xxx xxxx\n" +           
        "+298 xxx xxx\n" +            
        "+299 xxx xxx\n" +            
        "+30 xxx xxx xxxx\n" +        
        "+31 6 xxxx xxxx\n" +         
        "+31 xx xxx xxxx\n" +         
        "+31 xxx xx xxxx\n" +         
        "+32 2 xxx xx xx\n" +         
        "+32 3 xxx xx xx\n" +         
        "+32 4xx xx xx xx\n" +        
        "+32 9 xxx xx xx\n" +         
        "+32 xx xx xx xx\n" +         
        "+33 xxx xxx xxx\n" +         
        "+34 xxx xxx xxx\n" +        
        "+351 3xx xxx xxx\n" +       
        "+351 7xx xxx xxx\n" +       
        "+351 8xx xxx xxx\n" +       
        "+351 xx xxx xxxx\n" +       
        "+352 xx xxxx\n" +           
        "+352 6x1 xxx xxx\n" +       
        "+352 \n" +                  
        "+353 xxx xxxx\n" +          
        "+353 xxxx xxxx\n" +         
        "+353 xx xxx xxxx\n" +       
        "+354 3xx xxx xxx\n" +       
        "+354 xxx xxxx\n" +          
        "+355 6x xxx xxxx\n" +       
        "+355 xxx xxxx\n" +          
        "+356 xx xx xx xx\n" +       
        "+357 xx xx xx xx\n" +       
        "+358 \n" +                  
        "+359 \n" +                  
        "+36 1 xxx xxxx\n" +         
        "+36 20 xxx xxxx\n" +        
        "+36 21 xxx xxxx\n" +        
        "+36 30 xxx xxxx\n" +        
        "+36 70 xxx xxxx\n" +        
        "+36 71 xxx xxxx\n" +        
        "+36 xx xxx xxx\n" +         
        "+370 6x xxx xxx\n" +        
        "+370 xxx xx xxx\n" +        
        "+371 xxxx xxxx\n" +         
        "+372 5 xxx xxxx\n" +        
        "+372 xxx xxxx\n" +          
        "+373 6xx xx xxx\n" +        
        "+373 7xx xx xxx\n" +        
        "+373 xxx xxxxx\n" +         
        "+374 xx xxx xxx\n" +        
        "+375 xx xxx xxxx\n" +       
        "+376 xx xx xx\n" +          
        "+377 xxxx xxxx\n" +         
        "+378 xxx xxx xxxx\n" +      
        "+380 xxx xx xx xx\n" +      
        "+381 xx xxx xxxx\n" +       
        "+382 xx xxx xxxx\n" +       
        "+385 xx xxx xxxx\n" +       
        "+386 x xxx xxxx\n" +        
        "+387 xx xx xx xx\n" +       
        "+389 2 xxx xx xx\n" +       
        "+389 xx xx xx xx\n" +       
        "+39 xxx xxx xxx\n" +        
        "+39 3xx xxx xxxx\n" +       
        "+39 xx xxxx xxxx\n" +       
        "+40 xxx xxx xxx\n" +        
        "+41 xx xxx xx xx\n" +       
        "+420 xxx xxx xxx\n" +       
        "+421 xxx xxx xxx\n" +       
        "+421 xxx xxx xxxx\n" +      
        "+43 \n" +                   
        "+44 xxx xxx xxxx\n" +       
        "+45 xx xx xx xx\n" +        
        "+46 \n" +                   
        "+47 xxxx xxxx\n" +          
        "+48 xx xxx xxxx\n" +        
        "+49 1xx xxxx xxx\n" +       
        "+49 1xx xxxx xxxx\n" +      
        "+49 \n" +                   
        "+50x \n" +
        "+51 9xx xxx xxx\n" +        
        "+51 1 xxx xxxx\n" +         
        "+51 xx xx xxxx\n" +         
        "+52 1 xxx xxx xxxx\n" +     
        "+52 xxx xxx xxxx\n" +       
        "+53 xxxx xxxx\n" +          
        "+54 9 11 xxxx xxxx\n" +     
        "+54 9 xxx xxx xxxx\n" +     
        "+54 11 xxxx xxxx\n" +       
        "+54 xxx xxx xxxx\n" +       
        "+55 xx xxxx xxxx\n" +       
        "+56 2 xxxxxx\n" +           
        "+56 9 xxxx xxxx\n" +        
        "+56 xx xxxxxx\n" +          
        "+56 xx xxxxxxx\n" +         
        "+57 x xxx xxxx\n" +         
        "+57 3xx xxx xxxx\n" +       
        "+58 xxx xxx xxxx\n" +       
        "+59x \n" +
        "+60 3 xxxx xxxx\n" +        
        "+60 8x xxxxxx\n" +          
        "+60 x xxx xxxx\n" +         
        "+60 14 x xxx xxxx\n" +      
        "+60 1x xxx xxxx\n" +        
        "+60 x xxxx xxxx\n" +        
        "+60 \n" +                   
        "+61 4xx xxx xxx\n" +        
        "+61 x xxxx xxxx\n" +        
        "+62 8xx xxxx xxxx\n" +      
        "+62 21 xxxxx\n" +           
        "+62 xx xxxxxx\n" +          
        "+62 xx xxx xxxx\n" +        
        "+62 xx xxxx xxxx\n" +       
        "+63 2 xxx xxxx\n" +         
        "+63 xx xxx xxxx\n" +        
        "+63 9xx xxx xxxx\n" +       
        "+64 2 xxx xxxx\n" +         
        "+64 2 xxx xxxx x\n" +       
        "+64 2 xxx xxxx xx\n" +      
        "+64 x xxx xxxx\n" +         
        "+65 xxxx xxxx\n" +          
        "+66 8 xxxx xxxx\n" +        
        "+66 2 xxx xxxx\n" +         
        "+66 xx xx xxxx\n" +         
        "+67x \n" +
        "+68x \n" +
        "+690 x xxx\n" +             
        "+691 xxx xxxx\n" +          
        "+692 xxx xxxx\n" +          
        "+7 6xx xx xxxxx\n" +        
        "+7 7xx 2 xxxxxx\n" +        
        "+7 7xx xx xxxxx\n" +        
        "+7 xxx xxx xx xx\n" +       
        "+81 3 xxxx xxxx\n" +        
        "+81 6 xxxx xxxx\n" +        
        "+81 xx xxx xxxx\n" +        
        "+81 x0 xxxx xxxx\n" +       
        "+82 2 xxx xxxx\n" +         
        "+82 2 xxxx xxxx\n" +        
        "+82 xx xxxx xxxx\n" +       
        "+82 xx xxx xxxx\n" +        
        "+84 4 xxxx xxxx\n" +        
        "+84 xx xxxx xxx\n" +        
        "+84 xx xxxx xxxx\n" +       
        "+850 \n" +                  
        "+852 xxxx xxxx\n" +         
        "+853 xxxx xxxx\n" +         
        "+855 1x xxx xxx\n" +        
        "+855 9x xxx xxx\n" +        
        "+855 xx xx xx xx\n" +       
        "+856 20 x xxx xxx\n" +      
        "+856 xx xxx xxx\n" +        
        "+852 xxxx xxxx\n" +         
        "+86 10 xxxx xxxx\n" +       
        "+86 2x xxxx xxxx\n" +       
        "+86 xxx xxx xxxx\n" +       
        "+86 xxx xxxx xxxx\n" +      
        "+880 xx xxxx xxxx\n" +      
        "+886 \n" +                  
        "+90 xxx xxx xxxx\n" +       
        "+91 9x xx xxxxxx\n" +       
        "+91 xx xxxx xxxx\n" +       
        "+92 xx xxx xxxx\n" +        
        "+92 3xx xxx xxxx\n" +       
        "+93 70 xxx xxx\n" +         
        "+93 xx xxx xxxx\n" +        
        "+94 xx xxx xxxx\n" +        
        "+95 1 xxx xxx\n" +          
        "+95 2 xxx xxx\n" +          
        "+95 xx xxxxx\n" +           
        "+95 9 xxx xxxx\n" +         
        "+960 xxx xxxx\n" +          
        "+961 x xxx xxx\n" +         
        "+961 xx xxx xxx\n" +        
        "+962 7 xxxx xxxx\n" +       
        "+962 x xxx xxxx\n" +        
        "+963 11 xxx xxxx\n" +       
        "+963 xx xxx xxx\n" +        
        "+964 \n" +                  
        "+965 xxxx xxxx\n" +         
        "+966 5x xxx xxxx\n" +       
        "+966 x xxx xxxx\n" +        
        "+967 7xx xxx xxx\n" +       
        "+967 x xxx xxx\n" +         
        "+968 xxxx xxxx\n" +         
        "+970 5x xxx xxxx\n" +       
        "+970 x xxx xxxx\n" +        
        "+971 5x xxx xxxx\n" +       
        "+971 x xxx xxxx\n" +        
        "+972 5x xxx xxxx\n" +       
        "+972 x xxx xxxx\n" +        
        "+973 xxxx xxxx\n" +         
        "+974 xxx xxxx\n" +          
        "+975 1x xxx xxx\n" +        
        "+975 x xxx xxx\n" +         
        "+976 \n" +                  
        "+977 xxxx xxxx\n" +         
        "+977 98 xxxx xxxx\n" +      
        "+98 xxx xxx xxxx\n" +       
        "+992 xxx xxx xxx\n" +       
        "+993 xxxx xxxx\n" +         
        "+994 xx xxx xxxx\n" +       
        "+994 xxx xxxxx\n" +         
        "+995 xx xxx xxx\n" +        
        "+996 xxx xxx xxx\n" +       
        "+998 xx xxx xxxx\n";        
    private static String formatNumber(String formats, String number) {
        number = number.trim();
        final int nlen = number.length();
        final int formatslen = formats.length();
        StringBuffer sb = new StringBuffer();
        for (int f = 0; f < formatslen; ) {
            sb.setLength(0);
            int n = 0;
            while (true) {
                final char fch = formats.charAt(f);
                if (fch == '\n' && n >= nlen) return sb.toString();
                if (fch == '\n' || n >= nlen) break;
                final char nch = number.charAt(n);
                if (fch == nch || (fch == 'x' && Character.isDigit(nch))) {
                    f++;
                    n++;
                    sb.append(nch);
                }
                else if (fch == ' ') {
                    f++;
                    sb.append(' ');
                    if (formats.charAt(f) == '\n') {
                        return sb.append(number, n, nlen).toString();
                    }
                }
                else break;
            }
            f = formats.indexOf('\n', f) + 1;
            if (f == 0) break;
        }
        return null;
    }
    private static String formatNumber(String num) {
        String fmt = null;
        fmt = formatNumber(mPlusFormats, num);
        if (fmt != null) return fmt;
        fmt = formatNumber(mNanpFormats, num);
        if (fmt != null) return fmt;
        return null;
    }
    protected  void onRecognitionSuccess(RecognizerClient recognizerClient)
            throws InterruptedException {
        if (Config.LOGD) Log.d(TAG, "onRecognitionSuccess");
        if (mLogger != null) mLogger.logNbestHeader();
        ArrayList<Intent> intents = new ArrayList<Intent>();
        int highestConfidence = 0;
        int examineLimit = RESULT_LIMIT;
        if (mMinimizeResults) {
            examineLimit = 1;
        }
        for (int result = 0; result < mSrec.getResultCount() &&
                intents.size() < examineLimit; result++) {
            String conf = mSrec.getResult(result, Recognizer.KEY_CONFIDENCE);
            String literal = mSrec.getResult(result, Recognizer.KEY_LITERAL);
            String semantic = mSrec.getResult(result, Recognizer.KEY_MEANING);
            String msg = "conf=" + conf + " lit=" + literal + " sem=" + semantic;
            if (Config.LOGD) Log.d(TAG, msg);
            int confInt = Integer.parseInt(conf);
            if (highestConfidence < confInt) highestConfidence = confInt;
            if (confInt < MINIMUM_CONFIDENCE || confInt * 2 < highestConfidence) {
                if (Config.LOGD) Log.d(TAG, "confidence too low, dropping");
                break;
            }
            if (mLogger != null) mLogger.logLine(msg);
            String[] commands = semantic.trim().split(" ");
            if ("DIAL".equalsIgnoreCase(commands[0])) {
                Uri uri = Uri.fromParts("tel", commands[1], null);
                String num =  formatNumber(commands[1]);
                if (num != null) {
                    addCallIntent(intents, uri,
                            literal.split(" ")[0].trim() + " " + num, "", 0);
                }
            }
            else if ("CALL".equalsIgnoreCase(commands[0]) && commands.length >= 7) {
                long contactId = Long.parseLong(commands[1]); 
                long phoneId   = Long.parseLong(commands[2]); 
                long homeId    = Long.parseLong(commands[3]); 
                long mobileId  = Long.parseLong(commands[4]); 
                long workId    = Long.parseLong(commands[5]); 
                long otherId   = Long.parseLong(commands[6]); 
                Resources res  = mActivity.getResources();
                int count = 0;
                if (commands.length == 8) {
                    long spokenPhoneId =
                            "H".equalsIgnoreCase(commands[7]) ? homeId :
                            "M".equalsIgnoreCase(commands[7]) ? mobileId :
                            "W".equalsIgnoreCase(commands[7]) ? workId :
                            "O".equalsIgnoreCase(commands[7]) ? otherId :
                             VoiceContact.ID_UNDEFINED;
                    if (spokenPhoneId != VoiceContact.ID_UNDEFINED) {
                        addCallIntent(intents, ContentUris.withAppendedId(
                                Phone.CONTENT_URI, spokenPhoneId),
                                literal, commands[7], 0);
                        count++;
                    }
                }
                else if (commands.length == 7) {
                    String phoneType = null;
                    CharSequence phoneIdMsg = null;
                    if (phoneId == VoiceContact.ID_UNDEFINED) {
                        phoneType = null;
                        phoneIdMsg = null;
                    } else if (phoneId == homeId) {
                        phoneType = "H";
                        phoneIdMsg = res.getText(R.string.at_home);
                    } else if (phoneId == mobileId) {
                        phoneType = "M";
                        phoneIdMsg = res.getText(R.string.on_mobile);
                    } else if (phoneId == workId) {
                        phoneType = "W";
                        phoneIdMsg = res.getText(R.string.at_work);
                    } else if (phoneId == otherId) {
                        phoneType = "O";
                        phoneIdMsg = res.getText(R.string.at_other);
                    }
                    if (phoneIdMsg != null) {
                        addCallIntent(intents, ContentUris.withAppendedId(
                                Phone.CONTENT_URI, phoneId),
                                literal + phoneIdMsg, phoneType, 0);
                        count++;
                    }
                }
                if (count == 0 || !mMinimizeResults) {
                    String lit = literal;
                    if (commands.length == 8) {
                        String[] words = literal.trim().split(" ");
                        StringBuffer sb = new StringBuffer();
                        for (int i = 0; i < words.length - 2; i++) {
                            if (i != 0) {
                                sb.append(' ');
                            }
                            sb.append(words[i]);
                        }
                        lit = sb.toString();
                    }
                    if (homeId != VoiceContact.ID_UNDEFINED) {
                        addCallIntent(intents, ContentUris.withAppendedId(
                                Phone.CONTENT_URI, homeId),
                                lit + res.getText(R.string.at_home), "H",  0);
                        count++;
                    }
                    if (mobileId != VoiceContact.ID_UNDEFINED) {
                        addCallIntent(intents, ContentUris.withAppendedId(
                                Phone.CONTENT_URI, mobileId),
                                lit + res.getText(R.string.on_mobile), "M", 0);
                        count++;
                    }
                    if (workId != VoiceContact.ID_UNDEFINED) {
                        addCallIntent(intents, ContentUris.withAppendedId(
                                Phone.CONTENT_URI, workId),
                                lit + res.getText(R.string.at_work), "W", 0);
                        count++;
                    }
                    if (otherId != VoiceContact.ID_UNDEFINED) {
                        addCallIntent(intents, ContentUris.withAppendedId(
                                Phone.CONTENT_URI, otherId),
                                lit + res.getText(R.string.at_other), "O", 0);
                        count++;
                    }
                }
                if (count == 0 && contactId != VoiceContact.ID_UNDEFINED) {
                    addCallIntent(intents, ContentUris.withAppendedId(
                            Contacts.CONTENT_URI, contactId), literal, "", 0);
                }
            }
            else if ("X".equalsIgnoreCase(commands[0])) {
                Intent intent = new Intent(RecognizerEngine.ACTION_RECOGNIZER_RESULT, null);
                intent.putExtra(RecognizerEngine.SENTENCE_EXTRA, literal);
                intent.putExtra(RecognizerEngine.SEMANTIC_EXTRA, semantic);
                addIntent(intents, intent);
            }
            else if ("voicemail".equalsIgnoreCase(commands[0]) && commands.length == 1) {
                addCallIntent(intents, Uri.fromParts("voicemail", "x", null),
                        literal, "", Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            }
            else if ("redial".equalsIgnoreCase(commands[0]) && commands.length == 1) {
                String number = VoiceContact.redialNumber(mActivity);
                if (number != null) {
                    addCallIntent(intents, Uri.fromParts("tel", number, null),
                            literal, "", Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                }
            }
            else if ("Intent".equalsIgnoreCase(commands[0])) {
                for (int i = 1; i < commands.length; i++) {
                    try {
                        Intent intent = Intent.getIntent(commands[i]);
                        if (intent.getStringExtra(SENTENCE_EXTRA) == null) {
                            intent.putExtra(SENTENCE_EXTRA, literal);
                        }
                        addIntent(intents, intent);
                    } catch (URISyntaxException e) {
                        if (Config.LOGD) {
                            Log.d(TAG, "onRecognitionSuccess: poorly " +
                                    "formed URI in grammar" + e);
                        }
                    }
                }
            }
            else if ("OPEN".equalsIgnoreCase(commands[0]) && mAllowOpenEntries) {
                PackageManager pm = mActivity.getPackageManager();
                if (commands.length > 1 & mOpenEntries != null) {
                    String meaning = mOpenEntries.get(commands[1]);
                    String[] components = meaning.trim().split(" ");
                    for (int i=0; i < components.length; i++) {
                        String component = components[i];
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory("android.intent.category.VOICE_LAUNCH");
                        String packageName = component.substring(
                                0, component.lastIndexOf('/'));
                        String className = component.substring(
                                component.lastIndexOf('/')+1, component.length());
                        intent.setClassName(packageName, className);
                        List<ResolveInfo> riList = pm.queryIntentActivities(intent, 0);
                        for (ResolveInfo ri : riList) {
                            String label = ri.loadLabel(pm).toString();
                            intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory("android.intent.category.VOICE_LAUNCH");
                            intent.setClassName(packageName, className);
                            intent.putExtra(SENTENCE_EXTRA, literal.split(" ")[0] + " " + label);
                            addIntent(intents, intent);
                        }
                    }
                }
            }
            else {
                if (Config.LOGD) Log.d(TAG, "onRecognitionSuccess: parse error");
            }
        }
        if (mLogger != null) mLogger.logIntents(intents);
        if (Thread.interrupted()) throw new InterruptedException();
        if (intents.size() == 0) {
            recognizerClient.onRecognitionFailure("No Intents generated");
        }
        else {
            recognizerClient.onRecognitionSuccess(
                    intents.toArray(new Intent[intents.size()]));
        }
    }
    private static void addCallIntent(ArrayList<Intent> intents, Uri uri, String literal,
            String phoneType, int flags) {
        Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED, uri).
        setFlags(flags).
        putExtra(SENTENCE_EXTRA, literal).
        putExtra(PHONE_TYPE_EXTRA, phoneType);
        addIntent(intents, intent);
    }
}
