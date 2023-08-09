final class ReportGenerator extends AbstractReportGenerator
                            implements IAppErrorCodes
{
    public String getType ()
    {
        return TYPE;
    }
    public void process (final IMetaData mdata, final ICoverageData cdata,
                         final SourcePathCache cache, final IProperties properties)
        throws EMMARuntimeException
    {
        initialize (mdata, cdata, cache, properties);
        long start = 0, end;
        final boolean trace1 = m_log.atTRACE1 ();
        if (trace1) start = System.currentTimeMillis ();
        {
            m_view.getRoot ().accept (this, null);
            close ();
        }
        if (trace1)
        {
            end = System.currentTimeMillis ();
            m_log.trace1 ("process", "[" + getType () + "] report generated in " + (end - start) + " ms");
        }
    }
    public void cleanup ()
    {
        close ();
        super.cleanup ();
    }
    public Object visit (final AllItem item, final Object ctx)
    {
        try
        {
            File outFile = m_settings.getOutFile ();
            if (outFile == null)
            {
                outFile = new File ("coverage.xml");
                m_settings.setOutFile (outFile);
            }
            final File fullOutFile = Files.newFile (m_settings.getOutDir (), outFile);
            m_log.info ("writing [" + getType () + "] report to [" + fullOutFile.getAbsolutePath () + "] ...");
            openOutFile (fullOutFile, m_settings.getOutEncoding (), true);
            m_out.write ("<?xml version=\"1.0\" encoding=\"" + m_settings.getOutEncoding () + "\"?>");
            try
            {
                final StringBuffer label = new StringBuffer (101);
                label.append ("<!-- ");
                label.append (IAppConstants.APP_NAME);
                label.append (" v"); label.append (IAppConstants.APP_VERSION_WITH_BUILD_ID_AND_TAG);
                label.append (" report, generated ");
                label.append (new Date (EMMAProperties.getTimeStamp ()));
                label.append (" -->");
                m_out.write (label.toString ());
                m_out.newLine ();
                m_out.flush ();
            }
            catch (IOException ioe)
            {
                throw new EMMARuntimeException (IAppErrorCodes.REPORT_IO_FAILURE, ioe);
            }
            eol ();
            openElementTag ("report");
            closeElementTag (false);
            m_out.incIndent ();
            eol ();
            openElementTag ("stats");
            closeElementTag (false);
            m_out.incIndent ();
            {
                emitStatsCount ("packages", item.getChildCount ());
                emitStatsCount ("classes", item.getAggregate (IItem.TOTAL_CLASS_COUNT));
                emitStatsCount ("methods", item.getAggregate (IItem.TOTAL_METHOD_COUNT));
                if (m_srcView && m_hasSrcFileInfo)
                {
                    emitStatsCount ("srcfiles", item.getAggregate (IItem.TOTAL_SRCFILE_COUNT));
                    if (m_hasLineNumberInfo)
                        emitStatsCount ("srclines", item.getAggregate (IItem.TOTAL_LINE_COUNT));
                }
            }
            m_out.decIndent ();
            eol ();
            endElement ("stats");
            eol ();
            openElementTag ("data");
            closeElementTag (false);
            m_out.incIndent ();
            {
                final ItemComparator childrenOrder = m_typeSortComparators [PackageItem.getTypeMetadata ().getTypeID ()];
                emitItem (item, childrenOrder);
            }
            m_out.decIndent ();
            eol ();
            endElement ("data");
            m_out.decIndent ();
            eol ();
            endElement ("report");            
        }
        catch (IOException ioe)
        {
            throw new EMMARuntimeException (IAppErrorCodes.REPORT_IO_FAILURE, ioe);
        }
        return ctx;
    }
    public Object visit (final PackageItem item, final Object ctx)
    {
        if (m_verbose) m_log.verbose ("  report: processing package [" + item.getName () + "] ...");
        try
        {
            final ItemComparator childrenOrder = m_typeSortComparators [m_srcView ? SrcFileItem.getTypeMetadata ().getTypeID () : ClassItem.getTypeMetadata ().getTypeID ()];
            emitItem (item, childrenOrder);
        }
        catch (IOException ioe)
        {
            throw new EMMARuntimeException (IAppErrorCodes.REPORT_IO_FAILURE, ioe);
        }
        return ctx;
    }
    public Object visit (final SrcFileItem item, final Object ctx)
    {
        try
        {
            final ItemComparator childrenOrder = m_typeSortComparators [ClassItem.getTypeMetadata ().getTypeID ()];
            emitItem (item, childrenOrder);
        }
        catch (IOException ioe)
        {
            throw new EMMARuntimeException (IAppErrorCodes.REPORT_IO_FAILURE, ioe);
        }
        return ctx;
    }
    public Object visit (final ClassItem item, final Object ctx)
    {
        try
        {
            final ItemComparator childrenOrder = m_typeSortComparators [MethodItem.getTypeMetadata ().getTypeID ()];
            emitItem (item, childrenOrder);
        }
        catch (IOException ioe)
        {
            throw new EMMARuntimeException (IAppErrorCodes.REPORT_IO_FAILURE, ioe);
        }
        return ctx;
    }
    public Object visit (final MethodItem item, final Object ctx)
    {
        try
        {
            emitItem (item, null);
        }
        catch (IOException ioe)
        {
            throw new EMMARuntimeException (IAppErrorCodes.REPORT_IO_FAILURE, ioe);
        }
        return ctx;
    }
    private static final class IndentingWriter extends BufferedWriter
    {
        public void newLine () throws IOException
        {
            m_state = 0;
            super.write (IConstants.EOL, 0, IConstants.EOL.length ());
        }
        public void write (final char [] cbuf, final int off, final int len) throws IOException
        {
            indent ();
            super.write (cbuf, off, len);
        }
        public void write (int c) throws IOException
        {
            indent ();
            super.write (c);
        }
        public void write (final String s, final int off, final int len) throws IOException
        {
            indent ();
            super.write (s, off, len);
        }
        IndentingWriter (final Writer out, final int buffer, final int indent)
        {
            super (out, buffer);
            m_indent = indent;
        }
        void incIndent (final int delta)
        {
            if (delta < 0) throw new IllegalArgumentException ("delta be non-negative: " + delta);
            m_indent += delta;
        }
        void incIndent ()
        {
            incIndent (INDENT_INCREMENT);
        }
        void decIndent (final int delta)
        {
            if (delta < 0) throw new IllegalArgumentException ("delta be non-negative: " + delta);
            if (delta > m_indent) throw new IllegalArgumentException ("delta = " + delta + ", current indent = " + m_indent);
            m_indent -= delta;
        }
        void decIndent ()
        {
            decIndent (INDENT_INCREMENT);
        }
        String getIndent ()
        {
            if (m_indent <= 0)
                return "";
            else
            {
                if ((m_sindent == null) || (m_sindent.length () < m_indent))
                {
                    final char [] ca = new char [m_indent];
                    for (int i = 0; i < m_indent; ++ i) ca [i] = ' ';
                    m_sindent = new String (ca);
                    return m_sindent;
                }
                else
                {
                    return m_sindent.substring (0, m_indent);
                }
            }
        }
        private void indent ()
            throws IOException
        {
            if (m_state == 0)
            {
                final String indent = getIndent ();
                super.write (indent, 0, indent.length ());
                m_state = 1;
            }
        }
        private int m_indent;
        private int m_state;
        private transient String m_sindent;
        private static final int INDENT_INCREMENT = 2;
    } 
    private void emitStatsCount (final String name, final int value)
        throws IOException
    {
        eol ();
        openElementTag (name);
        m_out.write (" value=\"" + value);
        m_out.write ('"');
        closeElementTag (true);
    } 
    private void emitItem (final IItem item, final ItemComparator childrenOrder)
        throws IOException
    {        
        final IItemMetadata metadata = item.getMetadata (); 
        final int [] columns = m_settings.getColumnOrder ();            
        final String tag = metadata.getTypeName ();
        eol ();
        {
            openElementTag (tag);
            m_out.write (" name=\"");
            m_out.write (Strings.HTMLEscape (item.getName ()));
            m_out.write ('"');
            closeElementTag (false);
        }
        eol ();
        m_out.incIndent ();       
        emitItemCoverage (item, columns);
        final boolean deeper = (childrenOrder != null) && (m_settings.getDepth () > metadata.getTypeID ()) && (item.getChildCount () > 0);
        if (deeper)
        {
            for (Iterator packages = item.getChildren (childrenOrder); packages.hasNext (); )
            {
                ((IItem) packages.next ()).accept (this, null);
            }
            eol ();
        }
        m_out.decIndent ();
        {
            endElement (tag);
        }
    }
    private void emitItemCoverage (final IItem item, final int [] columns)
        throws IOException
    {
        final StringBuffer buf = new StringBuffer (64);
        for (int c = 0, cLimit = columns.length; c < cLimit; ++ c)
        {
            final int attrID = columns [c];
            if (attrID != IItemAttribute.ATTRIBUTE_NAME_ID)
            {
                final IItemAttribute attr = item.getAttribute (attrID, m_settings.getUnitsType ());
                if (attr != null)
                {
                    openElementTag ("coverage");
                    m_out.write (" type=\"");
                    m_out.write (Strings.HTMLEscape (attr.getName ()));
                    m_out.write ("\" value=\"");
                    attr.format (item, buf);
                    m_out.write (Strings.HTMLEscape (buf.toString ()));
                    m_out.write ('"');
                    buf.setLength (0);
                    closeElementTag (true);
                    eol ();
                }
            }
        }
    }
    private void openElementTag (final String tag)
        throws IOException
    {
        m_out.write ('<');
        m_out.write (tag);
    }
    private void closeElementTag (final boolean simple)
        throws IOException
    {
        if (simple)
            m_out.write ("/>");
        else
            m_out.write ('>');
    }
    private void endElement (final String tag)
        throws IOException
    {
        m_out.write ("</");
        m_out.write (tag);
        m_out.write ('>');
    }
    private void eol ()
        throws IOException
    {
        m_out.newLine ();
    }
    private void close ()
    {
        if (m_out != null)
        {
            try
            {
                m_out.flush ();
                m_out.close ();
            }
            catch (IOException ioe)
            {
                throw new EMMARuntimeException (IAppErrorCodes.REPORT_IO_FAILURE, ioe);
            }
            finally
            {
                m_out = null;
            }
        }
    }
    private void openOutFile (final File file, final String encoding, final boolean mkdirs)
    {
        try
        {
            if (mkdirs)
            {
                final File parent = file.getParentFile ();
                if (parent != null) parent.mkdirs ();
            }
            m_out = new IndentingWriter (new OutputStreamWriter (new FileOutputStream (file), encoding), IO_BUF_SIZE, 0);
        }
        catch (UnsupportedEncodingException uee)
        {
            throw new EMMARuntimeException (uee);
        }
        catch (IOException fnfe) 
        {
            throw new EMMARuntimeException (fnfe);
        }
    }
    private IndentingWriter m_out;
    private static final String TYPE = "xml";    
    private static final int IO_BUF_SIZE = 64 * 1024;
} 
