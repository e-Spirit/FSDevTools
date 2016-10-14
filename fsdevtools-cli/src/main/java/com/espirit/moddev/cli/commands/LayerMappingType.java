package com.espirit.moddev.cli.commands;

import de.espirit.firstspirit.transport.LayerMapper;
import de.espirit.firstspirit.transport.LayerMapper.LayerNameBasedLayerMapper;
import de.espirit.firstspirit.transport.LayerMapper.SchemaUidBasedLayerMapper;

import java.util.Map;
import java.util.function.Function;

public enum LayerMappingType {
    LAYER_NAME_BASED(LayerNameBasedLayerMapper::from), SCHEMA_UID_BASED(SchemaUidBasedLayerMapper::from);

    private final Function<Map<String, String>, LayerMapper> layerMapper;

    LayerMappingType(Function<Map<String, String>, LayerMapper> layerMapper) {
        this.layerMapper = layerMapper;
    }

    public LayerMapper getLayerMapper(Map<String, String> layerMapping) {
        return layerMapper.apply(layerMapping);
    }
}