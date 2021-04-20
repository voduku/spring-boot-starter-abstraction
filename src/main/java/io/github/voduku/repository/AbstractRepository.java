package io.github.voduku.repository;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.voduku.model.AbstractSearch;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author VuDo
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractRepository<ENTITY, KEY> implements CustomizableRepository<ENTITY, KEY> {

  protected static final String EMPTY = "";
  protected static final String DISTINCT = " distinct ";
  protected static final String COUNT = " count ";
  protected static final String ORDER_BY = " order by ";
  protected static final String GROUP_BY = " group by ";
  protected static final String OBJECT_REFERENCE = "o.";
  protected static final String PARAM_REFERENCE = ":";
  protected static final String SELECT_QUERY = "select %s o from %s o ";
  protected static final String CUSTOM_SELECT_QUERY = "select %s %s from %s o ";
  protected static final String COUNT_QUERY = "select %s(%s %s) from %s o ";
  protected static final String EQUAL = " = ";
  protected static final ObjectMapper mapper = new ObjectMapper()
      .setSerializationInclusion(Include.NON_NULL)
      .disable(FAIL_ON_UNKNOWN_PROPERTIES).disable(FAIL_ON_EMPTY_BEANS)
      .enable(WRITE_DATES_AS_TIMESTAMPS);
  private static final Set<String> RESERVED_SEARCH_FIELDS = Arrays.stream(AbstractSearch.Fields.values()).map(Enum::name).collect(Collectors.toSet());
  private static final TypeReference<LinkedHashMap<String, Object>> mapType = new TypeReference<>() {
  };
  protected final Class<ENTITY> clazz;
  protected final String entityName;
  protected final List<String> idFields;
  @PersistenceContext
  protected EntityManager entityManager;
  @Setter
  @Getter
  protected boolean distinct = false;

  @SneakyThrows
  @SuppressWarnings("unchecked")
  public AbstractRepository() {
    // This line takes ~0.00005 seconds. It could be even faster on aws.

    this.clazz = (Class<ENTITY>) Objects.requireNonNull(GenericTypeResolver.resolveTypeArguments(getClass(), AbstractRepository.class))[0];
    this.entityName = clazz.getSimpleName();
    this.idFields = Arrays.stream(clazz.getDeclaredFields()).filter(field -> Objects.nonNull(field.getAnnotation(Id.class))).map(Field::getName)
        .collect(Collectors.toUnmodifiableList());
  }

  public AbstractRepository(Class<ENTITY> clazz) {
    this.clazz = clazz;
    this.entityName = clazz.getSimpleName();
    this.idFields = Arrays.stream(clazz.getDeclaredFields()).filter(field -> Objects.nonNull(field.getAnnotation(Id.class))).map(Field::getName)
        .collect(Collectors.toUnmodifiableList());
  }

  public AbstractRepository(Class<ENTITY> clazz, List<String> idFields) {
    this.clazz = clazz;
    this.entityName = clazz.getSimpleName();
    this.idFields = idFields;
  }

  @Override
  public ENTITY get(KEY key, AbstractSearch<?> params) {
    return getEntity(key, params);
  }

  @Override
  public Slice<ENTITY> search(AbstractSearch<?> params, Pageable pageable) {
    List<ENTITY> results = findEntities(params, pageable);
    boolean hasNext = pageable.isPaged() && results.size() > pageable.getPageSize();
    return new SliceImpl<>(results, pageable, hasNext);
  }

  @Override
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
    String sql = selectSql();
    sql += criteria(key, params);
    TypedQuery<ENTITY> query = entityManager.createQuery(sql, clazz);
    addParams(query, key, params);
    return query.getSingleResult();
  }

  protected ENTITY customGetByKey(KEY key, AbstractSearch<?> params) {
    List<String> includes = params.getIncludes();
    String sql = customSelectSql(includes);
    sql += criteria(key, params);
    if (includes.size() == 1) {
      TypedQuery<Object> query = entityManager.createQuery(sql, Object.class);
      addParams(query, key, params);
      Object column = query.getSingleResult();
      return mapRowToObject(includes.toArray(new String[0]), column, clazz);
    } else {
      TypedQuery<Object[]> query = entityManager.createQuery(sql, Object[].class);
      addParams(query, key, params);
      Object[] columns = query.getSingleResult();
      return mapRowToObject(includes.toArray(new String[0]), columns, clazz);
    }
  }

  protected List<ENTITY> findAll(AbstractSearch<?> params, Pageable pageable) {
    String sql = selectSql();
    sql += criteria(params, pageable.getSort());
    TypedQuery<ENTITY> query = entityManager.createQuery(sql, clazz);
    addParams(query, params);
    query.setFirstResult((int) pageable.getOffset());
    query.setMaxResults(pageable.getPageSize());
    return query.getResultList();
  }

  protected List<ENTITY> customFindAll(AbstractSearch<?> params, Pageable pageable) {
    List<String> includes = params.getIncludes();
    String sql = customSelectSql(includes);
    sql += criteria(params, pageable.getSort());
    if (includes.size() == 1) {
      TypedQuery<Object> query = entityManager.createQuery(sql, Object.class);
      addParams(query, params);
      query.setFirstResult((int) pageable.getOffset());
      query.setMaxResults(pageable.getPageSize());
      return query.getResultList().stream().map(column -> mapRowToObject(includes.toArray(new String[0]), column, clazz)).collect(Collectors.toList());
    } else {
      TypedQuery<Object[]> query = entityManager.createQuery(sql, Object[].class);
      addParams(query, params);
      query.setFirstResult((int) pageable.getOffset());
      query.setMaxResults(pageable.getPageSize());
      return query.getResultList().stream().map(columns -> mapRowToObject(includes.toArray(new String[0]), columns, clazz)).collect(Collectors.toList());
    }

  }

  protected long count(AbstractSearch<?> params) {
    String sql = countSql();
    sql += countCriteria(params);
    TypedQuery<Long> query = entityManager.createQuery(sql, Long.class);
    addParams(query, params);
    return query.getSingleResult();
  }

  protected String criteria(KEY key, AbstractSearch<?> params) {
    return criteriaSql(key, params) + groupBy(idFields);
  }

  protected String criteria(AbstractSearch<?> params, Sort sort) {
    return criteriaSql(params) + groupBy(idFields, sort) + orderBy(sort);
  }

  protected String countCriteria(AbstractSearch<?> params) {
    return criteriaSql(params);
  }

  protected String selectSql() {
    return isDistinct() ? selectDistinct() : select();
  }

  protected String customSelectSql(List<String> includes) {
    return isDistinct() ? selectDistinct(includes) : select(includes);
  }

  protected String countSql() {
    return isDistinct() ? countDistinct() : count();
  }

  protected String criteriaSql(KEY key, AbstractSearch<?> params) {
    // @formatter:off
    Map<String, Object> keyMap = key instanceof Number ? new HashMap<>(){{ put(idFields.get(0), key); }} : mapper.convertValue(key, mapType);
    // @formatter:on
    Map<String, Object> paramMap = mapper.convertValue(params, mapType);
    paramMap.forEach(keyMap::putIfAbsent);
    return criteriaSql(keyMap);
  }

  protected String criteriaSql(AbstractSearch<?> params) {
    return criteriaSql(mapper.convertValue(params, mapType));
  }

  protected String criteriaSql(Map<String, Object> params) {
    List<String> criteria = new ArrayList<>();
    for (Entry<String, Object> entry : params.entrySet()) {
      if (RESERVED_SEARCH_FIELDS.contains(entry.getKey())) {
        continue;
      }

      criteria.add(OBJECT_REFERENCE + entry.getKey() + EQUAL + PARAM_REFERENCE + entry.getKey());
    }
    return criteria.isEmpty() ? EMPTY : " where " + String.join(" and ", criteria);
  }

  protected void addParams(Query query, AbstractSearch<?> params) {
    addParams(query, null, params);
  }

  protected void addParams(Query query, KEY key, AbstractSearch<?> params) {
    // @formatter:off
    Map<String, Object> keyMap = key instanceof Number ? new HashMap<>(){{ put(idFields.get(0), key); }} : mapper.convertValue(key, mapType);
    // @formatter:on
    LinkedHashMap<String, Object> paramMap = mapper.convertValue(params, mapType);
    if (keyMap != null) {
      for (Entry<String, Object> entry : keyMap.entrySet()) {
        query.setParameter(entry.getKey(), entry.getValue());
      }
    }
    for (Entry<String, Object> entry : paramMap.entrySet()) {
      if (RESERVED_SEARCH_FIELDS.contains(entry.getKey())) {
        continue;
      }
      query.setParameter(entry.getKey(), entry.getValue());
    }
  }

  protected String select() {
    return String.format(SELECT_QUERY, EMPTY, entityName);
  }

  protected String selectDistinct() {
    return String.format(SELECT_QUERY, DISTINCT, entityName);
  }

  protected String select(Collection<String> fields) {
    if (CollectionUtils.isEmpty(fields)) {
      return select();
    }
    return String.format(CUSTOM_SELECT_QUERY, EMPTY, getRequiredColumns(fields), entityName);
  }

  protected String selectDistinct(Collection<String> fields) {
    if (CollectionUtils.isEmpty(fields)) {
      return select();
    }
    return String.format(CUSTOM_SELECT_QUERY, DISTINCT, getRequiredColumns(fields), entityName);
  }

  protected String count() {
    return String.format(COUNT_QUERY, COUNT, EMPTY, getRequiredColumns(idFields), entityName);
  }

  protected String countDistinct() {
    return String.format(COUNT_QUERY, COUNT, DISTINCT, getRequiredColumns(idFields), entityName);
  }

  private String getRequiredColumns(Collection<String> fields) {
    return StringUtils.collectionToCommaDelimitedString(prependObjectReferenceToFields(fields));
  }

  protected String orderBy(Sort sort) {
    if (sort == null || sort.isUnsorted()) {
      return EMPTY;
    }
    return ORDER_BY + StringUtils.collectionToCommaDelimitedString(StreamSupport.stream(sort.spliterator(), false)
        .map(o -> o.getProperty() + " " + o.getDirection())
        .collect(Collectors.toList()));
  }

  protected String groupBy(Collection<String> fields) {
    return GROUP_BY + StringUtils.collectionToCommaDelimitedString(prependObjectReferenceToFields(fields));
  }

  protected String groupBy(Collection<String> fields, Sort sort) {
    if (sort == null || sort.isUnsorted()) {
      return EMPTY;
    }
    Set<String> prepended = prependObjectReferenceToFields(fields);
    prepended.addAll(orderFields(sort));
    return GROUP_BY + StringUtils.collectionToCommaDelimitedString(prepended);
  }

  protected ENTITY mapRowToObject(String[] fields, Object column, Class<ENTITY> clazz) {
    return mapRowToObject(fields, new Object[]{column}, clazz);
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

  private Set<String> orderFields(Sort sort) {
    if (sort == null || sort.isUnsorted()) {
      return null;
    }
    return StreamSupport.stream(sort.spliterator(), false)
        .map(o -> OBJECT_REFERENCE + o.getProperty())
        .collect(Collectors.toSet());
  }

  private Set<String> prependObjectReferenceToFields(Collection<String> fields) {
    if (CollectionUtils.isEmpty(fields)) {
      return new HashSet<>();
    }
    Set<String> columns = new LinkedHashSet<>();
    for (String field : fields) {
      columns.add(OBJECT_REFERENCE + field);
    }
    return columns;
  }
}
