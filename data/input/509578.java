final class OptsParser implements IOptsParser
{
    public synchronized void usage (final PrintWriter out, final int level, final int width)
    {
        final String prefix = OPT_PREFIXES [CANONICAL_OPT_PREFIX];
        for (Iterator i = m_metadata.getOptDefs (); i.hasNext (); )
        {
            final OptDef optdef = (OptDef) i.next ();
            if ((level < 2) && optdef.isDetailedOnly ()) 
                continue;
            final StringBuffer line = new StringBuffer ("  ");
            final String canonicalName = optdef.getCanonicalName ();
            final boolean isPattern = optdef.isPattern (); 
            line.append (prefix);
            line.append (canonicalName);
            if (isPattern) line.append ('*');
            final String [] names = optdef.getNames ();
            for (int n = 0; n < names.length; ++ n)
            {
                final String name = names [n];
                if (! name.equals (canonicalName))
                {
                    line.append (", ");
                    line.append (prefix);
                    line.append (name);
                    if (isPattern) line.append ('*');
                }
            }
            final String vmnemonic = optdef.getValueMnemonic ();
            if (vmnemonic != null)
            {
                line.append (' ');
                line.append (vmnemonic);
            }
            int padding = 16 - line.length ();
            if (padding < 2)
            {
                out.println (line);
                line.setLength (0);
                for (int p = 0; p < 16; ++ p) line.append (' ');
            }
            else
            {          
                for (int p = 0; p < padding; ++ p) line.append (' ');
            }
            if (optdef.isRequired ()) line.append ("{required} ");
            line.append (optdef.getDescription ());
            out.println (line);
        }
        if (level < DETAILED_USAGE)
        {
            final OptDef usageOptDef = m_metadata.getUsageOptDef ();
            if ((usageOptDef != null) && (usageOptDef.getNames () != null) && (usageOptDef.getNames ().length > 1))
            {
                out.println (); 
                out.println ("  {use '" + usageOptDef.getNames () [1] + "' option to see detailed usage help}");
            }
        }
    }
    public synchronized IOpts parse (final String [] args)
    {
        if (args == null) throw new IllegalArgumentException ("null input: args");
        final Opts opts = new Opts ();
        {
            final String [] nv = new String [2]; 
            final String [] pp = new String [1]; 
            int state = STATE_OPT;
            OptDef optdef = null;
            Opt opt = null;
            String value = null;
            int valueCount = 0;
            int a;
      scan: for (a = 0; a < args.length; )
            {
                final String av = args [a];
                if (av == null) throw new IllegalArgumentException ("null input: args[" + a + "]");
                switch (state)
                {
                    case STATE_OPT:
                    {
                        if (isOpt (av, valueCount, optdef))
                        {
                            valueCount = 0;
                            getOptNameAndValue (av, nv); 
                            final String optName = nv [0]; 
                            optdef = m_metadata.getOptDef (optName, pp); 
                            if (optdef == null)
                            {
                                opts.addError (formatMessage ("unknown option \'" + optName + "\'"));
                                state = STATE_ERROR;
                            }
                            else
                            {
                                final String canonicalName = getOptCanonicalName (optName, optdef);
                                final String patternPrefix = pp [0];
                                opt = opts.getOpt (canonicalName);
                                if (optdef.isMergeable ())
                                {
                                    if (opt == null)
                                    {
                                        opt = new Opt (optName, canonicalName, patternPrefix);
                                        opts.addOpt (opt, optdef, optName);
                                    }
                                }
                                else
                                {
                                    if (opt == null)
                                    {
                                        opt = new Opt (optName, canonicalName, patternPrefix);
                                        opts.addOpt (opt, optdef, optName);
                                    }
                                    else
                                    {
                                        opts.addError (formatMessage ("option \'" + optName + "\' cannot be specified more than once"));
                                        state = STATE_ERROR;
                                    }
                                }
                                value = nv [1];
                                if (value == null) ++ a;
                                state = STATE_OPT_VALUE;
                            }
                        }
                        else
                        {
                            state = STATE_FREE_ARGS;
                        }
                    }
                    break;
                    case STATE_OPT_VALUE:
                    {
                        if (value != null)
                        {
                            valueCount = 1;
                            final int [] cardinality = optdef.getValueCardinality ();
                            if (cardinality [1] < 1)
                            {
                                opts.addError (formatMessage ("option \'" + opt.getName () + "\' does not accept values: \'" + value + "\'"));
                                state = STATE_ERROR;
                            }
                            else
                            {
                                ++ a;
                                opt.addValue (value);
                            }
                        }
                        else
                        {
                            value = args [a];
                            final int [] cardinality = optdef.getValueCardinality ();
                            if (isOpt (value, valueCount, optdef))
                            {
                                if (valueCount < cardinality [0])
                                {
                                    opts.addError (formatMessage ("option \'" + opt.getName () + "\' does not accept fewer than " + cardinality [0] + " value(s)"));
                                    state = STATE_ERROR;
                                }
                                else
                                    state = STATE_OPT;
                            }
                            else
                            {
                                if (valueCount < cardinality [1])
                                {
                                    ++ valueCount;
                                    ++ a;
                                    opt.addValue (value);
                                }
                                else
                                {
                                        state = STATE_FREE_ARGS;
                                } 
                            }
                        }
                        value = null;
                    }
                    break;
                    case STATE_FREE_ARGS:
                    {
                        if (isOpt (args [a], valueCount, optdef))
                        {
                            state = STATE_OPT;
                        }
                        else
                        {
                            opts.setFreeArgs (args, a);
                            break scan;
                        }
                    }
                    break;
                    case STATE_ERROR:
                    {
                        break scan; 
                    }
                } 
            }
            if (a == args.length)
            {
                if (opt != null) 
                {
                    final int [] cardinality = optdef.getValueCardinality ();
                    if (valueCount < cardinality [0])
                    {
                        opts.addError (formatMessage ("option \'" + opt.getName () + "\' does not accept fewer than " + cardinality [0] + " value(s)"));
                    }
                }
                else
                {
                    opts.setFreeArgs (args, a);
                }
            }
        } 
        final IOpt [] specified = opts.getOpts ();
        if (specified != null)
        {
            final Set  required = new HashSet ();
            required.addAll (m_metadata.getRequiredOpts ());
            for (int s = 0; s < specified.length; ++ s)
            {
                required.remove (specified [s].getCanonicalName ());
            }
            if (! required.isEmpty ())
            {
                for (Iterator i = required.iterator (); i.hasNext (); )
                {
                    opts.addError (formatMessage ("missing required option \'" + (String) i.next () + "\'"));
                }
            }
            for (int s = 0; s < specified.length; ++ s)
            {
                final IOpt opt = specified [s];
                final OptDef optdef = m_metadata.getOptDef (opt.getCanonicalName (), null); 
                final String [] requires = optdef.getRequiresSet (); 
                if (requires != null)
                {
                    for (int r = 0; r < requires.length; ++ r)
                    {
                        if (opts.getOpt (requires [r]) == null)
                            opts.addError (formatMessage ("option \'" + opt.getName () + "\' requires option \'" + requires [r] +  "\'"));
                    }
                }
                final String [] excludes = optdef.getExcludesSet (); 
                if (excludes != null)
                {
                    for (int x = 0; x < excludes.length; ++ x)
                    {
                        final Opt xopt = opts.getOpt (excludes [x]);
                        if (xopt != null)
                            opts.addError (formatMessage ("option \'" + opt.getName () + "\' cannot be used with option \'" + xopt.getName () +  "\'"));
                    }
                }
                if (optdef.isUsage ())
                {
                    opts.setUsageRequested (opt.getName ().equals (opt.getCanonicalName ()) ? SHORT_USAGE : DETAILED_USAGE);
                }
            }
        }
        return opts;
    }
    private static String getOptCanonicalName (final String n, final OptDef optdef)
    {
        if (optdef.isPattern ())
        {
            final String canonicalPattern = optdef.getCanonicalName ();
            final String [] patterns = optdef.getNames ();
            for (int p = 0; p < patterns.length; ++ p)
            {
                final String pattern = patterns [p];
                if (n.startsWith (pattern))
                {
                    return canonicalPattern.concat (n.substring (pattern.length ()));
                }
            }
            throw new IllegalStateException ("failed to detect pattern prefix for [" + n + "]");
        }
        else
        {
            return optdef.getCanonicalName ();
        }
    }
    private static boolean isOpt (final String av, final int valueCount, final OptDef optdef)
    {
        if (optdef != null)
        {
            final int [] cardinality = optdef.getValueCardinality ();
            if (valueCount < cardinality [1]) return false;
        }
        for (int p = 0; p < OPT_PREFIXES.length; ++ p)
        {
            if (av.startsWith (OPT_PREFIXES [p]))
                return (av.length () > OPT_PREFIXES [p].length ());
        }
        return false;
    }
    private static void getOptNameAndValue (final String av, final String [] nv)
    {
        nv [0] = null;
        nv [1] = null;
        for (int p = 0; p < OPT_PREFIXES.length; ++ p)
        {
            if ((av.startsWith (OPT_PREFIXES [p])) && (av.length () > OPT_PREFIXES [p].length ()))
            {
                final String name = av.substring (OPT_PREFIXES [p].length ()); 
                char separator = 0;
                int sindex = Integer.MAX_VALUE;
                for (int s = 0; s < OPT_VALUE_SEPARATORS.length; ++ s)
                {
                    final int index = name.indexOf (OPT_VALUE_SEPARATORS [s]);
                    if ((index > 0) && (index < sindex))
                    {
                        separator = OPT_VALUE_SEPARATORS [s];
                        sindex = index;
                    }
                }
                if (separator != 0)
                {
                    nv [0] = name.substring (0, sindex);
                    nv [1] = name.substring (sindex + 1);
                }
                else
                {
                    nv [0] = name;
                }
                return;
            }
        }
    }
    static final class Opt implements IOptsParser.IOpt
    {
        public String getName ()
        {
            return m_name;
        }
        public String getCanonicalName ()
        {
            return m_canonicalName;
        }
        public int getValueCount ()
        {
            if (m_values == null) return 0;
            return m_values.size ();
        }
        public String getFirstValue ()
        {
            if (m_values == null) return null;
            return (String) m_values.get (0);
        }
        public String [] getValues ()
        {
            if (m_values == null) return IConstants.EMPTY_STRING_ARRAY;
            final String [] result = new String [m_values.size ()];
            m_values.toArray (result);
            return result; 
        }
        public String getPatternPrefix ()
        {
            return m_patternPrefix;
        }
        public String toString ()
        {
            final StringBuffer s = new StringBuffer (m_name);
            if (! m_canonicalName.equals (m_name)) s.append (" [" + m_canonicalName + "]");
            if (m_values != null)
            {
                s.append (": ");
                s.append (m_values);
            }
            return s.toString (); 
        } 
        Opt (final String name, final String canonicalName, final String patternPrefix)
        {
            m_name = name;
            m_canonicalName = canonicalName;
            m_patternPrefix = patternPrefix;
        }
        void addValue (final String value)
        {
            if (value == null) throw new IllegalArgumentException ("null input: value");
            if (m_values == null) m_values = new ArrayList ();
            m_values.add (value);
        }
        private final String m_name, m_canonicalName, m_patternPrefix;
        private ArrayList m_values; 
    } 
    static final class Opts implements IOptsParser.IOpts
    {
        public int usageRequestLevel ()
        {
            return m_usageRequestLevel; 
        }
        public void error (final PrintWriter out, final int width)
        {
            if (hasErrors ())
            {
                for (Iterator i = m_errors.iterator (); i.hasNext (); )
                {
                    out.println (i.next ());
                }
            } 
        }
        public String [] getFreeArgs ()
        {
            if (hasErrors ())
                throw new IllegalStateException (errorsToString ());
            return m_freeArgs;
        }
        public IOpt [] getOpts ()
        {
            if (hasErrors ()) return null;
            if (m_opts.isEmpty ())
                return EMPTY_OPT_ARRAY;
            else
            {
                final IOpt [] result = new IOpt [m_opts.size ()];
                m_opts.toArray (result);
                return result; 
            }
        }
        public IOpt [] getOpts (final String pattern)
        {
            if (hasErrors ()) return null;
            final List  patternOpts = (List) m_patternMap.get (pattern);
            if ((patternOpts == null) || patternOpts.isEmpty ())
                return EMPTY_OPT_ARRAY;
            else
            {
                final IOpt [] result = new IOpt [patternOpts.size ()];
                patternOpts.toArray (result);
                return result;
            }
        }
        public boolean hasArg (final String name)
        {
            if (hasErrors ())
                throw new IllegalStateException (errorsToString ());
            return m_nameMap.containsKey (name);
        }
        Opts ()
        {
            m_opts = new ArrayList ();
            m_nameMap = new HashMap ();
            m_patternMap = new HashMap ();
        }
        void addOpt (final Opt opt, final OptDef optdef, final String occuranceName)
        {
            if (opt == null) throw new IllegalArgumentException ("null input: opt");
            if (optdef == null) throw new IllegalArgumentException ("null input: optdef");
            if (occuranceName == null) throw new IllegalArgumentException ("null input: occuranceName");
            m_opts.add (opt);
            final String [] names = optdef.getNames ();
            final boolean isPattern = (opt.getPatternPrefix () != null);
            if (isPattern)
            {
                final String unprefixedName = occuranceName.substring (opt.getPatternPrefix ().length ());
                for (int n = 0; n < names.length; ++ n)
                {
                    m_nameMap.put (names [n].concat (unprefixedName), opt);
                }
                {
                    final String canonicalPattern = optdef.getCanonicalName ();
                    List patternList = (List) m_patternMap.get (canonicalPattern);
                    if (patternList == null)
                    {
                        patternList = new ArrayList ();
                        for (int n = 0; n < names.length; ++ n)
                        {
                            m_patternMap.put (names [n], patternList);
                        } 
                    }
                    patternList.add (opt);
                }
            }
            else
            {
                for (int n = 0; n < names.length; ++ n)
                {
                    m_nameMap.put (names [n], opt);
                }
            }
        }
        Opt getOpt (final String occuranceName)
        {
            if (occuranceName == null) throw new IllegalArgumentException ("null input: occuranceName");
            return (Opt) m_nameMap.get (occuranceName);
        }
        void setFreeArgs (final String [] args, final int start)
        {
            if (args == null) throw new IllegalArgumentException ("null input: args");
            if ((start < 0) || (start > args.length)) throw new IllegalArgumentException ("invalid start index: " + start);
            m_freeArgs = new String [args.length - start];
            System.arraycopy (args, start, m_freeArgs, 0, m_freeArgs.length);
        }
        void setUsageRequested (final int level)
        {
            m_usageRequestLevel = level;
        }
        void addError (final String msg)
        {
            if (msg != null)
            {
                if (m_errors == null) m_errors = new ArrayList ();
                m_errors.add (msg);
            }
        }
        boolean hasErrors ()
        {
            return (m_errors != null) && ! m_errors.isEmpty ();
        }
        String errorsToString ()
        {
            if (! hasErrors ()) return "<no errors>";
            final CharArrayWriter caw = new CharArrayWriter ();
            final PrintWriter pw = new PrintWriter (caw);
            error (pw, DEFAULT_ERROR_WIDTH);
            pw.flush ();
            return caw.toString (); 
        }
        private final List  m_opts;
        private final Map  m_nameMap;
        private final Map  m_patternMap;
        private String [] m_freeArgs;
        private List  m_errors;
        private int m_usageRequestLevel;
        private static final int DEFAULT_ERROR_WIDTH = 80;
        private static final IOpt [] EMPTY_OPT_ARRAY = new IOpt [0]; 
    } 
    static final class OptDef 
    {
        OptDef (final boolean usage)
        {
            m_usage = usage;
        }
        boolean isUsage ()
        {
            return m_usage;
        }
        String getCanonicalName ()
        {
            return m_names [0];
        }
        String [] getNames ()
        {
            return m_names;
        }
        boolean isRequired ()
        {
            return m_required;
        }
        String getValueMnemonic ()
        {
            return m_valueMnemonic;
        }
        boolean isMergeable ()
        {
            return m_mergeable;
        }
        boolean isDetailedOnly ()
        {
            return m_detailedOnly;
        }
        boolean isPattern ()
        {
            return m_pattern;
        }
        int [] getValueCardinality ()
        {
            return m_valueCardinality;
        }
        String [] getRequiresSet ()
        {
            return m_requiresSet;
        }
        String [] getExcludesSet ()
        {
            return m_excludesSet;
        }
        String getDescription ()
        {
            return m_description;
        }
        void setNames (final String [] names)
        {
            if (names == null) throw new IllegalArgumentException ("null input: names");
            m_names = names;
        }
        void setRequired (final boolean required)
        {
            m_required = required;
        }
        void setValueMnemonic (final String mnemonic)
        {
            if (mnemonic == null) throw new IllegalArgumentException ("null input: mnemonic");
            m_valueMnemonic = mnemonic;
        }
        void setMergeable (final boolean mergeable)
        {
            m_mergeable = mergeable;
        }
        void setDetailedOnly (final boolean detailedOnly)
        {
            m_detailedOnly = detailedOnly;
        }
        void setPattern (final boolean pattern)
        {
            m_pattern = pattern;
        }
        void setValueCardinality (final int [] cardinality)
        {
            if ((cardinality == null) || (cardinality.length != 2)) throw new IllegalArgumentException ("null or invalid input: cardinality");
            m_valueCardinality = cardinality;
        }
        void setRequiresSet (final String [] names)
        {
            if (names == null) throw new IllegalArgumentException ("null input: names"); 
            m_requiresSet = names.length > 0 ? names : null;
        }
        void setExcludesSet (final String [] names)
        {
            if (names == null) throw new IllegalArgumentException ("null input: names");
            m_excludesSet = names.length > 0 ? names : null;
        }
        void setDescription (final String description)
        {
            if (description == null) throw new IllegalArgumentException ("null input: description");
            m_description = description;
        }
        static final int [] C_ZERO = new int [] {0, 0};
        static final int [] C_ONE = new int [] {1, 1};
        static final int [] C_ZERO_OR_ONE = new int [] {0, 1};
        static final int [] C_ZERO_OR_MORE = new int [] {0, Integer.MAX_VALUE};
        static final int [] C_ONE_OR_MORE = new int [] {1, Integer.MAX_VALUE};
        private final boolean m_usage;
        private String [] m_names;
        private boolean m_required;
        private String m_valueMnemonic;
        private boolean m_mergeable;
        private boolean m_detailedOnly;
        private boolean m_pattern;
        private int [] m_valueCardinality;
        private String [] m_requiresSet, m_excludesSet;
        private String m_description;
    } 
    static final class OptDefMetadata
    {
        OptDefMetadata ()
        {
            m_optdefs = new ArrayList ();
            m_optdefMap = new HashMap ();
            m_requiredOpts = new HashSet ();
            m_patternOptDefMap = new HashMap ();
        }
        OptDef getOptDef (final String name, final String [] prefixout)
        {
            if (name == null) throw new IllegalArgumentException ("null input: name");
            if (prefixout != null) prefixout [0] = null;
            OptDef result = (OptDef) m_optdefMap.get (name);
            if (result == null)
            {
                for (Iterator ps = m_patternOptDefMap.entrySet ().iterator ();
                     ps.hasNext (); )
                {
                    final Map.Entry entry = (Map.Entry) ps.next ();
                    final String pattern = (String) entry.getKey ();
                    if (name.startsWith (pattern))
                    {
                        if (prefixout != null) prefixout [0] = pattern;
                        result = (OptDef) entry.getValue ();
                        break;
                    }
                }
            }
            return result;
        }
        Iterator  getOptDefs ()
        {
            return m_optdefs.iterator ();
        }
        OptDef getPatternOptDefs (final String pattern) 
        {
            if (pattern == null) throw new IllegalArgumentException ("null input: pattern");
            return (OptDef) m_patternOptDefMap.get (pattern);
        }
        Set  getRequiredOpts ()
        {
            return m_requiredOpts;
        }
        OptDef getUsageOptDef ()
        {
            return m_usageOptDef;
        }
        void addOptDef (final OptDef optdef)
        {
            if (optdef == null) throw new IllegalArgumentException ("null input: optdef");
            final Map map = optdef.isPattern () ? m_patternOptDefMap : m_optdefMap;
            final String [] names = optdef.getNames ();
            for (int n = 0; n < names.length; ++ n)
            {
                if (map.containsKey (names [n]))
                    throw new IllegalArgumentException ("duplicate option name [" + names [n] + "]");
                map.put (names [n], optdef);
            }
            m_optdefs.add (optdef);
            if (optdef.isRequired ())
                m_requiredOpts.add (optdef.getCanonicalName ());
            if (optdef.isUsage ())
            {
                if (m_usageOptDef != null)
                    throw new IllegalArgumentException ("usage optdef set already");
                m_usageOptDef = optdef;
            }
        }
        final List  m_optdefs; 
        final Map  m_optdefMap;
        final Set  m_requiredOpts;
        final Map  m_patternOptDefMap;
        private OptDef m_usageOptDef; 
    } 
    static final class MetadataParser
    {
         OptDef [] parse (final Reader in)
         {
             if (in == null) throw new IllegalArgumentException ("null input: in");             
             m_in = in;
             nextChar ();
             nextToken ();
             while (m_token != Token.EOF)
             {
                 if (m_opts == null) m_opts = new ArrayList ();
                 m_opts.add (optdef ());
             }
             final OptDef [] result;
             if ((m_opts == null) || (m_opts.size () == 0))
                result = EMPTY_OPTDEF_ARRAY;
             else
             {
                 result = new OptDef [m_opts.size ()];
                 m_opts.toArray (result);
             }
             m_in = null;
             m_opts = null;
             return result;
         }
         OptDef optdef ()
         {
             final OptDef optdef = new OptDef (false); 
             optdef.setNames (optnamelist ());
             accept (Token.COLON_ID);
             optmetadata (optdef);
             accept (Token.SEMICOLON_ID);
             return optdef;
         }
         String [] optnamelist ()
         {
             return namelist ();
         }
         void optmetadata (final OptDef optdef)
         {
             switch (m_token.getID ())
             {
                 case Token.REQUIRED_ID:
                 {
                     accept ();
                     optdef.setRequired (true);
                 }
                 break;
                 case Token.OPTIONAL_ID:
                 {
                     accept ();
                     optdef.setRequired (false);
                 }
                 break;
                 default:
                    throw new IllegalArgumentException ("parse error: invalid token " + m_token + ", expected " + Token.REQUIRED + " or " + Token.OPTIONAL);
             } 
             accept (Token.COMMA_ID);
             if (m_token.getID () == Token.MERGEABLE_ID)
             {
                 accept ();
                 optdef.setMergeable (true);
                 accept (Token.COMMA_ID);
             }
             if (m_token.getID () == Token.DETAILEDONLY_ID)
             {
                 accept ();
                 optdef.setDetailedOnly (true);
                 accept (Token.COMMA_ID);
             }
             if (m_token.getID () == Token.PATTERN_ID)
             {
                 accept ();
                 optdef.setPattern (true);
                 accept (Token.COMMA_ID);
             }
             accept (Token.VALUES_ID);
             accept (Token.COLON_ID);
             optdef.setValueCardinality (cardinality ());
             accept (Token.COMMA_ID);
             if (m_token.getID () == Token.STRING_ID)
             {
                 optdef.setValueMnemonic (m_token.getValue ());
                 accept ();
                 accept (Token.COMMA_ID);
             }
             if (m_token.getID () == Token.REQUIRES_ID)
             {
                 accept ();
                 accept (Token.LBRACKET_ID);
                 optdef.setRequiresSet (namelist ());
                 accept (Token.RBRACKET_ID);
                 accept (Token.COMMA_ID);
             }
             if (m_token.getID () == Token.EXCLUDES_ID)
             {
                 accept ();
                 accept (Token.LBRACKET_ID);
                 optdef.setExcludesSet (namelist ());
                 accept (Token.RBRACKET_ID);
                 accept (Token.COMMA_ID);
             }
             optdef.setDescription (accept (Token.TEXT_ID).getValue ());
         }
         int [] cardinality ()
         {
             final Token result = accept (Token.CARD_ID);
             if ("0".equals (result.getValue ()))
                return OptDef.C_ZERO;
             else if ("1".equals (result.getValue ()))
                return OptDef.C_ONE;
             else 
                return OptDef.C_ZERO_OR_ONE;
         }
         String [] namelist ()
         {
             final List _result = new ArrayList ();
             _result.add (accept (Token.STRING_ID).getValue ());
             while (m_token.getID () == Token.COMMA_ID)
             {
                 accept ();
                 _result.add (accept (Token.STRING_ID).getValue ());
             }
             final String [] result = new String [_result.size ()];
             _result.toArray (result);
             return result;
         }
         Token accept ()
         {
             final Token current = m_token;
             nextToken ();
             return current;
         }
         Token accept (final int tokenID)
         {
             final Token current = m_token;
             if (m_token.getID () == tokenID)
                nextToken ();
             else
                throw new IllegalArgumentException ("parse error: invalid token [" + m_token + "], expected type [" + tokenID + "]");
             return current;
         }
         void nextToken ()
         {
             consumeWS ();
             switch (m_currentChar)
             {
                 case -1: m_token = Token.EOF; break;
                 case ':':
                 {
                     nextChar ();
                     m_token = Token.COLON;
                 }
                 break;
                 case ';':
                 {
                     nextChar ();
                     m_token = Token.SEMICOLON;
                 }
                 break;
                 case ',':
                 {
                     nextChar ();
                     m_token = Token.COMMA;
                 }
                 break;
                 case '{':
                 {
                     nextChar ();
                     m_token = Token.LBRACKET;
                 }
                 break;
                 case '}':
                 {
                     nextChar ();
                     m_token = Token.RBRACKET;
                 }
                 break;
                 case '0':
                 {
                     nextChar ();
                     m_token = new Token (Token.CARD_ID, "0");
                 }
                 break;
                 case '1':
                 {
                     nextChar ();
                     m_token = new Token (Token.CARD_ID, "1");
                 }
                 break;
                 case '?':
                 {
                     nextChar ();
                     m_token = new Token (Token.CARD_ID, "?");
                 }
                 break;
                 case '\'':
                 {
                     final StringBuffer value = new StringBuffer ();
                     nextChar ();
                     while (m_currentChar != '\'')
                     {
                         value.append ((char) m_currentChar);
                         nextChar ();
                     }
                     nextChar ();
                     m_token = new Token (Token.STRING_ID, value.toString ());
                 }
                 break;
                 case '\"':
                 {
                     final StringBuffer value = new StringBuffer ();
                     nextChar ();
                     while (m_currentChar != '\"')
                     {
                         value.append ((char) m_currentChar);
                         nextChar ();
                     }
                     nextChar ();
                     m_token = new Token (Token.TEXT_ID, value.toString ());
                 }
                 break;
                 default:
                 {
                     final StringBuffer value = new StringBuffer ();
                     while (Character.isLetter ((char) m_currentChar))
                     {
                         value.append ((char) m_currentChar);
                         nextChar ();
                     }
                     final Token token = (Token) KEYWORDS.get (value.toString ());
                     if (token == null)
                        throw new IllegalArgumentException ("parse error: unrecognized keyword [" + value  + "]");
                     m_token = token;
                 }
             } 
         }
         private void consumeWS ()
         {
             if (m_currentChar == -1)
                return;
             else
             {
                 while (Character.isWhitespace ((char) m_currentChar))
                 {
                    nextChar ();
                 }
             }
         }
         private void nextChar ()
         {
             try
             {
                m_currentChar = m_in.read ();
             }
             catch (IOException ioe)
             {
                 throw new RuntimeException ("I/O error while parsing: " + ioe); 
             }
         }
         private Reader m_in;
         private List m_opts;
         private Token m_token;
         private int m_currentChar;
         private static final Map KEYWORDS;
         private static final OptDef [] EMPTY_OPTDEF_ARRAY = new OptDef [0];
         static
         {
             KEYWORDS = new HashMap (17);
             KEYWORDS.put (Token.OPTIONAL.getValue (), Token.OPTIONAL);
             KEYWORDS.put (Token.REQUIRED.getValue (), Token.REQUIRED);
             KEYWORDS.put (Token.VALUES.getValue (), Token.VALUES);
             KEYWORDS.put (Token.REQUIRES.getValue (), Token.REQUIRES);
             KEYWORDS.put (Token.EXCLUDES.getValue (), Token.EXCLUDES);
             KEYWORDS.put (Token.MERGEABLE.getValue (), Token.MERGEABLE);
             KEYWORDS.put (Token.DETAILEDONLY.getValue (), Token.DETAILEDONLY);
             KEYWORDS.put (Token.PATTERN.getValue (), Token.PATTERN);
         }
    } 
    OptsParser (final String metadataResourceName, final ClassLoader loader, final String [] usageOpts)
    {
        this (metadataResourceName, loader, null, usageOpts);
    }
    OptsParser (final String metadataResourceName, final ClassLoader loader, final String msgPrefix, final String [] usageOpts)
    {
        if (metadataResourceName == null) throw new IllegalArgumentException ("null input: metadataResourceName");
        m_msgPrefix = msgPrefix;
        InputStream in = null;
        try
        {
            in = ResourceLoader.getResourceAsStream (metadataResourceName, loader);
            if (in == null)
                throw new IllegalArgumentException ("resource [" + metadataResourceName + "] could not be loaded via [" + loader + "]");
            final Reader rin = new InputStreamReader (in);
            m_metadata = parseOptDefMetadata (rin, usageOpts);   
        }
        finally
        {
            if (in != null) try { in.close (); } catch (IOException ignore) {} 
        }
    }
    private static final class Token
    {
        Token (final int ID, final String value)
        {
            if (value == null) throw new IllegalArgumentException ("null input: value");
            m_ID = ID;
            m_value = value;
        }
        int getID ()
        {
            return m_ID;
        }
        String getValue ()
        {
            return m_value;
        }
        public String toString ()
        {
            return m_ID + ": [" + m_value + "]";
        }
        static final int EOF_ID = 0;
        static final int STRING_ID = 1;
        static final int COLON_ID = 2;
        static final int SEMICOLON_ID = 3;
        static final int COMMA_ID = 4;
        static final int LBRACKET_ID = 5;
        static final int RBRACKET_ID = 6;
        static final int OPTIONAL_ID = 7;
        static final int REQUIRED_ID = 8;
        static final int CARD_ID = 9;
        static final int VALUES_ID = 10;
        static final int TEXT_ID = 11;
        static final int REQUIRES_ID = 12;
        static final int EXCLUDES_ID = 13;
        static final int MERGEABLE_ID = 14;
        static final int DETAILEDONLY_ID = 15;
        static final int PATTERN_ID = 16;
        static final Token EOF = new Token (EOF_ID, "<EOF>");
        static final Token COLON = new Token (COLON_ID, ":");
        static final Token SEMICOLON = new Token (SEMICOLON_ID, ";");
        static final Token COMMA = new Token (COMMA_ID, ",");
        static final Token LBRACKET = new Token (LBRACKET_ID, "{");
        static final Token RBRACKET = new Token (RBRACKET_ID, "}");
        static final Token OPTIONAL = new Token (OPTIONAL_ID, "optional");
        static final Token REQUIRED = new Token (REQUIRED_ID, "required");
        static final Token VALUES = new Token (VALUES_ID, "values");
        static final Token REQUIRES = new Token (REQUIRES_ID, "requires");
        static final Token EXCLUDES = new Token (EXCLUDES_ID, "excludes");
        static final Token MERGEABLE = new Token (MERGEABLE_ID, "mergeable");
        static final Token DETAILEDONLY = new Token (DETAILEDONLY_ID, "detailedonly");
        static final Token PATTERN = new Token (PATTERN_ID, "pattern");
        private final int m_ID;
        private final String m_value;
    } 
    private static OptDefMetadata parseOptDefMetadata (final Reader in, final String [] usageOpts)
    {
        final MetadataParser parser = new MetadataParser ();
        final OptDef [] optdefs = parser.parse (in);
        final OptDefMetadata result = new OptDefMetadata ();
        for (int o = 0; o < optdefs.length; ++ o)
        {
            result.addOptDef (optdefs [o]);
        }
        if (usageOpts != null)
        {
            final OptDef usage = new OptDef (true);
            usage.setNames (usageOpts);
            usage.setDescription ("display usage information");
            usage.setValueCardinality (OptDef.C_ZERO);
            usage.setRequired (false);
            usage.setDetailedOnly (false);
            usage.setMergeable (false);
            result.addOptDef (usage);
        }
        for (int o = 0; o < optdefs.length; ++ o)
        {
            final OptDef optdef = optdefs [o];
            final String [] requires = optdef.getRequiresSet ();
            if (requires != null)
            {
                for (int r = 0; r < requires.length; ++ r)
                {
                    final OptDef ropt = result.getOptDef (requires [r], null);
                    if (ropt == null)
                        throw new IllegalArgumentException ("option [" + optdef.getCanonicalName () + "] specifies an unknown option [" + requires [r] + "] in its \'requires\' set");
                    if (ropt == optdef)
                        throw new IllegalArgumentException ("option [" + optdef.getCanonicalName () + "] specifies itself in its \'requires\' set");
                }
            }
            final String [] excludes = optdef.getExcludesSet ();
            if (excludes != null)
            {
                for (int x = 0; x < excludes.length; ++ x)
                {
                    final OptDef xopt = result.getOptDef (excludes [x], null);
                    if (xopt == null)
                        throw new IllegalArgumentException ("option [" + optdef.getCanonicalName () + "] specifies an unknown option [" + excludes [x] + "] in its \'excludes\' set");
                    if (xopt.isRequired ())
                        throw new IllegalArgumentException ("option [" + optdef.getCanonicalName () + "] specifies a required option [" + excludes [x] + "] in its \'excludes\' set");
                    if (xopt == optdef)
                        throw new IllegalArgumentException ("option [" + optdef.getCanonicalName () + "] specifies itself in its \'excludes\' set");
                }
            }
        }
        return result;
    }
    private String formatMessage (final String msg)
    {
        if (m_msgPrefix == null) return msg;
        else
        {
            return m_msgPrefix.concat (msg);
        }
    }
    private final String m_msgPrefix;
    private final OptDefMetadata m_metadata;
    private static final int CANONICAL_OPT_PREFIX = 1; 
    private static final String [] OPT_PREFIXES = new String [] {"--", "-"}; 
    private static final char [] OPT_VALUE_SEPARATORS = new char [] {':', '='};
    private static final int STATE_OPT = 0, STATE_OPT_VALUE = 1, STATE_FREE_ARGS = 2, STATE_ERROR = 3;
} 
