package com.cxf.imooc;

import com.cxf.imooc.dao.PilingDetailDao;
import com.cxf.imooc.dao.ProgramTaskRelationDao;
import com.cxf.imooc.entity.PilingTsDetailEntity;
import com.cxf.imooc.entity.ProgramTaskRelation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ：yuifeng
 * @date ：Created in 2019/4/24 23:00
 * @description：Autowired注解测试类
 * @version:
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AutoWiredTest {

/*    @Resource
    PilingDetailDao pilingDetailDao ;

    @Resource
    ProgramTaskRelationDao programTaskRelationDao;

    @Test
    public void testPilingDetailDao(){
        String id = "f0ce3a6a0d5240afb68e5642d4eaf978";
        PilingTsDetailEntity detailEntity = pilingDetailDao.getById(id);
        System.out.println(detailEntity);


        List<ProgramTaskRelation> temp = programTaskRelationDao.getAll();
        ProgramTaskRelation relation0 = temp.get(0);
        ProgramTaskRelation relation1 = temp.get(1);

        String taskId = "fc9c4b2fff264a5bbb34854c80dac2e6";
        ProgramTaskRelation relation0 = programTaskRelationDao.getByTaskId(taskId);

        taskId = "39b1296b65ea4483878c641e1af3328b";
        ProgramTaskRelation relation1 = programTaskRelationDao.getByTaskId(taskId);

        List<ProgramTaskRelation> relations = new ArrayList<>();
        relations.add(relation0);
        relations.add(relation1);
        long ret= pilingDetailDao.updateDetailProgramIdWithProjectId(temp);
        System.out.println(ret);
    }

    @Test
    public void testProgramTaskRelationDao(){
        List<ProgramTaskRelation> temp = programTaskRelationDao.getAll();
        System.out.println(temp.get(0));
    }*/
}
