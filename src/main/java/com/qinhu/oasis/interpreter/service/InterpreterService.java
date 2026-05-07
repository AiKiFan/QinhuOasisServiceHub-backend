package com.qinhu.oasis.interpreter.service;

import com.qinhu.oasis.common.result.PageResult;
import com.qinhu.oasis.interpreter.dto.ApplyInterpreterReq;
import com.qinhu.oasis.interpreter.dto.BookInterpreterReq;
import com.qinhu.oasis.interpreter.dto.InterpreterOrderVO;
import com.qinhu.oasis.interpreter.dto.InterpreterVO;

import java.util.List;

/**
 * 译员业务服务接口
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public interface InterpreterService {

    /**
     * 申请成为译员（提交档案，状态初始为待审核）
     *
     * @param req    申请参数
     * @param userId 申请人用户 ID
     * @return 新建的译员档案 VO
     */
    InterpreterVO applyInterpreter(ApplyInterpreterReq req, Long userId);

    /**
     * 分页查询已通过审核的译员列表（对外展示）
     *
     * @param page 页码（从 1 开始）
     * @param size 每页条数
     * @return 分页结果
     */
    PageResult<InterpreterVO> listInterpreters(int page, int size);

    /**
     * 查询译员档案详情（按档案 ID）
     *
     * @param profileId 档案 ID
     * @return 译员 VO
     */
    InterpreterVO getInterpreterDetail(Long profileId);

    /**
     * 游客预约译员服务，创建翻译订单
     *
     * @param req    预约参数
     * @param userId 游客用户 ID
     * @return 新建订单 VO
     */
    InterpreterOrderVO bookInterpreter(BookInterpreterReq req, Long userId);

    /**
     * 译员接单（将自己设为接单人，状态改为 ACCEPTED）
     *
     * @param orderId 订单 ID
     * @param userId  接单译员的用户 ID
     */
    void acceptOrder(Long orderId, Long userId);

    /**
     * 取消翻译订单（游客或译员均可操作）
     *
     * @param orderId 订单 ID
     * @param userId  操作人用户 ID
     */
    void cancelOrder(Long orderId, Long userId);

    /**
     * 分页查询当前用户的翻译订单列表
     *
     * @param userId 用户 ID
     * @param page   页码
     * @param size   每页条数
     * @return 分页结果
     */
    PageResult<InterpreterOrderVO> listMyOrders(Long userId, int page, int size);

    /**
     * 管理员分页查询译员档案（可按状态筛选）
     *
     * @param status 状态筛选（null=全部）
     * @param page   页码
     * @param size   每页条数
     * @return 分页结果
     */
    PageResult<InterpreterVO> adminListProfiles(Integer status, int page, int size);

    /**
     * 管理员审核译员申请
     *
     * @param profileId    档案 ID
     * @param approve      true=通过 false=拒绝
     * @param rejectReason 拒绝原因（approve=true 时可为 null）
     * @param adminId      操作管理员 ID
     */
    void adminReviewProfile(Long profileId, boolean approve, String rejectReason, Long adminId);

    /**
     * 根据ID列表获取译员列表（用于收藏功能）
     *
     * @param ids 译员档案ID列表
     * @return 译员列表
     */
    List<InterpreterVO> getInterpretersByIds(List<Long> ids);

    /**
     * 获取当前用户的译员申请档案（用于查看申请状态）
     *
     * @param userId 用户 ID
     * @return 译员档案 VO，不存在时返回 null
     */
    InterpreterVO getMyProfile(Long userId);

    /**
     * 更新译员申请信息（仅待审核状态可修改）
     *
     * @param req    申请参数
     * @param userId 用户 ID
     * @return 更新后的译员档案 VO
     */
    InterpreterVO updateMyApplication(ApplyInterpreterReq req, Long userId);
}
