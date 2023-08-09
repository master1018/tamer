public class NameConstraints extends ExtensionValue {
    private final GeneralSubtrees permittedSubtrees;
    private final GeneralSubtrees excludedSubtrees;
    private byte[] encoding;
    private ArrayList[] permitted_names;
    private ArrayList[] excluded_names;
    public NameConstraints() {
        this(null, null);
    }
    public NameConstraints(GeneralSubtrees permittedSubtrees, 
                           GeneralSubtrees excludedSubtrees) {
        if (permittedSubtrees != null) {
            List ps = permittedSubtrees.getSubtrees();
            if ((ps == null) || (ps.size() == 0)) {
                throw 
                    new IllegalArgumentException(Messages.getString("security.17D")); 
            }
        }
        if (excludedSubtrees != null) {
            List es = excludedSubtrees.getSubtrees();
            if ((es == null) || (es.size() == 0)) {
                throw 
                    new IllegalArgumentException(Messages.getString("security.17E")); 
            }
        }
        this.permittedSubtrees = permittedSubtrees;
        this.excludedSubtrees = excludedSubtrees;
    }
    private NameConstraints(GeneralSubtrees permittedSubtrees, 
                            GeneralSubtrees excludedSubtrees, byte[] encoding) {
        this(permittedSubtrees, excludedSubtrees);
        this.encoding = encoding;
    }
    public static NameConstraints decode(byte[] encoding) throws IOException {
        return (NameConstraints) ASN1.decode(encoding);
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    private void prepareNames() {
        permitted_names = new ArrayList[9];
        if (permittedSubtrees != null) {
            Iterator it =  permittedSubtrees.getSubtrees().iterator();
            while (it.hasNext()) {
                GeneralName name = ((GeneralSubtree) it.next()).getBase();
                int tag = name.getTag();
                if (permitted_names[tag] == null) {
                    permitted_names[tag] = new ArrayList();
                }
                permitted_names[tag].add(name);
            }
        }
        excluded_names = new ArrayList[9];
        if (excludedSubtrees != null) {
            Iterator it =  excludedSubtrees.getSubtrees().iterator();
            while (it.hasNext()) {
                GeneralName name = ((GeneralSubtree) it.next()).getBase();
                int tag = name.getTag();
                if (excluded_names[tag] == null) {
                    excluded_names[tag] = new ArrayList();
                }
                excluded_names[tag].add(name);
            }
        }
    }
    private byte[] getExtensionValue(X509Certificate cert, String OID) {
        try {
            byte[] bytes = cert.getExtensionValue(OID);
            if (bytes == null) {
                return null;
            }
            return (byte[]) ASN1OctetString.getInstance().decode(bytes);
        } catch (IOException e) {
            return null;
        }
    }
    public boolean isAcceptable(X509Certificate cert) {
        if (permitted_names == null) {
            prepareNames();
        }
        byte[] bytes = getExtensionValue(cert, "2.5.29.17"); 
        List names;
        try {
            names = (bytes == null) 
                ? new ArrayList(1) 
                : ((GeneralNames) GeneralNames.ASN1.decode(bytes)).getNames();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        if ((excluded_names[4] != null) || (permitted_names[4] != null)) {
            try {
                names.add(new GeneralName(4, 
                        cert.getSubjectX500Principal().getName()));
            } catch (IOException e) {
            }
        }
        return isAcceptable(names);
    }
    public boolean isAcceptable(List names) {
        if (permitted_names == null) {
            prepareNames();
        }
        Iterator it = names.iterator();
        boolean[] types_presented = new boolean[9];
        boolean[] permitted_found = new boolean[9];
        while (it.hasNext()) {
            GeneralName name = (GeneralName) it.next();
            int type = name.getTag();
            if (excluded_names[type] != null) {
                for (int i=0; i<excluded_names[type].size(); i++) {
                    if (((GeneralName) excluded_names[type].get(i))
                            .isAcceptable(name)) {
                        return false;
                    }
                }
            }
            if ((permitted_names[type] != null) && (!permitted_found[type])) {
                types_presented[type] = true;
                for (int i=0; i<permitted_names[type].size(); i++) {
                    if (((GeneralName) permitted_names[type].get(i))
                            .isAcceptable(name)) {
                        permitted_found[type] = true;
                    }
                }
            }
        }
        for (int type=0; type<9; type++) {
            if (types_presented[type] && !permitted_found[type]) {
                return false;
            }
        }
        return true;
    }
    public void dumpValue(StringBuffer buffer, String prefix) {
        buffer.append(prefix).append("Name Constraints: [\n"); 
        if (permittedSubtrees != null) {
            buffer.append(prefix).append("  Permitted: [\n"); 
            for (Iterator it=permittedSubtrees.getSubtrees().iterator();
                    it.hasNext();) {
                ((GeneralSubtree) it.next()).dumpValue(buffer, prefix + "    "); 
            }
            buffer.append(prefix).append("  ]\n"); 
        }
        if (excludedSubtrees != null) {
            buffer.append(prefix).append("  Excluded: [\n"); 
            for (Iterator it=excludedSubtrees.getSubtrees().iterator();
                    it.hasNext();) {
                ((GeneralSubtree) it.next()).dumpValue(buffer, prefix + "    "); 
            }
            buffer.append(prefix).append("  ]\n"); 
        }
        buffer.append('\n').append(prefix).append("]\n"); 
    }
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            new ASN1Implicit(0, GeneralSubtrees.ASN1), 
            new ASN1Implicit(1, GeneralSubtrees.ASN1) }) {
        {
            setOptional(0);
            setOptional(1);
        }
        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            return new NameConstraints(
                    (GeneralSubtrees) values[0], 
                    (GeneralSubtrees) values[1],
                    in.getEncoded());
        }
        protected void getValues(Object object, Object[] values) {
            NameConstraints nc = (NameConstraints) object;
            values[0] = nc.permittedSubtrees;
            values[1] = nc.excludedSubtrees;
        }
    };
}
