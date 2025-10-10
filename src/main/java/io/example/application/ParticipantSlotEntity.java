package io.example.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.TypeName;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import io.example.domain.Participant.ParticipantType;

@ComponentId("participant-slot")
public class ParticipantSlotEntity
        extends EventSourcedEntity<ParticipantSlotEntity.State, ParticipantSlotEntity.Event> {

    public Effect<Done> unmarkAvailable(ParticipantSlotEntity.Commands.UnmarkAvailable unmark) {
        return effects()
                .persist(new Event.UnmarkedAvailable(unmark.slotId, unmark.participantId, unmark.participantType))
                .thenReply(state -> Done.done());
    }

    public Effect<Done> markAvailable(ParticipantSlotEntity.Commands.MarkAvailable mark) {
        return effects()
                .persist(new Event.MarkedAvailable(mark.slotId, mark.participantId, mark.participantType))
                .thenReply(state -> Done.done());
    }

    public Effect<Done> book(ParticipantSlotEntity.Commands.Book book) {
        return effects()
                .persist(new Event.Booked(book.slotId, book.participantId, book.participantType, book.bookingId))
                .thenReply(state -> Done.done());
    }

    public Effect<Done> cancel(ParticipantSlotEntity.Commands.Cancel cancel) {
        return effects()
                .persist(new Event.Canceled(cancel.slotId, cancel.participantId, cancel.participantType, cancel.bookingId))
                .thenReply(state -> Done.done());
    }

    public record State(
            String slotId, String participantId, ParticipantType participantType, String status) {
    }

    public sealed interface Commands {
        record MarkAvailable(String slotId, String participantId, ParticipantType participantType)
                implements Commands {
        }

        record UnmarkAvailable(String slotId, String participantId, ParticipantType participantType)
                implements Commands {
        }

        record Book(
                String slotId, String participantId, ParticipantType participantType, String bookingId)
                implements Commands {
        }

        record Cancel(
                String slotId, String participantId, ParticipantType participantType, String bookingId)
                implements Commands {
        }
    }

    public sealed interface Event {
        @TypeName("marked-available")
        record MarkedAvailable(String slotId, String participantId, ParticipantType participantType)
                implements Event {
        }

        @TypeName("unmarked-available")
        record UnmarkedAvailable(String slotId, String participantId, ParticipantType participantType)
                implements Event {
        }

        @TypeName("participant-booked")
        record Booked(
                String slotId, String participantId, ParticipantType participantType, String bookingId)
                implements Event {
        }

        @TypeName("participant-canceled")
        record Canceled(
                String slotId, String participantId, ParticipantType participantType, String bookingId)
                implements Event {
        }
    }

    @Override
    public ParticipantSlotEntity.State applyEvent(ParticipantSlotEntity.Event event) {
        return switch (event) {
            case Event.MarkedAvailable e ->
                new State(e.slotId(), e.participantId(), e.participantType(),
                          ParticipantSlotsView.SlotRow.STATUS_AVAILABLE);
            case Event.UnmarkedAvailable e ->
                new State(e.slotId(), e.participantId(), e.participantType(),
                          ParticipantSlotsView.SlotRow.STATUS_UNAVAILABLE);
            case Event.Booked e ->
                new State(e.slotId(), e.participantId(), e.participantType(),
                          ParticipantSlotsView.SlotRow.STATUS_BOOKED);
            case Event.Canceled e ->
                new State(e.slotId(), e.participantId(), e.participantType(),
                          ParticipantSlotsView.SlotRow.STATUS_AVAILABLE);
        };
    }
}
