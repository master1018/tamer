public final class ExceptionMapper {
    private ExceptionMapper() {} 
    private static final boolean debug = false;
    public static final NamingException mapException(Exception e,
        CNCtx ctx, NameComponent[] inputName) throws NamingException {
        if (e instanceof NamingException) {
            return (NamingException)e;
        }
        if (e instanceof RuntimeException) {
            throw (RuntimeException)e;
        }
        NamingException ne;
        if (e instanceof NotFound) {
            if (ctx.federation) {
                return tryFed((NotFound)e, ctx, inputName);
            } else {
                ne = new NameNotFoundException();
            }
        } else if (e instanceof CannotProceed) {
            ne = new CannotProceedException();
            NamingContext nc = ((CannotProceed) e).cxt;
            NameComponent[] rest = ((CannotProceed) e).rest_of_name;
            if (inputName != null && (inputName.length > rest.length)) {
                NameComponent[] resolvedName =
                    new NameComponent[inputName.length - rest.length];
                System.arraycopy(inputName, 0, resolvedName, 0, resolvedName.length);
                ne.setResolvedObj(new CNCtx(ctx._orb, ctx.orbTracker, nc,
                                                ctx._env,
                    ctx.makeFullName(resolvedName)));
            } else {
                ne.setResolvedObj(ctx);
            }
            ne.setRemainingName(CNNameParser.cosNameToName(rest));
        } else if (e instanceof InvalidName) {
            ne = new InvalidNameException();
        } else if (e instanceof AlreadyBound) {
            ne = new NameAlreadyBoundException();
        } else if (e instanceof NotEmpty) {
            ne = new ContextNotEmptyException();
        } else {
            ne = new NamingException("Unknown reasons");
        }
        ne.setRootCause(e);
        return ne;
    }
    private static final NamingException tryFed(NotFound e, CNCtx ctx,
        NameComponent[] inputName) throws NamingException {
        NameComponent[] rest = ((NotFound) e).rest_of_name;
        if (debug) {
            System.out.println(((NotFound)e).why.value());
            System.out.println(rest.length);
        }
        if (rest.length == 1 && inputName != null) {
            NameComponent lastIn = inputName[inputName.length-1];
            if (rest[0].id.equals(lastIn.id) &&
                rest[0].kind != null &&
                rest[0].kind.equals(lastIn.kind)) {
                ;
            } else {
                NamingException ne = new NameNotFoundException();
                ne.setRemainingName(CNNameParser.cosNameToName(rest));
                ne.setRootCause(e);
                throw ne;
            }
        }
        NameComponent[] resolvedName = null;
        int len = 0;
        if (inputName != null && (inputName.length >= rest.length)) {
            if (e.why == NotFoundReason.not_context) {
                len = inputName.length - (rest.length - 1);
                if (rest.length == 1) {
                    rest = null;
                } else {
                    NameComponent[] tmp = new NameComponent[rest.length-1];
                    System.arraycopy(rest, 1, tmp, 0, tmp.length);
                    rest = tmp;
                }
            } else {
                len = inputName.length - rest.length;
            }
            if (len > 0) {
                resolvedName = new NameComponent[len];
                System.arraycopy(inputName, 0, resolvedName, 0, len);
            }
        }
        CannotProceedException cpe = new CannotProceedException();
        cpe.setRootCause(e);
        if (rest != null && rest.length > 0) {
            cpe.setRemainingName(CNNameParser.cosNameToName(rest));
        }
        cpe.setEnvironment(ctx._env);
        if (debug) {
            System.out.println("rest of name: " + cpe.getRemainingName());
        }
        final java.lang.Object resolvedObj =
            (resolvedName != null) ? ctx.callResolve(resolvedName) : ctx;
        if (resolvedObj instanceof javax.naming.Context) {
            RefAddr addr = new RefAddr("nns") {
                public java.lang.Object getContent() {
                    return resolvedObj;
                }
                private static final long serialVersionUID =
                    669984699392133792L;
            };
            Reference ref = new Reference("java.lang.Object", addr);
            CompositeName cname = new CompositeName();
            cname.add(""); 
            cpe.setResolvedObj(ref);
            cpe.setAltName(cname);
            cpe.setAltNameCtx((javax.naming.Context)resolvedObj);
            return cpe;
        } else {
            Name cname = CNNameParser.cosNameToName(resolvedName);
            java.lang.Object resolvedObj2;
            try {
                resolvedObj2 = NamingManager.getObjectInstance(resolvedObj,
                    cname, ctx, ctx._env);
            } catch (NamingException ge) {
                throw ge;
            } catch (Exception ge) {
                NamingException ne = new NamingException(
                    "problem generating object using object factory");
                ne.setRootCause(ge);
                throw ne;
            }
            if (resolvedObj2 instanceof javax.naming.Context) {
                cpe.setResolvedObj(resolvedObj2);
            } else {
                cname.add("");
                cpe.setAltName(cname);
                final java.lang.Object rf2 = resolvedObj2;
                RefAddr addr = new RefAddr("nns") {
                    public java.lang.Object getContent() {
                        return rf2;
                    }
                    private static final long serialVersionUID =
                        -785132553978269772L;
                };
                Reference ref = new Reference("java.lang.Object", addr);
                cpe.setResolvedObj(ref);
                cpe.setAltNameCtx(ctx);
            }
            return cpe;
        }
    }
}
