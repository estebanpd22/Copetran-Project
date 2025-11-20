package co.unimagdalena.services.mappers;

import co.unimagdalena.api.dto.PurchaseDto;
import co.unimagdalena.domine.entities.PaymentMethod;
import co.unimagdalena.domine.entities.PaymentStatus;
import co.unimagdalena.domine.entities.Purchase;
import co.unimagdalena.domine.entities.User;
import co.unimagdalena.services.mapper.PurchaseMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class PurchaseMapperTest {
    private final PurchaseMapper mapper = Mappers.getMapper(PurchaseMapper.class);

    @Test
    void toEntity_shouldMapToCreate() {
        PurchaseDto.PurchaseCreateRequest request = new PurchaseDto.PurchaseCreateRequest(
                1L,
                PaymentMethod.CARD,
                new ArrayList<>()
        );

        Purchase purchase = mapper.toEntity(request);

        assertThat(purchase).isNotNull();
    }

    @Test
    void toResponse_shouldMapEntity() {
        User user = User.builder().id(1L).fullName("John Doe").email("john@test.com").build();

        Purchase purchase = Purchase.builder()
                .id(1L)
                .paymentMethod(PaymentMethod.CARD)
                .paymentStatus(PaymentStatus.CONFIRMED)
                .user(user)
                .build();

        PurchaseDto.PurchaseResponse dto = mapper.toResponse(purchase);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.paymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(dto.paymentStatus()).isEqualTo(PaymentStatus.CONFIRMED);
        assertThat(dto.user()).isNotNull();
    }

    @Test
    void updateEntity_shouldUpdateStatus() {
        Purchase purchase = Purchase.builder()
                .id(1L)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        PurchaseDto.PurchaseUpdateRequest update = new PurchaseDto.PurchaseUpdateRequest(
                PaymentStatus.CONFIRMED
        );

        mapper.updateEntity(update, purchase);

        assertThat(purchase.getPaymentStatus()).isEqualTo(PaymentStatus.CONFIRMED);
    }
}
