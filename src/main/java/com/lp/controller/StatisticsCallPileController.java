package com.lp.controller;

import com.lp.data.entity.StatisticsCallPile;
import com.lp.data.service.IStatisticsCallPileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 呼叫桩操作统计
 * @author yangguang
 * @DateTime 2022/4/6 11:26
 */
@RestController
@RequestMapping("statistics")
public class StatisticsCallPileController {
    @Autowired
    private IStatisticsCallPileService statisticsCallPileService;

    /** 查询信息列表 */
    @PostMapping("/list")
    @ResponseBody
    public List<StatisticsCallPile> list(StatisticsCallPile obj) {
        List<StatisticsCallPile> students = statisticsCallPileService.findtimeByPage(obj);
        return students;
    }

    /** 查询信息列表 */
    @PostMapping("/add")
    @ResponseBody
    public int add(@RequestBody @Validated StatisticsCallPile obj) {
        int i = statisticsCallPileService.insertStatistics(obj);
        return i;
    }

    /** 查询信息列表 */
    @PostMapping("/edit")
    @ResponseBody
    public int edit(@RequestBody StatisticsCallPile obj) {
        int i = statisticsCallPileService.updateStatistics(obj);
        return i;
    }
}
