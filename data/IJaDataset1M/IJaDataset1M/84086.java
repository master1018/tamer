package net.sf.cb2xml.sablecc.node;

import java.util.*;
import net.sf.cb2xml.sablecc.analysis.*;

public final class ALowValuesLiteral extends PLiteral {

    private TLowValues _lowValues_;

    public ALowValuesLiteral() {
    }

    public ALowValuesLiteral(TLowValues _lowValues_) {
        setLowValues(_lowValues_);
    }

    public Object clone() {
        return new ALowValuesLiteral((TLowValues) cloneNode(_lowValues_));
    }

    public void apply(Switch sw) {
        ((Analysis) sw).caseALowValuesLiteral(this);
    }

    public TLowValues getLowValues() {
        return _lowValues_;
    }

    public void setLowValues(TLowValues node) {
        if (_lowValues_ != null) {
            _lowValues_.parent(null);
        }
        if (node != null) {
            if (node.parent() != null) {
                node.parent().removeChild(node);
            }
            node.parent(this);
        }
        _lowValues_ = node;
    }

    public String toString() {
        return "" + toString(_lowValues_);
    }

    void removeChild(Node child) {
        if (_lowValues_ == child) {
            _lowValues_ = null;
            return;
        }
    }

    void replaceChild(Node oldChild, Node newChild) {
        if (_lowValues_ == oldChild) {
            setLowValues((TLowValues) newChild);
            return;
        }
    }
}
