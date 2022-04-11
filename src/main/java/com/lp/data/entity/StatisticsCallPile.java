package com.lp.data.entity;

import com.lp.annotation.AutoIncKey;
import com.sun.istack.internal.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 统计
 * @author yangguang
 * @DateTime 2022/4/11 10:43
 */

@Document(collection = "statisticsCallPile")  //指定要对应的文档名(表名）
@Data
public class StatisticsCallPile {

    @Id
    @AutoIncKey
    private Long id;

    /**
     * 呼叫桩编号
     * */
    private String callPileNo;

    /**
     * 页面类型
     * */
    private Integer pageType;

    /**
     * 操作类型
     * */
    private Integer operateType;
    /**
     * 访问时长 s
     * */
    private Integer accessDuration;
    /**
     * 是否下单 1：是，o：否
     * */
    private Integer isOrder;

    /**
     * 操作时间
     * */

    private Long createTime;
    /**
     * 更新时间
     * */
    private Long updateTime;

}
