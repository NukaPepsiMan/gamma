# Documentazione Progetto Gamma

## Scelte Tecniche
- **Linguaggio e Framework**: Java 17 con Spring Boot 3.5.11.
- **Database**: PostgreSQL (gestito con Spring Data JPA).
- **Messaggistica Asincrona**: Apache Kafka (tramite Spring Kafka). Usato per disaccoppiare la ricezione del documento dalle fasi successive di elaborazione (firma e conservazione).
- **Sicurezza**: Spring Security con OAuth2 Resource Server. Delego l'autenticazione a un Identity Provider esterno (Keycloak) tramite JWT, rendendo l'applicazione "stateless". OAuth2 Client è utilizzato per le richieste alle (finte) api esterne di aruba.
- **Resilienza**: Resilience4j per l'implementazione del pattern Circuit Breaker verso i gateway esterni (API di Aruba). Evita il sovraccarico e gestisce i fallback in caso di disservizi del provider terzo.
- **Testing**:
    - *Testcontainers*: per test di integrazione usando container Docker reali (Postgres, Kafka).
    - *WireMock*: per simulare le risposte delle API esterne (Aruba) e dell'Identity Provider durante i test.
- **Ambiente Locale**: `docker-compose.yaml` setup di bootstrap contenente database, Kafka, Keycloak e Wiremock.

## Responsabilità dei Componenti
- **`PecPollingService`**: Punto di ingresso del flusso. Simula la ricezione di una PEC, crea la traccia a database (tramite `DocumentWorkflowFactory`) e pubblica l'evento `AttachmentToSignEventDTO` sul topic Kafka.
- **`SignatureService`**: In ascolto sul topic di firma. Aggiorna lo stato, invoca le API esterne per la firma del documento. In caso di successo pubblica un evento su un secondo topic Kafka (`document-signed-topic`).
- **`ConservationService`**: In ascolto sul topic dei documenti firmati. Invoca le API esterne per portare in conservazione il documento e segna il flusso come `CONSERVED` nel DB.
- **`ArubaSignatureGateway` / `ArubaConservationGateway`**: Componenti delegati alle chiamate di rete verso l'esterno (`RestClient`). Dono protetti da Circuit Breaker.
- **`PecFilterController` / `PecFilterService`**: Gestiscono l'operatività CRUD sulle regole di filtraggio delle PEC (`PecFilter`).
- **`RepoManager`**: Gestisce l'interazione coi repository JPA, query ricorrenti in modo centralizzato e separa le logiche di DB dalle classi Service.

## Guida al Testing in Locale con Postman
I file per testare le API sono presenti nella cartella `postman`.
1. **Avvio infrastruttura**: Eseguire `docker-compose up -d` dalla root per far partire tutti i servizi (Kafka, DB, Keycloak, Wiremock e l'App).
2. **Importazione Collection**: Aprire Postman e importare il file `postman/postman-collection.json`.
3. **Autenticazione**: Le API esposte su `localhost:8080` richiedono l'autenticazione tramite OAuth2.0

## Configurazione Keycloak per OAuth2.0
Per permettere alle chiamate REST (eseguite tramite Postman) di autenticarsi, è necessario configurare Keycloak:

1. **Accesso alla Console**: Navigare su `http://localhost:8081` con le credenziali bootstrap (in `docker-compose.yaml` solitamente admin/admin).
2. **Creazione Realm**: Creare un nuovo realm e chiamarlo esattamente `gamma` (per far combaciare `issuer-uri=http://localhost:8081/realms/gamma`).
3. **Creazione Client**:
    - Andare nella sezione "Clients" e creare un client con Client ID: `gamma-client`.
    - Abilitare "Client authentication" e "Service accounts roles".
    - valorizzaree Valid redirect URIs e Valid redirect URIs (va bene anche *)
    - Segnarsi il "Client Secret" generato nella tab "Credentials". Modificare se necessario la proprietà `spring.security.oauth2.client.registration.aruba.client-secret` per farla combaciare.
    - Crearesi un utenza casualee e settare una password per queell'utenza
4. **Ottenimento Token (tramite Postman)**:
    - Configurare in Postman una richiesta POST all'indirizzo `http://localhost:8081/realms/gamma/protocol/openid-connect/token`.
    - Impostare sotto authorization grant -type `Password credentials`, client ID, Client Secret, e valorizzare i campi relativi all'utenza creata in precedenza.
    - Premere su `Get New Access Token`, usare Bearer Token della risposta in Postman per chiamare le API del controller.