public class ElementType {
	private String theName;		
	private String theNamespace;	
	private String theLocalName;	
	private int theModel;		
	private int theMemberOf;	
	private int theFlags;		
	private AttributesImpl theAtts;	
	private ElementType theParent;	
	private Schema theSchema;	
	public ElementType(String name, int model, int memberOf, int flags, Schema schema) {
		theName = name;
		theModel = model;
		theMemberOf = memberOf;
		theFlags = flags;
		theAtts = new AttributesImpl();
		theSchema = schema;
		theNamespace = namespace(name, false);
		theLocalName = localName(name);
		}
	public String namespace(String name, boolean attribute) {
		int colon = name.indexOf(':');
		if (colon == -1) {
			return attribute ? "" : theSchema.getURI();
			}
		String prefix = name.substring(0, colon);
		if (prefix.equals("xml")) {
			return "http:
			}
		else {
			return ("urn:x-prefix:" + prefix).intern();
			}
		}
	public String localName(String name) {
		int colon = name.indexOf(':');
		if (colon == -1) {
			return name;
			}
		else {
			return name.substring(colon+1).intern();
			}
		}
	public String name() { return theName; }
	public String namespace() { return theNamespace; }
	public String localName() { return theLocalName; }
	public int model() { return theModel; }
	public int memberOf() { return theMemberOf; }
	public int flags() { return theFlags; }
	public AttributesImpl atts() {return theAtts;}
	public ElementType parent() {return theParent;}
	public Schema schema() {return theSchema;}
	public boolean canContain(ElementType other) {
		return (theModel & other.theMemberOf) != 0;
		}
	public void setAttribute(AttributesImpl atts, String name, String type, String value) {
		if (name.equals("xmlns") || name.startsWith("xmlns:")) {
			return;
			}
;
		String namespace = namespace(name, true);
		String localName = localName(name);
		int i = atts.getIndex(name);
		if (i == -1) {
			name = name.intern();
			if (type == null) type = "CDATA";
			if (!type.equals("CDATA")) value = normalize(value);
			atts.addAttribute(namespace, localName, name, type, value);
			}
		else {
			if (type == null) type = atts.getType(i);
			if (!type.equals("CDATA")) value=normalize(value);
			atts.setAttribute(i, namespace, localName, name, type, value);
			}
		}
	public static String normalize(String value) {
		if (value == null) return value;
		value = value.trim();
		if (value.indexOf("  ") == -1) return value;
		boolean space = false;
		int len = value.length();
		StringBuffer b = new StringBuffer(len);
		for (int i = 0; i < len; i++) {
			char v = value.charAt(i);
			if (v == ' ') {
				if (!space) b.append(v);
				space = true;
				}
			else {
				b.append(v);
				space = false;
				}
			}
		return b.toString();
		}
	public void setAttribute(String name, String type, String value) {
		setAttribute(theAtts, name, type, value);
		}
	public void setModel(int model) { theModel = model; }
	public void setMemberOf(int memberOf) { theMemberOf = memberOf; }
	public void setFlags(int flags) { theFlags = flags; }
	public void setParent(ElementType parent) { theParent = parent; }
	}
