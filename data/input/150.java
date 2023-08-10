public class Atom10Source extends Atom10AbstractContainer {
    public static final String TAG_SOURCE = "source";
    private Atom10Generator m_generator;
    private Atom10Icon m_icon;
    private Atom10Logo m_logo;
    private Atom10TextConstruct m_subtitle;
    public Atom10Source() {
        this(null);
    }
    public Atom10Source(Atom10CommonAttributes parent) {
        super(parent);
    }
    public Atom10Generator getGenerator() {
        return m_generator;
    }
    public void setGenerator(Atom10Generator generator) {
        if (m_generator != null) m_generator.setParentCommonAttributes(null);
        m_generator = generator;
        if (m_generator != null) m_generator.setParentCommonAttributes(this);
    }
    public Atom10Icon getIcon() {
        return m_icon;
    }
    public void setIcon(Atom10Icon icon) {
        if (m_icon != null) m_icon.setParentCommonAttributes(null);
        m_icon = icon;
        if (m_icon != null) m_icon.setParentCommonAttributes(this);
    }
    public Atom10Logo getLogo() {
        return m_logo;
    }
    public void setLogo(Atom10Logo logo) {
        if (m_logo != null) m_logo.setParentCommonAttributes(null);
        m_logo = logo;
        if (m_logo != null) m_logo.setParentCommonAttributes(this);
    }
    public Atom10TextConstruct getSubtitle() {
        return m_subtitle;
    }
    public void setSubtitle(Atom10TextConstruct subtitle) {
        if (m_subtitle != null) m_subtitle.setParentCommonAttributes(null);
        m_subtitle = subtitle;
        if (m_subtitle != null) m_subtitle.setParentCommonAttributes(this);
    }
}
