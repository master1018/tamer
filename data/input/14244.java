public class HelpWriter extends HtmlDocletWriter {
    public HelpWriter(ConfigurationImpl configuration,
                      String filename) throws IOException {
        super(configuration, filename);
    }
    public static void generate(ConfigurationImpl configuration) {
        HelpWriter helpgen;
        String filename = "";
        try {
            filename = "help-doc.html";
            helpgen = new HelpWriter(configuration, filename);
            helpgen.generateHelpFile();
            helpgen.close();
        } catch (IOException exc) {
            configuration.standardmessage.error(
                        "doclet.exception_encountered",
                        exc.toString(), filename);
            throw new DocletAbortException();
        }
    }
    protected void generateHelpFile() {
        String title = configuration.getText("doclet.Window_Help_title");
        Content body = getBody(true, getWindowTitle(title));
        addTop(body);
        addNavLinks(true, body);
        addHelpFileContents(body);
        addNavLinks(false, body);
        addBottom(body);
        printHtmlDocument(null, true, body);
    }
    protected void addHelpFileContents(Content contentTree) {
        Content heading = HtmlTree.HEADING(HtmlConstants.TITLE_HEADING, false, HtmlStyle.title,
                getResource("doclet.Help_line_1"));
        Content div = HtmlTree.DIV(HtmlStyle.header, heading);
        Content line2 = HtmlTree.DIV(HtmlStyle.subTitle,
                getResource("doclet.Help_line_2"));
        div.addContent(line2);
        contentTree.addContent(div);
        HtmlTree ul = new HtmlTree(HtmlTag.UL);
        ul.addStyle(HtmlStyle.blockList);
        if (configuration.createoverview) {
            Content overviewHeading = HtmlTree.HEADING(HtmlConstants.CONTENT_HEADING,
                getResource("doclet.Overview"));
            Content liOverview = HtmlTree.LI(HtmlStyle.blockList, overviewHeading);
            Content line3 = getResource("doclet.Help_line_3",
                    getHyperLinkString("overview-summary.html",
                    configuration.getText("doclet.Overview")));
            Content overviewPara = HtmlTree.P(line3);
            liOverview.addContent(overviewPara);
            ul.addContent(liOverview);
        }
        Content packageHead = HtmlTree.HEADING(HtmlConstants.CONTENT_HEADING,
                getResource("doclet.Package"));
        Content liPackage = HtmlTree.LI(HtmlStyle.blockList, packageHead);
        Content line4 = getResource("doclet.Help_line_4");
        Content packagePara = HtmlTree.P(line4);
        liPackage.addContent(packagePara);
        HtmlTree ulPackage = new HtmlTree(HtmlTag.UL);
        ulPackage.addContent(HtmlTree.LI(
                getResource("doclet.Interfaces_Italic")));
        ulPackage.addContent(HtmlTree.LI(
                getResource("doclet.Classes")));
        ulPackage.addContent(HtmlTree.LI(
                getResource("doclet.Enums")));
        ulPackage.addContent(HtmlTree.LI(
                getResource("doclet.Exceptions")));
        ulPackage.addContent(HtmlTree.LI(
                getResource("doclet.Errors")));
        ulPackage.addContent(HtmlTree.LI(
                getResource("doclet.AnnotationTypes")));
        liPackage.addContent(ulPackage);
        ul.addContent(liPackage);
        Content classHead = HtmlTree.HEADING(HtmlConstants.CONTENT_HEADING,
                getResource("doclet.Help_line_5"));
        Content liClass = HtmlTree.LI(HtmlStyle.blockList, classHead);
        Content line6 = getResource("doclet.Help_line_6");
        Content classPara = HtmlTree.P(line6);
        liClass.addContent(classPara);
        HtmlTree ul1 = new HtmlTree(HtmlTag.UL);
        ul1.addContent(HtmlTree.LI(
                getResource("doclet.Help_line_7")));
        ul1.addContent(HtmlTree.LI(
                getResource("doclet.Help_line_8")));
        ul1.addContent(HtmlTree.LI(
                getResource("doclet.Help_line_9")));
        ul1.addContent(HtmlTree.LI(
                getResource("doclet.Help_line_10")));
        ul1.addContent(HtmlTree.LI(
                getResource("doclet.Help_line_11")));
        ul1.addContent(HtmlTree.LI(
                getResource("doclet.Help_line_12")));
        liClass.addContent(ul1);
        HtmlTree ul2 = new HtmlTree(HtmlTag.UL);
        ul2.addContent(HtmlTree.LI(
                getResource("doclet.Nested_Class_Summary")));
        ul2.addContent(HtmlTree.LI(
                getResource("doclet.Field_Summary")));
        ul2.addContent(HtmlTree.LI(
                getResource("doclet.Constructor_Summary")));
        ul2.addContent(HtmlTree.LI(
                getResource("doclet.Method_Summary")));
        liClass.addContent(ul2);
        HtmlTree ul3 = new HtmlTree(HtmlTag.UL);
        ul3.addContent(HtmlTree.LI(
                getResource("doclet.Field_Detail")));
        ul3.addContent(HtmlTree.LI(
                getResource("doclet.Constructor_Detail")));
        ul3.addContent(HtmlTree.LI(
                getResource("doclet.Method_Detail")));
        liClass.addContent(ul3);
        Content line13 = getResource("doclet.Help_line_13");
        Content para = HtmlTree.P(line13);
        liClass.addContent(para);
        ul.addContent(liClass);
        Content aHead = HtmlTree.HEADING(HtmlConstants.CONTENT_HEADING,
                getResource("doclet.AnnotationType"));
        Content liAnnotation = HtmlTree.LI(HtmlStyle.blockList, aHead);
        Content aline1 = getResource("doclet.Help_annotation_type_line_1");
        Content aPara = HtmlTree.P(aline1);
        liAnnotation.addContent(aPara);
        HtmlTree aul = new HtmlTree(HtmlTag.UL);
        aul.addContent(HtmlTree.LI(
                getResource("doclet.Help_annotation_type_line_2")));
        aul.addContent(HtmlTree.LI(
                getResource("doclet.Help_annotation_type_line_3")));
        aul.addContent(HtmlTree.LI(
                getResource("doclet.Annotation_Type_Required_Member_Summary")));
        aul.addContent(HtmlTree.LI(
                getResource("doclet.Annotation_Type_Optional_Member_Summary")));
        aul.addContent(HtmlTree.LI(
                getResource("doclet.Annotation_Type_Member_Detail")));
        liAnnotation.addContent(aul);
        ul.addContent(liAnnotation);
        Content enumHead = HtmlTree.HEADING(HtmlConstants.CONTENT_HEADING,
                getResource("doclet.Enum"));
        Content liEnum = HtmlTree.LI(HtmlStyle.blockList, enumHead);
        Content eline1 = getResource("doclet.Help_enum_line_1");
        Content enumPara = HtmlTree.P(eline1);
        liEnum.addContent(enumPara);
        HtmlTree eul = new HtmlTree(HtmlTag.UL);
        eul.addContent(HtmlTree.LI(
                getResource("doclet.Help_enum_line_2")));
        eul.addContent(HtmlTree.LI(
                getResource("doclet.Help_enum_line_3")));
        eul.addContent(HtmlTree.LI(
                getResource("doclet.Enum_Constant_Summary")));
        eul.addContent(HtmlTree.LI(
                getResource("doclet.Enum_Constant_Detail")));
        liEnum.addContent(eul);
        ul.addContent(liEnum);
        if (configuration.classuse) {
            Content useHead = HtmlTree.HEADING(HtmlConstants.CONTENT_HEADING,
                    getResource("doclet.Help_line_14"));
            Content liUse = HtmlTree.LI(HtmlStyle.blockList, useHead);
            Content line15 = getResource("doclet.Help_line_15");
            Content usePara = HtmlTree.P(line15);
            liUse.addContent(usePara);
            ul.addContent(liUse);
        }
        if (configuration.createtree) {
            Content treeHead = HtmlTree.HEADING(HtmlConstants.CONTENT_HEADING,
                    getResource("doclet.Help_line_16"));
            Content liTree = HtmlTree.LI(HtmlStyle.blockList, treeHead);
            Content line17 = getResource("doclet.Help_line_17_with_tree_link",
                    getHyperLinkString("overview-tree.html",
                    configuration.getText("doclet.Class_Hierarchy")));
            Content treePara = HtmlTree.P(line17);
            liTree.addContent(treePara);
            HtmlTree tul = new HtmlTree(HtmlTag.UL);
            tul.addContent(HtmlTree.LI(
                    getResource("doclet.Help_line_18")));
            tul.addContent(HtmlTree.LI(
                    getResource("doclet.Help_line_19")));
            liTree.addContent(tul);
            ul.addContent(liTree);
        }
        if (!(configuration.nodeprecatedlist ||
                  configuration.nodeprecated)) {
            Content dHead = HtmlTree.HEADING(HtmlConstants.CONTENT_HEADING,
                    getResource("doclet.Deprecated_API"));
            Content liDeprecated = HtmlTree.LI(HtmlStyle.blockList, dHead);
            Content line20 = getResource("doclet.Help_line_20_with_deprecated_api_link",
                    getHyperLinkString("deprecated-list.html",
                    configuration.getText("doclet.Deprecated_API")));
            Content dPara = HtmlTree.P(line20);
            liDeprecated.addContent(dPara);
            ul.addContent(liDeprecated);
        }
        if (configuration.createindex) {
            String indexlink;
            if (configuration.splitindex) {
                indexlink = getHyperLinkString("index-files/index-1.html",
                        configuration.getText("doclet.Index"));
            } else {
                indexlink = getHyperLinkString("index-all.html",
                        configuration.getText("doclet.Index"));
            }
            Content indexHead = HtmlTree.HEADING(HtmlConstants.CONTENT_HEADING,
                    getResource("doclet.Help_line_21"));
            Content liIndex = HtmlTree.LI(HtmlStyle.blockList, indexHead);
            Content line22 = getResource("doclet.Help_line_22", indexlink);
            Content indexPara = HtmlTree.P(line22);
            liIndex.addContent(indexPara);
            ul.addContent(liIndex);
        }
        Content prevHead = HtmlTree.HEADING(HtmlConstants.CONTENT_HEADING,
                getResource("doclet.Help_line_23"));
        Content liPrev = HtmlTree.LI(HtmlStyle.blockList, prevHead);
        Content line24 = getResource("doclet.Help_line_24");
        Content prevPara = HtmlTree.P(line24);
        liPrev.addContent(prevPara);
        ul.addContent(liPrev);
        Content frameHead = HtmlTree.HEADING(HtmlConstants.CONTENT_HEADING,
                getResource("doclet.Help_line_25"));
        Content liFrame = HtmlTree.LI(HtmlStyle.blockList, frameHead);
        Content line26 = getResource("doclet.Help_line_26");
        Content framePara = HtmlTree.P(line26);
        liFrame.addContent(framePara);
        ul.addContent(liFrame);
        Content allclassesHead = HtmlTree.HEADING(HtmlConstants.CONTENT_HEADING,
                getResource("doclet.All_Classes"));
        Content liAllClasses = HtmlTree.LI(HtmlStyle.blockList, allclassesHead);
        Content line27 = getResource("doclet.Help_line_27",
                getHyperLinkString("allclasses-noframe.html",
                configuration.getText("doclet.All_Classes")));
        Content allclassesPara = HtmlTree.P(line27);
        liAllClasses.addContent(allclassesPara);
        ul.addContent(liAllClasses);
        Content sHead = HtmlTree.HEADING(HtmlConstants.CONTENT_HEADING,
                getResource("doclet.Serialized_Form"));
        Content liSerial = HtmlTree.LI(HtmlStyle.blockList, sHead);
        Content line28 = getResource("doclet.Help_line_28");
        Content serialPara = HtmlTree.P(line28);
        liSerial.addContent(serialPara);
        ul.addContent(liSerial);
        Content constHead = HtmlTree.HEADING(HtmlConstants.CONTENT_HEADING,
                getResource("doclet.Constants_Summary"));
        Content liConst = HtmlTree.LI(HtmlStyle.blockList, constHead);
        Content line29 = getResource("doclet.Help_line_29");
        Content constPara = HtmlTree.P(line29);
        liConst.addContent(constPara);
        ul.addContent(liConst);
        Content divContent = HtmlTree.DIV(HtmlStyle.contentContainer, ul);
        Content line30 = HtmlTree.EM(getResource("doclet.Help_line_30"));
        divContent.addContent(line30);
        contentTree.addContent(divContent);
    }
    protected Content getNavLinkHelp() {
        Content li = HtmlTree.LI(HtmlStyle.navBarCell1Rev, helpLabel);
        return li;
    }
}
