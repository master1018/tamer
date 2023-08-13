public class TestNewLanguageFeatures extends JavadocTester {
    private static final String BUG_ID = "4789689-4905985-4927164-4827184-4993906";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-use", "-source", "1.5", "-sourcepath", SRC_DIR, "pkg", "pkg1", "pkg2"
    };
    private static final String[][] TEST =
        {
            {BUG_ID + FS + "pkg" + FS + "Coin.html", "Enum Coin</h2>"},
            {BUG_ID + FS + "pkg" + FS + "Coin.html", "<pre>public enum " +
                     "<span class=\"strong\">Coin</span>" + NL +
                     "extends java.lang.Enum&lt;<a href=\"../pkg/Coin.html\" " +
                     "title=\"enum in pkg\">Coin</a>&gt;</pre>"
            },
            {BUG_ID + FS + "pkg" + FS + "Coin.html", "<caption><span>Enum Constants" +
                     "</span><span class=\"tabEnd\">&nbsp;</span></caption>"},
            {BUG_ID + FS + "pkg" + FS + "Coin.html",
                "<strong><a href=\"../pkg/Coin.html#Dime\">Dime</a></strong>"},
            {BUG_ID + FS + "pkg" + FS + "Coin.html",
                "Returns an array containing the constants of this enum type,"},
            {BUG_ID + FS + "pkg" + FS + "Coin.html",
                "Returns the enum constant of this type with the specified name"},
            {BUG_ID + FS + "pkg" + FS + "Coin.html", "for (Coin c : Coin.values())"},
            {BUG_ID + FS + "pkg" + FS + "Coin.html", "Overloaded valueOf() method has correct documentation."},
            {BUG_ID + FS + "pkg" + FS + "Coin.html", "Overloaded values method  has correct documentation."},
            {BUG_ID + FS + "pkg" + FS + "TypeParameters.html",
                "Class TypeParameters&lt;E&gt;</h2>"},
            {BUG_ID + FS + "pkg" + FS + "TypeParameters.html",
                "<dt><span class=\"strong\">Type Parameters:</span></dt><dd><code>E</code> - " +
                "the type parameter for this class."},
            {BUG_ID + FS + "pkg" + FS + "TypeParameters.html",
                "<dl><dt><span class=\"strong\">See Also:</span></dt><dd>" +
                "<a href=\"../pkg/TypeParameters.html\" title=\"class in pkg\">" +
                "<code>TypeParameters</code></a></dd></dl>"},
            {BUG_ID + FS + "pkg" + FS + "TypeParameters.html",
                "(<a href=\"../pkg/TypeParameters.html\" title=\"type " +
                    "parameter in TypeParameters\">E</a>&nbsp;param)"},
            {BUG_ID + FS + "pkg" + FS + "TypeParameters.html",
                "<span class=\"strong\">Type Parameters:</span></dt><dd><code>T</code> - This is the first " +
                    "type parameter.</dd><dd><code>V</code> - This is the second type " +
                    "parameter."},
            {BUG_ID + FS + "pkg" + FS + "TypeParameters.html",
                "public&nbsp;&lt;T extends java.util.List,V&gt;&nbsp;" +
                "java.lang.String[]&nbsp;methodThatHasTypeParameters"},
            {BUG_ID + FS + "pkg" + FS + "Wildcards.html",
                "<a href=\"../pkg/TypeParameters.html\" title=\"class in pkg\">" +
                "TypeParameters</a>&lt;? super java.lang.String&gt;&nbsp;a"},
            {BUG_ID + FS + "pkg" + FS + "Wildcards.html",
                "<a href=\"../pkg/TypeParameters.html\" title=\"class in pkg\">" +
                "TypeParameters</a>&lt;? extends java.lang.StringBuffer&gt;&nbsp;b"},
            {BUG_ID + FS + "pkg" + FS + "Wildcards.html",
                "<a href=\"../pkg/TypeParameters.html\" title=\"class in pkg\">" +
                    "TypeParameters</a>&nbsp;c"},
            {WARNING_OUTPUT, "warning - @param argument " +
                "\"<BadClassTypeParam>\" is not a type parameter name."},
            {WARNING_OUTPUT, "warning - @param argument " +
                "\"<BadMethodTypeParam>\" is not a type parameter name."},
            {BUG_ID + FS + "pkg" + FS + "TypeParameterSubClass.html",
                "<pre>public class <span class=\"strong\">TypeParameterSubClass&lt;T extends " +
                "java.lang.String&gt;</span>" + NL + "extends " +
                "<a href=\"../pkg/TypeParameterSuperClass.html\" title=\"class in pkg\">" +
                "TypeParameterSuperClass</a>&lt;T&gt;</pre>"},
            {BUG_ID + FS + "pkg" + FS + "TypeParameters.html",
                "<dl>" + NL + "<dt>All Implemented Interfaces:</dt>" + NL +
                "<dd><a href=\"../pkg/SubInterface.html\" title=\"interface in pkg\">" +
                "SubInterface</a>&lt;E&gt;, <a href=\"../pkg/SuperInterface.html\" " +
                "title=\"interface in pkg\">SuperInterface</a>&lt;E&gt;</dd>" + NL +
                "</dl>"},
            {BUG_ID + FS + "pkg" + FS + "SuperInterface.html",
                "<dl>" + NL + "<dt>All Known Subinterfaces:</dt>" + NL +
                "<dd><a href=\"../pkg/SubInterface.html\" title=\"interface in pkg\">" +
                "SubInterface</a>&lt;V&gt;</dd>" + NL + "</dl>"},
            {BUG_ID + FS + "pkg" + FS + "SubInterface.html",
                "<dl>" + NL + "<dt>All Superinterfaces:</dt>" + NL +
                "<dd><a href=\"../pkg/SuperInterface.html\" title=\"interface in pkg\">" +
                "SuperInterface</a>&lt;V&gt;</dd>" + NL + "</dl>"},
            {BUG_ID + FS + "pkg" + FS + "VarArgs.html", "(int...&nbsp;i)"},
            {BUG_ID + FS + "pkg" + FS + "VarArgs.html", "(int[][]...&nbsp;i)"},
            {BUG_ID + FS + "pkg" + FS + "VarArgs.html", "(int[]...)"},
            {BUG_ID + FS + "pkg" + FS + "VarArgs.html",
                "<a href=\"../pkg/TypeParameters.html\" title=\"class in pkg\">" +
                "TypeParameters</a>...&nbsp;t"},
            {BUG_ID + FS + "pkg" + FS + "AnnotationType.html",
                "<li>Summary:&nbsp;</li>" + NL +
                "<li><a href=\"#annotation_type_required_element_summary\">" +
                "Required</a>&nbsp;|&nbsp;</li>" + NL + "<li>" +
                "<a href=\"#annotation_type_optional_element_summary\">Optional</a></li>"},
            {BUG_ID + FS + "pkg" + FS + "AnnotationType.html",
                "<li>Detail:&nbsp;</li>" + NL +
                "<li><a href=\"#annotation_type_element_detail\">Element</a></li>"},
            {BUG_ID + FS + "pkg" + FS + "AnnotationType.html",
                "Annotation Type AnnotationType</h2>"},
            {BUG_ID + FS + "pkg" + FS + "AnnotationType.html",
                "public @interface <span class=\"strong\">AnnotationType</span>"},
            {BUG_ID + FS + "pkg" + FS + "AnnotationType.html",
                "<h3>Required Element Summary</h3>"},
            {BUG_ID + FS + "pkg" + FS + "AnnotationType.html",
                "<h3>Optional Element Summary</h3>"},
            {BUG_ID + FS + "pkg" + FS + "AnnotationType.html",
                "Element Detail"},
            {BUG_ID + FS + "pkg" + FS + "AnnotationType.html",
                "<dl>" + NL + "<dt>Default:</dt>" + NL + "<dd>\"unknown\"</dd>" + NL +
                "</dl>"},
            {BUG_ID + FS + "pkg" + FS + "package-summary.html",
                "<a href=\"../pkg/AnnotationType.html\" title=\"annotation in pkg\">@AnnotationType</a>(<a href=\"../pkg/AnnotationType.html#optional()\">optional</a>=\"Package Annotation\"," + NL +
                "                <a href=\"../pkg/AnnotationType.html#required()\">required</a>=1994)"},
            {BUG_ID + FS + "pkg" + FS + "AnnotationTypeUsage.html",
                "<pre><a href=\"../pkg/AnnotationType.html\" " +
                "title=\"annotation in pkg\">@AnnotationType</a>(" +
                "<a href=\"../pkg/AnnotationType.html#optional()\">optional</a>" +
                "=\"Class Annotation\"," + NL +
                "                <a href=\"../pkg/AnnotationType.html#required()\">" +
                "required</a>=1994)" + NL + "public class <span class=\"strong\">" +
                "AnnotationTypeUsage</span>" + NL + "extends java.lang.Object</pre>"},
            {BUG_ID + FS + "pkg" + FS + "AnnotationTypeUsage.html",
                "<pre><a href=\"../pkg/AnnotationType.html\" " +
                "title=\"annotation in pkg\">@AnnotationType</a>(" +
                "<a href=\"../pkg/AnnotationType.html#optional()\">optional</a>" +
                "=\"Field Annotation\"," + NL +
                "                <a href=\"../pkg/AnnotationType.html#required()\">" +
                "required</a>=1994)" + NL + "public&nbsp;int field</pre>"},
            {BUG_ID + FS + "pkg" + FS + "AnnotationTypeUsage.html",
                "<pre><a href=\"../pkg/AnnotationType.html\" " +
                "title=\"annotation in pkg\">@AnnotationType</a>(" +
                "<a href=\"../pkg/AnnotationType.html#optional()\">optional</a>" +
                "=\"Constructor Annotation\"," + NL +
                "                <a href=\"../pkg/AnnotationType.html#required()\">" +
                "required</a>=1994)" + NL + "public&nbsp;AnnotationTypeUsage()</pre>"},
            {BUG_ID + FS + "pkg" + FS + "AnnotationTypeUsage.html",
                "<pre><a href=\"../pkg/AnnotationType.html\" " +
                "title=\"annotation in pkg\">@AnnotationType</a>(" +
                "<a href=\"../pkg/AnnotationType.html#optional()\">optional</a>" +
                "=\"Method Annotation\"," + NL +
                "                <a href=\"../pkg/AnnotationType.html#required()\">" +
                "required</a>=1994)" + NL + "public&nbsp;void&nbsp;method()</pre>"},
            {BUG_ID + FS + "pkg" + FS + "AnnotationTypeUsage.html",
                "<pre>public&nbsp;void&nbsp;methodWithParams(" +
                "<a href=\"../pkg/AnnotationType.html\" title=\"annotation in pkg\">" +
                "@AnnotationType</a>(<a href=\"../pkg/AnnotationType.html#optional()\">" +
                "optional</a>=\"Parameter Annotation\",<a " +
                "href=\"../pkg/AnnotationType.html#required()\">required</a>=1994)" + NL +
                "                    int&nbsp;documented," + NL +
                "                    int&nbsp;undocmented)</pre>"},
            {BUG_ID + FS + "pkg" + FS + "AnnotationTypeUsage.html",
                "<pre>public&nbsp;AnnotationTypeUsage(<a " +
                "href=\"../pkg/AnnotationType.html\" title=\"annotation in pkg\">" +
                "@AnnotationType</a>(<a href=\"../pkg/AnnotationType.html#optional()\">" +
                "optional</a>=\"Constructor Param Annotation\",<a " +
                "href=\"../pkg/AnnotationType.html#required()\">required</a>=1994)" + NL +
                "                   int&nbsp;documented," + NL +
                "                   int&nbsp;undocmented)</pre>"},
            {BUG_ID + FS + "pkg1" + FS + "B.html",
                "<a href=\"../pkg1/A.html#d()\">d</a>=3.14,"},
            {BUG_ID + FS + "pkg1" + FS + "B.html",
                "<a href=\"../pkg1/A.html#d()\">d</a>=3.14,"},
            {BUG_ID + FS + "pkg1" + FS + "B.html",
                "<a href=\"../pkg1/A.html#b()\">b</a>=true,"},
            {BUG_ID + FS + "pkg1" + FS + "B.html",
                "<a href=\"../pkg1/A.html#s()\">s</a>=\"sigh\","},
            {BUG_ID + FS + "pkg1" + FS + "B.html",
                "<a href=\"../pkg1/A.html#c()\">c</a>=<a href=\"../pkg2/Foo.html\" title=\"class in pkg2\">Foo.class</a>,"},
            {BUG_ID + FS + "pkg1" + FS + "B.html",
                "<a href=\"../pkg1/A.html#w()\">w</a>=<a href=\"../pkg/TypeParameterSubClass.html\" title=\"class in pkg\">TypeParameterSubClass.class</a>,"},
            {BUG_ID + FS + "pkg1" + FS + "B.html",
                "<a href=\"../pkg1/A.html#e()\">e</a>=<a href=\"../pkg/Coin.html#Penny\">Penny</a>,"},
            {BUG_ID + FS + "pkg1" + FS + "B.html",
                "<a href=\"../pkg1/A.html#a()\">a</a>=<a href=\"../pkg/AnnotationType.html\" title=\"annotation in pkg\">@AnnotationType</a>(<a href=\"../pkg/AnnotationType.html#optional()\">optional</a>=\"foo\",<a href=\"../pkg/AnnotationType.html#required()\">required</a>=1994),"},
            {BUG_ID + FS + "pkg1" + FS + "B.html",
                "<a href=\"../pkg1/A.html#sa()\">sa</a>={\"up\",\"down\"},"},
            {BUG_ID + FS + "pkg1" + FS + "B.html",
                "<a href=\"../pkg1/A.html#primitiveClassTest()\">primitiveClassTest</a>=boolean.class,"},
            {BUG_ID + FS + "pkg1" + FS + "B.html",
                "<pre><a href=\"../pkg1/A.html\" title=\"annotation in pkg1\">@A</a>"},
            {BUG_ID + FS + "pkg1" + FS + "B.html",
                "public interface <span class=\"strong\">B</span></pre>"},
            {BUG_ID + FS + "pkg" + FS + "MultiTypeParameters.html",
                "public&nbsp;&lt;T extends java.lang.Number & java.lang.Runnable&gt;&nbsp;T&nbsp;foo(T&nbsp;t)"},
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "Foo.html",
                     "<caption><span>Classes in <a href=\"../../pkg2/" +
                     "package-summary.html\">pkg2</a> with type parameters of " +
                     "type <a href=\"../../pkg2/Foo.html\" title=\"class in pkg2\">" +
                     "Foo</a></span><span class=\"tabEnd\">&nbsp;</span></caption>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "Foo.html",
                     "<td class=\"colLast\"><code><strong><a href=\"../../pkg2/ClassUseTest1.html\" " +
                     "title=\"class in pkg2\">ClassUseTest1</a>&lt;T extends " +
                     "<a href=\"../../pkg2/Foo.html\" title=\"class in pkg2\">Foo" +
                     "</a> & <a href=\"../../pkg2/Foo2.html\" title=\"interface in pkg2\">" +
                     "Foo2</a>&gt;</strong></code>&nbsp;</td>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "Foo.html",
                     "<caption><span>Methods in <a href=\"../../pkg2/" +
                     "package-summary.html\">pkg2</a> with type parameters of " +
                     "type <a href=\"../../pkg2/Foo.html\" title=\"class in " +
                     "pkg2\">Foo</a></span><span class=\"tabEnd\">&nbsp;</span></caption>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "Foo.html",
                     "<td class=\"colLast\"><span class=\"strong\">ClassUseTest1." +
                     "</span><code><strong><a href=\"../../pkg2/" +
                     "ClassUseTest1.html#method(T)\">method</a></strong>" +
                     "(T&nbsp;t)</code>&nbsp;</td>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "Foo.html",
                     "<caption><span>Fields in <a href=\"../../pkg2/" +
                     "package-summary.html\">pkg2</a> with type parameters of " +
                     "type <a href=\"../../pkg2/Foo.html\" title=\"class in pkg2\">" +
                     "Foo</a></span><span class=\"tabEnd\">&nbsp;</span></caption>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "Foo.html",
                     "td class=\"colFirst\"><code><a href=\"../../pkg2/" +
                     "ParamTest.html\" title=\"class in pkg2\">ParamTest</a>" +
                     "&lt;<a href=\"../../pkg2/Foo.html\" title=\"class in pkg2\"" +
                     ">Foo</a>&gt;</code></td>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "ParamTest.html",
                     "<caption><span>Fields in <a href=\"../../pkg2/" +
                     "package-summary.html\">pkg2</a> declared as <a href=\"../" +
                     "../pkg2/ParamTest.html\" title=\"class in pkg2\">ParamTest" +
                     "</a></span><span class=\"tabEnd\">&nbsp;</span></caption>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "ParamTest.html",
                     "<td class=\"colFirst\"><code><a href=\"../../pkg2/" +
                     "ParamTest.html\" title=\"class in pkg2\">ParamTest</a>&lt;<a " +
                     "href=\"../../pkg2/Foo.html\" title=\"class in pkg2\">Foo</a" +
                     ">&gt;</code></td>"
            },
           {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "Foo2.html",
                    "<caption><span>Classes in <a href=\"../../pkg2/" +
                    "package-summary.html\">pkg2</a> with type parameters of " +
                    "type <a href=\"../../pkg2/Foo2.html\" title=\"interface " +
                    "in pkg2\">Foo2</a></span><span class=\"tabEnd\">&nbsp;" +
                    "</span></caption>"
           },
           {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "Foo2.html",
                    "<td class=\"colLast\"><code><strong><a href=\"../../pkg2/ClassUseTest1.html\" " +
                     "title=\"class in pkg2\">ClassUseTest1</a>&lt;T extends " +
                     "<a href=\"../../pkg2/Foo.html\" title=\"class in pkg2\">Foo" +
                     "</a> & <a href=\"../../pkg2/Foo2.html\" title=\"interface in pkg2\">" +
                     "Foo2</a>&gt;</strong></code>&nbsp;</td>"
           },
           {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "Foo2.html",
                    "<caption><span>Methods in <a href=\"../../pkg2/" +
                    "package-summary.html\">pkg2</a> with type parameters of " +
                    "type <a href=\"../../pkg2/Foo2.html\" title=\"interface " +
                    "in pkg2\">Foo2</a></span><span class=\"tabEnd\">&nbsp;" +
                    "</span></caption>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "Foo2.html",
                     "<td class=\"colLast\"><span class=\"strong\">" +
                     "ClassUseTest1.</span><code><strong><a href=\"../../" +
                     "pkg2/ClassUseTest1.html#method(T)\">method</a></strong>" +
                     "(T&nbsp;t)</code>&nbsp;</td>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "ParamTest.html",
                     "<caption><span>Classes in <a href=\"../../pkg2/" +
                     "package-summary.html\">pkg2</a> with type parameters of " +
                     "type <a href=\"../../pkg2/ParamTest.html\" title=\"class " +
                     "in pkg2\">ParamTest</a></span><span class=\"tabEnd\">" +
                     "&nbsp;</span></caption>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "ParamTest.html",
                     "<td class=\"colLast\"><code><strong><a href=\"../../pkg2/ClassUseTest2.html\" " +
                     "title=\"class in pkg2\">ClassUseTest2</a>&lt;T extends " +
                     "<a href=\"../../pkg2/ParamTest.html\" title=\"class in pkg2\">" +
                     "ParamTest</a>&lt;<a href=\"../../pkg2/Foo3.html\" title=\"class in pkg2\">" +
                     "Foo3</a>&gt;&gt;</strong></code>&nbsp;</td>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "ParamTest.html",
                     "<caption><span>Methods in <a href=\"../../pkg2/" +
                     "package-summary.html\">pkg2</a> with type parameters of " +
                     "type <a href=\"../../pkg2/ParamTest.html\" title=\"class " +
                     "in pkg2\">ParamTest</a></span><span class=\"tabEnd\">" +
                     "&nbsp;</span></caption>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "ParamTest.html",
                     "<td class=\"colLast\"><span class=\"strong\">ClassUseTest2." +
                     "</span><code><strong><a href=\"../../pkg2/" +
                     "ClassUseTest2.html#method(T)\">method</a></strong>" +
                     "(T&nbsp;t)</code>&nbsp;</td>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "ParamTest.html",
                     "<caption><span>Fields in <a href=\"../../pkg2/" +
                     "package-summary.html\">pkg2</a> declared as <a href=\"../" +
                     "../pkg2/ParamTest.html\" title=\"class in pkg2\">ParamTest" +
                     "</a></span><span class=\"tabEnd\">&nbsp;</span></caption>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "ParamTest.html",
                     "<td class=\"colFirst\"><code><a href=\"../../pkg2/" +
                     "ParamTest.html\" title=\"class in pkg2\">ParamTest</a>" +
                     "&lt;<a href=\"../../pkg2/Foo.html\" title=\"class in pkg2\">" +
                     "Foo</a>&gt;</code></td>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "ParamTest.html",
                     "<caption><span>Methods in <a href=\"../../pkg2/" +
                     "package-summary.html\">pkg2</a> with type parameters of " +
                     "type <a href=\"../../pkg2/ParamTest.html\" title=\"class " +
                     "in pkg2\">ParamTest</a></span><span class=\"tabEnd\">" +
                     "&nbsp;</span></caption>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "ParamTest.html",
                     "<td class=\"colFirst\"><code>&lt;T extends <a href=\"../" +
                     "../pkg2/ParamTest.html\" title=\"class in pkg2\">ParamTest" +
                     "</a>&lt;<a href=\"../../pkg2/Foo3.html\" title=\"class in " +
                     "pkg2\">Foo3</a>&gt;&gt;&nbsp;<br><a href=\"../../pkg2/" +
                     "ParamTest.html\" title=\"class in pkg2\">ParamTest</a>" +
                     "&lt;<a href=\"../../pkg2/Foo3.html\" title=\"class in " +
                     "pkg2\">Foo3</a>&gt;</code></td>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "Foo3.html",
                     "<caption><span>Classes in <a href=\"../../pkg2/" +
                     "package-summary.html\">pkg2</a> with type parameters of " +
                     "type <a href=\"../../pkg2/Foo3.html\" title=\"class in pkg2\">" +
                     "Foo3</a></span><span class=\"tabEnd\">&nbsp;</span></caption>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "Foo3.html",
                     "<td class=\"colLast\"><code><strong><a href=\"../../pkg2/ClassUseTest2.html\" " +
                     "title=\"class in pkg2\">ClassUseTest2</a>&lt;T extends " +
                     "<a href=\"../../pkg2/ParamTest.html\" title=\"class in pkg2\">" +
                     "ParamTest</a>&lt;<a href=\"../../pkg2/Foo3.html\" title=\"class in pkg2\">" +
                     "Foo3</a>&gt;&gt;</strong></code>&nbsp;</td>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "Foo3.html",
                     "<caption><span>Methods in <a href=\"../../pkg2/" +
                     "package-summary.html\">pkg2</a> with type parameters of " +
                     "type <a href=\"../../pkg2/Foo3.html\" title=\"class in " +
                     "pkg2\">Foo3</a></span><span class=\"tabEnd\">&nbsp;" +
                     "</span></caption>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "Foo3.html",
                     "<td class=\"colLast\"><span class=\"strong\">ClassUseTest2." +
                     "</span><code><strong><a href=\"../../pkg2/" +
                     "ClassUseTest2.html#method(T)\">method</a></strong>" +
                     "(T&nbsp;t)</code>&nbsp;</td>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "Foo3.html",
                     "<caption><span>Methods in <a href=\"../../pkg2/" +
                     "package-summary.html\">pkg2</a> that return types with " +
                     "arguments of type <a href=\"../../pkg2/Foo3.html\" title" +
                     "=\"class in pkg2\">Foo3</a></span><span class=\"tabEnd\">" +
                     "&nbsp;</span></caption>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "Foo3.html",
                     "<td class=\"colFirst\"><code>&lt;T extends <a href=\"../../" +
                     "pkg2/ParamTest.html\" title=\"class in pkg2\">ParamTest</a>&lt;" +
                     "<a href=\"../../pkg2/Foo3.html\" title=\"class in pkg2\">Foo3" +
                     "</a>&gt;&gt;&nbsp;<br><a href=\"../../pkg2/ParamTest.html\" " +
                     "title=\"class in pkg2\">ParamTest</a>&lt;<a href=\"../../pkg2/" +
                     "Foo3.html\" title=\"class in pkg2\">Foo3</a>&gt;</code></td>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "ParamTest2.html",
                     "<caption><span>Classes in <a href=\"../../pkg2/" +
                     "package-summary.html\">pkg2</a> with type parameters of " +
                     "type <a href=\"../../pkg2/ParamTest2.html\" title=\"class " +
                     "in pkg2\">ParamTest2</a></span><span class=\"tabEnd\">" +
                     "&nbsp;</span></caption>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "ParamTest2.html",
                     "<td class=\"colLast\"><code><strong><a href=\"../../pkg2/ClassUseTest3.html\" " +
                     "title=\"class in pkg2\">ClassUseTest3</a>&lt;T extends " +
                     "<a href=\"../../pkg2/ParamTest2.html\" title=\"class in pkg2\">" +
                     "ParamTest2</a>&lt;java.util.List&lt;? extends " +
                     "<a href=\"../../pkg2/Foo4.html\" title=\"class in pkg2\">" +
                     "Foo4</a>&gt;&gt;&gt;</strong></code>&nbsp;</td>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "ParamTest2.html",
                     "<caption><span>Methods in <a href=\"../../pkg2/" +
                     "package-summary.html\">pkg2</a> with type parameters of " +
                     "type <a href=\"../../pkg2/ParamTest2.html\" title=\"class " +
                     "in pkg2\">ParamTest2</a></span><span class=\"tabEnd\">" +
                     "&nbsp;</span></caption>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "ParamTest2.html",
                     "<td class=\"colLast\"><span class=\"strong\">ClassUseTest3" +
                     ".</span><code><strong><a href=\"../../pkg2/ClassUseTest3." +
                     "html#method(T)\">method</a></strong>(T&nbsp;t)</code>&nbsp;</td>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "ParamTest2.html",
                     "<td class=\"colFirst\"><code>&lt;T extends <a href=\"../" +
                     "../pkg2/ParamTest2.html\" title=\"class in pkg2\">" +
                     "ParamTest2</a>&lt;java.util.List&lt;? extends <a href=\".." +
                     "/../pkg2/Foo4.html\" title=\"class in pkg2\">Foo4</a>&gt;" +
                     "&gt;&gt;&nbsp;<br><a href=\"../../pkg2/ParamTest2.html\" " +
                     "title=\"class in pkg2\">ParamTest2</a>&lt;java.util.List" +
                     "&lt;? extends <a href=\"../../pkg2/Foo4.html\" title=\"" +
                     "class in pkg2\">Foo4</a>&gt;&gt;</code></td>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "Foo4.html",
                     "<caption><span>Classes in <a href=\"../../pkg2/" +
                     "package-summary.html\">pkg2</a> with type parameters of " +
                     "type <a href=\"../../pkg2/Foo4.html\" title=\"class in " +
                     "pkg2\">Foo4</a></span><span class=\"tabEnd\">&nbsp;" +
                     "</span></caption>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "Foo4.html",
                     "<td class=\"colLast\"><code><strong><a href=\"../../pkg2/ClassUseTest3.html\" " +
                     "title=\"class in pkg2\">ClassUseTest3</a>&lt;T extends " +
                     "<a href=\"../../pkg2/ParamTest2.html\" title=\"class in pkg2\">" +
                     "ParamTest2</a>&lt;java.util.List&lt;? extends " +
                     "<a href=\"../../pkg2/Foo4.html\" title=\"class in pkg2\">" +
                     "Foo4</a>&gt;&gt;&gt;</strong></code>&nbsp;</td>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "Foo4.html",
                     "<caption><span>Methods in <a href=\"../../pkg2/" +
                     "package-summary.html\">pkg2</a> with type parameters of " +
                     "type <a href=\"../../pkg2/Foo4.html\" title=\"class in " +
                     "pkg2\">Foo4</a></span><span class=\"tabEnd\">&nbsp;</span></caption>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "Foo4.html",
                     "<td class=\"colLast\"><span class=\"strong\">ClassUseTest3." +
                     "</span><code><strong><a href=\"../../pkg2/ClassUseTest3." +
                     "html#method(T)\">method</a></strong>(T&nbsp;t)</code>" +
                     "&nbsp;</td>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "Foo4.html",
                     "<caption><span>Methods in <a href=\"../../pkg2/" +
                     "package-summary.html\">pkg2</a> that return types with " +
                     "arguments of type <a href=\"../../pkg2/Foo4.html\" " +
                     "title=\"class in pkg2\">Foo4</a></span><span class=\"" +
                     "tabEnd\">&nbsp;</span></caption>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "Foo4.html",
                     "<td class=\"colFirst\"><code>&lt;T extends <a href=\"../" +
                     "../pkg2/ParamTest2.html\" title=\"class in pkg2\">" +
                     "ParamTest2</a>&lt;java.util.List&lt;? extends <a href=\".." +
                     "/../pkg2/Foo4.html\" title=\"class in pkg2\">Foo4</a>&gt;" +
                     "&gt;&gt;&nbsp;<br><a href=\"../../pkg2/ParamTest2.html\" " +
                     "title=\"class in pkg2\">ParamTest2</a>&lt;java.util.List" +
                     "&lt;? extends <a href=\"../../pkg2/Foo4.html\" title=\"" +
                     "class in pkg2\">Foo4</a>&gt;&gt;</code></td>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "Foo4.html",
                     "<caption><span>Method parameters in <a href=\"../../pkg2/" +
                     "package-summary.html\">pkg2</a> with type arguments of " +
                     "type <a href=\"../../pkg2/Foo4.html\" title=\"class in " +
                     "pkg2\">Foo4</a></span><span class=\"tabEnd\">&nbsp;" +
                     "</span></caption>" + NL + "<tr>" + NL +
                     "<th class=\"colFirst\" scope=\"col\">Modifier and Type</th>" + NL +
                     "<th class=\"colLast\" scope=\"col\">Method and Description</th>" + NL +
                     "</tr>" + NL + "<tbody>" + NL + "<tr class=\"altColor\">" + NL +
                     "<td class=\"colFirst\"><code>void</code></td>" + NL +
                     "<td class=\"colLast\"><span class=\"strong\">ClassUseTest3." +
                     "</span><code><strong><a href=\"../../pkg2/ClassUseTest3." +
                     "html#method(java.util.Set)\">method</a></strong>(java." +
                     "util.Set&lt;<a href=\"../../pkg2/Foo4.html\" title=\"" +
                     "class in pkg2\">Foo4</a>&gt;&nbsp;p)</code>&nbsp;</td>" + NL +
                     "</tr>" + NL + "</tbody>"
            },
            {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "Foo4.html",
                     "<caption><span>Constructor parameters in <a href=\"../../" +
                     "pkg2/package-summary.html\">pkg2</a> with type arguments " +
                     "of type <a href=\"../../pkg2/Foo4.html\" title=\"class in " +
                     "pkg2\">Foo4</a></span><span class=\"tabEnd\">&nbsp;" +
                     "</span></caption>"
            },
            {BUG_ID + FS + "pkg" + FS + "class-use" + FS + "AnnotationType.html",
                     "<caption><span>Packages with annotations of type <a href=\"" +
                     "../../pkg/AnnotationType.html\" title=\"annotation in pkg\">" +
                     "AnnotationType</a></span><span class=\"tabEnd\">&nbsp;" +
                     "</span></caption>"
            },
            {BUG_ID + FS + "pkg" + FS + "class-use" + FS + "AnnotationType.html",
                     "<caption><span>Classes in <a href=\"../../pkg/" +
                     "package-summary.html\">pkg</a> with annotations of type " +
                     "<a href=\"../../pkg/AnnotationType.html\" title=\"" +
                     "annotation in pkg\">AnnotationType</a></span><span class" +
                     "=\"tabEnd\">&nbsp;</span></caption>"
            },
            {BUG_ID + FS + "pkg" + FS + "class-use" + FS + "AnnotationType.html",
                     "<caption><span>Fields in <a href=\"../../pkg/" +
                     "package-summary.html\">pkg</a> with annotations of type " +
                     "<a href=\"../../pkg/AnnotationType.html\" title=\"annotation " +
                     "in pkg\">AnnotationType</a></span><span class=\"tabEnd\">" +
                     "&nbsp;</span></caption>"
            },
            {BUG_ID + FS + "pkg" + FS + "class-use" + FS + "AnnotationType.html",
                     "<caption><span>Methods in <a href=\"../../pkg/" +
                     "package-summary.html\">pkg</a> with annotations of type " +
                     "<a href=\"../../pkg/AnnotationType.html\" title=\"annotation " +
                     "in pkg\">AnnotationType</a></span><span class=\"tabEnd\">" +
                     "&nbsp;</span></caption>"
            },
            {BUG_ID + FS + "pkg" + FS + "class-use" + FS + "AnnotationType.html",
                     "<caption><span>Method parameters in <a href=\"../../pkg/" +
                     "package-summary.html\">pkg</a> with annotations of type " +
                     "<a href=\"../../pkg/AnnotationType.html\" title=\"annotation " +
                     "in pkg\">AnnotationType</a></span><span class=\"tabEnd\">" +
                     "&nbsp;</span></caption>"
            },
            {BUG_ID + FS + "pkg" + FS + "class-use" + FS + "AnnotationType.html",
                     "<caption><span>Constructors in <a href=\"../../pkg/" +
                     "package-summary.html\">pkg</a> with annotations of type " +
                     "<a href=\"../../pkg/AnnotationType.html\" title=\"annotation " +
                     "in pkg\">AnnotationType</a></span><span class=\"tabEnd\">" +
                     "&nbsp;</span></caption>"
            },
            {BUG_ID + FS + "pkg" + FS + "class-use" + FS + "AnnotationType.html",
                     "<caption><span>Constructor parameters in <a href=\"../../" +
                     "pkg/package-summary.html\">pkg</a> with annotations of " +
                     "type <a href=\"../../pkg/AnnotationType.html\" title=\"" +
                     "annotation in pkg\">AnnotationType</a></span><span class=\"" +
                     "tabEnd\">&nbsp;</span></caption>"
            },
            {BUG_ID + FS + "index-all.html",
                "<span class=\"strong\"><a href=\"./pkg2/Foo.html#method(java.util.Vector)\">" +
                "method(Vector&lt;Object&gt;)</a></span>"
            },
            {BUG_ID + FS + "index-all.html",
                "<span class=\"strong\"><a href=\"./pkg2/Foo.html#method(java.util.Vector)\">" +
                "method(Vector&lt;Object&gt;)</a></span>"
            },
        };
    private static final String[][] NEGATED_TEST = {
        {BUG_ID + FS + "pkg" + FS + "Coin.html", "<span class=\"strong\">Constructor Summary</span>"},
        {BUG_ID + FS + "allclasses-frame.html",
            "<a href=\"../pkg/TypeParameters.html\" title=\"class in pkg\">" +
                    "TypeParameters</a>&lt;<a href=\"../pkg/TypeParameters.html\" " +
                    "title=\"type parameter in TypeParameters\">E</a>&gt;"
        },
        {BUG_ID + FS + "pkg" + FS + "AnnotationTypeUsage.html",
            "<a href=\"../pkg/AnnotationTypeUndocumented.html\" title=\"annotation in pkg\">@AnnotationTypeUndocumented</a>(<a href=\"../pkg/AnnotationType.html#optional\">optional</a>=\"Class Annotation\"," + NL +
            "                <a href=\"../pkg/AnnotationType.html#required\">required</a>=1994)" + NL +
            "public class <strong>AnnotationTypeUsage</strong></dt><dt>extends java.lang.Object</dt>"},
        {BUG_ID + FS + "pkg" + FS + "AnnotationTypeUsage.html",
            "<a href=\"../pkg/AnnotationTypeUndocumented.html\" title=\"annotation in pkg\">@AnnotationTypeUndocumented</a>(<a href=\"../pkg/AnnotationType.html#optional\">optional</a>=\"Field Annotation\"," + NL +
            "                <a href=\"../pkg/AnnotationType.html#required\">required</a>=1994)" + NL +
            "public int <strong>field</strong>"},
        {BUG_ID + FS + "pkg" + FS + "AnnotationTypeUsage.html",
            "<a href=\"../pkg/AnnotationTypeUndocumented.html\" title=\"annotation in pkg\">@AnnotationTypeUndocumented</a>(<a href=\"../pkg/AnnotationType.html#optional\">optional</a>=\"Constructor Annotation\"," + NL +
            "                <a href=\"../pkg/AnnotationType.html#required\">required</a>=1994)" + NL +
            "public <strong>AnnotationTypeUsage</strong>()"},
        {BUG_ID + FS + "pkg" + FS + "AnnotationTypeUsage.html",
            "<a href=\"../pkg/AnnotationTypeUndocumented.html\" title=\"annotation in pkg\">@AnnotationTypeUndocumented</a>(<a href=\"../pkg/AnnotationType.html#optional\">optional</a>=\"Method Annotation\"," + NL +
            "                <a href=\"../pkg/AnnotationType.html#required\">required</a>=1994)" + NL +
            "public void <strong>method</strong>()"},
        {WARNING_OUTPUT,
            "Internal error: package sets don't match: [] with: null"
        },
    };
    public static void main(String[] args) {
        TestNewLanguageFeatures tester = new TestNewLanguageFeatures();
        run(tester, ARGS, TEST, NEGATED_TEST);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
