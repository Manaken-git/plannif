package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.SeanceDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.SeanceMapper;
import fr.manaken.plannif.model.Seance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeanceServiceTest {

    @Mock
    private DataFetcher dataFetcher;

    @Mock
    private SeanceMapper mapper;

    @InjectMocks
    private SeanceService seanceService;

    @Test
    void shouldReturnListOfSeances() {
        // Given
        Seance seance = new Seance();
        seance.setId(1L);
        SeanceDTO seanceDTO = new SeanceDTO(1L, null, null, null, null, null, null);

        when(dataFetcher.getSeances()).thenReturn(List.of(seance));
        when(mapper.toDto(seance)).thenReturn(seanceDTO);

        // When
        List<SeanceDTO> result = seanceService.getSeances();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        verify(dataFetcher).getSeances();
        verify(mapper).toDto(seance);
    }
}
