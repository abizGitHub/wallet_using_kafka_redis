package com.abiz.controller.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class PatchDto {

    @NonNull
    private String key;
    @NonNull
    private Object value;

}
