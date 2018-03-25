package com.piggymetrics.account.repository;

import com.piggymetrics.account.domain.Account;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Account 为mongodb的json文档document， String为Id / key
 * @ClassName: AccountRepository 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author lijinyang 
 * @date 2018年3月6日 下午11:34:33
 */
@Repository
public interface AccountRepository extends CrudRepository<Account, String> {

	Account findByName(String name);

}
