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
        request.setPartNumberID("PN123");
        request.setSupplierID("SUP456");

        // when
        Item item = ItemMapper.toDomain(request);

        // then
        assertThat(item.getPartNumberID()).isEqualTo("PN123");
        assertThat(item.getSupplierID()).isEqualTo("SUP456");
    }

    @Test
    void shouldConvertDomainToResponse() {
        // given
        Item item = Item.builder()
                .partNumberID("PN123")
                .supplierID("SUP456")
                .build();

        // when
        ItemResponse response =
                ItemMapper.toResponse(item, "Item criado com sucesso");

        // then
        assertThat(response.getPartNumberID()).isEqualTo("PN123");
        assertThat(response.getSupplierID()).isEqualTo("SUP456");
        assertThat(response.getMessage()).isEqualTo("Item criado com sucesso");
    }
}
