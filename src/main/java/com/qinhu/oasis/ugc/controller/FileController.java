package com.qinhu.oasis.ugc.controller;

import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.result.Result;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.common.security.LoginUser;
import com.qinhu.oasis.common.service.FileStorageService;
import com.qinhu.oasis.ugc.dto.FileUploadVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传 REST 接口控制器（需登录）
 * <p>当前仅支持图片上传到 qosh-ugc-images；证书上传由译员模块单独处理</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;
    private final I18nUtil i18nUtil;

    @Value("${minio.buckets.ugc-images}")
    private String ugcImagesBucket;

    /**
     * 上传 UGC 图片（需登录）
     * <p>支持 JPEG/PNG/WebP/GIF，单文件最大 10MB；返回 Minio 访问 URL</p>
     *
     * @param file 上传的图片文件
     * @return 文件访问 URL 及基本信息
     */
    @PostMapping("/upload")
    public Result<FileUploadVO> upload(@RequestParam("file") MultipartFile file) {
        Long userId = LoginUser.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, i18nUtil.msg(ResultCode.UNAUTHORIZED));
        }
        if (file.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR, i18nUtil.msg(ResultCode.PARAM_ERROR));
        }
        String url = fileStorageService.uploadImage(file, ugcImagesBucket);
        return Result.ok(new FileUploadVO(url, file.getOriginalFilename(), file.getSize()));
    }
}
