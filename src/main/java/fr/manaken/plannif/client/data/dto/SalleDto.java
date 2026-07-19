package fr.manaken.plannif.client.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalleDto {
    private Long id;
    private String code;
    private Integer capacite;
    private String type;
}
