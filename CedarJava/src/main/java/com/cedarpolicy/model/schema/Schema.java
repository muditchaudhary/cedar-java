/*
 * Copyright Cedar Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cedarpolicy.model.schema;

import com.cedarpolicy.loader.LibraryLoader;
import com.cedarpolicy.model.exception.InternalException;
import com.cedarpolicy.value.EntityUID;
import com.cedarpolicy.value.EntityTypeName;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

/** Represents a schema. */
public final class Schema {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        LibraryLoader.loadLibrary();
    }

    /** Is this schema in the JSON or Cedar format */
    public final JsonOrCedar type;

    /** This will be present if and only if `type` is `Json`. */
    public final Optional<JsonNode> schemaJson;

    /** This will be present if and only if `type` is `Cedar`. */
    public final Optional<String> schemaText;

    /**
     * If `type` is `Json`, `schemaJson` should be present and `schemaText` empty. If `type` is `Cedar`, `schemaText`
     * should be present and `schemaJson` empty. This constructor does not check that the input text represents a valid
     * JSON or Cedar schema. Use the `parse` function to ensure schema validity.
     *
     * @param type       The schema format used.
     * @param schemaJson Optional schema in the JSON schema format.
     * @param schemaText Optional schema in the Cedar schema format.
     */
    public Schema(JsonOrCedar type, Optional<String> schemaJson, Optional<String> schemaText) {
        this.type = type;
        this.schemaJson = schemaJson.map(jsonStr -> {
            try {
                return OBJECT_MAPPER.readTree(jsonStr);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
        this.schemaText = schemaText;
    }

    /**
     * Build a Schema from a json node. This does not check that the parsed JSON object represents a valid schema. Use
     * `parse` to check validity.
     *
     * @param schemaJson Schema in Cedar's JSON schema format.
     */
    public Schema(JsonNode schemaJson) {
        if (schemaJson == null) {
            throw new NullPointerException("schemaJson");
        }
        this.type = JsonOrCedar.Json;
        this.schemaJson = Optional.of(schemaJson);
        this.schemaText = Optional.empty();
    }

    /**
     * Build a Schema from a string. This does not check that the string represents a valid schema. Use `parse` to check
     * validity.
     *
     * @param schemaText Schema in the Cedar schema format.
     */
    public Schema(String schemaText) {
        if (schemaText == null) {
            throw new NullPointerException("schemaText");
        }
        this.type = JsonOrCedar.Cedar;
        this.schemaJson = Optional.empty();
        this.schemaText = Optional.of(schemaText);
    }

    public String toString() {
        if (type == JsonOrCedar.Json) {
            return "Schema(schemaJson=" + schemaJson.get() + ")";
        } else {
            return "Schema(schemaText=" + schemaText.get() + ")";
        }
    }

    /**
     * Get the Principals defined by the Schema
     *
     * @return the Principals defined by the Schema
     * @throws InternalException    if parsing fails.
     * @throws NullPointerException if the input text is null
     */
    public Iterable<EntityTypeName> getPrincipals() throws InternalException, NullPointerException {
        if (type == JsonOrCedar.Json) {
            return getSchemaPrincipalsJsonJni(schemaJson.get().toString());
        } else {
            return getSchemaPrincipalsJni(schemaText.get());
        }
    }

    /**
     * Get the Resources defined by the Schema
     *
     * @return the Resources defined by the Schema
     * @throws InternalException    if parsing fails.
     * @throws NullPointerException if the input text is null
     */
    public Iterable<EntityTypeName> getResources() throws InternalException, NullPointerException {
        if (type == JsonOrCedar.Json) {
            return getSchemaResourcesJsonJni(schemaJson.get().toString());
        } else {
            return getSchemaResourcesJni(schemaText.get());
        }
    }

    /**
     * Get the Principals for a specific Action defined by the Schema
     *
     * @return the Principals for a specific Action defined by the Schema
     * @throws InternalException    if parsing fails.
     * @throws NullPointerException if the input text is null
     */
    public Iterable<EntityTypeName> getPrincipalsForAction(EntityUID action)
            throws InternalException, NullPointerException {
        if (type == JsonOrCedar.Json) {
            return getSchemaPrincipalsForActionJsonJni(schemaJson.get().toString(), action);
        } else {
            return getSchemaPrincipalsForActionJni(schemaText.get(), action);
        }
    }

    /**
     * Get the Resources for a specific Action defined by the Schema
     *
     * @return the Resources for a specific Action defined by the Schema
     * @throws InternalException    if parsing fails.
     * @throws NullPointerException if the input text is null
     */
    public Iterable<EntityTypeName> getResourcesForAction(EntityUID action)
            throws InternalException, NullPointerException {
        if (type == JsonOrCedar.Json) {
            return getSchemaResourcesForActionJsonJni(schemaJson.get().toString(), action);
        } else {
            return getSchemaResourcesForActionJni(schemaText.get(), action);
        }
    }

    /**
     * Get the Entity Types defined by the Schema
     *
     * @return the Entity Types defined by the Schema
     * @throws InternalException    if parsing fails.
     * @throws NullPointerException if the input text is null
     */
    public Iterable<EntityTypeName> getEntityTypes() throws InternalException, NullPointerException {
        if (type == JsonOrCedar.Json) {
            return getSchemaEntityTypesJsonJni(schemaJson.get().toString());
        } else {
            return getSchemaEntityTypesJni(schemaText.get());
        }
    }

    /**
     * Get the Actions defined by the Schema
     *
     * @return the Actions defined by the Schema
     * @throws InternalException    if parsing fails.
     * @throws NullPointerException if the input text is null
     */
    public Iterable<EntityUID> getActions() throws InternalException, NullPointerException {
        if (type == JsonOrCedar.Json) {
            return getActionsJsonJni(schemaJson.get().toString());
        } else {
            return getActionsJni(schemaText.get());
        }
    }

    /**
     * Get the Action Groups defined by the Schema
     *
     * @return the Action Groups defined by the Schema
     * @throws InternalException    if parsing fails.
     * @throws NullPointerException if the input text is null
     */
    public Iterable<EntityUID> getActionGroups() throws InternalException, NullPointerException {
        if (type == JsonOrCedar.Json) {
            return getActionGroupsJsonJni(schemaJson.get().toString());
        } else {
            return getActionGroupsJni(schemaText.get());
        }
    }

    /**
     * Try to parse a string representing a JSON or Cedar schema. If parsing succeeds, return a `Schema`, otherwise
     * raise an exception.
     *
     * @param type The schema format used.
     * @param str  Schema text to parse.
     * @throws InternalException    If parsing fails.
     * @throws NullPointerException If the input text is null.
     * @return A {@link Schema} that is guaranteed to be valid.
     */
    public static Schema parse(JsonOrCedar type, String str) throws InternalException, NullPointerException {
        if (type == JsonOrCedar.Json) {
            parseJsonSchemaJni(str);
            return new Schema(JsonOrCedar.Json, Optional.of(str), Optional.empty());
        } else {
            parseCedarSchemaJni(str);
            return new Schema(JsonOrCedar.Cedar, Optional.empty(), Optional.of(str));
        }

    }

    /** Specifies the schema format used. */
    public enum JsonOrCedar {
        /**
         * Cedar JSON schema format. See <a href="https://docs.cedarpolicy.com/schema/json-schema.html">
         * https://docs.cedarpolicy.com/schema/json-schema.html</a>
         */
        Json,
        /**
         * Cedar schema format. See <a href="https://docs.cedarpolicy.com/schema/human-readable-schema.html">
         * https://docs.cedarpolicy.com/schema/human-readable-schema.html</a>
         */
        Cedar
    }

    private static native String parseJsonSchemaJni(String schemaJson) throws InternalException, NullPointerException;

    private static native String parseCedarSchemaJni(String schemaText) throws InternalException, NullPointerException;

    private static native List<EntityTypeName> getSchemaPrincipalsJni(String schemaText)
            throws InternalException, NullPointerException;

    private static native List<EntityTypeName> getSchemaPrincipalsJsonJni(String schemaJsonText)
            throws InternalException, NullPointerException;

    private static native List<EntityTypeName> getSchemaResourcesJni(String schemaText)
            throws InternalException, NullPointerException;

    private static native List<EntityTypeName> getSchemaResourcesJsonJni(String schemaJsonText)
            throws InternalException, NullPointerException;

    private static native List<EntityTypeName> getSchemaPrincipalsForActionJni(String schemaText, EntityUID action)
            throws InternalException, NullPointerException;

    private static native List<EntityTypeName> getSchemaPrincipalsForActionJsonJni(String schemaJsonText,
            EntityUID action) throws InternalException, NullPointerException;

    private static native List<EntityTypeName> getSchemaResourcesForActionJni(String schemaText, EntityUID action)
            throws InternalException, NullPointerException;

    private static native List<EntityTypeName> getSchemaResourcesForActionJsonJni(String schemaJsonText,
            EntityUID action) throws InternalException, NullPointerException;

    private static native List<EntityTypeName> getSchemaEntityTypesJni(String schemaText)
            throws InternalException, NullPointerException;

    private static native List<EntityTypeName> getSchemaEntityTypesJsonJni(String schemaJsonText)
            throws InternalException, NullPointerException;

    private static native List<EntityUID> getActionsJni(String schemaText)
            throws InternalException, NullPointerException;

    private static native List<EntityUID> getActionsJsonJni(String schemaText)
            throws InternalException, NullPointerException;

    private static native List<EntityUID> getActionGroupsJni(String schemaText)
            throws InternalException, NullPointerException;

    private static native List<EntityUID> getActionGroupsJsonJni(String schemaText)
            throws InternalException, NullPointerException;
}
