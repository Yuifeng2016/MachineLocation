package com.cxf.imooc.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @author ：XueFF
 * @date ：Created in 2019/4/22 9:50
 * @description：桩机实时位置类
 */
@Data
@EqualsAndHashCode
public class MachineRealTimeLocation implements Serializable {
    private static final long serialVersionUID = 8195570989072284198L;
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
    /**
     * 项目成员
     */
    private List<String> member;
    private String programId;
    private String taskId;
    private String detailId;
    private String updateTime;
    private String type;



}
