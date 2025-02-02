package com.bank.credits.entity.mapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public interface BaseMapper<E, D> {

    D toDTO(E entity);

    E toEntity(D dto);

    List<D> toDTOs(List<E> entityList);

    List<E> toEntities(List<D> dtoList);

    default Page<D> toDTOs(Page<E> entities) {
        List<E> content = entities.getContent();
        return new PageImpl<>(toDTOs(content), entities.getPageable(), entities.getTotalElements());
    }
}
