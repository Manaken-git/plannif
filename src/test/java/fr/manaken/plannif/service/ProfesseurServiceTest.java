package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.ProfesseurDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.ProfesseurMapper;
import fr.manaken.plannif.model.Professeur;
import fr.manaken.plannif.pusher.DataPusher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfesseurServiceTest {

    @Mock
    private DataFetcher dataFetcher;

    @Mock
    private DataPusher dataPusher;

    @Mock
    private ProfesseurMapper mapper;

    @InjectMocks
    private ProfesseurService professeurService;

    private Professeur professeur;
    private ProfesseurDTO professeurDTO;

    @BeforeEach
    void setUp() {
        professeur = new Professeur();
        professeur.setId(1L);
        professeur.setNom("Doe");
        professeur.setPrenom("John");

        professeurDTO = new ProfesseurDTO(1L, "Doe", "John", "john.doe@example.com", null, null, null);
    }

    @Test
    void shouldReturnListOfProfesseurs() {
        // Given
        when(dataFetcher.getProfesseurs()).thenReturn(List.of(professeur));
        when(mapper.toDto(professeur)).thenReturn(professeurDTO);

        // When
        List<ProfesseurDTO> result = professeurService.getProfesseurs();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).nom()).isEqualTo("Doe");
        verify(dataFetcher).getProfesseurs();
        verify(mapper).toDto(professeur);
    }

    @Test
    void shouldSaveNewProfesseur() {
        // Given
        ProfesseurDTO newProfDTO = new ProfesseurDTO(null, "Smith", "Jane", "jane.smith@example.com", null, null, null);
        Professeur savedProf = new Professeur();
        savedProf.setId(2L);
        savedProf.setNom("Smith");

        when(dataPusher.saveProfesseur(any())).thenReturn(savedProf);
        when(mapper.toDto(savedProf))
                .thenReturn(new ProfesseurDTO(2L, "Smith", "Jane", "jane.smith@example.com", null, null, null));

        // When
        ProfesseurDTO result = professeurService.saveProfesseur(newProfDTO);

        // Then
        assertThat(result.id()).isEqualTo(2L);
        verify(dataPusher).saveProfesseur(any());
        verify(mapper).mergeWDTO(eq(null), eq(newProfDTO));
    }

    @Test
    void shouldUpdateExistingProfesseur() {
        // Given
        when(dataFetcher.getProfesseur(1)).thenReturn(professeur);
        when(dataPusher.saveProfesseur(professeur)).thenReturn(professeur);
        when(mapper.toDto(professeur)).thenReturn(professeurDTO);

        // When
        ProfesseurDTO result = professeurService.saveProfesseur(professeurDTO);

        // Then
        assertThat(result.id()).isEqualTo(1L);
        verify(dataFetcher).getProfesseur(1);
        verify(mapper).mergeWDTO(eq(professeur), eq(professeurDTO));
        verify(dataPusher).saveProfesseur(professeur);
    }
}
