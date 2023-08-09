public class CommentChecker {
    static int errors = 0;
    final static boolean verbose = false;
    static void check(String fileName) {
        BufferedReader in = null;
        boolean inComment = false;
        boolean inLineComment = false;
        boolean inQuote = false;
        boolean inEscape = false;
        int lastChar = -1;
        int lineNumber = 1;
        try {
            in = new BufferedReader(new FileReader(fileName));
            while (true) {
                int ch = in.read();
                if (ch == -1) {
                    if (inQuote || inComment) {
                        error(fileName + ": premature EOF.");
                    }
                    return;
                }
                if (verbose) {
                    System.out.print((char)ch);
                }
                switch (ch) {
                  case '\n':
                    if (inQuote && !inComment) {
                        error(fileName + ":" + lineNumber +
                              " dangling quote.");
                        inQuote = false;
                    }
                    if (inLineComment) {
                        inLineComment = false;
                        if (verbose) {
                            System.out.println("\ninLineComment=false");
                        }
                    }
                    lineNumber++;
                    break;
                  case '\"':
                    if (!inComment && !inLineComment && !inEscape &&
                        !(!inQuote && lastChar == '\'')) {
                        inQuote = !inQuote;
                        if (verbose) {
                            System.out.println("\ninQuote=" + inQuote);
                        }
                    }
                    break;
                  case '/':
                    if (!inQuote && lastChar == '*') {
                        inComment = false;
                        if (verbose) {
                            System.out.println("\ninComment=false");
                        }
                    }
                    if (!inQuote && lastChar == '/') {
                        inLineComment = true;
                        if (verbose) {
                            System.out.println("\ninLineComment=true");
                        }
                    }
                    break;
                  case '*':
                    if (!inQuote && lastChar == '/') {
                        if (inComment) {
                            error(fileName + ":" + lineNumber +
                                  " nested comment.");
                        }
                        inComment = true;
                        if (verbose) {
                            System.out.println("\ninComment=true");
                        }
                    }
                    break;
                }
                lastChar = ch;
                if (ch == '\\' && !inEscape) {
                    inEscape = true;
                    if (verbose) {
                        System.out.println("\ninEscape set");
                    }
                } else {
                    inEscape = false;
                }
            }
        } catch (FileNotFoundException fnfe) {
            error(fileName + " not found.");
        } catch (IOException ioe) {
            error(fileName + ": " + ioe);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    error(fileName + ": " + e);
                }
            }
        }
    }
    static void error(String description) {
        System.err.println(description);
        errors++;
    }
    static void exit() {
        if (errors != 1) {
            System.out.println("There were " + errors + " errors.");
        } else {
            System.out.println("There was 1 error.");
        }
        System.exit(errors);
    }
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("usage: java CommentChecker [-] file.java ...");
            System.exit(1);
        }
        if (args.length == 1 && args[0].equals("-")) {
            try {
                BufferedReader br =
                    new BufferedReader(new InputStreamReader(System.in));
                while (true) {
                    String fileName = br.readLine();
                    if (fileName == null) {
                        break;
                    }
                    check(fileName);
                }
                br.close();
            } catch (Exception e) {
                error("error reading System.in: " + e);
            }
        } else {
            for (int i = 0; i < args.length; i++) {
                check(args[i]);
            }
        }
        exit();
    }
}
