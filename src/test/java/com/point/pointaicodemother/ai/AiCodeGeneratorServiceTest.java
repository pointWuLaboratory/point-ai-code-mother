package com.point.pointaicodemother.ai;

import com.point.pointaicodemother.ai.model.HtmlCodeResult;
import com.point.pointaicodemother.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("做个程序员的博客，不超过20行");
        Assertions.assertNotNull(result);
    }

    @Test
    void testGenerateMultiFileCode() {
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode("做个程序员的博客，不超过50行");
        Assertions.assertNotNull(result);
    }
}