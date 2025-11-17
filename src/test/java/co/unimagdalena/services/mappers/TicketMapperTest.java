package co.unimagdalena.services.mappers;

import co.unimagdalena.api.dto.TicketDto;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.services.mapper.TicketMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class TicketMapperTest {
    private final TicketMapper mapper = Mappers.getMapper(TicketMapper.class);

    @Test
    void toEntity_shouldMapToCreate() {
        TicketDto.TicketCreateRequest request = new TicketDto.TicketCreateRequest(
                new BigDecimal("50.00"),
                PaymentMethod.CARD,
                "A15",
                1L,
                1L,
                1L,
                1L,
                1L,
                2L
        );

        Ticket ticket = mapper.toEntity(request);

        assertThat(ticket.getPrice()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(ticket.getPurchase().getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(ticket.getSeatNumber()).isEqualTo("A15");
    }

    @Test
    void toResponse_shouldMapEntity() {
        Trip trip = Trip.builder().id(1L).build();
        Passenger passenger = Passenger.builder().id(1L).fullName("John Doe").build();
        Seat seat = Seat.builder().id(1L).number(15).build();
        Stop fromStop = Stop.builder().id(1L).name("Stop A").build();
        Stop toStop = Stop.builder().id(2L).name("Stop B").build();

        Ticket ticket = Ticket.builder()
                .id(1L)
                .price(new BigDecimal("50.00"))
                .purchase(Purchase.builder().paymentMethod(PaymentMethod.CARD).build())
                .status(TicketStatus.NO_SHOW)
                .qrCode("QR-001")
                .trip(trip)
                .passenger(passenger)
                .seat(seat)
                .fromStop(fromStop)
                .toStop(toStop)
                .build();

        TicketDto.TicketResponse dto = mapper.toResponse(ticket);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.price()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(dto.paymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(dto.status()).isEqualTo(TicketStatus.NO_SHOW);
        assertThat(dto.qrCode()).isEqualTo("QR-001");
    }

    @Test
    void toSummary_shouldMapEntity() {
        Ticket ticket = Ticket.builder()
                .id(1L)
                .price(new BigDecimal("50.00"))
                .status(TicketStatus.SOLD)
                .qrCode("QR-001")
                .build();

        TicketDto.TicketSummary dto = mapper.toSummary(ticket);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.price()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(dto.status()).isEqualTo(TicketStatus.SOLD);
        assertThat(dto.qrCode()).isEqualTo("QR-001");
    }

    @Test
    void updateEntity_shouldUpdateFields() {
        Ticket ticket = Ticket.builder()
                .id(1L)
                .price(new BigDecimal("50.00"))
                .status(TicketStatus.SOLD)
                .build();

        TicketDto.TicketUpdateRequest update = new TicketDto.TicketUpdateRequest(
                new BigDecimal("55.00"),
                PaymentMethod.CASH,
                TicketStatus.CANCELLED
        );

        mapper.updateEntity(update, ticket);

        assertThat(ticket.getPrice()).isEqualByComparingTo(new BigDecimal("55.00"));
        assertThat(ticket.getPurchase().getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CANCELLED);
    }
}
