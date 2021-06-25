package io.github.voduku.model.criteria;

import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.springframework.util.StringUtils;

/**
 * @author VuDo
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Accessors(fluent = true, chain = true)
public class StringCriteria extends SearchCriteria<String> {

  String like;
  @Default
  boolean caseSensitive = true;

  public String getLike() {
    return like;
  }

  public StringCriteria setLike(String like) {
    this.like = like;
    return this;
  }

  public boolean getCaseSensitive() {
    return caseSensitive;
  }

  public StringCriteria setCaseSensitive(boolean caseSensitive) {
    this.caseSensitive = caseSensitive;
    return this;
  }

  @Override
  public List<Predicate> handle(CriteriaBuilder cb, Expression<String> expression) {
    if (!caseSensitive) {
      expression = cb.upper(expression);
    }
    List<Predicate> predicates = handleCriteria(cb, expression);
    if (StringUtils.hasLength(like)) {
      predicates.add(cb.like(expression, caseSensitive ? like : like.toUpperCase()));
    }
    return predicates;
  }

  @Override
  protected Predicate handleEqual(CriteriaBuilder cb, Expression<String> expression) {
    return cb.equal(expression, caseSensitive ? eq : eq.toUpperCase());
  }
}
