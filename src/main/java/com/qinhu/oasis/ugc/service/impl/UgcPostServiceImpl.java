package com.qinhu.oasis.ugc.service.impl;

import cn.hutool.json.JSONUtil;
import com.qinhu.oasis.common.constant.LikeTargetType;
import com.qinhu.oasis.common.constant.PostStatus;
import com.qinhu.oasis.common.constant.PostType;
import com.qinhu.oasis.common.constant.SystemConfigKeys;
import com.qinhu.oasis.common.constant.UserRole;
import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.i18n.LocaleContextHolder;
import com.qinhu.oasis.common.result.PageResult;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.common.security.LoginUser;
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
import org.springframework.data.redis.core.StringRedisTemplate;
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

    /** 私密状态：公开 */
    private static final int PUBLIC = 0;
    /** 私密状态：仅作者可见 */
    private static final int PRIVATE = 1;

    private final UgcPostMapper ugcPostMapper;
    private final UgcLikeMapper ugcLikeMapper;
    private final I18nUtil i18nUtil;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public PageResult<PostListVO> listPosts(Integer postType, int page, int size) {
        int offset = (page - 1) * size;
        long total = ugcPostMapper.countPage(postType);
        List<PostListVO> list = ugcPostMapper.selectPage(postType, offset, size);
        list.forEach(this::fillDisplayTitle);
        return PageResult.of(total, list);
    }

    @Override
    public PostDetailVO getPostDetail(Long postId) {
        PostDetailVO detail = ugcPostMapper.selectDetailById(postId);
        ensureCanView(detail);
        ugcPostMapper.incrementViewCount(postId);
        if (detail.getViewCount() != null) {
            detail.setViewCount(detail.getViewCount() + 1);
        }
        fillDisplayTitle(detail);
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostDetailVO createPost(CreatePostReq req, Long userId) {
        validatePostType(req.getPostType(), userId);

        UgcPost post = new UgcPost();
        post.setUserId(userId);
        post.setPostType(req.getPostType());
        post.setTitle(req.getTitle());
        post.setTitleEn(req.getTitleEn());
        post.setSummary(req.getSummary());
        post.setContent(req.getContent());
        post.setCoverImg(req.getCoverImg());
        post.setImages(req.getImages() == null ? null : JSONUtil.toJsonStr(req.getImages()));
        post.setIsPrivate(Boolean.TRUE.equals(req.getIsPrivate()) ? PRIVATE : PUBLIC);
        post.setStatus(resolveInitialStatus(req.getPostType()));

        ugcPostMapper.insert(post);
        log.info("New post created: id={}, type={}, userId={}, status={}",
                post.getId(), post.getPostType(), userId, post.getStatus());
        return getPostDetail(post.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostDetailVO updateByUser(Long postId, Long userId, CreatePostReq req) {
        UgcPost existing = getOwnedPost(postId, userId);
        if (PostStatus.PUBLISHED == existing.getStatus()) {
            throw new BizException(ResultCode.FORBIDDEN, "已发布的攻略不可编辑内容，请先下架后再修改");
        }
        if (!existing.getPostType().equals(req.getPostType())) {
            throw new BizException(ResultCode.PARAM_ERROR, "攻略类型不允许修改");
        }

        existing.setTitle(req.getTitle());
        existing.setTitleEn(req.getTitleEn());
        existing.setSummary(req.getSummary());
        existing.setContent(req.getContent());
        existing.setCoverImg(req.getCoverImg());
        existing.setImages(req.getImages() == null ? null : JSONUtil.toJsonStr(req.getImages()));
        ugcPostMapper.updateByUserId(existing);
        return getPostDetail(postId);
    }

    @Override
    public PageResult<PostListVO> listMyPosts(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        long total = ugcPostMapper.countMyPosts(userId);
        List<PostListVO> list = ugcPostMapper.selectMyPosts(userId, offset, size);
        list.forEach(this::fillDisplayTitle);
        return PageResult.of(total, list);
    }

    @Override
    public boolean setPrivate(Long postId, Long userId, boolean isPrivate) {
        getOwnedPost(postId, userId);
        return ugcPostMapper.updateIsPrivate(postId, userId, isPrivate ? PRIVATE : PUBLIC) > 0;
    }

    @Override
    public boolean softDeleteByUser(Long postId, Long userId) {
        getOwnedPost(postId, userId);
        return ugcPostMapper.softDeleteByUser(postId, userId) > 0;
    }

    @Override
    public PageResult<PostListVO> adminList(Integer postType, Integer status, int page, int size) {
        int offset = (page - 1) * size;
        long total = ugcPostMapper.countAdminPage(postType, status);
        List<PostListVO> list = ugcPostMapper.selectAdminPage(postType, status, offset, size);
        list.forEach(this::fillDisplayTitle);
        return PageResult.of(total, list);
    }

    @Override
    public boolean adminPublish(Long postId) {
        UgcPost post = getPostOrThrow(postId);
        if (PostStatus.REVIEWING != post.getStatus() && PostStatus.TAKEN_DOWN != post.getStatus()) {
            throw new BizException(ResultCode.FORBIDDEN, "当前状态不允许发布");
        }
        return ugcPostMapper.adminUpdateStatus(postId, PostStatus.PUBLISHED) > 0;
    }

    @Override
    public boolean adminTakeDown(Long postId) {
        UgcPost post = getPostOrThrow(postId);
        if (PostStatus.PUBLISHED != post.getStatus()) {
            throw new BizException(ResultCode.FORBIDDEN, "当前状态不允许下架");
        }
        return ugcPostMapper.adminUpdateStatus(postId, PostStatus.TAKEN_DOWN) > 0;
    }

    @Override
    public boolean adminDelete(Long postId) {
        getPostOrThrow(postId);
        return ugcPostMapper.softDeleteByAdmin(postId) > 0;
    }

    @Override
    public boolean isReviewEnabled() {
        String value = stringRedisTemplate.opsForValue().get(SystemConfigKeys.GUIDE_REVIEW_ENABLED);
        return !"0".equals(value);
    }

    @Override
    public boolean setReviewEnabled(boolean enabled) {
        stringRedisTemplate.opsForValue().set(SystemConfigKeys.GUIDE_REVIEW_ENABLED, enabled ? "1" : "0");
        return enabled;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long postId, Long userId) {
        PostDetailVO detail = ugcPostMapper.selectDetailById(postId);
        ensureCanView(detail);
        UgcLike existing = ugcLikeMapper.selectByUserAndTarget(userId, postId, LikeTargetType.POST);
        if (existing != null) {
            ugcLikeMapper.delete(userId, postId, LikeTargetType.POST);
            ugcPostMapper.decrementLikeCount(postId);
            return false;
        }
        UgcLike like = new UgcLike();
        like.setUserId(userId);
        like.setTargetId(postId);
        like.setTargetType(LikeTargetType.POST);
        ugcLikeMapper.insert(like);
        ugcPostMapper.incrementLikeCount(postId);
        return true;
    }

    /**
     * 校验发布类型，官方攻略仅管理员可发布。
     */
    private void validatePostType(Integer postType, Long userId) {
        if (!Integer.valueOf(PostType.OFFICIAL).equals(postType)
                && !Integer.valueOf(PostType.TOURIST).equals(postType)
                && !Integer.valueOf(PostType.DYNAMIC).equals(postType)) {
            throw new BizException(ResultCode.PARAM_ERROR, i18nUtil.msg(ResultCode.PARAM_ERROR));
        }
        if (Integer.valueOf(PostType.OFFICIAL).equals(postType) && !isAdmin(userId)) {
            throw new BizException(ResultCode.FORBIDDEN, i18nUtil.msg(ResultCode.FORBIDDEN));
        }
    }

    /**
     * 计算新攻略初始状态。
     */
    private Integer resolveInitialStatus(Integer postType) {
        if (Integer.valueOf(PostType.OFFICIAL).equals(postType)
                || Integer.valueOf(PostType.DYNAMIC).equals(postType)) {
            return PostStatus.PUBLISHED;
        }
        return isReviewEnabled() ? PostStatus.REVIEWING : PostStatus.PUBLISHED;
    }

    /**
     * 查询并校验攻略归属，管理员可代管。
     */
    private UgcPost getOwnedPost(Long postId, Long userId) {
        UgcPost post = getPostOrThrow(postId);
        if (!userId.equals(post.getUserId()) && !isAdmin(userId)) {
            throw new BizException(ResultCode.FORBIDDEN, i18nUtil.msg(ResultCode.FORBIDDEN));
        }
        return post;
    }

    /**
     * 查询未删除攻略实体。
     */
    private UgcPost getPostOrThrow(Long postId) {
        UgcPost post = ugcPostMapper.selectEntityById(postId);
        if (post == null) {
            throw new BizException(ResultCode.NOT_FOUND, i18nUtil.msg(ResultCode.NOT_FOUND));
        }
        return post;
    }

    /**
     * 私密、审核中、已下架内容仅作者或管理员可见。
     */
    private void ensureCanView(PostDetailVO detail) {
        if (detail == null) {
            throw new BizException(ResultCode.NOT_FOUND, i18nUtil.msg(ResultCode.NOT_FOUND));
        }
        Long currentUserId = LoginUser.getUserId();
        boolean owner = currentUserId != null && currentUserId.equals(detail.getUserId());
        boolean admin = isAdmin(currentUserId);
        boolean published = PostStatus.PUBLISHED == detail.getStatus();
        boolean publicPost = published && !Integer.valueOf(PRIVATE).equals(detail.getIsPrivate());
        if (!publicPost && !owner && !admin) {
            throw new BizException(ResultCode.NOT_FOUND, i18nUtil.msg(ResultCode.NOT_FOUND));
        }
    }

    /**
     * 判断指定用户是否为当前登录管理员。
     */
    private boolean isAdmin(Long userId) {
        Integer role = LoginUser.getRole();
        return userId != null && Integer.valueOf(UserRole.ADMIN).equals(role);
    }

    /**
     * 填充国际化展示标题。
     */
    private void fillDisplayTitle(PostListVO vo) {
        String titleEn = vo.getTitleEn();
        vo.setDisplayTitle(isEnglish() && titleEn != null && !titleEn.isBlank() ? titleEn : vo.getTitle());
    }

    private boolean isEnglish() {
        return Locale.ENGLISH.getLanguage().equals(LocaleContextHolder.get().getLanguage());
    }
}
