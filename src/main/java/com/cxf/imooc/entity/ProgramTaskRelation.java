package com.cxf.imooc.entity;

import lombok.Data;

/**
 * @author ：XueFF
 * @date ：Created in 2019/4/25 10:55
 * @description：项目-任务关系类
 */
@Data
public class ProgramTaskRelation {

    private String programId;
    private String taskId;
}
