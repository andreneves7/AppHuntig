# AppHuntig 🦌

App Android nativa (Kotlin) para o ecossistema da caça — liga caçadores a associações/organizações de caça, aos seus grupos, e aos eventos que organizam.

## O que a app faz

- **Caçadores**: registam-se, pedem adesão a grupos de caça, veem e inscrevem-se em eventos (públicos ou privados dos seus grupos), marcam presença (com QR code ou manualmente), recebem notificações
- **Organizações**: administram os seus grupos, aprovam pedidos de adesão (individualmente ou em lote), criam eventos com localização no mapa e limite de participantes
- **SuperAdmin**: gestão global da plataforma — aprova organizações, gere todos os utilizadores e grupos

## Stack técnica

- **Kotlin** nativo, View Binding
- **Firebase**: Realtime Database, Authentication, Cloud Messaging, App Check
- **Google Maps** + clustering de marcadores
- **Navigation Component** (prova de conceito)

## Estrutura do repositório

```
app/src/main/java/com/example/app/   → código Kotlin (Activities, lógica)
app/src/main/res/                     → layouts, strings (PT/EN/ES), menus, drawables
database.rules.json                   → regras de segurança do Firebase Realtime Database
```

## Idiomas

Português (padrão), Inglês, Espanhol — muda automaticamente com o idioma do telemóvel, ou manualmente em Definições dentro da app.
