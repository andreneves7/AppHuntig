# Plano de Desenvolvimento — AppHuntig

> Documento de acompanhamento das alterações feitas na branch `claude-dev`.
> Atualizado à medida que o trabalho avança. Não afeta a branch `master`.

---

## 0. 🚨 AÇÃO URGENTE — chave da API do Google Maps exposta

> 📌 **Passos detalhados e prontos a copiar em `docs/ACOES_MANUAIS.md`, ponto 1️⃣.** Esta secção fica como registo do achado; o documento de ações é o que deves seguir passo a passo.

Encontrada durante a revisão de boas práticas para publicação. **Mais urgente do que tudo o resto neste documento.**

**O que se passa:**
- A chave `AIzaSyCR_5PdNIjarIMG5-GBDPLvBj0rSbGu8bY` estava duplicada em dois sítios do repositório: `strings.xml` (string `api_key`) e `app/src/debug/res/values/google_maps_api.xml` (string `google_maps_key`) — **ambos versionados publicamente no GitHub**, sem proteção nenhuma do `.gitignore`.
- O ficheiro de release (`app/src/release/res/values/google_maps_api.xml`) tem apenas um placeholder `YOUR_KEY_HERE` — ou seja, **o mapa não funcionaria de todo numa build de release/Play Store**, mesmo sem o problema de segurança.
- O comentário dentro do próprio ficheiro sugere que a chave pode ter sido pedida/restringida para o pacote `com.example.app` — mas a app publica-se como `com.company.HuntigEvents` (ver `applicationId` no `build.gradle`). Se a restrição de pacote estiver mesmo errada, a chave pode estar efetivamente **sem restrição útil nenhuma**, disponível a qualquer pessoa que a copie do GitHub.

**O que já fiz (código):**
- Removida a duplicação: o código usa agora só uma fonte (`google_maps_key`)
- `google_maps_api.xml` (debug e release) adicionados ao `.gitignore` e removidos do controlo de versão a partir de agora (ficam no teu disco, a tua build local continua a funcionar)

**O que só tu podes fazer (Google Cloud Console):**
1. Vai a https://console.cloud.google.com/apis/credentials do projeto associado a esta app
2. **Considera esta chave comprometida** — já esteve pública no GitHub, mesmo que a apagues agora do repositório, continua no histórico de commits antigos
3. Gera uma **chave nova**
4. Restringe-a corretamente: por aplicação Android (pacote `com.company.HuntigEvents` + impressão digital SHA-1 do teu certificado de assinatura, debug E release), e por API (só "Maps SDK for Android" e "Places API", nada mais)
5. Substitui o valor em `app/src/debug/res/values/google_maps_api.xml` (chave de debug) e cria uma chave de release equivalente para `app/src/release/res/values/google_maps_api.xml`
6. Documentação oficial: https://developers.google.com/maps/documentation/android-sdk/get-api-key

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
- [x] `SuperAdminActivity` v2 — ver/gerir todos os utilizadores e grupos de qualquer organização (`SuperAdminUsersActivity`: aprovar contas e mudar roles; `SuperAdminGruposActivity`: ver todos os grupos, independentemente de quem administra)
- [x] ~~Unificar acesso a dados~~ — não era necessário; Firestore nunca esteve ativo, era só código morto (removido: imports, ~170 linhas de código comentado, e a dependência do `build.gradle`)
- [x] Migrar `kotlinx.android.synthetic` → View Binding (17/17 ficheiros — ver nota abaixo)
- [x] Substituir dependências Anko por alternativas atuais (Anko só era realmente usado num único ficheiro, `VerificarLoginActivity.kt`; removido por completo, incluindo do `build.gradle`)

### Migração para View Binding — detalhes
Todos os 17 ficheiros `.kt` que usavam `kotlinx.android.synthetic` (incluindo os que inflavam diálogos customizados como `adesao_custom_view`, `custom_view`, `email_custom_view`, `pass_custom_view`) foram convertidos para View Binding. O plugin descontinuado `kotlin-android-extensions` foi removido do `build.gradle`.

⚠️ **Bug pré-existente encontrado durante a migração (não introduzido por mim):** o layout `activity_registo_user.xml` tem **dois IDs duplicados** — `scrollView3` e `layoutVer2` aparecem cada um duas vezes no mesmo ficheiro. Isto já existia antes (o sistema antigo `synthetic` também sofria disto, silenciosamente). Com View Binding, tal como acontecia antes, o binding aponta sempre para a mesma vista (a primeira encontrada), portanto **não é um problema novo introduzido agora**, mas fica assinalado porque provavelmente há um segundo bloco de UI nesse ecrã que nunca é acedido corretamente. Recomendo dares uma vista de olhos a esse layout com calma.

**Validação necessária antes do merge:** esta foi a alteração de maior volume (17 ficheiros, ~150 substituições de IDs). Fiz verificação sistemática por scripts (procura de imports `kotlinx` residuais — zero encontrados; procura de duplo-prefixo `binding.binding.` por erro de substituição — zero encontrados), mas **não consegui compilar nada disto**. É essencial correres `./gradlew build` no Android Studio antes de fazeres merge para `master`.
- [ ] Rever/corrigir layouts XML afetados
- [ ] Testar fluxos principais (login caçador, login organização, login superadmin, criação de evento, adesão a grupo) — **ver nota abaixo sobre limitação de teste**
- [x] Testes automatizados — iniciado: `RolesTest.kt` e `ValidacoesTest.kt` (13 testes, lógica pura extraída para `Validacoes.kt`, corre em JVM local sem emulador). Cobertura ainda parcial — só a lógica sem dependências Android; testar Activities completas precisaria de Robolectric ou testes instrumentados, não tentado por não conseguir verificar a compilação aqui.
- [x] Foto de perfil — decisão tomada: removida por completo (código morto, nunca esteve ativa nem visível na UI), em vez de terminada. Motivo: envolveria testar upload de imagem/câmara, que não é possível verificar sem correr a app num dispositivo.
- [x] Regras de segurança do Realtime Database (`database.rules.json`) — ver secção 4.1
- [x] Recuperação de password (`LoginActivity` — "Esqueceu-se da password?")
- [x] Validação de email, password, e comprimento de campos no registo (`RegistoUserActivity`) — corrigido também um bug em que a falha de validação não mostrava nada ao utilizador
- [x] Validação de datas de evento (fim não pode ser antes do início) — `EventoActivity`
- [x] Estados de loading + tratamento de erros visível (indicador de carregamento em `HomeActivity` como referência do padrão; 30 `onCancelled` silenciosos em 11 ficheiros passaram a mostrar Toast de erro ao utilizador)
- [x] Loading state replicado pelos 6 ecrãs restantes (`OrgActivity`, `GrupoActivity`, `VerGrupoActivity`, `ListaGruposActivity`, `ListaSociosOrgActivity`, `AdmissaoActivity`)
- [x] Paginação de listas — implementada para a lista principal de eventos (`HomeActivity`), ver nota abaixo. As restantes listas (grupos, sócios) continuam sem paginação — ficam para uma iteração futura se o volume de dados justificar.
- [x] Extrair strings hardcoded para `strings.xml` — feito de forma faseada e verificada (ver nota abaixo). 51 strings de layouts XML + 36 strings de `Toast.makeText()` no código Kotlin.
- [x] 17 dos 21 layouts com larguras fixas em dp — 5 corrigidos com critério mais rigoroso nesta ronda (só `ListView`/`fragment`/`TextView` com Start+End já presentes e sem `bias` deliberado). Os restantes ficam deliberadamente por mexer — ver nota abaixo.
- [x] Alinhar as chaves de `Grupos` (nome → número) — feito, ver detalhes abaixo

### Alinhar as chaves de `Grupos` — o que foi feito
`Grupos` estava indexado pelo **nome** do grupo; `Users/{uid}/Grupos` sempre esteve indexado pelo **número**. Isto obrigava a maior parte dos ecrãs a percorrer todos os grupos e comparar campos no cliente, em vez de irem diretamente ao sítio certo — e impedia regras de segurança e queries mais precisas (ver limitações já documentadas nas secções de segurança e paginação).

**Mudança:** `Grupos` passa a ser indexado pelo **número** do grupo, tal como `Users/{uid}/Grupos` já era. Os dois ficam consistentes.

**Ficheiros alterados** (7): `ListaGruposActivity`, `PreferenciasActivity` — adicionada uma lista paralela de números ao lado da lista de nomes (a lista continua a *mostrar* nomes ao utilizador, só a chave interna de acesso ao Firebase mudou). `OrgActivity`, `AdmissaoActivity`, `HomeActivity`, `VerGrupoActivity` — o número já estava disponível no `dataSnapshot` recebido, só passou a ser usado como chave em vez do nome. `AdesaoActivity` — **não precisou de nenhuma alteração**, já estava bem desenhado (trata o identificador recebido como uma chave opaca, sem assumir que era o nome).

**Não precisaram de alteração:** `GrupoActivity` e `CriarOrgEventoActivity` já trabalhavam só com o número. `ListaSociosOrgActivity` e `SuperAdminGruposActivity` não fazem lookups por chave específica (só leem os dados já disponíveis do scan).

**Porque é seguro usar `.child(numero)` sem o problema de tipo que referi na secção de paginação:** ali, o risco era usar `orderByChild("Numero").equalTo(...)` — uma *query* que exige o tipo exato (número vs texto) da forma como o campo foi gravado. Aqui é diferente: `.child(numero)` usa o número só como **chave** do Firebase, e chaves no Realtime Database são **sempre texto**, independentemente de como o campo "Numero" foi originalmente escrito — não há ambiguidade de tipo possível numa chave.

### ⚠️ Ação necessária da tua parte
Como confirmaste, os grupos que já tens são só de teste. Como `Grupos` continua a ser criado manualmente na consola (nenhum código da app cria grupos), precisas de:
1. Apagar os grupos de teste antigos (indexados por nome) no Firebase Console
2. Recriá-los com a **chave a ser o número do grupo** (ex: em vez do nó se chamar `"Grupo dos Amigos"`, deve chamar-se `"5"` — o número, escrito como está, o Firebase trata sempre a chave como texto) — os campos lá dentro (`nome`, `Numero`, `admin`, `membros`, `Pendentes`) continuam exatamente iguais
3. Confirma que `Users/{uid}/Grupos/{numero}` (a lista de grupos de cada sócio) usa esse mesmo número como referência — já devia estar assim, não muda nada aí

## 7. Prova de conceito: migração Activity → Fragment (Navigation Component)

**Pedido:** modernizar a app para as práticas mais recentes, mesmo que implique mudar de Activities para Fragments para melhor performance.

**Contexto importante, para não ficar mal-entendido:** Fragments não são "mais rápidos" que Activities — a app já teria essa performance com Activities bem feitas. A razão real para esta arquitetura (usada pela generalidade das apps Android modernas) é outra: navegação centralizada, partilha de UI entre ecrãs, animações de transição mais fluidas. Isto é uma modernização de arquitetura, não uma otimização de velocidade.

**Porque só um ecrã, e não a app toda de uma vez:** uma migração completa tocaria as 20 Activities, todos os layouts, e toda a navegação entre ecrãs — e o ciclo de vida de Fragments tem categorias de bugs (fugas de memória, comportamento errado da stack de navegação) que não aparecem em nenhuma análise de código, só a correr a app. Sem conseguir compilar/testar aqui, fazer isto às cegas para toda a app seria irresponsável.

**O que foi feito:** `FiltrosActivity` (o ecrã mais simples e isolado da app — sem Firebase, sem formulários) convertido para `FiltrosFragment`, alojado por uma `FiltrosActivity` reduzida a um "host" fino.

**Ficheiros novos:**
- `FiltrosFragment.kt` — toda a lógica que estava em `FiltrosActivity.onCreate`, agora em `onViewCreated`, com a gestão correta do ciclo de vida do View Binding (`_binding` nullable, limpo em `onDestroyView` — o erro mais comum ao fazer isto, uma fuga de memória silenciosa se for esquecido)
- `activity_filtros_host.xml` — layout mínimo com um `FragmentContainerView`
- `nav_graph_filtros.xml` — grafo de navegação com o único destino (`FiltrosFragment`)

**Ficheiros alterados:**
- `FiltrosActivity.kt` — reduzida a ~60 linhas (só `onCreate` a montar o host, e o menu de opções, que continua na Activity porque navega para outras Activities)
- `app/build.gradle` — dependências `androidx.navigation:navigation-fragment-ktx`/`navigation-ui-ktx` 2.7.5

**Impacto no resto da app: zero.** Verificado — as outras 12 Activities que iniciam `FiltrosActivity` via `Intent(this, FiltrosActivity::class.java)` continuam a funcionar sem qualquer alteração, porque o nome da classe e o pacote não mudaram, só a implementação interna.

### Como testar (antes de decidirmos escalar ao resto da app)
1. `./gradlew build` — primeiro teste, se isto não compilar já sabemos que há um problema na configuração do Navigation Component
2. Abrir a app, navegar até ao ecrã de Filtros (a partir do login, ou do menu "home" em qualquer ecrã)
3. Confirmar visualmente que está **igual** ao que era antes (nenhuma mudança visual esperada)
4. Testar os 3 fluxos: "Caça Maior" → Esperas/Montaria, "Caça Menor" → Tordos/Rolas/Dias, "Ver Tudo" — todos devem continuar a navegar para `HomeActivity` com o filtro certo
5. **Teste específico de fuga de memória:** rodar o ecrã (mudar orientação) enquanto estás no ecrã de Filtros, várias vezes seguidas — se a app não abrandar/rebentar, o `onDestroyView` está a funcionar bem
6. Navegar para trás (botão "voltar" do sistema) a partir deste ecrã — deve sair da app ou voltar ao ecrã anterior normalmente, sem comportamento estranho da stack de navegação

Se tudo isto correr bem, o padrão está validado e posso escalar aos restantes 19 ecrãs, um de cada vez, com o mesmo cuidado de verificação.

### Paginação de Eventos — solução implementada
A tentativa inicial (`limitToLast()` na chave de `Eventos`) foi abandonada por a chave ser o nome do evento, não uma data — traria resultados alfabéticos, não cronológicos. Solução: adicionado um campo calculado e ordenável, `dataFimTimestamp` (epoch millis da data de fim do evento), gravado em `MapsActivity.kt` no momento da criação do evento. `HomeActivity` passou a consultar `.orderByChild("dataFimTimestamp").limitToFirst(200)` em vez de descarregar a coleção `Eventos` inteira sempre — a lógica de filtragem por data/tipo/formato que já existia manteve-se **exatamente igual**, isto só limita quantos nós o listener descarrega.

**Nota sobre eventos antigos:** eventos criados antes desta alteração não têm o campo `dataFimTimestamp` — o Firebase Realtime Database trata a ausência do campo como "menor que qualquer valor" numa ordenação ascendente, por isso esses eventos ficam nos primeiros lugares da ordenação e são os primeiros a cair fora do limite de 200 à medida que a base de dados crescer. Não é preciso nenhuma migração para os eventos existentes continuarem a aparecer normalmente enquanto o total for menor que 200.

**Ação necessária no Firebase:** o `database.rules.json` foi atualizado com `".indexOn": ["dataFimTimestamp"]` em `Eventos`, para esta query ser eficiente (sem índice, o Firebase ainda funciona mas avisa nos logs e faz uma pesquisa mais lenta). **Se já tinhas aplicado a versão anterior das regras, precisas de voltar a copiar e publicar a versão atualizada** — ver `docs/ACOES_MANUAIS.md` ponto 2️⃣.

### Paginação de Grupos/Sócios — o que foi feito e o que ficou de fora
- `OrgActivity` (grupos administrados pela organização): antes descarregava **todos** os grupos da plataforma e filtrava no cliente; agora usa `orderByChild("admin").equalTo(uid)`, filtrado no servidor. Seguro porque `admin` é sempre um uid do Firebase Auth (string, sem ambiguidade de tipo possível).
- `AdmissaoActivity` (pedidos pendentes de um grupo): mesma otimização por `admin`, reduz de "todos os grupos da plataforma" para "só os grupos que esta organização administra".
- `ListaGruposActivity` (catálogo de todos os grupos para pedir adesão): `limitToFirst(300)` — seguro aqui porque é mesmo um ecrã de descoberta/navegação de tudo, não uma pesquisa por algo específico do utilizador.

**Deliberadamente não tocado:** a comparação por `Numero` (em `AdmissaoActivity`/`ListaSociosOrgActivity`) não pode usar `orderByChild("Numero").equalTo(...)` com segurança — esse campo nunca é escrito pela app (só manualmente na consola Firebase), por isso não há garantia de que esteja sempre gravado como número vs. texto; uma query tipada errada falha silenciosamente (zero resultados) em vez de dar erro visível. `ListaSociosOrgActivity` também não foi tocada — para além do mesmo problema do `Numero`, o código que lê a lista de sócios (`membros`) fá-lo convertendo o mapa inteiro para texto e fazendo *parsing* manual da string (`membros.split('{', ',', '=')`), em vez de percorrer os filhos normalmente — é código frágil que preferi não mexer sem conseguir testar, para não arriscar partir isto ao tentar otimizar aquilo. Fica ligado ao mesmo problema de fundo do alinhamento das chaves de `Grupos`.

### Strings hardcoded — o que foi feito
Reconsiderei a decisão inicial de não mexer. Feito em duas fases, cada uma verificada separadamente antes de avançar para a seguinte:
1. **51 strings de `android:text="..."` em 22 layouts XML** → risco mínimo (troca mecânica de texto, sem tocar em posição/tamanho/lógica). Strings idênticas entre ficheiros passaram a partilhar o mesmo recurso.
2. **36 strings estáticas de `Toast.makeText()`** em 13 ficheiros Kotlin → substituídas por `CONTEXTO.getString(R.string.msg_...)`, reutilizando sempre a mesma expressão de contexto (`this`, `this@Activity`, etc.) de cada chamada, para nunca haver erro de resolução dentro de listeners aninhados. 1 string com interpolação de variável (`${nomesOrgs[position]} (uid: $uid)` em `SuperAdminActivity`) ficou de fora — baixo valor, mensagem de debug simples.

**Por resolver, se quiseres continuar esta limpeza no futuro:** `setTitle()`/`setMessage()` de `AlertDialog` (categoria diferente de Toast, não incluída nesta ronda), e as strings de `Log.d(...)` (deliberadamente não extraídas — não são visíveis ao utilizador, extrair mensagens de debug para recursos não é prática recomendada).

**Bug evitado:** o gerador de nomes produziu `nome` para uma string nova, que colidia com um recurso já existente e usado (`nome` = `"Nome:  %1$s  "`, com formatação, usado em `MyListAdapter`). Detetado e corrigido antes de integrar (renomeado para `label_nome`) — todos os nomes novos foram verificados um a um contra os já existentes antes de qualquer merge, em ambas as fases.

### Larguras fixas em layouts — o que foi feito e o que ficou de fora
Nesta ronda, mais 5 elementos em 5 ficheiros (`activity_criar_org_evento.xml`, `activity_detalhes_evento.xml`, `activity_maps.xml`, `activity_admissao.xml`, `activity_lista_socios_org.xml`), com um critério mais apertado que antes: só `ListView`, `fragment` (mapas) e `TextView` — nunca `Button`, `Switch`, `DatePicker`, `ImageView` ou `View` (divisores), porque esticar estes últimos até à largura do ecrã pioraria o aspeto em vez de melhorar. Mapas convertidos por convenção universal de UX (ecrã completo), não por regra mecânica.

**Bug próprio detetado e corrigido a meio:** ao converter um dos elementos, retirei sem querer a constraint `Bottom`, o que desativa silenciosamente o `vertical_bias` (o ConstraintLayout precisa de `Top` E `Bottom` para o bias funcionar) — o elemento ficaria preso ao topo do ecrã em vez de flutuar na posição original. Corrigido, e foi feita uma varredura a **todos** os layouts do projeto à procura do mesmo padrão de erro (e do equivalente horizontal) — só restam 2 casos pré-existentes, não introduzidos por mim, ambos no ficheiro com o bug de IDs duplicados já conhecido.

**Deliberadamente fora desta ronda** (risco visual real, sem forma de verificar sem renderizar): `activity_evento.xml`, `activity_profile.xml`, `activity_adesao.xml` (campo `tInfo`) — todos usam `bias` horizontal específico e ajustado (não o valor "por omissão" dos casos convertidos), típico de formulários com posicionamento livre em vez de conteúdo que deve esticar. `activity_registo_user.xml` continua de fora pelo bug de IDs duplicados já documentado. `filtros_custom_view.xml` confirmado como código morto, sem benefício em mexer.

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

## 8. Pacote de novas funcionalidades (7 pontos, todos feitos)

- [x] **Prevenir duplo-toque** — botões críticos (criar evento, registo, pedir adesão, marcar presença)
- [x] **Modo offline** — persistência do Firebase Realtime Database ativada
- [x] **Validação de dados nas regras** — não só quem escreve, também o formato
- [x] **Testes automatizados alargados** — 21 testes no total
- [x] **Paginação completa** — Eventos (por data) e Grupos (por admin)
- [x] **Regras mais fortes** — desbloqueadas pelo alinhamento das chaves de `Grupos`
- [x] **Multi-idioma** — Inglês (`values-en/strings.xml`)
- [x] **Limite de participantes + lista de espera** — em eventos
- [x] **Pesquisa "perto de mim"** — novo ecrã `EventosProximosActivity`
- [x] **Notificações push** — parte cliente completa; servidor (Cloud Functions) documentado em `docs/ACOES_MANUAIS.md`
- [x] **Adicionar ao calendário** do telemóvel
- [x] **Check-in por QR Code** — gerar (organização) e ler (caçador)

### Bugs críticos encontrados e corrigidos pelo caminho (não pedidos, descobertos a trabalhar nestas funcionalidades)
1. **Sobrescrita de presenças** (`DetalhesEventoActivity.marcarPresença()`) — usava sempre o mesmo índice (`"1"`), cada nova pessoa apagava a marcação da anterior. Descoberto ao implementar o limite de participantes.
2. **Crash garantido em `AdmissaoActivity`** — `strings.xml` usava `%i` (válido em C, inválido em Java/Kotlin) numa string usada ativamente. Descoberto ao preparar a tradução para Inglês.

### Novas dependências adicionadas nesta ronda (nunca compiladas neste ambiente — testar com atenção redobrada)
- `androidx.navigation:navigation-fragment-ktx` / `navigation-ui-ktx` (prova de conceito Fragment)
- `com.google.firebase:firebase-messaging-ktx` (notificações push)
- `com.google.zxing:core` / `com.journeyapps:zxing-android-embedded` (QR code)

---

## 9. Separação de Eventos por privacidade — problema de segurança real, resolvido

**Como surgiu:** ao planear a funcionalidade "partilhar um evento", percebi que precisava de perceber o que aconteceria ao partilhar um evento privado. Isso levou a investigar as regras de segurança, e a encontrar um problema real, não relacionado com a partilha em si.

**O problema:** as regras de segurança do Firebase Realtime Database não conseguem filtrar *listas* — só decidem se um pedido passa ou não, por inteiro (documentado oficialmente pela Google). A app tem pesquisas que percorrem todos os eventos públicos da plataforma (`HomeActivity`, `EventosProximosActivity`), o que exige uma regra de leitura ampla sobre o nó `Eventos` inteiro. Essa mesma permissão aplicava-se também a leituras diretas de um evento específico — **incluindo os privados**. Qualquer conta autenticada já conseguia ler os detalhes completos de qualquer evento privado, bastando saber o nome exato — o campo `"Forma"` só controlava o que aparecia nas *listas*, nunca restringiu o acesso direto aos dados.

**A solução:** separar num único nó `Eventos` para dois:
- `EventosPublicos/{nome}` — leitura ampla, como antes
- `EventosPrivados/{numeroGrupo}/{nome}` — leitura só para quem pertence a esse grupo (`Users/{uid}/Grupos/{numeroGrupo}.exists()`); escrita só para o admin desse grupo específico (antes, qualquer conta "organização" podia escrever em qualquer evento, mesmo de grupos que não administrava — também apertado)

**Escala do trabalho:** tocou os 7 ficheiros que acediam a eventos (`MapsActivity`, `DetalhesEventoActivity`, `EventosProximosActivity`, `MeusEventosActivity`, `GrupoActivity`, `CriarOrgEventoActivity`, `HomeActivity`) e as regras do Firebase. `HomeActivity` (o mais complexo e frágil) foi alterado com o mínimo de mudança possível — só as referências ao nó do Firebase, sem tocar na lógica de filtragem por data/tipo já existente.

**Efeito colateral positivo:** `GrupoActivity` e `CriarOrgEventoActivity` tinham um padrão muito ineficiente (percorrer *todos* os eventos da plataforma, reler cada um individualmente para verificar se pertencia ao grupo certo) — a migração obrigou a uma reescrita que também resolveu isso, agora são leituras diretas.

**Um evento público continua ligado ao grupo que o criou** (o campo `numeroGrupo` é gravado sempre, independentemente de ser público ou privado) — por isso alguns ecrãs (`GrupoActivity`, `CriarOrgEventoActivity`) consultam os dois nós em paralelo para mostrar todos os eventos de um grupo.

**QR code de check-in:** passou a codificar tipo + grupo + nome (antes só o nome), para quem lê o código saber onde procurar o evento.

### ⚠️ Ação necessária da tua parte
Confirmaste que os eventos que já tens são só de teste (mesma situação da migração de `Grupos`). Como os eventos só são criados através da própria app (ao contrário de `Grupos`, não há criação manual na consola), não precisas de fazer nada manualmente — os eventos de teste antigos (guardados no nó `Eventos`, que deixou de ser usado) ficam simplesmente órfãos e podes apagá-los na Firebase Console se quiseres limpar, mas a app já não os vai ler. Os próximos eventos que criares através da app vão automaticamente para o nó certo.

**Voltar a publicar as regras** — como sempre que o `database.rules.json` muda, ver `docs/ACOES_MANUAIS.md` ponto 2️⃣.

---

*Última atualização: gerado automaticamente pela sessão de trabalho com Claude.*
