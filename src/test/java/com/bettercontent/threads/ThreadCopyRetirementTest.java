package com.bettercontent.threads;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class ThreadCopyRetirementTest {
    @Test
    void networkOnlyAcceptsReaderActions() {
        assertTrue(ThreadNetwork.Action.isReaderAction("open"));
        assertTrue(ThreadNetwork.Action.isReaderAction("read"));
        assertFalse(ThreadNetwork.Action.isReaderAction("issue"));
        assertFalse(ThreadNetwork.Action.isReaderAction("grant"));
    }
}
