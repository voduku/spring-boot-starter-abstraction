package io.github.voduku.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.voduku.model.BaseEntity;
import io.github.voduku.model.AbstractSearch;
import io.github.voduku.model.criteria.Operator;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
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
public abstract class AbstractCriteriaRepository<ENTITY extends BaseEntity, KEY> extends AbstractRepository<ENTITY, KEY> {

  // @formatter:off
  private static final TypeReference<LinkedHashMap<String, Object>> keyMapType = new TypeReference<>() {};
  private static final TypeReference<LinkedHashMap<String, Map<Operator, Object>>> mapType = new TypeReference<>() {};
  // @formatter:on
  protected CriteriaBuilder cb;

  /**
   * Initialize the class with necessary info to perform query creation. Using this should not be too bad since it only run once. This takes ~0.0001 seconds to
   * finish. It could be even faster on cloud server
   */
  public AbstractCriteriaRepository() {
    super();
  }

  /**
   * Initialize the class with necessary info to perform query creation. Using this should not be too bad since it only run once. This constructor is even
   * faster than {@link #AbstractCriteriaRepository()}
   */
  public AbstractCriteriaRepository(Class<ENTITY> entity) {
    super(entity);
  }

  /**
   * Initialize the class with necessary info to perform query creation. Using this should not be too bad since it only run once. This constructor is the
   * fastest but requires you to do tedious works if you have many entities.
   */
  public AbstractCriteriaRepository(Class<ENTITY> entity, List<String> idFields) {
    super(entity, idFields);
  }

  @PostConstruct
  public void initBuilder() {
    this.cb = entityManager.getCriteriaBuilder();
  }

  /**
   * Get an entity with given {@param key} with optional functionalities to optimize database request and response
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
    long count = count(params);
    List<ENTITY> results = count > 0 ? findEntities(params, pageable) : new ArrayList<>();
    return new PageImpl<>(results, pageable, count);
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
    cq = select(cq, root);
    cq = criteria(cq, root, key, params);
    cq = groupBy(cq, root);
    return entityManager.createQuery(cq).getSingleResult();
  }

  protected ENTITY customGetByKey(KEY key, AbstractSearch<?> params) {
    List<String> includes = params.getIncludes();
    CriteriaQuery<Tuple> cq = cb.createTupleQuery();
    Root<ENTITY> root = cq.from(clazz);
    cq = customSelect(cq, root, includes);
    cq = tupleCriteria(cq, root, key, params);
    TypedQuery<Tuple> query = entityManager.createQuery(cq);
    return mapRowToObject(includes.toArray(String[]::new), query.getSingleResult().toArray(), clazz);
  }

  protected List<ENTITY> findAll(AbstractSearch<?> params, Pageable pageable) {
    CriteriaQuery<ENTITY> cq = cb.createQuery(clazz);
    Root<ENTITY> root = cq.from(clazz);
    cq = select(cq, root);
    cq = criteria(cq, root, params);
    cq = groupBy(cq, root);
    cq = orderBy(cq, root, pageable.getSort());
    return entityManager.createQuery(cq).getResultList();
  }

  protected List<ENTITY> customFindAll(AbstractSearch<?> params, Pageable pageable) {
    List<String> includes = params.getIncludes();
    CriteriaQuery<Tuple> cq = cb.createTupleQuery();
    Root<ENTITY> root = cq.from(clazz);
    cq = customSelect(cq, root, includes);
    cq = tupleCriteria(cq, root, params);
    TypedQuery<Tuple> query = entityManager.createQuery(cq);
    query.setFirstResult((int) pageable.getOffset());
    query.setMaxResults(pageable.getPageSize());
    return query.getResultList().stream().map(tuple -> mapRowToObject(includes.toArray(new String[0]), tuple.toArray(), clazz)).collect(Collectors.toList());

  }

  protected long count(AbstractSearch<?> params) {
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    Root<ENTITY> root = cq.from(clazz);
    cq = count(cq, root);
    cq = countCriteria(cq, root, params);
    return entityManager.createQuery(cq).getSingleResult();
  }

  protected CriteriaQuery<ENTITY> select(CriteriaQuery<ENTITY> cq, Root<ENTITY> root) {
    return cq.select(root).distinct(isDistinct());
  }

  protected CriteriaQuery<Tuple> customSelect(CriteriaQuery<Tuple> cq, Root<ENTITY> root, List<String> includes) {
    return cq.multiselect(includes.stream().map(root::get).toArray(Selection[]::new)).distinct(isDistinct());
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
      paramMap.entrySet().forEach(entry -> predicates.addAll(getParamPredicates(root, entry)));
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
        if (attribute.getJavaType().isEnum()) {
          val = Enum.valueOf(attribute.getJavaType().asSubclass(Enum.class), (String) val);
        }
        return cb.equal(attribute, val);
      case in:
        if (attribute.getJavaType().isEnum()) {
          val = ((Collection<String>) val).stream().map(str -> Enum.valueOf(attribute.getJavaType().asSubclass(Enum.class), str)).collect(Collectors.toList());
        }
        return attribute.in(val);
      case gt:
        if (!(val instanceof Number)) {
          throw new IllegalArgumentException("Wrong input type for number or date");
        }
        if (Number.class.isAssignableFrom(attribute.getJavaType())) {
          return cb.gt(root.get(column), (Number) val);
        }
        return cb.greaterThan(root.get(column), new Timestamp(((Number) val).longValue()));
      case lt:
        if (!(val instanceof Number)) {
          throw new IllegalArgumentException("Wrong input type for number or date");
        }
        if (Number.class.isAssignableFrom(attribute.getJavaType())) {
          return cb.lt(root.get(column), (Number) val);
        }
        return cb.lessThan(root.get(column), new Timestamp(((Number) val).longValue()));
      case gte:
        if (!(val instanceof Number)) {
          throw new IllegalArgumentException("Wrong input type for number or date");
        }
        if (Number.class.isAssignableFrom(attribute.getJavaType())) {
          return cb.ge(root.get(column), (Number) val);
        }
        return cb.greaterThanOrEqualTo(root.get(column), new Timestamp(((Number) val).longValue()));
      case lte:
        if (!(val instanceof Number)) {
          throw new IllegalArgumentException("Wrong input type for number or date");
        }
        if (Number.class.isAssignableFrom(attribute.getJavaType())) {
          return cb.lt(root.get(column), (Number) val);
        }
        return cb.lessThanOrEqualTo(root.get(column), new Timestamp(((Number) val).longValue()));
      case like:
        return cb.like(root.get(column), (String) val);
      case isNull:
        Boolean isNull = (Boolean) val;
        return isNull ? cb.isNull(root.get(column)) : cb.isNotNull(root.get(column));
      default:
        throw new IllegalArgumentException("operator is not supported or wrong value type");
    }
  }
}
