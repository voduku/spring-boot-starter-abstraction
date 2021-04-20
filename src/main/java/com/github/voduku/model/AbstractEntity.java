package com.github.voduku.model;

import java.sql.Timestamp;
import javax.persistence.Column;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

/**
 * If you use this class, its property will be hidden from entity and criteria builder
 *
 * @author VuDo
 * @since 1.0.0
 */
@Data
@FieldNameConstants(asEnum = true)
public abstract class AbstractEntity implements BaseEntity {

  @Column(name = "created_at")
  protected Timestamp createdAt;

  @Column(name = "modified_at")
  protected Timestamp modifiedAt;

  @Column(name = "created_by")
  protected String createdBy;

  @Column(name = "modified_by")
  protected String modifiedBy;
}
