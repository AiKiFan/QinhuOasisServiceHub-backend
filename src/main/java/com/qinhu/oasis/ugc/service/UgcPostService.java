package com.qinhu.oasis.ugc.service;

import com.qinhu.oasis.common.result.PageResult;
import com.qinhu.oasis.ugc.dto.CreatePostReq;
import com.qinhu.oasis.ugc.dto.PostDetailVO;
import com.qinhu.oasis.ugc.dto.PostListVO;

/**
 * 攻略/动态业务接口
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public interface UgcPostService {

    PageResult<PostListVO> listPosts(Integer postType, int page, int size);

    PostDetailVO getPostDetail(Long postId);

    PostDetailVO createPost(CreatePostReq req, Long userId);

    PostDetailVO updateByUser(Long postId, Long userId, CreatePostReq req);

    PageResult<PostListVO> listMyPosts(Long userId, int page, int size);

    boolean setPrivate(Long postId, Long userId, boolean isPrivate);

    boolean softDeleteByUser(Long postId, Long userId);

    PageResult<PostListVO> adminList(Integer postType, Integer status, int page, int size);

    boolean adminPublish(Long postId);

    boolean adminTakeDown(Long postId);

    boolean adminDelete(Long postId);

    boolean isReviewEnabled();

    boolean setReviewEnabled(boolean enabled);

    boolean toggleLike(Long postId, Long userId);
}
