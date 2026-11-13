/*
 * swagger-filtro.js
 *
 * Melhora a busca do Swagger. A busca padrão só procura no NOME DA SEÇÃO (tag), diferencia
 * maiúsculas e acentos: buscar "comentario" não achava nada.
 *
 * Com este plugin a busca procura na seção, na rota, no método (GET, POST...), no nome e na
 * descrição de cada endpoint, sem diferenciar maiúsculas nem acentos. Com várias palavras,
 * mostra só o que tiver todas elas (ex.: "admin tipos").
 */
(function () {
    function normalizar(texto) {
        return String(texto || "")
            .normalize("NFD")
            .replace(/[̀-ͯ]/g, "")
            .toLowerCase();
    }

    function textoDaOperacao(op) {
        return normalizar([
            op.get("path"),
            op.get("method"),
            op.getIn(["operation", "summary"]),
            op.getIn(["operation", "description"]),
            op.getIn(["operation", "operationId"])
        ].join(" "));
    }

    window.FiltroAvancadoPlugin = function () {
        return {
            fn: {
                opsFilter: function (taggedOps, frase) {
                    var palavras = normalizar(frase).split(/\s+/).filter(Boolean);
                    if (palavras.length === 0) return taggedOps;

                    return taggedOps
                        .map(function (tagObj, nomeDaTag) {
                            var tag = normalizar(nomeDaTag);
                            var operacoes = tagObj.get("operations").filter(function (op) {
                                var texto = tag + " " + textoDaOperacao(op);
                                return palavras.every(function (p) { return texto.indexOf(p) !== -1; });
                            });
                            return tagObj.set("operations", operacoes);
                        })
                        .filter(function (tagObj) { return tagObj.get("operations").size > 0; });
                }
            }
        };
    };

    // Troca o texto de ajuda do campo de busca ("Filter by tag") por um em português
    var DICA = "Buscar por rota, nome ou descrição (ex.: comentario, ia, patch, admin tipos)";
    function ajustarCampoDeBusca() {
        var campo = document.querySelector(".swagger-ui .operation-filter-input");
        if (campo && campo.getAttribute("placeholder") !== DICA) {
            campo.setAttribute("placeholder", DICA);
        }
    }
    new MutationObserver(ajustarCampoDeBusca).observe(document.documentElement, { childList: true, subtree: true });
})();
