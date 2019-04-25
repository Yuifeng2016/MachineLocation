package com.cxf.imooc.entity;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 桩详情
 */
@Data
public class PilingTsDetailEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String pileName;   //桩名
    private String taskName;    // 任务名称
    private String areaName;    //区域名称
    private String projectId;   //工程名称
    private Double b;   //纬度
    private Double l;   //经度
    private Double h;   //高度
    private Double x;   //设计北坐标
    private Double y;   //设计东坐标
    private Double z;   //设计高程
    private Double realx; // 实际北坐标
    private Double realy;   //实际东坐标
    private Double realz;   //实际高程
    private Double radius;  //半径
    private Double length;  //长度
    private Double pilingError; //误差
    private String status; //桩状态，未完成/锁定/完成 0/1/2
    private String createId;    //创建者ID
    private Date createTime; //开始时间，（锁定时间）
    private Date finishTime;    //完成时间
    private String machineId;   //完成的桩机ID
    private String orgId;   //施工单位ID
    private Date uploadFinishTime; // 上传的完成时间
    private Double startOffset; // 开始误差
    private Double endOffset; // 结束误差
    private Double pressure; // 压力
    private String pileMachine; // 桩机名字

    private String location;
    private String orgName;
    private String programId;   //项目id
}
