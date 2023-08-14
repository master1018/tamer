public class CommandLine {
	static Hashtable options = new Hashtable(); static {
		options.put("--nocdata", Boolean.FALSE); 
		options.put("--files", Boolean.FALSE);	
		options.put("--reuse", Boolean.FALSE);	
		options.put("--nons", Boolean.FALSE);	
		options.put("--nobogons", Boolean.FALSE);  
		options.put("--any", Boolean.FALSE);	
		options.put("--emptybogons", Boolean.FALSE);	
		options.put("--norootbogons", Boolean.FALSE);	
		options.put("--pyxin", Boolean.FALSE);	
		options.put("--lexical", Boolean.FALSE); 
		options.put("--pyx", Boolean.FALSE);	
		options.put("--html", Boolean.FALSE);	
		options.put("--method=", Boolean.FALSE); 
		options.put("--doctype-public=", Boolean.FALSE); 
		options.put("--doctype-system=", Boolean.FALSE); 
		options.put("--output-encoding=", Boolean.FALSE); 
		options.put("--omit-xml-declaration", Boolean.FALSE); 
		options.put("--encoding=", Boolean.FALSE); 
		options.put("--help", Boolean.FALSE); 	
		options.put("--version", Boolean.FALSE);	
		options.put("--nodefaults", Boolean.FALSE); 
		options.put("--nocolons", Boolean.FALSE); 
		options.put("--norestart", Boolean.FALSE); 
		options.put("--ignorable", Boolean.FALSE);  
		}
	public static void main(String[] argv) throws IOException, SAXException {
		int optind = getopts(options, argv);
		if (hasOption(options, "--help")) {
			doHelp();
			return;
			}
		if (hasOption(options, "--version")) {
			System.err.println("TagSoup version 1.2");
			return;
			}
		if (argv.length == optind) {
			process("", System.out);
			}
		else if (hasOption(options, "--files")) {
			for (int i = optind; i < argv.length; i++) {
				String src = argv[i];
				String dst;
				int j = src.lastIndexOf('.');
				if (j == -1)
					dst = src + ".xhtml";
				else if (src.endsWith(".xhtml"))
					dst = src + "_";
				else
					dst = src.substring(0, j) + ".xhtml";
				System.err.println("src: " + src + " dst: " + dst);
				OutputStream os = new FileOutputStream(dst);
				process(src, os);
				}
			}
		else {
			for (int i = optind; i < argv.length; i++) {
				System.err.println("src: " + argv[i]);
				process(argv[i], System.out);
				}
			}
		}
	private static void doHelp() {
		System.err.print("usage: java -jar tagsoup-*.jar ");
		System.err.print(" [ ");
		boolean first = true;
		for (Enumeration e = options.keys(); e.hasMoreElements(); ) {
			if (!first) {
				System.err.print("| ");
				}
			first = false;
			String key = (String)(e.nextElement());
			System.err.print(key);
			if (key.endsWith("="))
				System.err.print("?");
				System.err.print(" ");
			}
		System.err.println("]*");
	}
	private static Parser theParser = null;
	private static HTMLSchema theSchema = null;
	private static String theOutputEncoding = null;
	private static void process(String src, OutputStream os)
			throws IOException, SAXException {
		XMLReader r;
		if (hasOption(options, "--reuse")) {
			if (theParser == null) theParser = new Parser();
			r = theParser;
			}
		else {
			r = new Parser();
			}
		theSchema = new HTMLSchema();
		r.setProperty(Parser.schemaProperty, theSchema);
		if (hasOption(options, "--nocdata")) {
			r.setFeature(Parser.CDATAElementsFeature, false);
			}
		if (hasOption(options, "--nons") || hasOption(options, "--html")) {
			r.setFeature(Parser.namespacesFeature, false);
			}
		if (hasOption(options, "--nobogons")) {
			r.setFeature(Parser.ignoreBogonsFeature, true);
			}
		if (hasOption(options, "--any")) {
			r.setFeature(Parser.bogonsEmptyFeature, false);
			}
		else if (hasOption(options, "--emptybogons")) {
			r.setFeature(Parser.bogonsEmptyFeature, true);
			}
		if (hasOption(options, "--norootbogons")) {
			r.setFeature(Parser.rootBogonsFeature, false);
			}
		if (hasOption(options, "--nodefaults")) {
			r.setFeature(Parser.defaultAttributesFeature, false);
			}
		if (hasOption(options, "--nocolons")) {
			r.setFeature(Parser.translateColonsFeature, true);
			}
		if (hasOption(options, "--norestart")) {
			r.setFeature(Parser.restartElementsFeature, false);
			}
		if (hasOption(options, "--ignorable")) {
			r.setFeature(Parser.ignorableWhitespaceFeature, true);
			}
		if (hasOption(options, "--pyxin")) {
			r.setProperty(Parser.scannerProperty, new PYXScanner());
			}
		Writer w;
		if (theOutputEncoding == null) {
			w = new OutputStreamWriter(os);
			}
		else {
			w = new OutputStreamWriter(os, theOutputEncoding);
			}
		ContentHandler h = chooseContentHandler(w);
		r.setContentHandler(h);
		if (hasOption(options, "--lexical") && h instanceof LexicalHandler) {
			r.setProperty(Parser.lexicalHandlerProperty, h);
			}
		InputSource s = new InputSource();
		if (src != "") {
			s.setSystemId(src);
			}
		else {
			s.setByteStream(System.in);
			}
		if (hasOption(options, "--encoding=")) {
			String encoding = (String)options.get("--encoding=");
			if (encoding != null) s.setEncoding(encoding);
			}
		r.parse(s);
		}
	private static ContentHandler chooseContentHandler(Writer w) {
		XMLWriter x;
		if (hasOption(options, "--pyx")) {
			return new PYXWriter(w);
			}
		x = new XMLWriter(w);
		if (hasOption(options, "--html")) {
			x.setOutputProperty(XMLWriter.METHOD, "html");
			x.setOutputProperty(XMLWriter.OMIT_XML_DECLARATION, "yes");
			}
		if (hasOption(options, "--method=")) {
			String method = (String)options.get("--method=");
			if (method != null) {
				x.setOutputProperty(XMLWriter.METHOD, method);
				}
			}
		if (hasOption(options, "--doctype-public=")) {
			String doctype_public = (String)options.get("--doctype-public=");
			if (doctype_public != null) {
				x.setOutputProperty(XMLWriter.DOCTYPE_PUBLIC, doctype_public);
				}
			}
		if (hasOption(options, "--doctype-system=")) {
			String doctype_system = (String)options.get("--doctype-system=");
			if (doctype_system != null) {
				x.setOutputProperty(XMLWriter.DOCTYPE_SYSTEM, doctype_system);
				}
			}
		if (hasOption(options, "--output-encoding=")) {
			theOutputEncoding = (String)options.get("--output-encoding=");
			if (theOutputEncoding != null) {
				x.setOutputProperty(XMLWriter.ENCODING, theOutputEncoding);
				}
			}
		if (hasOption(options, "--omit-xml-declaration")) {
			x.setOutputProperty(XMLWriter.OMIT_XML_DECLARATION, "yes");
			}
		x.setPrefix(theSchema.getURI(), "");
		return x;
		}
	private static int getopts(Hashtable options, String[] argv) {
		int optind;
		for (optind = 0; optind < argv.length; optind++) {
			String arg = argv[optind];
			String value = null;
			if (arg.charAt(0) != '-') break;
			int eqsign = arg.indexOf('=');
			if (eqsign != -1) {
				value = arg.substring(eqsign + 1, arg.length());
				arg = arg.substring(0, eqsign + 1);
				}
			if (options.containsKey(arg)) {
				if (value == null) options.put(arg, Boolean.TRUE);
				else options.put(arg, value);
				}
			else {
				System.err.print("Unknown option ");
				System.err.println(arg);
				System.exit(1);
				}
			}
		return optind;
		}
	private static boolean hasOption(Hashtable options, String option) {
		if (Boolean.getBoolean(option)) return true;
		else if (options.get(option) != Boolean.FALSE) return true;
		return false;
		}
	}
