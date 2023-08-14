public class WifiConfiguration implements Parcelable {
    public static final String ssidVarName = "ssid";
    public static final String bssidVarName = "bssid";
    public static final String pskVarName = "psk";
    public static final String[] wepKeyVarNames = { "wep_key0", "wep_key1", "wep_key2", "wep_key3" };
    public static final String wepTxKeyIdxVarName = "wep_tx_keyidx";
    public static final String priorityVarName = "priority";
    public static final String hiddenSSIDVarName = "scan_ssid";
    public class EnterpriseField {
        private String varName;
        private String value;
        private EnterpriseField(String varName) {
            this.varName = varName;
            this.value = null;
        }
        public void setValue(String value) {
            this.value = value;
        }
        public String varName() {
            return varName;
        }
        public String value() {
            return value;
        }
    }
    public EnterpriseField eap = new EnterpriseField("eap");
    public EnterpriseField phase2 = new EnterpriseField("phase2");
    public EnterpriseField identity = new EnterpriseField("identity");
    public EnterpriseField anonymous_identity = new EnterpriseField("anonymous_identity");
    public EnterpriseField password = new EnterpriseField("password");
    public EnterpriseField client_cert = new EnterpriseField("client_cert");
    public EnterpriseField private_key = new EnterpriseField("private_key");
    public EnterpriseField ca_cert = new EnterpriseField("ca_cert");
    public EnterpriseField[] enterpriseFields = {
            eap, phase2, identity, anonymous_identity, password, client_cert,
            private_key, ca_cert };
    public static class KeyMgmt {
        private KeyMgmt() { }
        public static final int NONE = 0;
        public static final int WPA_PSK = 1;
        public static final int WPA_EAP = 2;
        public static final int IEEE8021X = 3;
        public static final String varName = "key_mgmt";
        public static final String[] strings = { "NONE", "WPA_PSK", "WPA_EAP", "IEEE8021X" };
    }
    public static class Protocol {
        private Protocol() { }
        public static final int WPA = 0;
        public static final int RSN = 1;
        public static final String varName = "proto";
        public static final String[] strings = { "WPA", "RSN" };
    }
    public static class AuthAlgorithm {
        private AuthAlgorithm() { }
        public static final int OPEN = 0;
        public static final int SHARED = 1;
        public static final int LEAP = 2;
        public static final String varName = "auth_alg";
        public static final String[] strings = { "OPEN", "SHARED", "LEAP" };
    }
    public static class PairwiseCipher {
        private PairwiseCipher() { }
        public static final int NONE = 0;
        public static final int TKIP = 1;
        public static final int CCMP = 2;
        public static final String varName = "pairwise";
        public static final String[] strings = { "NONE", "TKIP", "CCMP" };
    }
    public static class GroupCipher {
        private GroupCipher() { }
        public static final int WEP40 = 0;
        public static final int WEP104 = 1;
        public static final int TKIP = 2;
        public static final int CCMP = 3;
        public static final String varName = "group";
        public static final String[] strings = { "WEP40", "WEP104", "TKIP", "CCMP" };
    }
    public static class Status {
        private Status() { }
        public static final int CURRENT = 0;
        public static final int DISABLED = 1;
        public static final int ENABLED = 2;
        public static final String[] strings = { "current", "disabled", "enabled" };
    }
    public int networkId;
    public int status;
    public String SSID;
    public String BSSID;
    public String preSharedKey;
    public String[] wepKeys;
    public int wepTxKeyIndex;
    public int priority;
    public boolean hiddenSSID;
    public BitSet allowedKeyManagement;
    public BitSet allowedProtocols;
    public BitSet allowedAuthAlgorithms;
    public BitSet allowedPairwiseCiphers;
    public BitSet allowedGroupCiphers;
    public WifiConfiguration() {
        networkId = -1;
        SSID = null;
        BSSID = null;
        priority = 0;
        hiddenSSID = false;
        allowedKeyManagement = new BitSet();
        allowedProtocols = new BitSet();
        allowedAuthAlgorithms = new BitSet();
        allowedPairwiseCiphers = new BitSet();
        allowedGroupCiphers = new BitSet();
        wepKeys = new String[4];
        for (int i = 0; i < wepKeys.length; i++)
            wepKeys[i] = null;
        for (EnterpriseField field : enterpriseFields) {
            field.setValue(null);
        }
    }
    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        if (this.status == WifiConfiguration.Status.CURRENT) {
            sbuf.append("* ");
        } else if (this.status == WifiConfiguration.Status.DISABLED) {
            sbuf.append("- ");
        }
        sbuf.append("ID: ").append(this.networkId).append(" SSID: ").append(this.SSID).
                append(" BSSID: ").append(this.BSSID).append(" PRIO: ").append(this.priority).
                append('\n');
        sbuf.append(" KeyMgmt:");
        for (int k = 0; k < this.allowedKeyManagement.size(); k++) {
            if (this.allowedKeyManagement.get(k)) {
                sbuf.append(" ");
                if (k < KeyMgmt.strings.length) {
                    sbuf.append(KeyMgmt.strings[k]);
                } else {
                    sbuf.append("??");
                }
            }
        }
        sbuf.append(" Protocols:");
        for (int p = 0; p < this.allowedProtocols.size(); p++) {
            if (this.allowedProtocols.get(p)) {
                sbuf.append(" ");
                if (p < Protocol.strings.length) {
                    sbuf.append(Protocol.strings[p]);
                } else {
                    sbuf.append("??");
                }
            }
        }
        sbuf.append('\n');
        sbuf.append(" AuthAlgorithms:");
        for (int a = 0; a < this.allowedAuthAlgorithms.size(); a++) {
            if (this.allowedAuthAlgorithms.get(a)) {
                sbuf.append(" ");
                if (a < AuthAlgorithm.strings.length) {
                    sbuf.append(AuthAlgorithm.strings[a]);
                } else {
                    sbuf.append("??");
                }
            }
        }
        sbuf.append('\n');
        sbuf.append(" PairwiseCiphers:");
        for (int pc = 0; pc < this.allowedPairwiseCiphers.size(); pc++) {
            if (this.allowedPairwiseCiphers.get(pc)) {
                sbuf.append(" ");
                if (pc < PairwiseCipher.strings.length) {
                    sbuf.append(PairwiseCipher.strings[pc]);
                } else {
                    sbuf.append("??");
                }
            }
        }
        sbuf.append('\n');
        sbuf.append(" GroupCiphers:");
        for (int gc = 0; gc < this.allowedGroupCiphers.size(); gc++) {
            if (this.allowedGroupCiphers.get(gc)) {
                sbuf.append(" ");
                if (gc < GroupCipher.strings.length) {
                    sbuf.append(GroupCipher.strings[gc]);
                } else {
                    sbuf.append("??");
                }
            }
        }
        sbuf.append('\n').append(" PSK: ");
        if (this.preSharedKey != null) {
            sbuf.append('*');
        }
        for (EnterpriseField field : enterpriseFields) {
            sbuf.append('\n').append(" " + field.varName() + ": ");
            String value = field.value();
            if (value != null) sbuf.append(value);
        }
        sbuf.append('\n');
        return sbuf.toString();
    }
    private static BitSet readBitSet(Parcel src) {
        int cardinality = src.readInt();
        BitSet set = new BitSet();
        for (int i = 0; i < cardinality; i++)
            set.set(src.readInt());
        return set;
    }
    private static void writeBitSet(Parcel dest, BitSet set) {
        int nextSetBit = -1;
        dest.writeInt(set.cardinality());
        while ((nextSetBit = set.nextSetBit(nextSetBit + 1)) != -1)
            dest.writeInt(nextSetBit);
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(networkId);
        dest.writeInt(status);
        dest.writeString(SSID);
        dest.writeString(BSSID);
        dest.writeString(preSharedKey);
        for (String wepKey : wepKeys)
            dest.writeString(wepKey);
        dest.writeInt(wepTxKeyIndex);
        dest.writeInt(priority);
        dest.writeInt(hiddenSSID ? 1 : 0);
        writeBitSet(dest, allowedKeyManagement);
        writeBitSet(dest, allowedProtocols);
        writeBitSet(dest, allowedAuthAlgorithms);
        writeBitSet(dest, allowedPairwiseCiphers);
        writeBitSet(dest, allowedGroupCiphers);
        for (EnterpriseField field : enterpriseFields) {
            dest.writeString(field.value());
        }
    }
    public static final Creator<WifiConfiguration> CREATOR =
        new Creator<WifiConfiguration>() {
            public WifiConfiguration createFromParcel(Parcel in) {
                WifiConfiguration config = new WifiConfiguration();
                config.networkId = in.readInt();
                config.status = in.readInt();
                config.SSID = in.readString();
                config.BSSID = in.readString();
                config.preSharedKey = in.readString();
                for (int i = 0; i < config.wepKeys.length; i++)
                    config.wepKeys[i] = in.readString();
                config.wepTxKeyIndex = in.readInt();
                config.priority = in.readInt();
                config.hiddenSSID = in.readInt() != 0;
                config.allowedKeyManagement   = readBitSet(in);
                config.allowedProtocols       = readBitSet(in);
                config.allowedAuthAlgorithms  = readBitSet(in);
                config.allowedPairwiseCiphers = readBitSet(in);
                config.allowedGroupCiphers    = readBitSet(in);
                for (EnterpriseField field : config.enterpriseFields) {
                    field.setValue(in.readString());
                }
                return config;
            }
            public WifiConfiguration[] newArray(int size) {
                return new WifiConfiguration[size];
            }
        };
}
