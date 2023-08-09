class SpreadGeneric {
    private final MethodType targetType;
    private final int spreadCount;
    private final Adapter adapter;
    private final MethodHandle entryPoint;
    private SpreadGeneric(MethodType targetType, int spreadCount) {
        assert(targetType == targetType.generic());
        this.targetType = targetType;
        this.spreadCount = spreadCount;
        MethodHandle[] ep = { null };
        Adapter ad = findAdapter(this, ep);
        if (ad != null) {
            this.adapter = ad;
            this.entryPoint = ep[0];
            return;
        }
        this.adapter = buildAdapterFromBytecodes(targetType, spreadCount, ep);
        this.entryPoint = ep[0];
    }
    static {
        assert(MethodHandleNatives.workaroundWithoutRicochetFrames());  
    }
    static MethodType preSpreadType(MethodType targetType, int spreadCount) {
        @SuppressWarnings("unchecked")
        ArrayList<Class<?>> params = new ArrayList(targetType.parameterList());
        int outargs = params.size();
        params.subList(outargs - spreadCount, outargs).clear();
        params.add(Object.class);
        return MethodType.methodType(targetType.returnType(), params);
    }
    MethodHandle makeInstance(MethodHandle target) {
        MethodType type = target.type();
        if (type != targetType) {
            throw new UnsupportedOperationException("NYI type="+type);
        }
        return adapter.makeInstance(this, target);
    }
    public static MethodHandle make(MethodHandle target, int spreadCount) {
        MethodType type = target.type();
        MethodType gtype = type.generic();
        if (type == gtype) {
            return SpreadGeneric.of(type, spreadCount).makeInstance(target);
        } else {
            MethodHandle gtarget = FromGeneric.make(target);
            assert(gtarget.type() == gtype);
            MethodHandle gspread = SpreadGeneric.of(gtype, spreadCount).makeInstance(gtarget);
            return ToGeneric.make(preSpreadType(type, spreadCount), gspread);
        }
    }
    static SpreadGeneric of(MethodType targetType, int spreadCount) {
        if (targetType != targetType.generic())
            throw new UnsupportedOperationException("NYI type="+targetType);
        MethodTypeForm form = targetType.form();
        int outcount = form.parameterCount();
        assert(spreadCount <= outcount);
        SpreadGeneric[] spreadGens = form.spreadGeneric;
        if (spreadGens == null)
            form.spreadGeneric = spreadGens = new SpreadGeneric[outcount+1];
        SpreadGeneric spreadGen = spreadGens[spreadCount];
        if (spreadGen == null)
            spreadGens[spreadCount] = spreadGen = new SpreadGeneric(form.erasedType(), spreadCount);
        return spreadGen;
    }
    String debugString() {
        return getClass().getSimpleName()+targetType+"["+spreadCount+"]";
    }
    protected Object check(Object av, int n) {
        checkSpreadArgument(av, n);
        return av;
    }
    protected Object select(Object av, int n) {
        return ((Object[])av)[n];
    }
    static Adapter findAdapter(SpreadGeneric outer, MethodHandle[] ep) {
        MethodType targetType = outer.targetType;
        int spreadCount = outer.spreadCount;
        int outargs = targetType.parameterCount();
        int inargs = outargs - spreadCount;
        if (inargs < 0)  return null;
        MethodType entryType = MethodType.genericMethodType(inargs + 1); 
        String cname1 = "S" + outargs;
        String[] cnames = { cname1 };
        String iname = "invoke_S"+spreadCount;
        for (String cname : cnames) {
            Class<? extends Adapter> acls = Adapter.findSubClass(cname);
            if (acls == null)  continue;
            MethodHandle entryPoint = null;
            try {
                entryPoint = IMPL_LOOKUP.findSpecial(acls, iname, entryType, acls);
            } catch (ReflectiveOperationException ex) {
            }
            if (entryPoint == null)  continue;
            Constructor<? extends Adapter> ctor = null;
            try {
                ctor = acls.getDeclaredConstructor(SpreadGeneric.class);
            } catch (NoSuchMethodException ex) {
            } catch (SecurityException ex) {
            }
            if (ctor == null)  continue;
            try {
                Adapter ad = ctor.newInstance(outer);
                ep[0] = entryPoint;
                return ad;
            } catch (IllegalArgumentException ex) {
            } catch (InvocationTargetException wex) {
                Throwable ex = wex.getTargetException();
                if (ex instanceof Error)  throw (Error)ex;
                if (ex instanceof RuntimeException)  throw (RuntimeException)ex;
            } catch (InstantiationException ex) {
            } catch (IllegalAccessException ex) {
            }
        }
        return null;
    }
    static Adapter buildAdapterFromBytecodes(MethodType targetType,
            int spreadCount, MethodHandle[] ep) {
        throw new UnsupportedOperationException("NYI");
    }
    static abstract class Adapter extends BoundMethodHandle {
        protected final SpreadGeneric outer;
        protected final MethodHandle target;   
        @Override
        String debugString() {
            return addTypeString(target, this);
        }
        static final MethodHandle NO_ENTRY = ValueConversions.identity();
        protected boolean isPrototype() { return target == null; }
        protected Adapter(SpreadGeneric outer) {
            super(NO_ENTRY);
            this.outer = outer;
            this.target = null;
            assert(isPrototype());
        }
        protected Adapter(SpreadGeneric outer, MethodHandle target) {
            super(outer.entryPoint);
            this.outer = outer;
            this.target = target;
        }
        protected abstract Adapter makeInstance(SpreadGeneric outer, MethodHandle target);
        protected Object check(Object av, int n) {
            return outer.check(av, n);
        }
        protected Object select(Object av, int n) {
            return outer.select(av, n);
        }
        static private final String CLASS_PREFIX; 
        static {
            String aname = Adapter.class.getName();
            String sname = Adapter.class.getSimpleName();
            if (!aname.endsWith(sname))  throw new InternalError();
            CLASS_PREFIX = aname.substring(0, aname.length() - sname.length());
        }
        static Class<? extends Adapter> findSubClass(String name) {
            String cname = Adapter.CLASS_PREFIX + name;
            try {
                return Class.forName(cname).asSubclass(Adapter.class);
            } catch (ClassNotFoundException ex) {
                return null;
            } catch (ClassCastException ex) {
                return null;
            }
        }
    }
    /* generated classes follow this pattern:
    static class xS2 extends Adapter {
        protected xS2(SpreadGeneric outer) { super(outer); }  
        protected xS2(SpreadGeneric outer, MethodHandle t) { super(outer, t); }
        protected xS2 makeInstance(SpreadGeneric outer, MethodHandle t) { return new xS2(outer, t); }
        protected Object invoke_S0(Object a0, Object a1, Object av) throws Throwable { av = super.check(av,0);
             return target.invokeExact(a0, a1)); }
        protected Object invoke_S1(Object a0, Object av) throws Throwable { av = super.check(av,1);
             return target.invokeExact(a0,
                super.select(av,0)); }
        protected Object invoke_S2(Object a0, Object av) throws Throwable { av = super.check(av,1);
             return target.invokeExact(
                super.select(av,0), super.select(av,1)); }
    }
/*
: SHELL; n=SpreadGeneric; cp -p $n.java $n.java-; sed < $n.java- > $n.java+ -e '/{{*{{/,/}}*}}/w /tmp/genclasses.java' -e '/}}*}}/q'; (cd /tmp; javac -d . genclasses.java; java -cp . genclasses) >> $n.java+; echo '}' >> $n.java+; mv $n.java+ $n.java; mv $n.java- $n.java~
import java.util.*;
class genclasses {
    static String[][] TEMPLATES = { {
        "@for@ N=0..10",
        "    
        "    static class @cat@ extends Adapter {",
        "        protected @cat@(SpreadGeneric outer) { super(outer); }  
        "        protected @cat@(SpreadGeneric outer, MethodHandle t) { super(outer, t); }",
        "        protected @cat@ makeInstance(SpreadGeneric outer, MethodHandle t) { return new @cat@(outer, t); }",
        "        protected Object invoke_S0(@Tvav,@Object av) throws Throwable { av = super.check(av, 0);",
        "            return target.invokeExact(@av@); }",
        "        
        "        protected Object invoke_S@S@(@Tvav,@Object av) throws Throwable { av = super.check(av, @S@);",
        "            return target.invokeExact(@av,@@sv@); }",
        "        
        "    }",
    } };
    static final String NEWLINE_INDENT = "\n                ";
    enum VAR {
        cat, N, S, av, av_, Tvav_, sv;
        public final String pattern = "@"+toString().replace('_','.')+"@";
        public String binding = toString();
        static void makeBindings(boolean topLevel, int outargs, int spread) {
            int inargs = outargs - spread;
            VAR.cat.binding = "S"+outargs;
            VAR.N.binding = String.valueOf(outargs); 
            VAR.S.binding = String.valueOf(spread);  
            String[] av = new String[inargs];
            String[] Tvav = new String[inargs];
            for (int i = 0; i < inargs; i++) {
                av[i] = arg(i);
                Tvav[i] = param("Object", av[i]);
            }
            VAR.av.binding = comma(av);
            VAR.av_.binding = comma(av, ", ");
            VAR.Tvav_.binding = comma(Tvav, ", ");
            String[] sv = new String[spread];
            for (int i = 0; i < spread; i++) {
                String spc = "";
                if (i % 4 == 0) spc = NEWLINE_INDENT;
                sv[i] = spc+"super.select(av,"+i+")";
            }
            VAR.sv.binding = comma(sv);
        }
        static String arg(int i) { return "a"+i; }
        static String param(String t, String a) { return t+" "+a; }
        static String comma(String[] v) { return comma(v, ""); }
        static String comma(String[] v, String sep) {
            if (v.length == 0)  return "";
            String res = v[0];
            for (int i = 1; i < v.length; i++)  res += ", "+v[i];
            return res + sep;
        }
        static String transform(String string) {
            for (VAR var : values())
                string = string.replaceAll(var.pattern, var.binding);
            return string;
        }
    }
    static String[] stringsIn(String[] strings, int beg, int end) {
        return Arrays.copyOfRange(strings, beg, Math.min(end, strings.length));
    }
    static String[] stringsBefore(String[] strings, int pos) {
        return stringsIn(strings, 0, pos);
    }
    static String[] stringsAfter(String[] strings, int pos) {
        return stringsIn(strings, pos, strings.length);
    }
    static int indexAfter(String[] strings, int pos, String tag) {
        return Math.min(indexBefore(strings, pos, tag) + 1, strings.length);
    }
    static int indexBefore(String[] strings, int pos, String tag) {
        for (int i = pos, end = strings.length; ; i++) {
            if (i == end || strings[i].endsWith(tag))  return i;
        }
    }
    static int MIN_ARITY, MAX_ARITY;
    public static void main(String... av) {
        for (String[] template : TEMPLATES) {
            int forLinesLimit = indexBefore(template, 0, "@each-cat@");
            String[] forLines = stringsBefore(template, forLinesLimit);
            template = stringsAfter(template, forLinesLimit);
            for (String forLine : forLines)
                expandTemplate(forLine, template);
        }
    }
    static void expandTemplate(String forLine, String[] template) {
        String[] params = forLine.split("[^0-9]+");
        if (params[0].length() == 0)  params = stringsAfter(params, 1);
        System.out.println("
        int pcur = 0;
        MIN_ARITY = Integer.valueOf(params[pcur++]);
        MAX_ARITY = Integer.valueOf(params[pcur++]);
        if (pcur != params.length)  throw new RuntimeException("bad extra param: "+forLine);
        for (int outargs = MIN_ARITY; outargs <= MAX_ARITY; outargs++) {
            expandTemplate(template, true, outargs, 0);
        }
    }
    static void expandTemplate(String[] template, boolean topLevel, int outargs, int spread) {
        VAR.makeBindings(topLevel, outargs, spread);
        for (int i = 0; i < template.length; i++) {
            String line = template[i];
            if (line.endsWith("@each-cat@")) {
            } else if (line.endsWith("@each-S@")) {
                int blockEnd = indexAfter(template, i, "@end-S@");
                String[] block = stringsIn(template, i+1, blockEnd-1);
                for (int spread1 = spread+1; spread1 <= outargs; spread1++)
                    expandTemplate(block, false, outargs, spread1);
                VAR.makeBindings(topLevel, outargs, spread);
                i = blockEnd-1; continue;
            } else {
                System.out.println(VAR.transform(line));
            }
        }
    }
}
    static class S0 extends Adapter {
        protected S0(SpreadGeneric outer) { super(outer); }  
        protected S0(SpreadGeneric outer, MethodHandle t) { super(outer, t); }
        protected S0 makeInstance(SpreadGeneric outer, MethodHandle t) { return new S0(outer, t); }
        protected Object invoke_S0(Object av) throws Throwable { av = super.check(av, 0);
            return target.invokeExact(); }
    }
    static class S1 extends Adapter {
        protected S1(SpreadGeneric outer) { super(outer); }  
        protected S1(SpreadGeneric outer, MethodHandle t) { super(outer, t); }
        protected S1 makeInstance(SpreadGeneric outer, MethodHandle t) { return new S1(outer, t); }
        protected Object invoke_S0(Object a0, Object av) throws Throwable { av = super.check(av, 0);
            return target.invokeExact(a0); }
        protected Object invoke_S1(Object av) throws Throwable { av = super.check(av, 1);
            return target.invokeExact(
                super.select(av,0)); }
    }
    static class S2 extends Adapter {
        protected S2(SpreadGeneric outer) { super(outer); }  
        protected S2(SpreadGeneric outer, MethodHandle t) { super(outer, t); }
        protected S2 makeInstance(SpreadGeneric outer, MethodHandle t) { return new S2(outer, t); }
        protected Object invoke_S0(Object a0, Object a1, Object av) throws Throwable { av = super.check(av, 0);
            return target.invokeExact(a0, a1); }
        protected Object invoke_S1(Object a0, Object av) throws Throwable { av = super.check(av, 1);
            return target.invokeExact(a0,
                super.select(av,0)); }
        protected Object invoke_S2(Object av) throws Throwable { av = super.check(av, 2);
            return target.invokeExact(
                super.select(av,0), super.select(av,1)); }
    }
    static class S3 extends Adapter {
        protected S3(SpreadGeneric outer) { super(outer); }  
        protected S3(SpreadGeneric outer, MethodHandle t) { super(outer, t); }
        protected S3 makeInstance(SpreadGeneric outer, MethodHandle t) { return new S3(outer, t); }
        protected Object invoke_S0(Object a0, Object a1, Object a2, Object av) throws Throwable { av = super.check(av, 0);
            return target.invokeExact(a0, a1, a2); }
        protected Object invoke_S1(Object a0, Object a1, Object av) throws Throwable { av = super.check(av, 1);
            return target.invokeExact(a0, a1,
                super.select(av,0)); }
        protected Object invoke_S2(Object a0, Object av) throws Throwable { av = super.check(av, 2);
            return target.invokeExact(a0,
                super.select(av,0), super.select(av,1)); }
        protected Object invoke_S3(Object av) throws Throwable { av = super.check(av, 3);
            return target.invokeExact(
                super.select(av,0), super.select(av,1), super.select(av,2)); }
    }
    static class S4 extends Adapter {
        protected S4(SpreadGeneric outer) { super(outer); }  
        protected S4(SpreadGeneric outer, MethodHandle t) { super(outer, t); }
        protected S4 makeInstance(SpreadGeneric outer, MethodHandle t) { return new S4(outer, t); }
        protected Object invoke_S0(Object a0, Object a1, Object a2, Object a3, Object av) throws Throwable { av = super.check(av, 0);
            return target.invokeExact(a0, a1, a2, a3); }
        protected Object invoke_S1(Object a0, Object a1, Object a2, Object av) throws Throwable { av = super.check(av, 1);
            return target.invokeExact(a0, a1, a2,
                super.select(av,0)); }
        protected Object invoke_S2(Object a0, Object a1, Object av) throws Throwable { av = super.check(av, 2);
            return target.invokeExact(a0, a1,
                super.select(av,0), super.select(av,1)); }
        protected Object invoke_S3(Object a0, Object av) throws Throwable { av = super.check(av, 3);
            return target.invokeExact(a0,
                super.select(av,0), super.select(av,1), super.select(av,2)); }
        protected Object invoke_S4(Object av) throws Throwable { av = super.check(av, 4);
            return target.invokeExact(
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3)); }
    }
    static class S5 extends Adapter {
        protected S5(SpreadGeneric outer) { super(outer); }  
        protected S5(SpreadGeneric outer, MethodHandle t) { super(outer, t); }
        protected S5 makeInstance(SpreadGeneric outer, MethodHandle t) { return new S5(outer, t); }
        protected Object invoke_S0(Object a0, Object a1, Object a2, Object a3, Object a4, Object av) throws Throwable { av = super.check(av, 0);
            return target.invokeExact(a0, a1, a2, a3, a4); }
        protected Object invoke_S1(Object a0, Object a1, Object a2, Object a3, Object av) throws Throwable { av = super.check(av, 1);
            return target.invokeExact(a0, a1, a2, a3,
                super.select(av,0)); }
        protected Object invoke_S2(Object a0, Object a1, Object a2, Object av) throws Throwable { av = super.check(av, 2);
            return target.invokeExact(a0, a1, a2,
                super.select(av,0), super.select(av,1)); }
        protected Object invoke_S3(Object a0, Object a1, Object av) throws Throwable { av = super.check(av, 3);
            return target.invokeExact(a0, a1,
                super.select(av,0), super.select(av,1), super.select(av,2)); }
        protected Object invoke_S4(Object a0, Object av) throws Throwable { av = super.check(av, 4);
            return target.invokeExact(a0,
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3)); }
        protected Object invoke_S5(Object av) throws Throwable { av = super.check(av, 5);
            return target.invokeExact(
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3),
                super.select(av,4)); }
    }
    static class S6 extends Adapter {
        protected S6(SpreadGeneric outer) { super(outer); }  
        protected S6(SpreadGeneric outer, MethodHandle t) { super(outer, t); }
        protected S6 makeInstance(SpreadGeneric outer, MethodHandle t) { return new S6(outer, t); }
        protected Object invoke_S0(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object av) throws Throwable { av = super.check(av, 0);
            return target.invokeExact(a0, a1, a2, a3, a4, a5); }
        protected Object invoke_S1(Object a0, Object a1, Object a2, Object a3, Object a4, Object av) throws Throwable { av = super.check(av, 1);
            return target.invokeExact(a0, a1, a2, a3, a4,
                super.select(av,0)); }
        protected Object invoke_S2(Object a0, Object a1, Object a2, Object a3, Object av) throws Throwable { av = super.check(av, 2);
            return target.invokeExact(a0, a1, a2, a3,
                super.select(av,0), super.select(av,1)); }
        protected Object invoke_S3(Object a0, Object a1, Object a2, Object av) throws Throwable { av = super.check(av, 3);
            return target.invokeExact(a0, a1, a2,
                super.select(av,0), super.select(av,1), super.select(av,2)); }
        protected Object invoke_S4(Object a0, Object a1, Object av) throws Throwable { av = super.check(av, 4);
            return target.invokeExact(a0, a1,
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3)); }
        protected Object invoke_S5(Object a0, Object av) throws Throwable { av = super.check(av, 5);
            return target.invokeExact(a0,
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3),
                super.select(av,4)); }
        protected Object invoke_S6(Object av) throws Throwable { av = super.check(av, 6);
            return target.invokeExact(
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3),
                super.select(av,4), super.select(av,5)); }
    }
    static class S7 extends Adapter {
        protected S7(SpreadGeneric outer) { super(outer); }  
        protected S7(SpreadGeneric outer, MethodHandle t) { super(outer, t); }
        protected S7 makeInstance(SpreadGeneric outer, MethodHandle t) { return new S7(outer, t); }
        protected Object invoke_S0(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object av) throws Throwable { av = super.check(av, 0);
            return target.invokeExact(a0, a1, a2, a3, a4, a5, a6); }
        protected Object invoke_S1(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object av) throws Throwable { av = super.check(av, 1);
            return target.invokeExact(a0, a1, a2, a3, a4, a5,
                super.select(av,0)); }
        protected Object invoke_S2(Object a0, Object a1, Object a2, Object a3, Object a4, Object av) throws Throwable { av = super.check(av, 2);
            return target.invokeExact(a0, a1, a2, a3, a4,
                super.select(av,0), super.select(av,1)); }
        protected Object invoke_S3(Object a0, Object a1, Object a2, Object a3, Object av) throws Throwable { av = super.check(av, 3);
            return target.invokeExact(a0, a1, a2, a3,
                super.select(av,0), super.select(av,1), super.select(av,2)); }
        protected Object invoke_S4(Object a0, Object a1, Object a2, Object av) throws Throwable { av = super.check(av, 4);
            return target.invokeExact(a0, a1, a2,
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3)); }
        protected Object invoke_S5(Object a0, Object a1, Object av) throws Throwable { av = super.check(av, 5);
            return target.invokeExact(a0, a1,
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3),
                super.select(av,4)); }
        protected Object invoke_S6(Object a0, Object av) throws Throwable { av = super.check(av, 6);
            return target.invokeExact(a0,
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3),
                super.select(av,4), super.select(av,5)); }
        protected Object invoke_S7(Object av) throws Throwable { av = super.check(av, 7);
            return target.invokeExact(
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3),
                super.select(av,4), super.select(av,5), super.select(av,6)); }
    }
    static class S8 extends Adapter {
        protected S8(SpreadGeneric outer) { super(outer); }  
        protected S8(SpreadGeneric outer, MethodHandle t) { super(outer, t); }
        protected S8 makeInstance(SpreadGeneric outer, MethodHandle t) { return new S8(outer, t); }
        protected Object invoke_S0(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object av) throws Throwable { av = super.check(av, 0);
            return target.invokeExact(a0, a1, a2, a3, a4, a5, a6, a7); }
        protected Object invoke_S1(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object av) throws Throwable { av = super.check(av, 1);
            return target.invokeExact(a0, a1, a2, a3, a4, a5, a6,
                super.select(av,0)); }
        protected Object invoke_S2(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object av) throws Throwable { av = super.check(av, 2);
            return target.invokeExact(a0, a1, a2, a3, a4, a5,
                super.select(av,0), super.select(av,1)); }
        protected Object invoke_S3(Object a0, Object a1, Object a2, Object a3, Object a4, Object av) throws Throwable { av = super.check(av, 3);
            return target.invokeExact(a0, a1, a2, a3, a4,
                super.select(av,0), super.select(av,1), super.select(av,2)); }
        protected Object invoke_S4(Object a0, Object a1, Object a2, Object a3, Object av) throws Throwable { av = super.check(av, 4);
            return target.invokeExact(a0, a1, a2, a3,
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3)); }
        protected Object invoke_S5(Object a0, Object a1, Object a2, Object av) throws Throwable { av = super.check(av, 5);
            return target.invokeExact(a0, a1, a2,
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3),
                super.select(av,4)); }
        protected Object invoke_S6(Object a0, Object a1, Object av) throws Throwable { av = super.check(av, 6);
            return target.invokeExact(a0, a1,
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3),
                super.select(av,4), super.select(av,5)); }
        protected Object invoke_S7(Object a0, Object av) throws Throwable { av = super.check(av, 7);
            return target.invokeExact(a0,
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3),
                super.select(av,4), super.select(av,5), super.select(av,6)); }
        protected Object invoke_S8(Object av) throws Throwable { av = super.check(av, 8);
            return target.invokeExact(
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3),
                super.select(av,4), super.select(av,5), super.select(av,6), super.select(av,7)); }
    }
    static class S9 extends Adapter {
        protected S9(SpreadGeneric outer) { super(outer); }  
        protected S9(SpreadGeneric outer, MethodHandle t) { super(outer, t); }
        protected S9 makeInstance(SpreadGeneric outer, MethodHandle t) { return new S9(outer, t); }
        protected Object invoke_S0(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object av) throws Throwable { av = super.check(av, 0);
            return target.invokeExact(a0, a1, a2, a3, a4, a5, a6, a7, a8); }
        protected Object invoke_S1(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object av) throws Throwable { av = super.check(av, 1);
            return target.invokeExact(a0, a1, a2, a3, a4, a5, a6, a7,
                super.select(av,0)); }
        protected Object invoke_S2(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object av) throws Throwable { av = super.check(av, 2);
            return target.invokeExact(a0, a1, a2, a3, a4, a5, a6,
                super.select(av,0), super.select(av,1)); }
        protected Object invoke_S3(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object av) throws Throwable { av = super.check(av, 3);
            return target.invokeExact(a0, a1, a2, a3, a4, a5,
                super.select(av,0), super.select(av,1), super.select(av,2)); }
        protected Object invoke_S4(Object a0, Object a1, Object a2, Object a3, Object a4, Object av) throws Throwable { av = super.check(av, 4);
            return target.invokeExact(a0, a1, a2, a3, a4,
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3)); }
        protected Object invoke_S5(Object a0, Object a1, Object a2, Object a3, Object av) throws Throwable { av = super.check(av, 5);
            return target.invokeExact(a0, a1, a2, a3,
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3),
                super.select(av,4)); }
        protected Object invoke_S6(Object a0, Object a1, Object a2, Object av) throws Throwable { av = super.check(av, 6);
            return target.invokeExact(a0, a1, a2,
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3),
                super.select(av,4), super.select(av,5)); }
        protected Object invoke_S7(Object a0, Object a1, Object av) throws Throwable { av = super.check(av, 7);
            return target.invokeExact(a0, a1,
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3),
                super.select(av,4), super.select(av,5), super.select(av,6)); }
        protected Object invoke_S8(Object a0, Object av) throws Throwable { av = super.check(av, 8);
            return target.invokeExact(a0,
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3),
                super.select(av,4), super.select(av,5), super.select(av,6), super.select(av,7)); }
        protected Object invoke_S9(Object av) throws Throwable { av = super.check(av, 9);
            return target.invokeExact(
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3),
                super.select(av,4), super.select(av,5), super.select(av,6), super.select(av,7),
                super.select(av,8)); }
    }
    static class S10 extends Adapter {
        protected S10(SpreadGeneric outer) { super(outer); }  
        protected S10(SpreadGeneric outer, MethodHandle t) { super(outer, t); }
        protected S10 makeInstance(SpreadGeneric outer, MethodHandle t) { return new S10(outer, t); }
        protected Object invoke_S0(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object av) throws Throwable { av = super.check(av, 0);
            return target.invokeExact(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9); }
        protected Object invoke_S1(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object av) throws Throwable { av = super.check(av, 1);
            return target.invokeExact(a0, a1, a2, a3, a4, a5, a6, a7, a8,
                super.select(av,0)); }
        protected Object invoke_S2(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object av) throws Throwable { av = super.check(av, 2);
            return target.invokeExact(a0, a1, a2, a3, a4, a5, a6, a7,
                super.select(av,0), super.select(av,1)); }
        protected Object invoke_S3(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object av) throws Throwable { av = super.check(av, 3);
            return target.invokeExact(a0, a1, a2, a3, a4, a5, a6,
                super.select(av,0), super.select(av,1), super.select(av,2)); }
        protected Object invoke_S4(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object av) throws Throwable { av = super.check(av, 4);
            return target.invokeExact(a0, a1, a2, a3, a4, a5,
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3)); }
        protected Object invoke_S5(Object a0, Object a1, Object a2, Object a3, Object a4, Object av) throws Throwable { av = super.check(av, 5);
            return target.invokeExact(a0, a1, a2, a3, a4,
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3),
                super.select(av,4)); }
        protected Object invoke_S6(Object a0, Object a1, Object a2, Object a3, Object av) throws Throwable { av = super.check(av, 6);
            return target.invokeExact(a0, a1, a2, a3,
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3),
                super.select(av,4), super.select(av,5)); }
        protected Object invoke_S7(Object a0, Object a1, Object a2, Object av) throws Throwable { av = super.check(av, 7);
            return target.invokeExact(a0, a1, a2,
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3),
                super.select(av,4), super.select(av,5), super.select(av,6)); }
        protected Object invoke_S8(Object a0, Object a1, Object av) throws Throwable { av = super.check(av, 8);
            return target.invokeExact(a0, a1,
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3),
                super.select(av,4), super.select(av,5), super.select(av,6), super.select(av,7)); }
        protected Object invoke_S9(Object a0, Object av) throws Throwable { av = super.check(av, 9);
            return target.invokeExact(a0,
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3),
                super.select(av,4), super.select(av,5), super.select(av,6), super.select(av,7),
                super.select(av,8)); }
        protected Object invoke_S10(Object av) throws Throwable { av = super.check(av, 10);
            return target.invokeExact(
                super.select(av,0), super.select(av,1), super.select(av,2), super.select(av,3),
                super.select(av,4), super.select(av,5), super.select(av,6), super.select(av,7),
                super.select(av,8), super.select(av,9)); }
    }
}
