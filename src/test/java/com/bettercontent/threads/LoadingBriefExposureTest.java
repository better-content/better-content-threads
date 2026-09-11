package com.bettercontent.threads;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

final class LoadingBriefExposureTest {
    private LoadingBriefSession session() {
        return new LoadingBriefSession(List.of(LoadingBriefs.FALLBACK), new LoadingBriefRotation.State(Set.of(), ""));
    }

    @Test void quickJoinOrBrowsingDoesNotConsumeTheOnlyChanceToReadALesson() {
        var session = session();
        session.observe(0);
        session.observe(100);
        session.move(1);
        assertTrue(session.viewed().isEmpty());
        var saved = LoadingBriefRotation.commit(new LoadingBriefRotation.State(Set.of(), ""), session.viewed(), List.of(LoadingBriefs.FALLBACK));
        assertTrue(saved.seen().isEmpty());
    }

    @Test void sustainedRenderedExposureRecordsTheLessonExactlyOnce() {
        var session = session();
        for (long time = 0; time <= 10_000; time += 100) session.observe(time);
        assertEquals(List.of("threads"), session.viewed());
    }

    @Test void loadingStallAndClockRollbackDoNotCountAsReading() {
        var session = session();
        session.observe(10_000);
        session.observe(100_000);
        session.observe(0);
        assertTrue(session.viewed().isEmpty());
    }
}
