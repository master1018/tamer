public class BasicRouteDirector implements HttpRouteDirector {
    public int nextStep(RouteInfo plan, RouteInfo fact) {
        if (plan == null) {
            throw new IllegalArgumentException
                ("Planned route may not be null.");
        }
        int step = UNREACHABLE;
        if ((fact == null) || (fact.getHopCount() < 1))
            step = firstStep(plan);
        else if (plan.getHopCount() > 1)
            step = proxiedStep(plan, fact);
        else
            step = directStep(plan, fact);
        return step;
    } 
    protected int firstStep(RouteInfo plan) {
        return (plan.getHopCount() > 1) ?
            CONNECT_PROXY : CONNECT_TARGET;
    }
    protected int directStep(RouteInfo plan, RouteInfo fact) {
        if (fact.getHopCount() > 1)
            return UNREACHABLE;
        if (!plan.getTargetHost().equals(fact.getTargetHost()))
            return UNREACHABLE;
        if (plan.isSecure() != fact.isSecure())
            return UNREACHABLE;
        if ((plan.getLocalAddress() != null) &&
            !plan.getLocalAddress().equals(fact.getLocalAddress())
            )
            return UNREACHABLE;
        return COMPLETE;
    }
    protected int proxiedStep(RouteInfo plan, RouteInfo fact) {
        if (fact.getHopCount() <= 1)
            return UNREACHABLE;
        if (!plan.getTargetHost().equals(fact.getTargetHost()))
            return UNREACHABLE;
        final int phc = plan.getHopCount();
        final int fhc = fact.getHopCount();
        if (phc < fhc)
            return UNREACHABLE;
        for (int i=0; i<fhc-1; i++) {
            if (!plan.getHopTarget(i).equals(fact.getHopTarget(i)))
                return UNREACHABLE;
        }
        if (phc > fhc)
            return TUNNEL_PROXY; 
        if ((fact.isTunnelled() && !plan.isTunnelled()) ||
            (fact.isLayered()   && !plan.isLayered()))
            return UNREACHABLE;
        if (plan.isTunnelled() && !fact.isTunnelled())
            return TUNNEL_TARGET;
        if (plan.isLayered() && !fact.isLayered())
            return LAYER_PROTOCOL;
        if (plan.isSecure() != fact.isSecure())
            return UNREACHABLE;
        return COMPLETE;
    }
} 
