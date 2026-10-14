package com.example.conference.room;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface RoomRepository extends CrudRepository<Room, Long> {

    List<Room> findAllOrderById();
}
