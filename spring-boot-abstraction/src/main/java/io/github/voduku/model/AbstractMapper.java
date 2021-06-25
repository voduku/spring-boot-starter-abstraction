package io.github.voduku.model;

import org.mapstruct.MappingTarget;

/**
 * @author VuDo
 * @since 1.0.0
 */
public abstract class AbstractMapper<REQUEST, RESPONSE, ENTITY, KEY> {

  public abstract ENTITY toEntity(KEY key, REQUEST request);

  public abstract ENTITY updateEntity(REQUEST request, @MappingTarget ENTITY entity);

  public abstract RESPONSE toResponse(ENTITY entity);
}
