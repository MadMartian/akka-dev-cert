package io.example.application;

import akka.javasdk.testkit.TestKit;
import akka.javasdk.testkit.TestKitSupport;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParticipantSlotsViewTest extends TestKitSupport {

    @Override
    protected TestKit.Settings testKitSettings() {
        return TestKit.Settings.DEFAULT.withKeyValueEntityIncomingMessages("participant-slot");
    }

    @Test
    public void getSlotsByParticipant () {
        var eventQueue = testKit.getKeyValueEntityIncomingMessages("participant-slot");

        ParticipantSlotsView.SlotRow
            john = new ParticipantSlotsView.SlotRow("slotId", "john", "type", "bookingId", "STATUS"),
            bill = new ParticipantSlotsView.SlotRow("slotId", "bill", "type", "bookingId", "STATUS");
        eventQueue.publish(john, "foo");
        eventQueue.publish(bill, "bar");

        Awaitility.await()
            .ignoreExceptions()
            .atMost(Duration.of(10, ChronoUnit.YEARS))
            .untilAsserted(() -> {
                var response = componentClient.forView()
                    .method(ParticipantSlotsView::getSlotsByParticipant)
                    .invoke("john");

                assertEquals(1, response.slots().size());
                assertEquals(john, response.slots().getFirst());
            });
    }
}
