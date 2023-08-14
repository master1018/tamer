public class XmlPullParserFactory {
    final static Class referenceContextClass;
    static {
        XmlPullParserFactory f = new XmlPullParserFactory();
        referenceContextClass = f.getClass();
    }
    public static final String PROPERTY_NAME =
        "org.xmlpull.v1.XmlPullParserFactory";
    private static final String RESOURCE_NAME =
        "/META-INF/services/" + PROPERTY_NAME;
    protected ArrayList parserClasses;
    protected String classNamesLocation;
    protected ArrayList serializerClasses;
    protected HashMap features = new HashMap();
    protected XmlPullParserFactory() {
    }
    public void setFeature(String name,
                           boolean state) throws XmlPullParserException {
        features.put(name, new Boolean(state));
    }
    public boolean getFeature (String name) {
        Boolean value = (Boolean) features.get(name);
        return value != null ? value.booleanValue() : false;
    }
    public void setNamespaceAware(boolean awareness) {
        features.put (XmlPullParser.FEATURE_PROCESS_NAMESPACES, new Boolean (awareness));
    }
    public boolean isNamespaceAware() {
        return getFeature (XmlPullParser.FEATURE_PROCESS_NAMESPACES);
    }
    public void setValidating(boolean validating) {
        features.put (XmlPullParser.FEATURE_VALIDATION, new Boolean (validating));
    }
    public boolean isValidating() {
        return getFeature (XmlPullParser.FEATURE_VALIDATION);
    }
    public XmlPullParser newPullParser() throws XmlPullParserException {
        if (parserClasses == null) throw new XmlPullParserException
                ("Factory initialization was incomplete - has not tried "+classNamesLocation);
        if (parserClasses.size() == 0) throw new XmlPullParserException
                ("No valid parser classes found in "+classNamesLocation);
        final StringBuffer issues = new StringBuffer ();
        for (int i = 0; i < parserClasses.size(); i++) {
            final Class ppClass = (Class) parserClasses.get(i);
            try {
                final XmlPullParser pp = (XmlPullParser) ppClass.newInstance();
                for (Iterator iter = features.keySet().iterator(); iter.hasNext(); ) {
                    final String key = (String) iter.next();
                    final Boolean value = (Boolean) features.get(key);
                    if(value != null && value.booleanValue()) {
                        pp.setFeature(key, true);
                    }
                }
                return pp;
            } catch(Exception ex) {
                issues.append (ppClass.getName () + ": "+ ex.toString ()+"; ");
            }
        }
        throw new XmlPullParserException ("could not create parser: "+issues);
    }
    public XmlSerializer newSerializer() throws XmlPullParserException {
        if (serializerClasses == null) {
            throw new XmlPullParserException
                ("Factory initialization incomplete - has not tried "+classNamesLocation);
        }
        if(serializerClasses.size() == 0) {
            throw new XmlPullParserException
                ("No valid serializer classes found in "+classNamesLocation);
        }
        final StringBuffer issues = new StringBuffer ();
        for (int i = 0; i < serializerClasses.size (); i++) {
            final Class ppClass = (Class) serializerClasses.get(i);
            try {
                final XmlSerializer ser = (XmlSerializer) ppClass.newInstance();
                return ser;
            } catch(Exception ex) {
                issues.append (ppClass.getName () + ": "+ ex.toString ()+"; ");
            }
        }
        throw new XmlPullParserException ("could not create serializer: "+issues);
    }
    public static XmlPullParserFactory newInstance () throws XmlPullParserException {
        return newInstance(null, null);
    }
    public static XmlPullParserFactory newInstance (String classNames, Class context)
        throws XmlPullParserException {
        classNames = "org.kxml2.io.KXmlParser,org.kxml2.io.KXmlSerializer";
        XmlPullParserFactory factory = null;
        final ArrayList parserClasses = new ArrayList();
        final ArrayList serializerClasses = new ArrayList();
        int pos = 0;
        while (pos < classNames.length ()) {
            int cut = classNames.indexOf (',', pos);
            if (cut == -1) cut = classNames.length ();
            final String name = classNames.substring (pos, cut);
            Class candidate = null;
            Object instance = null;
            try {
                candidate = Class.forName (name);
                instance = candidate.newInstance ();
            }
            catch (Exception e) {}
            if (candidate != null) {
                boolean recognized = false;
                if (instance instanceof XmlPullParser) {
                    parserClasses.add(candidate);
                    recognized = true;
                }
                if (instance instanceof XmlSerializer) {
                    serializerClasses.add(candidate);
                    recognized = true;
                }
                if (instance instanceof XmlPullParserFactory) {
                    if (factory == null) {
                        factory = (XmlPullParserFactory) instance;
                    }
                    recognized = true;
                }
                if (!recognized) {
                    throw new XmlPullParserException ("incompatible class: "+name);
                }
            }
            pos = cut + 1;
        }
        if (factory == null) {
            factory = new XmlPullParserFactory ();
        }
        factory.parserClasses = parserClasses;
        factory.serializerClasses = serializerClasses;
        factory.classNamesLocation = "org.kxml2.io.kXmlParser,org.kxml2.io.KXmlSerializer";
        return factory;
    }
}
