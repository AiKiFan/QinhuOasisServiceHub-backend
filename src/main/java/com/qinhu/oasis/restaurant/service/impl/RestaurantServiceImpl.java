package com.qinhu.oasis.restaurant.service.impl;

import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.i18n.LocaleContextHolder;
import com.qinhu.oasis.common.result.PageResult;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.restaurant.dto.RankListVO;
import com.qinhu.oasis.restaurant.dto.RestaurantDetailVO;
import com.qinhu.oasis.restaurant.dto.RestaurantListVO;
import com.qinhu.oasis.restaurant.entity.Restaurant;
import com.qinhu.oasis.restaurant.mapper.RestaurantMapper;
import com.qinhu.oasis.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 餐厅业务服务实现，排行榜由 Redis ZSet（key: restaurant:rank）驱动
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private static final String RANK_KEY = "restaurant:rank";

    private final RestaurantMapper restaurantMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final I18nUtil i18nUtil;

    @Override
    public PageResult<RestaurantListVO> listRestaurants(String category, int page, int size) {
        int offset = (page - 1) * size;
        List<Restaurant> list = restaurantMapper.selectPage(category, offset, size);
        long total = restaurantMapper.countByCategory(category);
        return PageResult.of(total, list.stream().map(this::toListVO).collect(Collectors.toList()));
    }

    @Override
    public List<RankListVO> getTopRank(int top) {
        Set<ZSetOperations.TypedTuple<String>> tuples =
                stringRedisTemplate.opsForZSet().reverseRangeWithScores(RANK_KEY, 0, top - 1);

        // Redis 为空时降级查 DB
        if (tuples == null || tuples.isEmpty()) {
            List<Restaurant> all = restaurantMapper.selectTopByScore(top);
            return IntStream.range(0, all.size())
                    .mapToObj(i -> toRankVO(all.get(i), i + 1))
                    .collect(Collectors.toList());
        }

        List<Long> ids = tuples.stream()
                .map(t -> Long.valueOf(Objects.requireNonNull(t.getValue())))
                .collect(Collectors.toList());

        List<Restaurant> restaurants = restaurantMapper.selectByIds(ids);
        Map<Long, Restaurant> map = restaurants.stream()
                .collect(Collectors.toMap(Restaurant::getId, r -> r));

        List<RankListVO> result = new ArrayList<>();
        int rank = 1;
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            Long id = Long.valueOf(Objects.requireNonNull(tuple.getValue()));
            Restaurant r = map.get(id);
            if (r != null) {
                result.add(toRankVO(r, rank++));
            }
        }
        return result;
    }

    @Override
    public RestaurantDetailVO getById(Long id) {
        Restaurant r = restaurantMapper.selectById(id);
        if (r == null || r.getDeleted() == 1) {
            throw new BizException(ResultCode.NOT_FOUND, i18nUtil.msg(ResultCode.NOT_FOUND));
        }
        return toDetailVO(r);
    }

    @Override
    public void initRankToRedis() {
        stringRedisTemplate.delete(RANK_KEY);
        List<Restaurant> all = restaurantMapper.selectAll();
        if (all.isEmpty()) {
            return;
        }
        Set<ZSetOperations.TypedTuple<String>> tuples = all.stream()
                .map(r -> (ZSetOperations.TypedTuple<String>)
                        new DefaultTypedTuple<>(String.valueOf(r.getId()), r.getSortScore()))
                .collect(Collectors.toSet());
        stringRedisTemplate.opsForZSet().add(RANK_KEY, tuples);
        log.info("Initialized restaurant rank ZSet with {} entries", all.size());
    }

    @Override
    public List<RestaurantListVO> getRestaurantsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Restaurant> restaurants = restaurantMapper.selectByIds(ids);
        return restaurants.stream().map(this::toListVO).collect(Collectors.toList());
    }

    // ───────────── 私有转换方法 ─────────────

    private boolean isEnglish() {
        return Locale.ENGLISH.getLanguage().equals(LocaleContextHolder.get().getLanguage());
    }

    private String resolveDisplayName(String name, String nameEn) {
        return (isEnglish() && nameEn != null && !nameEn.isBlank()) ? nameEn : name;
    }

    private RestaurantListVO toListVO(Restaurant r) {
        RestaurantListVO vo = new RestaurantListVO();
        vo.setId(r.getId());
        vo.setDisplayName(resolveDisplayName(r.getName(), r.getNameEn()));
        vo.setCategory(r.getCategory());
        vo.setCoverImg(r.getCoverImg());
        vo.setAvgPrice(r.getAvgPrice());
        vo.setRating(r.getRating());
        vo.setReviewCount(r.getReviewCount());
        vo.setBusinessHours(r.getBusinessHours());
        return vo;
    }

    private RankListVO toRankVO(Restaurant r, int rank) {
        RankListVO vo = new RankListVO();
        vo.setRank(rank);
        vo.setId(r.getId());
        vo.setDisplayName(resolveDisplayName(r.getName(), r.getNameEn()));
        vo.setCategory(r.getCategory());
        vo.setCoverImg(r.getCoverImg());
        vo.setRating(r.getRating());
        vo.setReviewCount(r.getReviewCount());
        vo.setSortScore(r.getSortScore());
        return vo;
    }

    private RestaurantDetailVO toDetailVO(Restaurant r) {
        RestaurantDetailVO vo = new RestaurantDetailVO();
        vo.setId(r.getId());
        vo.setDisplayName(resolveDisplayName(r.getName(), r.getNameEn()));
        vo.setCategory(r.getCategory());
        vo.setCoverImg(r.getCoverImg());
        vo.setAvgPrice(r.getAvgPrice());
        vo.setRating(r.getRating());
        vo.setReviewCount(r.getReviewCount());
        vo.setBusinessHours(r.getBusinessHours());
        vo.setAddress(r.getAddress());
        vo.setLat(r.getLat());
        vo.setLng(r.getLng());
        vo.setPhone(r.getPhone());
        vo.setImages(r.getImages());
        vo.setTags(r.getTags());
        return vo;
    }
}
