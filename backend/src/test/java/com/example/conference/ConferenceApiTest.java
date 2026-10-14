package com.example.conference;

import com.example.conference.model.Room;
import com.example.conference.model.Speaker;
import com.example.conference.model.Talk;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
class ConferenceApiTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void listsAllTalksWithRoomAndSpeakers() {
        List<Talk> talks = client.toBlocking().retrieve(HttpRequest.GET("/api/talks"), Argument.listOf(Talk.class));

        assertEquals(37, talks.size());
        assertTrue(talks.stream().allMatch(t -> t.room() != null && !t.speakers().isEmpty()));
    }

    @Test
    void returnsCoPresentedTalkWithBothSpeakers() {
        Talk keynote = client.toBlocking().retrieve(HttpRequest.GET("/api/talks/1"), Talk.class);

        assertEquals("The Foundry", keynote.room().name());
        assertEquals(2, keynote.speakers().size());
    }

    @Test
    void listsSpeakersAndRooms() {
        List<Speaker> speakers = client.toBlocking().retrieve(HttpRequest.GET("/api/speakers"), Argument.listOf(Speaker.class));
        List<Room> rooms = client.toBlocking().retrieve(HttpRequest.GET("/api/rooms"), Argument.listOf(Room.class));

        assertEquals(24, speakers.size());
        assertEquals(4, rooms.size());
    }
}
