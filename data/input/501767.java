public class ProcessorImport extends ProcessorInclude
{
    static final long serialVersionUID = -8247537698214245237L;
  protected int getStylesheetType()
  {
    return StylesheetHandler.STYPE_IMPORT;
  }
  protected String getStylesheetInclErr()
  {
    return XSLTErrorResources.ER_IMPORTING_ITSELF;
  }
}
