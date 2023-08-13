public final class DescriptorsUtils {
    private static final String DEFAULT_WIDGET_PREFIX = "widget";
    private static final int JAVADOC_BREAK_LENGTH = 60;
    public static final String MANIFEST_SDK_URL = "/reference/android/R.styleable.html#";  
    public static final String IMAGE_KEY = "image"; 
    private static final String CODE  = "$code";  
    private static final String LINK  = "$link";  
    private static final String ELEM  = "$elem";  
    private static final String BREAK = "$break"; 
    public interface ITextAttributeCreator {
        public TextAttributeDescriptor create(String xmlName, String uiName, String nsUri,
                String tooltip);
    }
    public static void appendAttributes(ArrayList<AttributeDescriptor> attributes,
            String elementXmlName,
            String nsUri, AttributeInfo[] infos,
            Set<String> requiredAttributes,
            Map<String, Object> overrides) {
        for (AttributeInfo info : infos) {
            boolean required = false;
            if (requiredAttributes != null) {
                String attr_name = info.getName();
                if (requiredAttributes.contains("*/" + attr_name) ||
                        requiredAttributes.contains(elementXmlName + "/" + attr_name)) {
                    required = true;
                }
            }
            appendAttribute(attributes, elementXmlName, nsUri, info, required, overrides);
        }
    }
    public static void appendAttribute(ArrayList<AttributeDescriptor> attributes,
            String elementXmlName,
            String nsUri,
            AttributeInfo info, boolean required,
            Map<String, Object> overrides) {
        AttributeDescriptor attr = null;
        String xmlLocalName = info.getName();
        String uiName = prettyAttributeUiName(info.getName()); 
        if (required) {
            uiName += "*"; 
        }
        String tooltip = null;
        String rawTooltip = info.getJavaDoc();
        if (rawTooltip == null) {
            rawTooltip = "";
        }
        String deprecated = info.getDeprecatedDoc();
        if (deprecated != null) {
            if (rawTooltip.length() > 0) {
                rawTooltip += "@@"; 
            }
            rawTooltip += "* Deprecated";
            if (deprecated.length() != 0) {
                rawTooltip += ": " + deprecated;                            
            }
            if (deprecated.length() == 0 || !deprecated.endsWith(".")) {    
                rawTooltip += ".";                                          
            }
        }
        Format[] formats_list = info.getFormats();
        int flen = formats_list.length;
        if (flen > 0) {
            HashSet<Format> formats_set = new HashSet<Format>();
            StringBuilder sb = new StringBuilder();
            if (rawTooltip != null && rawTooltip.length() > 0) {
                sb.append(rawTooltip);
                sb.append(" ");     
            }
            if (sb.length() > 0) {
                sb.append("@@");    
            }
            sb.append("[");         
            for (int i = 0; i < flen; i++) {
                Format f = formats_list[i];
                formats_set.add(f);
                sb.append(f.toString().toLowerCase());
                if (i < flen - 1) {
                    sb.append(", "); 
                }
            }
            sb.append("]"); 
            if (required) {
                sb.append(".@@* ");          
                sb.append("Required.");
            }
            sb.append(" "); 
            rawTooltip = sb.toString();
            tooltip = formatTooltip(rawTooltip);
            if (overrides != null) {
                for (Entry<String, Object> entry: overrides.entrySet()) {
                    String key = entry.getKey();
                    String elements[] = key.split("/");          
                    String overrideAttrLocalName = null;
                    if (elements.length < 1) {
                        continue;
                    } else if (elements.length == 1) {
                        overrideAttrLocalName = elements[0];
                        elements = null;
                    } else {
                        overrideAttrLocalName = elements[elements.length - 1];
                        elements = elements[0].split(",");       
                    }
                    if (overrideAttrLocalName == null ||
                            !overrideAttrLocalName.equals(xmlLocalName)) {
                        continue;
                    }
                    boolean ok_element = elements != null && elements.length < 1;
                    if (!ok_element && elements != null) {
                        for (String element : elements) {
                            if (element.equals("*")              
                                    || element.equals(elementXmlName)) {
                                ok_element = true;
                                break;
                            }
                        }
                    }
                    if (!ok_element) {
                        continue;
                    }
                    Object override = entry.getValue();
                    if (override instanceof Class<?>) {
                        try {
                            @SuppressWarnings("unchecked") 
                            Class<? extends TextAttributeDescriptor> clazz =
                                (Class<? extends TextAttributeDescriptor>) override;
                            Constructor<? extends TextAttributeDescriptor> cons;
                                cons = clazz.getConstructor(new Class<?>[] {
                                        String.class, String.class, String.class, String.class } );
                            attr = cons.newInstance(
                                    new Object[] { xmlLocalName, uiName, nsUri, tooltip });
                        } catch (SecurityException e) {
                        } catch (NoSuchMethodException e) {
                        } catch (IllegalArgumentException e) {
                        } catch (InstantiationException e) {
                        } catch (IllegalAccessException e) {
                        } catch (InvocationTargetException e) {
                        }
                    } else if (override instanceof ITextAttributeCreator) {
                        attr = ((ITextAttributeCreator) override).create(
                                xmlLocalName, uiName, nsUri, tooltip);
                    }
                }
            } 
            if (attr == null) {
                if (formats_set.contains(Format.REFERENCE)) {
                    attr = new ReferenceAttributeDescriptor(xmlLocalName, uiName, nsUri, tooltip);
                } else if (formats_set.contains(Format.ENUM)) {
                    attr = new ListAttributeDescriptor(xmlLocalName, uiName, nsUri, tooltip,
                            info.getEnumValues());
                } else if (formats_set.contains(Format.FLAG)) {
                    attr = new FlagAttributeDescriptor(xmlLocalName, uiName, nsUri, tooltip,
                            info.getFlagValues());
                } else if (formats_set.contains(Format.BOOLEAN)) {
                    attr = new BooleanAttributeDescriptor(xmlLocalName, uiName, nsUri, tooltip);
                } else if (formats_set.contains(Format.STRING)) {
                    attr = new ReferenceAttributeDescriptor(ResourceType.STRING,
                            xmlLocalName, uiName, nsUri,
                            tooltip);
                }
            }
        }
        if (attr == null) {
            if (tooltip == null) {
                tooltip = formatTooltip(rawTooltip);
            }
            attr = new TextAttributeDescriptor(xmlLocalName, uiName, nsUri, tooltip);
        }
        attr.setDeprecated(info.getDeprecatedDoc() != null);
        attributes.add(attr);
    }
    public static boolean containsAttribute(ArrayList<AttributeDescriptor> attributes,
            String nsUri,
            AttributeInfo info) {
        String xmlLocalName = info.getName();
        for (AttributeDescriptor desc : attributes) {
            if (desc.getXmlLocalName().equals(xmlLocalName)) {
                if (nsUri == desc.getNamespaceUri() ||
                        (nsUri != null && nsUri.equals(desc.getNamespaceUri()))) {
                    return true;
                }
            }
        }
        return false;
    }
    public static String prettyAttributeUiName(String name) {
        if (name.length() < 1) {
            return name;
        }
        StringBuffer buf = new StringBuffer();
        char c = name.charAt(0);
        buf.append((char)(c >= 'a' && c <= 'z' ? c + 'A' - 'a' : c));
        int len = name.length();
        for (int i = 1; i < len; i++) {
            c = name.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                buf.append(' ');
                if (c >= 'X' && c <= 'Z' &&
                        (i == len-1 ||
                            (i < len-1 && name.charAt(i+1) >= 'A' && name.charAt(i+1) <= 'Z'))) {
                    buf.append(c);
                } else {
                    buf.append((char)(c - 'A' + 'a'));
                }
            } else if (c == '_') {
                buf.append(' ');
            } else {
                buf.append(c);
            }
        }
        name = buf.toString();
        name = name.replaceAll("(?<=^| )sdk(?=$| )", "SDK");
        name = name.replaceAll("(?<=^| )uri(?=$| )", "URI");
        return name;
    }
    public static String capitalize(String str) {
        if (str == null || str.length() < 1 || str.charAt(0) < 'a' || str.charAt(0) > 'z') {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        sb.append((char)(str.charAt(0) + 'A' - 'a'));
        sb.append(str.substring(1));
        return sb.toString();
    }
    public static String formatTooltip(String javadoc) {
        ArrayList<String> spans = scanJavadoc(javadoc);
        StringBuilder sb = new StringBuilder();
        boolean needBreak = false;
        for (int n = spans.size(), i = 0; i < n; ++i) {
            String s = spans.get(i);
            if (CODE.equals(s)) {
                s = spans.get(++i);
                if (s != null) {
                    sb.append('"').append(s).append('"');
                }
            } else if (LINK.equals(s)) {
                String base   = spans.get(++i);
                String anchor = spans.get(++i);
                String text   = spans.get(++i);
                if (base != null) {
                    base = base.trim();
                }
                if (anchor != null) {
                    anchor = anchor.trim();
                }
                if (text != null) {
                    text = text.trim();
                }
                if (text == null || text.length() == 0) {
                    text = anchor;
                }
                if (base != null && base.length() > 0) {
                    if (text == null || text.length() == 0) {
                        text = base;
                    }
                }
                if (text != null) {
                    sb.append(text);
                }
            } else if (ELEM.equals(s)) {
                s = spans.get(++i);
                if (s != null) {
                    sb.append(s);
                }
            } else if (BREAK.equals(s)) {
                needBreak = true;
            } else if (s != null) {
                if (needBreak && s.trim().length() > 0) {
                    sb.append('\r');
                }
                sb.append(s);
                needBreak = false;
            }
        }
        return sb.toString();
    }
    public static String formatFormText(String javadoc,
            ElementDescriptor elementDescriptor,
            String androidDocBaseUrl) {
        ArrayList<String> spans = scanJavadoc(javadoc);
        String fullSdkUrl = androidDocBaseUrl + MANIFEST_SDK_URL;
        String sdkUrl = elementDescriptor.getSdkUrl();
        if (sdkUrl != null && sdkUrl.startsWith(MANIFEST_SDK_URL)) {
            fullSdkUrl = androidDocBaseUrl + sdkUrl;
        }
        StringBuilder sb = new StringBuilder();
        Image icon = elementDescriptor.getIcon();
        if (icon != null) {
            sb.append("<form><li style=\"image\" value=\"" +        
                    IMAGE_KEY + "\">");                             
        } else {
            sb.append("<form><p>");                                 
        }
        for (int n = spans.size(), i = 0; i < n; ++i) {
            String s = spans.get(i);
            if (CODE.equals(s)) {
                s = spans.get(++i);
                if (elementDescriptor.getXmlName().equals(s) && fullSdkUrl != null) {
                    sb.append("<a href=\"");                        
                    sb.append(fullSdkUrl);
                    sb.append("\">");                               
                    sb.append(s);
                    sb.append("</a>");                              
                } else if (s != null) {
                    sb.append('"').append(s).append('"');
                }
            } else if (LINK.equals(s)) {
                String base   = spans.get(++i);
                String anchor = spans.get(++i);
                String text   = spans.get(++i);
                if (base != null) {
                    base = base.trim();
                }
                if (anchor != null) {
                    anchor = anchor.trim();
                }
                if (text != null) {
                    text = text.trim();
                }
                if (text == null || text.length() == 0) {
                    text = anchor;
                }
                if ((base == null || base.length() == 0) && fullSdkUrl != null) {
                    base = fullSdkUrl;
                }
                String url = null;
                if (base != null && base.length() > 0) {
                    if (base.startsWith("http")) {                  
                        url = base;
                        if (anchor != null && anchor.length() > 0) {
                            int pos = url.lastIndexOf('#');
                            if (pos < 0) {
                                url += "#";                         
                            } else if (pos < url.length() - 1) {
                                url = url.substring(0, pos + 1);
                            }
                            url += anchor;
                        }
                    } else if (text == null || text.length() == 0) {
                        text = base;
                    }
                }
                if (url != null && text != null) {
                    sb.append("<a href=\"");                        
                    sb.append(url);
                    sb.append("\">");                               
                    sb.append(text);
                    sb.append("</a>");                              
                } else if (text != null) {
                    sb.append("<b>").append(text).append("</b>");   
                }
            } else if (ELEM.equals(s)) {
                s = spans.get(++i);
                if (sdkUrl != null && s != null) {
                    sb.append("<a href=\"");                        
                    sb.append(sdkUrl);
                    sb.append("\">");                               
                    sb.append(s);
                    sb.append("</a>");                              
                } else if (s != null) {
                    sb.append("<b>").append(s).append("</b>");      
                }
            } else if (BREAK.equals(s)) {
            } else if (s != null) {
                sb.append(s);
            }
        }
        if (icon != null) {
            sb.append("</li></form>");                              
        } else {
            sb.append("</p></form>");                               
        }
        return sb.toString();
    }
    private static ArrayList<String> scanJavadoc(String javadoc) {
        ArrayList<String> spans = new ArrayList<String>();
        if (javadoc != null) {
            javadoc = javadoc.replaceAll("[ \t\f\r\n]+", " "); 
        }
        Pattern p_link = Pattern.compile("\\{@link\\s+([^#\\}\\s]*)(?:#([^\\s\\}]*))?(?:\\s*([^\\}]*))?\\}(.*)"); 
        Pattern p_code = Pattern.compile("<code>(.+?)</code>(.*)");                 
        Pattern p_elem = Pattern.compile("@([\\w -]+)@(.*)");                       
        Pattern p_break = Pattern.compile("@@(.*)");                                
        Pattern p_open = Pattern.compile("([@<\\{])(.*)");                          
        Pattern p_text = Pattern.compile("([^@<\\{]+)(.*)");                        
        int currentLength = 0;
        String text = null;
        while(javadoc != null && javadoc.length() > 0) {
            Matcher m;
            String s = null;
            if ((m = p_code.matcher(javadoc)).matches()) {
                spans.add(CODE);
                spans.add(text = cleanupJavadocHtml(m.group(1))); 
                javadoc = m.group(2);
                if (text != null) {
                    currentLength += text.length();
                }
            } else if ((m = p_link.matcher(javadoc)).matches()) {
                spans.add(LINK);
                spans.add(m.group(1)); 
                spans.add(m.group(2)); 
                spans.add(text = cleanupJavadocHtml(m.group(3))); 
                javadoc = m.group(4);
                if (text != null) {
                    currentLength += text.length();
                }
            } else if ((m = p_elem.matcher(javadoc)).matches()) {
                spans.add(ELEM);
                spans.add(text = cleanupJavadocHtml(m.group(1))); 
                javadoc = m.group(2);
                if (text != null) {
                    currentLength += text.length() - 2;
                }
            } else if ((m = p_break.matcher(javadoc)).matches()) {
                spans.add(BREAK);
                currentLength = 0;
                javadoc = m.group(1);
            } else if ((m = p_open.matcher(javadoc)).matches()) {
                s = m.group(1);
                javadoc = m.group(2);
            } else if ((m = p_text.matcher(javadoc)).matches()) {
                s = m.group(1);
                javadoc = m.group(2);
            } else {
                s = javadoc;
                javadoc = null;
            }
            if (s != null && s.length() > 0) {
                s = cleanupJavadocHtml(s);
                if (currentLength >= JAVADOC_BREAK_LENGTH) {
                    spans.add(BREAK);
                    currentLength = 0;
                }
                while (currentLength + s.length() > JAVADOC_BREAK_LENGTH) {
                    int pos = s.indexOf(' ', JAVADOC_BREAK_LENGTH - currentLength);
                    if (pos <= 0) {
                        break;
                    }
                    spans.add(s.substring(0, pos + 1));
                    spans.add(BREAK);
                    currentLength = 0;
                    s = s.substring(pos + 1);
                }
                spans.add(s);
                currentLength += s.length();
            }
        }
        return spans;
    }
    private static String cleanupJavadocHtml(String s) {
        if (s != null) {
            s = s.replaceAll("&lt;", "\"");     
            s = s.replaceAll("&gt;", "\"");     
            s = s.replaceAll("<[^>]+>", "");    
        }
        return s;
    }
    public static void setDefaultLayoutAttributes(UiElementNode ui_node, boolean updateLayout) {
        boolean fill = ui_node.getDescriptor().hasChildren() &&
                       ui_node.getUiParent() instanceof UiDocumentNode;
        ui_node.setAttributeValue(LayoutConstants.ATTR_LAYOUT_WIDTH,
                fill ? LayoutConstants.VALUE_FILL_PARENT : LayoutConstants.VALUE_WRAP_CONTENT,
                false );
        ui_node.setAttributeValue(LayoutConstants.ATTR_LAYOUT_HEIGHT,
                fill ? LayoutConstants.VALUE_FILL_PARENT : LayoutConstants.VALUE_WRAP_CONTENT,
                false );
        String widget_id = getFreeWidgetId(ui_node);
        if (widget_id != null) {
            ui_node.setAttributeValue(LayoutConstants.ATTR_ID, widget_id, false );
        }
        ui_node.setAttributeValue(LayoutConstants.ATTR_TEXT, widget_id, false );
        if (updateLayout) {
            UiElementNode ui_parent = ui_node.getUiParent();
            if (ui_parent != null &&
                    ui_parent.getDescriptor().getXmlLocalName().equals(
                            LayoutConstants.RELATIVE_LAYOUT)) {
                UiElementNode ui_previous = ui_node.getUiPreviousSibling();
                if (ui_previous != null) {
                    String id = ui_previous.getAttributeValue(LayoutConstants.ATTR_ID);
                    if (id != null && id.length() > 0) {
                        id = id.replace("@+", "@");                     
                        ui_node.setAttributeValue(LayoutConstants.ATTR_LAYOUT_BELOW, id,
                                false );
                    }
                }
            }
        }
    }
    public static String getFreeWidgetId(UiElementNode uiNode) {
        String name = uiNode.getDescriptor().getXmlLocalName();
        if ("TabWidget".equals(name)) {                        
            return "@android:id/tabs";                         
        }
        return "@+id/" + getFreeWidgetId(uiNode.getUiRoot(),   
                new Object[] { name, null, null, null });
    }
    @SuppressWarnings("unchecked")
    private static String getFreeWidgetId(UiElementNode uiRoot,
            Object[] params) {
        Set<String> map = (Set<String>)params[3];
        if (map == null) {
            params[3] = map = new HashSet<String>();
        }
        int num = params[1] == null ? 0 : ((Integer)params[1]).intValue();
        String generated = (String) params[2];
        String prefix = (String) params[0];
        if (generated == null) {
            int pos = prefix.indexOf('.');
            if (pos >= 0) {
                prefix = prefix.substring(pos + 1);
            }
            pos = prefix.indexOf('$');
            if (pos >= 0) {
                prefix = prefix.substring(pos + 1);
            }
            prefix = prefix.replaceAll("[^a-zA-Z]", "");                
            if (prefix.length() == 0) {
                prefix = DEFAULT_WIDGET_PREFIX;
            }
            do {
                num++;
                generated = String.format("%1$s%2$02d", prefix, num);   
            } while (map.contains(generated));
            params[0] = prefix;
            params[1] = num;
            params[2] = generated;
        }
        String id = uiRoot.getAttributeValue(LayoutConstants.ATTR_ID);
        if (id != null) {
            id = id.replace("@+id/", "");                               
            id = id.replace("@id/", "");                                
            if (map.add(id) && map.contains(generated)) {
                do {
                    num++;
                    generated = String.format("%1$s%2$02d", prefix, num);   
                } while (map.contains(generated));
                params[1] = num;
                params[2] = generated;
            }
        }
        for (UiElementNode uiChild : uiRoot.getUiChildren()) {
            getFreeWidgetId(uiChild, params);
        }
        return (String) params[2];
    }
}
