package io.github.voduku.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author VuDo
 * @since 1.0.0
 */
@NoRepositoryBean
public interface Repository<ENTITY, KEY> extends JpaRepository<ENTITY, KEY>, CustomizableRepository<ENTITY, KEY> {

}
