public class DeprecatedListWriter extends SubWriterHolderWriter {
    private static final String[] ANCHORS = new String[] {
        "package", "interface", "class", "enum", "exception", "error",
        "annotation_type", "field", "method", "constructor", "enum_constant",
        "annotation_type_member"
    };
    private static final String[] HEADING_KEYS = new String[] {
        "doclet.Deprecated_Packages", "doclet.Deprecated_Interfaces",
        "doclet.Deprecated_Classes", "doclet.Deprecated_Enums",
        "doclet.Deprecated_Exceptions", "doclet.Deprecated_Errors",
        "doclet.Deprecated_Annotation_Types",
        "doclet.Deprecated_Fields",
        "doclet.Deprecated_Methods", "doclet.Deprecated_Constructors",
        "doclet.Deprecated_Enum_Constants",
        "doclet.Deprecated_Annotation_Type_Members"
    };
    private static final String[] SUMMARY_KEYS = new String[] {
        "doclet.deprecated_packages", "doclet.deprecated_interfaces",
        "doclet.deprecated_classes", "doclet.deprecated_enums",
        "doclet.deprecated_exceptions", "doclet.deprecated_errors",
        "doclet.deprecated_annotation_types",
        "doclet.deprecated_fields",
        "doclet.deprecated_methods", "doclet.deprecated_constructors",
        "doclet.deprecated_enum_constants",
        "doclet.deprecated_annotation_type_members"
    };
    private static final String[] HEADER_KEYS = new String[] {
        "doclet.Package", "doclet.Interface", "doclet.Class",
        "doclet.Enum", "doclet.Exceptions",
        "doclet.Errors",
        "doclet.AnnotationType",
        "doclet.Field",
        "doclet.Method", "doclet.Constructor",
        "doclet.Enum_Constant",
        "doclet.Annotation_Type_Member"
    };
    private AbstractMemberWriter[] writers;
    private ConfigurationImpl configuration;
    public DeprecatedListWriter(ConfigurationImpl configuration,
                                String filename) throws IOException {
        super(configuration, filename);
        this.configuration = configuration;
        NestedClassWriterImpl classW = new NestedClassWriterImpl(this);
        writers = new AbstractMemberWriter[]
            {classW, classW, classW, classW, classW, classW,
            new FieldWriterImpl(this),
            new MethodWriterImpl(this),
            new ConstructorWriterImpl(this),
            new EnumConstantWriterImpl(this),
            new AnnotationTypeOptionalMemberWriterImpl(this, null)};
    }
    public static void generate(ConfigurationImpl configuration) {
        String filename = "deprecated-list.html";
        try {
            DeprecatedListWriter depr =
                   new DeprecatedListWriter(configuration, filename);
            depr.generateDeprecatedListFile(
                   new DeprecatedAPIListBuilder(configuration));
            depr.close();
        } catch (IOException exc) {
            configuration.standardmessage.error(
                        "doclet.exception_encountered",
                        exc.toString(), filename);
            throw new DocletAbortException();
        }
    }
    protected void generateDeprecatedListFile(DeprecatedAPIListBuilder deprapi)
            throws IOException {
        Content body = getHeader();
        body.addContent(getContentsList(deprapi));
        String memberTableSummary;
        String[] memberTableHeader = new String[1];
        HtmlTree div = new HtmlTree(HtmlTag.DIV);
        div.addStyle(HtmlStyle.contentContainer);
        for (int i = 0; i < DeprecatedAPIListBuilder.NUM_TYPES; i++) {
            if (deprapi.hasDocumentation(i)) {
                addAnchor(deprapi, i, div);
                memberTableSummary =
                        configuration.getText("doclet.Member_Table_Summary",
                        configuration.getText(HEADING_KEYS[i]),
                        configuration.getText(SUMMARY_KEYS[i]));
                memberTableHeader[0] = configuration.getText("doclet.0_and_1",
                        configuration.getText(HEADER_KEYS[i]),
                        configuration.getText("doclet.Description"));
                if (i == DeprecatedAPIListBuilder.PACKAGE)
                    addPackageDeprecatedAPI(deprapi.getList(i),
                            HEADING_KEYS[i], memberTableSummary, memberTableHeader, div);
                else
                    writers[i - 1].addDeprecatedAPI(deprapi.getList(i),
                            HEADING_KEYS[i], memberTableSummary, memberTableHeader, div);
            }
        }
        body.addContent(div);
        addNavLinks(false, body);
        addBottom(body);
        printHtmlDocument(null, true, body);
    }
    private void addIndexLink(DeprecatedAPIListBuilder builder,
            int type, Content contentTree) {
        if (builder.hasDocumentation(type)) {
            Content li = HtmlTree.LI(getHyperLink("#" + ANCHORS[type],
                    getResource(HEADING_KEYS[type])));
            contentTree.addContent(li);
        }
    }
    public Content getContentsList(DeprecatedAPIListBuilder deprapi) {
        Content headContent = getResource("doclet.Deprecated_API");
        Content heading = HtmlTree.HEADING(HtmlConstants.TITLE_HEADING, true,
                HtmlStyle.title, headContent);
        Content div = HtmlTree.DIV(HtmlStyle.header, heading);
        Content headingContent = getResource("doclet.Contents");
        div.addContent(HtmlTree.HEADING(HtmlConstants.CONTENT_HEADING, true,
                headingContent));
        Content ul = new HtmlTree(HtmlTag.UL);
        for (int i = 0; i < DeprecatedAPIListBuilder.NUM_TYPES; i++) {
            addIndexLink(deprapi, i, ul);
        }
        div.addContent(ul);
        return div;
    }
    private void addAnchor(DeprecatedAPIListBuilder builder, int type, Content htmlTree) {
        if (builder.hasDocumentation(type)) {
            htmlTree.addContent(getMarkerAnchor(ANCHORS[type]));
        }
    }
    public Content getHeader() {
        String title = configuration.getText("doclet.Window_Deprecated_List");
        Content bodyTree = getBody(true, getWindowTitle(title));
        addTop(bodyTree);
        addNavLinks(true, bodyTree);
        return bodyTree;
    }
    protected Content getNavLinkDeprecated() {
        Content li = HtmlTree.LI(HtmlStyle.navBarCell1Rev, deprecatedLabel);
        return li;
    }
}
