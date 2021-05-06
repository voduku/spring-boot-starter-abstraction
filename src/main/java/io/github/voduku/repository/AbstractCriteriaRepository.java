package io.github.voduku.repository;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.voduku.model.AbstractSearch;
import io.github.voduku.model.criteria.Operator;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.hibernate.query.criteria.internal.predicate.ComparisonPredicate;
import org.hibernate.query.criteria.internal.predicate.ComparisonPredicate.ComparisonOperator;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.CollectionUtils;

/**
 * // @formatter:off
 * Provide a an approach which utilize Criteria APIs to create HQL queries and perform executions
 * You can override any method to change query creation and result mapping processes
 * // @formatter:on
 *
 * @param <ENTITY> Entity Type
 * @param <KEY> Entity Key. This could be single column or composite key.
 * @author VuDo
 * @since 1.0.0
 */
@Slf4j
@Getter
@Setter
public abstract class AbstractCriteriaRepository<ENTITY, KEY> implements CustomizableRepository<ENTITY, KEY> {

  // @formatter:off
  protected static final ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL)
      .disable(FAIL_ON_UNKNOWN_PROPERTIES)
      .disable(FAIL_ON_EMPTY_BEANS)
      .enable(WRITE_DATES_AS_TIMESTAMPS);
  private static final TypeReference<LinkedHashMap<String, Object>> keyMapType = new TypeReference<>() {};
  private static final TypeReference<LinkedHashMap<String, Map<Operator, Object>>> mapType = new TypeReference<>() {};
  // @formatter:on
  protected final Class<ENTITY> clazz;
  protected final String entityName;
  protected final List<String> idFields = new ArrayList<>();
  protected final Set<String> fields = new HashSet<>();
  private final Map<String, Function<Object, ?>> javaDateInitializer = new HashMap<>();
  private final Map<String, Function<Object, ?>> javaTemporalInitializer = new HashMap<>();
  private final ZoneId defaultZone = ZoneId.systemDefault();
  @PersistenceContext
  protected EntityManager entityManager;
  protected CriteriaBuilderImpl cb;

  /**
   * Initialize the class with necessary info to perform query creation. Using this should not be too bad since it only run once. This takes ~0.0001 seconds to
   * finish. It could be even faster on cloud server
   */
  @SuppressWarnings("unchecked")
  public AbstractCriteriaRepository() {
    this.clazz = (Class<ENTITY>) Objects.requireNonNull(GenericTypeResolver.resolveTypeArguments(getClass(), AbstractCriteriaRepository.class))[0];
    this.entityName = clazz.getSimpleName();
    Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
      this.fields.add(field.getName());
      if (Objects.nonNull(field.getAnnotation(Id.class))) {
        idFields.add(field.getName());
      }
      createInitializers(field);
    });
  }

  public AbstractCriteriaRepository(Class<ENTITY> clazz) {
    this.clazz = clazz;
    this.entityName = clazz.getSimpleName();
    Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
      this.fields.add(field.getName());
      if (Objects.nonNull(field.getAnnotation(Id.class))) {
        idFields.add(field.getName());
      }
      createInitializers(field);
    });
  }

  @PostConstruct
  public void initBuilder() {
    this.cb = (CriteriaBuilderImpl) entityManager.getCriteriaBuilder();
  }

  /**
   * Get an entity with given {@link KEY} with optional functionalities to optimize database request and response
   *
   * @param key    entity key
   * @param params optional customizing params
   * @return an {@link ENTITY} entity
   */
  public ENTITY get(KEY key, AbstractSearch<?> params) {
    return getEntity(key, params);
  }

  /**
   * // @formatter:off
   * Search a {@link Slice} of {@link ENTITY} entities filtering by subclasses of {@link AbstractSearch} with options to customize response to get only what is needed all the way to database and back.
   * <br>Correct usage of this api should improve the overall performance of the server.
   * // @formatter:on
   *
   * @param params filtering params {@link AbstractSearch}
   * @param pageable paging for the search
   * @return a {@link Slice} {@link ENTITY} which is never null other wise throw an exception if something goes wrong in the process. Ex: no entity found for the given key.
   */
  public Slice<ENTITY> search(AbstractSearch<?> params, Pageable pageable) {
    List<ENTITY> results = findEntities(params, pageable);
    boolean hasNext = pageable.isPaged() && results.size() > pageable.getPageSize();
    return new SliceImpl<>(results, pageable, hasNext);
  }

  /**
   * // @formatter:off
   * Search a {@link Page} of {@link ENTITY} entities filtering by subclasses of {@link AbstractSearch} with options to customize response to get only what is needed all the way to database and back.
   * <br>Correct usage of this api should improve the overall performance of the server.
   * // @formatter:on
   *
   * @param params filtering params {@link AbstractSearch}
   * @param pageable paging for the search
   * @return an updated {@link ENTITY} which is never null other wise throw an exception if something goes wrong in the process. Ex: no entity found for the given key.
   */
  public Page<ENTITY> searchPage(AbstractSearch<?> params, Pageable pageable) {
    return PageableExecutionUtils.getPage(findEntities(params, pageable), pageable, () -> count(params));
  }

  protected List<ENTITY> findEntities(AbstractSearch<?> params, Pageable pageable) {
    return CollectionUtils.isEmpty(params.getExcludes()) ? findAll(params, pageable) : customFindAll(params, pageable);
  }

  protected ENTITY getEntity(KEY key, AbstractSearch<?> params) {
    return CollectionUtils.isEmpty(params.getExcludes()) ? getByKey(key, params) : customGetByKey(key, params);
  }

  protected ENTITY getByKey(KEY key, AbstractSearch<?> params) {
    CriteriaQuery<ENTITY> cq = cb.createQuery(clazz);
    Root<ENTITY> root = cq.from(clazz);
    cq = select(cq, root, params.isDistinct());
    cq = criteria(cq, root, key, params);
    cq = groupBy(cq, root);
    return entityManager.createQuery(cq).getSingleResult();
  }

  protected ENTITY customGetByKey(KEY key, AbstractSearch<?> params) {
    List<String> includes = params.getIncludes();
    CriteriaQuery<Tuple> cq = cb.createTupleQuery();
    Root<ENTITY> root = cq.from(clazz);
    cq = customSelect(cq, root, includes, params.isDistinct());
    cq = tupleCriteria(cq, root, key, params);
    TypedQuery<Tuple> query = entityManager.createQuery(cq);
    return mapRowToObject(includes.toArray(String[]::new), query.getSingleResult().toArray(), clazz);
  }

  protected List<ENTITY> findAll(AbstractSearch<?> params, Pageable pageable) {
    CriteriaQuery<ENTITY> cq = cb.createQuery(clazz);
    Root<ENTITY> root = cq.from(clazz);
    cq = select(cq, root, params.isDistinct());
    cq = criteria(cq, root, params);
    cq = groupBy(cq, root);
    cq = orderBy(cq, root, pageable.getSort());
    TypedQuery<ENTITY> query = entityManager.createQuery(cq);
    query.setFirstResult((int) pageable.getOffset());
    query.setMaxResults(pageable.getPageSize());
    return query.getResultList();
  }

  protected List<ENTITY> customFindAll(AbstractSearch<?> params, Pageable pageable) {
    List<String> includes = params.getIncludes();
    CriteriaQuery<Tuple> cq = cb.createTupleQuery();
    Root<ENTITY> root = cq.from(clazz);
    cq = customSelect(cq, root, includes, params.isDistinct());
    cq = tupleCriteria(cq, root, params);
    TypedQuery<Tuple> query = entityManager.createQuery(cq);
    query.setFirstResult((int) pageable.getOffset());
    query.setMaxResults(pageable.getPageSize());
    return query.getResultList().stream().map(tuple -> mapRowToObject(includes.toArray(String[]::new), tuple.toArray(), clazz)).collect(Collectors.toList());

  }

  protected long count(AbstractSearch<?> params) {
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    Root<ENTITY> root = cq.from(clazz);
    cq = count(cq, root);
    cq = countCriteria(cq, root, params);
    return entityManager.createQuery(cq).getSingleResult();
  }

  protected CriteriaQuery<ENTITY> select(CriteriaQuery<ENTITY> cq, Root<ENTITY> root, boolean distinct) {
    return cq.select(root).distinct(distinct);
  }

  protected CriteriaQuery<Tuple> customSelect(CriteriaQuery<Tuple> cq, Root<ENTITY> root, List<String> includes, boolean distinct) {
    return cq.multiselect(includes.stream().map(root::get).toArray(Selection[]::new)).distinct(distinct);
  }

  protected CriteriaQuery<Long> count(CriteriaQuery<Long> cq, Root<ENTITY> root) {
    return cq.select(cb.count(root));
  }

  @SuppressWarnings("unchecked")
  protected CriteriaQuery<ENTITY> criteria(CriteriaQuery<ENTITY> cq, Root<ENTITY> root, KEY key, AbstractSearch<?> params) {
    return (CriteriaQuery<ENTITY>) criteriaSql(cq, root, key, params);
  }

  @SuppressWarnings("unchecked")
  protected CriteriaQuery<ENTITY> criteria(CriteriaQuery<ENTITY> cq, Root<ENTITY> root, AbstractSearch<?> params) {
    return (CriteriaQuery<ENTITY>) criteriaSql(cq, root, null, params);
  }

  @SuppressWarnings("unchecked")
  protected CriteriaQuery<Tuple> tupleCriteria(CriteriaQuery<Tuple> cq, Root<ENTITY> root, KEY key, AbstractSearch<?> params) {
    return (CriteriaQuery<Tuple>) criteriaSql(cq, root, key, params);
  }


  @SuppressWarnings("unchecked")
  protected CriteriaQuery<Tuple> tupleCriteria(CriteriaQuery<Tuple> cq, Root<ENTITY> root, AbstractSearch<?> params) {
    return (CriteriaQuery<Tuple>) criteriaSql(cq, root, null, params);
  }

  @SuppressWarnings("unchecked")
  protected CriteriaQuery<Long> countCriteria(CriteriaQuery<Long> cq, Root<ENTITY> root, AbstractSearch<?> params) {
    return (CriteriaQuery<Long>) criteriaSql(cq, root, null, params);
  }

  protected CriteriaQuery<ENTITY> orderBy(CriteriaQuery<ENTITY> cq, Root<ENTITY> root, Sort sort) {
    if (sort == null || sort.isUnsorted()) {
      return cq;
    }

    Order[] orders = sort.stream().map(order -> order.getDirection().isAscending() ?
        cb.asc(root.get(order.getProperty())) : cb.desc(root.get(order.getProperty())))
        .toArray(Order[]::new);
    return cq.orderBy(orders);
  }

  protected CriteriaQuery<?> criteriaSql(CriteriaQuery<?> cq, Root<ENTITY> root, KEY key, AbstractSearch<?> params) {
    List<Predicate> predicates = new ArrayList<>();

    if (key != null) {
      Map<String, Object> keyMap = key instanceof Number ? Map.of(idFields.get(0), key) : mapper.convertValue(key, keyMapType);
      predicates.addAll(getKeyPredicates(root, keyMap));
    }

    if (params != null) {
      Map<String, Map<Operator, Object>> paramMap = mapper.convertValue(params, mapType);
      paramMap.entrySet().stream().filter(entry -> fields.contains(entry.getKey())).forEach(entry -> predicates.addAll(getParamPredicates(root, entry)));
    }

    return cq.where(predicates.toArray(Predicate[]::new));
  }

  protected CriteriaQuery<ENTITY> groupBy(CriteriaQuery<ENTITY> cq, Root<ENTITY> root) {
    return cq.groupBy(idFields.stream().map(root::get).collect(Collectors.toUnmodifiableList()));
  }

  private List<Predicate> getKeyPredicates(Root<ENTITY> root, Map<String, Object> keyMap) {
    List<Predicate> predicates = new ArrayList<>(keyMap.size());
    keyMap.forEach((k, v) -> predicates.add(cb.equal(root.get(k), v)));
    return predicates;
  }

  private List<Predicate> getParamPredicates(Root<ENTITY> root, Entry<String, Map<Operator, Object>> criteria) {
    String column = criteria.getKey();
    List<Predicate> predicates = new ArrayList<>(criteria.getValue().size());
    criteria.getValue().forEach((k, v) -> predicates.add(getPredicate(root, column, k, v)));
    return predicates;
  }

  @SuppressWarnings("unchecked")
  @SneakyThrows
  private Predicate getPredicate(Root<ENTITY> root, String column, Operator operator, Object val) {
    Path<?> attribute = root.get(column);
    switch (operator) {
      case eq:
        return resolvePredicate(column, attribute, ComparisonOperator.EQUAL, val);
      case in:
        if (attribute.getJavaType().isEnum()) {
          val = ((Collection<String>) val).stream().map(str -> Enum.valueOf(attribute.getJavaType().asSubclass(Enum.class), str)).collect(Collectors.toList());
        }
        return attribute.in((Collection<?>) val);
      case gt:
        return resolvePredicate(column, attribute, ComparisonOperator.GREATER_THAN, val);
      case lt:
        return resolvePredicate(column, attribute, ComparisonOperator.LESS_THAN, val);
      case gte:
        return resolvePredicate(column, attribute, ComparisonOperator.GREATER_THAN_OR_EQUAL, val);
      case lte:
        return resolvePredicate(column, attribute, ComparisonOperator.LESS_THAN_OR_EQUAL, val);
      case like:
        return cb.like(root.get(column), (String) val);
      case isNull:
        Boolean isNull = (Boolean) val;
        return isNull ? cb.isNull(root.get(column)) : cb.isNotNull(root.get(column));
      default:
        throw new IllegalArgumentException("operator is not supported or wrong value type");
    }
  }

  private Predicate resolvePredicate(String column, Path<?> attribute, ComparisonOperator operator, Object val) {
    return new ComparisonPredicate(cb, operator, attribute, resolveValue(column, attribute, val));
  }

  @SneakyThrows
  public Object resolveValue(String column, Path<?> attribute, Object val) {
    if (Date.class.isAssignableFrom(attribute.getJavaType())) {
      val = javaDateInitializer.get(column).apply(val);
    }
    if (Temporal.class.isAssignableFrom(attribute.getJavaType())) {
      val = javaTemporalInitializer.get(column).apply(val);
    }
    if (attribute.getJavaType().isEnum()) {
      val = Enum.valueOf(attribute.getJavaType().asSubclass(Enum.class), (String) val);
    }
    return val;
  }

  protected ENTITY mapRowToObject(String[] fields, Object[] columns, Class<ENTITY> clazz) {
    if (fields == null || columns == null || columns.length != fields.length) {
      throw new IllegalArgumentException("row columns and object fields does not match");
    }
    Map<String, Object> object = new HashMap<>();
    for (int i = 0; i < columns.length; i++) {
      object.put(fields[i], columns[i]);
    }
    return mapper.convertValue(object, clazz);
  }

  private void createInitializers(Field field) {
    if (Date.class.isAssignableFrom(field.getType())) {
      javaDateInitializer.put(field.getName(), (Object val) -> {
        try {
          return field.getType().getDeclaredConstructor(long.class).newInstance(((Number) val).longValue());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
          throw new RuntimeException("can't instantiate date object");
        }
      });
    }
    if (Temporal.class.isAssignableFrom(field.getType())) {
      javaTemporalInitializer.put(field.getName(), (Object val) -> {
        try {
          Instant instant = Instant.ofEpochMilli(((Number) val).longValue());
          return field.getType().getDeclaredMethod("ofInstant", Instant.class, ZoneId.class).invoke(null, instant, defaultZone);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
          throw new RuntimeException("can't instantiate temporal object");
        }
      });
    }
  }
}
