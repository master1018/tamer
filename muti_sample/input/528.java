public final class DocumentHandler extends DefaultHandler {
    private final Map<String, Class<? extends ElementHandler>> handlers = new HashMap<String, Class<? extends ElementHandler>>();
    private final Map<String, Object> environment = new HashMap<String, Object>();
    private final List<Object> objects = new ArrayList<Object>();
    private Reference<ClassLoader> loader;
    private ExceptionListener listener;
    private Object owner;
    private ElementHandler handler;
    public DocumentHandler() {
        setElementHandler("java", JavaElementHandler.class); 
        setElementHandler("null", NullElementHandler.class); 
        setElementHandler("array", ArrayElementHandler.class); 
        setElementHandler("class", ClassElementHandler.class); 
        setElementHandler("string", StringElementHandler.class); 
        setElementHandler("object", ObjectElementHandler.class); 
        setElementHandler("void", VoidElementHandler.class); 
        setElementHandler("char", CharElementHandler.class); 
        setElementHandler("byte", ByteElementHandler.class); 
        setElementHandler("short", ShortElementHandler.class); 
        setElementHandler("int", IntElementHandler.class); 
        setElementHandler("long", LongElementHandler.class); 
        setElementHandler("float", FloatElementHandler.class); 
        setElementHandler("double", DoubleElementHandler.class); 
        setElementHandler("boolean", BooleanElementHandler.class); 
        setElementHandler("new", NewElementHandler.class); 
        setElementHandler("var", VarElementHandler.class); 
        setElementHandler("true", TrueElementHandler.class); 
        setElementHandler("false", FalseElementHandler.class); 
        setElementHandler("field", FieldElementHandler.class); 
        setElementHandler("method", MethodElementHandler.class); 
        setElementHandler("property", PropertyElementHandler.class); 
    }
    public ClassLoader getClassLoader() {
        return (this.loader != null)
                ? this.loader.get()
                : null;
    }
    public void setClassLoader(ClassLoader loader) {
        this.loader = new WeakReference<ClassLoader>(loader);
    }
    public ExceptionListener getExceptionListener() {
        return this.listener;
    }
    public void setExceptionListener(ExceptionListener listener) {
        this.listener = listener;
    }
    public Object getOwner() {
        return this.owner;
    }
    public void setOwner(Object owner) {
        this.owner = owner;
    }
    public Class<? extends ElementHandler> getElementHandler(String name) {
        Class<? extends ElementHandler> type = this.handlers.get(name);
        if (type == null) {
            throw new IllegalArgumentException("Unsupported element: " + name);
        }
        return type;
    }
    public void setElementHandler(String name, Class<? extends ElementHandler> handler) {
        this.handlers.put(name, handler);
    }
    public boolean hasVariable(String id) {
        return this.environment.containsKey(id);
    }
    public Object getVariable(String id) {
        if (!this.environment.containsKey(id)) {
            throw new IllegalArgumentException("Unbound variable: " + id);
        }
        return this.environment.get(id);
    }
    public void setVariable(String id, Object value) {
        this.environment.put(id, value);
    }
    public Object[] getObjects() {
        return this.objects.toArray();
    }
    void addObject(Object object) {
        this.objects.add(object);
    }
    @Override
    public void startDocument() {
        this.objects.clear();
        this.handler = null;
    }
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        ElementHandler parent = this.handler;
        try {
            this.handler = getElementHandler(qName).newInstance();
            this.handler.setOwner(this);
            this.handler.setParent(parent);
        }
        catch (Exception exception) {
            throw new SAXException(exception);
        }
        for (int i = 0; i < attributes.getLength(); i++)
            try {
                String name = attributes.getQName(i);
                String value = attributes.getValue(i);
                this.handler.addAttribute(name, value);
            }
            catch (RuntimeException exception) {
                handleException(exception);
            }
        this.handler.startElement();
    }
    @Override
    public void endElement(String uri, String localName, String qName) {
        try {
            this.handler.endElement();
        }
        catch (RuntimeException exception) {
            handleException(exception);
        }
        finally {
            this.handler = this.handler.getParent();
        }
    }
    @Override
    public void characters(char[] chars, int start, int length) {
        if (this.handler != null) {
            try {
                while (0 < length--) {
                    this.handler.addCharacter(chars[start++]);
                }
            }
            catch (RuntimeException exception) {
                handleException(exception);
            }
        }
    }
    public void handleException(Exception exception) {
        if (this.listener == null) {
            throw new IllegalStateException(exception);
        }
        this.listener.exceptionThrown(exception);
    }
    public void parse(InputSource input) {
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(input, this);
        }
        catch (ParserConfigurationException exception) {
            handleException(exception);
        }
        catch (SAXException wrapper) {
            Exception exception = wrapper.getException();
            if (exception == null) {
                exception = wrapper;
            }
            handleException(exception);
        }
        catch (IOException exception) {
            handleException(exception);
        }
    }
    public Class<?> findClass(String name) {
        try {
            return ClassFinder.resolveClass(name, getClassLoader());
        }
        catch (ClassNotFoundException exception) {
            handleException(exception);
            return null;
        }
    }
}
