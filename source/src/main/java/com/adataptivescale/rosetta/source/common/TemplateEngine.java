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

package com.adataptivescale.rosetta.source.common;

import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.Map;

public class TemplateEngine {
    private static TemplateEngine instance = null;
    private org.thymeleaf.TemplateEngine engine = null;

    private TemplateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setTemplateMode(TemplateMode.TEXT);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".sqlt");
        engine = new org.thymeleaf.TemplateEngine();
        engine.setTemplateResolver(resolver);
    }

    public static TemplateEngine get() {
        if(TemplateEngine.instance == null) {
            TemplateEngine.instance = new TemplateEngine();
        }
        return TemplateEngine.instance;
    }

    public static String processString(String templateName, Map<String, Object> variables) {
        org.thymeleaf.TemplateEngine templateEngine = new org.thymeleaf.TemplateEngine();
        StringTemplateResolver templateResolver = new   StringTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateEngine.setTemplateResolver(templateResolver);

        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

    public static String process(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return get().engine.process(templateName, context);
    }
}
