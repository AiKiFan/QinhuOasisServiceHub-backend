package com.qinhu.oasis.common.config;

import com.qinhu.oasis.common.i18n.I18nInterceptor;
import com.qinhu.oasis.common.security.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 全局配置：注册拦截器链与跨域规则
 * <p>拦截器顺序：{@link I18nInterceptor}（先设 Locale）→ {@link AuthInterceptor}（再校验 Token）</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // i18n 拦截器先行，确保 Locale 在鉴权前已设置
        registry.addInterceptor(new I18nInterceptor())
                .addPathPatterns("/**");
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
