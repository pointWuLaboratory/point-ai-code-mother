package com.point.pointaicodemother;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.point.pointaicodemother.mapper")
public class PointAiCodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(PointAiCodeMotherApplication.class, args);
    }

}
