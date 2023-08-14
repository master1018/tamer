public class WebRowSetXmlReader implements XmlReader, Serializable {
    private JdbcRowSetResourceBundle resBundle;
    public WebRowSetXmlReader(){
        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    public void readXML(WebRowSet caller, java.io.Reader reader) throws SQLException {
        try {
            InputSource is = new InputSource(reader);
            DefaultHandler dh = new XmlErrorHandler();
            XmlReaderContentHandler hndr = new XmlReaderContentHandler((RowSet)caller);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(true);
            SAXParser parser = factory.newSAXParser() ;
            parser.setProperty(
                               "http:
            XMLReader reader1 = parser.getXMLReader() ;
            reader1.setEntityResolver(new XmlResolver());
            reader1.setContentHandler(hndr);
            reader1.setErrorHandler(dh);
            reader1.parse(is);
        } catch (SAXParseException err) {
            System.out.println (MessageFormat.format(resBundle.handleGetObject("wrsxmlreader.parseerr").toString(), new Object[]{ err.getMessage (), err.getLineNumber(), err.getSystemId()}));
            err.printStackTrace();
            throw new SQLException(err.getMessage());
        } catch (SAXException e) {
            Exception   x = e;
            if (e.getException () != null)
                x = e.getException();
            x.printStackTrace ();
            throw new SQLException(x.getMessage());
        }
         catch (ArrayIndexOutOfBoundsException aie) {
              throw new SQLException(resBundle.handleGetObject("wrsxmlreader.invalidcp").toString());
        }
        catch (Throwable e) {
            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("wrsxmlreader.readxml").toString() , e.getMessage()));
        }
    }
    public void readXML(WebRowSet caller, java.io.InputStream iStream) throws SQLException {
        try {
            InputSource is = new InputSource(iStream);
            DefaultHandler dh = new XmlErrorHandler();
            XmlReaderContentHandler hndr = new XmlReaderContentHandler((RowSet)caller);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(true);
            SAXParser parser = factory.newSAXParser() ;
            parser.setProperty("http:
                     "http:
            XMLReader reader1 = parser.getXMLReader() ;
            reader1.setEntityResolver(new XmlResolver());
            reader1.setContentHandler(hndr);
            reader1.setErrorHandler(dh);
            reader1.parse(is);
        } catch (SAXParseException err) {
            System.out.println (MessageFormat.format(resBundle.handleGetObject("wrsxmlreader.parseerr").toString(), new Object[]{err.getLineNumber(), err.getSystemId() }));
            System.out.println("   " + err.getMessage ());
            err.printStackTrace();
            throw new SQLException(err.getMessage());
        } catch (SAXException e) {
            Exception   x = e;
            if (e.getException () != null)
                x = e.getException();
            x.printStackTrace ();
            throw new SQLException(x.getMessage());
        }
         catch (ArrayIndexOutOfBoundsException aie) {
              throw new SQLException(resBundle.handleGetObject("wrsxmlreader.invalidcp").toString());
        }
        catch (Throwable e) {
            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("wrsxmlreader.readxml").toString() , e.getMessage()));
        }
    }
    public void readData(RowSetInternal caller) {
    }
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    static final long serialVersionUID = -9127058392819008014L;
}
