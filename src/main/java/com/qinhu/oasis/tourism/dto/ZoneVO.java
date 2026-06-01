package com.qinhu.oasis.tourism.dto;

import com.qinhu.oasis.common.i18n.LocaleContextHolder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * 停车区域 VO（给 detail 页区域 Tab 用）
 */
@Data
public class ZoneVO {
    private Long id;
    private String zoneName;
    private String zoneNameEn;
    private String zoneCode;
    private BigDecimal hourlyRate;
    private String locationDesc;
    private Integer status;

    public String getDisplayName() {
        if (Locale.ENGLISH.getLanguage().equals(LocaleContextHolder.get().getLanguage())
                && zoneNameEn != null && !zoneNameEn.isBlank()) {
            return zoneNameEn;
        }
        return zoneName;
    }
}
