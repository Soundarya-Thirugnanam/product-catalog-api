package com.dhl.productcatalog.controller;

import com.dhl.productcatalog.dto.ProductRequest;
import com.dhl.productcatalog.entity.ProductStatus;
import com.dhl.productcatalog.repository.ProductRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.h2.console.enabled=false", "spring.cache.type=simple"})
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProductRepository repository;

    /**
     * End-to-end happy path through the full CRUD lifecycle.
     */
    @Test
    void shouldCreateGetUpdateAndDeleteProduct() throws Exception {
        ProductRequest createRequest =
                new ProductRequest("Laptop", "A powerful laptop", new BigDecimal("999.99"), ProductStatus.ACTIVE);

        String body = mockMvc.perform(post(ApiPaths.PRODUCTS)
                        .header("X-User-Name", "alice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Laptop")))
                .andExpect(jsonPath("$.description", is("A powerful laptop")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.createdBy", is("alice")))
                .andExpect(jsonPath("$.updatedBy", is("alice")))
                .andExpect(jsonPath("$.createdOn").exists())
                .andExpect(jsonPath("$.updatedOn").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode created = objectMapper.readTree(body);
        String id = created.get("id").asText();

        mockMvc.perform(get(ApiPaths.PRODUCTS + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)));

        ProductRequest updateRequest =
                new ProductRequest("Updated Laptop", "An even better laptop", new BigDecimal("1099.99"), ProductStatus.INACTIVE);

        mockMvc.perform(put(ApiPaths.PRODUCTS + "/{id}", id)
                        .header("X-User-Name", "bob")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Laptop")))
                .andExpect(jsonPath("$.description", is("An even better laptop")))
                .andExpect(jsonPath("$.status", is("INACTIVE")))
                .andExpect(jsonPath("$.createdBy", is("alice")))
                .andExpect(jsonPath("$.updatedBy", is("bob")));

        mockMvc.perform(get(ApiPaths.PRODUCTS + "/{id}/audit", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].action", is("UPDATED")))
                .andExpect(jsonPath("$[1].action", is("CREATED")));

        mockMvc.perform(delete(ApiPaths.PRODUCTS + "/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(ApiPaths.PRODUCTS + "/{id}", id))
                .andExpect(status().isNotFound());

        mockMvc.perform(get(ApiPaths.PRODUCTS + "/{id}/audit", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(3)))
                .andExpect(jsonPath("$[0].action", is("DELETED")));
    }

    /**
     * A soft-deleted product must disappear from the list endpoint, not just GET-by-id.
     */
    @Test
    void shouldExcludeDeletedProductFromList() throws Exception {
        String body = mockMvc.perform(post(ApiPaths.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ProductRequest("Soft Delete Target", null, new BigDecimal("10.00"), ProductStatus.ACTIVE))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(body).get("id").asText();

        mockMvc.perform(delete(ApiPaths.PRODUCTS + "/{id}", id))
                .andExpect(status().isNoContent());

        MvcResult listResult = mockMvc.perform(get(ApiPaths.PRODUCTS).param("size", "100"))
                .andReturn();
        int listStatus = listResult.getResponse().getStatus();

        assertThat(listStatus == 200 || listStatus == 204).isTrue();
        if (listStatus == 200) {
            assertThat(listResult.getResponse().getContentAsString()).doesNotContain(id);
        }
    }

    /**
     * A deleted product's name must be reusable by a new product, exercising the real
     * database unique constraint (a mock-based test can't catch this).
     */
    @Test
    void shouldAllowReusingNameAfterSoftDelete() throws Exception {
        ProductRequest request =
                new ProductRequest("Reusable Name", null, new BigDecimal("10.00"), ProductStatus.ACTIVE);

        String firstBody = mockMvc.perform(post(ApiPaths.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String firstId = objectMapper.readTree(firstBody).get("id").asText();

        mockMvc.perform(delete(ApiPaths.PRODUCTS + "/{id}", firstId))
                .andExpect(status().isNoContent());

        mockMvc.perform(post(ApiPaths.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Reusable Name")));
    }

    /**
     * A product name must be unique across the catalog.
     */
    @Test
    void shouldRejectDuplicateProductName() throws Exception {
        ProductRequest request =
                new ProductRequest("Unique Widget", null, new BigDecimal("10.00"), ProductStatus.ACTIVE);

        mockMvc.perform(post(ApiPaths.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(ApiPaths.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Product name already exists: Unique Widget")));
    }

    /**
     * Renaming a product to another product's name must be rejected.
     */
    @Test
    void shouldRejectRenamingToAnExistingProductName() throws Exception {
        String firstBody = mockMvc.perform(post(ApiPaths.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ProductRequest("Rename Target", null, new BigDecimal("10.00"), ProductStatus.ACTIVE))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String secondBody = mockMvc.perform(post(ApiPaths.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ProductRequest("Rename Source", null, new BigDecimal("10.00"), ProductStatus.ACTIVE))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String secondId = objectMapper.readTree(secondBody).get("id").asText();
        String firstName = objectMapper.readTree(firstBody).get("name").asText();

        mockMvc.perform(put(ApiPaths.PRODUCTS + "/{id}", secondId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ProductRequest(firstName, null, new BigDecimal("10.00"), ProductStatus.ACTIVE))))
                .andExpect(status().isConflict());
    }

    /**
     * A whitespace-only name must fail {@code @NotBlank}.
     */
    @Test
    void shouldRejectBlankName() throws Exception {
        ProductRequest request =
                new ProductRequest(" ", null, new BigDecimal("10.00"), ProductStatus.ACTIVE);

        mockMvc.perform(post(ApiPaths.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name", is("name must not be blank")));
    }

    /**
     * All three required fields must be validated in one request.
     */
    @Test
    void shouldRejectNullNamePriceAndStatus() throws Exception {
        ProductRequest request =
                new ProductRequest(null, null, null, null);

        mockMvc.perform(post(ApiPaths.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.price").exists())
                .andExpect(jsonPath("$.fieldErrors.status").exists());
    }

    /**
     * Zero must be rejected as a boundary value.
     */
    @Test
    void shouldRejectZeroPrice() throws Exception {
        ProductRequest request =
                new ProductRequest("Laptop", null, BigDecimal.ZERO, ProductStatus.ACTIVE);

        mockMvc.perform(post(ApiPaths.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.price", is("price must be greater than zero")));
    }

    /**
     * Negative prices must also be rejected.
     */
    @Test
    void shouldRejectNegativePrice() throws Exception {
        ProductRequest request =
                new ProductRequest("Laptop", null, new BigDecimal("-1"), ProductStatus.ACTIVE);

        mockMvc.perform(post(ApiPaths.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.price", is("price must be greater than zero")));
    }

    /**
     * A random id should not exist, exercising the 404 path.
     */
    @Test
    void shouldReturn404ForUnknownProductOnGet() throws Exception {
        mockMvc.perform(get(ApiPaths.PRODUCTS + "/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    /**
     * The not-found check must also apply to updates.
     */
    @Test
    void shouldReturn404WhenUpdatingUnknownProduct() throws Exception {
        ProductRequest request =
                new ProductRequest("Laptop", null, new BigDecimal("10.00"), ProductStatus.ACTIVE);

        mockMvc.perform(put(ApiPaths.PRODUCTS + "/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    /**
     * The not-found check must also apply to deletes.
     */
    @Test
    void shouldReturn404WhenDeletingUnknownProduct() throws Exception {
        mockMvc.perform(delete(ApiPaths.PRODUCTS + "/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    /**
     * An empty product list should return 204, not an empty page body.
     */
    @Test
    void shouldReturnNoContentWhenListIsEmpty() throws Exception {
        repository.deleteAll();

        mockMvc.perform(get(ApiPaths.PRODUCTS))
                .andExpect(status().isNoContent());
    }

    /**
     * The requested page size must reach the query.
     */
    @Test
    void shouldSupportPagination() throws Exception {
        mockMvc.perform(post(ApiPaths.PRODUCTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new ProductRequest("Pagination Product", null, new BigDecimal("5.00"), ProductStatus.ACTIVE))));

        mockMvc.perform(get(ApiPaths.PRODUCTS)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "name")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size", is(10)));
    }

    /**
     * A {@code sortBy} value that isn't a real property must be rejected.
     */
    @Test
    void shouldRejectInvalidSortField() throws Exception {
        mockMvc.perform(get(ApiPaths.PRODUCTS).param("sortBy", "doesNotExist"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.sortBy").exists());
    }

    /**
     * The maximum page size must be enforced.
     */
    @Test
    void shouldRejectPageSizeAboveMaximum() throws Exception {
        mockMvc.perform(get(ApiPaths.PRODUCTS).param("size", "1000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    /**
     * Malformed JSON must return 400, not an unhandled 500.
     */
    @Test
    void shouldRejectMalformedJsonBody() throws Exception {
        mockMvc.perform(post(ApiPaths.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not valid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }
}
