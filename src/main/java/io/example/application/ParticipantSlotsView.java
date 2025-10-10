package io.example.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ComponentId("view-participant-slots")
public class ParticipantSlotsView extends View {

    private static Logger logger = LoggerFactory.getLogger(ParticipantSlotsView.class);

    @Consume.FromEventSourcedEntity(ParticipantSlotEntity.class)
    public static class ParticipantSlotsViewUpdater extends TableUpdater<SlotRow> {

        public Effect<SlotRow> onEvent(ParticipantSlotEntity.Event event) {
            return switch (event) {
                case ParticipantSlotEntity.Event.Booked booked -> effects()
                    .updateRow(
                        new SlotRow(
                            booked.slotId(),
                            booked.participantId(),
                            booked.participantType().name(),
                            booked.bookingId(),
                            SlotRow.STATUS_BOOKED
                        )
                    );
                case ParticipantSlotEntity.Event.Canceled canceled -> effects()
                    .updateRow(
                        new SlotRow(
                            canceled.slotId(),
                            canceled.participantId(),
                            canceled.participantType().name(),
                            canceled.bookingId(),
                            SlotRow.STATUS_CANCELLED
                        )
                    );
                case ParticipantSlotEntity.Event.MarkedAvailable available -> effects()
                    .updateRow(
                        new SlotRow(
                            available.slotId(),
                            available.participantId(),
                            available.participantType().name(),
                            rowState().bookingId,
                            SlotRow.STATUS_AVAILABLE
                        )
                    );
                case ParticipantSlotEntity.Event.UnmarkedAvailable unavailable -> effects()
                    .updateRow(
                        new SlotRow(
                            unavailable.slotId(),
                            unavailable.participantId(),
                            unavailable.participantType().name(),
                            rowState().bookingId,
                            SlotRow.STATUS_UNAVAILABLE
                        )
                    );

            };
        }
    }

    public record SlotRow(
            String slotId,
            String participantId,
            String participantType,
            String bookingId,
            String status) {
        public static final String
                STATUS_AVAILABLE = "AVAILABLE",
                STATUS_UNAVAILABLE = "UNAVAILABLE",
                STATUS_BOOKED = "BOOKED",
                STATUS_CANCELLED = "CANCELLED";
    }

    public record ParticipantStatusInput(String participantId, String status) {
    }

    public record SlotList(List<SlotRow> slots) {
    }

    @Query("SELECT * FROM view_participant_slots WHERE participantId = :participantId")
    public QueryEffect<SlotList> getSlotsByParticipant(String participantId) {
        return queryResult();
    }

    @Query("SELECT * FROM view_participant_slots WHERE participantId = :participantId AND status = :status")
    public QueryEffect<SlotList> getSlotsByParticipantAndStatus(ParticipantStatusInput input) {
        return queryResult();
    }
}