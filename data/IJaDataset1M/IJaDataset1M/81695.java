package org.compiere.process;

import java.util.logging.*;
import org.compiere.impexp.*;
import org.compiere.model.*;

/**
 *	Bank Statement Matching
 *	
 *  @author Jorg Janke
 *  @version $Id: BankStatementMatcher.java,v 1.3 2006/09/25 00:59:41 jjanke Exp $
 */
public class BankStatementMatcher extends SvrProcess {

    /**	Matchers					*/
    MBankStatementMatcher[] m_matchers = null;

    /**
	 *  Prepare - e.g., get Parameters.
	 */
    protected void prepare() {
        ProcessInfoParameter[] para = getParameter();
        for (int i = 0; i < para.length; i++) {
            String name = para[i].getParameterName();
            if (para[i].getParameter() == null) ; else log.log(Level.SEVERE, "Unknown Parameter: " + name);
        }
        m_matchers = MBankStatementMatcher.getMatchers(getCtx(), get_TrxName());
    }

    /**
	 *  Perform process.
	 *  @return Message 
	 *  @throws Exception if not successful
	 */
    protected String doIt() throws Exception {
        int Table_ID = getTable_ID();
        int Record_ID = getRecord_ID();
        if (m_matchers == null || m_matchers.length == 0) throw new IllegalStateException("No Matchers found");
        log.info("doIt - Table_ID=" + Table_ID + ", Record_ID=" + Record_ID + ", Matchers=" + m_matchers.length);
        if (Table_ID == X_I_BankStatement.Table_ID) return match(new X_I_BankStatement(getCtx(), Record_ID, get_TrxName())); else if (Table_ID == MBankStatement.Table_ID) return match(new MBankStatement(getCtx(), Record_ID, get_TrxName())); else if (Table_ID == MBankStatementLine.Table_ID) return match(new MBankStatementLine(getCtx(), Record_ID, get_TrxName()));
        return "??";
    }

    /**
	 * 	Perform Match
	 *	@param ibs import bank statement line
	 *	@return Message
	 */
    private String match(X_I_BankStatement ibs) {
        if (m_matchers == null || ibs == null || ibs.getC_Payment_ID() != 0) return "--";
        log.fine("" + ibs);
        BankStatementMatchInfo info = null;
        for (int i = 0; i < m_matchers.length; i++) {
            if (m_matchers[i].isMatcherValid()) {
                info = m_matchers[i].getMatcher().findMatch(ibs);
                if (info != null && info.isMatched()) {
                    if (info.getC_Payment_ID() > 0) ibs.setC_Payment_ID(info.getC_Payment_ID());
                    if (info.getC_Invoice_ID() > 0) ibs.setC_Invoice_ID(info.getC_Invoice_ID());
                    if (info.getC_BPartner_ID() > 0) ibs.setC_BPartner_ID(info.getC_BPartner_ID());
                    ibs.save();
                    return "OK";
                }
            }
        }
        return "--";
    }

    /**
	 * 	Perform Match
	 *	@param bsl bank statement line
	 *	@return Message
	 */
    private String match(MBankStatementLine bsl) {
        if (m_matchers == null || bsl == null || bsl.getC_Payment_ID() != 0) return "--";
        log.fine("match - " + bsl);
        BankStatementMatchInfo info = null;
        for (int i = 0; i < m_matchers.length; i++) {
            if (m_matchers[i].isMatcherValid()) {
                info = m_matchers[i].getMatcher().findMatch(bsl);
                if (info != null && info.isMatched()) {
                    if (info.getC_Payment_ID() > 0) bsl.setC_Payment_ID(info.getC_Payment_ID());
                    if (info.getC_Invoice_ID() > 0) bsl.setC_Invoice_ID(info.getC_Invoice_ID());
                    if (info.getC_BPartner_ID() > 0) bsl.setC_BPartner_ID(info.getC_BPartner_ID());
                    bsl.save();
                    return "OK";
                }
            }
        }
        return "--";
    }

    /**
	 * 	Perform Match
	 *	@param bs bank statement
	 *	@return Message
	 */
    private String match(MBankStatement bs) {
        if (m_matchers == null || bs == null) return "--";
        log.fine("match - " + bs);
        int count = 0;
        MBankStatementLine[] lines = bs.getLines(false);
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].getC_Payment_ID() == 0) {
                match(lines[i]);
                count++;
            }
        }
        return String.valueOf(count);
    }
}
