package io.github.voduku.model;

import java.sql.Timestamp;
import java.util.Calendar;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

/**
 * Listener to update metadata
 * You should do @EntityListeners(Metadata.class) in order for them to be auto updated
 *
 * @author VuDo
 * @since 3/31/2021
 */
public class Metadata {

  @PrePersist
  public void onCreate(Object object) {
    if (!(object instanceof AbstractEntity)) {
      return;
    }
    AbstractEntity entity = (AbstractEntity) object;
    entity.setCreatedBy(getActorId());
    entity.setCreatedAt(new Timestamp(Calendar.getInstance().getTimeInMillis()));
    entity.setModifiedBy(getActorId());
    entity.setModifiedAt(new Timestamp(Calendar.getInstance().getTimeInMillis()));
  }

  @PreUpdate
  public void onUpdate(Object object) {
    if (!(object instanceof AbstractEntity)) {
      return;
    }
    AbstractEntity entity = (AbstractEntity) object;
    entity.setModifiedBy(getActorId());
    entity.setModifiedAt(new Timestamp(Calendar.getInstance().getTimeInMillis()));
  }

  private String getActorId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated()) {
      return StringUtils.hasLength(auth.getName()) ? auth.getName() : "USER";
    } else {
      return "ANONYMOUS";
    }
  }
}
