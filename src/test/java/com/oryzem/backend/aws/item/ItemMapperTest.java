package com.oryzem.backend.aws.item;

import com.oryzem.backend.domain.aws.item.Item;
import com.oryzem.backend.domain.aws.item.ItemMapper;
import com.oryzem.backend.domain.aws.item.ItemRequest;
import com.oryzem.backend.domain.aws.item.ItemResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    @Test
    void shouldConvertRequestToDomain() {
        // given
        ItemRequest request = new ItemRequest();
        request.setPartNumberVersion("PN123");
        request.setSupplierID("SUP456");

        // when
        Item item = ItemMapper.toDomain(request);

        // then
        assertThat(item.getPartNumberVersion()).isEqualTo("PN123");
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


