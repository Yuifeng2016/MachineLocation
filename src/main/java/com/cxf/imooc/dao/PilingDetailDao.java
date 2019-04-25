package com.cxf.imooc.dao;

import com.cxf.imooc.entity.PilingTsDetailEntity;
import com.cxf.imooc.entity.ProgramTaskRelation;

import java.util.List;

/**
 * @author ：XueFF
 * @date ：Created in 2019/4/25 10:16
 * @description：临时用打桩详情dao
 */
public interface PilingDetailDao {

    long updateDetailProgramIdWithProjectId(List<ProgramTaskRelation> relations);

    PilingTsDetailEntity getById(String id);
}
