package fr.manaken.plannif.client.data;

import fr.manaken.plannif.client.data.dto.ClasseDto;
import fr.manaken.plannif.client.data.dto.EleveDto;
import fr.manaken.plannif.client.data.dto.MatiereDto;
import fr.manaken.plannif.client.data.dto.ProfesseurDto;
import fr.manaken.plannif.client.data.dto.SalleDto;
import fr.manaken.plannif.client.data.dto.SeanceDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class PlannifDataApiClient {

    private final RestClient restClient;

    public PlannifDataApiClient(RestClient plannifDataRestClient) {
        this.restClient = plannifDataRestClient;
    }

    public List<SeanceDto> getSeances() {
        return restClient.get()
                .uri("/seances/list")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<ProfesseurDto> getProfesseurs() {
        return restClient.get()
                .uri("/profs/list")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<ClasseDto> getClasses() {

        return restClient.get()
                .uri("/classes/list")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<EleveDto> getEleves() {
        return restClient.get()
                .uri("/eleves/list")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<MatiereDto> getMatieres() {
        return restClient.get()
                .uri("/matieres/list")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<SalleDto> getSalles() {
        return restClient.get()
                .uri("/salles/list")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
