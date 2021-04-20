package com.github.voduku.model;

import java.sql.Timestamp;

/**
 * @author VuDo
 * @since 1.0.0
 */
public interface BaseEntity {

  Timestamp getCreatedAt();

  void setCreatedAt(Timestamp timestamp);

  Timestamp getModifiedAt();

  void setModifiedAt(Timestamp timestamp);
}
