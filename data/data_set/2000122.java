package com.iclotho.eshop.domain.dao;

import com.iclotho.eshop.domain.po.Address;
import com.iclotho.eshop.domain.po.AddressCriteria;
import java.util.List;

public interface AddressDAO {

    /**
     * This method was generated by Apache iBATIS ibator.
     * This method corresponds to the database table t_address
     *
     * @ibatorgenerated Fri Feb 13 10:31:27 VET 2009
     */
    int countByExample(AddressCriteria example);

    /**
     * This method was generated by Apache iBATIS ibator.
     * This method corresponds to the database table t_address
     *
     * @ibatorgenerated Fri Feb 13 10:31:27 VET 2009
     */
    int deleteByExample(AddressCriteria example);

    /**
     * This method was generated by Apache iBATIS ibator.
     * This method corresponds to the database table t_address
     *
     * @ibatorgenerated Fri Feb 13 10:31:27 VET 2009
     */
    int deleteByPrimaryKey(Long addressId);

    /**
     * This method was generated by Apache iBATIS ibator.
     * This method corresponds to the database table t_address
     *
     * @ibatorgenerated Fri Feb 13 10:31:27 VET 2009
     */
    void insert(Address record);

    /**
     * This method was generated by Apache iBATIS ibator.
     * This method corresponds to the database table t_address
     *
     * @ibatorgenerated Fri Feb 13 10:31:27 VET 2009
     */
    void insertSelective(Address record);

    /**
     * This method was generated by Apache iBATIS ibator.
     * This method corresponds to the database table t_address
     *
     * @ibatorgenerated Fri Feb 13 10:31:27 VET 2009
     */
    List selectByExample(AddressCriteria example);

    /**
     * This method was generated by Apache iBATIS ibator.
     * This method corresponds to the database table t_address
     *
     * @ibatorgenerated Fri Feb 13 10:31:27 VET 2009
     */
    Address selectByPrimaryKey(Long addressId);

    /**
     * This method was generated by Apache iBATIS ibator.
     * This method corresponds to the database table t_address
     *
     * @ibatorgenerated Fri Feb 13 10:31:27 VET 2009
     */
    int updateByExampleSelective(Address record, AddressCriteria example);

    /**
     * This method was generated by Apache iBATIS ibator.
     * This method corresponds to the database table t_address
     *
     * @ibatorgenerated Fri Feb 13 10:31:27 VET 2009
     */
    int updateByExample(Address record, AddressCriteria example);

    /**
     * This method was generated by Apache iBATIS ibator.
     * This method corresponds to the database table t_address
     *
     * @ibatorgenerated Fri Feb 13 10:31:27 VET 2009
     */
    int updateByPrimaryKeySelective(Address record);

    /**
     * This method was generated by Apache iBATIS ibator.
     * This method corresponds to the database table t_address
     *
     * @ibatorgenerated Fri Feb 13 10:31:27 VET 2009
     */
    int updateByPrimaryKey(Address record);
}
