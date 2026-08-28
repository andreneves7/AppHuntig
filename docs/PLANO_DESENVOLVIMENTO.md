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

| # | Problema | Ficheiro(s) | Risco/Impacto |
|---|---|---|---|
| 1 | `google-services.json` (chave de configuração Firebase) está versionado no Git, incluindo no histórico | `app/google-services.json` | Médio — não é a chave secreta do servidor, mas não é boa prática expô-la publicamente; API keys do Firebase devem ser restringidas nas definições da Google Cloud Console |
| 2 | Uso simultâneo de **Firebase Realtime Database** e **Cloud Firestore** para os mesmos dados (`Eventos`, `Users`) | `HomeActivity.kt`, `LoginActivity.kt`, outros | Alto — duplicação de lógica, inconsistência de dados, mais superfície para bugs |
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

### 4.1 Regras de segurança do Firestore (a rever/criar)
Regras sugeridas (rascunho inicial, por rever):

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    function isSignedIn() {
      return request.auth != null;
    }
    function role() {
      return get(/databases/$(database)/documents/Users/$(request.auth.uid)).data.role;
    }
    function isSuperAdmin() {
      return isSignedIn() && role() == 'superadmin';
    }
    function isOrg() {
      return isSignedIn() && role() == 'organizacao';
    }

    match /Users/{userId} {
      allow read: if isSignedIn();
      allow write: if isSignedIn() && (request.auth.uid == userId || isSuperAdmin());
    }

    match /Eventos/{eventoId} {
      allow read: if isSignedIn();
      allow write: if isOrg() || isSuperAdmin();
    }

    match /Grupos/{grupoId} {
      allow read: if isSignedIn();
      allow write: if isOrg() || isSuperAdmin();
    }
  }
}
```

> ⚠️ Isto é um ponto de partida, não uma versão final — precisa de ser validado contra a estrutura real de dados que tens hoje no teu Firebase (posso não estar a ver 100% da estrutura só pelo código-cliente).

### 4.2 Passos que **tu** precisas de validar/fazer
1. Confirmar na Firebase Console se já existem regras de segurança ativas (Firestore → Regras).
2. Restringir a API Key do `google-services.json` na Google Cloud Console (Credentials → restringir por app/pacote).
3. Criar manualmente o(s) documento(s) `Users/{teu-uid}` com `role: "superadmin"`.
4. Decidir: mantemos os dados espalhados por Realtime Database **e** Firestore, ou migramos tudo para um só (recomendo Firestore, é o que a Google recomenda para dados estruturados como este — ver documentação oficial: https://firebase.google.com/docs/firestore/rtdb-vs-firestore).

---

## 5. Plano de trabalho (código, branch `claude-dev`)

- [ ] Adicionar `.gitignore` para ficheiros sensíveis (feito nesta sessão)
- [ ] Introduzir campo `role` no modelo de dados e lógica de login/routing
- [ ] Criar `SuperAdminActivity` (painel com acesso a todas as organizações/utilizadores)
- [ ] Corrigir bug de aceitação múltipla em grupos (`GrupoActivity.kt`)
- [ ] Unificar acesso a dados (escolher Firestore **ou** Realtime DB, não os dois)
- [ ] Migrar `kotlinx.android.synthetic` → View Binding
- [ ] Substituir dependências Anko por alternativas atuais
- [ ] Rever/corrigir layouts XML afetados
- [ ] Testar fluxos principais (login caçador, login organização, login superadmin, criação de evento, adesão a grupo)

## 6. Ponto em aberto — stack para Web Admin + iOS

A decidir em conjunto antes de qualquer reescrita. Ver mensagem no chat com a proposta e razões. Nada nesta secção é executado sem confirmação explícita.

---

*Última atualização: gerado automaticamente pela sessão de trabalho com Claude.*
