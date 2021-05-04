package io.github.voduku.model.criteria;

import java.util.Collection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * @author VuDo
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Accessors(fluent = true, chain = true)
public class SearchCriteria<T> {

  T eq;
  Collection<? extends T> in;
  Boolean isNull;

  public T getEq() {
    return this.eq;
  }

  public SearchCriteria<T> setEq(T eq) {
    this.eq = eq;
    return this;
  }

  public Collection<? extends T> getIn() {
    return this.in;
  }

  public SearchCriteria<T> setIn(Collection<? extends T> in) {
    this.in = in;
    return this;
  }

  public Boolean getIsNull() {
    return this.isNull;
  }

  public SearchCriteria<T> setIsNull(Boolean isNull) {
    this.isNull = isNull;
    return this;
  }
}

