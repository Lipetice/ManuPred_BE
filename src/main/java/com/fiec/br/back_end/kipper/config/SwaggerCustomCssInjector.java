package com.fiec.br.back_end.kipper.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Personaliza a página do Swagger:
 *  - index.html: adiciona o CSS do tema (swagger-theme.css) e o script da busca (swagger-filtro.js);
 *  - swagger-initializer.js: registra o plugin de busca no Swagger UI.
 */
public class SwaggerCustomCssInjector extends SwaggerIndexPageTransformer {

    private static final String INDEX = "index.html";
    private static final String INITIALIZER = "swagger-initializer.js";

    public SwaggerCustomCssInjector(
            final SwaggerUiConfigProperties swaggerUiConfig,
            final SwaggerUiOAuthProperties swaggerUiOAuthProperties,
            final SwaggerWelcomeCommon swaggerWelcomeCommon,
            final ObjectMapperProvider objectMapperProvider) {
        super(swaggerUiConfig, swaggerUiOAuthProperties, swaggerWelcomeCommon, objectMapperProvider);
    }

    @Override
    public @NonNull Resource transform(
            @NonNull HttpServletRequest request,
            @NonNull Resource resource,
            @NonNull ResourceTransformerChain transformer) throws IOException {

        if (INDEX.equals(resource.getFilename())) {
            String html = ler(resource);
            return new TransformedResource(resource, injetarNoHead(request, html).getBytes(StandardCharsets.UTF_8));
        }

        // Deixa o springdoc aplicar a configuração dele primeiro
        Resource resultado = super.transform(request, resource, transformer);

        if (INITIALIZER.equals(resource.getFilename())) {
            String js = ler(resultado);
            return new TransformedResource(resource, registrarPluginDeBusca(js).getBytes(StandardCharsets.UTF_8));
        }
        return resultado;
    }

    private String injetarNoHead(HttpServletRequest request, String html) {
        String prefix = request.getContextPath();
        if (prefix == null || prefix.isBlank()) {
            String forwarded = request.getHeader("X-Forwarded-Prefix");
            if (forwarded != null && !forwarded.isBlank()) {
                prefix = forwarded.trim();
            } else {
                prefix = "";
            }
        }
        if (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        String tags = "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + prefix + "/swagger-theme.css\" />"
                + "<script src=\"" + prefix + "/swagger-filtro.js\"></script>";
        return html.replace("</head>", tags + "</head>");
    }

    /**
     * Adiciona o plugin de busca à lista de plugins do Swagger UI. Se o script não carregar,
     * a lista fica igual à original e o Swagger continua funcionando com a busca padrão.
     */
    private String registrarPluginDeBusca(String js) {
        return js.replaceFirst("plugins:\\s*\\[",
                "plugins: [...(window.FiltroAvancadoPlugin ? [window.FiltroAvancadoPlugin] : []), ");
    }

    private String ler(Resource resource) throws IOException {
        try (InputStream in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
