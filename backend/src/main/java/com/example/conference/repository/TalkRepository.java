package com.example.conference.repository;

import com.example.conference.model.Talk;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.GenericRepository;

import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface TalkRepository extends GenericRepository<Talk, Long> {

    @Join(value = "room", type = Join.Type.LEFT_FETCH)
    @Join(value = "speakers", type = Join.Type.LEFT_FETCH)
    List<Talk> findAllOrderByStartsAt();

    @Join(value = "room", type = Join.Type.LEFT_FETCH)
    @Join(value = "speakers", type = Join.Type.LEFT_FETCH)
    Optional<Talk> findById(Long id);
}
