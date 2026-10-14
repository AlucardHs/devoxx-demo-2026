package com.example.conference.controller;

import com.example.conference.model.Room;
import com.example.conference.repository.RoomRepository;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import java.util.List;

@Controller("/api/rooms")
public class RoomController {

    private final RoomRepository rooms;

    public RoomController(RoomRepository rooms) {
        this.rooms = rooms;
    }

    @Get
    public List<Room> list() {
        return rooms.findAllOrderById();
    }
}
