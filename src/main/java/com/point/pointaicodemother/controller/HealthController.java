package com.point.pointaicodemother.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author PointWu
 * @date 2026/4/16
 * @description
 */
@RestController
@RequestMapping("/health")
public class HealthController {
    @GetMapping("/")
    public String healthCheck() {
        return "OK";
    }
}
