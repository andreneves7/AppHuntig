# Guia de Testes

Este documento existe para dares o primeiro teste real a tudo o que foi feito nesta sessão de trabalho — nunca consegui compilar nada aqui, por isso este é o momento em que vamos mesmo descobrir se algo falhou.

**Como usar isto:** segue por ordem, de cima para baixo. Cada teste diz exatamente o que fazer e o que esperar de ver. Se algo correr diferente do esperado, **para nesse ponto, não continues a lista** — volta a falar comigo com:
- Em que passo exato falhou
- O que esperavas de ver vs. o que viste
- Se houver um erro no Android Studio (Logcat) ou um crash, copia o texto do erro

---

## 0. Antes de começares

Nada disto funciona sem os passos de `docs/ACOES_MANUAIS.md` feitos primeiro, por esta ordem:

1. 1️⃣ Chave do Google Maps regenerada e restrita
2. 2️⃣ Regras do Realtime Database publicadas (versão mais recente)
3. 3️⃣ Chave do `google-services.json` regenerada — **urgente**, esteve exposta
4. 5️⃣ Keystore criada (só necessário se vieres a testar uma build de *release*, não para debug)
5. 6️⃣ Grupos de teste recriados com a chave nova (número, não nome)

Sem os pontos 1-3 feitos, a app nem vai comunicar com o Firebase. Sem o ponto 6, os grupos de teste antigos ficam invisíveis (não é um bug — é o efeito esperado da migração).

---

## 1. Compilar

1. Abre o projeto no Android Studio (`File → Open`, escolhe a pasta do projeto)
2. Deixa o Gradle sincronizar sozinho (pode demorar alguns minutos na primeira vez, por causa das dependências novas desta sessão — Navigation Component, Biometric, ZXing, Maps Utils, SwipeRefreshLayout, Firebase Messaging, App Check)
3. `Build → Make Project`

**O que esperar:** deve compilar sem erros. Se houver erros, são o mais importante de todos para me reportares — copia a mensagem completa do painel "Build" do Android Studio.

**Se falhar por causa de uma dependência não encontrada:** confirma que tens ligação à internet e tenta `File → Sync Project with Gradle Files` outra vez.

---

## 2. Fluxos essenciais

### 2.1 Registo de conta nova
1. Abre a app → ecrã de login → botão de registo
2. Preenche: Nome, Email, Password (mín. 6 caracteres), Telefone (9 dígitos), Número da Carta de Caçador (6 caracteres)
3. Toca em "Registar"

**Esperado:** conta criada, email de verificação enviado, volta ao ecrã de login. **Nota:** o formulário está muito mais curto do que estarias à espera se já conhecias a app — morada, contribuinte, documentos de identificação, seguro e licença de arma foram removidos deliberadamente (pediste isto explicitamente).

### 2.2 Login como caçador
1. Faz login com uma conta `cacador`
2. Se for a primeira vez desta conta: deves ver o ecrã de boas-vindas (onboarding, 4 passos) → depois o ecrã de preferências
3. Login normal (não primeira vez): deves ir direto para **"Os teus próximos eventos"** (ecrã novo, mostra os eventos dos teus grupos)

**Esperado:** entra sem erros, mostra o nome/eventos corretos.

### 2.3 Login como organização
1. Faz login com uma conta `organizacao`
2. Deve abrir o painel da organização (lista de grupos que administra)

### 2.4 Login como SuperAdmin
1. Precisa da conta criada no ponto 4️⃣ de `ACOES_MANUAIS.md`
2. Deve abrir o painel SuperAdmin (organizações, com botões para "Todos os Utilizadores" e "Todos os Grupos")

### 2.5 Criar um evento
1. Como organização, entra num grupo → "Criar Evento"
2. Preenche os dados, escolhe localização no mapa
3. Ao tocar em "Guardar": deve aparecer um diálogo a pedir o **limite de participantes** (podes deixar vazio para "sem limite")
4. Confirma

**Esperado:** evento criado, aparece na lista do grupo.

**Atenção especial:** confirma que o evento aparece corretamente consoante o marcaste como público ou privado — isto testa diretamente a divisão `EventosPublicos`/`EventosPrivados` feita na auditoria de segurança.

### 2.6 Pedir adesão a um grupo
1. Como caçador, menu → "Lista Grupos Disponíveis"
2. Escolhe um grupo → pede adesão

**Esperado:** pedido registado. Como organização (admin desse grupo), deve aparecer no ecrã de admissão.

### 2.7 Aceitar/Rejeitar sócio
1. Como organização, abre o ecrã de admissão do grupo
2. Aceita um pedido individualmente
3. Se houver vários pendentes, testa também **"Aceitar todos os pendentes"** (menu do ecrã)
4. Testa "Rejeitar" um pedido — confirma que pede confirmação antes, e que o pedido desaparece da lista depois

---

## 3. Funcionalidades novas (testar cada uma pelo menos uma vez)

| Funcionalidade | Onde encontrar | O que confirmar |
|---|---|---|
| Marcar presença + lista de espera | Detalhes de um evento | Marca presença; se o evento tiver limite e já estiver cheio, confirma que entras na lista de espera em vez de "Presenças" |
| Exportar participantes | Menu de detalhes do evento | Abre o seletor de partilha com a lista de nomes |
| Mostrar QR Code | Menu de detalhes do evento | Mostra um QR code legível |
| Check-in por QR Code | Menu principal | Pede câmara, lê um QR gerado por ti mesmo, abre o evento certo |
| Partilhar evento | Menu de detalhes do evento | Abre o seletor de partilha com texto + link `apphuntig://` |
| **Testar o link do passo anterior:** cola o link partilhado nas Notas do telemóvel, toca nele | — | Deve abrir a app diretamente nesse evento (só funciona se a app já estiver instalada) |
| Eventos perto de mim (lista e mapa) | Menu principal | Testa os filtros (raio, público/privado), e o botão "Ver mapa" — confirma que marcadores próximos se agrupam e se separam ao dar zoom |
| Pesquisa de local | Dentro de "Eventos perto de mim" | Escreve um nome de sítio (ex. uma cidade) em vez de usar a tua localização |
| Definições — biometria | Menu → Definições | Ativa; a próxima vez que abrires a app deve pedir impressão digital/reconhecimento facial antes de entrares |
| Definições — notificações | Menu → Definições | Desliga; confirma (se tiveres forma de testar notificações) que deixam de aparecer |
| Definições — tema | Menu → Definições | Testa as 3 opções (seguir sistema / claro / escuro) — como o app nunca teve um tema escuro feito à mão, não esperes perfeição visual, só que não fique ilegível |
| Definições — idioma | Menu → Definições | Muda para Inglês e Espanhol, confirma que os textos mudam |
| Pesquisa na lista de grupos | Lista de Grupos Disponíveis | Escreve para filtrar; confirma que **clicar num resultado filtrado abre o grupo certo** (era o bug que corrigi antes de existir) |
| Pull-to-refresh | Qualquer ecrã com lista | Puxa para baixo, confirma que atualiza sem duplicar itens |
| Ajuda/FAQ | Menu → Ajuda | Abre, lê as perguntas |

---

## 4. Os pontos de maior risco — mereces atenção redobrada aqui

Estes são os que sinalizei como mais arriscados ao longo da sessão, exatamente porque nunca os consegui testar de forma nenhuma:

### 4.1 Prova de conceito Fragment (`FiltrosActivity`)
1. Abre o ecrã de Filtros (normal, a partir do fluxo de caçador)
2. Testa "Caça Maior"/"Caça Menor"/"Ver Tudo" — cada opção deve levar à lista de eventos certa
3. **Teste específico de fuga de memória:** fica neste ecrã e roda o telemóvel (muda de vertical para horizontal) repetidamente, uns 10-15 vezes seguidas. Se a app não abrandar nem rebentar, está tudo bem
4. Testa o botão "voltar" do telemóvel a partir daqui — deve sair da app ou voltar ao ecrã anterior normalmente

### 4.2 Migração das chaves de `Grupos` e divisão de `Eventos`
Estes dois mudaram o modelo de dados todo. Confirma com atenção:
- Um grupo criado com a chave nova (número) aparece corretamente em toda a app — lista de grupos, detalhes, sócios
- Um evento privado **nunca** aparece para alguém fora do grupo (tenta com duas contas de teste, uma dentro e outra fora do grupo)
- Um evento público aparece para todos

### 4.3 App Check
Não vais conseguir testar isto por completo sem primeiro fazeres o ponto 7️⃣ de `ACOES_MANUAIS.md` na Firebase Console. Mas depois de o fazeres (em modo de monitorização, **não** "Enforced"), confirma na consola que os pedidos da app aparecem como "verificados".

### 4.4 Os 17 layouts com dp fixo, deliberadamente não convertidos
Não é um teste de funcionalidade — é um teste visual. Passa por cada ecrã em telemóveis com tamanhos de ecrã diferentes (ou o emulador com perfis diferentes) e confirma que nada aparece cortado ou mal posicionado. Estes são precisamente os que fiquei deliberadamente sem tocar por não conseguir renderizar.

---

## 5. Se alguma coisa falhar

Volta a falar comigo com:
1. **O que estavas a testar** (aponta para a secção/número deste documento)
2. **O erro exato** — mensagem do Android Studio, do Logcat, ou uma descrição precisa do que aconteceu de errado no ecrã
3. Se conseguires, **os passos exatos para reproduzir**

Vou usar o histórico completo de commits desta sessão (todos com mensagens detalhadas explicando o porquê de cada decisão) para encontrar rapidamente onde o problema pode estar.
