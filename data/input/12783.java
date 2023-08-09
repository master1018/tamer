public class HtmlTree extends Content {
    private HtmlTag htmlTag;
    private Map<HtmlAttr,String> attrs = Collections.<HtmlAttr,String>emptyMap();
    private List<Content> content = Collections.<Content>emptyList();
    public static final Content EMPTY = new StringContent("");
    public HtmlTree(HtmlTag tag) {
        htmlTag = nullCheck(tag);
    }
    public HtmlTree(HtmlTag tag, Content... contents) {
        this(tag);
        for (Content content: contents)
            addContent(content);
    }
    public void addAttr(HtmlAttr attrName, String attrValue) {
        if (attrs.isEmpty())
            attrs = new LinkedHashMap<HtmlAttr,String>();
        attrs.put(nullCheck(attrName), nullCheck(attrValue));
    }
    public void addStyle(HtmlStyle style) {
        addAttr(HtmlAttr.CLASS, style.toString());
    }
    public void addContent(Content tagContent) {
        if (tagContent == HtmlTree.EMPTY || tagContent.isValid()) {
            if (content.isEmpty())
                content = new ArrayList<Content>();
            content.add(tagContent);
        }
    }
    public void addContent(String stringContent) {
        if (!content.isEmpty()) {
            Content lastContent = content.get(content.size() - 1);
            if (lastContent instanceof StringContent)
                lastContent.addContent(stringContent);
            else
                addContent(new StringContent(stringContent));
        }
        else
            addContent(new StringContent(stringContent));
    }
    public static HtmlTree A(String ref, Content body) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.A, nullCheck(body));
        htmltree.addAttr(HtmlAttr.HREF, nullCheck(ref));
        return htmltree;
    }
    public static HtmlTree A_NAME(String name, Content body) {
        HtmlTree htmltree = HtmlTree.A_NAME(name);
        htmltree.addContent(nullCheck(body));
        return htmltree;
    }
    public static HtmlTree A_NAME(String name) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.A);
        htmltree.addAttr(HtmlAttr.NAME, nullCheck(name));
        return htmltree;
    }
    public static HtmlTree CAPTION(Content body) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.CAPTION, nullCheck(body));
        return htmltree;
    }
    public static HtmlTree CODE(Content body) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.CODE, nullCheck(body));
        return htmltree;
    }
    public static HtmlTree DD(Content body) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.DD, nullCheck(body));
        return htmltree;
    }
    public static HtmlTree DL(Content body) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.DL, nullCheck(body));
        return htmltree;
    }
    public static HtmlTree DIV(HtmlStyle styleClass, Content body) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.DIV, nullCheck(body));
        if (styleClass != null)
            htmltree.addStyle(styleClass);
        return htmltree;
    }
    public static HtmlTree DIV(Content body) {
        return DIV(null, body);
    }
    public static HtmlTree DT(Content body) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.DT, nullCheck(body));
        return htmltree;
    }
    public static HtmlTree EM(Content body) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.EM, nullCheck(body));
        return htmltree;
    }
    public static HtmlTree FRAME(String src, String name, String title, String scrolling) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.FRAME);
        htmltree.addAttr(HtmlAttr.SRC, nullCheck(src));
        htmltree.addAttr(HtmlAttr.NAME, nullCheck(name));
        htmltree.addAttr(HtmlAttr.TITLE, nullCheck(title));
        if (scrolling != null)
            htmltree.addAttr(HtmlAttr.SCROLLING, scrolling);
        return htmltree;
    }
    public static HtmlTree FRAME(String src, String name, String title) {
        return FRAME(src, name, title, null);
    }
    public static HtmlTree FRAMESET(String cols, String rows, String title, String onload) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.FRAMESET);
        if (cols != null)
            htmltree.addAttr(HtmlAttr.COLS, cols);
        if (rows != null)
            htmltree.addAttr(HtmlAttr.ROWS, rows);
        htmltree.addAttr(HtmlAttr.TITLE, nullCheck(title));
        htmltree.addAttr(HtmlAttr.ONLOAD, nullCheck(onload));
        return htmltree;
    }
    public static HtmlTree HEADING(HtmlTag headingTag, boolean printTitle,
            HtmlStyle styleClass, Content body) {
        HtmlTree htmltree = new HtmlTree(headingTag, nullCheck(body));
        if (printTitle)
            htmltree.addAttr(HtmlAttr.TITLE, Util.stripHtml(body.toString()));
        if (styleClass != null)
            htmltree.addStyle(styleClass);
        return htmltree;
    }
    public static HtmlTree HEADING(HtmlTag headingTag, HtmlStyle styleClass, Content body) {
        return HEADING(headingTag, false, styleClass, body);
    }
    public static HtmlTree HEADING(HtmlTag headingTag, boolean printTitle, Content body) {
        return HEADING(headingTag, printTitle, null, body);
    }
    public static HtmlTree HEADING(HtmlTag headingTag, Content body) {
        return HEADING(headingTag, false, null, body);
    }
    public static HtmlTree HTML(String lang, Content head, Content body) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.HTML, nullCheck(head), nullCheck(body));
        htmltree.addAttr(HtmlAttr.LANG, nullCheck(lang));
        return htmltree;
    }
    public static HtmlTree I(Content body) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.I, nullCheck(body));
        return htmltree;
    }
    public static HtmlTree LI(Content body) {
        return LI(null, body);
    }
    public static HtmlTree LI(HtmlStyle styleClass, Content body) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.LI, nullCheck(body));
        if (styleClass != null)
            htmltree.addStyle(styleClass);
        return htmltree;
    }
    public static HtmlTree LINK(String rel, String type, String href, String title) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.LINK);
        htmltree.addAttr(HtmlAttr.REL, nullCheck(rel));
        htmltree.addAttr(HtmlAttr.TYPE, nullCheck(type));
        htmltree.addAttr(HtmlAttr.HREF, nullCheck(href));
        htmltree.addAttr(HtmlAttr.TITLE, nullCheck(title));
        return htmltree;
    }
    public static HtmlTree META(String httpEquiv, String content, String charSet) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.META);
        htmltree.addAttr(HtmlAttr.HTTP_EQUIV, nullCheck(httpEquiv));
        htmltree.addAttr(HtmlAttr.CONTENT, nullCheck(content));
        htmltree.addAttr(HtmlAttr.CHARSET, nullCheck(charSet));
        return htmltree;
    }
    public static HtmlTree META(String name, String content) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.META);
        htmltree.addAttr(HtmlAttr.NAME, nullCheck(name));
        htmltree.addAttr(HtmlAttr.CONTENT, nullCheck(content));
        return htmltree;
    }
    public static HtmlTree NOSCRIPT(Content body) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.NOSCRIPT, nullCheck(body));
        return htmltree;
    }
    public static HtmlTree P(Content body) {
        return P(null, body);
    }
    public static HtmlTree P(HtmlStyle styleClass, Content body) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.P, nullCheck(body));
        if (styleClass != null)
            htmltree.addStyle(styleClass);
        return htmltree;
    }
    public static HtmlTree SMALL(Content body) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.SMALL, nullCheck(body));
        return htmltree;
    }
    public static HtmlTree STRONG(Content body) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.STRONG, nullCheck(body));
        return htmltree;
    }
    public static HtmlTree SPAN(Content body) {
        return SPAN(null, body);
    }
    public static HtmlTree SPAN(HtmlStyle styleClass, Content body) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.SPAN, nullCheck(body));
        if (styleClass != null)
            htmltree.addStyle(styleClass);
        return htmltree;
    }
    public static HtmlTree TABLE(int border, int width, String summary,
            Content body) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.TABLE, nullCheck(body));
        htmltree.addAttr(HtmlAttr.BORDER, Integer.toString(border));
        htmltree.addAttr(HtmlAttr.WIDTH, Integer.toString(width));
        htmltree.addAttr(HtmlAttr.SUMMARY, nullCheck(summary));
        return htmltree;
    }
    public static HtmlTree TABLE(HtmlStyle styleClass, int border, int cellPadding,
            int cellSpacing, String summary, Content body) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.TABLE, nullCheck(body));
        if (styleClass != null)
            htmltree.addStyle(styleClass);
        htmltree.addAttr(HtmlAttr.BORDER, Integer.toString(border));
        htmltree.addAttr(HtmlAttr.CELLPADDING, Integer.toString(cellPadding));
        htmltree.addAttr(HtmlAttr.CELLSPACING, Integer.toString(cellSpacing));
        htmltree.addAttr(HtmlAttr.SUMMARY, nullCheck(summary));
        return htmltree;
    }
    public static HtmlTree TABLE(int border, int cellPadding,
            int cellSpacing, String summary, Content body) {
        return TABLE(null, border, cellPadding, cellSpacing, summary, body);
    }
    public static HtmlTree TD(HtmlStyle styleClass, Content body) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.TD, nullCheck(body));
        if (styleClass != null)
            htmltree.addStyle(styleClass);
        return htmltree;
    }
    public static HtmlTree TD(Content body) {
        return TD(null, body);
    }
    public static HtmlTree TH(HtmlStyle styleClass, String scope, Content body) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.TH, nullCheck(body));
        if (styleClass != null)
            htmltree.addStyle(styleClass);
        htmltree.addAttr(HtmlAttr.SCOPE, nullCheck(scope));
        return htmltree;
    }
    public static HtmlTree TH(String scope, Content body) {
        return TH(null, scope, body);
    }
    public static HtmlTree TITLE(Content body) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.TITLE, nullCheck(body));
        return htmltree;
    }
    public static HtmlTree TR(Content body) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.TR, nullCheck(body));
        return htmltree;
    }
    public static HtmlTree UL(HtmlStyle styleClass, Content body) {
        HtmlTree htmltree = new HtmlTree(HtmlTag.UL, nullCheck(body));
        htmltree.addStyle(nullCheck(styleClass));
        return htmltree;
    }
    public boolean isEmpty() {
        return (!hasContent() && !hasAttrs());
    }
    public boolean hasContent() {
        return (!content.isEmpty());
    }
    public boolean hasAttrs() {
        return (!attrs.isEmpty());
    }
    public boolean hasAttr(HtmlAttr attrName) {
        return (attrs.containsKey(attrName));
    }
    public boolean isValid() {
        switch (htmlTag) {
            case A :
                return (hasAttr(HtmlAttr.NAME) || (hasAttr(HtmlAttr.HREF) && hasContent()));
            case BR :
                return (!hasContent() && (!hasAttrs() || hasAttr(HtmlAttr.CLEAR)));
            case FRAME :
                return (hasAttr(HtmlAttr.SRC) && !hasContent());
            case HR :
                return (!hasContent());
            case IMG :
                return (hasAttr(HtmlAttr.SRC) && hasAttr(HtmlAttr.ALT) && !hasContent());
            case LINK :
                return (hasAttr(HtmlAttr.HREF) && !hasContent());
            case META :
                return (hasAttr(HtmlAttr.CONTENT) && !hasContent());
            default :
                return hasContent();
        }
    }
    public boolean isInline() {
        return (htmlTag.blockType == HtmlTag.BlockType.INLINE);
    }
    public void write(StringBuilder contentBuilder) {
        if (!isInline() && !endsWithNewLine(contentBuilder))
            contentBuilder.append(DocletConstants.NL);
        String tagString = htmlTag.toString();
        contentBuilder.append("<");
        contentBuilder.append(tagString);
        Iterator<HtmlAttr> iterator = attrs.keySet().iterator();
        HtmlAttr key;
        String value = "";
        while (iterator.hasNext()) {
            key = iterator.next();
            value = attrs.get(key);
            contentBuilder.append(" ");
            contentBuilder.append(key.toString());
            if (!value.isEmpty()) {
                contentBuilder.append("=\"");
                contentBuilder.append(value);
                contentBuilder.append("\"");
            }
        }
        contentBuilder.append(">");
        for (Content c : content)
            c.write(contentBuilder);
        if (htmlTag.endTagRequired()) {
            contentBuilder.append("</");
            contentBuilder.append(tagString);
            contentBuilder.append(">");
        }
        if (!isInline())
            contentBuilder.append(DocletConstants.NL);
    }
}
