package com.github.dactiv.healthan.security.audit;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.exception.SystemException;
import com.github.dactiv.healthan.security.AuditIndexProperties;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 日期索引生成器
 *
 * @author maurice.chen
 */
public class DateIndexGenerator extends PropertyIndexGenerator {

    public DateIndexGenerator(AuditIndexProperties auditIndexProperties) {
        super(auditIndexProperties);
    }

    @Override
    protected List<String> afterAppend(Object object) {

        List<String> result = super.afterAppend(object);

        Object date = getDateValue(object);

        if (Objects.isNull(date)) {
            throw new SystemException("在 " + object.getClass().getName() + "中，找不到 " + getAuditIndexProperties().getDatePropertyNames() + " 字段作为日期值使用");
        }

        if (ChronoLocalDateTime.class.isAssignableFrom(date.getClass())) {

            ChronoLocalDateTime<?> time = Casts.cast(date);

            result.add(time.format(DateTimeFormatter.ofPattern(getAuditIndexProperties().getPattern())));

        } else if (Date.class.isAssignableFrom(date.getClass())) {
            Date d = Casts.cast(date);
            result.add(new SimpleDateFormat(getAuditIndexProperties().getPattern()).format(d));
        } else if (Instant.class.isAssignableFrom(date.getClass())) {
            Instant i = Casts.cast(date);
            LocalDateTime localDateTime = LocalDateTime.ofInstant(i, ZoneId.systemDefault());
            result.add(localDateTime.format(DateTimeFormatter.ofPattern(getAuditIndexProperties().getPattern())));
        } else {
            throw new SystemException("对象 [" + object.getClass().getName() + "] 的 [" + getAuditIndexProperties().getDatePropertyNames() + "] 属性非日期类型");
        }

        return result;
    }

    private Object getDateValue(Object object) {
        Object date = null;
        for (String datePropertyName : getAuditIndexProperties().getDatePropertyNames()) {
            Field field = ReflectionUtils.findField(object.getClass(), datePropertyName);
            if (Objects.isNull(field)) {
                continue;
            }
            field.setAccessible(true);
            date = ReflectionUtils.getField(field, object);
            if (Objects.nonNull(date)) {
                break;
            }
        }
        return date;
    }
}
