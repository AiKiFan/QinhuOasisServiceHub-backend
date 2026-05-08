package com.qinhu.oasis.interpreter.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.qinhu.oasis.common.constant.InterpreterStatus;
import com.qinhu.oasis.common.constant.OrderStatus;
import com.qinhu.oasis.common.constant.OrderType;
import com.qinhu.oasis.common.constant.UserRole;
import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.i18n.LocaleContextHolder;
import com.qinhu.oasis.common.result.PageResult;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.interpreter.dto.ApplyInterpreterReq;
import com.qinhu.oasis.interpreter.dto.BookInterpreterReq;
import com.qinhu.oasis.interpreter.dto.InterpreterOrderVO;
import com.qinhu.oasis.interpreter.dto.InterpreterVO;
import com.qinhu.oasis.interpreter.entity.InterpreterProfile;
import com.qinhu.oasis.interpreter.mapper.InterpreterOrderMapper;
import com.qinhu.oasis.interpreter.mapper.InterpreterProfileMapper;
import com.qinhu.oasis.interpreter.service.InterpreterService;
import com.qinhu.oasis.sys.mapper.SysUserMapper;
import com.qinhu.oasis.tourism.entity.BizOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 译员业务服务实现
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterpreterServiceImpl implements InterpreterService {

    @SuppressWarnings("deprecation")
    private static final Snowflake SNOWFLAKE = IdUtil.createSnowflake(1, 2);

    private final InterpreterProfileMapper profileMapper;
    private final InterpreterOrderMapper orderMapper;
    private final SysUserMapper sysUserMapper;
    private final I18nUtil i18nUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterpreterVO applyInterpreter(ApplyInterpreterReq req, Long userId) {
        if (profileMapper.selectByUserId(userId) != null) {
            throw new BizException(ResultCode.INTERPRETER_PROFILE_EXISTS,
                    i18nUtil.msg(ResultCode.INTERPRETER_PROFILE_EXISTS));
        }
        InterpreterProfile profile = new InterpreterProfile();
        profile.setUserId(userId);
        profile.setRealName(req.getRealName());
        profile.setStudentId(req.getStudentId());
        profile.setSchool(req.getSchool());
        profile.setEnglishLevel(req.getEnglishLevel());
        profile.setCertUrl(req.getCertUrl());
        profile.setCertNo(req.getCertNo());
        profile.setIntroduction(req.getIntroduction());
        profile.setIntroductionEn(req.getIntroductionEn());
        profile.setServiceTypes(req.getServiceTypes());
        profile.setHourlyRate(req.getHourlyRate());
        profile.setStatus(InterpreterStatus.PENDING);
        profileMapper.insert(profile);
        log.info("Interpreter application submitted: profileId={}, userId={}", profile.getId(), userId);
        return toVO(profileMapper.selectById(profile.getId()));
    }

    @Override
    public PageResult<InterpreterVO> listInterpreters(int page, int size) {
        int offset = (page - 1) * size;
        long total = profileMapper.countPage();
        List<InterpreterVO> list = profileMapper.selectPage(offset, size);
        list.forEach(vo -> vo.setDisplayIntroduction(resolveIntroduction(vo)));
        return PageResult.of(total, list);
    }

    @Override
    public InterpreterVO getInterpreterDetail(Long profileId) {
        InterpreterProfile profile = profileMapper.selectById(profileId);
        if (profile == null || profile.getStatus() != InterpreterStatus.APPROVED) {
            throw new BizException(ResultCode.NOT_FOUND, i18nUtil.msg(ResultCode.NOT_FOUND));
        }
        InterpreterVO vo = toVO(profile);
        // 关联 sys_user 获取头像和昵称
        var user = sysUserMapper.selectById(profile.getUserId());
        if (user != null) {
            vo.setAvatar(user.getAvatar());
            vo.setNickname(user.getNickname());
        }
        vo.setDisplayIntroduction(resolveIntroduction(vo));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterpreterOrderVO bookInterpreter(BookInterpreterReq req, Long userId) {
        InterpreterProfile profile = profileMapper.selectById(req.getProfileId());
        if (profile == null) {
            throw new BizException(ResultCode.NOT_FOUND, i18nUtil.msg(ResultCode.NOT_FOUND));
        }
        if (profile.getStatus() != InterpreterStatus.APPROVED) {
            throw new BizException(ResultCode.INTERPRETER_NOT_APPROVED,
                    i18nUtil.msg(ResultCode.INTERPRETER_NOT_APPROVED));
        }
        // 防止自预约：下单用户不能是译员本人
        if (userId.equals(profile.getUserId())) {
            throw new BizException(ResultCode.SELF_BOOKING_NOT_ALLOWED,
                    i18nUtil.msg(ResultCode.SELF_BOOKING_NOT_ALLOWED));
        }

        // 验证预约时间至少提前一天
        java.time.LocalDate tomorrow = java.time.LocalDate.now().plusDays(1);
        java.time.LocalDate startDay = req.getStartTime().toLocalDate();
        if (startDay.isBefore(tomorrow)) {
            throw new BizException(ResultCode.BOOKING_TIME_INVALID,
                    i18nUtil.msg(ResultCode.BOOKING_TIME_INVALID));
        }

        long hours = ChronoUnit.HOURS.between(req.getStartTime(), req.getEndTime());
        if (hours <= 0) {
            throw new BizException(ResultCode.PARAM_ERROR, i18nUtil.msg(ResultCode.PARAM_ERROR));
        }
        BigDecimal totalAmount = profile.getHourlyRate().multiply(BigDecimal.valueOf(hours));

        BizOrder order = new BizOrder();
        order.setOrderNo(String.valueOf(SNOWFLAKE.nextId()));
        order.setOrderType(OrderType.INTERPRETER);
        order.setUserId(userId);
        order.setInterpreterId(profile.getUserId());
        order.setServiceType(req.getServiceType());
        order.setGroupSize(req.getGroupSize() != null ? req.getGroupSize() : 1);
        order.setStartTime(req.getStartTime());
        order.setEndTime(req.getEndTime());
        order.setTotalAmount(totalAmount);
        order.setPaidAmount(BigDecimal.ZERO);
        order.setStatus(OrderStatus.PENDING);
        order.setRemark(req.getRemark());
        order.setPhone(req.getPhone());
        orderMapper.insert(order);
        log.info("Interpreter order created: orderId={}, userId={}, interpreterId={}", order.getId(), userId, profile.getUserId());

        List<InterpreterOrderVO> result = orderMapper.selectByUserId(userId, 0, 1);
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptOrder(Long orderId, Long userId) {
        BizOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ResultCode.ORDER_NOT_EXIST, i18nUtil.msg(ResultCode.ORDER_NOT_EXIST));
        }
        if (!userId.equals(order.getInterpreterId())) {
            throw new BizException(ResultCode.FORBIDDEN, i18nUtil.msg(ResultCode.FORBIDDEN));
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BizException(ResultCode.ORDER_STATUS_INVALID, i18nUtil.msg(ResultCode.ORDER_STATUS_INVALID));
        }
        orderMapper.updateStatus(orderId, OrderStatus.ACCEPTED);
        log.info("Interpreter order accepted: orderId={}, interpreterId={}", orderId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId, String reason) {
        BizOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ResultCode.ORDER_NOT_EXIST, i18nUtil.msg(ResultCode.ORDER_NOT_EXIST));
        }
        boolean isUser = userId.equals(order.getUserId());
        boolean isInterpreter = userId.equals(order.getInterpreterId());
        if (!isUser && !isInterpreter) {
            throw new BizException(ResultCode.FORBIDDEN, i18nUtil.msg(ResultCode.FORBIDDEN));
        }
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.ACCEPTED) {
            throw new BizException(ResultCode.ORDER_STATUS_INVALID, i18nUtil.msg(ResultCode.ORDER_STATUS_INVALID));
        }
        // 记录取消方
        String cancelledBy = isUser ? "user" : "interpreter";
        order.setCancelledBy(cancelledBy);
        order.setCancelReason(reason);
        order.setStatus(OrderStatus.CANCELLED);
        orderMapper.updateById(order);
        log.info("Interpreter order cancelled: orderId={}, operatorId={}, cancelledBy={}, reason={}", orderId, userId, cancelledBy, reason);
    }

    @Override
    public PageResult<InterpreterOrderVO> listMyOrders(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        long total = orderMapper.countByUserId(userId);
        List<InterpreterOrderVO> list = orderMapper.selectByUserId(userId, offset, size);
        return PageResult.of(total, list);
    }

    @Override
    public PageResult<InterpreterOrderVO> listReceivedOrders(Long interpreterId, Integer status, int page, int size) {
        int offset = (page - 1) * size;
        List<InterpreterOrderVO> list = orderMapper.selectByInterpreterId(interpreterId, offset, size);
        if (status != null) {
            list = list.stream().filter(o -> o.getStatus().equals(status)).collect(Collectors.toList());
        }
        long total = status == null
                ? orderMapper.countByInterpreterId(interpreterId)
                : list.size();
        return PageResult.of(total, list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectOrder(Long orderId, Long userId, String reason) {
        BizOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ResultCode.ORDER_NOT_EXIST, i18nUtil.msg(ResultCode.ORDER_NOT_EXIST));
        }
        if (!userId.equals(order.getInterpreterId())) {
            throw new BizException(ResultCode.FORBIDDEN, i18nUtil.msg(ResultCode.FORBIDDEN));
        }
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.ACCEPTED) {
            throw new BizException(ResultCode.ORDER_STATUS_INVALID, i18nUtil.msg(ResultCode.ORDER_STATUS_INVALID));
        }
        order.setCancelledBy("interpreter");
        order.setCancelReason(reason);
        order.setStatus(OrderStatus.CANCELLED);
        orderMapper.updateById(order);
        log.info("Interpreter order rejected: orderId={}, interpreterId={}, reason={}", orderId, userId, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long orderId, Long userId) {
        BizOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ResultCode.ORDER_NOT_EXIST, i18nUtil.msg(ResultCode.ORDER_NOT_EXIST));
        }
        if (!userId.equals(order.getInterpreterId())) {
            throw new BizException(ResultCode.FORBIDDEN, i18nUtil.msg(ResultCode.FORBIDDEN));
        }
        if (order.getStatus() != OrderStatus.ACCEPTED && order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new BizException(ResultCode.ORDER_STATUS_INVALID, i18nUtil.msg(ResultCode.ORDER_STATUS_INVALID));
        }
        orderMapper.updateStatus(orderId, OrderStatus.COMPLETED);
        log.info("Interpreter order completed: orderId={}, interpreterId={}", orderId, userId);
    }

    @Override
    public PageResult<InterpreterVO> adminListProfiles(Integer status, int page, int size) {
        int offset = (page - 1) * size;
        long total = profileMapper.countAdminPage(status);
        List<InterpreterVO> list = profileMapper.selectAdminPage(status, offset, size);
        return PageResult.of(total, list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminReviewProfile(Long profileId, boolean approve, String rejectReason, Long adminId) {
        InterpreterProfile profile = profileMapper.selectById(profileId);
        if (profile == null) {
            throw new BizException(ResultCode.NOT_FOUND, i18nUtil.msg(ResultCode.NOT_FOUND));
        }
        if (profile.getStatus() != InterpreterStatus.PENDING) {
            throw new BizException(ResultCode.ORDER_STATUS_INVALID, i18nUtil.msg(ResultCode.ORDER_STATUS_INVALID));
        }
        int newStatus = approve ? InterpreterStatus.APPROVED : InterpreterStatus.REJECTED;
        profileMapper.updateStatus(profileId, newStatus, approve ? null : rejectReason);
        if (approve) {
            sysUserMapper.updateRole(profile.getUserId(), UserRole.STUDENT);
            log.info("Interpreter approved: profileId={}, userId={}, adminId={}", profileId, profile.getUserId(), adminId);
        } else {
            log.info("Interpreter rejected: profileId={}, userId={}, reason={}, adminId={}",
                    profileId, profile.getUserId(), rejectReason, adminId);
        }
    }

    // ───────────── 私有辅助方法 ─────────────

    private boolean isEnglish() {
        return Locale.ENGLISH.getLanguage().equals(LocaleContextHolder.get().getLanguage());
    }

    private String resolveIntroduction(InterpreterVO vo) {
        String en = vo.getIntroductionEn();
        return (isEnglish() && en != null && !en.isBlank()) ? en : vo.getIntroduction();
    }

    private InterpreterVO toVO(InterpreterProfile profile) {
        if (profile == null) return null;
        InterpreterVO vo = new InterpreterVO();
        vo.setId(profile.getId());
        vo.setUserId(profile.getUserId());
        vo.setRealName(profile.getRealName());
        vo.setStudentId(profile.getStudentId());
        vo.setSchool(profile.getSchool());
        vo.setEnglishLevel(profile.getEnglishLevel());
        vo.setCertUrl(profile.getCertUrl());
        vo.setCertNo(profile.getCertNo());
        vo.setIntroduction(profile.getIntroduction());
        vo.setIntroductionEn(profile.getIntroductionEn());
        vo.setServiceTypes(profile.getServiceTypes());
        vo.setHourlyRate(profile.getHourlyRate());
        vo.setRating(profile.getRating());
        vo.setTotalOrders(profile.getTotalOrders());
        vo.setStatus(profile.getStatus());
        vo.setRejectReason(profile.getRejectReason());
        vo.setCreateTime(profile.getCreateTime());
        return vo;
    }

    @Override
    public List<InterpreterVO> getInterpretersByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<InterpreterProfile> profiles = profileMapper.selectByIds(ids);
        return profiles.stream().map(profile -> {
            InterpreterVO vo = toVO(profile);
            var user = sysUserMapper.selectById(profile.getUserId());
            if (user != null) {
                vo.setAvatar(user.getAvatar());
                vo.setNickname(user.getNickname());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public InterpreterVO getMyProfile(Long userId) {
        InterpreterProfile profile = profileMapper.selectByUserId(userId);
        return toVO(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterpreterVO updateMyApplication(ApplyInterpreterReq req, Long userId) {
        InterpreterProfile profile = profileMapper.selectByUserId(userId);
        if (profile == null) {
            throw new BizException(ResultCode.NOT_FOUND, i18nUtil.msg(ResultCode.NOT_FOUND));
        }
        if (profile.getStatus() != InterpreterStatus.PENDING && profile.getStatus() != InterpreterStatus.REJECTED) {
            throw new BizException(ResultCode.ORDER_STATUS_INVALID, i18nUtil.msg(ResultCode.ORDER_STATUS_INVALID));
        }
        // 被拒绝后重新提交：将状态重置为待审核
        if (profile.getStatus() == InterpreterStatus.REJECTED) {
            profile.setStatus(InterpreterStatus.PENDING);
        }
        profile.setRealName(req.getRealName());
        profile.setStudentId(req.getStudentId());
        profile.setSchool(req.getSchool());
        profile.setEnglishLevel(req.getEnglishLevel());
        profile.setCertUrl(req.getCertUrl());
        profile.setCertNo(req.getCertNo());
        profile.setIntroduction(req.getIntroduction());
        profile.setIntroductionEn(req.getIntroductionEn());
        profile.setServiceTypes(req.getServiceTypes());
        profile.setHourlyRate(req.getHourlyRate());
        profileMapper.updateById(profile);
        log.info("Interpreter application updated: profileId={}, userId={}", profile.getId(), userId);
        return toVO(profileMapper.selectById(profile.getId()));
    }

    @Override
    public InterpreterOrderVO getOrderDetail(Long orderId, Long userId) {
        BizOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ResultCode.ORDER_NOT_EXIST, i18nUtil.msg(ResultCode.ORDER_NOT_EXIST));
        }
        // 验证权限：必须是订单的游客或译员
        boolean isUser = userId.equals(order.getUserId());
        boolean isInterpreter = userId.equals(order.getInterpreterId());
        if (!isUser && !isInterpreter) {
            throw new BizException(ResultCode.FORBIDDEN, i18nUtil.msg(ResultCode.FORBIDDEN));
        }
        // 使用 selectByUserId 或 selectByInterpreterId 获取完整 VO（含关联信息）
        List<InterpreterOrderVO> list;
        if (isUser) {
            list = orderMapper.selectByUserId(userId, 0, 100);
        } else {
            list = orderMapper.selectByInterpreterId(userId, 0, 100);
        }
        return list.stream()
                .filter(vo -> vo.getId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new BizException(ResultCode.NOT_FOUND, i18nUtil.msg(ResultCode.NOT_FOUND)));
    }
}
