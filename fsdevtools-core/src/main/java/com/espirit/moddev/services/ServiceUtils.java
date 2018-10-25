package com.espirit.moddev.services;

import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.module.descriptor.AbstractDescriptor;
import de.espirit.firstspirit.module.descriptor.ServiceDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.espirit.firstspirit.module.descriptor.ComponentDescriptor.Type.SERVICE;

public class ServiceUtils {
    @NotNull
    public static List<String> getAllServiceNamesFromServer(@NotNull ModuleAdminAgent moduleAdminAgent) {
        return moduleAdminAgent
                .getModules()
                .stream()
                .flatMap(moduleDescriptor -> Stream.of(moduleDescriptor.getComponents()))
                .filter(it -> SERVICE.equals(it.getType()))
                .map(ServiceDescriptor.class::cast)
                .map(AbstractDescriptor::getName)
                .sorted()
                .collect(Collectors.toList());
    }
}
