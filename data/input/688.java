abstract class BodyBuilder {
    abstract Message createMessage(Object[] methodArgs);
    static final BodyBuilder EMPTY_SOAP11 = new Empty(SOAPVersion.SOAP_11);
    static final BodyBuilder EMPTY_SOAP12 = new Empty(SOAPVersion.SOAP_12);
    private static final class Empty extends BodyBuilder {
        private final SOAPVersion soapVersion;
        public Empty(SOAPVersion soapVersion) {
            this.soapVersion = soapVersion;
        }
        Message createMessage(Object[] methodArgs) {
            return Messages.createEmpty(soapVersion);
        }
    }
    private abstract static class JAXB extends BodyBuilder {
        private final Bridge bridge;
        private final SOAPVersion soapVersion;
        protected JAXB(Bridge bridge, SOAPVersion soapVersion) {
            assert bridge != null;
            this.bridge = bridge;
            this.soapVersion = soapVersion;
        }
        final Message createMessage(Object[] methodArgs) {
            return JAXBMessage.create(bridge, build(methodArgs), soapVersion);
        }
        abstract Object build(Object[] methodArgs);
    }
    static final class Bare extends JAXB {
        private final int methodPos;
        private final ValueGetter getter;
        Bare(ParameterImpl p, SOAPVersion soapVersion, ValueGetter getter) {
            super(p.getBridge(), soapVersion);
            this.methodPos = p.getIndex();
            this.getter = getter;
        }
        Object build(Object[] methodArgs) {
            return getter.get(methodArgs[methodPos]);
        }
    }
    abstract static class Wrapped extends JAXB {
        protected final int[] indices;
        protected final ValueGetter[] getters;
        protected Wrapped(WrapperParameter wp, SOAPVersion soapVersion, ValueGetterFactory getter) {
            super(wp.getBridge(), soapVersion);
            List<ParameterImpl> children = wp.getWrapperChildren();
            indices = new int[children.size()];
            getters = new ValueGetter[children.size()];
            for (int i = 0; i < indices.length; i++) {
                ParameterImpl p = children.get(i);
                indices[i] = p.getIndex();
                getters[i] = getter.get(p);
            }
        }
    }
    static final class DocLit extends Wrapped {
        private final RawAccessor[] accessors;
        private final Class wrapper;
        DocLit(WrapperParameter wp, SOAPVersion soapVersion, ValueGetterFactory getter) {
            super(wp, soapVersion, getter);
            wrapper = (Class) wp.getBridge().getTypeReference().type;
            List<ParameterImpl> children = wp.getWrapperChildren();
            accessors = new RawAccessor[children.size()];
            for (int i = 0; i < accessors.length; i++) {
                ParameterImpl p = children.get(i);
                QName name = p.getName();
                try {
                    accessors[i] = p.getOwner().getJAXBContext().getElementPropertyAccessor(wrapper, name.getNamespaceURI(), name.getLocalPart());
                } catch (JAXBException e) {
                    throw new WebServiceException(wrapper + " do not have a property of the name " + name, e);
                }
            }
        }
        Object build(Object[] methodArgs) {
            try {
                Object bean = wrapper.newInstance();
                for (int i = indices.length - 1; i >= 0; i--) {
                    accessors[i].set(bean, getters[i].get(methodArgs[indices[i]]));
                }
                return bean;
            } catch (InstantiationException e) {
                Error x = new InstantiationError(e.getMessage());
                x.initCause(e);
                throw x;
            } catch (IllegalAccessException e) {
                Error x = new IllegalAccessError(e.getMessage());
                x.initCause(e);
                throw x;
            } catch (AccessorException e) {
                throw new WebServiceException(e);
            }
        }
    }
    static final class RpcLit extends Wrapped {
        private final Bridge[] parameterBridges;
        private final List<ParameterImpl> children;
        RpcLit(WrapperParameter wp, SOAPVersion soapVersion, ValueGetterFactory getter) {
            super(wp, soapVersion, getter);
            assert wp.getTypeReference().type == CompositeStructure.class;
            this.children = wp.getWrapperChildren();
            parameterBridges = new Bridge[children.size()];
            for (int i = 0; i < parameterBridges.length; i++) parameterBridges[i] = children.get(i).getBridge();
        }
        CompositeStructure build(Object[] methodArgs) {
            CompositeStructure cs = new CompositeStructure();
            cs.bridges = parameterBridges;
            cs.values = new Object[parameterBridges.length];
            for (int i = indices.length - 1; i >= 0; i--) {
                Object arg = getters[i].get(methodArgs[indices[i]]);
                if (arg == null) {
                    throw new WebServiceException("Method Parameter: " + children.get(i).getName() + " cannot be null. This is BP 1.1 R2211 violation.");
                }
                cs.values[i] = arg;
            }
            return cs;
        }
    }
}
