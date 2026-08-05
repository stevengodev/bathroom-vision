package com.foliaco.vision_bathroom.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foliaco.vision_bathroom.dto.DashboardOverviewResponse;
import com.foliaco.vision_bathroom.dto.IncidentStatisticsResponse;
import com.foliaco.vision_bathroom.dto.MaintenanceStatisticsResponse;
import com.foliaco.vision_bathroom.entity.Bathroom;
import com.foliaco.vision_bathroom.entity.Block;
import com.foliaco.vision_bathroom.entity.Incident;
import com.foliaco.vision_bathroom.entity.IncidentMessage;
import com.foliaco.vision_bathroom.entity.Maintenance;
import com.foliaco.vision_bathroom.repository.BathroomRepository;
import com.foliaco.vision_bathroom.repository.IncidentRepository;
import com.foliaco.vision_bathroom.repository.MaintenanceRepository;
import com.foliaco.vision_bathroom.service.impl.AnalyticsServiceImpl;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceTest {

    @Mock
    private BathroomRepository bathroomRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private MaintenanceRepository maintenanceRepository;

    @InjectMocks
    private AnalyticsServiceImpl service;

    @Test
    void shouldGetDashboardOverview() {
        when(bathroomRepository.count()).thenReturn(3L);
        when(bathroomRepository.findByStatus(Bathroom.BathroomStatus.DISPONIBLE)).thenReturn(List.of(new Bathroom()));
        when(bathroomRepository.findByStatus(Bathroom.BathroomStatus.EN_LIMPIEZA)).thenReturn(List.of(new Bathroom()));
        when(bathroomRepository.findByStatus(Bathroom.BathroomStatus.EN_MANTENIMIENTO))
                .thenReturn(List.of(new Bathroom()));

        when(incidentRepository.countByStatus(Incident.Status.PENDING)).thenReturn(2L);
        when(maintenanceRepository.countByStatus(Maintenance.Status.ABIERTO)).thenReturn(1L);
        when(maintenanceRepository.countByStatus(Maintenance.Status.CERRADO)).thenReturn(4L);

        DashboardOverviewResponse response = service.getDashboardOverview();

        assertNotNull(response);
        assertEquals(3L, response.totalBathrooms());
        assertEquals(1L, response.availableBathrooms());
        assertEquals(1L, response.occupiedBathrooms());
        assertEquals(1L, response.maintenanceBathrooms());
        assertEquals(2L, response.activeIncidents());
        assertEquals(1L, response.openMaintenances());
        assertEquals(4L, response.closedMaintenances());
    }

    @Test
    void shouldGetIncidentStatisticsGroupedByBlock() {
        Block blockA = new Block();
        blockA.setId(1L);
        blockA.setName("Bloque A");

        Block blockB = new Block();
        blockB.setId(2L);
        blockB.setName("Bloque B");

        Bathroom bathroomA = new Bathroom();
        bathroomA.setId(10L);
        bathroomA.setBlock(blockA);

        Bathroom bathroomB = new Bathroom();
        bathroomB.setId(11L);
        bathroomB.setBlock(blockB);

        IncidentMessage incidentMessage = new IncidentMessage();
        incidentMessage.setCategory(IncidentMessage.Category.LIMPIEZA);

        Incident incident1 = new Incident();
        incident1.setBathroom(bathroomA);
        incident1.setIncidentMessage(incidentMessage);

        Incident incident2 = new Incident();
        incident2.setBathroom(bathroomA);
        incident2.setIncidentMessage(incidentMessage);

        Incident incident3 = new Incident();
        incident3.setBathroom(bathroomB);
        incident3.setIncidentMessage(incidentMessage);

        when(incidentRepository.findAll()).thenReturn(List.of(incident1, incident2, incident3));

        IncidentStatisticsResponse response = service.getIncidentStatistics("block", "desc");

        assertEquals("block", response.groupBy());
        assertEquals("desc", response.sort());
        assertEquals(2, response.items().size());
        assertTrue(
                response.items().stream().anyMatch(item -> "Bloque A".equals(item.blockName()) && item.count() == 2));
        assertTrue(
                response.items().stream().anyMatch(item -> "Bloque B".equals(item.blockName()) && item.count() == 1));
    }

    @Test
    void shouldGetIncidentStatisticsGroupedByBathroom() {
        Bathroom bathroom1 = new Bathroom();
        bathroom1.setId(1L);

        Bathroom bathroom2 = new Bathroom();
        bathroom2.setId(2L);

        Incident i1 = new Incident();
        i1.setBathroom(bathroom1);

        Incident i2 = new Incident();
        i2.setBathroom(bathroom1);

        Incident i3 = new Incident();
        i3.setBathroom(bathroom2);

        when(incidentRepository.findAll()).thenReturn(List.of(i1, i2, i3));

        IncidentStatisticsResponse response = service.getIncidentStatistics("bathroom", "desc");

        assertEquals(2, response.items().size());

        assertTrue(response.items().stream()
                .anyMatch(i -> i.bathroomId().equals(1L) && i.count() == 2));

        assertTrue(response.items().stream()
                .anyMatch(i -> i.bathroomId().equals(2L) && i.count() == 1));
    }

    @Test
    void shouldGetIncidentStatisticsGroupedByCategory() {

        Bathroom bathroom = new Bathroom();
        bathroom.setId(1L);

        IncidentMessage msg1 = new IncidentMessage();
        msg1.setCategory(IncidentMessage.Category.LIMPIEZA);

        IncidentMessage msg2 = new IncidentMessage();
        msg2.setCategory(IncidentMessage.Category.MANTENIMIENTO);

        Incident i1 = new Incident();
        i1.setBathroom(bathroom);
        i1.setIncidentMessage(msg1);

        Incident i2 = new Incident();
        i2.setBathroom(bathroom);
        i2.setIncidentMessage(msg1);

        Incident i3 = new Incident();
        i3.setBathroom(bathroom);
        i3.setIncidentMessage(msg2);

        when(incidentRepository.findAll()).thenReturn(List.of(i1, i2, i3));

        IncidentStatisticsResponse response = service.getIncidentStatistics("category", "desc");

        assertEquals(2, response.items().size());

        assertTrue(response.items().stream()
                .anyMatch(i -> "LIMPIEZA".equals(i.category()) && i.count() == 2));

        assertTrue(response.items().stream()
                .anyMatch(i -> "MANTENIMIENTO".equals(i.category()) && i.count() == 1));
    }

    @Test
    void shouldIgnoreIncidentsWithoutBathroom() {

        Bathroom bathroom = new Bathroom();
        bathroom.setId(1L);

        Incident valid = new Incident();
        valid.setBathroom(bathroom);

        Incident invalid = new Incident();
        invalid.setBathroom(null);

        when(incidentRepository.findAll()).thenReturn(List.of(valid, invalid));

        IncidentStatisticsResponse response = service.getIncidentStatistics("bathroom", "desc");

        assertEquals(1, response.items().size());
        assertEquals(1L, response.items().get(0).count());
    }

    @Test
void shouldReturnMaintenanceStatisticsFilteredByStatusBathroomAndDates() {

    Block block = new Block();
    block.setName("Bloque A");

    Bathroom bathroom = new Bathroom();
    bathroom.setId(1L);
    bathroom.setBlock(block);

    LocalDateTime now = LocalDateTime.now();

    Maintenance open = new Maintenance();
    open.setId(1L);
    open.setBathroom(bathroom);
    open.setStatus(Maintenance.Status.ABIERTO);
    open.setReportedAt(now.minusDays(1));

    Maintenance closed = new Maintenance();
    closed.setId(2L);
    closed.setBathroom(bathroom);
    closed.setStatus(Maintenance.Status.CERRADO);
    closed.setReportedAt(now.minusDays(5));
    closed.setResolvedAt(now.minusDays(2));

    when(maintenanceRepository.findAll())
            .thenReturn(List.of(open, closed));

    MaintenanceStatisticsResponse response =
            service.getMaintenanceStatistics(
                    "open",
                    1L,
                    now.minusDays(2),
                    now);

    assertEquals(1, response.history().size());
    assertEquals(1L, response.openCount());
    assertEquals(0L, response.closedCount());

    MaintenanceStatisticsResponse.HistoryItem item =
            response.history().get(0);

    assertEquals(1L, item.bathroomId());
    assertEquals("Bloque A", item.blockName());
    assertEquals(Maintenance.Status.ABIERTO, item.status());
}

@Test
void shouldReturnAllMaintenancesWhenNoFiltersAreProvided() {

    Bathroom bathroom = new Bathroom();
    bathroom.setId(1L);

    Maintenance m1 = new Maintenance();
    m1.setStatus(Maintenance.Status.ABIERTO);
    m1.setBathroom(bathroom);
    m1.setReportedAt(LocalDateTime.now());

    Maintenance m2 = new Maintenance();
    m2.setStatus(Maintenance.Status.CERRADO);
    m2.setBathroom(bathroom);
    m2.setReportedAt(LocalDateTime.now().minusDays(1));

    when(maintenanceRepository.findAll())
            .thenReturn(List.of(m1, m2));

    MaintenanceStatisticsResponse response =
            service.getMaintenanceStatistics(null, null, null, null);

    assertEquals(2, response.history().size());
    assertEquals(1L, response.openCount());
    assertEquals(1L, response.closedCount());
}

}
