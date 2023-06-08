package com.javapoint.ibatis.entity.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TicketExample {

    /**
	 * This field was generated by Abator for iBATIS. This field corresponds to the database table HELPDESKDB.TKT_TICKET
	 * @abatorgenerated  Thu Sep 10 20:41:52 GMT 2009
	 */
    protected String orderByClause;

    /**
	 * This field was generated by Abator for iBATIS. This field corresponds to the database table HELPDESKDB.TKT_TICKET
	 * @abatorgenerated  Thu Sep 10 20:41:52 GMT 2009
	 */
    protected List oredCriteria;

    /**
	 * This method was generated by Abator for iBATIS. This method corresponds to the database table HELPDESKDB.TKT_TICKET
	 * @abatorgenerated  Thu Sep 10 20:41:52 GMT 2009
	 */
    public TicketExample() {
        oredCriteria = new ArrayList();
    }

    /**
	 * This method was generated by Abator for iBATIS. This method corresponds to the database table HELPDESKDB.TKT_TICKET
	 * @abatorgenerated  Thu Sep 10 20:41:52 GMT 2009
	 */
    protected TicketExample(TicketExample example) {
        this.orderByClause = example.orderByClause;
        this.oredCriteria = example.oredCriteria;
    }

    /**
	 * This method was generated by Abator for iBATIS. This method corresponds to the database table HELPDESKDB.TKT_TICKET
	 * @abatorgenerated  Thu Sep 10 20:41:52 GMT 2009
	 */
    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    /**
	 * This method was generated by Abator for iBATIS. This method corresponds to the database table HELPDESKDB.TKT_TICKET
	 * @abatorgenerated  Thu Sep 10 20:41:52 GMT 2009
	 */
    public String getOrderByClause() {
        return orderByClause;
    }

    /**
	 * This method was generated by Abator for iBATIS. This method corresponds to the database table HELPDESKDB.TKT_TICKET
	 * @abatorgenerated  Thu Sep 10 20:41:52 GMT 2009
	 */
    public List getOredCriteria() {
        return oredCriteria;
    }

    /**
	 * This method was generated by Abator for iBATIS. This method corresponds to the database table HELPDESKDB.TKT_TICKET
	 * @abatorgenerated  Thu Sep 10 20:41:52 GMT 2009
	 */
    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    /**
	 * This method was generated by Abator for iBATIS. This method corresponds to the database table HELPDESKDB.TKT_TICKET
	 * @abatorgenerated  Thu Sep 10 20:41:52 GMT 2009
	 */
    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    /**
	 * This method was generated by Abator for iBATIS. This method corresponds to the database table HELPDESKDB.TKT_TICKET
	 * @abatorgenerated  Thu Sep 10 20:41:52 GMT 2009
	 */
    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    /**
	 * This method was generated by Abator for iBATIS. This method corresponds to the database table HELPDESKDB.TKT_TICKET
	 * @abatorgenerated  Thu Sep 10 20:41:52 GMT 2009
	 */
    public void clear() {
        oredCriteria.clear();
    }

    /**
	 * This class was generated by Abator for iBATIS. This class corresponds to the database table HELPDESKDB.TKT_TICKET
	 * @abatorgenerated  Thu Sep 10 20:41:52 GMT 2009
	 */
    public static class Criteria {

        protected List criteriaWithoutValue;

        protected List criteriaWithSingleValue;

        protected List criteriaWithListValue;

        protected List criteriaWithBetweenValue;

        protected Criteria() {
            super();
            criteriaWithoutValue = new ArrayList();
            criteriaWithSingleValue = new ArrayList();
            criteriaWithListValue = new ArrayList();
            criteriaWithBetweenValue = new ArrayList();
        }

        public boolean isValid() {
            return criteriaWithoutValue.size() > 0 || criteriaWithSingleValue.size() > 0 || criteriaWithListValue.size() > 0 || criteriaWithBetweenValue.size() > 0;
        }

        public List getCriteriaWithoutValue() {
            return criteriaWithoutValue;
        }

        public List getCriteriaWithSingleValue() {
            return criteriaWithSingleValue;
        }

        public List getCriteriaWithListValue() {
            return criteriaWithListValue;
        }

        public List getCriteriaWithBetweenValue() {
            return criteriaWithBetweenValue;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteriaWithoutValue.add(condition);
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            Map map = new HashMap();
            map.put("condition", condition);
            map.put("value", value);
            criteriaWithSingleValue.add(map);
        }

        protected void addCriterion(String condition, List values, String property) {
            if (values == null || values.size() == 0) {
                throw new RuntimeException("Value list for " + property + " cannot be null or empty");
            }
            Map map = new HashMap();
            map.put("condition", condition);
            map.put("values", values);
            criteriaWithListValue.add(map);
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            List list = new ArrayList();
            list.add(value1);
            list.add(value2);
            Map map = new HashMap();
            map.put("condition", condition);
            map.put("values", list);
            criteriaWithBetweenValue.add(map);
        }

        protected void addCriterionForJDBCDate(String condition, Date value, String property) {
            addCriterion(condition, new java.sql.Date(value.getTime()), property);
        }

        protected void addCriterionForJDBCDate(String condition, List values, String property) {
            if (values == null || values.size() == 0) {
                throw new RuntimeException("Value list for " + property + " cannot be null or empty");
            }
            List dateList = new ArrayList();
            Iterator iter = values.iterator();
            while (iter.hasNext()) {
                dateList.add(new java.sql.Date(((Date) iter.next()).getTime()));
            }
            addCriterion(condition, dateList, property);
        }

        protected void addCriterionForJDBCDate(String condition, Date value1, Date value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            addCriterion(condition, new java.sql.Date(value1.getTime()), new java.sql.Date(value2.getTime()), property);
        }

        public Criteria andEquipmentIdIsNull() {
            addCriterion("EQUIPMENT_ID is null");
            return this;
        }

        public Criteria andEquipmentIdIsNotNull() {
            addCriterion("EQUIPMENT_ID is not null");
            return this;
        }

        public Criteria andEquipmentIdEqualTo(Integer value) {
            addCriterion("EQUIPMENT_ID =", value, "equipmentId");
            return this;
        }

        public Criteria andEquipmentIdNotEqualTo(Integer value) {
            addCriterion("EQUIPMENT_ID <>", value, "equipmentId");
            return this;
        }

        public Criteria andEquipmentIdGreaterThan(Integer value) {
            addCriterion("EQUIPMENT_ID >", value, "equipmentId");
            return this;
        }

        public Criteria andEquipmentIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("EQUIPMENT_ID >=", value, "equipmentId");
            return this;
        }

        public Criteria andEquipmentIdLessThan(Integer value) {
            addCriterion("EQUIPMENT_ID <", value, "equipmentId");
            return this;
        }

        public Criteria andEquipmentIdLessThanOrEqualTo(Integer value) {
            addCriterion("EQUIPMENT_ID <=", value, "equipmentId");
            return this;
        }

        public Criteria andEquipmentIdIn(List values) {
            addCriterion("EQUIPMENT_ID in", values, "equipmentId");
            return this;
        }

        public Criteria andEquipmentIdNotIn(List values) {
            addCriterion("EQUIPMENT_ID not in", values, "equipmentId");
            return this;
        }

        public Criteria andEquipmentIdBetween(Integer value1, Integer value2) {
            addCriterion("EQUIPMENT_ID between", value1, value2, "equipmentId");
            return this;
        }

        public Criteria andEquipmentIdNotBetween(Integer value1, Integer value2) {
            addCriterion("EQUIPMENT_ID not between", value1, value2, "equipmentId");
            return this;
        }

        public Criteria andTicketIdIsNull() {
            addCriterion("TICKET_ID is null");
            return this;
        }

        public Criteria andTicketIdIsNotNull() {
            addCriterion("TICKET_ID is not null");
            return this;
        }

        public Criteria andTicketIdEqualTo(Integer value) {
            addCriterion("TICKET_ID =", value, "ticketId");
            return this;
        }

        public Criteria andTicketIdNotEqualTo(Integer value) {
            addCriterion("TICKET_ID <>", value, "ticketId");
            return this;
        }

        public Criteria andTicketIdGreaterThan(Integer value) {
            addCriterion("TICKET_ID >", value, "ticketId");
            return this;
        }

        public Criteria andTicketIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("TICKET_ID >=", value, "ticketId");
            return this;
        }

        public Criteria andTicketIdLessThan(Integer value) {
            addCriterion("TICKET_ID <", value, "ticketId");
            return this;
        }

        public Criteria andTicketIdLessThanOrEqualTo(Integer value) {
            addCriterion("TICKET_ID <=", value, "ticketId");
            return this;
        }

        public Criteria andTicketIdIn(List values) {
            addCriterion("TICKET_ID in", values, "ticketId");
            return this;
        }

        public Criteria andTicketIdNotIn(List values) {
            addCriterion("TICKET_ID not in", values, "ticketId");
            return this;
        }

        public Criteria andTicketIdBetween(Integer value1, Integer value2) {
            addCriterion("TICKET_ID between", value1, value2, "ticketId");
            return this;
        }

        public Criteria andTicketIdNotBetween(Integer value1, Integer value2) {
            addCriterion("TICKET_ID not between", value1, value2, "ticketId");
            return this;
        }

        public Criteria andUserIdIsNull() {
            addCriterion("USER_ID is null");
            return this;
        }

        public Criteria andUserIdIsNotNull() {
            addCriterion("USER_ID is not null");
            return this;
        }

        public Criteria andUserIdEqualTo(String value) {
            addCriterion("USER_ID =", value, "userId");
            return this;
        }

        public Criteria andUserIdNotEqualTo(String value) {
            addCriterion("USER_ID <>", value, "userId");
            return this;
        }

        public Criteria andUserIdGreaterThan(String value) {
            addCriterion("USER_ID >", value, "userId");
            return this;
        }

        public Criteria andUserIdGreaterThanOrEqualTo(String value) {
            addCriterion("USER_ID >=", value, "userId");
            return this;
        }

        public Criteria andUserIdLessThan(String value) {
            addCriterion("USER_ID <", value, "userId");
            return this;
        }

        public Criteria andUserIdLessThanOrEqualTo(String value) {
            addCriterion("USER_ID <=", value, "userId");
            return this;
        }

        public Criteria andUserIdLike(String value) {
            addCriterion("USER_ID like", value, "userId");
            return this;
        }

        public Criteria andUserIdNotLike(String value) {
            addCriterion("USER_ID not like", value, "userId");
            return this;
        }

        public Criteria andUserIdIn(List values) {
            addCriterion("USER_ID in", values, "userId");
            return this;
        }

        public Criteria andUserIdNotIn(List values) {
            addCriterion("USER_ID not in", values, "userId");
            return this;
        }

        public Criteria andUserIdBetween(String value1, String value2) {
            addCriterion("USER_ID between", value1, value2, "userId");
            return this;
        }

        public Criteria andUserIdNotBetween(String value1, String value2) {
            addCriterion("USER_ID not between", value1, value2, "userId");
            return this;
        }

        public Criteria andReportedDateIsNull() {
            addCriterion("REPORTED_DATE is null");
            return this;
        }

        public Criteria andReportedDateIsNotNull() {
            addCriterion("REPORTED_DATE is not null");
            return this;
        }

        public Criteria andReportedDateEqualTo(Date value) {
            addCriterionForJDBCDate("REPORTED_DATE =", value, "reportedDate");
            return this;
        }

        public Criteria andReportedDateNotEqualTo(Date value) {
            addCriterionForJDBCDate("REPORTED_DATE <>", value, "reportedDate");
            return this;
        }

        public Criteria andReportedDateGreaterThan(Date value) {
            addCriterionForJDBCDate("REPORTED_DATE >", value, "reportedDate");
            return this;
        }

        public Criteria andReportedDateGreaterThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("REPORTED_DATE >=", value, "reportedDate");
            return this;
        }

        public Criteria andReportedDateLessThan(Date value) {
            addCriterionForJDBCDate("REPORTED_DATE <", value, "reportedDate");
            return this;
        }

        public Criteria andReportedDateLessThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("REPORTED_DATE <=", value, "reportedDate");
            return this;
        }

        public Criteria andReportedDateIn(List values) {
            addCriterionForJDBCDate("REPORTED_DATE in", values, "reportedDate");
            return this;
        }

        public Criteria andReportedDateNotIn(List values) {
            addCriterionForJDBCDate("REPORTED_DATE not in", values, "reportedDate");
            return this;
        }

        public Criteria andReportedDateBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("REPORTED_DATE between", value1, value2, "reportedDate");
            return this;
        }

        public Criteria andReportedDateNotBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("REPORTED_DATE not between", value1, value2, "reportedDate");
            return this;
        }

        public Criteria andDescriptionIsNull() {
            addCriterion("DESCRIPTION is null");
            return this;
        }

        public Criteria andDescriptionIsNotNull() {
            addCriterion("DESCRIPTION is not null");
            return this;
        }

        public Criteria andDescriptionEqualTo(String value) {
            addCriterion("DESCRIPTION =", value, "description");
            return this;
        }

        public Criteria andDescriptionNotEqualTo(String value) {
            addCriterion("DESCRIPTION <>", value, "description");
            return this;
        }

        public Criteria andDescriptionGreaterThan(String value) {
            addCriterion("DESCRIPTION >", value, "description");
            return this;
        }

        public Criteria andDescriptionGreaterThanOrEqualTo(String value) {
            addCriterion("DESCRIPTION >=", value, "description");
            return this;
        }

        public Criteria andDescriptionLessThan(String value) {
            addCriterion("DESCRIPTION <", value, "description");
            return this;
        }

        public Criteria andDescriptionLessThanOrEqualTo(String value) {
            addCriterion("DESCRIPTION <=", value, "description");
            return this;
        }

        public Criteria andDescriptionLike(String value) {
            addCriterion("DESCRIPTION like", value, "description");
            return this;
        }

        public Criteria andDescriptionNotLike(String value) {
            addCriterion("DESCRIPTION not like", value, "description");
            return this;
        }

        public Criteria andDescriptionIn(List values) {
            addCriterion("DESCRIPTION in", values, "description");
            return this;
        }

        public Criteria andDescriptionNotIn(List values) {
            addCriterion("DESCRIPTION not in", values, "description");
            return this;
        }

        public Criteria andDescriptionBetween(String value1, String value2) {
            addCriterion("DESCRIPTION between", value1, value2, "description");
            return this;
        }

        public Criteria andDescriptionNotBetween(String value1, String value2) {
            addCriterion("DESCRIPTION not between", value1, value2, "description");
            return this;
        }
    }
}