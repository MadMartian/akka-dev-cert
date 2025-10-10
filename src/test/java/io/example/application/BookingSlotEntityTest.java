package io.example.application;

import akka.Done;
import akka.javasdk.testkit.EventSourcedResult;
import akka.javasdk.testkit.EventSourcedTestKit;

import io.example.domain.BookingEvent;
import io.example.domain.Participant;
import io.example.domain.Timeslot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class BookingSlotEntityTest {

    private EventSourcedTestKit<Timeslot, BookingEvent, BookingSlotEntity> testKit;

    @BeforeEach
    void setup() {
        testKit = EventSourcedTestKit.of(BookingSlotEntity::new);
    }

    @Test
    public void initialState() {
        EventSourcedResult<Timeslot> result = testKit.method(BookingSlotEntity::getSlot).invoke();

        assertNotNull(result);

        Timeslot timeslot = result.getReply();

        assertEquals(0, timeslot.bookings().size());
        assertEquals(0, timeslot.available().size());
    }

    @Test
    public void booking() {
        {
            var result = testKit.method(BookingSlotEntity::bookSlot)
                .invoke(
                    new BookingSlotEntity.Command.BookReservation(
                    "studentId",
                    "aircraftId",
                    "instructorId",
                    "bookingId"
                    )
                );

            assertEquals(Done.getInstance(), result.getReply());

            var booking = List.of(
                result.getNextEventOfType(BookingEvent.ParticipantBooked.class),
                result.getNextEventOfType(BookingEvent.ParticipantBooked.class),
                result.getNextEventOfType(BookingEvent.ParticipantBooked.class)
            );

            assertEquals(
                Set.of("testkit-entity-id"),
                booking.stream().map(BookingEvent.ParticipantBooked::slotId).collect(Collectors.toSet())
            );
            assertEquals(
                Set.of("studentId", "aircraftId", "instructorId"),
                booking.stream().map(BookingEvent.ParticipantBooked::participantId).collect(Collectors.toSet())
            );
            assertEquals(
                Arrays.stream(Participant.ParticipantType.values()).collect(Collectors.toSet()),
                booking.stream().map(BookingEvent.ParticipantBooked::participantType).collect(Collectors.toSet())
            );
        }

        {
            var result = testKit.method(BookingSlotEntity::cancelBooking)
                    .invoke(
                            "bookingId"
                    );

            assertEquals(Done.getInstance(), result.getReply());

            var cancellations = List.of(
                result.getNextEventOfType(BookingEvent.ParticipantCanceled.class),
                result.getNextEventOfType(BookingEvent.ParticipantCanceled.class),
                result.getNextEventOfType(BookingEvent.ParticipantCanceled.class)
            );

            assertEquals(
                Set.of("testkit-entity-id"),
                cancellations.stream().map(BookingEvent.ParticipantCanceled::slotId).collect(Collectors.toSet())
            );
            assertEquals(
                Set.of("studentId", "aircraftId", "instructorId"),
                cancellations.stream().map(BookingEvent.ParticipantCanceled::participantId).collect(Collectors.toSet())
            );
            assertEquals(
                Arrays.stream(Participant.ParticipantType.values()).collect(Collectors.toSet()),
                cancellations.stream().map(BookingEvent.ParticipantCanceled::participantType).collect(Collectors.toSet())
            );
        }
    }
    
    @Test
    public void markAvailable() {
        var result = testKit.method(BookingSlotEntity::markSlotAvailable)
                .invoke(
                        new BookingSlotEntity.Command.MarkSlotAvailable(
                                new Participant("studentId", Participant.ParticipantType.STUDENT)
                        )
                );
        
        assertEquals(Done.getInstance(), result.getReply());
        
        var available = result.getNextEventOfType(BookingEvent.ParticipantMarkedAvailable.class);
        
        assertEquals("testkit-entity-id", available.slotId());
        assertEquals("studentId", available.participantId());
        assertEquals(Participant.ParticipantType.STUDENT, available.participantType());
    }
    
    @Test
    public void markUnavailable() {
        var result = testKit.method(BookingSlotEntity::unmarkSlotAvailable)
                .invoke(
                        new BookingSlotEntity.Command.UnmarkSlotAvailable(
                                new Participant("studentId", Participant.ParticipantType.STUDENT)
                        )
                );

        assertEquals(Done.getInstance(), result.getReply());

        var unavailable = result.getNextEventOfType(BookingEvent.ParticipantUnmarkedAvailable.class);

        assertEquals("testkit-entity-id", unavailable.slotId());
        assertEquals("studentId", unavailable.participantId());
        assertEquals(Participant.ParticipantType.STUDENT, unavailable.participantType());
    }
}
