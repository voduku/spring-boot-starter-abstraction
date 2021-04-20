package io.github.voduku.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Timestamp;
import java.util.Date;
import org.mapstruct.Mapper;
import org.mapstruct.Qualifier;

/**
 * @author VuDo
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface CommonMapper {

  default Timestamp longToTimestamp(Long l) {
    return l == null ? null : new Timestamp(l);
  }

  default Long timestampToLong(Timestamp timestamp) {
    return timestamp == null ? null : timestamp.getTime();
  }

  @LongToDate
  default Date longToDate(Long l) {
    return l == null ? null : new Date(l);
  }

  default Long dateToLong(Date date) {
    return date == null ? null : date.getTime();
  }

  default Timestamp currentTimestamp() {
    return new Timestamp(System.currentTimeMillis());
  }

  @Qualifier
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.CLASS)
  @interface LongToDate {

  }
}
