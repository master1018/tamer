class ServiceLocator {
    private static final String SRV_RR = "SRV";
    private static final String[] SRV_RR_ATTR = new String[]{SRV_RR};
    private static final Random random = new Random();
    private ServiceLocator() {
    }
    static String mapDnToDomainName(String dn) throws InvalidNameException {
        if (dn == null) {
            return null;
        }
        StringBuffer domain = new StringBuffer();
        LdapName ldapName = new LdapName(dn);
        List rdnList = ldapName.getRdns();
        for (int i = rdnList.size() - 1; i >= 0; i--) {
            Rdn rdn = (Rdn) rdnList.get(i);
            if ((rdn.size() == 1) &&
                ("dc".equalsIgnoreCase(rdn.getType()) )) {
                Object attrval = rdn.getValue();
                if (attrval instanceof String) {
                    if (attrval.equals(".") ||
                        (domain.length() == 1 && domain.charAt(0) == '.')) {
                        domain.setLength(0); 
                    }
                    if (domain.length() > 0) {
                        domain.append('.');
                    }
                    domain.append(attrval);
                } else {
                    domain.setLength(0); 
                }
            } else {
                domain.setLength(0); 
            }
        }
        return (domain.length() != 0) ? domain.toString() : null;
    }
    static String[] getLdapService(String domainName, Hashtable environment) {
        if (domainName == null || domainName.length() == 0) {
            return null;
        }
        String dnsUrl = "dns:
        String[] hostports = null;
        try {
            Context ctx = NamingManager.getURLContext("dns", environment);
            if (!(ctx instanceof DirContext)) {
                return null; 
            }
            Attributes attrs =
                ((DirContext)ctx).getAttributes(dnsUrl, SRV_RR_ATTR);
            Attribute attr;
            if (attrs != null && ((attr = attrs.get(SRV_RR)) != null)) {
                int numValues = attr.size();
                int numRecords = 0;
                SrvRecord[] srvRecords = new SrvRecord[numValues];
                int i = 0;
                int j = 0;
                while (i < numValues) {
                    try {
                        srvRecords[j] = new SrvRecord((String) attr.get(i));
                        j++;
                    } catch (Exception e) {
                    }
                    i++;
                }
                numRecords = j;
                if (numRecords < numValues) {
                    SrvRecord[] trimmed = new SrvRecord[numRecords];
                    System.arraycopy(srvRecords, 0, trimmed, 0, numRecords);
                    srvRecords = trimmed;
                }
                if (numRecords > 1) {
                    Arrays.sort(srvRecords);
                }
                hostports = extractHostports(srvRecords);
            }
        } catch (NamingException e) {
        }
        return hostports;
    }
    private static String[] extractHostports(SrvRecord[] srvRecords) {
        String[] hostports = null;
        int head = 0;
        int tail = 0;
        int sublistLength = 0;
        int k = 0;
        for (int i = 0; i < srvRecords.length; i++) {
            if (hostports == null) {
                hostports = new String[srvRecords.length];
            }
            head = i;
            while (i < srvRecords.length - 1 &&
                srvRecords[i].priority == srvRecords[i + 1].priority) {
                i++;
            }
            tail = i;
            sublistLength = (tail - head) + 1;
            for (int j = 0; j < sublistLength; j++) {
                hostports[k++] = selectHostport(srvRecords, head, tail);
            }
        }
        return hostports;
    }
    private static String selectHostport(SrvRecord[] srvRecords, int head,
            int tail) {
        if (head == tail) {
            return srvRecords[head].hostport;
        }
        int sum = 0;
        for (int i = head; i <= tail; i++) {
            if (srvRecords[i] != null) {
                sum += srvRecords[i].weight;
                srvRecords[i].sum = sum;
            }
        }
        String hostport = null;
        int target = (sum == 0 ? 0 : random.nextInt(sum + 1));
        for (int i = head; i <= tail; i++) {
            if (srvRecords[i] != null && srvRecords[i].sum >= target) {
                hostport = srvRecords[i].hostport;
                srvRecords[i] = null; 
                break;
            }
        }
        return hostport;
    }
static class SrvRecord implements Comparable {
    int priority;
    int weight;
    int sum;
    String hostport;
    SrvRecord(String srvRecord) throws Exception {
        StringTokenizer tokenizer = new StringTokenizer(srvRecord, " ");
        String port;
        if (tokenizer.countTokens() == 4) {
            priority = Integer.parseInt(tokenizer.nextToken());
            weight = Integer.parseInt(tokenizer.nextToken());
            port = tokenizer.nextToken();
            hostport = tokenizer.nextToken() + ":" + port;
        } else {
            throw new IllegalArgumentException();
        }
    }
    public int compareTo(Object o) {
        SrvRecord that = (SrvRecord) o;
        if (priority > that.priority) {
            return 1; 
        } else if (priority < that.priority) {
            return -1; 
        } else if (weight == 0 && that.weight != 0) {
            return -1; 
        } else if (weight != 0 && that.weight == 0) {
            return 1; 
        } else {
            return 0; 
        }
    }
}
}
