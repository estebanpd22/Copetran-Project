package co.unimagdalena.repositories;

import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ParcelRepositoryTest extends AbstractRepositoryTI {
    @Autowired
    private ParcelRepository parcelRepository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private StopRepository stopRepository;
    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private BusRepository busRepository;

    private Route createRoute(String code, String name, String origin, String destination, Float distanceKm, Float durationMin) {
        return routeRepository.save(Route.builder()
                .code(code)
                .name(name)
                .origin(origin)
                .destination(destination)
                .distanceKm(distanceKm)
                .durationMin(durationMin)
                .build());
    }

    private Stop createStop(Route route, String name, Integer order, double latitude, double longitude) {
        return stopRepository.save(Stop.builder()
                .route(route)
                .name(name)
                .order(order)
                .latitude(latitude)
                .longitude(longitude)
                .build());
    }

    private Bus createBus(String plate, Integer capacity, BusStatus status, OffsetDateTime soatExpirationDate) {
        return busRepository.save(Bus.builder()
                .plate(plate)
                .capacity(capacity)
                .status(status)
                .soatExpirationDate(soatExpirationDate)
                .build());
    }

    private Trip createTrip(Bus bus, Route route, LocalDate date, OffsetDateTime departureAt, OffsetDateTime arrivalAt, TripStatus status) {
        return tripRepository.save(Trip.builder()
                .bus(bus)
                .route(route)
                .date(date)
                .departureAt(departureAt)
                .arrivalAt(arrivalAt)
                .status(status)
                .build());
    }

    private Parcel createParcel(String code, String senderName, String senderPhone, String receiverName,
                                String receiverPhone, BigDecimal price, ParcelStatus status, Stop fromStop,
                                Stop toStop, Trip trip, String deliveryOTP) {
        return parcelRepository.save(Parcel.builder()
                .code(code)
                .senderName(senderName)
                .senderPhone(senderPhone)
                .receiverName(receiverName)
                .receiverPhone(receiverPhone)
                .price(price)
                .status(status)
                .fromStop(fromStop)
                .toStop(toStop)
                .trip(trip)
                .deliveryOTP(deliveryOTP)
                .build());
    }

    @Test
    @DisplayName("Debe encontrar un parcel por ID")
    void shouldFindById() {
        // Given
        Route route = createRoute("R001", "Ruta 1", "Bogotá", "Medellín", 400.0f, 480.0f);
        Stop stop1 = createStop(route, "Terminal Bogotá", 1, 4.6097, -74.0817);
        Stop stop2 = createStop(route, "Terminal Medellín", 2, 6.2442, -75.5812);

        Parcel parcel = createParcel("PCL001", "Juan Pérez", "3001234567", "María López",
                "3009876543", BigDecimal.valueOf(50000), ParcelStatus.CREATED,
                stop1, stop2, null, "123456");

        // When
        Optional<Parcel> found = parcelRepository.findById(parcel.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(parcel.getId());
        assertThat(found.get().getCode()).isEqualTo("PCL001");
        assertThat(found.get().getSenderName()).isEqualTo("Juan Pérez");
        assertThat(found.get().getReceiverName()).isEqualTo("María López");
        assertThat(found.get().getStatus()).isEqualTo(ParcelStatus.CREATED);
    }

    @Test
    @DisplayName("Debe encontrar un parcel por código")
    void shouldFindByCode() {
        // Given
        Route route = createRoute("R002", "Ruta 2", "Cali", "Pasto", 300.0f, 360.0f);
        Stop stop1 = createStop(route, "Terminal Cali", 1, 3.4516, -76.5320);
        Stop stop2 = createStop(route, "Terminal Pasto", 2, 1.2136, -77.2811);

        createParcel("PCL002", "Carlos Ruiz", "3002345678", "Ana Torres",
                "3008765432", BigDecimal.valueOf(35000), ParcelStatus.CREATED,
                stop1, stop2, null, "654321");

        // When
        Optional<Parcel> found = parcelRepository.findByCode("PCL002");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCode()).isEqualTo("PCL002");
        assertThat(found.get().getSenderName()).isEqualTo("Carlos Ruiz");
    }

    @Test
    @DisplayName("Debe encontrar parcels por nombre del remitente (ignorando mayúsculas)")
    void shouldFindBySenderNameIgnoringCase() {
        // Given
        Route route = createRoute("R003", "Ruta 3", "Barranquilla", "Cartagena", 120.0f, 150.0f);
        Stop stop1 = createStop(route, "Terminal Barranquilla", 1, 10.9639, -74.7964);
        Stop stop2 = createStop(route, "Terminal Cartagena", 2, 10.3910, -75.4794);

        createParcel("PCL003", "Pedro Gómez", "3003456789", "Laura Díaz",
                "3007654321", BigDecimal.valueOf(40000), ParcelStatus.IN_TRANSIT,
                stop1, stop2, null, "111222");

        // When
        List<Parcel> found = parcelRepository.findBySenderNameIgnoringCase("pedro gómez");

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getSenderName()).isEqualTo("Pedro Gómez");
    }

    @Test
    @DisplayName("Debe encontrar parcels por nombre y teléfono del remitente")
    void shouldFindBySenderNameAndPhone() {
        // Given
        Route route = createRoute("R004", "Ruta 4", "Bucaramanga", "Cúcuta", 200.0f, 240.0f);
        Stop stop1 = createStop(route, "Terminal Bucaramanga", 1, 7.1193, -73.1227);
        Stop stop2 = createStop(route, "Terminal Cúcuta", 2, 7.8939, -72.5078);

        createParcel("PCL004", "Luis Martínez", "3004567890", "Sofia García",
                "3006543210", BigDecimal.valueOf(45000), ParcelStatus.CREATED,
                stop1, stop2, null, "333444");

        // When
        List<Parcel> found = parcelRepository.findBySenderNameIgnoreCaseAndSenderPhone("luis martínez", "3004567890");

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getSenderName()).isEqualTo("Luis Martínez");
        assertThat(found.getFirst().getSenderPhone()).isEqualTo("3004567890");
    }

    @Test
    @DisplayName("Debe encontrar parcels por nombre del destinatario")
    void shouldFindByReceiverNameIgnoringCase() {
        // Given
        Route route = createRoute("R005", "Ruta 5", "Pereira", "Armenia", 50.0f, 60.0f);
        Stop stop1 = createStop(route, "Terminal Pereira", 1, 4.8133, -75.6961);
        Stop stop2 = createStop(route, "Terminal Armenia", 2, 4.5339, -75.6811);

        createParcel("PCL005", "Miguel Ángel", "3005678901", "Daniela Ramos",
                "3005432109", BigDecimal.valueOf(30000), ParcelStatus.DELIVERED,
                stop1, stop2, null, "555666");

        // When
        List<Parcel> found = parcelRepository.findByReceiverNameIgnoringCase("DANIELA RAMOS");

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getReceiverName()).isEqualTo("Daniela Ramos");
    }

    @Test
    @DisplayName("Debe encontrar parcels por nombre y teléfono del destinatario")
    void shouldFindByReceiverNameAndPhone() {
        // Given
        Route route = createRoute("R006", "Ruta 6", "Manizales", "Ibagué", 150.0f, 180.0f);
        Stop stop1 = createStop(route, "Terminal Manizales", 1, 5.0689, -75.5174);
        Stop stop2 = createStop(route, "Terminal Ibagué", 2, 4.4389, -75.2322);

        createParcel("PCL006", "Fernando Castro", "3006789012", "Valentina Moreno",
                "3004321098", BigDecimal.valueOf(55000), ParcelStatus.IN_TRANSIT,
                stop1, stop2, null, "777888");

        // When
        List<Parcel> found = parcelRepository.findByReceiverNameIgnoringCaseAndReceiverPhone("valentina moreno", "3004321098");

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getReceiverName()).isEqualTo("Valentina Moreno");
        assertThat(found.get(0).getReceiverPhone()).isEqualTo("3004321098");
    }

    @Test
    @DisplayName("Debe encontrar un parcel por deliveryOTP")
    void shouldFindByDeliveryOTP() {
        // Given
        Route route = createRoute("R007", "Ruta 7", "Santa Marta", "Riohacha", 160.0f, 200.0f);
        Stop stop1 = createStop(route, "Terminal Santa Marta", 1, 11.2408, -74.1990);
        Stop stop2 = createStop(route, "Terminal Riohacha", 2, 11.5444, -72.9072);

        createParcel("PCL007", "Andrés Villa", "3007890123", "Carolina Pérez",
                "3003210987", BigDecimal.valueOf(38000), ParcelStatus.IN_TRANSIT,
                stop1, stop2, null, "999000");

        // When
        Optional<Parcel> found = parcelRepository.findByDeliveryOTP("999000");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getDeliveryOTP()).isEqualTo("999000");
        assertThat(found.get().getCode()).isEqualTo("PCL007");
    }

    @Test
    @DisplayName("Debe encontrar parcels por tramo (fromStop y toStop)")
    void shouldFindAllByStretch() {
        // Given
        Route route = createRoute("R008", "Ruta 8", "Montería", "Sincelejo", 100.0f, 120.0f);
        Stop stop1 = createStop(route, "Terminal Montería", 1, 8.7479, -75.8814);
        Stop stop2 = createStop(route, "Terminal Sincelejo", 2, 9.3047, -75.3978);

        createParcel("PCL008", "Roberto López", "3008901234", "Patricia Silva",
                "3002109876", BigDecimal.valueOf(42000), ParcelStatus.CREATED,
                stop1, stop2, null, "121212");

        // When
        List<Parcel> found = parcelRepository.findAllByStretch(stop1.getId(), stop2.getId());

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getFromStop().getId()).isEqualTo(stop1.getId());
        assertThat(found.get(0).getToStop().getId()).isEqualTo(stop2.getId());
    }

    @Test
    @DisplayName("Debe contar parcels por estado")
    void shouldCountByStatus() {
        // Given
        Route route = createRoute("R009", "Ruta 9", "Popayán", "Neiva", 250.0f, 300.0f);
        Stop stop1 = createStop(route, "Terminal Popayán", 1, 2.4448, -76.6147);
        Stop stop2 = createStop(route, "Terminal Neiva", 2, 2.9273, -75.2819);

        createParcel("PCL009", "Gabriel Mendoza", "3009012345", "Isabel Vargas",
                "3001098765", BigDecimal.valueOf(48000), ParcelStatus.DELIVERED,
                stop1, stop2, null, "343434");
        createParcel("PCL010", "Ricardo Ortiz", "3000123456", "Mónica Jiménez",
                "3000987654", BigDecimal.valueOf(52000), ParcelStatus.DELIVERED,
                stop1, stop2, null, "454545");

        // When
        long count = parcelRepository.countByStatus(ParcelStatus.DELIVERED);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Debe encontrar parcels por estado con paginación")
    void shouldFindAllByStatus() {
        // Given
        Route route = createRoute("R010", "Ruta 10", "Villavicencio", "Bogotá", 120.0f, 150.0f);
        Stop stop1 = createStop(route, "Terminal Villavicencio", 1, 4.1420, -73.6266);
        Stop stop2 = createStop(route, "Terminal Bogotá", 2, 4.6097, -74.0817);

        createParcel("PCL011", "Esteban Rojas", "3001234560", "Natalia Cruz",
                "3009876540", BigDecimal.valueOf(36000), ParcelStatus.IN_TRANSIT,
                stop1, stop2, null, "565656");

        // When
        Page<Parcel> page = parcelRepository.findAllByStatus(ParcelStatus.IN_TRANSIT, PageRequest.of(0, 10));

        // Then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getStatus()).isEqualTo(ParcelStatus.IN_TRANSIT);
    }

    @Test
    @DisplayName("Debe calcular el total de precios de todos los parcels")
    void shouldCalculateTotal() {
        // Given
        Route route = createRoute("R011", "Ruta 11", "Tunja", "Duitama", 50.0f, 60.0f);
        Stop stop1 = createStop(route, "Terminal Tunja", 1, 5.5353, -73.3678);
        Stop stop2 = createStop(route, "Terminal Duitama", 2, 5.8272, -73.0344);

        createParcel("PCL012", "Camilo Sánchez", "3002345601", "Andrea Herrera",
                "3008765401", BigDecimal.valueOf(25000), ParcelStatus.CREATED,
                stop1, stop2, null, "676767");
        createParcel("PCL013", "Diego Ramírez", "3003456012", "Juliana Ospina",
                "3007654012", BigDecimal.valueOf(35000), ParcelStatus.CREATED,
                stop1, stop2, null, "787878");

        // When
        BigDecimal total = parcelRepository.calculateTotal();

        // Then
        assertThat(total).isNotNull();
        assertThat(total).isGreaterThanOrEqualTo(BigDecimal.valueOf(60000));
    }

    @Test
    @DisplayName("Debe cambiar el estado de un parcel")
    void shouldChangeParcelStatus() {
        // Given
        Route route = createRoute("R012", "Ruta 12", "Pasto", "Ipiales", 80.0f, 100.0f);
        Stop stop1 = createStop(route, "Terminal Pasto", 1, 1.2136, -77.2811);
        Stop stop2 = createStop(route, "Terminal Ipiales", 2, 0.8311, -77.6419);

        Parcel parcel = createParcel("PCL014", "Sergio Molina", "3004567023", "Paola Ríos",
                "3006543023", BigDecimal.valueOf(28000), ParcelStatus.CREATED,
                stop1, stop2, null, "898989");

        // When
        parcelRepository.changeParcelStatus(parcel.getId(), ParcelStatus.IN_TRANSIT);
        parcelRepository.flush();

        // Then
        Optional<Parcel> updated = parcelRepository.findById(parcel.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getStatus()).isEqualTo(ParcelStatus.IN_TRANSIT);
    }

    @Test
    @DisplayName("Debe agregar URL de foto de prueba de entrega")
    void shouldAddProofPhotoUrl() {
        // Given
        Route route = createRoute("R013", "Ruta 13", "Valledupar", "Riohacha", 140.0f, 180.0f);
        Stop stop1 = createStop(route, "Terminal Valledupar", 1, 10.4631, -73.2532);
        Stop stop2 = createStop(route, "Terminal Riohacha", 2, 11.5444, -72.9072);

        Parcel parcel = createParcel("PCL015", "Alberto Gómez", "3005678034", "Claudia Reyes",
                "3005432034", BigDecimal.valueOf(46000), ParcelStatus.DELIVERED,
                stop1, stop2, null, "101010");

        // When
        String photoUrl = "https://storage.example.com/proofs/photo123.jpg";
        parcelRepository.addProofPhotoUrl(parcel.getId(), photoUrl);
        parcelRepository.flush();

        // Then
        Optional<Parcel> updated = parcelRepository.findById(parcel.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getProofPhotoUrl()).isEqualTo(photoUrl);
    }

    @Test
    @DisplayName("Debe retornar empty cuando no existe el parcel")
    void shouldReturnEmptyWhenParcelNotFound() {
        // When
        Optional<Parcel> found = parcelRepository.findByCode("NONEXISTENT");

        // Then
        assertThat(found).isEmpty();
    }
}