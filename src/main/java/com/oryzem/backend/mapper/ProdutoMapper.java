package com.oryzem.backend.mapper;

import com.oryzem.backend.dto.ProdutoDTO;
import com.oryzem.backend.model.Produto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProdutoMapper {

    ProdutoMapper INSTANCE = Mappers.getMapper(ProdutoMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sku", expression = "java(gerarSKU(dto.getNome()))")
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    @Mapping(target = "ativo", constant = "true")
    Produto toEntity(ProdutoDTO dto);

    ProdutoDTO toDTO(Produto produto);

    default String gerarSKU(String nome) {
        // Lógica para gerar SKU único
        return nome.toUpperCase().replaceAll("\\s+", "-")
                + "-" + System.currentTimeMillis();
    }
}
