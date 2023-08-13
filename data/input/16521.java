class KinitOptions {
    public boolean validate = false;
    public short forwardable = -1;
    public short proxiable = -1;
    public boolean renew = false;
    public KerberosTime lifetime;
    public KerberosTime renewable_lifetime;
    public String target_service;
    public String keytab_file;
    public String cachename;
    private PrincipalName principal;
    public String realm;
    char[] password = null;
    public boolean keytab;
    private boolean DEBUG = Krb5.DEBUG;
    private boolean includeAddresses = true; 
    private boolean useKeytab = false; 
    private String ktabName; 
    public KinitOptions() throws RuntimeException, RealmException {
        cachename = FileCredentialsCache.getDefaultCacheName();
        if (cachename == null) {
            throw new RuntimeException("default cache name error");
        }
        principal = getDefaultPrincipal();
    }
    public void setKDCRealm(String r) throws RealmException {
        realm = r;
    }
    public String getKDCRealm() {
        if (realm == null) {
            if (principal != null) {
                return principal.getRealmString();
            }
        }
        return null;
    }
    public KinitOptions(String[] args)
        throws KrbException, RuntimeException, IOException {
        String p = null; 
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-f")) {
                forwardable = 1;
            } else if (args[i].equals("-p")) {
                proxiable = 1;
            } else if (args[i].equals("-c")) {
                if (args[i + 1].startsWith("-")) {
                    throw new IllegalArgumentException("input format " +
                                                       " not correct: " +
                                                       " -c  option " +
                                                       "must be followed " +
                                                       "by the cache name");
                }
                cachename = args[++i];
                if ((cachename.length() >= 5) &&
                    cachename.substring(0, 5).equalsIgnoreCase("FILE:")) {
                    cachename = cachename.substring(5);
                };
            } else if (args[i].equals("-A")) {
                includeAddresses = false;
            } else if (args[i].equals("-k")) {
                useKeytab = true;
            } else if (args[i].equals("-t")) {
                if (ktabName != null) {
                    throw new IllegalArgumentException
                        ("-t option/keytab file name repeated");
                } else if (i + 1 < args.length) {
                    ktabName = args[++i];
                } else {
                    throw new IllegalArgumentException
                        ("-t option requires keytab file name");
                }
                useKeytab = true;
            } else if (args[i].equalsIgnoreCase("-help")) {
                printHelp();
                System.exit(0);
            } else if (p == null) { 
                p = args[i];
                try {
                    principal = new PrincipalName(p);
                } catch (Exception e) {
                    throw new IllegalArgumentException("invalid " +
                                                       "Principal name: " + p +
                                                       e.getMessage());
                }
                if (principal.getRealm() == null) {
                    String realm =
                        Config.getInstance().getDefault("default_realm",
                                                        "libdefaults");
                    if (realm != null) {
                        principal.setRealm(realm);
                    } else throw new IllegalArgumentException("invalid " +
                                                              "Realm name");
                }
            } else if (this.password == null) {
                password = args[i].toCharArray();
            } else {
                throw new IllegalArgumentException("too many parameters");
            }
        }
        if (cachename == null) {
            cachename = FileCredentialsCache.getDefaultCacheName();
            if (cachename == null) {
                throw new RuntimeException("default cache name error");
            }
        }
        if (principal == null) {
            principal = getDefaultPrincipal();
        }
    }
    PrincipalName getDefaultPrincipal() {
        String cname;
        String realm = null;
        try {
            realm = Config.getInstance().getDefaultRealm();
        } catch (KrbException e) {
            System.out.println ("Can not get default realm " +
                                e.getMessage());
            e.printStackTrace();
            return null;
        }
        try {
            CCacheInputStream cis =
                new CCacheInputStream(new FileInputStream(cachename));
            int version;
            if ((version = cis.readVersion()) ==
                    FileCCacheConstants.KRB5_FCC_FVNO_4) {
                cis.readTag();
            } else {
                if (version == FileCCacheConstants.KRB5_FCC_FVNO_1 ||
                        version == FileCCacheConstants.KRB5_FCC_FVNO_2) {
                    cis.setNativeByteOrder();
                }
            }
            PrincipalName p = cis.readPrincipal(version);
            cis.close();
            String temp = p.getRealmString();
            if (temp == null) {
                p.setRealm(realm);
            }
            if (DEBUG) {
                System.out.println(">>>KinitOptions principal name from "+
                                   "the cache is :" + p);
            }
            return p;
        } catch (IOException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        } catch (RealmException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        String username = System.getProperty("user.name");
        if (DEBUG) {
            System.out.println(">>>KinitOptions default username is :"
                               + username);
        }
        if (realm != null) {
            try {
                PrincipalName p = new PrincipalName(username);
                if (p.getRealm() == null)
                    p.setRealm(realm);
                return p;
            } catch (RealmException e) {
                if (DEBUG) {
                    System.out.println ("Exception in getting principal " +
                                        "name " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    void printHelp() {
        System.out.println("Usage: kinit " +
                           "[-A] [-f] [-p] [-c cachename] " +
                           "[[-k [-t keytab_file_name]] [principal] " +
                           "[password]");
        System.out.println("\tavailable options to " +
                           "Kerberos 5 ticket request:");
        System.out.println("\t    -A   do not include addresses");
        System.out.println("\t    -f   forwardable");
        System.out.println("\t    -p   proxiable");
        System.out.println("\t    -c   cache name " +
                           "(i.e., FILE:\\d:\\myProfiles\\mykrb5cache)");
        System.out.println("\t    -k   use keytab");
        System.out.println("\t    -t   keytab file name");
        System.out.println("\t    principal   the principal name "+
                           "(i.e., qweadf@ATHENA.MIT.EDU qweadf)");
        System.out.println("\t    password   " +
                           "the principal's Kerberos password");
    }
    public boolean getAddressOption() {
        return includeAddresses;
    }
    public boolean useKeytabFile() {
        return useKeytab;
    }
    public String keytabFileName() {
        return ktabName;
    }
    public PrincipalName getPrincipal() {
        return principal;
    }
}
