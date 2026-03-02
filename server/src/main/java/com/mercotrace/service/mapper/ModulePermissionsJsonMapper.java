package com.mercotrace.service.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercotrace.service.dto.RoleDTO.ModulePermissionEntry;
import java.util.Collections;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Small helper to convert module-permissions maps to and from a JSON string stored on the Role entity.
 *
 * This keeps JSON handling isolated from the JPA entity and allows the mapper layer to stay focused on
 * DTO ↔ entity translation.
 */
public final class ModulePermissionsJsonMapper {

    private static final Logger LOG = LoggerFactory.getLogger(ModulePermissionsJsonMapper.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final TypeReference<Map<String, ModulePermissionEntry>> TYPE_REFERENCE =
        new TypeReference<Map<String, ModulePermissionEntry>>() {};

    private ModulePermissionsJsonMapper() {}

    public static Map<String, ModulePermissionEntry> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return OBJECT_MAPPER.readValue(json, TYPE_REFERENCE);
        } catch (Exception e) {
            LOG.warn("Failed to deserialize module permissions JSON, returning empty map", e);
            return Collections.emptyMap();
        }
    }

    public static String toJson(Map<String, ModulePermissionEntry> value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            LOG.warn("Failed to serialize module permissions to JSON, returning null", e);
            return null;
        }
    }
}

