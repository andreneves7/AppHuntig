# Ações Manuais — só tu podes fazer estas

> Documento único e ordenado com **tudo** o que precisa de acesso a consolas
> externas (Google Cloud, Firebase, a tua máquina) que eu não tenho. Cada
> ponto já vem com o texto/ficheiro pronto a copiar — a ideia é que sigas
> de cima para baixo e cada passo seja só "colar aqui".
>
> Atualizado sempre que preparo mais alguma coisa nova. Os outros documentos
> (`PLANO_DESENVOLVIMENTO.md`, `RELEASE_CHECKLIST.md`) continuam a existir
> para contexto e detalhe técnico — este é o índice de ação rápida.

---

## 1️⃣ Regenerar e restringir a chave do Google Maps

**Porquê:** esteve exposta publicamente no GitHub (duplicada em dois ficheiros, sem proteção). Considera-a comprometida.

**Passos:**
1. Abre https://console.cloud.google.com/apis/credentials (projeto Firebase desta app)
2. Localiza a chave `AIzaSyCR_5PdNIjarIMG5-GBDPLvBj0rSbGu8bY` → **Delete** (ou "Regenerate key" se preferires manter o mesmo registo)
3. **Create Credentials → API Key** para criar uma nova
4. Clica na chave nova → em **"Application restrictions"** escolhe **Android apps** → **Add package name and fingerprint**:
   - Package name: `com.company.HuntigEvents`
   - SHA-1: o da tua keystore de **debug** (corre `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android` para o obteres) — e depois repete o processo com o SHA-1 da tua keystore de **release** quando a tiveres (ponto 5️⃣ abaixo)
5. Em **"API restrictions"** escolhe **Restrict key** e seleciona só: `Maps SDK for Android` e `Places API`
6. Copia a chave nova (começa por `AIza...`)

**Onde colar — dois ficheiros, ambos já preparados e vazios/prontos a receber:**

```
app/src/debug/res/values/google_maps_api.xml
```
Substitui o valor entre as tags `<string name="google_maps_key" ...>AQUI</string>` pela chave nova (usa a mesma para já, restringida com o SHA-1 de debug).

```
app/src/release/res/values/google_maps_api.xml
```
Aqui tens de criar uma **segunda chave** (repete os passos acima) restringida com o SHA-1 da tua keystore de **release** (ponto 5️⃣), e colar essa.

> Estes dois ficheiros já estão no `.gitignore` — o que colares aqui nunca mais vai para o Git.

---

## 2️⃣ Aplicar as regras de segurança do Realtime Database

**Porquê:** sem isto, qualquer pessoa com a app instalada consegue ler/escrever a tua base de dados inteira.

> ⚠️ Se já tinhas aplicado uma versão anterior destas regras, o ficheiro foi atualizado outra vez — desta vez uma mudança importante: `Eventos` foi separado em `EventosPublicos`/`EventosPrivados`, para corrigir um problema real de privacidade (eventos privados eram legíveis por qualquer conta autenticada que soubesse o nome exato — ver `docs/PLANO_DESENVOLVIMENTO.md` secção 9). **Publicar a versão mais recente é importante desta vez, não só uma otimização.**

**Passos:**
1. Firebase Console → o teu projeto → **Realtime Database** → separador **Regras**
2. Copia o conteúdo completo do ficheiro `database.rules.json` (raiz do repositório, já pronto)
3. Cola no editor, substituindo o que lá estiver
4. Usa o **"Rules Playground"** para testar antes de publicar (ex: simula uma leitura sem autenticação → deve falhar; uma escrita de um utilizador no seu próprio registo → deve passar)
5. **Publicar**

---

## 3️⃣ Regenerar a chave do `google-services.json` — URGENTE

**Porquê:** este ficheiro esteve no repositório **durante toda esta sessão de trabalho**, não só antes — o `.gitignore` foi adicionado logo no início, mas por uma falha minha, o ficheiro que já estava rastreado desde o repositório original nunca foi removido do controlo de versão (só descoberto e corrigido numa auditoria de segurança pedida mais tarde). Isto significa que a chave esteve presente em **todos os ficheiros `.bundle` gerados e partilhados** ao longo da sessão — trata isto com prioridade máxima, não como um "seria bom fazer".

**Passos:**
1. Google Cloud Console → mesmo projeto → **APIs & Services → Credentials**
2. Localiza a chave de API associada ao `google-services.json` (normalmente chamada "Android key (auto created by Firebase)")
3. Regenera-a (ou aplica as mesmas restrições de pacote+SHA-1 do ponto 1️⃣, se ainda não tiver)
4. Firebase Console → **Definições do projeto** → aba **Geral** → a tua app Android → **Descarregar google-services.json**
5. Substitui o ficheiro em `app/google-services.json` (já está no `.gitignore`, fica só na tua máquina)

---

## 4️⃣ Criar a tua conta de SuperAdmin

**Porquê:** não existe nenhum fluxo na app para criar esta conta — é sempre manual, por design (é a conta com acesso total, não deve poder ser criada por registo público).

**Passos:**
1. Regista-te normalmente na app (fica como `role: "cacador"`, à espera de aprovação)
2. Firebase Console → **Realtime Database** → navega até `Users/{o-teu-uid}` (encontras o teu uid em **Authentication** → lista de utilizadores)
3. Edita esse registo, adicionando/alterando estes dois campos exatamente:
   ```json
   "role": "superadmin",
   "Controlo": true
   ```
4. Fecha a app por completo e volta a abrir → faz login → deves cair automaticamente no painel SuperAdmin

> Repete este processo para cada pessoa adicional que precises que tenha acesso total. Recomendo manter isto a 1-2 contas no máximo.

---

## 5️⃣ Criar a keystore de assinatura

**Porquê:** sem isto não existe build de release nenhuma — é o "cartão de identidade" que prova que as atualizações futuras vêm de ti.

**Passos:**
1. No terminal, dentro da pasta onde queres guardar a keystore (⚠️ **fora** da pasta do projeto, para nunca correres o risco de a commitar):
   ```bash
   keytool -genkey -v -keystore apphuntig-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias apphuntig
   ```
2. Vai pedir uma password para a keystore e outra para a chave (podem ser iguais) — **escolhe passwords fortes e guarda-as já num gestor de passwords**
3. Copia o ficheiro `keystore.properties.example` (raiz do repositório) para `keystore.properties` (mesmo sítio, sem `.example`)
4. Edita `keystore.properties` com os valores reais:
   ```properties
   storeFile=../apphuntig-release.jks
   storePassword=<a password que escolheste>
   keyAlias=apphuntig
   keyPassword=<a password que escolheste>
   ```
   (ajusta o caminho de `storeFile` consoante onde guardaste o `.jks`)
5. Confirma que `git status` **não** lista `keystore.properties` nem o `.jks` — se listar, algo está mal no `.gitignore`, avisa-me

**⚠️ Faz já um backup** da keystore e das passwords (gestor de passwords + cópia num disco/cloud privado). Perdê-la = nunca mais conseguires publicar atualizações com a mesma ficha na Play Store.

Depois de teres a keystore de release, volta ao ponto 1️⃣ para criares a segunda chave do Maps (a de release), usando o SHA-1 desta keystore:
```bash
keytool -list -v -keystore apphuntig-release.jks -alias apphuntig
```

---

## 6️⃣ Recriar os grupos de teste com a nova chave

**Porquê:** `Grupos` passou a ser indexado pelo número do grupo em vez do nome (ver `docs/PLANO_DESENVOLVIMENTO.md`, secção "Alinhar as chaves de Grupos"). Confirmaste que os grupos que tens hoje são só de teste — por isso a via mais simples é apagar e recriar, em vez de migrar dados.

**Passos:**
1. Firebase Console → Realtime Database → localiza o nó `Grupos`
2. Apaga os grupos de teste que lá tiveres
3. Recria-os, mas desta vez com a **chave do nó a ser o número do grupo** (ex: o nó chama-se `5`, não `"Grupo dos Amigos"`) — os campos dentro (`nome`, `Numero`, `admin`, `membros`, `Pendentes`) mantêm-se exatamente como antes
4. Confirma que os campos `Users/{uid}/Grupos/{numero}` de cada sócio de teste continuam a corresponder ao mesmo número

---

### Notificações push — parte cliente feita, falta a parte de servidor

**O que já está feito (client-side):**
- `HuntigMessagingService.kt` — recebe e mostra notificações, guarda o token FCM de cada utilizador em `Users/{uid}/fcmToken`
- Pedido de permissão (`POST_NOTIFICATIONS`, obrigatória a partir do Android 13) nos 3 ecrãs de entrada (`FiltrosActivity`, `OrgActivity`, `SuperAdminActivity`) — cobre os 3 papéis (caçador, organização, superadmin)
- Dependência `firebase-messaging-ktx` e registo do serviço no manifesto

**O que falta — só é possível fazer com acesso à consola/CLI do Firebase, que não tenho aqui:**

As notificações só são realmente *enviadas* por uma **Cloud Function** que corre nos servidores do Firebase e reage a mudanças na base de dados (ex: alguém aceite num grupo → notificar essa pessoa). Sem isto, o código cliente que já está pronto nunca é ativado sozinho.

**Passos:**
1. `npm install -g firebase-tools` (Node.js precisa de estar instalado)
2. `firebase login` e depois `firebase init functions` na raiz do projeto (cria uma pasta `functions/`)
3. Exemplo de função para notificar quando um pedido de adesão é aceite (adapta o caminho se os teus dados estiverem estruturados de forma diferente):

```javascript
const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

// Dispara sempre que um membro é acrescentado a um grupo
exports.notificarAdmissaoAceite = functions.database
  .ref("/Grupos/{grupoId}/membros/{numeroSocio}")
  .onCreate(async (snapshot, context) => {
    const uid = snapshot.val();

    const tokenSnap = await admin.database()
      .ref(`/Users/${uid}/fcmToken`)
      .once("value");
    const token = tokenSnap.val();
    if (!token) return null;

    return admin.messaging().send({
      token: token,
      notification: {
        title: "Pedido aceite!",
        body: "O teu pedido de adesão foi aceite.",
      },
    });
  });
```

4. `firebase deploy --only functions` para publicar
5. Podes replicar o mesmo padrão para outros eventos: evento novo criado num grupo (`onCreate` em `/EventosPrivados/{numeroGrupo}/{eventoId}`, notificar todos os membros desse grupo), conta aprovada (`onUpdate` em `/Users/{uid}/Controlo`), etc.

Documentação oficial: https://firebase.google.com/docs/functions/database-events

---

## 7️⃣ Ativar o Firebase App Check

**O que é:** uma proteção diferente das Regras de Segurança — confirma que os pedidos ao Firebase vêm mesmo da app genuína a correr num dispositivo real, não de um script ou de uma cópia modificada a usar a mesma chave. O código já está pronto (`VariaveisGlobais.kt`), mas só fica realmente ativo depois destes passos na consola.

**Passos:**
1. Firebase Console → **App Check** (menu lateral, em "Compilação")
2. Regista a tua app Android → escolhe **Play Integrity** como fornecedor
3. Isto precisa que o projeto Firebase esteja ligado a um projeto Google Cloud com a **Play Integrity API ativada** — a consola guia-te por isso, mas se pedir, ativa a API em [console.cloud.google.com](https://console.cloud.google.com) → **APIs & Services** → procura "Play Integrity API" → Ativar
4. **Testar em debug primeiro:** corre a app no Android Studio em modo debug, procura no Logcat por uma linha a começar com "AppCheck" — mostra um token de depuração. Copia-o e regista-o em Firebase Console → App Check → a tua app → **Gerir tokens de depuração**
5. **Não ativar "Enforce" (aplicar) já** — a Firebase Console tem um modo de monitorização primeiro, onde vês métricas de quantos pedidos estão a chegar com token válido, sem bloquear nada. Só depois de confirmares que os pedidos reais da app aparecem como válidos é que deves mudar para "Enforced" no Realtime Database — ativar isto cedo demais, antes de confirmares que está tudo a funcionar, arrisca bloquear utilizadores reais por engano

Documentação oficial: https://firebase.google.com/docs/app-check/android/play-integrity-provider

---

## Depois de tudo isto feito

Segue para o `docs/RELEASE_CHECKLIST.md` secção 3 (compilar e testar) — essa parte já não depende de mais nenhuma chave ou consola, só de correres o build no Android Studio.
