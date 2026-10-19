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

import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

/**
 * Read-only tools the assistant can call to look up the conference programme.
 */
@Singleton
public class ConferenceTools {

    private static final Logger LOG = LoggerFactory.getLogger(ConferenceTools.class);

    public record TalkSummary(long id, String title, String track, String format, String level,
                              String day, String start, String end, String room, List<String> speakers) {
    }

    public record TalkDetails(long id, String title, String description, String track, String format, String level,
                              String day, String start, String end, String room, int roomFloor,
                              List<SpeakerSummary> speakers) {
    }

    public record SpeakerSummary(long id, String name, String jobTitle, String company, String country) {
    }

    public record SpeakerDetails(long id, String name, String jobTitle, String company, String country, String bio,
                                 List<TalkSummary> talks) {
    }

    private final TalkRepository talks;
    private final SpeakerRepository speakers;

    public ConferenceTools(TalkRepository talks, SpeakerRepository speakers) {
        this.talks = talks;
        this.speakers = speakers;
    }

    @Tool("Find talks, keynotes and workshops. All filters are optional and combined; call without filters to get the whole programme.")
    public List<TalkSummary> findTalks(
            @P(value = "Day as a date: 2026-11-18 (day 1) or 2026-11-19 (day 2)", required = false) String day,
            @P(value = "Time as HH:mm; returns sessions running at that time", required = false) String time,
            @P(value = "Track, e.g. 'Java & JVM', 'AI & Data', 'Cloud & Ops', 'Frontend & UX', 'Architecture', 'Security'", required = false) String track,
            @P(value = "Room name: The Foundry, Harbor Hall, Lighthouse or The Galley", required = false) String room,
            @P(value = "Text to search in titles, descriptions and speaker names", required = false) String text) {
        List<TalkSummary> result = talks.findAllOrderByStartsAt().stream()
                .filter(t -> isBlank(day) || t.startsAt().toLocalDate().toString().equals(day.trim()))
                .filter(t -> isBlank(time) || runsAt(t, LocalTime.parse(time.trim())))
                .filter(t -> isBlank(track) || contains(t.track(), track))
                .filter(t -> isBlank(room) || contains(t.room().name(), room))
                .filter(t -> isBlank(text) || matches(t, text))
                .map(ConferenceTools::summary)
                .toList();
        LOG.info("Tool findTalks(day={}, time={}, track={}, room={}, text={}) -> {} talks",
                day, time, track, room, text, result.size());
        return result;
    }

    @Tool("Get the full details of one talk, including its description and speakers")
    public TalkDetails getTalk(@P("Talk id") long id) {
        TalkDetails result = talks.findById(id).map(ConferenceTools::details).orElse(null);
        LOG.info("Tool getTalk(id={}) -> {}", id, result == null ? "not found" : result.title());
        return result;
    }

    @Tool("Find speakers by name, job title, company or country. Call without text to list all speakers.")
    public List<SpeakerSummary> findSpeakers(
            @P(value = "Text to search in name, job title, company and country", required = false) String text) {
        List<SpeakerSummary> result = speakers.findAllOrderByName().stream()
                .filter(s -> isBlank(text) || contains(s.name(), text) || contains(s.jobTitle(), text)
                        || contains(s.company(), text) || contains(s.country(), text))
                .map(ConferenceTools::summary)
                .toList();
        LOG.info("Tool findSpeakers(text={}) -> {} speakers", text, result.size());
        return result;
    }

    @Tool("Get the full details of one speaker, including their bio and their sessions")
    public SpeakerDetails getSpeaker(@P("Speaker id") long id) {
        SpeakerDetails result = speakers.findById(id)
                .map(s -> new SpeakerDetails(s.id(), s.name(), s.jobTitle(), s.company(), s.country(), s.bio(),
                        talks.findAllOrderByStartsAt().stream()
                                .filter(t -> t.speakers().stream().anyMatch(sp -> sp.id().equals(s.id())))
                                .map(ConferenceTools::summary)
                                .toList()))
                .orElse(null);
        LOG.info("Tool getSpeaker(id={}) -> {}", id,
                result == null ? "not found" : result.name() + ", " + result.talks().size() + " talks");
        return result;
    }

    private static TalkSummary summary(Talk t) {
        return new TalkSummary(t.id(), t.title(), t.track(), t.format(), t.level(),
                t.startsAt().toLocalDate().toString(), t.startsAt().toLocalTime().toString(),
                t.endsAt().toLocalTime().toString(), t.room().name(),
                t.speakers().stream().map(Speaker::name).toList());
    }

    private static TalkDetails details(Talk t) {
        return new TalkDetails(t.id(), t.title(), t.description(), t.track(), t.format(), t.level(),
                t.startsAt().toLocalDate().toString(), t.startsAt().toLocalTime().toString(),
                t.endsAt().toLocalTime().toString(), t.room().name(), t.room().floor(),
                t.speakers().stream().map(ConferenceTools::summary).toList());
    }

    private static SpeakerSummary summary(Speaker s) {
        return new SpeakerSummary(s.id(), s.name(), s.jobTitle(), s.company(), s.country());
    }

    private static boolean runsAt(Talk t, LocalTime time) {
        return !t.startsAt().toLocalTime().isAfter(time) && t.endsAt().toLocalTime().isAfter(time);
    }

    private static boolean matches(Talk t, String text) {
        return contains(t.title(), text) || contains(t.description(), text)
                || t.speakers().stream().anyMatch(s -> contains(s.name(), text));
    }

    private static boolean contains(String value, String text) {
        return value.toLowerCase(Locale.ROOT).contains(text.trim().toLowerCase(Locale.ROOT));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
