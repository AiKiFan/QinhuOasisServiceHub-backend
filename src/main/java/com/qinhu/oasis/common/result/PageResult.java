package com.qinhu.oasis.common.result;

import lombok.Data;

import java.util.List;

/**
 * 分页查询结果封装
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class PageResult<T> {

    /** 总记录数 */
    private long total;
    /** 当前页数据列表 */
    private List<T> list;

    /**
     * 构建分页结果
     *
     * @param total 总记录数
     * @param list  当前页数据列表
     * @param <T>   数据类型
     * @return 分页结果对象
     */
    public static <T> PageResult<T> of(long total, List<T> list) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setList(list);
        return result;
    }
}
