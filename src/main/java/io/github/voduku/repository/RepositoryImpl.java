package io.github.voduku.repository;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.voduku.model.AbstractSearch;
import io.github.voduku.model.criteria.SearchCriteria;
import io.github.voduku.repository.ExtendedValueHandlerFactory.EnumValueHandler;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.hibernate.query.criteria.internal.ValueHandlerFactory.ValueHandler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * // @formatter:off Provide a an approach which utilize Criteria APIs to create HQL queries and perform executions You can override any method to change query
 * creation and result mapping processes // @formatter:on
 *
 * @param <ENTITY> Entity Type
 * @param <KEY>    Entity Key. This could be single column or composite key.
 * @author VuDo
 * @since 1.0.0
 */
@Slf4j
@Getter
@Setter
@Transactional
public class RepositoryImpl<ENTITY, KEY extends Serializable> extends SimpleJpaRepository<ENTITY, KEY> implements
    Repository<ENTITY, KEY> {

  // @formatter:off
  protected static final ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL)
      .disable(FAIL_ON_UNKNOWN_PROPERTIES)
      .disable(FAIL_ON_EMPTY_BEANS)
      .enable(WRITE_DATES_AS_TIMESTAMPS);
  private static final TypeReference<LinkedHashMap<String, Object>> keyMapType = new TypeReference<>() {};
  private static final TypeReference<LinkedHashMap<String, ? extends SearchCriteria<?>>> mapType = new TypeReference<>() {};
  private static final ZoneId defaultZone = ZoneId.systemDefault();
  // @formatter:on
  private final Class<ENTITY> clazz;
  private final String entityName;
  private final List<String> idFields = new ArrayList<>();
  private final Map<String, ValueHandler<?>> fields = new HashMap<>();
  private final EntityManager em;
  private final CriteriaBuilderImpl cb;

  /**
   * Initialize the class with necessary info to perform query creation. Using this should not be too bad since it only run once. This takes ~0.0001 seconds to
   * finish. It could be even faster on cloud server
   */
  public RepositoryImpl(JpaEntityInformation<ENTITY, ?> entityInformation, EntityManager em) {
    super(entityInformation, em);
    this.em = em;
    this.cb = (CriteriaBuilderImpl) em.getCriteriaBuilder();
    this.clazz = entityInformation.getJavaType();
    this.entityName = entityInformation.getEntityName();
    entityInformation.getIdAttributeNames().forEach(idFields::add);
    Arrays.stream(clazz.getDeclaredFields()).forEach(field ->
        this.fields.put(field.getName(), !field.getType().isEnum() ?
            ExtendedValueHandlerFactory.determineAppropriateHandler(field.getType())
            : new EnumValueHandler(field.getType())
        ));
  }

  public Class<ENTITY> getEntityClass() {
    return this.clazz;
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
   * // @formatter:off Search a {@link Slice} of {@link ENTITY} entities filtering by subclasses of {@link AbstractSearch} with options to customize response to
   * get only what is needed all the way to database and back.
   * <br>Correct usage of this api should improve the overall performance of the server.
   * // @formatter:on
   *
   * @param params   filtering params {@link AbstractSearch}
   * @param pageable paging for the search
   * @return a {@link Slice} {@link ENTITY} which is never null other wise throw an exception if something goes wrong in the process. Ex: no entity found for
   * the given key.
   */
  public Slice<ENTITY> search(AbstractSearch<?> params, Pageable pageable) {
    List<ENTITY> results = findEntities(params, pageable);
    boolean hasNext = pageable.isPaged() && results.size() > pageable.getPageSize();
    return new SliceImpl<>(results, pageable, hasNext);
  }

  /**
   * // @formatter:off Search a {@link Page} of {@link ENTITY} entities filtering by subclasses of {@link AbstractSearch} with options to customize response to
   * get only what is needed all the way to database and back.
   * <br>Correct usage of this api should improve the overall performance of the server.
   * // @formatter:on
   *
   * @param params   filtering params {@link AbstractSearch}
   * @param pageable paging for the search
   * @return an updated {@link ENTITY} which is never null other wise throw an exception if something goes wrong in the process. Ex: no entity found for the
   * given key.
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
    return em.createQuery(cq).getSingleResult();
  }

  protected ENTITY customGetByKey(KEY key, AbstractSearch<?> params) {
    Set<String> includes = params.getIncludes();
    CriteriaQuery<Tuple> cq = cb.createTupleQuery();
    Root<ENTITY> root = cq.from(clazz);
    cq = customSelect(cq, root, includes, params.isDistinct());
    cq = tupleCriteria(cq, root, key, params);
    TypedQuery<Tuple> query = em.createQuery(cq);
    return mapRowToObject(includes, query.getSingleResult(), clazz);
  }

  protected List<ENTITY> findAll(AbstractSearch<?> params, Pageable pageable) {
    CriteriaQuery<ENTITY> cq = cb.createQuery(clazz);
    Root<ENTITY> root = cq.from(clazz);
    cq = select(cq, root, params.isDistinct());
    cq = criteria(cq, root, params);
    cq = groupBy(cq, root);
    cq = orderBy(cq, root, pageable.getSort());
    TypedQuery<ENTITY> query = em.createQuery(cq);
    query.setFirstResult((int) pageable.getOffset());
    query.setMaxResults(pageable.getPageSize());
    return query.getResultList();
  }

  protected List<ENTITY> customFindAll(AbstractSearch<?> params, Pageable pageable) {
    Set<String> includes = params.getIncludes();
    CriteriaQuery<Tuple> cq = cb.createTupleQuery();
    Root<ENTITY> root = cq.from(clazz);
    cq = customSelect(cq, root, includes, params.isDistinct());
    cq = tupleCriteria(cq, root, params);
    TypedQuery<Tuple> query = em.createQuery(cq);
    query.setFirstResult((int) pageable.getOffset());
    query.setMaxResults(pageable.getPageSize());
    return query.getResultList().stream()
        .map(tuple -> mapRowToObject(includes, tuple, clazz))
        .collect(Collectors.toList());

  }

  protected long count(AbstractSearch<?> params) {
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    Root<ENTITY> root = cq.from(clazz);
    cq = count(cq, root);
    cq = countCriteria(cq, root, params);
    return em.createQuery(cq).getSingleResult();
  }

  protected CriteriaQuery<ENTITY> select(CriteriaQuery<ENTITY> cq, Root<ENTITY> root, boolean distinct) {
    return cq.select(root).distinct(distinct);
  }

  protected CriteriaQuery<Tuple> customSelect(CriteriaQuery<Tuple> cq, Root<ENTITY> root, Set<String> includes, boolean distinct) {
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

  @SneakyThrows
  protected CriteriaQuery<?> criteriaSql(CriteriaQuery<?> cq, Root<ENTITY> root, KEY key, AbstractSearch<?> params) {
    List<Predicate> predicates = new ArrayList<>();

    if (key != null) {
      Map<String, Object> keyMap = key instanceof Number ? Map.of(idFields.get(0), key) : mapper.convertValue(key, keyMapType);
      predicates.addAll(getKeyPredicates(root, keyMap));
    }

    if (params != null) {
      params.getCriteria().forEach((column, handler) -> {
        if (handler != null) {
          predicates.addAll(handler.handle(cb, root.get(column)));
        }
      });
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

  @SuppressWarnings("all")
  protected ENTITY mapRowToObject(Set<String> fields, Tuple tuple, Class<ENTITY> clazz) {
    Object[] columns = tuple.toArray();
    if (fields == null || columns == null || columns.length != fields.size()) {
      throw new IllegalArgumentException("row columns and object fields does not match");
    }
    Map<String, Object> object = new HashMap<>();
    int i = 0;
    for (String field : fields) {
      object.put(field, columns[i++]);
    }
    return mapper.convertValue(object, clazz);
  }


}
