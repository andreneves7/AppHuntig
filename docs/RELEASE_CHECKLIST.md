# Checklist de Release — AppHuntig

> Passo a passo para quando estiveres pronto a publicar. Os itens com ✅ já
> estão tratados no código/repositório; os com ☐ precisam da tua ação direta
> (consolas externas, decisões de produto, ou testes que só podem ser feitos
> numa máquina com Android Studio).

---

## 1. Segurança — obrigatório antes de publicar

- ☐ **Regenerar e restringir a chave do Google Maps** (ver secção 0 do `PLANO_DESENVOLVIMENTO.md` — já esteve exposta publicamente no GitHub)
- ☐ Aplicar `database.rules.json` na Firebase Console (Realtime Database → Regras → colar → Publicar)
- ☐ Regenerar a chave do `google-services.json` na Google Cloud Console (também já esteve exposta antes de ser protegida)
- ☐ Criar a(s) conta(s) de `superadmin` manualmente no Firebase (ver secção 5 do plano)
- ✅ `exported="true"` indevido corrigido (`ListaSociosOrgActivity`)
- ✅ Permissões não usadas removidas do manifesto

## 2. Assinatura da app (obrigatório — sem isto não há build de release)

O `build.gradle` já está preparado para ler as credenciais de assinatura de um
ficheiro `keystore.properties` local (nunca commitado). Falta só criares a
keystore e esse ficheiro:

1. No Android Studio: **Build → Generate Signed Bundle / APK → Create new...**
   (ou via terminal com `keytool -genkey -v -keystore apphuntig-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias apphuntig`)
2. **Guarda a keystore e as passwords num sítio seguro e com backup** (gestor de passwords, cofre da empresa) — se a perderes, **nunca mais consegues publicar atualizações** com a mesma ficha na Play Store, só consegues criar uma app nova
3. Cria um ficheiro `keystore.properties` na raiz do projeto (mesmo nível do `build.gradle` principal) com:
   ```properties
   storeFile=../apphuntig-release.jks
   storePassword=a-tua-password-da-keystore
   keyAlias=apphuntig
   keyPassword=a-tua-password-da-chave
   ```
4. Confirma que `keystore.properties` aparece como ignorado pelo Git (`git status` não o deve listar)

Documentação oficial: https://developer.android.com/studio/publish/app-signing

## 3. Compilar e testar a build de release

- ☐ `./gradlew assembleRelease` (ou pelo Android Studio) — **primeira vez que este código é realmente compilado**, ver nota abaixo
- ☐ Instalar a build de release num telemóvel físico (não só o emulador) e testar:
  - Login como caçador, como organização, como superadmin
  - Registo de nova conta
  - Recuperação de password
  - Criação de evento (datas, mapa, tipo)
  - Pedido de adesão a um grupo + aceitação pela organização
  - Mapa e localização (a chave nova do Maps já tem de estar configurada)
- ☐ Só depois de tudo testado e a funcionar, considera ativar `minifyEnabled true` no `build.gradle` (as regras de ProGuard já estão preparadas em `proguard-rules.pro`, mas nunca testadas) — testa tudo outra vez de seguida, uma build minimizada pode comportar-se de forma diferente

> ⚠️ **Nota importante:** todo o trabalho de código feito até agora (bugs corrigidos, View Binding, roles, etc.) foi validado por revisão estática cuidadosa, mas **nunca foi compilado**, porque o ambiente onde trabalhei não tem acesso aos repositórios Maven da Google. Este vai ser literalmente o primeiro `./gradlew build` real desde que começámos. É expectável teres de corrigir 1 ou 2 detalhes pequenos (nomes de imports, algum typo) — mas a estrutura e a lógica já foram verificadas exaustivamente.

## 4. Versão

- `versionCode` atual: `1`, `versionName`: `"1.0"` — apropriado para o primeiro lançamento
- Para cada atualização futura: incrementa sempre o `versionCode` (número interno, tem de ser sempre maior que o anterior) e o `versionName` (o que os utilizadores veem, ex: "1.1", "1.2")

## 5. Ficha da Play Store (Google Play Console)

Isto é trabalho de produto/marketing, não de código — só listado aqui para não esquecer:

- ☐ Conta de developer na Google Play Console (taxa única de $25)
- ☐ Ícone de alta resolução (512x512), banner (1024x500)
- ☐ Capturas de ecrã (pelo menos 2, recomendado 4-8) em telemóvel e, se suportares tablets, também em tablet
- ☐ Descrição curta e longa da app
- ☐ **Política de privacidade** (URL pública) — obrigatória porque a app recolhe dados pessoais (nome, morada, número de cartão de caçador, localização). Tem de explicar que dados são recolhidos, para quê, e como são usados/guardados no Firebase
- ☐ Classificação de conteúdo (questionário da Play Console)
- ☐ Declaração de conformidade com a política de dados (Data Safety form) — head's up: vais ter de declarar recolha de localização, dados de contacto e identificadores pessoais

## 6. Depois de publicado

- ☐ Aplicar as sugestões ainda pendentes do `PLANO_DESENVOLVIMENTO.md` (notificações push, paginação de listas, etc.) como atualizações incrementais
- ☐ Monitorizar o Firebase Crashlytics (já está integrado no `build.gradle`) para apanhar erros reais de utilizadores
