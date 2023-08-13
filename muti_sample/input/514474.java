public class X509Name
    extends ASN1Encodable
{
    public static final DERObjectIdentifier C = new DERObjectIdentifier("2.5.4.6");
    public static final DERObjectIdentifier O = new DERObjectIdentifier("2.5.4.10");
    public static final DERObjectIdentifier OU = new DERObjectIdentifier("2.5.4.11");
    public static final DERObjectIdentifier T = new DERObjectIdentifier("2.5.4.12");
    public static final DERObjectIdentifier CN = new DERObjectIdentifier("2.5.4.3");
    public static final DERObjectIdentifier SN = new DERObjectIdentifier("2.5.4.5");
    public static final DERObjectIdentifier STREET = new DERObjectIdentifier("2.5.4.9");
    public static final DERObjectIdentifier SERIALNUMBER = SN;
    public static final DERObjectIdentifier L = new DERObjectIdentifier("2.5.4.7");
    public static final DERObjectIdentifier ST = new DERObjectIdentifier("2.5.4.8");
    public static final DERObjectIdentifier SURNAME = new DERObjectIdentifier("2.5.4.4");
    public static final DERObjectIdentifier GIVENNAME = new DERObjectIdentifier("2.5.4.42");
    public static final DERObjectIdentifier INITIALS = new DERObjectIdentifier("2.5.4.43");
    public static final DERObjectIdentifier GENERATION = new DERObjectIdentifier("2.5.4.44");
    public static final DERObjectIdentifier UNIQUE_IDENTIFIER = new DERObjectIdentifier("2.5.4.45");
    public static final DERObjectIdentifier BUSINESS_CATEGORY = new DERObjectIdentifier(
                    "2.5.4.15");
    public static final DERObjectIdentifier POSTAL_CODE = new DERObjectIdentifier(
                    "2.5.4.17");
    public static final DERObjectIdentifier DN_QUALIFIER = new DERObjectIdentifier(
                    "2.5.4.46");
    public static final DERObjectIdentifier PSEUDONYM = new DERObjectIdentifier(
                    "2.5.4.65");
    public static final DERObjectIdentifier DATE_OF_BIRTH = new DERObjectIdentifier(
                    "1.3.6.1.5.5.7.9.1");
    public static final DERObjectIdentifier PLACE_OF_BIRTH = new DERObjectIdentifier(
                    "1.3.6.1.5.5.7.9.2");
    public static final DERObjectIdentifier GENDER = new DERObjectIdentifier(
                    "1.3.6.1.5.5.7.9.3");
    public static final DERObjectIdentifier COUNTRY_OF_CITIZENSHIP = new DERObjectIdentifier(
                    "1.3.6.1.5.5.7.9.4");
    public static final DERObjectIdentifier COUNTRY_OF_RESIDENCE = new DERObjectIdentifier(
                    "1.3.6.1.5.5.7.9.5");
    public static final DERObjectIdentifier NAME_AT_BIRTH =  new DERObjectIdentifier("1.3.36.8.3.14");
    public static final DERObjectIdentifier POSTAL_ADDRESS = new DERObjectIdentifier(
                    "2.5.4.16");
    public static final DERObjectIdentifier EmailAddress = PKCSObjectIdentifiers.pkcs_9_at_emailAddress;
    public static final DERObjectIdentifier UnstructuredName = PKCSObjectIdentifiers.pkcs_9_at_unstructuredName;
    public static final DERObjectIdentifier UnstructuredAddress = PKCSObjectIdentifiers.pkcs_9_at_unstructuredAddress;
    public static final DERObjectIdentifier E = EmailAddress;
    public static final DERObjectIdentifier DC = new DERObjectIdentifier("0.9.2342.19200300.100.1.25");
    public static final DERObjectIdentifier UID = new DERObjectIdentifier("0.9.2342.19200300.100.1.1");
    public static Hashtable OIDLookUp = new Hashtable();
    public static boolean DefaultReverse = false;
    public static Hashtable DefaultSymbols = OIDLookUp;
    public static Hashtable RFC2253Symbols = new Hashtable();
    public static Hashtable RFC1779Symbols = new Hashtable();
    public static Hashtable SymbolLookUp = new Hashtable();
    public static Hashtable DefaultLookUp = SymbolLookUp;
    static
    {
        DefaultSymbols.put(C, "C");
        DefaultSymbols.put(O, "O");
        DefaultSymbols.put(T, "T");
        DefaultSymbols.put(OU, "OU");
        DefaultSymbols.put(CN, "CN");
        DefaultSymbols.put(L, "L");
        DefaultSymbols.put(ST, "ST");
        DefaultSymbols.put(SN, "SN");
        DefaultSymbols.put(EmailAddress, "E");
        DefaultSymbols.put(DC, "DC");
        DefaultSymbols.put(UID, "UID");
        DefaultSymbols.put(STREET, "STREET");
        DefaultSymbols.put(SURNAME, "SURNAME");
        DefaultSymbols.put(GIVENNAME, "GIVENNAME");
        DefaultSymbols.put(INITIALS, "INITIALS");
        DefaultSymbols.put(GENERATION, "GENERATION");
        DefaultSymbols.put(UnstructuredAddress, "unstructuredAddress");
        DefaultSymbols.put(UnstructuredName, "unstructuredName");
        DefaultSymbols.put(UNIQUE_IDENTIFIER, "UniqueIdentifier");
        DefaultSymbols.put(DN_QUALIFIER, "DN");
        DefaultSymbols.put(PSEUDONYM, "Pseudonym");
        DefaultSymbols.put(POSTAL_ADDRESS, "PostalAddress");
        DefaultSymbols.put(NAME_AT_BIRTH, "NameAtBirth");
        DefaultSymbols.put(COUNTRY_OF_CITIZENSHIP, "CountryOfCitizenship");
        DefaultSymbols.put(COUNTRY_OF_RESIDENCE, "CountryOfResidence");
        DefaultSymbols.put(GENDER, "Gender");
        DefaultSymbols.put(PLACE_OF_BIRTH, "PlaceOfBirth");
        DefaultSymbols.put(DATE_OF_BIRTH, "DateOfBirth");
        DefaultSymbols.put(POSTAL_CODE, "PostalCode");
        DefaultSymbols.put(BUSINESS_CATEGORY, "BusinessCategory");
        RFC2253Symbols.put(C, "C");
        RFC2253Symbols.put(O, "O");
        RFC2253Symbols.put(OU, "OU");
        RFC2253Symbols.put(CN, "CN");
        RFC2253Symbols.put(L, "L");
        RFC2253Symbols.put(ST, "ST");
        RFC2253Symbols.put(STREET, "STREET");
        RFC2253Symbols.put(DC, "DC");
        RFC2253Symbols.put(UID, "UID");
        RFC1779Symbols.put(C, "C");
        RFC1779Symbols.put(O, "O");
        RFC1779Symbols.put(OU, "OU");
        RFC1779Symbols.put(CN, "CN");
        RFC1779Symbols.put(L, "L");
        RFC1779Symbols.put(ST, "ST");
        RFC1779Symbols.put(STREET, "STREET");
        DefaultLookUp.put("c", C);
        DefaultLookUp.put("o", O);
        DefaultLookUp.put("t", T);
        DefaultLookUp.put("ou", OU);
        DefaultLookUp.put("cn", CN);
        DefaultLookUp.put("l", L);
        DefaultLookUp.put("st", ST);
        DefaultLookUp.put("sn", SN);
        DefaultLookUp.put("serialnumber", SN);
        DefaultLookUp.put("street", STREET);
        DefaultLookUp.put("emailaddress", E);
        DefaultLookUp.put("dc", DC);
        DefaultLookUp.put("e", E);
        DefaultLookUp.put("uid", UID);
        DefaultLookUp.put("surname", SURNAME);
        DefaultLookUp.put("givenname", GIVENNAME);
        DefaultLookUp.put("initials", INITIALS);
        DefaultLookUp.put("generation", GENERATION);
        DefaultLookUp.put("unstructuredaddress", UnstructuredAddress);
        DefaultLookUp.put("unstructuredname", UnstructuredName);
        DefaultLookUp.put("uniqueidentifier", UNIQUE_IDENTIFIER);
        DefaultLookUp.put("dn", DN_QUALIFIER);
        DefaultLookUp.put("pseudonym", PSEUDONYM);
        DefaultLookUp.put("postaladdress", POSTAL_ADDRESS);
        DefaultLookUp.put("nameofbirth", NAME_AT_BIRTH);
        DefaultLookUp.put("countryofcitizenship", COUNTRY_OF_CITIZENSHIP);
        DefaultLookUp.put("countryofresidence", COUNTRY_OF_RESIDENCE);
        DefaultLookUp.put("gender", GENDER);
        DefaultLookUp.put("placeofbirth", PLACE_OF_BIRTH);
        DefaultLookUp.put("dateofbirth", DATE_OF_BIRTH);
        DefaultLookUp.put("postalcode", POSTAL_CODE);
        DefaultLookUp.put("businesscategory", BUSINESS_CATEGORY);
    }
    private X509NameEntryConverter  converter = null;
    private X509NameElementList     elems = new X509NameElementList();
    private ASN1Sequence            seq;
    public static X509Name getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }
    public static X509Name getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof X509Name)
        {
            return (X509Name)obj;
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new X509Name((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("unknown object in factory \"" + obj.getClass().getName()+"\"");
    }
    public X509Name(
        ASN1Sequence  seq)
    {
        this.seq = seq;
        Enumeration e = seq.getObjects();
        while (e.hasMoreElements())
        {
            ASN1Set         set = (ASN1Set)e.nextElement();
            for (int i = 0; i < set.size(); i++) 
            {
                   ASN1Sequence s = (ASN1Sequence)set.getObjectAt(i);
                   DERObjectIdentifier key =
                       (DERObjectIdentifier) s.getObjectAt(0);
                   DEREncodable value = s.getObjectAt(1);
                   String valueStr;
                   if (value instanceof DERString)
                   {
                       valueStr = ((DERString)value).getString();
                   }
                   else
                   {
                       valueStr = "#" + bytesToString(Hex.encode(value.getDERObject().getDEREncoded()));
                   }
                   elems.add(key, valueStr, i != 0);
            }
        }
    }
    public X509Name(
        Hashtable  attributes)
    {
        this(null, attributes);
    }
    public X509Name(
        Vector      ordering,
        Hashtable   attributes)
    {
        this(ordering, attributes, new X509DefaultEntryConverter());
    }
    public X509Name(
        Vector                      ordering,
        Hashtable                   attributes,
        X509DefaultEntryConverter   converter)
    {
        DERObjectIdentifier problem = null;
        this.converter = converter;
        if (ordering != null)
        {
            for (int i = 0; i != ordering.size(); i++)
            {
                DERObjectIdentifier key =
                    (DERObjectIdentifier) ordering.elementAt(i);
                String value = (String) attributes.get(key);
                if (value == null)
                {
                    problem = key;
                    break;
                }
                elems.add(key, value);
            }
        }
        else
        {
            Enumeration     e = attributes.keys();
            while (e.hasMoreElements())
            {
                DERObjectIdentifier key =
                    (DERObjectIdentifier) e.nextElement();
                String value = (String) attributes.get(key);
                if (value == null)
                {
                    problem = key;
                    break;
                }
                elems.add(key, value);
            }
        }
        if (problem != null)
        {
            throw new IllegalArgumentException("No attribute for object id - " + problem.getId() + " - passed to distinguished name");
        }
    }
    public X509Name(
        Vector  oids,
        Vector  values)
    {
        this(oids, values, new X509DefaultEntryConverter());
    }
    public X509Name(
        Vector                  oids,
        Vector                  values,
        X509NameEntryConverter  converter)
    {
        this.converter = converter;
        if (oids.size() != values.size())
        {
            throw new IllegalArgumentException("oids vector must be same length as values.");
        }
        for (int i = 0; i < oids.size(); i++)
        {
            elems.add((DERObjectIdentifier) oids.elementAt(i),
                    (String) values.elementAt(i));
        }
    }
    public X509Name(
        String  dirName)
    {
        this(DefaultReverse, DefaultLookUp, dirName);
    }
    public X509Name(
        String                  dirName,
        X509NameEntryConverter  converter)
    {
        this(DefaultReverse, DefaultLookUp, dirName, converter);
    }
    public X509Name(
        boolean reverse,
        String  dirName)
    {
        this(reverse, DefaultLookUp, dirName);
    }
    public X509Name(
        boolean                 reverse,
        String                  dirName,
        X509NameEntryConverter  converter)
    {
        this(reverse, DefaultLookUp, dirName, converter);
    }
    public X509Name(
        boolean     reverse,
        Hashtable   lookUp,
        String      dirName)
    {
        this(reverse, lookUp, dirName, new X509DefaultEntryConverter());
    }
    private DERObjectIdentifier decodeOID(
        String      name,
        Hashtable   lookUp)
    {
        if (Strings.toUpperCase(name).startsWith("OID."))
        {
            return new DERObjectIdentifier(name.substring(4));
        }
        else if (name.charAt(0) >= '0' && name.charAt(0) <= '9')
        {
            return new DERObjectIdentifier(name);
        }
        DERObjectIdentifier oid = (DERObjectIdentifier)lookUp.get(Strings.toLowerCase(name));
        if (oid == null)
        {
            throw new IllegalArgumentException("Unknown object id - " + name + " - passed to distinguished name");
        }
        return oid;
    }
    public X509Name(
        boolean                 reverse,
        Hashtable               lookUp,
        String                  dirName,
        X509NameEntryConverter  converter)
    {
        this.converter = converter;
        X509NameTokenizer   nTok = new X509NameTokenizer(dirName);
        while (nTok.hasMoreTokens())
        {
            String  token = nTok.nextToken();
            int     index = token.indexOf('=');
            if (index == -1)
            {
                throw new IllegalArgumentException("badly formatted directory string");
            }
            String              name = token.substring(0, index);
            String              value = token.substring(index + 1);
            DERObjectIdentifier oid = decodeOID(name, lookUp);
            if (value.indexOf('+') > 0)
            {
                X509NameTokenizer   vTok = new X509NameTokenizer(value, '+');
                elems.add(oid, vTok.nextToken());
                while (vTok.hasMoreTokens())
                {
                    String  sv = vTok.nextToken();
                    int     ndx = sv.indexOf('=');
                    String  nm = sv.substring(0, ndx);
                    String  vl = sv.substring(ndx + 1);
                    elems.add(decodeOID(nm, lookUp), vl, true);
                }
            }
            else
            {
                elems.add(oid, value);
            }
        }
        if (reverse)
        {
            elems = elems.reverse();
        }
    }
    public Vector getOIDs()
    {
        Vector  v = new Vector();
        int     size = elems.size();
        for (int i = 0; i < size; i++)
        {
            v.addElement(elems.getKey(i));
        }
        return v;
    }
    public Vector getValues()
    {
        Vector  v = new Vector();
        int     size = elems.size();
        for (int i = 0; i < size; i++)
        {
            v.addElement(elems.getValue(i));
        }
        return v;
    }
    public DERObject toASN1Object()
    {
        if (seq == null)
        {
            ASN1EncodableVector  vec = new ASN1EncodableVector();
            ASN1EncodableVector  sVec = new ASN1EncodableVector();
            DERObjectIdentifier  lstOid = null;
            int                  size = elems.size();
            for (int i = 0; i != size; i++)
            {
                ASN1EncodableVector     v = new ASN1EncodableVector();
                DERObjectIdentifier     oid = elems.getKey(i);
                v.add(oid);
                String  str = elems.getValue(i);
                v.add(converter.getConvertedValue(oid, str));
                if (lstOid == null || elems.getAdded(i))
                {
                    sVec.add(new DERSequence(v));
                }
                else
                {
                    vec.add(new DERSet(sVec));
                    sVec = new ASN1EncodableVector();
                    sVec.add(new DERSequence(v));
                }
                lstOid = oid;
            }
            vec.add(new DERSet(sVec));
            seq = new DERSequence(vec);
        }
        return seq;
    }
    public boolean equals(Object _obj, boolean inOrder) 
    {
        if (_obj == this)
        {
            return true;
        }
        if (!inOrder)
        {
            return this.equals(_obj);
        }
        if (!(_obj instanceof X509Name))
        {
            return false;
        }
        X509Name _oxn          = (X509Name)_obj;
        int      _orderingSize = elems.size();
        if (_orderingSize != _oxn.elems.size()) 
        {
            return false;
        }
        for(int i = 0; i < _orderingSize; i++) 
        {
            String  _oid   = elems.getKey(i).getId();
            String  _val   = elems.getValue(i);
            String _oOID = _oxn.elems.getKey(i).getId();
            String _oVal = _oxn.elems.getValue(i);
            if (_oid.equals(_oOID))
            {
                _val = Strings.toLowerCase(_val.trim());
                _oVal = Strings.toLowerCase(_oVal.trim());
                if (_val.equals(_oVal))
                {
                    continue;
                }
                else
                {
                    StringBuffer    v1 = new StringBuffer();
                    StringBuffer    v2 = new StringBuffer();
                    if (_val.length() != 0)
                    {
                        char    c1 = _val.charAt(0);
                        v1.append(c1);
                        for (int k = 1; k < _val.length(); k++)
                        {
                            char    c2 = _val.charAt(k);
                            if (!(c1 == ' ' && c2 == ' '))
                            {
                                v1.append(c2);
                            }
                            c1 = c2;
                        }
                    }
                    if (_oVal.length() != 0)
                    {
                        char    c1 = _oVal.charAt(0);
                        v2.append(c1);
                        for (int k = 1; k < _oVal.length(); k++)
                        {
                            char    c2 = _oVal.charAt(k);
                            if (!(c1 == ' ' && c2 == ' '))
                            {
                                v2.append(c2);
                            }
                            c1 = c2;
                        }
                    }
                    if (!v1.toString().equals(v2.toString()))
                    {
                        return false;
                    }
                }
            }
            else
            {
                return false;
            }
        }
        return true;
    }
    public boolean equals(Object _obj) 
    {
        if (_obj == this)
        {
            return true;
        }
        if (!(_obj instanceof X509Name || _obj instanceof ASN1Sequence))
        {
            return false;
        }
        DERObject derO = ((DEREncodable)_obj).getDERObject();
        if (this.getDERObject().equals(derO))
        {
            return true;
        }
        if (!(_obj instanceof X509Name))
        {
            return false;
        }
        X509Name _oxn          = (X509Name)_obj;
        int      _orderingSize = elems.size();
        if (_orderingSize != _oxn.elems.size()) 
        {
            return false;
        }
        boolean[] _indexes = new boolean[_orderingSize];
        for(int i = 0; i < _orderingSize; i++) 
        {
            boolean _found = false;
            String  _oid   = elems.getKey(i).getId();
            String  _val   = elems.getValue(i);
            for(int j = 0; j < _orderingSize; j++) 
            {
                if (_indexes[j])
                {
                    continue;
                }
                String _oOID = elems.getKey(j).getId();
                String _oVal = _oxn.elems.getValue(j);
                if (_oid.equals(_oOID))
                {
                    _val = Strings.toLowerCase(_val.trim());
                    _oVal = Strings.toLowerCase(_oVal.trim());
                    if (_val.equals(_oVal))
                    {
                        _indexes[j] = true;
                        _found      = true;
                        break;
                    }
                    else
                    {
                        StringBuffer    v1 = new StringBuffer();
                        StringBuffer    v2 = new StringBuffer();
                        if (_val.length() != 0)
                        {
                            char    c1 = _val.charAt(0);
                            v1.append(c1);
                            for (int k = 1; k < _val.length(); k++)
                            {
                                char    c2 = _val.charAt(k);
                                if (!(c1 == ' ' && c2 == ' '))
                                {
                                    v1.append(c2);
                                }
                                c1 = c2;
                            }
                        }
                        if (_oVal.length() != 0)
                        {
                            char    c1 = _oVal.charAt(0);
                            v2.append(c1);
                            for (int k = 1; k < _oVal.length(); k++)
                            {
                                char    c2 = _oVal.charAt(k);
                                if (!(c1 == ' ' && c2 == ' '))
                                {
                                    v2.append(c2);
                                }
                                c1 = c2;
                            }
                        }
                        if (v1.toString().equals(v2.toString()))
                        {
                            _indexes[j] = true;
                            _found      = true;
                            break;
                        }
                    }
                }
            }
            if(!_found)
            {
                return false;
            }
        }
        return true;
    }
    public int hashCode()
    {
        ASN1Sequence  seq = (ASN1Sequence)this.getDERObject();
        Enumeration   e = seq.getObjects();
        int           hashCode = 0;
        while (e.hasMoreElements())
        {
            hashCode ^= e.nextElement().hashCode();
        }
        return hashCode;
    }
    private void appendValue(
        StringBuffer        buf,
        Hashtable           oidSymbols,
        DERObjectIdentifier oid,
        String              value)
    {
        String  sym = (String)oidSymbols.get(oid);
        if (sym != null)
        {
            buf.append(sym);
        }
        else
        {
            buf.append(oid.getId());
        }
        buf.append('=');
        int     index = buf.length();
        buf.append(value);
        int     end = buf.length();
        while (index != end)
        {
            if ((buf.charAt(index) == ',')
               || (buf.charAt(index) == '"')
               || (buf.charAt(index) == '\\')
               || (buf.charAt(index) == '+')
               || (buf.charAt(index) == '<')
               || (buf.charAt(index) == '>')
               || (buf.charAt(index) == ';'))
            {
                buf.insert(index, "\\");
                index++;
                end++;
            }
            index++;
        }
    }
    public String toString(
        boolean     reverse,
        Hashtable   oidSymbols)
    {
        StringBuffer            buf = new StringBuffer();
        boolean                 first = true;
        if (reverse)
        {
            for (int i = elems.size() - 1; i >= 0; i--)
            {
                if (first)
                {
                    first = false;
                }
                else
                {
                    if (elems.getAdded(i + 1))
                    {
                        buf.append('+');
                    }
                    else
                    {
                        buf.append(',');
                    }
                }
                appendValue(buf, oidSymbols, 
                            elems.getKey(i),
                            elems.getValue(i));
            }
        }
        else
        {
            for (int i = 0; i < elems.size(); i++)
            {
                if (first)
                {
                    first = false;
                }
                else
                {
                    if (elems.getAdded(i))
                    {
                        buf.append('+');
                    }
                    else
                    {
                        buf.append(',');
                    }
                }
                appendValue(buf, oidSymbols, 
                            elems.getKey(i),
                            elems.getValue(i));
            }
        }
        return buf.toString();
    }
    private String bytesToString(
        byte[] data)
    {
        char[]  cs = new char[data.length];
        for (int i = 0; i != cs.length; i++)
        {
            cs[i] = (char)(data[i] & 0xff);
        }
        return new String(cs);
    }
    public String toString()
    {
        return toString(DefaultReverse, DefaultSymbols);
    }
}
