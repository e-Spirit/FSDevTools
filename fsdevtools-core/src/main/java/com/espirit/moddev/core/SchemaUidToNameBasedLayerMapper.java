package com.espirit.moddev.core;

import de.espirit.firstspirit.transport.LayerMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SchemaUidToNameBasedLayerMapper implements LayerMapper {

    public static final String CREATE_NEW = "CREATE_NEW";
    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaUidToNameBasedLayerMapper.class);
    private static final String WILD_CARD = "*";
    private static final long serialVersionUID = -5844759858010789237L;
    private final Map<String, String> map;

    private SchemaUidToNameBasedLayerMapper(final Map<String, String> map) {
        if (map == null) {
            throw new LayerMappingException("Layer mapping is null!");
        }
        this.map = map;
    }

    /**
     * From.
     *
     * @param map the map
     * @return the layer mapper
     */
    public static LayerMapper from(final Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            throw new LayerMappingException("Layer mapping is null or empty!");
        }
        return new SchemaUidToNameBasedLayerMapper(new HashMap<>(map));
    }

    /**
     * Empty.
     *
     * @return the layer mapper
     */
    public static LayerMapper empty() {
        return new SchemaUidToNameBasedLayerMapper(Collections.<String, String>emptyMap());
    }

    @Override
    public String getLayer(final MappingContext context) {
        LOGGER.debug("getLayer() with context -> schema: {}", context.getSchema().getName());
        if (map.isEmpty()) {
            throw new LayerMappingException("Missing mapping for source layer '" + context.getSourceLayer()
            + "'! Please specify a layer mapping. For more information type 'fs-cli help import'.");
        }
        final String layer = figureOutTargetLayer(context);
        if (CREATE_NEW.equals(layer)) {
            LOGGER.debug("...new default layer!");
            return CREATE_NEW_DEFAULT_LAYER;
        }
        LOGGER.debug("...layer: {}", layer);
        return layer;
    }

    private String figureOutTargetLayer(final MappingContext context) {
        final String schemaUid = context.getSchema().getUid();
        if (map.containsKey(schemaUid)) {
            LOGGER.debug("...found mapping for '{}'", schemaUid);
            return map.get(schemaUid);
        }
        if (map.containsKey(WILD_CARD)) {
            LOGGER.debug("...use wild card!");
            return map.get(WILD_CARD);
        }
        throw new LayerMappingException("Missing mapping for source layer '" + context.getSourceLayer()
        + "'! Please specify a layer mapping. For more information type 'fs-cli help import'.");
    }
}
