package com.lp.nettyserver.data.service;

import com.lp.nettyserver.data.entity.StatisticsCallPile;

import java.util.List;

/**
 * @author yangguang
 * @DateTime 2022/4/6 11:11
 */
public interface IStatisticsCallPileService {
    int insertStatistics(StatisticsCallPile statisticsCallPile);

    int updateStatistics(StatisticsCallPile statisticsCallPile);

    int removeStatistics(Long id);

    StatisticsCallPile findOne(StatisticsCallPile student);

    List<StatisticsCallPile> findlike(StatisticsCallPile statisticsCallPile);

    List<StatisticsCallPile> findmore(StatisticsCallPile statisticsCallPile);

    List<StatisticsCallPile> findtime(StatisticsCallPile statisticsCallPile);

    List<StatisticsCallPile> findtimeByPage(StatisticsCallPile statisticsCallPile);

}
