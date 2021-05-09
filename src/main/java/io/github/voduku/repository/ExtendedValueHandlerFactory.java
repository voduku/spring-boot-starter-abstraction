package io.github.voduku.repository;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.NoArgsConstructor;
import org.hibernate.query.criteria.internal.ValueHandlerFactory;
import org.hibernate.query.criteria.internal.ValueHandlerFactory.BaseValueHandler;
import org.hibernate.query.criteria.internal.ValueHandlerFactory.ValueHandler;

/**
 * @author VuDo
 * @since 5/8/2021
 */
@NoArgsConstructor
public class ExtendedValueHandlerFactory {

  private static final ZoneId defaultZone = ZoneId.systemDefault();

  @SuppressWarnings("unchecked")
  public static <T> ValueHandler<T> determineAppropriateHandler(Class<T> targetType) {
    if (String.class.equals(targetType)) {
      return (ValueHandler<T>) StringValueHandler.INSTANCE;
    }
    if (Byte.class.equals(targetType) || Byte.TYPE.equals(targetType)) {
      return (ValueHandler<T>) ByteValueHandler.INSTANCE;
    }
    if (Short.class.equals(targetType) || Short.TYPE.equals(targetType)) {
      return (ValueHandler<T>) ShortValueHandler.INSTANCE;
    }
    if (Integer.class.equals(targetType) || Integer.TYPE.equals(targetType)) {
      return (ValueHandler<T>) IntegerValueHandler.INSTANCE;
    }
    if (Long.class.equals(targetType) || Long.TYPE.equals(targetType)) {
      return (ValueHandler<T>) LongValueHandler.INSTANCE;
    }
    if (Float.class.equals(targetType) || Float.TYPE.equals(targetType)) {
      return (ValueHandler<T>) FloatValueHandler.INSTANCE;
    }
    if (Double.class.equals(targetType) || Double.TYPE.equals(targetType)) {
      return (ValueHandler<T>) DoubleValueHandler.INSTANCE;
    }
    if (BigInteger.class.equals(targetType)) {
      return (ValueHandler<T>) BigIntegerValueHandler.INSTANCE;
    }
    if (BigDecimal.class.equals(targetType)) {
      return (ValueHandler<T>) BigDecimalValueHandler.INSTANCE;
    }
    if (Boolean.class.equals(targetType)) {
      return (ValueHandler<T>) BooleanValueHandler.INSTANCE;
    }
    if (Date.class.equals(targetType)) {
      return (ValueHandler<T>) UtilDateValueHandler.INSTANCE;
    }
    if (java.sql.Date.class.equals(targetType)) {
      return (ValueHandler<T>) SqlDateValueHandler.INSTANCE;
    }
    if (Timestamp.class.equals(targetType)) {
      return (ValueHandler<T>) TimestampValueHandler.INSTANCE;
    }
    if (LocalDate.class.equals(targetType)) {
      return (ValueHandler<T>) LocalDateValueHandler.INSTANCE;
    }
    if (LocalDateTime.class.equals(targetType)) {
      return (ValueHandler<T>) LocalDateTimeValueHandler.INSTANCE;
    }
    if (OffsetTime.class.equals(targetType)) {
      return (ValueHandler<T>) OffsetTimeValueHandler.INSTANCE;
    }
    if (OffsetDateTime.class.equals(targetType)) {
      return (ValueHandler<T>) OffsetDateTimeValueHandler.INSTANCE;
    }
    return null;
  }

  private static IllegalArgumentException unknownConversion(Object value, Class<?> type) {
    return new IllegalArgumentException(
        "Unaware how to convert value [" + value + " : " + typeName(value) + "] to requested type [" + type.getName() + "]"
    );
  }

  private static String typeName(Object value) {
    return value == null ? "???" : value.getClass().getName();
  }

  public static class ByteValueHandler extends BaseValueHandler<Byte> implements Serializable {

    public static final ValueHandlerFactory.ByteValueHandler INSTANCE = new ValueHandlerFactory.ByteValueHandler();

    @SuppressWarnings({"UnnecessaryBoxing"})
    public Byte convert(Object value) {
      if (value == null) {
        return null;
      }
      if (value instanceof Number) {
        return Byte.valueOf(((Number) value).byteValue());
      } else if (value instanceof String) {
        return Byte.valueOf(((String) value));
      }
      throw unknownConversion(value, Byte.class);
    }
  }

  public static class ShortValueHandler extends BaseValueHandler<Short> implements Serializable {

    public static final ValueHandlerFactory.ShortValueHandler INSTANCE = new ValueHandlerFactory.ShortValueHandler();

    @SuppressWarnings({"UnnecessaryBoxing"})
    public Short convert(Object value) {
      if (value == null) {
        return null;
      }
      if (value instanceof Number) {
        return Short.valueOf(((Number) value).shortValue());
      } else if (value instanceof String) {
        return Short.valueOf(((String) value));
      }
      throw unknownConversion(value, Short.class);
    }
  }

  public static class IntegerValueHandler extends BaseValueHandler<Integer> implements Serializable {

    public static final ValueHandlerFactory.IntegerValueHandler INSTANCE = new ValueHandlerFactory.IntegerValueHandler();

    @SuppressWarnings({"UnnecessaryBoxing"})
    public Integer convert(Object value) {
      if (value == null) {
        return null;
      }
      if (value instanceof Number) {
        return Integer.valueOf(((Number) value).intValue());
      } else if (value instanceof String) {
        return Integer.valueOf(((String) value));
      }
      throw unknownConversion(value, Integer.class);
    }
  }

  public static class LongValueHandler extends BaseValueHandler<Long> implements Serializable {

    public static final ValueHandlerFactory.LongValueHandler INSTANCE = new ValueHandlerFactory.LongValueHandler();

    @SuppressWarnings({"UnnecessaryBoxing"})
    public Long convert(Object value) {
      if (value == null) {
        return null;
      }
      if (value instanceof Number) {
        return Long.valueOf(((Number) value).longValue());
      } else if (value instanceof String) {
        return Long.valueOf(((String) value));
      }
      throw unknownConversion(value, Long.class);
    }

    @Override
    public String render(Long value) {
      return value.toString() + 'L';
    }
  }

  public static class FloatValueHandler extends BaseValueHandler<Float> implements Serializable {

    public static final ValueHandlerFactory.FloatValueHandler INSTANCE = new ValueHandlerFactory.FloatValueHandler();

    @SuppressWarnings({"UnnecessaryBoxing"})
    public Float convert(Object value) {
      if (value == null) {
        return null;
      }
      if (value instanceof Number) {
        return Float.valueOf(((Number) value).floatValue());
      } else if (value instanceof String) {
        return Float.valueOf(((String) value));
      }
      throw unknownConversion(value, Float.class);
    }

    @Override
    public String render(Float value) {
      return value.toString() + 'F';
    }
  }

  public static class DoubleValueHandler extends BaseValueHandler<Double> implements Serializable {

    public static final ValueHandlerFactory.DoubleValueHandler INSTANCE = new ValueHandlerFactory.DoubleValueHandler();

    @SuppressWarnings({"UnnecessaryBoxing"})
    public Double convert(Object value) {
      if (value == null) {
        return null;
      }
      if (value instanceof Number) {
        return Double.valueOf(((Number) value).doubleValue());
      } else if (value instanceof String) {
        return Double.valueOf(((String) value));
      }
      throw unknownConversion(value, Double.class);
    }

    @Override
    public String render(Double value) {
      return value.toString() + 'D';
    }
  }

  public static class BigIntegerValueHandler extends BaseValueHandler<BigInteger> implements Serializable {

    public static final ValueHandlerFactory.BigIntegerValueHandler INSTANCE = new ValueHandlerFactory.BigIntegerValueHandler();

    public BigInteger convert(Object value) {
      if (value == null) {
        return null;
      }
      if (value instanceof BigInteger) {
        return (BigInteger) value;
      }
      if (value instanceof BigDecimal) {
        return ((BigDecimal) value).toBigInteger();
      }
      if (value instanceof Number) {
        return BigInteger.valueOf(((Number) value).longValue());
      } else if (value instanceof String) {
        return new BigInteger((String) value);
      }
      throw unknownConversion(value, BigInteger.class);
    }

    @Override
    public String render(BigInteger value) {
      return value.toString() + "BI";
    }
  }

  public static class BigDecimalValueHandler extends BaseValueHandler<BigDecimal> implements Serializable {

    public static final ValueHandlerFactory.BigDecimalValueHandler INSTANCE = new ValueHandlerFactory.BigDecimalValueHandler();

    public BigDecimal convert(Object value) {
      if (value == null) {
        return null;
      }
      if (value instanceof BigDecimal) {
        return (BigDecimal) value;
      }
      if (value instanceof BigInteger) {
        return new BigDecimal((BigInteger) value);
      } else if (value instanceof Number) {
        return BigDecimal.valueOf(((Number) value).doubleValue());
      } else if (value instanceof String) {
        return new BigDecimal((String) value);
      }
      throw unknownConversion(value, BigDecimal.class);
    }

    @Override
    public String render(BigDecimal value) {
      return value.toString() + "BD";
    }
  }

  public static class BooleanValueHandler extends BaseValueHandler<Boolean> implements Serializable {

    public static final ValueHandlerFactory.BooleanValueHandler INSTANCE = new ValueHandlerFactory.BooleanValueHandler();

    public Boolean convert(Object value) {
      if (value == null) {
        return null;
      }
      if (value instanceof Boolean) {
        return (Boolean) value;
      }
      if (value instanceof String) {
        return Boolean.getBoolean((String) value);
      }
      throw unknownConversion(value, Boolean.class);
    }
  }

  public static class StringValueHandler extends BaseValueHandler<String> implements Serializable {

    public static final ValueHandlerFactory.StringValueHandler INSTANCE = new ValueHandlerFactory.StringValueHandler();

    public String convert(Object value) {
      return value == null ? null : value.toString();
    }
  }

  public static class UtilDateValueHandler extends BaseValueHandler<Date> implements Serializable {

    public static final UtilDateValueHandler INSTANCE = new UtilDateValueHandler();

    public Date convert(Object value) {
      if (value == null) {
        return null;
      }
      if (Number.class.isAssignableFrom(value.getClass())) {
        return new Date(((Number) value).longValue());
      } else if (value instanceof Date) {
        return (Date) value;
      }
      throw unknownConversion(value, Date.class);
    }
  }

  public static class SqlDateValueHandler extends BaseValueHandler<java.sql.Date> implements Serializable {

    public static final SqlDateValueHandler INSTANCE = new SqlDateValueHandler();

    public java.sql.Date convert(Object value) {
      if (value == null) {
        return null;
      }
      if (Number.class.isAssignableFrom(value.getClass())) {
        return new java.sql.Date(((Number) value).longValue());
      } else if (value instanceof LocalDate) {
        return java.sql.Date.valueOf((LocalDate) value);
      } else if (value instanceof java.sql.Date) {
        return (java.sql.Date) value;
      }
      throw unknownConversion(value, java.sql.Date.class);
    }
  }

  public static class TimestampValueHandler extends BaseValueHandler<Timestamp> implements Serializable {

    public static final TimestampValueHandler INSTANCE = new TimestampValueHandler();

    public Timestamp convert(Object value) {
      if (value == null) {
        return null;
      }
      if (Number.class.isAssignableFrom(value.getClass())) {
        return new Timestamp(((Number) value).longValue());
      } else if (value instanceof LocalDateTime) {
        return Timestamp.valueOf((LocalDateTime) value);
      } else if (value instanceof Timestamp) {
        return (Timestamp) value;
      }
      throw unknownConversion(value, Timestamp.class);
    }
  }

  public static class LocalDateValueHandler extends BaseValueHandler<LocalDate> implements Serializable {

    public static final LocalDateValueHandler INSTANCE = new LocalDateValueHandler();

    public LocalDate convert(Object value) {
      if (value == null) {
        return null;
      }
      if (Number.class.isAssignableFrom(value.getClass())) {
        return LocalDate.ofInstant(Instant.ofEpochMilli(((Number) value).longValue()), defaultZone);
      } else if (value instanceof String) {
        return LocalDate.parse((String) value);
      } else if (value instanceof LocalDate) {
        return (LocalDate) value;
      }
      throw unknownConversion(value, LocalDate.class);
    }
  }

  public static class LocalDateTimeValueHandler extends BaseValueHandler<LocalDateTime> implements Serializable {

    public static final LocalDateTimeValueHandler INSTANCE = new LocalDateTimeValueHandler();

    public LocalDateTime convert(Object value) {
      if (value == null) {
        return null;
      }
      if (Number.class.isAssignableFrom(value.getClass())) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(((Number) value).longValue()), defaultZone);
      } else if (value instanceof String) {
        return LocalDateTime.parse((String) value);
      } else if (value instanceof LocalDateTime) {
        return (LocalDateTime) value;
      }
      throw unknownConversion(value, LocalDateTime.class);
    }
  }

  public static class OffsetTimeValueHandler extends BaseValueHandler<OffsetTime> implements Serializable {

    public static final OffsetTimeValueHandler INSTANCE = new OffsetTimeValueHandler();

    public OffsetTime convert(Object value) {
      if (value == null) {
        return null;
      }
      if (Number.class.isAssignableFrom(value.getClass())) {
        return OffsetTime.ofInstant(Instant.ofEpochMilli(((Number) value).longValue()), defaultZone);
      } else if (value instanceof String) {
        return OffsetTime.parse((String) value);
      } else if (value instanceof OffsetTime) {
        return (OffsetTime) value;
      }
      throw unknownConversion(value, OffsetTime.class);
    }
  }

  public static class OffsetDateTimeValueHandler extends BaseValueHandler<OffsetDateTime> implements Serializable {

    public static final OffsetDateTimeValueHandler INSTANCE = new OffsetDateTimeValueHandler();

    public OffsetDateTime convert(Object value) {
      if (value == null) {
        return null;
      }
      if (Number.class.isAssignableFrom(value.getClass())) {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(((Number) value).longValue()), defaultZone);
      } else if (value instanceof String) {
        return OffsetDateTime.parse((String) value);
      } else if (value instanceof OffsetDateTime) {
        return (OffsetDateTime) value;
      }
      throw unknownConversion(value, OffsetDateTime.class);
    }
  }

  @SuppressWarnings("all")
  public static class EnumValueHandler extends BaseValueHandler<Enum<?>> implements Serializable {

    private final Class clazz;

    public EnumValueHandler(Class<?> enumClass) {
      this.clazz = enumClass;
    }

    public Enum<?> convert(Object value) {
      if (value == null) {
        return null;
      }
      if (value instanceof String) {
        return Enum.valueOf(clazz, (String) value);
      }
      throw unknownConversion(value, clazz);
    }
  }
}
