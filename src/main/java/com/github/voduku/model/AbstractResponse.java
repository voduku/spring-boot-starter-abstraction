package com.github.voduku.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;

/**
 * @author VuDo
 * @since 1.0.0
 */
@Value
@NonFinal
@SuperBuilder(toBuilder = true)
@JsonInclude(Include.NON_NULL)
public abstract class AbstractResponse {

  Long createdAt;
  Long modifiedAt;
  String createdBy;
  String modifiedBy;
}
