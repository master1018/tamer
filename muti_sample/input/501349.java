abstract class GenericInflater<T, P extends GenericInflater.Parent> {
    private final boolean DEBUG = false;
    protected final Context mContext;
    private boolean mFactorySet;
    private Factory<T> mFactory;
    private final Object[] mConstructorArgs = new Object[2];
    private static final Class[] mConstructorSignature = new Class[] {
            Context.class, AttributeSet.class};
    private static final HashMap sConstructorMap = new HashMap();
    private String mDefaultPackage;
    public interface Parent<T> {
        public void addItemFromInflater(T child);
    }
    public interface Factory<T> {
        public T onCreateItem(String name, Context context, AttributeSet attrs);
    }
    private static class FactoryMerger<T> implements Factory<T> {
        private final Factory<T> mF1, mF2;
        FactoryMerger(Factory<T> f1, Factory<T> f2) {
            mF1 = f1;
            mF2 = f2;
        }
        public T onCreateItem(String name, Context context, AttributeSet attrs) {
            T v = mF1.onCreateItem(name, context, attrs);
            if (v != null) return v;
            return mF2.onCreateItem(name, context, attrs);
        }
    }
    protected GenericInflater(Context context) {
        mContext = context;
    }
    protected GenericInflater(GenericInflater<T,P> original, Context newContext) {
        mContext = newContext;
        mFactory = original.mFactory;
    }
    public abstract GenericInflater cloneInContext(Context newContext);
    public void setDefaultPackage(String defaultPackage) {
        mDefaultPackage = defaultPackage;
    }
    public String getDefaultPackage() {
        return mDefaultPackage;
    }
    public Context getContext() {
        return mContext;
    }
    public final Factory<T> getFactory() {
        return mFactory;
    }
    public void setFactory(Factory<T> factory) {
        if (mFactorySet) {
            throw new IllegalStateException("" +
            		"A factory has already been set on this inflater");
        }
        if (factory == null) {
            throw new NullPointerException("Given factory can not be null");
        }
        mFactorySet = true;
        if (mFactory == null) {
            mFactory = factory;
        } else {
            mFactory = new FactoryMerger<T>(factory, mFactory);
        }
    }
    public T inflate(int resource, P root) {
        return inflate(resource, root, root != null);
    }
    public T inflate(XmlPullParser parser, P root) {
        return inflate(parser, root, root != null);
    }
    public T inflate(int resource, P root, boolean attachToRoot) {
        if (DEBUG) System.out.println("INFLATING from resource: " + resource);
        XmlResourceParser parser = getContext().getResources().getXml(resource);
        try {
            return inflate(parser, root, attachToRoot);
        } finally {
            parser.close();
        }
    }
    public T inflate(XmlPullParser parser, P root,
            boolean attachToRoot) {
        synchronized (mConstructorArgs) {
            final AttributeSet attrs = Xml.asAttributeSet(parser);
            mConstructorArgs[0] = mContext;
            T result = (T) root;
            try {
                int type;
                while ((type = parser.next()) != parser.START_TAG
                        && type != parser.END_DOCUMENT) {
                    ;
                }
                if (type != parser.START_TAG) {
                    throw new InflateException(parser.getPositionDescription()
                            + ": No start tag found!");
                }
                if (DEBUG) {
                    System.out.println("**************************");
                    System.out.println("Creating root: "
                            + parser.getName());
                    System.out.println("**************************");
                }
                T xmlRoot = createItemFromTag(parser, parser.getName(),
                        attrs);
                result = (T) onMergeRoots(root, attachToRoot, (P) xmlRoot);
                if (DEBUG) {
                    System.out.println("-----> start inflating children");
                }
                rInflate(parser, result, attrs);
                if (DEBUG) {
                    System.out.println("-----> done inflating children");
                }
            } catch (InflateException e) {
                throw e;
            } catch (XmlPullParserException e) {
                InflateException ex = new InflateException(e.getMessage());
                ex.initCause(e);
                throw ex;
            } catch (IOException e) {
                InflateException ex = new InflateException(
                        parser.getPositionDescription()
                        + ": " + e.getMessage());
                ex.initCause(e);
                throw ex;
            }
            return result;
        }
    }
    public final T createItem(String name, String prefix, AttributeSet attrs)
            throws ClassNotFoundException, InflateException {
        Constructor constructor = (Constructor) sConstructorMap.get(name);
        try {
            if (null == constructor) {
                Class clazz = mContext.getClassLoader().loadClass(
                        prefix != null ? (prefix + name) : name);
                constructor = clazz.getConstructor(mConstructorSignature);
                sConstructorMap.put(name, constructor);
            }
            Object[] args = mConstructorArgs;
            args[1] = attrs;
            return (T) constructor.newInstance(args);
        } catch (NoSuchMethodException e) {
            InflateException ie = new InflateException(attrs
                    .getPositionDescription()
                    + ": Error inflating class "
                    + (prefix != null ? (prefix + name) : name));
            ie.initCause(e);
            throw ie;
        } catch (ClassNotFoundException e) {
            throw e;
        } catch (Exception e) {
            InflateException ie = new InflateException(attrs
                    .getPositionDescription()
                    + ": Error inflating class "
                    + constructor.getClass().getName());
            ie.initCause(e);
            throw ie;
        }
    }
    protected T onCreateItem(String name, AttributeSet attrs) throws ClassNotFoundException {
        return createItem(name, mDefaultPackage, attrs);
    }
    private final T createItemFromTag(XmlPullParser parser, String name, AttributeSet attrs) {
        if (DEBUG) System.out.println("******** Creating item: " + name);
        try {
            T item = (mFactory == null) ? null : mFactory.onCreateItem(name, mContext, attrs);
            if (item == null) {
                if (-1 == name.indexOf('.')) {
                    item = onCreateItem(name, attrs);
                } else {
                    item = createItem(name, null, attrs);
                }
            }
            if (DEBUG) System.out.println("Created item is: " + item);
            return item;
        } catch (InflateException e) {
            throw e;
        } catch (ClassNotFoundException e) {
            InflateException ie = new InflateException(attrs
                    .getPositionDescription()
                    + ": Error inflating class " + name);
            ie.initCause(e);
            throw ie;
        } catch (Exception e) {
            InflateException ie = new InflateException(attrs
                    .getPositionDescription()
                    + ": Error inflating class " + name);
            ie.initCause(e);
            throw ie;
        }
    }
    private void rInflate(XmlPullParser parser, T parent, final AttributeSet attrs)
            throws XmlPullParserException, IOException {
        final int depth = parser.getDepth();
        int type;
        while (((type = parser.next()) != parser.END_TAG || 
                parser.getDepth() > depth) && type != parser.END_DOCUMENT) {
            if (type != parser.START_TAG) {
                continue;
            }
            if (onCreateCustomFromTag(parser, parent, attrs)) {
                continue;
            }
            if (DEBUG) {
                System.out.println("Now inflating tag: " + parser.getName());
            }
            String name = parser.getName();
            T item = createItemFromTag(parser, name, attrs);
            if (DEBUG) {
                System.out
                        .println("Creating params from parent: " + parent);
            }
            ((P) parent).addItemFromInflater(item);
            if (DEBUG) {
                System.out.println("-----> start inflating children");
            }
            rInflate(parser, item, attrs);
            if (DEBUG) {
                System.out.println("-----> done inflating children");
            }
        }
    }
    protected boolean onCreateCustomFromTag(XmlPullParser parser, T parent,
            final AttributeSet attrs) throws XmlPullParserException {
        return false;
    }
    protected P onMergeRoots(P givenRoot, boolean attachToGivenRoot, P xmlRoot) {
        return xmlRoot;
    }
}
