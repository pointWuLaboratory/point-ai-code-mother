package com.point.pointaicodemother.ai.model;

import jdk.jfr.Description;
import lombok.Data;

@Description("生成 HTML 代码文件的结果")
@Data
public class HtmlCodeResult {
    @Description("生成的 HTML 代码文件")
    private String htmlCode;
    @Description("生成的 HTML 代码文件的描述信息")
    private String description;
}
