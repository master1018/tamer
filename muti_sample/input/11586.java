class ToGeneric {
    private final MethodType entryType;
    private final MethodType rawEntryType;
    private final Adapter adapter;
    private final MethodHandle entryPoint;
    private final int[] primsAtEndOrder;
    private final MethodHandle invoker;
    private final MethodHandle returnConversion;
    private ToGeneric(MethodType entryType) {
        assert(entryType.erase() == entryType); 
        this.entryType = entryType;
        MethodHandle invoker0 = entryType.generic().invokers().exactInvoker();
        MethodType rawEntryTypeInit;
        Adapter ad = findAdapter(rawEntryTypeInit = entryType);
        if (ad != null) {
            this.returnConversion = computeReturnConversion(entryType, rawEntryTypeInit, false);
            this.rawEntryType = rawEntryTypeInit;
            this.adapter = ad;
            this.entryPoint = ad.prototypeEntryPoint();
            this.primsAtEndOrder = null;
            this.invoker = invoker0;
            return;
        }
        MethodType primsAtEnd = entryType.form().primsAtEnd();
        this.primsAtEndOrder = MethodTypeForm.primsAtEndOrder(entryType);
        if (primsAtEndOrder != null) {
            ToGeneric va2 = ToGeneric.of(primsAtEnd);
            this.adapter = va2.adapter;
            if (true) throw new UnsupportedOperationException("NYI: primitive parameters must follow references; entryType = "+entryType);
            this.entryPoint = MethodHandleImpl.permuteArguments(
                    va2.entryPoint, primsAtEnd, entryType, primsAtEndOrder);
            return;
        }
        MethodType intsAtEnd = primsAtEnd.form().primsAsInts();
        ad = findAdapter(rawEntryTypeInit = intsAtEnd);
        MethodHandle rawEntryPoint;
        if (ad != null) {
            rawEntryPoint = ad.prototypeEntryPoint();
        } else {
            MethodType longsAtEnd = primsAtEnd.form().primsAsLongs();
            ad = findAdapter(rawEntryTypeInit = longsAtEnd);
            if (ad != null) {
                MethodType eptWithLongs = longsAtEnd.insertParameterTypes(0, ad.getClass());
                MethodType eptWithInts  =  intsAtEnd.insertParameterTypes(0, ad.getClass());
                rawEntryPoint = ad.prototypeEntryPoint();
                MethodType midType = eptWithLongs;  
                for (int i = 0, nargs = midType.parameterCount(); i < nargs; i++) {
                    if (midType.parameterType(i) != eptWithInts.parameterType(i)) {
                        assert(midType.parameterType(i) == long.class);
                        assert(eptWithInts.parameterType(i) == int.class);
                        MethodType nextType = midType.changeParameterType(i, int.class);
                        rawEntryPoint = MethodHandleImpl.convertArguments(
                                rawEntryPoint, nextType, midType, 0);
                        midType = nextType;
                    }
                }
                assert(midType == eptWithInts);
            } else {
                ad = buildAdapterFromBytecodes(rawEntryTypeInit = intsAtEnd);
                rawEntryPoint = ad.prototypeEntryPoint();
            }
        }
        MethodType tepType = entryType.insertParameterTypes(0, ad.getClass());
        this.entryPoint =
            AdapterMethodHandle.makeRetypeRaw(tepType, rawEntryPoint);
        if (this.entryPoint == null)
            throw new UnsupportedOperationException("cannot retype to "+entryType
                    +" from "+rawEntryPoint.type().dropParameterTypes(0, 1));
        this.returnConversion = computeReturnConversion(entryType, rawEntryTypeInit, false);
        this.rawEntryType = rawEntryTypeInit;
        this.adapter = ad;
        this.invoker = makeRawArgumentFilter(invoker0, rawEntryTypeInit, entryType);
    }
    static {
        assert(MethodHandleNatives.workaroundWithoutRicochetFrames());  
    }
    static MethodHandle makeRawArgumentFilter(MethodHandle invoker,
            MethodType raw, MethodType cooked) {
        MethodHandle filteredInvoker = null;
        for (int i = 0, nargs = raw.parameterCount(); i < nargs; i++) {
            Class<?> src = raw.parameterType(i);
            Class<?> dst = cooked.parameterType(i);
            if (src == dst)  continue;
            assert(src.isPrimitive() && dst.isPrimitive());
            if (filteredInvoker == null) {
                filteredInvoker =
                        AdapterMethodHandle.makeCheckCast(
                            invoker.type().generic(), invoker, 0, MethodHandle.class);
                if (filteredInvoker == null)  throw new UnsupportedOperationException("NYI");
            }
            MethodHandle reboxer = ValueConversions.rebox(dst);
            filteredInvoker = FilterGeneric.makeArgumentFilter(1+i, reboxer, filteredInvoker);
            if (filteredInvoker == null)  throw new InternalError();
        }
        if (filteredInvoker == null)  return invoker;
        return AdapterMethodHandle.makeRetypeOnly(invoker.type(), filteredInvoker);
    }
    private static MethodHandle computeReturnConversion(
            MethodType type, MethodType rawEntryType, boolean mustCast) {
        Class<?> tret = type.returnType();
        Class<?> rret = rawEntryType.returnType();
        if (mustCast || !tret.isPrimitive()) {
            assert(!tret.isPrimitive());
            assert(!rret.isPrimitive());
            if (rret == Object.class && !mustCast)
                return null;
            return ValueConversions.cast(tret);
        } else if (tret == rret) {
            return ValueConversions.unbox(tret);
        } else {
            assert(rret.isPrimitive());
            assert(tret == double.class ? rret == long.class : rret == int.class);
            return ValueConversions.unboxRaw(tret);
        }
    }
    Adapter makeInstance(MethodType type, MethodHandle genericTarget) {
        genericTarget.getClass();  
        MethodHandle convert = returnConversion;
        if (primsAtEndOrder != null)
            throw new UnsupportedOperationException("NYI");
        if (type == entryType) {
            if (convert == null)  convert = ValueConversions.identity();
            return adapter.makeInstance(entryPoint, invoker, convert, genericTarget);
        }
        assert(type.erase() == entryType);  
        if (convert == null)
            convert = computeReturnConversion(type, rawEntryType, true);
        MethodType tepType = type.insertParameterTypes(0, adapter.getClass());
        MethodHandle typedEntryPoint =
            AdapterMethodHandle.makeRetypeRaw(tepType, entryPoint);
        return adapter.makeInstance(typedEntryPoint, invoker, convert, genericTarget);
    }
    public static MethodHandle make(MethodType type, MethodHandle genericTarget) {
        MethodType gtype = genericTarget.type();
        if (type.generic() != gtype)
            throw newIllegalArgumentException("type must be generic");
        if (type == gtype)  return genericTarget;
        return ToGeneric.of(type).makeInstance(type, genericTarget);
    }
    static ToGeneric of(MethodType type) {
        MethodTypeForm form = type.form();
        ToGeneric toGen = form.toGeneric;
        if (toGen == null)
            form.toGeneric = toGen = new ToGeneric(form.erasedType());
        return toGen;
    }
    String debugString() {
        return "ToGeneric"+entryType
                +(primsAtEndOrder!=null?"[reorder]":"");
    }
    static Adapter findAdapter(MethodType entryPointType) {
        MethodTypeForm form = entryPointType.form();
        Class<?> rtype = entryPointType.returnType();
        int argc = form.parameterCount();
        int lac = form.longPrimitiveParameterCount();
        int iac = form.primitiveParameterCount() - lac;
        String intsAndLongs = (iac > 0 ? "I"+iac : "")+(lac > 0 ? "J"+lac : "");
        String rawReturn = String.valueOf(Wrapper.forPrimitiveType(rtype).basicTypeChar());
        String iname0 = "invoke_"+rawReturn;
        String iname1 = "invoke";
        String[] inames = { iname0, iname1 };
        String cname0 = rawReturn + argc;
        String cname1 = "A"       + argc;
        String[] cnames = { cname1, cname1+intsAndLongs, cname0, cname0+intsAndLongs };
        for (String cname : cnames) {
            Class<? extends Adapter> acls = Adapter.findSubClass(cname);
            if (acls == null)  continue;
            for (String iname : inames) {
                MethodHandle entryPoint = null;
                try {
                    entryPoint = IMPL_LOOKUP.
                                    findSpecial(acls, iname, entryPointType, acls);
                } catch (ReflectiveOperationException ex) {
                }
                if (entryPoint == null)  continue;
                Constructor<? extends Adapter> ctor = null;
                try {
                    ctor = acls.getDeclaredConstructor(MethodHandle.class);
                } catch (NoSuchMethodException ex) {
                } catch (SecurityException ex) {
                }
                if (ctor == null)  continue;
                try {
                    return ctor.newInstance(entryPoint);
                } catch (IllegalArgumentException ex) {
                } catch (InvocationTargetException wex) {
                    Throwable ex = wex.getTargetException();
                    if (ex instanceof Error)  throw (Error)ex;
                    if (ex instanceof RuntimeException)  throw (RuntimeException)ex;
                } catch (InstantiationException ex) {
                } catch (IllegalAccessException ex) {
                }
            }
        }
        return null;
    }
    static Adapter buildAdapterFromBytecodes(MethodType entryPointType) {
        throw new UnsupportedOperationException("NYI: "+entryPointType);
    }
    static abstract class Adapter extends BoundMethodHandle {
        protected final MethodHandle invoker;  
        protected final MethodHandle target;   
        protected final MethodHandle convert;  
        @Override
        String debugString() {
            return target == null ? "prototype:"+convert : addTypeString(target, this);
        }
        protected boolean isPrototype() { return target == null; }
        protected Adapter(MethodHandle entryPoint) {
            super(entryPoint);
            this.invoker = null;
            this.convert = entryPoint;
            this.target = null;
            assert(isPrototype());
        }
        protected MethodHandle prototypeEntryPoint() {
            if (!isPrototype())  throw new InternalError();
            return convert;
        }
        protected Adapter(MethodHandle entryPoint, MethodHandle invoker, MethodHandle convert, MethodHandle target) {
            super(entryPoint);
            this.invoker = invoker;
            this.convert = convert;
            this.target = target;
        }
        protected abstract Adapter makeInstance(MethodHandle entryPoint,
                MethodHandle invoker, MethodHandle convert, MethodHandle target);
        protected Object target()               throws Throwable { return invoker.invokeExact(target); }
        protected Object target(Object a0)      throws Throwable { return invoker.invokeExact(target, a0); }
        protected Object target(Object a0, Object a1)
                                                throws Throwable { return invoker.invokeExact(target, a0, a1); }
        protected Object target(Object a0, Object a1, Object a2)
                                                throws Throwable { return invoker.invokeExact(target, a0, a1, a2); }
        protected Object target(Object a0, Object a1, Object a2, Object a3)
                                                throws Throwable { return invoker.invokeExact(target, a0, a1, a2, a3); }
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
    static class A1 extends Adapter {
        protected A1(MethodHandle entryPoint) { super(entryPoint); }  
        protected A1(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { super(e, i, c, t); }
        protected A1 makeInstance(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { return new A1(e, i, c, t); }
        protected Object target(Object a0)   throws Throwable { return invoker.invokeExact(target, a0); }
        protected Object targetA1(Object a0) throws Throwable { return target(a0); }
        protected Object targetA1(int    a0) throws Throwable { return target(a0); }
        protected Object targetA1(long   a0) throws Throwable { return target(a0); }
        protected Object invoke_L(Object a0) throws Throwable { return return_L(targetA1(a0)); }
        protected int    invoke_I(Object a0) throws Throwable { return return_I(targetA1(a0)); }
        protected long   invoke_J(Object a0) throws Throwable { return return_J(targetA1(a0)); }
        protected float  invoke_F(Object a0) throws Throwable { return return_F(targetA1(a0)); }
        protected double invoke_D(Object a0) throws Throwable { return return_D(targetA1(a0)); }
        protected Object invoke_L(int    a0) throws Throwable { return return_L(targetA1(a0)); }
        protected int    invoke_I(int    a0) throws Throwable { return return_I(targetA1(a0)); }
        protected long   invoke_J(int    a0) throws Throwable { return return_J(targetA1(a0)); }
        protected float  invoke_F(int    a0) throws Throwable { return return_F(targetA1(a0)); }
        protected double invoke_D(int    a0) throws Throwable { return return_D(targetA1(a0)); }
        protected Object invoke_L(long   a0) throws Throwable { return return_L(targetA1(a0)); }
        protected int    invoke_I(long   a0) throws Throwable { return return_I(targetA1(a0)); }
        protected long   invoke_J(long   a0) throws Throwable { return return_J(targetA1(a0)); }
        protected float  invoke_F(long   a0) throws Throwable { return return_F(targetA1(a0)); }
        protected double invoke_D(long   a0) throws Throwable { return return_D(targetA1(a0)); }
    }
/*
: SHELL; n=ToGeneric; cp -p $n.java $n.java-; sed < $n.java- > $n.java+ -e '/{{*{{/,/}}*}}/w /tmp/genclasses.java' -e '/}}*}}/q'; (cd /tmp; javac -d . genclasses.java; java -cp . genclasses) >> $n.java+; echo '}' >> $n.java+; mv $n.java+ $n.java; mv $n.java- $n.java~
import java.util.*;
class genclasses {
    static String[] TYPES = { "Object", "int   ", "long  ", "float ", "double" };
    static String[] TCHARS = { "L",     "I",      "J",      "F",      "D",     "A" };
    static String[][] TEMPLATES = { {
        "@for@ arity=0..3   rcat<=4 nrefs<=99 nints<=99 nlongs<=99",
        "@for@ arity=4..4   rcat<=4 nrefs<=99 nints<=99 nlongs<=99",
        "@for@ arity=5..5   rcat<=2 nrefs<=99 nints<=99 nlongs<=99",
        "@for@ arity=6..10  rcat<=2 nrefs<=99 nints=0   nlongs<=99",
        "    
        "    static class @cat@ extends Adapter {",
        "        protected @cat@(MethodHandle entryPoint) { super(entryPoint); }  
        "        protected @cat@(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { super(e, i, c, t); }",
        "        protected @cat@ makeInstance(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { return new @cat@(e, i, c, t); }",
        "        protected Object target(@Ovav@)   throws Throwable { return invoker.invokeExact(target@comma@@av@); }",
        "        
        "        protected Object target@cat@(@Tvav@) throws Throwable { return target(@av@); }",
        "        
        "        
        "        
        "        protected @R@ invoke_@Rc@(@Tvav@) throws Throwable { return return_@Rc@(target@cat@(@av@)); }",
        "        
        "        
        "    }",
    } };
    enum VAR {
        cat, R, Rc, Tv, av, comma, Tvav, Ovav;
        public final String pattern = "@"+toString().replace('_','.')+"@";
        public String binding;
        static void makeBindings(boolean topLevel, int rcat, int nrefs, int nints, int nlongs) {
            int nargs = nrefs + nints + nlongs;
            if (topLevel)
                VAR.cat.binding = catstr(ALL_RETURN_TYPES ? TYPES.length : rcat, nrefs, nints, nlongs);
            VAR.R.binding = TYPES[rcat];
            VAR.Rc.binding = TCHARS[rcat];
            String[] Tv = new String[nargs];
            String[] av = new String[nargs];
            String[] Tvav = new String[nargs];
            String[] Ovav = new String[nargs];
            for (int i = 0; i < nargs; i++) {
                int tcat = (i < nrefs) ? 0 : (i < nrefs + nints) ? 1 : 2;
                Tv[i] = TYPES[tcat];
                av[i] = arg(i);
                Tvav[i] = param(Tv[i], av[i]);
                Ovav[i] = param("Object", av[i]);
            }
            VAR.Tv.binding = comma(Tv);
            VAR.av.binding = comma(av);
            VAR.comma.binding = (av.length == 0 ? "" : ", ");
            VAR.Tvav.binding = comma(Tvav);
            VAR.Ovav.binding = comma(Ovav);
        }
        static String arg(int i) { return "a"+i; }
        static String param(String t, String a) { return t+" "+a; }
        static String comma(String[] v) { return comma("", v); }
        static String comma(String sep, String[] v) {
            if (v.length == 0)  return "";
            String res = sep+v[0];
            for (int i = 1; i < v.length; i++)  res += ", "+v[i];
            return res;
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
    static int MIN_ARITY, MAX_ARITY, MAX_RCAT, MAX_REFS, MAX_INTS, MAX_LONGS;
    static boolean ALL_ARG_TYPES, ALL_RETURN_TYPES;
    static HashSet<String> done = new HashSet<String>();
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
        MAX_RCAT  = Integer.valueOf(params[pcur++]);
        MAX_REFS  = Integer.valueOf(params[pcur++]);
        MAX_INTS  = Integer.valueOf(params[pcur++]);
        MAX_LONGS = Integer.valueOf(params[pcur++]);
        if (pcur != params.length)  throw new RuntimeException("bad extra param: "+forLine);
        if (MAX_RCAT >= TYPES.length)  MAX_RCAT = TYPES.length - 1;
        ALL_ARG_TYPES = (indexBefore(template, 0, "@each-Tv@") < template.length);
        ALL_RETURN_TYPES = (indexBefore(template, 0, "@each-R@") < template.length);
        for (int nargs = MIN_ARITY; nargs <= MAX_ARITY; nargs++) {
            for (int rcat = 0; rcat <= MAX_RCAT; rcat++) {
                expandTemplate(template, true, rcat, nargs, 0, 0);
                if (ALL_ARG_TYPES)  break;
                expandTemplateForPrims(template, true, rcat, nargs, 1, 1);
                if (ALL_RETURN_TYPES)  break;
            }
        }
    }
    static String catstr(int rcat, int nrefs, int nints, int nlongs) {
        int nargs = nrefs + nints + nlongs;
        String cat = TCHARS[rcat] + nargs;
        if (!ALL_ARG_TYPES)  cat += (nints==0?"":"I"+nints)+(nlongs==0?"":"J"+nlongs);
        return cat;
    }
    static void expandTemplateForPrims(String[] template, boolean topLevel, int rcat, int nargs, int minints, int minlongs) {
        for (int isLong = 0; isLong <= 1; isLong++) {
            for (int nprims = 1; nprims <= nargs; nprims++) {
                int nrefs = nargs - nprims;
                int nints = ((1-isLong) * nprims);
                int nlongs = (isLong * nprims);
                expandTemplate(template, topLevel, rcat, nrefs, nints, nlongs);
            }
        }
    }
    static void expandTemplate(String[] template, boolean topLevel,
                               int rcat, int nrefs, int nints, int nlongs) {
        int nargs = nrefs + nints + nlongs;
        if (nrefs > MAX_REFS || nints > MAX_INTS || nlongs > MAX_LONGS)  return;
        VAR.makeBindings(topLevel, rcat, nrefs, nints, nlongs);
        if (topLevel && !done.add(VAR.cat.binding)) {
            System.out.println("    
            return;
        }
        for (int i = 0; i < template.length; i++) {
            String line = template[i];
            if (line.endsWith("@each-cat@")) {
            } else if (line.endsWith("@each-R@")) {
                int blockEnd = indexAfter(template, i, "@end-R@");
                String[] block = stringsIn(template, i+1, blockEnd-1);
                for (int rcat1 = rcat; rcat1 <= MAX_RCAT; rcat1++)
                    expandTemplate(block, false, rcat1, nrefs, nints, nlongs);
                VAR.makeBindings(topLevel, rcat, nrefs, nints, nlongs);
                i = blockEnd-1; continue;
            } else if (line.endsWith("@each-Tv@")) {
                int blockEnd = indexAfter(template, i, "@end-Tv@");
                String[] block = stringsIn(template, i+1, blockEnd-1);
                expandTemplate(block, false, rcat, nrefs, nints, nlongs);
                expandTemplateForPrims(block, false, rcat, nargs, nints+1, nlongs+1);
                VAR.makeBindings(topLevel, rcat, nrefs, nints, nlongs);
                i = blockEnd-1; continue;
            } else {
                System.out.println(VAR.transform(line));
            }
        }
    }
}
    static class A0 extends Adapter {
        protected A0(MethodHandle entryPoint) { super(entryPoint); }  
        protected A0(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { super(e, i, c, t); }
        protected A0 makeInstance(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { return new A0(e, i, c, t); }
        protected Object target()   throws Throwable { return invoker.invokeExact(target); }
        protected Object targetA0() throws Throwable { return target(); }
        protected Object invoke_L() throws Throwable { return return_L(targetA0()); }
        protected int    invoke_I() throws Throwable { return return_I(targetA0()); }
        protected long   invoke_J() throws Throwable { return return_J(targetA0()); }
        protected float  invoke_F() throws Throwable { return return_F(targetA0()); }
        protected double invoke_D() throws Throwable { return return_D(targetA0()); }
    }
    static class A1 extends Adapter {
        protected A1(MethodHandle entryPoint) { super(entryPoint); }  
        protected A1(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { super(e, i, c, t); }
        protected A1 makeInstance(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { return new A1(e, i, c, t); }
        protected Object target(Object a0)   throws Throwable { return invoker.invokeExact(target, a0); }
        protected Object targetA1(Object a0) throws Throwable { return target(a0); }
        protected Object targetA1(int    a0) throws Throwable { return target(a0); }
        protected Object targetA1(long   a0) throws Throwable { return target(a0); }
        protected Object invoke_L(Object a0) throws Throwable { return return_L(targetA1(a0)); }
        protected int    invoke_I(Object a0) throws Throwable { return return_I(targetA1(a0)); }
        protected long   invoke_J(Object a0) throws Throwable { return return_J(targetA1(a0)); }
        protected float  invoke_F(Object a0) throws Throwable { return return_F(targetA1(a0)); }
        protected double invoke_D(Object a0) throws Throwable { return return_D(targetA1(a0)); }
        protected Object invoke_L(int    a0) throws Throwable { return return_L(targetA1(a0)); }
        protected int    invoke_I(int    a0) throws Throwable { return return_I(targetA1(a0)); }
        protected long   invoke_J(int    a0) throws Throwable { return return_J(targetA1(a0)); }
        protected float  invoke_F(int    a0) throws Throwable { return return_F(targetA1(a0)); }
        protected double invoke_D(int    a0) throws Throwable { return return_D(targetA1(a0)); }
        protected Object invoke_L(long   a0) throws Throwable { return return_L(targetA1(a0)); }
        protected int    invoke_I(long   a0) throws Throwable { return return_I(targetA1(a0)); }
        protected long   invoke_J(long   a0) throws Throwable { return return_J(targetA1(a0)); }
        protected float  invoke_F(long   a0) throws Throwable { return return_F(targetA1(a0)); }
        protected double invoke_D(long   a0) throws Throwable { return return_D(targetA1(a0)); }
    }
    static class A2 extends Adapter {
        protected A2(MethodHandle entryPoint) { super(entryPoint); }  
        protected A2(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { super(e, i, c, t); }
        protected A2 makeInstance(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { return new A2(e, i, c, t); }
        protected Object target(Object a0, Object a1)   throws Throwable { return invoker.invokeExact(target, a0, a1); }
        protected Object targetA2(Object a0, Object a1) throws Throwable { return target(a0, a1); }
        protected Object targetA2(Object a0, int    a1) throws Throwable { return target(a0, a1); }
        protected Object targetA2(int    a0, int    a1) throws Throwable { return target(a0, a1); }
        protected Object targetA2(Object a0, long   a1) throws Throwable { return target(a0, a1); }
        protected Object targetA2(long   a0, long   a1) throws Throwable { return target(a0, a1); }
        protected Object invoke_L(Object a0, Object a1) throws Throwable { return return_L(targetA2(a0, a1)); }
        protected int    invoke_I(Object a0, Object a1) throws Throwable { return return_I(targetA2(a0, a1)); }
        protected long   invoke_J(Object a0, Object a1) throws Throwable { return return_J(targetA2(a0, a1)); }
        protected float  invoke_F(Object a0, Object a1) throws Throwable { return return_F(targetA2(a0, a1)); }
        protected double invoke_D(Object a0, Object a1) throws Throwable { return return_D(targetA2(a0, a1)); }
        protected Object invoke_L(Object a0, int    a1) throws Throwable { return return_L(targetA2(a0, a1)); }
        protected int    invoke_I(Object a0, int    a1) throws Throwable { return return_I(targetA2(a0, a1)); }
        protected long   invoke_J(Object a0, int    a1) throws Throwable { return return_J(targetA2(a0, a1)); }
        protected float  invoke_F(Object a0, int    a1) throws Throwable { return return_F(targetA2(a0, a1)); }
        protected double invoke_D(Object a0, int    a1) throws Throwable { return return_D(targetA2(a0, a1)); }
        protected Object invoke_L(int    a0, int    a1) throws Throwable { return return_L(targetA2(a0, a1)); }
        protected int    invoke_I(int    a0, int    a1) throws Throwable { return return_I(targetA2(a0, a1)); }
        protected long   invoke_J(int    a0, int    a1) throws Throwable { return return_J(targetA2(a0, a1)); }
        protected float  invoke_F(int    a0, int    a1) throws Throwable { return return_F(targetA2(a0, a1)); }
        protected double invoke_D(int    a0, int    a1) throws Throwable { return return_D(targetA2(a0, a1)); }
        protected Object invoke_L(Object a0, long   a1) throws Throwable { return return_L(targetA2(a0, a1)); }
        protected int    invoke_I(Object a0, long   a1) throws Throwable { return return_I(targetA2(a0, a1)); }
        protected long   invoke_J(Object a0, long   a1) throws Throwable { return return_J(targetA2(a0, a1)); }
        protected float  invoke_F(Object a0, long   a1) throws Throwable { return return_F(targetA2(a0, a1)); }
        protected double invoke_D(Object a0, long   a1) throws Throwable { return return_D(targetA2(a0, a1)); }
        protected Object invoke_L(long   a0, long   a1) throws Throwable { return return_L(targetA2(a0, a1)); }
        protected int    invoke_I(long   a0, long   a1) throws Throwable { return return_I(targetA2(a0, a1)); }
        protected long   invoke_J(long   a0, long   a1) throws Throwable { return return_J(targetA2(a0, a1)); }
        protected float  invoke_F(long   a0, long   a1) throws Throwable { return return_F(targetA2(a0, a1)); }
        protected double invoke_D(long   a0, long   a1) throws Throwable { return return_D(targetA2(a0, a1)); }
    }
    static class A3 extends Adapter {
        protected A3(MethodHandle entryPoint) { super(entryPoint); }  
        protected A3(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { super(e, i, c, t); }
        protected A3 makeInstance(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { return new A3(e, i, c, t); }
        protected Object target(Object a0, Object a1, Object a2)   throws Throwable { return invoker.invokeExact(target, a0, a1, a2); }
        protected Object targetA3(Object a0, Object a1, Object a2) throws Throwable { return target(a0, a1, a2); }
        protected Object targetA3(Object a0, Object a1, int    a2) throws Throwable { return target(a0, a1, a2); }
        protected Object targetA3(Object a0, int    a1, int    a2) throws Throwable { return target(a0, a1, a2); }
        protected Object targetA3(int    a0, int    a1, int    a2) throws Throwable { return target(a0, a1, a2); }
        protected Object targetA3(Object a0, Object a1, long   a2) throws Throwable { return target(a0, a1, a2); }
        protected Object targetA3(Object a0, long   a1, long   a2) throws Throwable { return target(a0, a1, a2); }
        protected Object targetA3(long   a0, long   a1, long   a2) throws Throwable { return target(a0, a1, a2); }
        protected Object invoke_L(Object a0, Object a1, Object a2) throws Throwable { return return_L(targetA3(a0, a1, a2)); }
        protected int    invoke_I(Object a0, Object a1, Object a2) throws Throwable { return return_I(targetA3(a0, a1, a2)); }
        protected long   invoke_J(Object a0, Object a1, Object a2) throws Throwable { return return_J(targetA3(a0, a1, a2)); }
        protected float  invoke_F(Object a0, Object a1, Object a2) throws Throwable { return return_F(targetA3(a0, a1, a2)); }
        protected double invoke_D(Object a0, Object a1, Object a2) throws Throwable { return return_D(targetA3(a0, a1, a2)); }
        protected Object invoke_L(Object a0, Object a1, int    a2) throws Throwable { return return_L(targetA3(a0, a1, a2)); }
        protected int    invoke_I(Object a0, Object a1, int    a2) throws Throwable { return return_I(targetA3(a0, a1, a2)); }
        protected long   invoke_J(Object a0, Object a1, int    a2) throws Throwable { return return_J(targetA3(a0, a1, a2)); }
        protected float  invoke_F(Object a0, Object a1, int    a2) throws Throwable { return return_F(targetA3(a0, a1, a2)); }
        protected double invoke_D(Object a0, Object a1, int    a2) throws Throwable { return return_D(targetA3(a0, a1, a2)); }
        protected Object invoke_L(Object a0, int    a1, int    a2) throws Throwable { return return_L(targetA3(a0, a1, a2)); }
        protected int    invoke_I(Object a0, int    a1, int    a2) throws Throwable { return return_I(targetA3(a0, a1, a2)); }
        protected long   invoke_J(Object a0, int    a1, int    a2) throws Throwable { return return_J(targetA3(a0, a1, a2)); }
        protected float  invoke_F(Object a0, int    a1, int    a2) throws Throwable { return return_F(targetA3(a0, a1, a2)); }
        protected double invoke_D(Object a0, int    a1, int    a2) throws Throwable { return return_D(targetA3(a0, a1, a2)); }
        protected Object invoke_L(int    a0, int    a1, int    a2) throws Throwable { return return_L(targetA3(a0, a1, a2)); }
        protected int    invoke_I(int    a0, int    a1, int    a2) throws Throwable { return return_I(targetA3(a0, a1, a2)); }
        protected long   invoke_J(int    a0, int    a1, int    a2) throws Throwable { return return_J(targetA3(a0, a1, a2)); }
        protected float  invoke_F(int    a0, int    a1, int    a2) throws Throwable { return return_F(targetA3(a0, a1, a2)); }
        protected double invoke_D(int    a0, int    a1, int    a2) throws Throwable { return return_D(targetA3(a0, a1, a2)); }
        protected Object invoke_L(Object a0, Object a1, long   a2) throws Throwable { return return_L(targetA3(a0, a1, a2)); }
        protected int    invoke_I(Object a0, Object a1, long   a2) throws Throwable { return return_I(targetA3(a0, a1, a2)); }
        protected long   invoke_J(Object a0, Object a1, long   a2) throws Throwable { return return_J(targetA3(a0, a1, a2)); }
        protected float  invoke_F(Object a0, Object a1, long   a2) throws Throwable { return return_F(targetA3(a0, a1, a2)); }
        protected double invoke_D(Object a0, Object a1, long   a2) throws Throwable { return return_D(targetA3(a0, a1, a2)); }
        protected Object invoke_L(Object a0, long   a1, long   a2) throws Throwable { return return_L(targetA3(a0, a1, a2)); }
        protected int    invoke_I(Object a0, long   a1, long   a2) throws Throwable { return return_I(targetA3(a0, a1, a2)); }
        protected long   invoke_J(Object a0, long   a1, long   a2) throws Throwable { return return_J(targetA3(a0, a1, a2)); }
        protected float  invoke_F(Object a0, long   a1, long   a2) throws Throwable { return return_F(targetA3(a0, a1, a2)); }
        protected double invoke_D(Object a0, long   a1, long   a2) throws Throwable { return return_D(targetA3(a0, a1, a2)); }
        protected Object invoke_L(long   a0, long   a1, long   a2) throws Throwable { return return_L(targetA3(a0, a1, a2)); }
        protected int    invoke_I(long   a0, long   a1, long   a2) throws Throwable { return return_I(targetA3(a0, a1, a2)); }
        protected long   invoke_J(long   a0, long   a1, long   a2) throws Throwable { return return_J(targetA3(a0, a1, a2)); }
        protected float  invoke_F(long   a0, long   a1, long   a2) throws Throwable { return return_F(targetA3(a0, a1, a2)); }
        protected double invoke_D(long   a0, long   a1, long   a2) throws Throwable { return return_D(targetA3(a0, a1, a2)); }
    }
    static class A4 extends Adapter {
        protected A4(MethodHandle entryPoint) { super(entryPoint); }  
        protected A4(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { super(e, i, c, t); }
        protected A4 makeInstance(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { return new A4(e, i, c, t); }
        protected Object target(Object a0, Object a1, Object a2, Object a3)   throws Throwable { return invoker.invokeExact(target, a0, a1, a2, a3); }
        protected Object targetA4(Object a0, Object a1, Object a2, Object a3) throws Throwable { return target(a0, a1, a2, a3); }
        protected Object targetA4(Object a0, Object a1, Object a2, int    a3) throws Throwable { return target(a0, a1, a2, a3); }
        protected Object targetA4(Object a0, Object a1, int    a2, int    a3) throws Throwable { return target(a0, a1, a2, a3); }
        protected Object targetA4(Object a0, int    a1, int    a2, int    a3) throws Throwable { return target(a0, a1, a2, a3); }
        protected Object targetA4(int    a0, int    a1, int    a2, int    a3) throws Throwable { return target(a0, a1, a2, a3); }
        protected Object targetA4(Object a0, Object a1, Object a2, long   a3) throws Throwable { return target(a0, a1, a2, a3); }
        protected Object targetA4(Object a0, Object a1, long   a2, long   a3) throws Throwable { return target(a0, a1, a2, a3); }
        protected Object targetA4(Object a0, long   a1, long   a2, long   a3) throws Throwable { return target(a0, a1, a2, a3); }
        protected Object targetA4(long   a0, long   a1, long   a2, long   a3) throws Throwable { return target(a0, a1, a2, a3); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3) throws Throwable { return return_L(targetA4(a0, a1, a2, a3)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3) throws Throwable { return return_I(targetA4(a0, a1, a2, a3)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3) throws Throwable { return return_J(targetA4(a0, a1, a2, a3)); }
        protected float  invoke_F(Object a0, Object a1, Object a2, Object a3) throws Throwable { return return_F(targetA4(a0, a1, a2, a3)); }
        protected double invoke_D(Object a0, Object a1, Object a2, Object a3) throws Throwable { return return_D(targetA4(a0, a1, a2, a3)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, int    a3) throws Throwable { return return_L(targetA4(a0, a1, a2, a3)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, int    a3) throws Throwable { return return_I(targetA4(a0, a1, a2, a3)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, int    a3) throws Throwable { return return_J(targetA4(a0, a1, a2, a3)); }
        protected float  invoke_F(Object a0, Object a1, Object a2, int    a3) throws Throwable { return return_F(targetA4(a0, a1, a2, a3)); }
        protected double invoke_D(Object a0, Object a1, Object a2, int    a3) throws Throwable { return return_D(targetA4(a0, a1, a2, a3)); }
        protected Object invoke_L(Object a0, Object a1, int    a2, int    a3) throws Throwable { return return_L(targetA4(a0, a1, a2, a3)); }
        protected int    invoke_I(Object a0, Object a1, int    a2, int    a3) throws Throwable { return return_I(targetA4(a0, a1, a2, a3)); }
        protected long   invoke_J(Object a0, Object a1, int    a2, int    a3) throws Throwable { return return_J(targetA4(a0, a1, a2, a3)); }
        protected float  invoke_F(Object a0, Object a1, int    a2, int    a3) throws Throwable { return return_F(targetA4(a0, a1, a2, a3)); }
        protected double invoke_D(Object a0, Object a1, int    a2, int    a3) throws Throwable { return return_D(targetA4(a0, a1, a2, a3)); }
        protected Object invoke_L(Object a0, int    a1, int    a2, int    a3) throws Throwable { return return_L(targetA4(a0, a1, a2, a3)); }
        protected int    invoke_I(Object a0, int    a1, int    a2, int    a3) throws Throwable { return return_I(targetA4(a0, a1, a2, a3)); }
        protected long   invoke_J(Object a0, int    a1, int    a2, int    a3) throws Throwable { return return_J(targetA4(a0, a1, a2, a3)); }
        protected float  invoke_F(Object a0, int    a1, int    a2, int    a3) throws Throwable { return return_F(targetA4(a0, a1, a2, a3)); }
        protected double invoke_D(Object a0, int    a1, int    a2, int    a3) throws Throwable { return return_D(targetA4(a0, a1, a2, a3)); }
        protected Object invoke_L(int    a0, int    a1, int    a2, int    a3) throws Throwable { return return_L(targetA4(a0, a1, a2, a3)); }
        protected int    invoke_I(int    a0, int    a1, int    a2, int    a3) throws Throwable { return return_I(targetA4(a0, a1, a2, a3)); }
        protected long   invoke_J(int    a0, int    a1, int    a2, int    a3) throws Throwable { return return_J(targetA4(a0, a1, a2, a3)); }
        protected float  invoke_F(int    a0, int    a1, int    a2, int    a3) throws Throwable { return return_F(targetA4(a0, a1, a2, a3)); }
        protected double invoke_D(int    a0, int    a1, int    a2, int    a3) throws Throwable { return return_D(targetA4(a0, a1, a2, a3)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, long   a3) throws Throwable { return return_L(targetA4(a0, a1, a2, a3)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, long   a3) throws Throwable { return return_I(targetA4(a0, a1, a2, a3)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, long   a3) throws Throwable { return return_J(targetA4(a0, a1, a2, a3)); }
        protected float  invoke_F(Object a0, Object a1, Object a2, long   a3) throws Throwable { return return_F(targetA4(a0, a1, a2, a3)); }
        protected double invoke_D(Object a0, Object a1, Object a2, long   a3) throws Throwable { return return_D(targetA4(a0, a1, a2, a3)); }
        protected Object invoke_L(Object a0, Object a1, long   a2, long   a3) throws Throwable { return return_L(targetA4(a0, a1, a2, a3)); }
        protected int    invoke_I(Object a0, Object a1, long   a2, long   a3) throws Throwable { return return_I(targetA4(a0, a1, a2, a3)); }
        protected long   invoke_J(Object a0, Object a1, long   a2, long   a3) throws Throwable { return return_J(targetA4(a0, a1, a2, a3)); }
        protected float  invoke_F(Object a0, Object a1, long   a2, long   a3) throws Throwable { return return_F(targetA4(a0, a1, a2, a3)); }
        protected double invoke_D(Object a0, Object a1, long   a2, long   a3) throws Throwable { return return_D(targetA4(a0, a1, a2, a3)); }
        protected Object invoke_L(Object a0, long   a1, long   a2, long   a3) throws Throwable { return return_L(targetA4(a0, a1, a2, a3)); }
        protected int    invoke_I(Object a0, long   a1, long   a2, long   a3) throws Throwable { return return_I(targetA4(a0, a1, a2, a3)); }
        protected long   invoke_J(Object a0, long   a1, long   a2, long   a3) throws Throwable { return return_J(targetA4(a0, a1, a2, a3)); }
        protected float  invoke_F(Object a0, long   a1, long   a2, long   a3) throws Throwable { return return_F(targetA4(a0, a1, a2, a3)); }
        protected double invoke_D(Object a0, long   a1, long   a2, long   a3) throws Throwable { return return_D(targetA4(a0, a1, a2, a3)); }
        protected Object invoke_L(long   a0, long   a1, long   a2, long   a3) throws Throwable { return return_L(targetA4(a0, a1, a2, a3)); }
        protected int    invoke_I(long   a0, long   a1, long   a2, long   a3) throws Throwable { return return_I(targetA4(a0, a1, a2, a3)); }
        protected long   invoke_J(long   a0, long   a1, long   a2, long   a3) throws Throwable { return return_J(targetA4(a0, a1, a2, a3)); }
        protected float  invoke_F(long   a0, long   a1, long   a2, long   a3) throws Throwable { return return_F(targetA4(a0, a1, a2, a3)); }
        protected double invoke_D(long   a0, long   a1, long   a2, long   a3) throws Throwable { return return_D(targetA4(a0, a1, a2, a3)); }
    }
    static class A5 extends Adapter {
        protected A5(MethodHandle entryPoint) { super(entryPoint); }  
        protected A5(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { super(e, i, c, t); }
        protected A5 makeInstance(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { return new A5(e, i, c, t); }
        protected Object target(Object a0, Object a1, Object a2, Object a3, Object a4)   throws Throwable { return invoker.invokeExact(target, a0, a1, a2, a3, a4); }
        protected Object targetA5(Object a0, Object a1, Object a2, Object a3, Object a4) throws Throwable { return target(a0, a1, a2, a3, a4); }
        protected Object targetA5(Object a0, Object a1, Object a2, Object a3, int    a4) throws Throwable { return target(a0, a1, a2, a3, a4); }
        protected Object targetA5(Object a0, Object a1, Object a2, int    a3, int    a4) throws Throwable { return target(a0, a1, a2, a3, a4); }
        protected Object targetA5(Object a0, Object a1, int    a2, int    a3, int    a4) throws Throwable { return target(a0, a1, a2, a3, a4); }
        protected Object targetA5(Object a0, int    a1, int    a2, int    a3, int    a4) throws Throwable { return target(a0, a1, a2, a3, a4); }
        protected Object targetA5(int    a0, int    a1, int    a2, int    a3, int    a4) throws Throwable { return target(a0, a1, a2, a3, a4); }
        protected Object targetA5(Object a0, Object a1, Object a2, Object a3, long   a4) throws Throwable { return target(a0, a1, a2, a3, a4); }
        protected Object targetA5(Object a0, Object a1, Object a2, long   a3, long   a4) throws Throwable { return target(a0, a1, a2, a3, a4); }
        protected Object targetA5(Object a0, Object a1, long   a2, long   a3, long   a4) throws Throwable { return target(a0, a1, a2, a3, a4); }
        protected Object targetA5(Object a0, long   a1, long   a2, long   a3, long   a4) throws Throwable { return target(a0, a1, a2, a3, a4); }
        protected Object targetA5(long   a0, long   a1, long   a2, long   a3, long   a4) throws Throwable { return target(a0, a1, a2, a3, a4); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, Object a4) throws Throwable { return return_L(targetA5(a0, a1, a2, a3, a4)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, Object a4) throws Throwable { return return_I(targetA5(a0, a1, a2, a3, a4)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, Object a4) throws Throwable { return return_J(targetA5(a0, a1, a2, a3, a4)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, int    a4) throws Throwable { return return_L(targetA5(a0, a1, a2, a3, a4)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, int    a4) throws Throwable { return return_I(targetA5(a0, a1, a2, a3, a4)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, int    a4) throws Throwable { return return_J(targetA5(a0, a1, a2, a3, a4)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, int    a3, int    a4) throws Throwable { return return_L(targetA5(a0, a1, a2, a3, a4)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, int    a3, int    a4) throws Throwable { return return_I(targetA5(a0, a1, a2, a3, a4)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, int    a3, int    a4) throws Throwable { return return_J(targetA5(a0, a1, a2, a3, a4)); }
        protected Object invoke_L(Object a0, Object a1, int    a2, int    a3, int    a4) throws Throwable { return return_L(targetA5(a0, a1, a2, a3, a4)); }
        protected int    invoke_I(Object a0, Object a1, int    a2, int    a3, int    a4) throws Throwable { return return_I(targetA5(a0, a1, a2, a3, a4)); }
        protected long   invoke_J(Object a0, Object a1, int    a2, int    a3, int    a4) throws Throwable { return return_J(targetA5(a0, a1, a2, a3, a4)); }
        protected Object invoke_L(Object a0, int    a1, int    a2, int    a3, int    a4) throws Throwable { return return_L(targetA5(a0, a1, a2, a3, a4)); }
        protected int    invoke_I(Object a0, int    a1, int    a2, int    a3, int    a4) throws Throwable { return return_I(targetA5(a0, a1, a2, a3, a4)); }
        protected long   invoke_J(Object a0, int    a1, int    a2, int    a3, int    a4) throws Throwable { return return_J(targetA5(a0, a1, a2, a3, a4)); }
        protected Object invoke_L(int    a0, int    a1, int    a2, int    a3, int    a4) throws Throwable { return return_L(targetA5(a0, a1, a2, a3, a4)); }
        protected int    invoke_I(int    a0, int    a1, int    a2, int    a3, int    a4) throws Throwable { return return_I(targetA5(a0, a1, a2, a3, a4)); }
        protected long   invoke_J(int    a0, int    a1, int    a2, int    a3, int    a4) throws Throwable { return return_J(targetA5(a0, a1, a2, a3, a4)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, long   a4) throws Throwable { return return_L(targetA5(a0, a1, a2, a3, a4)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, long   a4) throws Throwable { return return_I(targetA5(a0, a1, a2, a3, a4)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, long   a4) throws Throwable { return return_J(targetA5(a0, a1, a2, a3, a4)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, long   a3, long   a4) throws Throwable { return return_L(targetA5(a0, a1, a2, a3, a4)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, long   a3, long   a4) throws Throwable { return return_I(targetA5(a0, a1, a2, a3, a4)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, long   a3, long   a4) throws Throwable { return return_J(targetA5(a0, a1, a2, a3, a4)); }
        protected Object invoke_L(Object a0, Object a1, long   a2, long   a3, long   a4) throws Throwable { return return_L(targetA5(a0, a1, a2, a3, a4)); }
        protected int    invoke_I(Object a0, Object a1, long   a2, long   a3, long   a4) throws Throwable { return return_I(targetA5(a0, a1, a2, a3, a4)); }
        protected long   invoke_J(Object a0, Object a1, long   a2, long   a3, long   a4) throws Throwable { return return_J(targetA5(a0, a1, a2, a3, a4)); }
        protected Object invoke_L(Object a0, long   a1, long   a2, long   a3, long   a4) throws Throwable { return return_L(targetA5(a0, a1, a2, a3, a4)); }
        protected int    invoke_I(Object a0, long   a1, long   a2, long   a3, long   a4) throws Throwable { return return_I(targetA5(a0, a1, a2, a3, a4)); }
        protected long   invoke_J(Object a0, long   a1, long   a2, long   a3, long   a4) throws Throwable { return return_J(targetA5(a0, a1, a2, a3, a4)); }
        protected Object invoke_L(long   a0, long   a1, long   a2, long   a3, long   a4) throws Throwable { return return_L(targetA5(a0, a1, a2, a3, a4)); }
        protected int    invoke_I(long   a0, long   a1, long   a2, long   a3, long   a4) throws Throwable { return return_I(targetA5(a0, a1, a2, a3, a4)); }
        protected long   invoke_J(long   a0, long   a1, long   a2, long   a3, long   a4) throws Throwable { return return_J(targetA5(a0, a1, a2, a3, a4)); }
    }
    static class A6 extends Adapter {
        protected A6(MethodHandle entryPoint) { super(entryPoint); }  
        protected A6(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { super(e, i, c, t); }
        protected A6 makeInstance(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { return new A6(e, i, c, t); }
        protected Object target(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5)   throws Throwable { return invoker.invokeExact(target, a0, a1, a2, a3, a4, a5); }
        protected Object targetA6(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5) throws Throwable { return target(a0, a1, a2, a3, a4, a5); }
        protected Object targetA6(Object a0, Object a1, Object a2, Object a3, Object a4, long   a5) throws Throwable { return target(a0, a1, a2, a3, a4, a5); }
        protected Object targetA6(Object a0, Object a1, Object a2, Object a3, long   a4, long   a5) throws Throwable { return target(a0, a1, a2, a3, a4, a5); }
        protected Object targetA6(Object a0, Object a1, Object a2, long   a3, long   a4, long   a5) throws Throwable { return target(a0, a1, a2, a3, a4, a5); }
        protected Object targetA6(Object a0, Object a1, long   a2, long   a3, long   a4, long   a5) throws Throwable { return target(a0, a1, a2, a3, a4, a5); }
        protected Object targetA6(Object a0, long   a1, long   a2, long   a3, long   a4, long   a5) throws Throwable { return target(a0, a1, a2, a3, a4, a5); }
        protected Object targetA6(long   a0, long   a1, long   a2, long   a3, long   a4, long   a5) throws Throwable { return target(a0, a1, a2, a3, a4, a5); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5) throws Throwable { return return_L(targetA6(a0, a1, a2, a3, a4, a5)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5) throws Throwable { return return_I(targetA6(a0, a1, a2, a3, a4, a5)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5) throws Throwable { return return_J(targetA6(a0, a1, a2, a3, a4, a5)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, Object a4, long   a5) throws Throwable { return return_L(targetA6(a0, a1, a2, a3, a4, a5)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, Object a4, long   a5) throws Throwable { return return_I(targetA6(a0, a1, a2, a3, a4, a5)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, Object a4, long   a5) throws Throwable { return return_J(targetA6(a0, a1, a2, a3, a4, a5)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, long   a4, long   a5) throws Throwable { return return_L(targetA6(a0, a1, a2, a3, a4, a5)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, long   a4, long   a5) throws Throwable { return return_I(targetA6(a0, a1, a2, a3, a4, a5)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, long   a4, long   a5) throws Throwable { return return_J(targetA6(a0, a1, a2, a3, a4, a5)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, long   a3, long   a4, long   a5) throws Throwable { return return_L(targetA6(a0, a1, a2, a3, a4, a5)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, long   a3, long   a4, long   a5) throws Throwable { return return_I(targetA6(a0, a1, a2, a3, a4, a5)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, long   a3, long   a4, long   a5) throws Throwable { return return_J(targetA6(a0, a1, a2, a3, a4, a5)); }
        protected Object invoke_L(Object a0, Object a1, long   a2, long   a3, long   a4, long   a5) throws Throwable { return return_L(targetA6(a0, a1, a2, a3, a4, a5)); }
        protected int    invoke_I(Object a0, Object a1, long   a2, long   a3, long   a4, long   a5) throws Throwable { return return_I(targetA6(a0, a1, a2, a3, a4, a5)); }
        protected long   invoke_J(Object a0, Object a1, long   a2, long   a3, long   a4, long   a5) throws Throwable { return return_J(targetA6(a0, a1, a2, a3, a4, a5)); }
        protected Object invoke_L(Object a0, long   a1, long   a2, long   a3, long   a4, long   a5) throws Throwable { return return_L(targetA6(a0, a1, a2, a3, a4, a5)); }
        protected int    invoke_I(Object a0, long   a1, long   a2, long   a3, long   a4, long   a5) throws Throwable { return return_I(targetA6(a0, a1, a2, a3, a4, a5)); }
        protected long   invoke_J(Object a0, long   a1, long   a2, long   a3, long   a4, long   a5) throws Throwable { return return_J(targetA6(a0, a1, a2, a3, a4, a5)); }
        protected Object invoke_L(long   a0, long   a1, long   a2, long   a3, long   a4, long   a5) throws Throwable { return return_L(targetA6(a0, a1, a2, a3, a4, a5)); }
        protected int    invoke_I(long   a0, long   a1, long   a2, long   a3, long   a4, long   a5) throws Throwable { return return_I(targetA6(a0, a1, a2, a3, a4, a5)); }
        protected long   invoke_J(long   a0, long   a1, long   a2, long   a3, long   a4, long   a5) throws Throwable { return return_J(targetA6(a0, a1, a2, a3, a4, a5)); }
    }
    static class A7 extends Adapter {
        protected A7(MethodHandle entryPoint) { super(entryPoint); }  
        protected A7(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { super(e, i, c, t); }
        protected A7 makeInstance(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { return new A7(e, i, c, t); }
        protected Object target(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6)   throws Throwable { return invoker.invokeExact(target, a0, a1, a2, a3, a4, a5, a6); }
        protected Object targetA7(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6); }
        protected Object targetA7(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, long   a6) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6); }
        protected Object targetA7(Object a0, Object a1, Object a2, Object a3, Object a4, long   a5, long   a6) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6); }
        protected Object targetA7(Object a0, Object a1, Object a2, Object a3, long   a4, long   a5, long   a6) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6); }
        protected Object targetA7(Object a0, Object a1, Object a2, long   a3, long   a4, long   a5, long   a6) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6); }
        protected Object targetA7(Object a0, Object a1, long   a2, long   a3, long   a4, long   a5, long   a6) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6); }
        protected Object targetA7(Object a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6); }
        protected Object targetA7(long   a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6) throws Throwable { return return_L(targetA7(a0, a1, a2, a3, a4, a5, a6)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6) throws Throwable { return return_I(targetA7(a0, a1, a2, a3, a4, a5, a6)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6) throws Throwable { return return_J(targetA7(a0, a1, a2, a3, a4, a5, a6)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, long   a6) throws Throwable { return return_L(targetA7(a0, a1, a2, a3, a4, a5, a6)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, long   a6) throws Throwable { return return_I(targetA7(a0, a1, a2, a3, a4, a5, a6)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, long   a6) throws Throwable { return return_J(targetA7(a0, a1, a2, a3, a4, a5, a6)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, Object a4, long   a5, long   a6) throws Throwable { return return_L(targetA7(a0, a1, a2, a3, a4, a5, a6)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, Object a4, long   a5, long   a6) throws Throwable { return return_I(targetA7(a0, a1, a2, a3, a4, a5, a6)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, Object a4, long   a5, long   a6) throws Throwable { return return_J(targetA7(a0, a1, a2, a3, a4, a5, a6)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, long   a4, long   a5, long   a6) throws Throwable { return return_L(targetA7(a0, a1, a2, a3, a4, a5, a6)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, long   a4, long   a5, long   a6) throws Throwable { return return_I(targetA7(a0, a1, a2, a3, a4, a5, a6)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, long   a4, long   a5, long   a6) throws Throwable { return return_J(targetA7(a0, a1, a2, a3, a4, a5, a6)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, long   a3, long   a4, long   a5, long   a6) throws Throwable { return return_L(targetA7(a0, a1, a2, a3, a4, a5, a6)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, long   a3, long   a4, long   a5, long   a6) throws Throwable { return return_I(targetA7(a0, a1, a2, a3, a4, a5, a6)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, long   a3, long   a4, long   a5, long   a6) throws Throwable { return return_J(targetA7(a0, a1, a2, a3, a4, a5, a6)); }
        protected Object invoke_L(Object a0, Object a1, long   a2, long   a3, long   a4, long   a5, long   a6) throws Throwable { return return_L(targetA7(a0, a1, a2, a3, a4, a5, a6)); }
        protected int    invoke_I(Object a0, Object a1, long   a2, long   a3, long   a4, long   a5, long   a6) throws Throwable { return return_I(targetA7(a0, a1, a2, a3, a4, a5, a6)); }
        protected long   invoke_J(Object a0, Object a1, long   a2, long   a3, long   a4, long   a5, long   a6) throws Throwable { return return_J(targetA7(a0, a1, a2, a3, a4, a5, a6)); }
        protected Object invoke_L(Object a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6) throws Throwable { return return_L(targetA7(a0, a1, a2, a3, a4, a5, a6)); }
        protected int    invoke_I(Object a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6) throws Throwable { return return_I(targetA7(a0, a1, a2, a3, a4, a5, a6)); }
        protected long   invoke_J(Object a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6) throws Throwable { return return_J(targetA7(a0, a1, a2, a3, a4, a5, a6)); }
        protected Object invoke_L(long   a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6) throws Throwable { return return_L(targetA7(a0, a1, a2, a3, a4, a5, a6)); }
        protected int    invoke_I(long   a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6) throws Throwable { return return_I(targetA7(a0, a1, a2, a3, a4, a5, a6)); }
        protected long   invoke_J(long   a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6) throws Throwable { return return_J(targetA7(a0, a1, a2, a3, a4, a5, a6)); }
    }
    static class A8 extends Adapter {
        protected A8(MethodHandle entryPoint) { super(entryPoint); }  
        protected A8(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { super(e, i, c, t); }
        protected A8 makeInstance(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { return new A8(e, i, c, t); }
        protected Object target(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7)   throws Throwable { return invoker.invokeExact(target, a0, a1, a2, a3, a4, a5, a6, a7); }
        protected Object targetA8(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7); }
        protected Object targetA8(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, long   a7) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7); }
        protected Object targetA8(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, long   a6, long   a7) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7); }
        protected Object targetA8(Object a0, Object a1, Object a2, Object a3, Object a4, long   a5, long   a6, long   a7) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7); }
        protected Object targetA8(Object a0, Object a1, Object a2, Object a3, long   a4, long   a5, long   a6, long   a7) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7); }
        protected Object targetA8(Object a0, Object a1, Object a2, long   a3, long   a4, long   a5, long   a6, long   a7) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7); }
        protected Object targetA8(Object a0, Object a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7); }
        protected Object targetA8(Object a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7); }
        protected Object targetA8(long   a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7) throws Throwable { return return_L(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7) throws Throwable { return return_I(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7) throws Throwable { return return_J(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, long   a7) throws Throwable { return return_L(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, long   a7) throws Throwable { return return_I(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, long   a7) throws Throwable { return return_J(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, long   a6, long   a7) throws Throwable { return return_L(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, long   a6, long   a7) throws Throwable { return return_I(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, long   a6, long   a7) throws Throwable { return return_J(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, Object a4, long   a5, long   a6, long   a7) throws Throwable { return return_L(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, Object a4, long   a5, long   a6, long   a7) throws Throwable { return return_I(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, Object a4, long   a5, long   a6, long   a7) throws Throwable { return return_J(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, long   a4, long   a5, long   a6, long   a7) throws Throwable { return return_L(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, long   a4, long   a5, long   a6, long   a7) throws Throwable { return return_I(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, long   a4, long   a5, long   a6, long   a7) throws Throwable { return return_J(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, long   a3, long   a4, long   a5, long   a6, long   a7) throws Throwable { return return_L(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, long   a3, long   a4, long   a5, long   a6, long   a7) throws Throwable { return return_I(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, long   a3, long   a4, long   a5, long   a6, long   a7) throws Throwable { return return_J(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected Object invoke_L(Object a0, Object a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7) throws Throwable { return return_L(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected int    invoke_I(Object a0, Object a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7) throws Throwable { return return_I(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected long   invoke_J(Object a0, Object a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7) throws Throwable { return return_J(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected Object invoke_L(Object a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7) throws Throwable { return return_L(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected int    invoke_I(Object a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7) throws Throwable { return return_I(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected long   invoke_J(Object a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7) throws Throwable { return return_J(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected Object invoke_L(long   a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7) throws Throwable { return return_L(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected int    invoke_I(long   a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7) throws Throwable { return return_I(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
        protected long   invoke_J(long   a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7) throws Throwable { return return_J(targetA8(a0, a1, a2, a3, a4, a5, a6, a7)); }
    }
    static class A9 extends Adapter {
        protected A9(MethodHandle entryPoint) { super(entryPoint); }  
        protected A9(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { super(e, i, c, t); }
        protected A9 makeInstance(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { return new A9(e, i, c, t); }
        protected Object target(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8)   throws Throwable { return invoker.invokeExact(target, a0, a1, a2, a3, a4, a5, a6, a7, a8); }
        protected Object targetA9(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7, a8); }
        protected Object targetA9(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, long   a8) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7, a8); }
        protected Object targetA9(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, long   a7, long   a8) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7, a8); }
        protected Object targetA9(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, long   a6, long   a7, long   a8) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7, a8); }
        protected Object targetA9(Object a0, Object a1, Object a2, Object a3, Object a4, long   a5, long   a6, long   a7, long   a8) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7, a8); }
        protected Object targetA9(Object a0, Object a1, Object a2, Object a3, long   a4, long   a5, long   a6, long   a7, long   a8) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7, a8); }
        protected Object targetA9(Object a0, Object a1, Object a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7, a8); }
        protected Object targetA9(Object a0, Object a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7, a8); }
        protected Object targetA9(Object a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7, a8); }
        protected Object targetA9(long   a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7, a8); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8) throws Throwable { return return_L(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8) throws Throwable { return return_I(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8) throws Throwable { return return_J(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, long   a8) throws Throwable { return return_L(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, long   a8) throws Throwable { return return_I(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, long   a8) throws Throwable { return return_J(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, long   a7, long   a8) throws Throwable { return return_L(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, long   a7, long   a8) throws Throwable { return return_I(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, long   a7, long   a8) throws Throwable { return return_J(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, long   a6, long   a7, long   a8) throws Throwable { return return_L(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, long   a6, long   a7, long   a8) throws Throwable { return return_I(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, long   a6, long   a7, long   a8) throws Throwable { return return_J(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, Object a4, long   a5, long   a6, long   a7, long   a8) throws Throwable { return return_L(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, Object a4, long   a5, long   a6, long   a7, long   a8) throws Throwable { return return_I(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, Object a4, long   a5, long   a6, long   a7, long   a8) throws Throwable { return return_J(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, long   a4, long   a5, long   a6, long   a7, long   a8) throws Throwable { return return_L(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, long   a4, long   a5, long   a6, long   a7, long   a8) throws Throwable { return return_I(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, long   a4, long   a5, long   a6, long   a7, long   a8) throws Throwable { return return_J(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8) throws Throwable { return return_L(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8) throws Throwable { return return_I(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8) throws Throwable { return return_J(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected Object invoke_L(Object a0, Object a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8) throws Throwable { return return_L(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected int    invoke_I(Object a0, Object a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8) throws Throwable { return return_I(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected long   invoke_J(Object a0, Object a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8) throws Throwable { return return_J(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected Object invoke_L(Object a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8) throws Throwable { return return_L(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected int    invoke_I(Object a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8) throws Throwable { return return_I(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected long   invoke_J(Object a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8) throws Throwable { return return_J(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected Object invoke_L(long   a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8) throws Throwable { return return_L(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected int    invoke_I(long   a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8) throws Throwable { return return_I(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
        protected long   invoke_J(long   a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8) throws Throwable { return return_J(targetA9(a0, a1, a2, a3, a4, a5, a6, a7, a8)); }
    }
    static class A10 extends Adapter {
        protected A10(MethodHandle entryPoint) { super(entryPoint); }  
        protected A10(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { super(e, i, c, t); }
        protected A10 makeInstance(MethodHandle e, MethodHandle i, MethodHandle c, MethodHandle t) { return new A10(e, i, c, t); }
        protected Object target(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9)   throws Throwable { return invoker.invokeExact(target, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9); }
        protected Object targetA10(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9); }
        protected Object targetA10(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, long   a9) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9); }
        protected Object targetA10(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, long   a8, long   a9) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9); }
        protected Object targetA10(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, long   a7, long   a8, long   a9) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9); }
        protected Object targetA10(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9); }
        protected Object targetA10(Object a0, Object a1, Object a2, Object a3, Object a4, long   a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9); }
        protected Object targetA10(Object a0, Object a1, Object a2, Object a3, long   a4, long   a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9); }
        protected Object targetA10(Object a0, Object a1, Object a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9); }
        protected Object targetA10(Object a0, Object a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9); }
        protected Object targetA10(Object a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9); }
        protected Object targetA10(long   a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return target(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9) throws Throwable { return return_L(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9) throws Throwable { return return_I(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9) throws Throwable { return return_J(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, long   a9) throws Throwable { return return_L(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, long   a9) throws Throwable { return return_I(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, long   a9) throws Throwable { return return_J(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, long   a8, long   a9) throws Throwable { return return_L(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, long   a8, long   a9) throws Throwable { return return_I(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, long   a8, long   a9) throws Throwable { return return_J(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, long   a7, long   a8, long   a9) throws Throwable { return return_L(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, long   a7, long   a8, long   a9) throws Throwable { return return_I(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, long   a7, long   a8, long   a9) throws Throwable { return return_J(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return return_L(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return return_I(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return return_J(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, Object a4, long   a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return return_L(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, Object a4, long   a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return return_I(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, Object a4, long   a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return return_J(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, Object a3, long   a4, long   a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return return_L(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, Object a3, long   a4, long   a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return return_I(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, Object a3, long   a4, long   a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return return_J(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected Object invoke_L(Object a0, Object a1, Object a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return return_L(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected int    invoke_I(Object a0, Object a1, Object a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return return_I(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected long   invoke_J(Object a0, Object a1, Object a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return return_J(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected Object invoke_L(Object a0, Object a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return return_L(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected int    invoke_I(Object a0, Object a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return return_I(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected long   invoke_J(Object a0, Object a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return return_J(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected Object invoke_L(Object a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return return_L(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected int    invoke_I(Object a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return return_I(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected long   invoke_J(Object a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return return_J(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected Object invoke_L(long   a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return return_L(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected int    invoke_I(long   a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return return_I(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
        protected long   invoke_J(long   a0, long   a1, long   a2, long   a3, long   a4, long   a5, long   a6, long   a7, long   a8, long   a9) throws Throwable { return return_J(targetA10(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9)); }
    }
}
