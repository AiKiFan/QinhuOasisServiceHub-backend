package com.qinhu.oasis.tourism.mapper;

import com.qinhu.oasis.tourism.entity.BizOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 业务订单数据访问层（MyBatis Mapper），对应 mapper/tourism/BizOrderMapper.xml
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public interface BizOrderMapper {

    /**
     * 插入订单记录，自动回填主键 ID
     *
     * @param order 订单实体
     * @return 受影响行数
     */
    int insert(BizOrder order);

    /**
     * 根据 ID 查询订单（排除软删除记录）
     *
     * @param id 订单 ID
     * @return 订单实体，不存在时返回 null
     */
    BizOrder selectById(@Param("id") Long id);

    /**
     * 更新订单状态
     *
     * @param id     订单 ID
     * @param status 目标状态（参见 OrderStatus）
     * @return 受影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 查询超时未入场的停车订单（定时任务用）
     * <p>条件：停车订单 + 待入场状态 + 入场时间已超过30分钟</p>
     *
     * @return 超时订单列表
     */
    List<BizOrder> selectTimeoutParkingOrders();

    /**
     * 更新订单取消信息（取消原因 + 取消方）
     *
     * @param id            订单ID
     * @param status        目标状态
     * @param cancelReason  取消原因
     * @param cancelledBy   取消方
     * @return 受影响行数
     */
    int updateCancelInfo(@Param("id") Long id,
                         @Param("status") Integer status,
                         @Param("cancelReason") String cancelReason,
                         @Param("cancelledBy") String cancelledBy);

    /**
     * 更新订单实付金额（停车结算时使用）
     *
     * @param id          订单ID
     * @param paidAmount  实付金额
     * @return 受影响行数
     */
    int updatePaidAmount(@Param("id") Long id, @Param("paidAmount") java.math.BigDecimal paidAmount);
}
