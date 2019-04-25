package com.cxf.imooc.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ：XueFF
 * @date ：Created in 2019/4/22 9:50
 * @description：桩机实时位置类
 */
@Data
public class MachineRealTimeLocation implements Serializable {
    private static final long serialVersionUID = 8195570989072284198L;
    private String id;
    private String machineName;
    private Double x;
    private Double y;
    private Double longitude;
    private Double latitude;
    private Double directAngel;
    private String username;
    private String taskId;
    private String detailId;
    private String updateTime;
    private String type;



}
