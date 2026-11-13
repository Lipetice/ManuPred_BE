package com.fiec.br.back_end.kipper.features.ia.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ResumoIaDTO(String resumo) {}