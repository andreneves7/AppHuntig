# Plano de Desenvolvimento — AppHuntig

> Documento de acompanhamento das alterações feitas na branch `claude-dev`.
> Atualizado à medida que o trabalho avança. Não afeta a branch `master`.

---

## 1. Contexto e requisitos confirmados

- App para **caçadores** e **organizações de caça** (associações), com eventos, grupos e adesões.
- **3 tipos de acesso obrigatórios** (requisito confirmado pelo dono do projeto):
  1. **Caçador** (utilizador comum) — vê e inscreve-se em eventos, adere a grupos.
  2. **Organização** — cria eventos, gere sócios e admissões da sua própria organização.
  3. **SuperUser / Admin** (o dono da plataforma) — acesso total a todas as organizações, todos os utilizadores e todos os dados. Só deve existir um número reduzido de contas com este papel.
- Objetivo futuro: disponibilizar a app a mais pessoas, com possível portal web de administração e app iOS — **decisão de stack para essa fase em aberto, a confirmar separadamente** (ver secção 6).

---

## 2. Problemas identificados no código atual

> **Atualização:** o ponto #2 original ("app usa Firestore e Realtime Database ao mesmo tempo") foi investigado a fundo e **não era o problema real**. Confirmação: em toda a base de código, todas as referências a `FirebaseFirestore`/`.collection(...)` eram imports não usados ou código comentado/morto (incluindo um bloco de 161 linhas em `HomeActivity.kt`). **A app corre inteiramente sobre o Realtime Database** — nunca existiu duplicação de dados ativa, era resíduo de uma tentativa de migração para Firestore que foi abandonada a meio. Já foi tudo limpo (imports, código morto, e a dependência `firebase-firestore` removida do `build.gradle`). Não há necessidade de "unificar" nada — só resta decidir se, no futuro, faz sentido migrar de Realtime DB para Firestore de propósito (não é urgente).

| # | Problema | Ficheiro(s) | Risco/Impacto |
|---|---|---|---|
| 1 | `google-services.json` (chave de configuração Firebase) está versionado no Git, incluindo no histórico | `app/google-services.json` | Médio — não é a chave secreta do servidor, mas não é boa prática expô-la publicamente; API keys do Firebase devem ser restringidas nas definições da Google Cloud Console |
| 2 | ~~Uso simultâneo de Firebase Realtime Database e Cloud Firestore~~ **[CORRIGIDO — ver nota abaixo]** | — | — |
| 3 | Uso de `kotlin-android-extensions` / `kotlinx.android.synthetic`, **descontinuado** pelo JetBrains | `build.gradle`, todas as Activities | Médio — deixará de compilar em versões futuras do Kotlin/Android Gradle Plugin |
| 4 | Biblioteca `Anko`, **sem manutenção desde 2019** | `build.gradle` | Médio — risco de segurança/compatibilidade a prazo |
| 5 | Não existe atualmente nenhum papel de **SuperUser/Admin** nem regras de segurança Firebase visíveis no repositório | — | Alto — é um requisito novo confirmado, tem de ser desenhado de raiz |
| 6 | Bugs conhecidos assinalados nos próprios commits do autor (ex: aceitação em grupos só permitia 1 utilizador) | `GrupoActivity.kt` | A validar e corrigir |
| 7 | Muito código comentado e blocos mortos (ex: em `HomeActivity.kt`) | Várias Activities | Baixo — limpeza, legibilidade |
| 8 | Sem regras de segurança do Firestore/Realtime DB no repositório (não sabemos se estão abertas/inseguras no projeto real) | — | **Crítico — a validar diretamente na consola Firebase, ver secção 4** |

---

## 3. Desenho do sistema de papéis (roles)

Campo novo a adicionar ao documento do utilizador (`Users/{uid}`):

```
role: "cacador" | "organizacao" | "superadmin"
```

- Substitui a lógica atual baseada apenas no booleano `Org`, que não suporta um terceiro papel.
- Compatibilidade: mantém-se `Org` como campo derivado/legado durante a transição, para não partir nada que já leia esse campo, e remove-se depois de todas as Activities migradas.
- **SuperUser**: conta(s) criadas manualmente pelo dono do projeto diretamente na consola Firebase (nunca por registo público). Sugiro claramente definir 1 a 2 contas apenas.

---

## 4. Alterações necessárias no Firebase (a fazer por ti na consola, ou a validar comigo)

Isto **não posso testar/aplicar diretamente** — não tenho acesso à tua consola Firebase. Vou deixando aqui os passos exatos para aplicares (ou dares-me acesso temporário, se preferires).

### 4.1 Regras de segurança do Realtime Database

**Ficheiro real, pronto a aplicar:** [`database.rules.json`](../database.rules.json) (na raiz do repositório). Substitui o rascunho anterior — este já reflete os caminhos e campos reais confirmados no código (`Users`, `Grupos`, `Eventos`, e os fluxos de escrita cruzada como aceitar sócios ou pedir adesão).

**O que cada regra garante:**
- `Users`: qualquer utilizador autenticado pode ler (necessário — a app lista todos os utilizadores em vários sítios); só o próprio, o superadmin, ou uma organização podem escrever no registo de outra pessoa (necessário para a organização aceitar sócios).
- `Grupos`: leitura livre para autenticados; escrita restrita ao `admin` desse grupo específico ou ao superadmin; exceção explícita para `Pendentes/{uid}`, que o próprio utilizador pode escrever (pedido de adesão).
- `Eventos`: leitura livre para autenticados; escrita do evento em si restrita a organizações/superadmin; exceção explícita para `Presenças`, que qualquer autenticado pode escrever (inscrição num evento).

**⚠️ Limitação conhecida (não resolvida, por design das Realtime Database rules):** a regra que permite a uma organização escrever em `Users/{uid}/Grupos` não está limitada a "só o grupo que essa organização administra" — permite a qualquer conta com `role: organizacao` escrever ali. Isto acontece porque `Grupos` é indexado pelo **nome** do grupo, mas `Users/{uid}/Grupos` é indexado pelo **número** do grupo — as regras do Realtime Database não conseguem fazer essa referência cruzada (não há como "pesquisar por valor"), ao contrário do Firestore. É uma melhoria real na segurança (antes: qualquer pessoa, agora: só organizações), mas não é perfeito. Para resolver bem, precisaria de alinhar as duas chaves (usar sempre o número do grupo como chave em `Grupos` também) — fica registado como possível melhoria futura, não bloqueia a aplicação destas regras agora.

**Como aplicar (tens de o fazer tu — não tenho acesso à tua consola Firebase):**
1. Firebase Console → o teu projeto → Realtime Database → separador "Regras" (Rules)
2. Copia o conteúdo de `database.rules.json` e cola no editor
3. Usa o botão "Simulador de regras" (Rules Playground) para testar alguns casos antes de publicar — por exemplo, simula uma leitura sem autenticação (deve falhar) e uma escrita de um utilizador no seu próprio registo (deve passar)
4. Clica em "Publicar" (Publish)

Documentação oficial: https://firebase.google.com/docs/database/security

### 4.2 Passos que **tu** precisas de validar/fazer
1. Confirmar na Firebase Console se já existem regras de segurança ativas (Firestore → Regras).
2. Restringir a API Key do `google-services.json` na Google Cloud Console (Credentials → restringir por app/pacote).
3. Criar manualmente o(s) documento(s) `Users/{teu-uid}` com `role: "superadmin"`.
4. ~~Decidir: mantemos os dados espalhados por Realtime Database e Firestore...~~ **Resolvido:** a app usa só Realtime Database (ver nota na secção 2). Regras de segurança a aplicar são as do **Realtime Database**, não as do Firestore — o rascunho na secção 4.1 acima está escrito em sintaxe Firestore e precisa de ser reescrito em sintaxe de regras do Realtime Database antes de aplicares (formato JSON, não `service cloud.firestore`). Documentação oficial: https://firebase.google.com/docs/database/security

---

## 5. Plano de trabalho (código, branch `claude-dev`)

- [x] Adicionar `.gitignore` para ficheiros sensíveis
- [x] Corrigir bug de duplicação na lista de admissão de sócios (`AdmissaoActivity.kt`) — a causa real era um `addValueEventListener` (contínuo) que reconstruía a lista sem a limpar, e não propriamente o `GrupoActivity.kt`
- [x] Remover **todos** os `TODO("Not yet implemented")` de callbacks Firebase (11 ficheiros, ~60 ocorrências) — cada um destes rebentava a app (`NotImplementedError`) se o evento correspondente disparasse. Substituídos por `Log.d` seguro.
- [x] Introduzir campo `role` (`Roles.kt`) com resolução de compatibilidade com o campo legado `Org`
- [x] Atualizar `LoginActivity` (login caçador e login organização) para ler e encaminhar por `role`
- [x] Atualizar `RegistoUserActivity` para gravar `role: "cacador"` nas novas contas
- [x] Criar `SuperAdminActivity` v1 — lista todas as organizações da plataforma
- [ ] `SuperAdminActivity` v2 — ver/gerir todos os utilizadores e grupos de qualquer organização (bloqueado por: os ecrãs existentes de organização assumem que o utilizador autenticado É o admin dessa organização; precisa de nova lógica de acesso, não reutilização direta)
- [x] ~~Unificar acesso a dados~~ — não era necessário; Firestore nunca esteve ativo, era só código morto (removido: imports, ~170 linhas de código comentado, e a dependência do `build.gradle`)
- [x] Migrar `kotlinx.android.synthetic` → View Binding (17/17 ficheiros — ver nota abaixo)
- [x] Substituir dependências Anko por alternativas atuais (Anko só era realmente usado num único ficheiro, `VerificarLoginActivity.kt`; removido por completo, incluindo do `build.gradle`)

### Migração para View Binding — detalhes
Todos os 17 ficheiros `.kt` que usavam `kotlinx.android.synthetic` (incluindo os que inflavam diálogos customizados como `adesao_custom_view`, `custom_view`, `email_custom_view`, `pass_custom_view`) foram convertidos para View Binding. O plugin descontinuado `kotlin-android-extensions` foi removido do `build.gradle`.

⚠️ **Bug pré-existente encontrado durante a migração (não introduzido por mim):** o layout `activity_registo_user.xml` tem **dois IDs duplicados** — `scrollView3` e `layoutVer2` aparecem cada um duas vezes no mesmo ficheiro. Isto já existia antes (o sistema antigo `synthetic` também sofria disto, silenciosamente). Com View Binding, tal como acontecia antes, o binding aponta sempre para a mesma vista (a primeira encontrada), portanto **não é um problema novo introduzido agora**, mas fica assinalado porque provavelmente há um segundo bloco de UI nesse ecrã que nunca é acedido corretamente. Recomendo dares uma vista de olhos a esse layout com calma.

**Validação necessária antes do merge:** esta foi a alteração de maior volume (17 ficheiros, ~150 substituições de IDs). Fiz verificação sistemática por scripts (procura de imports `kotlinx` residuais — zero encontrados; procura de duplo-prefixo `binding.binding.` por erro de substituição — zero encontrados), mas **não consegui compilar nada disto**. É essencial correres `./gradlew build` no Android Studio antes de fazeres merge para `master`.
- [ ] Rever/corrigir layouts XML afetados
- [ ] Testar fluxos principais (login caçador, login organização, login superadmin, criação de evento, adesão a grupo) — **ver nota abaixo sobre limitação de teste**
- [x] Regras de segurança do Realtime Database (`database.rules.json`) — ver secção 4.1
- [x] Recuperação de password (`LoginActivity` — "Esqueceu-se da password?")
- [x] Validação de email, password, e comprimento de campos no registo (`RegistoUserActivity`) — corrigido também um bug em que a falha de validação não mostrava nada ao utilizador
- [x] Validação de datas de evento (fim não pode ser antes do início) — `EventoActivity`
- [x] Estados de loading + tratamento de erros visível (indicador de carregamento em `HomeActivity` como referência do padrão; 30 `onCancelled` silenciosos em 11 ficheiros passaram a mostrar Toast de erro ao utilizador)
- [ ] Alinhar as chaves de `Grupos` (nome → número)

### Erros visíveis (onCancelled) — detalhes
Criado `Utils.kt` com a função `Context.mostrarErroLigacao()`, reutilizada em todos os `onCancelled` que antes só escreviam num `Log.d` silencioso (30 ocorrências em 11 ficheiros). Isto é especialmente relevante depois de aplicares as regras de segurança do `database.rules.json` — se uma leitura for recusada, o utilizador agora vê um aviso em vez de um ecrã parado sem explicação.

### Loading state — detalhes
Adicionado `ProgressBar` (`progressHome`) a `activity_home.xml`/`HomeActivity.kt` como implementação de referência: aparece ao iniciar o carregamento, desaparece assim que chega o primeiro evento (ou ao fim de 5 segundos, para nunca ficar preso a girar se a lista estiver mesmo vazia — o `ChildEventListener` do Realtime Database não tem um callback nativo de "carregamento inicial concluído", ao contrário do `addListenerForSingleValueEvent`).

**Ecrãs que ainda beneficiariam do mesmo padrão** (não feito por limitação de tempo, não de dificuldade — é mecânico, repetir o que está em `HomeActivity`): `OrgActivity`, `GrupoActivity`, `VerGrupoActivity`, `ListaGruposActivity`, `ListaSociosOrgActivity`, `AdmissaoActivity`.

### ⚠️ Nota sobre testes
Não foi possível compilar/correr a app neste ambiente de trabalho — a rede disponível não tem acesso aos repositórios Maven da Google/Maven Central, necessários para as dependências do Gradle (Firebase, AndroidX, Google Maps). Todo o trabalho acima foi feito por revisão estática cuidadosa (leitura linha a linha, verificação de imports, verificação de duplicação de nomes). **Antes de fazer merge para `master`, corre localmente no Android Studio**: `./gradlew build` (ou `assembleDebug`) para confirmar que compila, e testa manualmente os fluxos de login dos 3 papéis.

### Passo manual necessário no Firebase (para testar o SuperAdmin)
Como não existe fluxo de registo para organizações nem superadmin dentro da app (confirmado — só é possível criar contas "cacador" pelo registo normal), para testares o painel SuperAdmin:
1. Regista uma conta normal na app (fica com `role: "cacador"`)
2. Na Firebase Console → Realtime Database → `Users/{uid}`, muda manualmente `role` para `"superadmin"` e `Controlo` para `true`
3. Faz login novamente (usa o botão de login normal ou o de organização — ambos já reconhecem `superadmin`)

## 6. Ponto em aberto — stack para Web Admin + iOS

**Decidido (confirmado no chat):** manter o Android atual em Kotlin nativo. iOS e portal Web de administração ficam como projetos separados, a criar mais tarde noutras ferramentas, todos a falar com o mesmo backend Firebase. Nada disto é executado nesta branch.

---

*Última atualização: gerado automaticamente pela sessão de trabalho com Claude.*
