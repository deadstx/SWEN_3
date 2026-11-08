package at.technikum.swen3backend.Mapper;

import java.util.List;

public abstract class EntityMapper<E, D> {

    public abstract D toDto(E entity);

    public List<D> toDtoList(List<E> entities) {
        return entities.stream()
                .map(this::toDto)
                .toList();
    }
}