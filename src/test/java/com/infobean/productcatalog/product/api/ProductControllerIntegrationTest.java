package com.infobean.productcatalog.product.api;

import com.infobean.productcatalog.product.application.ProductRequest;
import com.infobean.productcatalog.product.domain.ProductStatus;
import com.infobean.productcatalog.shared.constants.ApiPaths;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void shouldCreateGetUpdateAndDeleteProduct() throws Exception {
        ProductRequest createRequest =
                new ProductRequest("Laptop", new BigDecimal("999.99"), ProductStatus.ACTIVE);

        String body = mockMvc.perform(post(ApiPaths.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Laptop")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode created = objectMapper.readTree(body);
        String id = created.get("id").asText();

        mockMvc.perform(get(ApiPaths.PRODUCTS + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)));

        ProductRequest updateRequest =
                new ProductRequest("Updated Laptop", new BigDecimal("1099.99"), ProductStatus.INACTIVE);

        mockMvc.perform(put(ApiPaths.PRODUCTS + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Laptop")))
                .andExpect(jsonPath("$.status", is("INACTIVE")));

        mockMvc.perform(delete(ApiPaths.PRODUCTS + "/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(ApiPaths.PRODUCTS + "/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectInvalidProduct() throws Exception {
        ProductRequest request =
                new ProductRequest(" ", BigDecimal.ZERO, null);

        mockMvc.perform(post(ApiPaths.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.price").exists())
                .andExpect(jsonPath("$.fieldErrors.status").exists());
    }

    @Test
    void shouldRejectNegativePrice() throws Exception {
        ProductRequest request =
                new ProductRequest("Laptop", new BigDecimal("-1"), ProductStatus.ACTIVE);

        mockMvc.perform(post(ApiPaths.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.price", is("price must be greater than zero")));
    }

    @Test
    void shouldReturn404ForUnknownProduct() throws Exception {
        mockMvc.perform(get(ApiPaths.PRODUCTS + "/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void shouldSupportPagination() throws Exception {
        mockMvc.perform(get(ApiPaths.PRODUCTS)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "name")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size", is(10)));
    }
}
