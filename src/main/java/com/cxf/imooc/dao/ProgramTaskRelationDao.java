package com.cxf.imooc.dao;

import com.cxf.imooc.entity.ProgramTaskRelation;

import java.util.List;

/**
 * @author ：XueFF
 * @date ：Created in 2019/4/25 10:56
 * @description：项目-任务关系dao
 */
public interface ProgramTaskRelationDao {

    List<ProgramTaskRelation> getAll();

    ProgramTaskRelation getByTaskId(String id);
}
