package com.lp.data.service.impl;

import com.lp.data.entity.StatisticsCallPile;
import com.lp.data.entity.StatisticsCallPile;
import com.lp.data.service.IStatisticsCallPileService;
import com.lp.data.service.IStatisticsCallPileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.temporal.TemporalField;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author yangguang
 * @DateTime 2022/4/6 11:08
 */
@Service
public class StatisticsCallPileServiceImpl implements IStatisticsCallPileService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public int insertStatistics(StatisticsCallPile statisticsCallPile) {
        try {
            statisticsCallPile.setCreateTime( new Date().getTime() );
            statisticsCallPile.setUpdateTime( statisticsCallPile.getCreateTime() );
            mongoTemplate.insert(statisticsCallPile);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


    @Override
    public int updateStatistics(StatisticsCallPile statisticsCallPile) {
        //通过query根据id查询出对应对象，通过update对象进行修改
        Query query = new Query(Criteria.where("_id").is(statisticsCallPile.getId()));
        Update update = new Update()
                .set("isOrder", statisticsCallPile.getIsOrder())
                .set("updateTime",System.currentTimeMillis());
        try {
            mongoTemplate.updateFirst(query, update, StatisticsCallPile.class);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


    @Override
    public int removeStatistics(Long id) {
        Query query=new Query(Criteria.where("_id").is(id));
        try {
            mongoTemplate.remove(query,StatisticsCallPile.class);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public StatisticsCallPile findOne(StatisticsCallPile statisticsCallPile) {
        Query query = new Query(Criteria.where("_id").is(statisticsCallPile.getId()));
        StatisticsCallPile one = mongoTemplate.findOne(query, StatisticsCallPile.class);
        return one;
    }

    @Override
    public List<StatisticsCallPile> findlike(StatisticsCallPile statisticsCallPile) {
        Pattern pattern = Pattern.compile("^.*" + statisticsCallPile.getCallPileNo() + ".*$", Pattern.CASE_INSENSITIVE);
        Query query = new Query(Criteria.where("username").regex(pattern));
        List<StatisticsCallPile> studentList = mongoTemplate.find(query, StatisticsCallPile.class);
        return studentList;
    }

    @Override
    public List<StatisticsCallPile> findmore(StatisticsCallPile statisticsCallPile) {
        Query query = new Query(Criteria.where("username").is(statisticsCallPile.getCallPileNo()));
        List<StatisticsCallPile> students = mongoTemplate.find(query, StatisticsCallPile.class);
        return students;
    }

    @Override
    public List<StatisticsCallPile> findtime(StatisticsCallPile statisticsCallPile) {
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "timer"));
        List<StatisticsCallPile> students = mongoTemplate.find(query, StatisticsCallPile.class);
        return students;
    }

    @Override
    public List<StatisticsCallPile> findtimeByPage(StatisticsCallPile statisticsCallPile) {
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC,"timer" ));
        query.skip(0).limit(3);
        List<StatisticsCallPile> students = mongoTemplate.find(query, StatisticsCallPile.class);
        return students;
    }





}