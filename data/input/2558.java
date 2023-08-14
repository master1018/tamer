class CertParseError extends CertException
{
    private static final long serialVersionUID = -4559645519017017804L;
    CertParseError (String where)
    {
        super (CertException.verf_PARSE_ERROR, where);
    }
}
