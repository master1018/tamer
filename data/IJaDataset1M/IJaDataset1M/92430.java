package net.sf.cb2xml.sablecc.node;

import net.sf.cb2xml.sablecc.analysis.*;

public final class TDollar extends Token {

    public TDollar() {
        super.setText("$");
    }

    public TDollar(int line, int pos) {
        super.setText("$");
        setLine(line);
        setPos(pos);
    }

    public Object clone() {
        return new TDollar(getLine(), getPos());
    }

    public void apply(Switch sw) {
        ((Analysis) sw).caseTDollar(this);
    }

    public void setText(String text) {
        throw new RuntimeException("Cannot change TDollar text.");
    }
}
