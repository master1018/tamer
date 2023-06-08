    @Test
    public void testUsage() {
        BigTest opts = new BigTest();
        CommandLine line = new CommandLine(opts, "prog", "file...", "This is some descriptive text for the command, which ought" + " to be formatted by the underlying usage() method." + "  This lets us test whether that works or not.");
        line.setOptionDescriptionIndent(12);
        StringWriter out = new StringWriter();
        line.briefUsage(out);
        assertEquals(out.toString(), "prog [-?|--help] [-b|--boolT] [-B|--boolF] [--boolDT] [--boolDF] [--boolN]\n" + "    [-c|--charV char] [--charD char] [--charN char] [-S|--strV str] [--strD str]\n" + "    [--strN str] [-y|--byteV num] [--byteD num] [--byteN num] [-s|--shortV num]\n" + "    [--shortD num] [--shortN num] [-i|--intV num] [--intD num] [--intN num]\n" + "    [-l|--longV num] [--longD num] [--longN num] [-f|--floatV float]\n" + "    [--floatD float] [--floatN float] [-d|--doubleV double] [--doubleD double]\n" + "    [--doubleN double] [-t|--thingV thing] [--thingD thing] [--thingN thing]\n" + "    [-F|--fileV path] [--fileD path] [--fileN path] [-r|--readV file]\n" + "    [--readD file] [--readN file] [-w|--writeV file] [--writeD file]\n" + "    [--writeN file] [-I|--inV file] [--inD file] [--inN file] [-O|--outV file]\n" + "    [--outD file] [--outN file] [--randN file] [-v|--valsV num,...]\n" + "    [--valsD num,...] [--valsN num,...] file...\n");
        out = new StringWriter();
        line.fullUsage(out);
        assertEquals(out.toString(), "prog [-?|--help] [-b|--boolT] [-B|--boolF] [--boolDT] [--boolDF] [--boolN]\n" + "    [-c|--charV char] [--charD char] [--charN char] [-S|--strV str] [--strD str]\n" + "    [--strN str] [-y|--byteV num] [--byteD num] [--byteN num] [-s|--shortV num]\n" + "    [--shortD num] [--shortN num] [-i|--intV num] [--intD num] [--intN num]\n" + "    [-l|--longV num] [--longD num] [--longN num] [-f|--floatV float]\n" + "    [--floatD float] [--floatN float] [-d|--doubleV double] [--doubleD double]\n" + "    [--doubleN double] [-t|--thingV thing] [--thingD thing] [--thingN thing]\n" + "    [-F|--fileV path] [--fileD path] [--fileN path] [-r|--readV file]\n" + "    [--readD file] [--readN file] [-w|--writeV file] [--writeD file]\n" + "    [--writeN file] [-I|--inV file] [--inD file] [--inN file] [-O|--outV file]\n" + "    [--outD file] [--outN file] [--randN file] [-v|--valsV num,...]\n" + "    [--valsD num,...] [--valsN num,...] file...\n" + "This is some descriptive text for the command, which ought to be formatted by\n" + "the underlying usage() method.  This lets us test whether that works or not.\n" + "\n" + "-?|--help   Print usage message\n" + "-b|--boolT  a boolean value that defaults to 'true'\n" + "-B|--boolF  a boolean value that defaults to 'false'. This also has a lot of\n" + "            extra text so that we can see how wrapping is handled, which we hope\n" + "            will be handled correctly\n" + "--boolDT\n" + "--boolDF\n" + "--boolN\n" + "-c|--charV char\n" + "--charD char\n" + "            A character that might be any darn thing.\n" + "--charN char\n" + "-S|--strV str\n" + "--strD str\n" + "--strN str\n" + "-y|--byteV num\n" + "--byteD num\n" + "--byteN num\n" + "-s|--shortV num\n" + "--shortD num\n" + "--shortN num\n" + "-i|--intV num\n" + "--intD num\n" + "--intN num\n" + "-l|--longV num\n" + "--longD num\n" + "--longN num\n" + "-f|--floatV float\n" + "--floatD float\n" + "--floatN float\n" + "-d|--doubleV double\n" + "--doubleD double\n" + "--doubleN double\n" + "-t|--thingV thing\n" + "--thingD thing\n" + "--thingN thing\n" + "-F|--fileV path\n" + "--fileD path\n" + "--fileN path\n" + "-r|--readV file\n" + "--readD file\n" + "--readN file\n" + "-w|--writeV file\n" + "--writeD file\n" + "--writeN file\n" + "-I|--inV file\n" + "--inD file\n" + "--inN file\n" + "-O|--outV file\n" + "--outD file\n" + "--outN file\n" + "--randN file\n" + "-v|--valsV num,...\n" + "--valsD num,...\n" + "--valsN num,...\n");
        line.setLineWidth(0);
        out = new StringWriter();
        line.briefUsage(out);
        assertEquals(out.toString(), "prog [-?|--help] [-b|--boolT] [-B|--boolF] [--boolDT] [--boolDF] [--boolN] [-c|--charV char] [--charD char] [--charN char] [-S|--strV str] [--strD str] [--strN str] [-y|--byteV num] [--byteD num] [--byteN num] [-s|--shortV num] [--shortD num] [--shortN num] [-i|--intV num] [--intD num] [--intN num] [-l|--longV num] [--longD num] [--longN num] [-f|--floatV float] [--floatD float] [--floatN float] [-d|--doubleV double] [--doubleD double] [--doubleN double] [-t|--thingV thing] [--thingD thing] [--thingN thing] [-F|--fileV path] [--fileD path] [--fileN path] [-r|--readV file] [--readD file] [--readN file] [-w|--writeV file] [--writeD file] [--writeN file] [-I|--inV file] [--inD file] [--inN file] [-O|--outV file] [--outD file] [--outN file] [--randN file] [-v|--valsV num,...] [--valsD num,...] [--valsN num,...] file...\n");
        out = new StringWriter();
        line.fullUsage(out);
        assertEquals(out.toString(), "prog [-?|--help] [-b|--boolT] [-B|--boolF] [--boolDT] [--boolDF] [--boolN] [-c|--charV char] [--charD char] [--charN char] [-S|--strV str] [--strD str] [--strN str] [-y|--byteV num] [--byteD num] [--byteN num] [-s|--shortV num] [--shortD num] [--shortN num] [-i|--intV num] [--intD num] [--intN num] [-l|--longV num] [--longD num] [--longN num] [-f|--floatV float] [--floatD float] [--floatN float] [-d|--doubleV double] [--doubleD double] [--doubleN double] [-t|--thingV thing] [--thingD thing] [--thingN thing] [-F|--fileV path] [--fileD path] [--fileN path] [-r|--readV file] [--readD file] [--readN file] [-w|--writeV file] [--writeD file] [--writeN file] [-I|--inV file] [--inD file] [--inN file] [-O|--outV file] [--outD file] [--outN file] [--randN file] [-v|--valsV num,...] [--valsD num,...] [--valsN num,...] file...\n" + "This is some descriptive text for the command, which ought to be formatted by the underlying usage() method.  This lets us test whether that works or not.\n" + "\n" + "-?|--help   Print usage message\n" + "-b|--boolT  a boolean value that defaults to 'true'\n" + "-B|--boolF  a boolean value that defaults to 'false'. This also has a lot of extra text so that we can see how wrapping is handled, which we hope will be handled correctly\n" + "--boolDT\n" + "--boolDF\n" + "--boolN\n" + "-c|--charV char\n" + "--charD char\n" + "            A character that might be any darn thing.\n" + "--charN char\n" + "-S|--strV str\n" + "--strD str\n" + "--strN str\n" + "-y|--byteV num\n" + "--byteD num\n" + "--byteN num\n" + "-s|--shortV num\n" + "--shortD num\n" + "--shortN num\n" + "-i|--intV num\n" + "--intD num\n" + "--intN num\n" + "-l|--longV num\n" + "--longD num\n" + "--longN num\n" + "-f|--floatV float\n" + "--floatD float\n" + "--floatN float\n" + "-d|--doubleV double\n" + "--doubleD double\n" + "--doubleN double\n" + "-t|--thingV thing\n" + "--thingD thing\n" + "--thingN thing\n" + "-F|--fileV path\n" + "--fileD path\n" + "--fileN path\n" + "-r|--readV file\n" + "--readD file\n" + "--readN file\n" + "-w|--writeV file\n" + "--writeD file\n" + "--writeN file\n" + "-I|--inV file\n" + "--inD file\n" + "--inN file\n" + "-O|--outV file\n" + "--outD file\n" + "--outN file\n" + "--randN file\n" + "-v|--valsV num,...\n" + "--valsD num,...\n" + "--valsN num,...\n");
    }