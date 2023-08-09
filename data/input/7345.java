public class DocumentParser extends javax.swing.text.html.parser.Parser {
    private int inbody;
    private int intitle;
    private int inhead;
    private int instyle;
    private int inscript;
    private boolean seentitle;
    private HTMLEditorKit.ParserCallback callback = null;
    private boolean ignoreCharSet = false;
    private static final boolean debugFlag = false;
    public DocumentParser(DTD dtd) {
        super(dtd);
    }
    public void parse(Reader in,  HTMLEditorKit.ParserCallback callback, boolean ignoreCharSet) throws IOException {
        this.ignoreCharSet = ignoreCharSet;
        this.callback = callback;
        parse(in);
        callback.handleEndOfLineString(getEndOfLineString());
    }
    protected void handleStartTag(TagElement tag) {
        Element elem = tag.getElement();
        if (elem == dtd.body) {
            inbody++;
        } else if (elem == dtd.html) {
        } else if (elem == dtd.head) {
            inhead++;
        } else if (elem == dtd.title) {
            intitle++;
        } else if (elem == dtd.style) {
            instyle++;
        } else if (elem == dtd.script) {
            inscript++;
        }
        if (debugFlag) {
            if (tag.fictional()) {
                debug("Start Tag: " + tag.getHTMLTag() + " pos: " + getCurrentPos());
            } else {
                debug("Start Tag: " + tag.getHTMLTag() + " attributes: " +
                      getAttributes() + " pos: " + getCurrentPos());
            }
        }
        if (tag.fictional()) {
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            attrs.addAttribute(HTMLEditorKit.ParserCallback.IMPLIED,
                               Boolean.TRUE);
            callback.handleStartTag(tag.getHTMLTag(), attrs,
                                    getBlockStartPosition());
        } else {
            callback.handleStartTag(tag.getHTMLTag(), getAttributes(),
                                    getBlockStartPosition());
            flushAttributes();
        }
    }
    protected void handleComment(char text[]) {
        if (debugFlag) {
            debug("comment: ->" + new String(text) + "<-"
                  + " pos: " + getCurrentPos());
        }
        callback.handleComment(text, getBlockStartPosition());
    }
    protected void handleEmptyTag(TagElement tag) throws ChangedCharSetException {
        Element elem = tag.getElement();
        if (elem == dtd.meta && !ignoreCharSet) {
            SimpleAttributeSet atts = getAttributes();
            if (atts != null) {
                String content = (String)atts.getAttribute(HTML.Attribute.CONTENT);
                if (content != null) {
                    if ("content-type".equalsIgnoreCase((String)atts.getAttribute(HTML.Attribute.HTTPEQUIV))) {
                        if (!content.equalsIgnoreCase("text/html") &&
                                !content.equalsIgnoreCase("text/plain")) {
                            throw new ChangedCharSetException(content, false);
                        }
                    } else if ("charset" .equalsIgnoreCase((String)atts.getAttribute(HTML.Attribute.HTTPEQUIV))) {
                        throw new ChangedCharSetException(content, true);
                    }
                }
            }
        }
        if (inbody != 0 || elem == dtd.meta || elem == dtd.base || elem == dtd.isindex || elem == dtd.style || elem == dtd.link) {
            if (debugFlag) {
                if (tag.fictional()) {
                    debug("Empty Tag: " + tag.getHTMLTag() + " pos: " + getCurrentPos());
                } else {
                    debug("Empty Tag: " + tag.getHTMLTag() + " attributes: "
                          + getAttributes() + " pos: " + getCurrentPos());
                }
            }
            if (tag.fictional()) {
                SimpleAttributeSet attrs = new SimpleAttributeSet();
                attrs.addAttribute(HTMLEditorKit.ParserCallback.IMPLIED,
                                   Boolean.TRUE);
                callback.handleSimpleTag(tag.getHTMLTag(), attrs,
                                         getBlockStartPosition());
            } else {
                callback.handleSimpleTag(tag.getHTMLTag(), getAttributes(),
                                         getBlockStartPosition());
                flushAttributes();
            }
        }
    }
    protected void handleEndTag(TagElement tag) {
        Element elem = tag.getElement();
        if (elem == dtd.body) {
            inbody--;
        } else if (elem == dtd.title) {
            intitle--;
            seentitle = true;
        } else if (elem == dtd.head) {
            inhead--;
        } else if (elem == dtd.style) {
            instyle--;
        } else if (elem == dtd.script) {
            inscript--;
        }
        if (debugFlag) {
            debug("End Tag: " + tag.getHTMLTag() + " pos: " + getCurrentPos());
        }
        callback.handleEndTag(tag.getHTMLTag(), getBlockStartPosition());
    }
    protected void handleText(char data[]) {
        if (data != null) {
            if (inscript != 0) {
                callback.handleComment(data, getBlockStartPosition());
                return;
            }
            if (inbody != 0 || ((instyle != 0) ||
                                ((intitle != 0) && !seentitle))) {
                if (debugFlag) {
                    debug("text:  ->" + new String(data) + "<-" + " pos: " + getCurrentPos());
                }
                callback.handleText(data, getBlockStartPosition());
            }
        }
    }
    protected void handleError(int ln, String errorMsg) {
        if (debugFlag) {
            debug("Error: ->" + errorMsg + "<-" + " pos: " + getCurrentPos());
        }
        callback.handleError(errorMsg, getCurrentPos());
    }
    private void debug(String msg) {
        System.out.println(msg);
    }
}
