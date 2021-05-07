package io.github.voduku.repository;

/**
 * @author VuDo
 * @since 5/5/2021
 */
public class RepositoryImpl<ENTITY, KEY> extends AbstractCriteriaRepository<ENTITY, KEY> {

  public RepositoryImpl(Class<ENTITY> clazz, Class<KEY> key) {
    super(clazz);
  }
}
