package com.lp.listener;

import com.lp.annotation.AutoIncKey;
import com.lp.data.entity.StatisticsCallPile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author yangguang
 * @DateTime 2022/4/11 14:14
 */
@Component
public class MongoDbSaveEventListener extends AbstractMongoEventListener<Object>{
    private static final Logger logger= LoggerFactory.getLogger(MongoDbSaveEventListener.class);
    @Autowired
    private MongoTemplate mongo;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        logger.info(event.getSource().toString());
        final Object source = event.getSource();
        if (source != null) {
            ReflectionUtils.doWithFields(source.getClass(), new ReflectionUtils.FieldCallback() {
                @Override
                public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                    ReflectionUtils.makeAccessible(field);
                    // 如果字段添加了我们自定义的AutoIncKey注解
                    if (field.isAnnotationPresent(AutoIncKey.class)) {
                        // 设置自增ID
                        field.set(source, getNextId(source.getClass().getSimpleName()));
                    }
                }
            });
        }

    }

    private Long getNextId(String collName) {
        Query query = new Query().with(Sort.by(Sort.Direction.DESC,"id"));
        query.fields().include("id");

        StatisticsCallPile statisticsCallPile = mongo.findOne(query, StatisticsCallPile.class);
        if( statisticsCallPile == null || statisticsCallPile.getId() == null ){
            return 1L;
        }
        return statisticsCallPile.getId()+1;

//        Update update = new Update();
//        update.inc("id", 1);
//
//        FindAndModifyOptions options = new FindAndModifyOptions();
//        options.upsert(true);
//        options.returnNew(true);
//        StatisticsCallPile inc= mongo.findAndModify(query, update, options, StatisticsCallPile.class);
//        return inc.getId();
    }
}