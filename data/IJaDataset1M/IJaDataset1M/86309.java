package com.google.gwt.dev.jjs;

import com.google.gwt.dev.jjs.Correlation.Axis;
import com.google.gwt.dev.jjs.Correlation.Literal;
import com.google.gwt.dev.jjs.ast.JDeclaredType;
import com.google.gwt.dev.jjs.ast.JField;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JType;
import com.google.gwt.dev.js.ast.JsFunction;
import com.google.gwt.dev.js.ast.JsName;
import org.apache.commons.collections.map.ReferenceMap;
import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * A {@link Correlation} factory.
 */
public abstract class CorrelationFactory implements Serializable {

    /**
   * A dummy factory that always returns <code>null</code>.
   */
    public static final class DummyCorrelationFactory extends CorrelationFactory {

        @Override
        public Correlation by(JField field) {
            return null;
        }

        @Override
        public Correlation by(JMethod method) {
            return null;
        }

        @Override
        public Correlation by(JDeclaredType type) {
            return null;
        }

        @Override
        public Correlation by(JsFunction function) {
            return null;
        }

        @Override
        public Correlation by(JsName name) {
            return null;
        }

        @Override
        public Correlation by(JsName name, boolean isAlias) {
            return null;
        }

        @Override
        public Correlation by(Literal type) {
            return null;
        }

        @Override
        public Correlation by(SourceOrigin origin) {
            return null;
        }

        @Override
        public SourceInfo makeSourceInfo(SourceOrigin origin) {
            return origin;
        }
    }

    /**
   * A real factory that returns new {@link Correlation Correlations}.
   */
    public static final class RealCorrelationFactory extends CorrelationFactory {

        /**
     * Correlations based on Literals are all the same, so we'll just cook up a
     * Map to make {@link #by(Literal)} fast.
     */
        private static final Map<Literal, Correlation> LITERAL_CORRELATIONS = new EnumMap<Literal, Correlation>(Literal.class);

        static {
            for (Literal l : Literal.values()) {
                LITERAL_CORRELATIONS.put(l, new Correlation(Axis.LITERAL, l.getDescription(), l));
            }
        }

        private static String getMethodIdent(JMethod method) {
            StringBuilder sb = new StringBuilder();
            sb.append(method.getEnclosingType().getName()).append("::");
            sb.append(method.getName()).append("(");
            for (JType type : method.getOriginalParamTypes()) {
                sb.append(type.getJsniSignatureName());
            }
            sb.append(")");
            sb.append(method.getOriginalReturnType().getJsniSignatureName());
            return sb.toString();
        }

        /**
     * This cuts down on the total number of Correlation objects allocated.
     */
        @SuppressWarnings("unchecked")
        private final Map<Object, Correlation> canonicalMap = Collections.synchronizedMap(new ReferenceMap(ReferenceMap.WEAK, ReferenceMap.WEAK));

        @Override
        public Correlation by(JField field) {
            Correlation toReturn = canonicalMap.get(field);
            if (toReturn == null) {
                toReturn = new Correlation(Axis.FIELD, field.getEnclosingType().getName() + "::" + field.getName(), field);
                canonicalMap.put(field, toReturn);
            }
            return toReturn;
        }

        @Override
        public Correlation by(JMethod method) {
            Correlation toReturn = canonicalMap.get(method);
            if (toReturn == null) {
                toReturn = new Correlation(Axis.METHOD, getMethodIdent(method), method);
                canonicalMap.put(method, toReturn);
            }
            return toReturn;
        }

        @Override
        public Correlation by(JDeclaredType type) {
            Correlation toReturn = canonicalMap.get(type);
            if (toReturn == null) {
                toReturn = new Correlation(Axis.CLASS, type.getName(), type);
                canonicalMap.put(type, toReturn);
            }
            return toReturn;
        }

        @Override
        public Correlation by(JsFunction function) {
            Correlation toReturn = canonicalMap.get(function);
            if (toReturn == null) {
                toReturn = new Correlation(Axis.FUNCTION, function.getName().getIdent(), function);
                canonicalMap.put(function, toReturn);
            }
            return toReturn;
        }

        /**
     * Creates a JS_NAME Correlation.
     */
        @Override
        public Correlation by(JsName name) {
            return by(name, false);
        }

        /**
     * Creates either a JS_NAME or JS_ALIAS correlation, based on the value of
     * <code>isAlias</code>.
     */
        @Override
        public Correlation by(JsName name, boolean isAlias) {
            Correlation toReturn = canonicalMap.get(name);
            if (toReturn == null) {
                toReturn = new Correlation(isAlias ? Axis.JS_ALIAS : Axis.JS_NAME, name.getIdent(), name);
                canonicalMap.put(name, toReturn);
            }
            return toReturn;
        }

        @Override
        public Correlation by(Literal type) {
            assert LITERAL_CORRELATIONS.containsKey(type);
            return LITERAL_CORRELATIONS.get(type);
        }

        @Override
        public Correlation by(SourceOrigin origin) {
            Correlation toReturn = canonicalMap.get(origin);
            if (toReturn == null) {
                toReturn = new Correlation(Axis.ORIGIN, origin.getFileName() + ":" + origin.getStartLine(), origin);
                canonicalMap.put(origin, toReturn);
            }
            return toReturn;
        }

        @Override
        public SourceInfo makeSourceInfo(SourceOrigin origin) {
            return new SourceInfoCorrelation(origin);
        }
    }

    public abstract Correlation by(JField field);

    public abstract Correlation by(JMethod method);

    public abstract Correlation by(JDeclaredType type);

    public abstract Correlation by(JsFunction function);

    public abstract Correlation by(JsName name);

    public abstract Correlation by(JsName name, boolean isAlias);

    public abstract Correlation by(Literal type);

    public abstract Correlation by(SourceOrigin origin);

    public abstract SourceInfo makeSourceInfo(SourceOrigin origin);
}
