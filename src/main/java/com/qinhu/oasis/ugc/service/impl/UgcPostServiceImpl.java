package com.qinhu.oasis.ugc.service.impl;

import cn.hutool.json.JSONUtil;
import com.qinhu.oasis.common.constant.LikeTargetType;
import com.qinhu.oasis.common.constant.PostStatus;
import com.qinhu.oasis.common.constant.PostType;
import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.i18n.LocaleContextHolder;
import com.qinhu.oasis.common.result.PageResult;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.ugc.dto.CreatePostReq;
import com.qinhu.oasis.ugc.dto.PostDetailVO;
import com.qinhu.oasis.ugc.dto.PostListVO;
import com.qinhu.oasis.ugc.entity.UgcLike;
import com.qinhu.oasis.ugc.entity.UgcPost;
import com.qinhu.oasis.ugc.mapper.UgcLikeMapper;
import com.qinhu.oasis.ugc.mapper.UgcPostMapper;
import com.qinhu.oasis.ugc.service.UgcPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * 攻略/动态业务服务实现
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UgcPostServiceImpl implements UgcPostService {

    private final UgcPostMapper ugcPostMapper;
    private final UgcLikeMapper ugcLikeMapper;
    private final I18nUtil i18nUtil;

    @Override
    public PageResult<PostListVO> listPosts(Integer postType, int page, int size) {
        int offset = (page - 1) * size;
        long total = ugcPostMapper.countPage(postType);
        List<PostListVO> list = ugcPostMapper.selectPage(postType, offset, size);
        list.forEach(vo -> vo.setDisplayTitle(resolveTitle(vo)));
        return PageResult.of(total, list);
    }

    @Override
    public PostDetailVO getPostDetail(Long postId) {
        ugcPostMapper.incrementViewCount(postId);
        PostDetailVO detail = ugcPostMapper.selectDetailById(postId);
        if (detail == null) {
            throw new BizException(ResultCode.NOT_FOUND, i18nUtil.msg(ResultCode.NOT_FOUND));
        }
        detail.setDisplayTitle(resolveTitle(detail));
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostDetailVO createPost(CreatePostReq req, Long userId) {
        UgcPost post = new UgcPost();
        post.setUserId(userId);
        post.setPostType(req.getPostType());
        post.setTitle(req.getTitle());
        post.setTitleEn(req.getTitleEn());
        post.setSummary(req.getSummary());
        post.setContent(req.getContent());
        post.setCoverImg(req.getCoverImg());
        post.setImages(req.getImages() != null ? JSONUtil.toJsonStr(req.getImages()) : null);
        // 游客动态直接发布，游客攻略进入审核
        post.setStatus(req.getPostType() == PostType.DYNAMIC ? PostStatus.PUBLISHED : PostStatus.REVIEWING);

        ugcPostMapper.insert(post);
        log.info("New post created: id={}, type={}, userId={}", post.getId(), post.getPostType(), userId);

        PostDetailVO detail = ugcPostMapper.selectDetailById(post.getId());
        if (detail != null) {
            detail.setDisplayTitle(resolveTitle(detail));
        }
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long postId, Long userId) {
        UgcLike existing = ugcLikeMapper.selectByUserAndTarget(userId, postId, LikeTargetType.POST);
        if (existing != null) {
            ugcLikeMapper.delete(userId, postId, LikeTargetType.POST);
            ugcPostMapper.decrementLikeCount(postId);
            return false;
        } else {
            UgcLike like = new UgcLike();
            like.setUserId(userId);
            like.setTargetId(postId);
            like.setTargetType(LikeTargetType.POST);
            ugcLikeMapper.insert(like);
            ugcPostMapper.incrementLikeCount(postId);
            return true;
        }
    }

    // ───────────── 私有辅助方法 ─────────────

    private boolean isEnglish() {
        return Locale.ENGLISH.getLanguage().equals(LocaleContextHolder.get().getLanguage());
    }

    private String resolveTitle(PostListVO vo) {
        String titleEn = vo.getTitleEn();
        return (isEnglish() && titleEn != null && !titleEn.isBlank()) ? titleEn : vo.getTitle();
    }
}
