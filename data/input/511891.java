public final class UnresolvedPermission extends Permission
    implements Serializable {
    private static final long serialVersionUID = -4821973115467008846L;
    private String type;    
    private String name;
    private String actions;
    private transient Certificate[] targetCerts;
    private transient int hash;
    public UnresolvedPermission(String type, String name, String actions,
                                Certificate[] certs) {
        super(type);
        checkType(type);
        this.type = type;
        this.name = name;
        this.actions = actions;
        if (certs != null) {
            this.targetCerts = new Certificate[certs.length];
            System.arraycopy(certs, 0, targetCerts, 0, certs.length);
        }
        hash = 0;
    }
    private final void checkType(String type) {
        if (type == null) {
            throw new NullPointerException(Messages.getString("security.2F")); 
        }
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof UnresolvedPermission) {
            UnresolvedPermission that = (UnresolvedPermission) obj;
            if (getName().equals(that.getName())
                    && (name == null ? that.name == null : name
                            .equals(that.name))
                    && (actions == null ? that.actions == null : actions
                            .equals(that.actions))
                    && equalsCertificates(this.targetCerts, that.targetCerts)) {
                return true;
            }
        }
        return false;
    }
    private boolean equalsCertificates(Certificate[] certs1,
            Certificate[] certs2) {
        if (certs1 == null || certs2 == null) {
            return certs1 == certs2;
        }
        int length = certs1.length;
        if (length != certs2.length) {
            return false;
        }
        if (length > 0) {
            boolean found;
            for (int i = 0; i < length; i++) {
            	if(certs1[i] == null){
            		continue;
            	}
                found = false;
                for (int j = 0; j < length; j++) {
                    if (certs1[i].equals(certs2[j])) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            for (int i = 0; i < length; i++) {
            	if(certs2[i] == null){
            		continue;
            	}
                found = false;
                for (int j = 0; j < length; j++) {
                    if (certs2[i].equals(certs1[j])) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
        }
        return true;
    }
    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = getName().hashCode();
            if (name != null) {
                hash ^= name.hashCode();
            }
            if (actions != null) {
                hash ^= actions.hashCode();
            }
        }
        return hash;
    }
    @Override
    public String getActions() {
        return ""; 
    }
    public String getUnresolvedName() {
        return name;
    }
    public String getUnresolvedActions() {
        return actions;
    }
    public String getUnresolvedType() {
        return super.getName();
    }
    public Certificate[] getUnresolvedCerts() {
        if (targetCerts != null) {
            Certificate[] certs = new Certificate[targetCerts.length];
            System.arraycopy(targetCerts, 0, certs, 0, certs.length);
            return certs;
        }
        return null;
    }
    @Override
    public boolean implies(Permission permission) {
        return false;
    }
    @Override
    public String toString() {
        return "(unresolved " + type + " " + name + " " 
            + actions + ")"; 
    }
    @Override
    public PermissionCollection newPermissionCollection() {
        return new UnresolvedPermissionCollection();
    }
    Permission resolve(Class targetType) {
        if (PolicyUtils.matchSubset(targetCerts, targetType.getSigners())) {
            try {
                return PolicyUtils.instantiatePermission(targetType,
                                                         name,
                                                         actions);
            } catch (Exception ignore) {
            }
        }
        return null;
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        if (targetCerts == null) {
            out.writeInt(0);
        } else {
            out.writeInt(targetCerts.length);
            for (int i = 0; i < targetCerts.length; i++) {
                try {
                    byte[] enc = targetCerts[i].getEncoded();
                    out.writeUTF(targetCerts[i].getType());
                    out.writeInt(enc.length);
                    out.write(enc);
                } catch (CertificateEncodingException cee) {
                    throw ((IOException)new NotSerializableException(
                        Messages.getString("security.30",  
                        targetCerts[i])).initCause(cee));
                }
            }
        }
    }
    private void readObject(ObjectInputStream in) throws IOException,
        ClassNotFoundException {
        in.defaultReadObject();        
        checkType(getUnresolvedType());      
        int certNumber = in.readInt();
        if (certNumber != 0) {
            targetCerts = new Certificate[certNumber];
            for (int i = 0; i < certNumber; i++) {
                try {
                    String type = in.readUTF();
                    int length = in.readInt();
                    byte[] enc = new byte[length];
                    in.readFully(enc, 0, length);
                    targetCerts[i] = CertificateFactory.getInstance(type)
                        .generateCertificate(new ByteArrayInputStream(enc));
                } catch (CertificateException cee) {
                    throw ((IOException)new IOException(
                        Messages.getString("security.32")).initCause(cee)); 
                }
            }
        }
    }
}
