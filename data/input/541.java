public class ToNewsMLFilter extends XSLFilter {
    static Category cat = Category.getInstance(ToNewsMLFilter.class.getName());
    private DocHandler _instance = null;
    public DocHandler getInstance(ExtendedProperties config) {
        if (_instance == null) {
            _instance = (DocHandler) new ToNewsMLFilter(config);
        }
        return _instance;
    }
    public ToNewsMLFilter(ExtendedProperties config) {
        super(config);
        setXslfile(config.getString(TSN_XSL_FILENAME_PROPERTY, DEFAULT_TSN_XSL_FILENAME));
    }
    protected String setDocParameter(Transformer xslt, Document dom, String doctag) {
        DocType dtd = dom.getDocType();
        if (dtd != null && dtd.getSystemID() != null) {
            cat.debug("PublicID = " + dtd.getPublicID() + " : SystemID = " + dtd.getSystemID());
        } else {
            cat.error("cannot read DocType/SystemID from " + doctag);
            return null;
        }
        String sysid = dtd.getSystemID();
        if (sysid != null && sysid.endsWith(".dtd")) {
            int endpath = sysid.lastIndexOf('/');
            sysid = sysid.substring(((endpath >= 0) ? endpath + 1 : 0), sysid.lastIndexOf(".dtd"));
        } else {
            cat.warn("SysID does not end in .dtd: " + sysid);
        }
        cat.debug("Set param " + XSL_SYSID_PARAM_PROPERTY + " = " + sysid);
        xslt.setParameter(XSL_SYSID_PARAM_PROPERTY, sysid);
        return sysid;
    }
}
