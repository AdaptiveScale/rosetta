/*
 *  Copyright 2022 AdaptiveScale
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.adaptivescale.rosetta.common.helpers;

import com.adaptivescale.rosetta.common.annotations.RosettaModule;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;
import org.reflections.Reflections;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ModuleLoader {

    public static Optional<Class<?>> loadModuleByAnnotationClassValues(
            String packageName,
            RosettaModuleTypes annotationType,
            String annotationName
    ) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(RosettaModule.class, true);
        Optional<Class<?>> classFound = typesAnnotatedWith.stream().filter(cls -> {
            RosettaModule annotation = cls.getAnnotation(RosettaModule.class);
            return annotation.type().equals(annotationType) && annotation.name().equals(annotationName);
        }).reduce((a, b) -> null);
        return classFound;
    }

    public static List<Class<?>> loadModuleByAnnotationType(
            String packageName,
            RosettaModuleTypes annotationType
    ) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(RosettaModule.class, true);
        List<Class<?>> classList = typesAnnotatedWith.stream().filter(cls -> {
            RosettaModule annotation = cls.getAnnotation(RosettaModule.class);
            return annotation.type().equals(annotationType);
        }).collect(Collectors.toList());
        return classList;
    }
}
