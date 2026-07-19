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
                .uri("/plannif-data/seances/list")
                .retrieve()
                .body(new ParameterizedTypeReference<List<SeanceDto>>() {});
    }

    public List<ProfesseurDto> getProfesseurs() {
        return restClient.get()
                .uri("/plannif-data/profs/list")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProfesseurDto>>() {});
    }

    public List<ClasseDto> getClasses() {
        return restClient.get()
                .uri("/plannif-data/classes/list")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ClasseDto>>() {});
    }

    public List<EleveDto> getEleves() {
        return restClient.get()
                .uri("/plannif-data/eleves/list")
                .retrieve()
                .body(new ParameterizedTypeReference<List<EleveDto>>() {});
    }

    public List<MatiereDto> getMatieres() {
        return restClient.get()
                .uri("/plannif-data/matieres/list")
                .retrieve()
                .body(new ParameterizedTypeReference<List<MatiereDto>>() {});
    }

    public List<SalleDto> getSalles() {
        return restClient.get()
                .uri("/plannif-data/salles/list")
                .retrieve()
                .body(new ParameterizedTypeReference<List<SalleDto>>() {});
    }
}
