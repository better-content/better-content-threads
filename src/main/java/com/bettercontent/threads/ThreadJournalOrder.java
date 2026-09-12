package com.bettercontent.threads;
import java.util.*;
/** Selection is keyed by identity; filter changes never constrain the unread reading sequence. */
final class ThreadJournalOrder {
 private ThreadJournalOrder(){}
 static ThreadNetwork.Card nextUnread(List<ThreadNetwork.Card> cards,Set<String> readHere){return cards.stream().filter(c->c.known()&&c.unread()&&!readHere.contains(c.id())).min(Comparator.comparingLong(ThreadNetwork.Card::discoveryOrder)).orElse(null);}
 static List<ThreadNetwork.Card> visible(List<ThreadNetwork.Card> cards,Set<String> readHere,ThreadTopic topic){return cards.stream().filter(c->c.known()&&(topic==null||c.topic().equals(topic.id()))).sorted(Comparator.comparing((ThreadNetwork.Card c)->!c.unread()||readHere.contains(c.id())).thenComparingLong(ThreadNetwork.Card::discoveryOrder)).toList();}
}
