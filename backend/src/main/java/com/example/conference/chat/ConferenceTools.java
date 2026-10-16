package com.example.conference.chat;

import com.example.conference.model.Speaker;
import com.example.conference.model.Talk;
import com.example.conference.repository.SpeakerRepository;
import com.example.conference.repository.TalkRepository;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

@Singleton
public class ConferenceTools {

    private static final Logger LOG = LoggerFactory.getLogger(ConferenceTools.class);

    private final TalkRepository talks;
    private final SpeakerRepository speakers;

    public ConferenceTools(TalkRepository talks, SpeakerRepository speakers) {
        this.talks = talks;
        this.speakers = speakers;
    }

    public record TalkSummary(long id, String title, String track, String level, String format,
                              String day, String time, String room, List<String> speakers) {
    }

    public record TalkInfo(long id, String title, String description, String track, String level, String format,
                           String day, String time, String room, int floor, List<SpeakerSummary> speakers) {
    }

    public record SpeakerSummary(long id, String name, String jobTitle, String company, String country) {
    }

    public record SpeakerInfo(long id, String name, String jobTitle, String company, String country,
                              String bio, List<TalkSummary> sessions) {
    }

    @Tool("""
            Finds conference sessions (talks, keynotes and workshops). All filters are optional and combined.
            The conference runs on 2026-11-18 (Day 1, Wednesday) and 2026-11-19 (Day 2, Thursday).""")
    public List<TalkSummary> findTalks(
            @P(value = "Day as YYYY-MM-DD", required = false) String day,
            @P(value = "Local time as HH:mm; returns sessions running at that time", required = false) String time,
            @P(value = "Track, e.g. 'Java & JVM', 'AI & Data', 'Cloud & Ops', 'Frontend & UX', 'Architecture', 'Security'", required = false) String track,
            @P(value = "Room name, e.g. 'The Foundry', 'Harbor Hall', 'Lighthouse', 'The Galley'", required = false) String room,
            @P(value = "Text to search in titles, descriptions and speaker names", required = false) String text) {
        Stream<Talk> result = talks.findAllOrderByStartsAt().stream();
        if (present(day)) {
            LocalDate date = LocalDate.parse(day);
            result = result.filter(t -> t.startsAt().toLocalDate().equals(date));
        }
        if (present(time)) {
            LocalTime at = LocalTime.parse(time);
            result = result.filter(t -> !t.startsAt().toLocalTime().isAfter(at) && t.endsAt().toLocalTime().isAfter(at));
        }
        if (present(track)) {
            result = result.filter(t -> contains(t.track(), track));
        }
        if (present(room)) {
            result = result.filter(t -> contains(t.room().name(), room));
        }
        if (present(text)) {
            result = result.filter(t -> contains(t.title(), text) || contains(t.description(), text)
                    || t.speakers().stream().anyMatch(s -> contains(s.name(), text)));
        }
        List<TalkSummary> found = result.map(ConferenceTools::summary).toList();
        LOG.info("Tool findTalks(day={}, time={}, track={}, room={}, text={}) -> {} talks",
                day, time, track, room, text, found.size());
        return found;
    }

    @Tool("Gets the full details of one session, including its description and speakers")
    public TalkInfo getTalk(@P("Talk id") long id) {
        TalkInfo talk = talks.findById(id).map(t -> new TalkInfo(
                t.id(), t.title(), t.description(), t.track(), t.level(), t.format(),
                day(t), time(t), t.room().name(), t.room().floor(),
                t.speakers().stream().map(ConferenceTools::summary).toList()))
                .orElseThrow(() -> notFound("getTalk", "talk", id));
        LOG.info("Tool getTalk(id={}) -> {}", id, talk.title());
        return talk;
    }

    @Tool("Finds speakers. Leave the text empty to list all speakers.")
    public List<SpeakerSummary> findSpeakers(
            @P(value = "Text to search in name, job title, company, country and bio", required = false) String text) {
        List<SpeakerSummary> found = speakers.findAllOrderByName().stream()
                .filter(s -> !present(text) || contains(s.name(), text) || contains(s.jobTitle(), text)
                        || contains(s.company(), text) || contains(s.country(), text) || contains(s.bio(), text))
                .map(ConferenceTools::summary)
                .toList();
        LOG.info("Tool findSpeakers(text={}) -> {} speakers", text, found.size());
        return found;
    }

    @Tool("Gets the full details of one speaker, including their bio and sessions")
    public SpeakerInfo getSpeaker(@P("Speaker id") long id) {
        SpeakerInfo speaker = speakers.findById(id).map(s -> new SpeakerInfo(
                s.id(), s.name(), s.jobTitle(), s.company(), s.country(), s.bio(),
                talks.findAllOrderByStartsAt().stream()
                        .filter(t -> t.speakers().stream().anyMatch(ts -> ts.id().equals(s.id())))
                        .map(ConferenceTools::summary)
                        .toList()))
                .orElseThrow(() -> notFound("getSpeaker", "speaker", id));
        LOG.info("Tool getSpeaker(id={}) -> {}", id, speaker.name());
        return speaker;
    }

    private static TalkSummary summary(Talk t) {
        return new TalkSummary(t.id(), t.title(), t.track(), t.level(), t.format(), day(t), time(t),
                t.room().name(), t.speakers().stream().map(Speaker::name).toList());
    }

    private static SpeakerSummary summary(Speaker s) {
        return new SpeakerSummary(s.id(), s.name(), s.jobTitle(), s.company(), s.country());
    }

    private static String day(Talk t) {
        return t.startsAt().toLocalDate() + " (" + t.startsAt().getDayOfWeek() + ")";
    }

    private static String time(Talk t) {
        return t.startsAt().toLocalTime() + "–" + t.endsAt().toLocalTime();
    }

    private static IllegalArgumentException notFound(String tool, String what, long id) {
        LOG.info("Tool {}(id={}) -> not found", tool, id);
        return new IllegalArgumentException("No " + what + " found with id " + id);
    }

    private static boolean present(String value) {
        return value != null && !value.isBlank();
    }

    private static boolean contains(String value, String search) {
        return value.toLowerCase().contains(search.toLowerCase());
    }
}
