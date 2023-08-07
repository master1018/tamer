public class AddParser extends DefaultHandler {
    protected AddType add;
    protected boolean readMetadataText;
    protected boolean readItemXml;
    protected boolean readItemBase64;
    protected StringBuffer sb = null;
    protected String itemXml;
    protected String itemBase64;
    public AddParser() {
        readMetadataText = false;
        readItemXml = false;
        readItemBase64 = false;
    }
    public AddType getAdd() {
        return add;
    }
    public String getXmlContent() {
        return itemXml;
    }
    public String getBase64Content() {
        byte[] data = Base64.decode(itemBase64);
        return new String(data);
    }
    public String getItemContentAsString() {
        if (add.getItem() == null) {
            return null;
        } else if (add.getItem().getContentTransferEncoding().toString().equals("x-native-xml")) {
            return getXmlContent();
        } else {
            return getBase64Content();
        }
    }
    public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes attributes) throws SAXException {
        Logger.instance().log(Logger.DEBUG, "sub", "AddParser.startElement", "localName=" + localName + " namespace=" + namespaceURI);
        if (readItemXml) {
            String eName = localName;
            if ("".equals(eName)) eName = qName;
            sb.append("<" + eName);
            if (attributes != null) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    String aName = attributes.getLocalName(i);
                    if ("".equals(aName)) aName = attributes.getQName(i);
                    sb.append(" ");
                    sb.append(aName + "=\"" + attributes.getValue(i) + "\"");
                }
            }
            sb.append(">");
        } else if ("addType".equals(localName)) {
            add = new AddType();
            String elementId = attributes.getValue("subscription-element-id");
            if (elementId != null) {
                add.setSubscriptionElementId(new Token(elementId));
            }
            String isNew = attributes.getValue("is-new");
            if (isNew != null) {
                add.setIsNew(Boolean.valueOf(isNew.trim()).booleanValue());
            }
            String activation = attributes.getValue("activation");
            String expiration = attributes.getValue("expiration");
        } else if ("metadata".equals(localName)) {
            MetadataType md = new MetadataType();
            add.setMetadata(md);
            String contentFilename = attributes.getValue("content-filename");
            if (contentFilename != null) {
                md.setContentFilename(new Token(contentFilename));
            }
            String contentType = attributes.getValue("content-type");
            if (contentType != null) {
                md.setContentType(new Token(contentType));
            }
            String atomicUse = attributes.getValue("atomic-use");
            if (atomicUse != null) {
                md.setAtomicUse(Boolean.valueOf(atomicUse.trim()).booleanValue());
            }
            String editable = attributes.getValue("editable");
            if (editable != null) {
                md.setEditable(Boolean.valueOf(editable.trim()).booleanValue());
            }
            String ipStatus = attributes.getValue("ip-status");
            if (ipStatus != null) {
                md.setIpStatus(new Token(ipStatus));
            }
            String license = attributes.getValue("license");
            if (license != null) {
                md.setLicense(new Token(license));
            }
            String rightsHolder = attributes.getValue("rights-holder");
            if (rightsHolder != null) {
                md.setRightsHolder(new Token(rightsHolder));
            }
            String showCredit = attributes.getValue("show-credit");
            if (showCredit != null) {
                md.setShowCredit(Boolean.valueOf(showCredit.trim()).booleanValue());
            }
            String itemType = attributes.getValue("item-type");
        } else if ("description".equals(localName)) {
            readMetadataText = true;
            sb = new StringBuffer();
        } else if ("item-ref".equals(localName)) {
            String nil = attributes.getValue("xsi:nil");
            if (nil == null) {
                AddTypeItemRef itemRef = new AddTypeItemRef();
                add.setItemRef(itemRef);
                String name = attributes.getValue("name");
                if (name != null) {
                    itemRef.setName(new Token(name));
                }
                String retrieveAfter = attributes.getValue("retrieve-after");
            }
        } else if ("reference".equals(localName)) {
            String url = attributes.getValue("url");
            UrlAccessType ref = new UrlAccessType();
            try {
                ref.setUrl(new URI(url));
            } catch (URI.MalformedURIException e) {
            }
            String username = attributes.getValue("username");
            if (username != null) {
                ref.setUsername(new Token(username));
            }
            String password = attributes.getValue("password");
            if (password != null) {
                ref.setPassword(new Token(password));
            }
            String authScheme = attributes.getValue("authentication-scheme");
            if (authScheme != null) {
                UrlAccessTypeAuthenticationScheme uatAuthScheme = new UrlAccessTypeAuthenticationScheme();
                uatAuthScheme.setValue(authScheme);
                ref.setAuthenticationScheme(uatAuthScheme);
            }
            add.getItemRef().setReference(ref);
        } else if ("item".equals(localName)) {
            String encoding = attributes.getValue("content-transfer-encoding");
            if (encoding != null) {
                ItemType item = new ItemType();
                add.setItem(item);
                item.setContentTransferEncoding(ItemTypeContentTransferEncoding.fromString(encoding));
                String name = attributes.getValue("name");
                if (name != null) {
                    item.setName(new Token(name));
                }
                if (encoding.equals("x-native-xml")) {
                    readItemXml = true;
                    sb = new StringBuffer();
                    sb.append("<?xml version='1.0'?>");
                }
            }
        } else if ("base64Binary".equals(localName)) {
            readItemBase64 = true;
            sb = new StringBuffer();
        }
    }
    public void characters(char[] data, int start, int length) throws SAXException {
        if (readMetadataText || readItemXml || readItemBase64) {
            sb.append(data, start, length);
        }
    }
    public void endElement(final String namespaceURI, final String localName, final String qName) throws SAXException {
        Logger.instance().log(Logger.DEBUG, "sub", "AddParser.endElement", "localName=" + localName + " namespace=" + namespaceURI);
        if ("description".equals(localName)) {
            String textData = sb.toString().trim();
            readMetadataText = false;
            TextType tt = new TextType(textData);
            add.getMetadata().setDescription(tt);
        } else if ("item".equals(localName)) {
            if (readItemXml) {
                itemXml = sb.toString().trim();
                readItemXml = false;
            }
        } else if ("base64Binary".equals(localName)) {
            if (readItemBase64) {
                itemBase64 = sb.toString().trim();
                readItemBase64 = false;
            }
        } else if (readItemXml) {
            sb.append("</" + localName + ">");
        }
    }
}
