package io.example.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import akka.Done;
import akka.javasdk.testkit.EventSourcedTestKit;

import io.example.domain.Participant.ParticipantType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ParticipantSlotEntityTest {

    private EventSourcedTestKit<ParticipantSlotEntity.State, ParticipantSlotEntity.Event, ParticipantSlotEntity> testKit;

    @BeforeEach
    void setup() {
        testKit = EventSourcedTestKit.of(ParticipantSlotEntity::new);
    }

    @Test
    public void markAvailable() {
        var result = testKit
            .method(ParticipantSlotEntity::markAvailable)
            .invoke(
                new ParticipantSlotEntity.Commands.MarkAvailable(
                    "slotId", 
                    "participantId", 
                    ParticipantType.STUDENT
                )
            );

        assertEquals(Done.getInstance(), result.getReply());
        
        var available = result.getNextEventOfType(ParticipantSlotEntity.Event.MarkedAvailable.class);
        
        assertEquals("slotId", available.slotId());
        assertEquals("participantId", available.participantId());
        assertEquals(ParticipantType.STUDENT, available.participantType());
    }

    @Test
    public void unmarkAvailable() {
        var result = testKit
                .method(ParticipantSlotEntity::unmarkAvailable)
                .invoke(
                        new ParticipantSlotEntity.Commands.UnmarkAvailable(
                                "slotId",
                                "participantId",
                                ParticipantType.STUDENT
                        )
                );

        assertEquals(Done.getInstance(), result.getReply());

        var unmarked = result.getNextEventOfType(ParticipantSlotEntity.Event.UnmarkedAvailable.class);

        assertEquals("slotId", unmarked.slotId());
        assertEquals("participantId", unmarked.participantId());
        assertEquals(ParticipantType.STUDENT, unmarked.participantType());
    }

    @Test
    public void book (){
        var result = testKit
                .method(ParticipantSlotEntity::book)
                .invoke(
                        new ParticipantSlotEntity.Commands.Book(
                                "slotId",
                                "participantId",
                                ParticipantType.STUDENT,
                                "bookingId"
                        )
                );

        assertEquals(Done.getInstance(), result.getReply());

        var booked = result.getNextEventOfType(ParticipantSlotEntity.Event.Booked.class);

        assertEquals("slotId", booked.slotId());
        assertEquals("participantId", booked.participantId());
        assertEquals(ParticipantType.STUDENT, booked.participantType());
        assertEquals("bookingId", booked.bookingId());
    }

    @Test
    public void cancel() {
        var result = testKit
                .method(ParticipantSlotEntity::cancel)
                .invoke(
                        new ParticipantSlotEntity.Commands.Cancel(
                                "slotId",
                                "participantId",
                                ParticipantType.STUDENT,
                                "bookingId"
                        )
                );

        assertEquals(Done.getInstance(), result.getReply());

        var canceled = result.getNextEventOfType(ParticipantSlotEntity.Event.Canceled.class);

        assertEquals("slotId", canceled.slotId());
        assertEquals("participantId", canceled.participantId());
        assertEquals(ParticipantType.STUDENT, canceled.participantType());
        assertEquals("bookingId", canceled.bookingId());
    }
}
