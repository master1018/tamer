public class EintragTag extends TagSupport {
    private static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("dd.MM.yyyy hh:mm");
    private String feld;
    public String getFeld() {
        return feld;
    }
    public void setFeld(String feld) {
        this.feld = feld;
    }
    @Override
    public int doStartTag() throws JspException {
        try {
            Eintrag eintrag = (Eintrag) pageContext.getAttribute("eintrag");
            if ("email".equals(feld)) pageContext.getOut().print(eintrag.getEmail()); else if ("zeitpunkt".equals(feld)) pageContext.getOut().print(DATEFORMAT.format(eintrag.getZeitpunkt())); else if ("zusage".equals(feld)) pageContext.getOut().print((eintrag.isZusage()) ? "ja" : "nein");
            return SKIP_BODY;
        } catch (IOException ex) {
            throw new JspException(ex);
        }
    }
}
