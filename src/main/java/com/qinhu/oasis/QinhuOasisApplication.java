package com.qinhu.oasis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 沁湖驿站云服务平台 启动类
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@SpringBootApplication
@MapperScan("com.qinhu.oasis.*.mapper")
@EnableScheduling
public class QinhuOasisApplication {

    public static void main(String[] args) {
        SpringApplication.run(QinhuOasisApplication.class, args);
    }
}
