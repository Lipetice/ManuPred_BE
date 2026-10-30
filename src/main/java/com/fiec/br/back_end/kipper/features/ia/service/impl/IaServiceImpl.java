package com.fiec.br.back_end.kipper.features.ia.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiec.br.back_end.kipper.features.ia.client.ClienteIa;
import com.fiec.br.back_end.kipper.features.ia.exception.IaIndisponivelException;
import com.fiec.br.back_end.kipper.features.ia.exception.LimiteIaException;
import com.fiec.br.back_end.kipper.features.ia.model.dto.*;
import com.fiec.br.back_end.kipper.features.ia.service.IaService;
import com.fiec.br.back_end.kipper.features.solicitacao.model.dto.SolicitacaoResponseDTO;
import com.fiec.br.back_end.kipper.features.solicitacao.model.dto.SolicitacaoSearchFilterDTO;
import com.fiec.br.back_end.kipper.features.solicitacao.model.entities.Comentario;
import com.fiec.br.back_end.kipper.features.solicitacao.model.entities.Movimentacao;
import com.fiec.br.back_end.kipper.features.solicitacao.model.entities.Solicitacao;
import com.fiec.br.back_end.kipper.features.solicitacao.model.enums.PrioridadeSolicitacao;
import com.fiec.br.back_end.kipper.features.solicitacao.model.enums.StatusSolicitacao;
import com.fiec.br.back_end.kipper.features.solicitacao.repositories.*;
import com.fiec.br.back_end.kipper.features.solicitacao.service.SolicitacaoService;
import com.fiec.br.back_end.kipper.features.user.model.entities.Users;
import com.fiec.br.back_end.kipper.features.user.repositories.UserRepository;
import com.fiec.br.back_end.kipper.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IaServiceImpl implements IaService {

    private final ClienteIa clienteIa;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final TipoChamadoRepository tipoChamadoRepository;
    private final LocalRepository localRepository;
    private final SetorRepository setorRepository;
    private final SolicitacaoService solicitacaoService;

    // Novas dependências para o Resumo
    private final SolicitacaoRepository solicitacaoRepository;
    private final ComentarioRepository comentarioRepository;
    private final MovimentacaoRepository movimentacaoRepository;

    @Value("${app.ia.api-key}")
    private String apiKey;

    @Value("${app.ia.limite-por-minuto:10}")
    private int limitePorMinuto;

    private final Map<String, List<Long>> controleChamadas = new ConcurrentHashMap<>();

    @Override
    public IaStatusDTO verificarStatus() {
        return new IaStatusDTO(apiKey != null && !apiKey.isBlank());
    }

    @Override
    public IaBuscaResponseDTO buscarComLinguagemNatural(IaBuscaRequestDTO request, String emailUsuario) {
        verificarLimite(emailUsuario);

        String instrucao = construirPromptBusca();
        String respostaJson = clienteIa.gerarJson(instrucao, request.pergunta());
        FiltroIa filtroIa = parseJsonSeguro(respostaJson, FiltroIa.class);

        List<String> avisos = new ArrayList<>();
        Map<String, Object> filtrosAplicados = new HashMap<>();

        if (Boolean.TRUE.equals(filtroIa.foraDoEscopo())) {
            return new IaBuscaResponseDTO(
                    "Só consigo buscar chamados. Ex.: 'chamados urgentes abertos esta semana'.",
                    filtrosAplicados, avisos, Page.empty()
            );
        }

        // --- Validações e Traduções ---
        StatusSolicitacao status = null;
        if (filtroIa.status() != null) {
            try { status = StatusSolicitacao.valueOf(filtroIa.status()); filtrosAplicados.put("status", status); }
            catch (IllegalArgumentException e) { avisos.add("Status '" + filtroIa.status() + "' inválido, ignorado."); }
        }

        PrioridadeSolicitacao prioridade = null;
        if (filtroIa.prioridade() != null) {
            try { prioridade = PrioridadeSolicitacao.valueOf(filtroIa.prioridade()); filtrosAplicados.put("prioridade", prioridade); }
            catch (IllegalArgumentException e) { avisos.add("Prioridade '" + filtroIa.prioridade() + "' inválida, ignorada."); }
        }

        UUID tipoId = null;
        if (filtroIa.tipoNome() != null) {
            var tipo = tipoChamadoRepository.findAll().stream()
                    .filter(t -> t.getNome().equalsIgnoreCase(filtroIa.tipoNome()))
                    .findFirst().orElse(null);
            if (tipo != null) { tipoId = tipo.getId(); filtrosAplicados.put("tipo", tipo.getNome()); }
            else avisos.add("Tipo '" + filtroIa.tipoNome() + "' não encontrado; ignorado.");
        }

        UUID localId = null;
        if (filtroIa.localNome() != null) {
            var local = localRepository.findAll().stream()
                    .filter(l -> l.getNome().equalsIgnoreCase(filtroIa.localNome()))
                    .findFirst().orElse(null);
            if (local != null) { localId = local.getId(); filtrosAplicados.put("local", local.getNome()); }
            else avisos.add("Local '" + filtroIa.localNome() + "' não encontrado; ignorado.");
        }

        UUID setorId = null;
        if (filtroIa.setorNome() != null) {
            var setor = setorRepository.findAll().stream()
                    .filter(s -> s.getNome().equalsIgnoreCase(filtroIa.setorNome()))
                    .findFirst().orElse(null);
            if (setor != null) { setorId = setor.getId(); filtrosAplicados.put("setor", setor.getNome()); }
            else avisos.add("Setor '" + filtroIa.setorNome() + "' não encontrado; ignorado.");
        }

        List<UUID> solicitantesIds = resolverUsuarios(filtroIa.solicitanteNome(), "Solicitante", avisos, filtrosAplicados);
        List<UUID> tecnicosIds = resolverUsuarios(filtroIa.tecnicoNome(), "Técnico", avisos, filtrosAplicados);

        LocalDateTime aberturaDe = parseDataInicio(filtroIa.aberturaDe(), avisos, "Abertura (De)");
        LocalDateTime aberturaAte = parseDataFim(filtroIa.aberturaAte(), avisos, "Abertura (Até)");
        LocalDateTime finalizacaoDe = parseDataInicio(filtroIa.finalizacaoDe(), avisos, "Finalização (De)");
        LocalDateTime finalizacaoAte = parseDataFim(filtroIa.finalizacaoAte(), avisos, "Finalização (Até)");

        if (aberturaDe != null) filtrosAplicados.put("aberturaDe", filtroIa.aberturaDe());
        if (aberturaAte != null) filtrosAplicados.put("aberturaAte", filtroIa.aberturaAte());
        if (filtroIa.termo() != null) filtrosAplicados.put("termo", filtroIa.termo());

        SolicitacaoSearchFilterDTO filtroFinal = new SolicitacaoSearchFilterDTO(
                null, filtroIa.termo(), status, prioridade, filtroIa.numeroPatrimonio(), null,
                solicitantesIds, tecnicosIds, tipoId, localId, setorId, filtroIa.urgente(),
                aberturaDe, aberturaAte, finalizacaoDe, finalizacaoAte
        );

        int page = request.page() != null ? request.page() : 0;
        int size = request.size() != null ? request.size() : 20;
        Sort sort = "maisAntigos".equalsIgnoreCase(filtroIa.ordem())
                ? Sort.by("createdAt").ascending()
                : Sort.by("createdAt").descending();

        Page<SolicitacaoResponseDTO> resultados = solicitacaoService.search(filtroFinal, PageRequest.of(page, size, sort));

        return new IaBuscaResponseDTO(
                filtroIa.resumoDoPedido() != null ? filtroIa.resumoDoPedido() : "Busca gerada",
                filtrosAplicados, avisos, resultados
        );
    }

    @Override
    public OrganizarDescricaoResponseDTO organizarDescricao(OrganizarDescricaoRequestDTO request, String emailUsuario) {
        verificarLimite(emailUsuario);

        String instrucao = construirPromptDescricao();
        String textoCompleto = "Localização informada: " + (request.localizacao() != null ? request.localizacao() : "Não informada") + "\nTexto original: " + request.texto();

        String respostaJson = clienteIa.gerarJson(instrucao, textoCompleto);
        SugestaoDescricaoIa sugestao = parseJsonSeguro(respostaJson, SugestaoDescricaoIa.class);

        String tipoId = null;
        if (sugestao.tipoNome() != null) {
            var tipo = tipoChamadoRepository.findAll().stream()
                    .filter(t -> t.getNome().equalsIgnoreCase(sugestao.tipoNome()))
                    .findFirst().orElse(null);
            if (tipo != null) tipoId = tipo.getId().toString();
        }

        return new OrganizarDescricaoResponseDTO(
                sugestao.descricao() != null ? sugestao.descricao() : request.texto(),
                tipoId,
                sugestao.tipoNome(),
                sugestao.pareceUrgente() != null ? sugestao.pareceUrgente() : false,
                sugestao.motivoUrgencia()
        );
    }

    @Override
    public ResumoSolicitacaoResponseDTO gerarResumoSolicitacao(UUID solicitacaoId, String emailUsuario) {
        verificarLimite(emailUsuario);

        Solicitacao solicitacao = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado não encontrado."));

        // Monta o histórico para enviar à IA
        StringBuilder contexto = new StringBuilder();
        contexto.append("Título: ").append(solicitacao.getTitulo()).append("\n");
        contexto.append("Descrição: ").append(solicitacao.getDescricao()).append("\n");
        contexto.append("Status: ").append(solicitacao.getStatus()).append("\n");
        contexto.append("Tipo: ").append(solicitacao.getTipo() != null ? solicitacao.getTipo().getNome() : "Não informado").append("\n");
        contexto.append("Local: ").append(solicitacao.getLocalizacaoProblema()).append("\n");
        contexto.append("Data de Abertura: ").append(solicitacao.getCreatedAt()).append("\n");

        if (solicitacao.getDataFinalizacao() != null) {
            contexto.append("Data de Finalização: ").append(solicitacao.getDataFinalizacao()).append("\n");
        }

        contexto.append("\n--- HISTÓRICO DE MOVIMENTAÇÕES ---\n");
        List<Movimentacao> movs = movimentacaoRepository.findBySolicitacaoIdOrderByCreatedAtDesc(solicitacaoId);
        for (Movimentacao m : movs) {
            contexto.append("- ").append(m.getTitulo());
            if (m.getDescricao() != null) contexto.append(": ").append(m.getDescricao());
            contexto.append(" (").append(m.getCreatedAt()).append(")\n");
        }

        contexto.append("\n--- COMENTÁRIOS (Últimos 30) ---\n");
        // Pega os últimos 30 comentários para não estourar o limite da IA
        List<Comentario> comentarios = comentarioRepository.findBySolicitacaoIdOrderByCreatedAtAsc(solicitacaoId);
        int start = Math.max(0, comentarios.size() - 30);
        for (int i = start; i < comentarios.size(); i++) {
            Comentario c = comentarios.get(i);
            String autor = c.getAutor() != null ? c.getAutor().getName() : "Desconhecido";
            contexto.append("- ").append(autor).append(" (").append(c.getCreatedAt()).append("): ").append(c.getTexto()).append("\n");
        }

        String instrucao = """
            Resuma o histórico do chamado de manutenção predial em até 4 linhas para um técnico.
            Foque em: situação atual, o que já foi feito, o que falta e há quanto tempo está parado.
            Seja direto. Não invente informações.
            Responda SOMENTE com um objeto JSON neste formato: {"resumo": "seu texto aqui"}
            """;

        String respostaJson = clienteIa.gerarJson(instrucao, contexto.toString());
        ResumoIaDTO resumoIa = parseJsonSeguro(respostaJson, ResumoIaDTO.class);

        return new ResumoSolicitacaoResponseDTO(
                resumoIa.resumo(),
                LocalDateTime.now()
        );
    }

    // ============ MÉTODOS AUXILIARES ============

    private void verificarLimite(String email) {
        long agora = System.currentTimeMillis();
        controleChamadas.putIfAbsent(email, new ArrayList<>());
        List<Long> chamadas = controleChamadas.get(email);

        chamadas.removeIf(tempo -> agora - tempo > 60000);

        if (chamadas.size() >= limitePorMinuto) {
            throw new LimiteIaException("Você atingiu o limite de uso da IA. Aguarde um minuto.");
        }
        chamadas.add(agora);
    }

    private <T> T parseJsonSeguro(String json, Class<T> classe) {
        try {
            String limpo = json.replaceAll("```json", "").replaceAll("```", "").trim();
            return objectMapper.readValue(limpo, classe);
        } catch (Exception e) {
            log.error("Erro ao fazer parse do JSON da IA: {}", json);
            throw new IaIndisponivelException("A IA retornou um formato inválido.");
        }
    }

    private List<UUID> resolverUsuarios(String nome, String papel, List<String> avisos, Map<String, Object> filtros) {
        if (nome == null || nome.isBlank()) return null;

        List<Users> users = userRepository.findByNameContainingIgnoreCase(nome);
        if (users.isEmpty()) {
            avisos.add(papel + " '" + nome + "' não encontrado. O filtro foi ignorado.");
            return null;
        }

        if (users.size() > 1) {
            String nomes = users.stream().map(Users::getName).collect(Collectors.joining(", "));
            avisos.add("Há mais de um " + papel + " com o nome '" + nome + "': " + nomes + ". Mostrei os resultados de todos.");
        }

        filtros.put(papel.toLowerCase(), users.size() == 1 ? users.get(0).getName() : "Múltiplos (" + nome + ")");
        return users.stream().map(Users::getId).collect(Collectors.toList());
    }

    private LocalDateTime parseDataInicio(String dataString, List<String> avisos, String campo) {
        if (dataString == null || dataString.isBlank()) return null;
        try {
            return LocalDate.parse(dataString, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
        } catch (Exception e) {
            avisos.add("Data inválida em " + campo + " ('" + dataString + "'). Ignorada.");
            return null;
        }
    }

    private LocalDateTime parseDataFim(String dataString, List<String> avisos, String campo) {
        if (dataString == null || dataString.isBlank()) return null;
        try {
            return LocalDate.parse(dataString, DateTimeFormatter.ISO_LOCAL_DATE).atTime(LocalTime.MAX);
        } catch (Exception e) {
            avisos.add("Data inválida em " + campo + " ('" + dataString + "'). Ignorada.");
            return null;
        }
    }

    private String construirPromptBusca() {
        String hoje = LocalDate.now().toString();
        String tipos = tipoChamadoRepository.findAll().stream().map(t -> t.getNome()).collect(Collectors.joining(", "));
        String locais = localRepository.findAll().stream().map(l -> l.getNome()).collect(Collectors.joining(", "));
        String setores = setorRepository.findAll().stream().map(s -> s.getNome()).collect(Collectors.joining(", "));

        return """
            Você converte pedidos em português sobre chamados de manutenção predial em filtros de busca.
            Hoje é %s, fuso America/Sao_Paulo.

            Responda SOMENTE com um objeto JSON neste formato (use null para o que não foi pedido):
            {
              "foraDoEscopo": false,
              "resumoDoPedido": "frase curta descrevendo a busca",
              "termo": null,
              "status": null,
              "prioridade": null,
              "urgente": null,
              "tipoNome": null,
              "localNome": null,
              "setorNome": null,
              "solicitanteNome": null,
              "tecnicoNome": null,
              "numeroPatrimonio": null,
              "aberturaDe": null,
              "aberturaAte": null,
              "finalizacaoDe": null,
              "finalizacaoAte": null,
              "ordem": "maisRecentes"
            }

            Regras:
            - Datas no formato yyyy-MM-dd. Interprete expressões relativas a partir de hoje.
            - status deve ser: ABERTO, EM_ANDAMENTO, AGUARDANDO_ASSISTENCIA_EXTERNA, CONCLUIDO, FINALIZADA, REJEITADA, CANCELADO.
            - prioridade deve ser: BAIXA, MEDIA, ALTA, URGENTE.
            - tipoNome deve ser EXATAMENTE um destes ou null: [%s]
            - localNome deve ser EXATAMENTE um destes ou null: [%s]
            - setorNome deve ser EXATAMENTE um destes ou null: [%s]
            - solicitanteNome = quem abriu; tecnicoNome = técnico. Copie o nome como foi escrito.
            - Se o pedido não for sobre buscar chamados, responda {"foraDoEscopo": true} e nada mais.
            """.formatted(hoje, tipos, locais, setores);
    }

    private String construirPromptDescricao() {
        String tipos = tipoChamadoRepository.findAll().stream().map(t -> t.getNome()).collect(Collectors.joining(", "));
        return """
            Você ajuda usuários a descrever problemas de manutenção predial em uma escola.
            Recebe um texto informal e devolve SOMENTE um JSON:
            {
              "descricao": "texto reescrito",
              "tipoNome": "um dos tipos abaixo ou null",
              "pareceUrgente": false,
              "motivoUrgencia": null
            }
            Regras:
            - Reescreva em português correto, objetivo, em 1 a 3 frases. Mantenha TODOS os fatos.
            - Remova vícios de fala.
            - tipoNome deve ser EXATAMENTE um destes ou null: [%s]
            - pareceUrgente = true só para risco à segurança ou dano grave imediato. Explique em motivoUrgencia.
            """.formatted(tipos);
    }
}