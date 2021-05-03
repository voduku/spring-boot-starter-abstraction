package io.github.voduku.model.criteria;

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
public class StringCriteria extends SearchCriteria<String> {

  String like;

  public String getLike() {
    return like;
  }

  public StringCriteria setLike(String like) {
    this.like = like;
    return this;
  }
}
