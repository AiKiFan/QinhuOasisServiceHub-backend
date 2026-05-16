package com.qinhu.oasis.tourism.service.impl;

import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.result.PageResult;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.tourism.dto.ScenicSpotDetailVO;
import com.qinhu.oasis.tourism.dto.ScenicSpotListVO;
import com.qinhu.oasis.tourism.entity.ScenicSpot;
import com.qinhu.oasis.tourism.mapper.ScenicSpotMapper;
import com.qinhu.oasis.tourism.service.ScenicSpotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * 景点服务实现类
 *
 * @author AiKiFan
 * @date 2026-05-06
 */
@Service
@RequiredArgsConstructor
public class ScenicSpotServiceImpl implements ScenicSpotService {

    private final ScenicSpotMapper scenicSpotMapper;
    private final I18nUtil i18nUtil;

    @Override
    public List<ScenicSpotListVO> getScenicSpotList(int page, int size) {
        int offset = (page - 1) * size;
        List<ScenicSpot> spots = scenicSpotMapper.selectPage(offset, size);
        return spots.stream().map(this::toListVO).collect(Collectors.toList());
    }

    @Override
    public ScenicSpotDetailVO getScenicSpotDetail(Long id) {
        ScenicSpot spot = scenicSpotMapper.selectById(id);
        if (spot == null) {
            throw new RuntimeException("景点不存在");
        }
        return toDetailVO(spot);
    }

    @Override
    public List<ScenicSpotListVO> getScenicSpotsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<ScenicSpot> spots = scenicSpotMapper.selectByIds(ids);
        return spots.stream().map(this::toListVO).collect(Collectors.toList());
    }

    // ───────────── 管理员接口 ─────────────

    @Override
    public PageResult<ScenicSpot> adminList(String keyword, int page, int size) {
        int offset = (page - 1) * size;
        long total = scenicSpotMapper.countAdminPage(keyword);
        List<ScenicSpot> list = scenicSpotMapper.selectAdminPage(keyword, offset, size);
        return PageResult.of(total, list);
    }

    @Override
    public ScenicSpot adminCreate(ScenicSpot scenicSpot) {
        if (scenicSpot.getStatus() == null) {
            scenicSpot.setStatus(1);
        }
        if (scenicSpot.getRating() == null) {
            scenicSpot.setRating(java.math.BigDecimal.valueOf(5.00));
        }
        if (scenicSpot.getReviewCount() == null) {
            scenicSpot.setReviewCount(0);
        }
        if (scenicSpot.getSortScore() == null) {
            scenicSpot.setSortScore(0.0);
        }
        scenicSpotMapper.insert(scenicSpot);
        return scenicSpot;
    }

    @Override
    public ScenicSpot adminUpdate(ScenicSpot incoming) {
        if (incoming.getId() == null) {
            throw new BizException(ResultCode.PARAM_ERROR, i18nUtil.msg(ResultCode.PARAM_ERROR));
        }
        ScenicSpot existing = scenicSpotMapper.selectById(incoming.getId());
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, i18nUtil.msg(ResultCode.NOT_FOUND));
        }
        incoming.setRating(existing.getRating());
        incoming.setReviewCount(existing.getReviewCount());
        incoming.setSortScore(existing.getSortScore());
        scenicSpotMapper.updateById(incoming);
        return scenicSpotMapper.selectById(incoming.getId());
    }

    @Override
    public void adminDelete(Long id) {
        ScenicSpot existing = scenicSpotMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, i18nUtil.msg(ResultCode.NOT_FOUND));
        }
        scenicSpotMapper.deleteById(id);
    }

    @Override
    public void adminUpdateStatus(Long id, Integer status) {
        if (id == null || status == null) {
            throw new BizException(ResultCode.PARAM_ERROR, i18nUtil.msg(ResultCode.PARAM_ERROR));
        }
        scenicSpotMapper.updateStatus(id, status);
    }

    // ───────────── 私有转换方法 ─────────────

    private boolean isEnglish() {
        return Locale.ENGLISH.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage());
    }

    private String resolveDisplayName(String name, String nameEn) {
        return (isEnglish() && nameEn != null && !nameEn.isBlank()) ? nameEn : name;
    }

    private String resolveDisplayDescription(String desc, String descEn) {
        return (isEnglish() && descEn != null && !descEn.isBlank()) ? descEn : desc;
    }

    private ScenicSpotListVO toListVO(ScenicSpot s) {
        ScenicSpotListVO vo = new ScenicSpotListVO();
        vo.setId(s.getId());
        vo.setDisplayName(resolveDisplayName(s.getName(), s.getNameEn()));
        vo.setCoverImg(s.getCoverImg());
        vo.setRating(s.getRating());
        vo.setReviewCount(s.getReviewCount());
        vo.setOpeningHours(s.getOpeningHours());
        vo.setTicketPrice(s.getTicketPrice());
        vo.setTags(s.getTags());
        return vo;
    }

    private ScenicSpotDetailVO toDetailVO(ScenicSpot s) {
        ScenicSpotDetailVO vo = new ScenicSpotDetailVO();
        vo.setId(s.getId());
        vo.setDisplayName(resolveDisplayName(s.getName(), s.getNameEn()));
        vo.setDisplayDescription(resolveDisplayDescription(s.getDescription(), s.getDescriptionEn()));
        vo.setCoverImg(s.getCoverImg());
        vo.setRating(s.getRating());
        vo.setReviewCount(s.getReviewCount());
        vo.setOpeningHours(s.getOpeningHours());
        vo.setTicketPrice(s.getTicketPrice());
        vo.setTags(s.getTags());
        vo.setAddress(s.getAddress());
        vo.setLat(s.getLat());
        vo.setLng(s.getLng());
        vo.setImages(s.getImages());
        return vo;
    }
}