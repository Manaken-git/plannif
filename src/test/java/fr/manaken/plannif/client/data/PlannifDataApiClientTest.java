package fr.manaken.plannif.client.data;

import fr.manaken.plannif.client.data.dto.ClasseDto;
import fr.manaken.plannif.client.data.dto.CreneauDTO;
import fr.manaken.plannif.client.data.dto.EleveDto;
import fr.manaken.plannif.client.data.dto.MatiereClasseConfigDto;
import fr.manaken.plannif.client.data.dto.MatiereDto;
import fr.manaken.plannif.client.data.dto.ProfesseurDto;
import fr.manaken.plannif.client.data.dto.SalleDto;
import fr.manaken.plannif.client.data.dto.SeanceDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PlannifDataApiClientTest {

    private PlannifDataApiClient client;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8081/plannif-data");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new PlannifDataApiClient(builder.build());
    }

    @Test
    void testGetSeances() {
        String json = "[{\"id\":1,\"professeurId\":10,\"classeId\":20,\"matiereId\":30,\"salleId\":40,\"type\":\"COURS\"}]";
        mockServer.expect(requestTo("http://localhost:8081/plannif-data/seances/list"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<SeanceDto> result = client.getSeances();
        mockServer.verify();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(10L, result.get(0).getProfesseurId());
    }

    @Test
    void testGetProfesseurs() {
        String json = "[{\"id\":10,\"nom\":\"Dupont\",\"prenom\":\"Jean\",\"email\":\"jean.dupont@test.fr\"}]";
        mockServer.expect(requestTo("http://localhost:8081/plannif-data/profs/list"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<ProfesseurDto> result = client.getProfesseurs();
        mockServer.verify();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Dupont", result.get(0).getNom());
    }

    @Test
    void testGetClasses() {
        String json = "[{\"id\":20,\"nom\":\"6ème A\"}]";
        mockServer.expect(requestTo("http://localhost:8081/plannif-data/classes/list"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<ClasseDto> result = client.getClasses();
        mockServer.verify();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("6ème A", result.get(0).getNom());
    }

    @Test
    void testGetEleves() {
        String json = "[{\"id\":100,\"nom\":\"Martin\",\"prenom\":\"Alice\",\"classeId\":20}]";
        mockServer.expect(requestTo("http://localhost:8081/plannif-data/eleves/list"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<EleveDto> result = client.getEleves();
        mockServer.verify();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Martin", result.get(0).getNom());
    }

    @Test
    void testGetMatieres() {
        String json = "[{\"id\":30,\"nom\":\"Mathématiques\",\"volumeHoraireAnnuel\":120}]";
        mockServer.expect(requestTo("http://localhost:8081/plannif-data/matieres/list"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<MatiereDto> result = client.getMatieres();
        mockServer.verify();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Mathématiques", result.get(0).getNom());
    }

    @Test
    void testGetSalles() {
        String json = "[{\"id\":40,\"code\":\"A101\",\"capacite\":30,\"type\":\"COURS\"}]";
        mockServer.expect(requestTo("http://localhost:8081/plannif-data/salles/list"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<SalleDto> result = client.getSalles();
        mockServer.verify();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("A101", result.get(0).getCode());
    }

    @Test
    void testGetCreneaux() {
        String json = "[{\"id\":50,\"debut\":\"2024-02-12T08:00:00\",\"fin\":\"2024-02-12T09:00:00\"}]";
        mockServer.expect(requestTo("http://localhost:8081/plannif-data/creneaux/list"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<CreneauDTO> result = client.getCreneaux();
        mockServer.verify();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(50L, result.get(0).id());
        assertEquals(LocalDateTime.of(2024, 2, 12, 8, 0), result.get(0).debut());
        assertEquals(LocalDateTime.of(2024, 2, 12, 9, 0), result.get(0).fin());
    }

    @Test
    void testGetMatiereClasseConfigs() {
        String json = "[{\"id\":100,\"classeId\":1,\"classeNom\":\"6ème 1\",\"matiereId\":2,\"matiereNom\":\"Maths\",\"dateDebut\":\"2024-02-12\",\"dateFin\":\"2024-02-13\"}]";
        mockServer.expect(requestTo("http://localhost:8081/plannif-data/configs/list"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<MatiereClasseConfigDto> result = client.getMatiereClasseConfigs();
        mockServer.verify();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).id());
        assertEquals(1L, result.get(0).classeId());
        assertEquals("6ème 1", result.get(0).classeNom());
        assertEquals(2L, result.get(0).matiereId());
        assertEquals("Maths", result.get(0).matiereNom());
        assertEquals(java.time.LocalDate.of(2024, 2, 12), result.get(0).dateDebut());
        assertEquals(java.time.LocalDate.of(2024, 2, 13), result.get(0).dateFin());
    }
}
