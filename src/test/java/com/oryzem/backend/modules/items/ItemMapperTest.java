package com.oryzem.backend.modules.items;

import com.oryzem.backend.modules.items.domain.Item;
import com.oryzem.backend.modules.items.dto.ItemRequest;
import com.oryzem.backend.modules.items.dto.ItemResponse;
import com.oryzem.backend.modules.items.service.ItemMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    @Test
    void shouldConvertRequestToDomain() {
        // given
        ItemRequest request = new ItemRequest();
        request.setPartNumber("PN123");
        request.setSupplierID("SUP456");

        // when
        Item item = ItemMapper.toDomain(request);

        // then
        assertThat(item.getPartNumberVersion()).isNull();
        assertThat(item.getSupplierID()).isEqualTo("SUP456");
    }

    @Test
    void shouldConvertDomainToResponse() {
        // given
        Item item = Item.builder()
                .partNumberVersion("PN123")
                .supplierID("SUP456")
                .build();

        // when
        ItemResponse response =
                ItemMapper.toResponse(item, "Item criado com sucesso");

        // then
        assertThat(response.getPartNumberVersion()).isEqualTo("PN123");
        assertThat(response.getSupplierID()).isEqualTo("SUP456");
        assertThat(response.getMessage()).isEqualTo("Item criado com sucesso");
    }
}



