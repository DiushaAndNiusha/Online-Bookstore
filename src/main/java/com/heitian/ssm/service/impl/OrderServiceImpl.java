package com.heitian.ssm.service.impl;

import com.heitian.ssm.dao.AmountDao;
import com.heitian.ssm.dao.OrderDao;
import com.heitian.ssm.dao.UserDao;
import com.heitian.ssm.model.Amount;
import com.heitian.ssm.model.Order;
import com.heitian.ssm.model.User;
import com.heitian.ssm.service.OrderService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class,timeout = 1,isolation= Isolation.DEFAULT)
public class OrderServiceImpl implements OrderService{

    @Resource
    private OrderDao orderDao;
    @Resource
    private UserDao userDao;
    @Resource
    private AmountDao amountDao;

    @Transactional(propagation= Propagation.REQUIRED,rollbackForClassName="Exception")
    @CacheEvict(value = {"getAllOrder"}, allEntries = true)
    public void addOrder(Order order) {
        synchronized (Order.class){
            Amount amount = amountDao.getAmountByUId(order.getOuid());
            amount.setAmount(amount.getAmount() - order.getO_amount());
            this.amountDao.updateAmount(amount);
            this.orderDao.addOrder(order);
        }
    }

    @Transactional(propagation=Propagation.REQUIRED,rollbackForClassName="Exception")
    @Cacheable("getAllOrder")
    public List<Order> getAllOrder() {
        return orderDao.selectAllOrder();
    }

    @Override
    public void updateOrderStatus(String orderCode, int status) {
        orderDao.updateOrderStatus(orderCode, status);
    }

    @Override
    public List<Order> getOrderByName(String name) {
        User user = userDao.selectUserByName(name);
        return orderDao.getOrderByUId(user.getUid());
    }

    @Override
    public Order getOrderByCode(String ocode) {
        return orderDao.getOrderByCode(ocode);
    }

    @Override
    public List<Order> getOrderByStatus(String name, int status) {
        User user = userDao.selectUserByName(name);
        return orderDao.getOrderByStatus(user.getUid(),status);
    }

    @Override
    public List<Order> getOrderByPId(long opid) {
        return orderDao.getOrderByPId(opid);
    }
}
