package com.cxf.imooc.dto;

import lombok.Data;

import java.util.List;

/**
 * @author ：XueFF
 * @date ：Created in 2019/5/7 16:30
 * @description：
 */
@Data
public class MachineDto{

    private String id;
    private String machineName;
    private Double x;
    private Double y;
    private Double longitude;
    private Double latitude;
    private Double directAngel;
    /**
     * 上传用户
     */
    private String username;

    private String programId;
    private String taskId;
    private String detailId;
    private String updateTime;
    private String type;
}
