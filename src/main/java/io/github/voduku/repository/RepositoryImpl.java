package io.github.voduku.repository;

import java.io.Serializable;
import javax.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author VuDo
 * @since 5/5/2021
 */
@NoRepositoryBean
public class RepositoryImpl<ENTITY, KEY extends Serializable> extends AbstractCriteriaRepository<ENTITY, KEY> {

  public RepositoryImpl(JpaEntityInformation<ENTITY, ?> entityInformation, EntityManager em) {
    super(entityInformation, em);
  }

}
