package com.example.conference.repository;

import com.example.conference.model.Speaker;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface SpeakerRepository extends CrudRepository<Speaker, Long> {

    List<Speaker> findAllOrderByName();
}
