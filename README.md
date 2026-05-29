# Music Library

Progetto finale del corso di Object-Oriented Programming.
Sviluppato da: **Matteo Zuppiroli**.

---

## Indice

1. [Panoramica del progetto](#1-panoramica-del-progetto)
2. [Tecnologie e pattern utilizzati](#2-tecnologie-e-pattern-utilizzati)
3. [Setup ed esecuzione](#3-setup-ed-esecuzione)
4. [Come si usa: la CLI](#4-come-si-usa-la-cli)
5. [Diagrammi UML](#5-diagrammi-uml)
6. [Architettura del codice](#6-architettura-del-codice)
7. [Test](#7-test)
8. [Limitazioni note e lavori futuri](#8-limitazioni-note-e-lavori-futuri)

---

## 1. Panoramica del progetto

**Music Library** è un'applicazione Java SE che permette di organizzare una libreria musicale in una struttura gerarchica annidata di **collezioni** e **tracce**. È pensata come strumento di catalogazione dei metadati (titolo, artista, BPM, chiave musicale, durata, genere), non come player audio.

L'applicazione include una **CLI interattiva** che permette di gestire la libreria direttamente da terminale.

### Il dominio scelto

Il dominio nasce da un caso d'uso personale: la gestione delle tracce per un format di DJ mix chiamato *Certified Blends*, pubblicato su Instagram e TikTok. Avere uno strumento per organizzare le tracce in collezioni annidate (per mood, BPM, anno, energia) è un bisogno concreto che ho voluto trasformare in un esercizio formativo di OOP.

### Cosa fa l'applicazione

L'utente può:
- Creare tracce musicali con i loro metadati
- Creare collezioni (es. *"R&B Slow"*, *"Mix Estate"*)
- Annidare collezioni dentro altre collezioni a profondità arbitraria
- Aggiungere tracce alle collezioni
- Calcolare conteggio totale e durata totale di una collezione (con ricorsione sui sotto-livelli)
- Iterare su tutte le tracce di una collezione (anche annidate)
- Filtrare le tracce per range di BPM, chiave musicale, genere
- Ordinare le tracce secondo criteri intercambiabili (BPM, titolo, artista, durata)
- Calcolare statistiche aggregate (BPM medio, durata totale, raggruppamento per genere)
- Annullare e rifare operazioni (undo/redo)
- Salvare la libreria su un file JSON e ricaricarla

### Cosa NON fa l'applicazione (scelta consapevole di scope)

- Non riproduce musica (niente audio decoding)
- Non ha una GUI: l'interazione avviene tramite CLI da terminale
- Non scarica metadati automaticamente da fonti esterne (Spotify API, MusicBrainz, ecc.)

Ho deliberatamente limitato lo scope a metadati e organizzazione perché aggiungere riproduzione o GUI avrebbe gonfiato il progetto con codice tecnico (codec audio, threading di playback, Swing/JavaFX) che non avrebbe esercitato i pattern OOP richiesti, e avrebbe introdotto superfici d'errore difficili da gestire dentro il vincolo temporale.

---

## 2. Tecnologie e pattern utilizzati

### Tecnologie

| Componente | Versione | Motivazione |
|---|---|---|
| **Java** | OpenJDK 21 (LTS) | Versione LTS consolidata, indicata dal corso, con ecosistema librerie maturo. Ho scartato Java 25 (LTS più recente) perché uscito da pochi mesi e con compatibilità librerie ancora non al pieno della maturità |
| **Maven** | 3.9.15 | Standard industriale per gestione dipendenze e build riproducibili. Permette di dichiarare JUnit nel `pom.xml` invece di gestire `.jar` a mano |
| **JUnit Jupiter** | 5.10.2 | Versione moderna di JUnit, API di assertion più espressive e supporto nativo per lambda e Java 8+ |
| **Maven Surefire** | 3.2.5 | Plugin Maven necessario per eseguire test JUnit 5 (la versione di default dell'archetype Maven era la 2.22.1, incompatibile con JUnit 5) |
| **exec-maven-plugin** | 3.1.1 | Permette di lanciare la CLI con `mvn exec:java` |
| **java.util.logging** | (standard JDK) | Scelto invece di SLF4J/Log4j per evitare dipendenze esterne. Sufficiente per il bisogno del progetto |
| **java.nio.file** | (standard JDK) | API moderna per I/O su file, più sicura e meno verbosa di `java.io` |

### Design Pattern obbligatori

#### Composite

**Dove**: interfaccia `LibraryNode`, classi `Track` (foglia) e `Collection` (composito).

**Perché**: il dominio è naturalmente ricorsivo. Una collezione musicale contiene tracce ed eventualmente altre collezioni (es. *"Tutta la musica"* > *"R&B"* > *"R&B 90s"* > tracce). Il Composite permette di trattare uniformemente foglie e compositi: quando chiedo `getTrackCount()` su un nodo, la risposta è coerente sia per una traccia singola (1) sia per una collezione (somma ricorsiva sui figli).

**Cosa l'ha resa ricorsiva**: la lista interna di `Collection` è dichiarata come `List<LibraryNode>`, non `List<Track>`. Questo permette di mettere dentro una `Collection` sia tracce sia altre collezioni. È la scelta progettuale chiave del pattern.

#### Iterator

**Dove**: classe `Collection` implementa `Iterable<Track>`, classe `RecursiveTrackIterator` implementa `Iterator<Track>`.

**Perché**: dato che la `Collection` può avere annidamenti, "scorrere tutte le tracce" non è banale. L'iteratore custom incapsula la logica di **visita in profondità (DFS)** e restituisce al client una sequenza piatta di tracce, indipendentemente da quanti livelli di annidamento ci siano.

**Tecnica**: uso uno **stack esplicito** (`ArrayDeque<LibraryNode>`) invece della ricorsione tradizionale. Quando incontro una `Collection` nella visita, ne espando i figli sulla pila; quando trovo una `Track`, la restituisco. Lo stack esplicito previene `StackOverflowError` su strutture molto profonde.

#### Factory

**Dove**: classe `LibraryNodeFactory`.

**Perché**: centralizzo la creazione di `Track` e `Collection` in un punto solo, così il resto del codice non si accoppia alle classi concrete. Il metodo polimorfico `createFromType(String type, Map<String, String> params)` decide quale classe istanziare in base al `type` ricevuto. Particolarmente utile nella deserializzazione JSON: il `JsonDeserializer` non sa quali classi esistono, chiama solo la factory.

**Implementazione**: classe utility con metodi `static` e costruttore privato che lancia `UnsupportedOperationException`. Idioma standard di Java per classi utility, in linea con `java.util.Collections` o `java.lang.Math`.

#### Exception Shielding

**Dove**: classe `LibraryService` (boundary del sistema) + classe generica `Result<T>` + gerarchia di eccezioni di dominio (`LibraryException`, `DuplicateNodeException`, `NodeNotFoundException`).

**Perché**: nessuna eccezione interna deve mai raggiungere l'utente finale così com'è. Stack trace esposti sono una vulnerabilità di information disclosure e generano penalità.

**Tecnica su 3 livelli**:

1. **Gerarchia di eccezioni di dominio** distinta dalle eccezioni tecniche di Java. Tutte estendono `RuntimeException` per essere unchecked: la gestione è centralizzata al boundary, non sparpagliata nelle firme dei metodi.

2. **Classe generica `Result<T>`** che incapsula esito di successo (con valore) o fallimento (con messaggio user-friendly). Ispirata a `Result<T, E>` di Rust e Kotlin. Il client lavora con un valore di ritorno tipizzato invece che con eccezioni propagate.

3. **`LibraryService` come boundary**: ogni metodo pubblico cattura le eccezioni interne, le **logga internamente** con stack trace completo, e ritorna un `Result.failure(messaggio)`. Distinguo nel catch tra errori "attesi" (validazione, duplicato) — per i quali espongo il messaggio specifico — ed errori "inattesi" — per i quali uso un messaggio generico per non rischiare information disclosure.

### Bonus β implementati

#### Stream API & Lambda

**Dove**: classe `Collection` espone `streamTracks()` + 6 metodi di convenienza basati su stream.

**Cosa permette**: query dichiarative su tutte le tracce annidate ricorsivamente. Esempi:

```java
collection.findTracksByBpmRange(85, 95);    // filtro per BPM
collection.findTracksByGenre("R&B");        // filtro per genere
collection.getAverageBpm();                  // OptionalDouble
collection.getTracksGroupedByGenre();        // Map<String, List<Track>>
```

Lo `streamTracks()` riusa l'iteratore esistente come sorgente, garantendo coerenza tra iterazione tradizionale (for-each) e funzionale (stream).

Tecniche moderne usate: **method reference** (`Track::getBpm`), **OptionalDouble** per evitare NPE, **Collectors.groupingBy** per raggruppamenti, **mapToInt** per statistiche su tipi primitivi.

#### Strategy

**Dove**: interfaccia `SortStrategy` + 4 implementazioni (`SortByBpm`, `SortByDuration`, `SortByTitle`, `SortByArtist`).

**Perché**: i criteri di ordinamento sono intercambiabili a runtime senza modificare il codice del chiamante. Applico l'**Open/Closed Principle**: aggiungere un nuovo criterio = nuova classe, nessuna modifica al codice esistente.

**Nota didattica**: Java 8 ha già l'interfaccia `Comparator<T>` che copre lo stesso caso d'uso. Ho preferito un'interfaccia `SortStrategy` esplicita per rendere il pattern visibile e didatticamente chiaro nel codice. In un'app reale userei direttamente `Comparator` per non reinventare la ruota.

#### Builder

**Dove**: classe statica annidata `Track.Builder`.

**Perché**: `Track` ha 6 parametri di costruzione, di cui 4 obbligatori e 2 opzionali (`key` e `genre`). Un costruttore con 6 parametri posizionali è illeggibile (cosa significa il `93`? il `280`?). Il Builder rende esplicito ogni campo con una sintassi fluente:

```java
Track t = new Track.Builder("Brown Sugar", "D'Angelo")
    .bpm(93)
    .key("Eb")
    .durationSeconds(280)
    .genre("R&B")
    .build();
```

La validazione è delegata al costruttore di `Track`, garantendo **un'unica fonte di verità** per le regole di dominio. Il costruttore originale resta disponibile per chi preferisce.

#### Memento

**Dove**: classe statica annidata `Collection.CollectionMemento` (Memento), metodi `saveMemento`/`restoreFromMemento` su `Collection` (Originator), classe `LibraryHistory` (Caretaker).

**Perché**: supportare undo/redo delle operazioni sulla libreria. I tre attori del pattern sono separati:
- **Originator** sa creare e ripristinare snapshot di sé stesso
- **Memento** è opaco dall'esterno: solo l'Originator può crearlo e leggerlo
- **Caretaker** gestisce le pile di undo/redo, ma non guarda mai dentro il Memento

L'opacità è garantita da: classe annidata statica + costruttore e getter **package-private** (visibili solo a chi sta nel package).

**Ottimizzazione**: poiché `Track` è già immutabile, lo snapshot fa **shallow copy** delle Track (sicura: nessuno può modificarle) e **deep copy ricorsiva** solo per le sotto-Collection. Sfrutto l'immutabilità come ottimizzazione di memoria.

#### Singleton

**Dove**: classe `LibraryConfig` per la configurazione applicativa.

**Perché**: la configurazione (cartella di default per i file salvati, range BPM valido, lunghezza massima dei nomi) deve essere coerente in tutta l'applicazione. Non avrebbe senso avere più istanze.

**Implementazione**: lazy initialization con **double-checked locking** + variabile `volatile`. Il primo check evita il costo del `synchronized` nel caso comune (istanza già creata), il secondo previene la doppia creazione con accessi concorrenti. `volatile` garantisce visibilità del valore tra thread.

**Nota didattica**: sono consapevole che Singleton è spesso considerato un anti-pattern quando applicato a sproposito (introduce stato globale, complica i test). Qui l'uso è limitato a configurazione di sola lettura, quindi gli inconvenienti tipici non si manifestano. Per i test ho previsto un metodo `resetForTesting()` package-private.

### Tecnologie core utilizzate

| Tecnologia | Dove | Punti |
|---|---|---|
| **Collections Framework** | `ArrayList<LibraryNode>` (figli), `HashMap<String, String>` (params della factory), `ArrayDeque<LibraryNode>` (stack iteratore e undo), `Collections.unmodifiableList()` (immutabilità della vista esposta) | 3 |
| **Generics** | `List<LibraryNode>`, `Iterator<Track>`, `Iterable<Track>`, classe generica `Result<T>` con metodi statici generici | 3 |
| **Java I/O** | `java.nio.file.Path`, `Files.writeString`, `Files.readString`, `Files.exists`. JSON parser custom (`JsonSerializer`, `JsonDeserializer`) | 3 |
| **Logging** | `java.util.logging.Logger` con livelli `SEVERE`/`WARNING`/`INFO` differenziati per tipo di errore | 2 |
| **JUnit Testing** | 113 test in 11 suite, pattern AAA, `@TempDir` per test su filesystem, test specifici anti-information-disclosure | 3 |

### Validazione e sicurezza

Ogni costruttore valida i parametri in ingresso e lancia eccezioni di dominio appropriate per input invalidi. Le eccezioni vengono catturate al boundary (`LibraryService`) e tradotte in messaggi user-friendly. Questo previene:
- Crash dell'applicazione su input invalido (validazione fail-fast)
- Information disclosure di dettagli implementativi (Exception Shielding)

In sostanza, **un oggetto invalido non può esistere nel sistema**.

---

## 3. Setup ed esecuzione

### Prerequisiti

- Java 21 (OpenJDK)
- Apache Maven 3.9 o superiore

Su macOS con Homebrew:
```bash
brew install openjdk@21 maven
```

Verifica:
```bash
java -version    # deve riportare 21.x.x
mvn --version    # deve riportare 3.9.x e Java 21
```

### Costruzione del progetto

Posizionati nella cartella radice del progetto (quella che contiene `pom.xml`):

```bash
cd music-library
mvn clean compile
```

Output atteso: `BUILD SUCCESS`.

### Esecuzione dei test

```bash
mvn test
```

Output atteso: `Tests run: 113, Failures: 0, Errors: 0` + `BUILD SUCCESS`.

### Esecuzione della CLI

```bash
mvn exec:java
```

Si avvia la CLI interattiva (vedi sezione successiva).

---

## 4. Come si usa: la CLI

Una volta lanciata la CLI, vedi il prompt:

```
=================================================
   🎵  Music Library CLI v1.0
=================================================
Digita 'help' per la lista dei comandi.

music-library>
```

### Comandi disponibili

| Comando | Effetto |
|---|---|
| `help` | Mostra l'elenco dei comandi |
| `create-collection <nome>` | Crea una nuova collezione e la rende corrente |
| `create-track <titolo> <artista> <bpm> <key> <durata> <genere>` | Crea una traccia e la aggiunge alla collezione corrente. Usare le virgolette per titoli/nomi con spazi |
| `list` | Mostra tutte le tracce della collezione corrente |
| `filter-bpm <min> <max>` | Mostra solo le tracce con BPM nell'intervallo |
| `filter-genre <genere>` | Mostra solo le tracce di un genere |
| `sort <bpm\|title\|artist\|duration>` | Mostra le tracce ordinate secondo il criterio |
| `stats` | Mostra statistiche: BPM medio, durata totale, raggruppamento per genere |
| `save <file>` | Salva la collezione corrente su file JSON |
| `load <file>` | Carica una collezione da file JSON |
| `undo` | Annulla l'ultima operazione |
| `redo` | Rifà l'operazione annullata |
| `exit` | Esci |

### Esempio di sessione tipica

```
music-library> create-collection "Mix Estate 2026"
✓ Creata collezione: Mix Estate 2026

music-library [Mix Estate 2026]> create-track "Brown Sugar" "D'Angelo" 93 Eb 280 R&B
✓ Aggiunta: Brown Sugar — D'Angelo

music-library [Mix Estate 2026]> create-track "One in a Million" "Aaliyah" 90 Am 270 R&B
✓ Aggiunta: One in a Million — Aaliyah

music-library [Mix Estate 2026]> stats

Statistiche di 'Mix Estate 2026':
  Tracce totali:  2
  Durata totale:  9:10
  BPM medio:      91.5
  Per genere:
    R&B: 2

music-library [Mix Estate 2026]> filter-bpm 85 95

Tracce con BPM tra 85 e 95 (2 trovate):
   1. Brown Sugar         D'Angelo       93 BPM  Eb   4:40  [R&B]
   2. One in a Million    Aaliyah        90 BPM  Am   4:30  [R&B]

music-library [Mix Estate 2026]> save mix.json
✓ Salvato su: /Users/matteo/Projects/music-library/mix.json
```

### Architettura della CLI

La CLI è un **thin wrapper** sopra il `LibraryService`: si occupa solo del parsing degli input testuali e della formattazione degli output. Tutta la logica di business resta nei layer sottostanti. Eredita automaticamente l'**Exception Shielding** dal `LibraryService` — nessuno stack trace o dettaglio interno raggiunge l'utente.

---

## 5. Diagrammi UML

I diagrammi sono disponibili in formato draw.io nella cartella `docs/`.

### Diagramma di classe

File: [`docs/class-diagram.drawio`](docs/class-diagram.drawio)

Mostra la gerarchia del pattern Composite:
- L'interfaccia `LibraryNode` con i 3 metodi del contratto
- `Track` come foglia (con tutti gli attributi e metodi)
- `Collection` come composito, con due relazioni distinte verso `LibraryNode`:
  - Una di **realizzazione** (`Collection` implementa `LibraryNode`)
  - Una di **composizione ricorsiva** (`Collection` contiene `children: List<LibraryNode>`)

Questa doppia natura è il cuore del Composite.

### Diagramma architetturale

File: [`docs/architecture-diagram.drawio`](docs/architecture-diagram.drawio)

Mostra i 5 layer logici dell'applicazione, dall'esterno verso l'interno:

1. **User / CLI** — punto di ingresso
2. **LibraryService** — boundary del sistema, applica Exception Shielding
3. **LibraryNodeFactory** — creazione centralizzata degli oggetti
4. **Domain Layer** — `LibraryNode`, `Track`, `Collection`, `RecursiveTrackIterator` (cuore del Composite + Iterator)
5. **Persistence** — `LibraryRepository`, `JsonSerializer`, `JsonDeserializer` (Java I/O)

Lateralmente, i **cross-cutting concerns** (`Result<T>`, `Logger`, gerarchia di eccezioni) sono usati trasversalmente da tutti i layer.

---

## 6. Architettura del codice

### Struttura dei sorgenti

```
music-library/
├── pom.xml                                       # Configurazione Maven
├── README.md
├── docs/
│   ├── class-diagram.drawio
│   └── architecture-diagram.drawio
├── src/
│   ├── main/java/com/matteo/musiclibrary/
│   │   ├── LibraryNode.java                      # Interfaccia (Composite)
│   │   ├── Track.java                            # Foglia + Builder annidato
│   │   ├── Collection.java                       # Composito + Iterable + Stream + Memento
│   │   ├── RecursiveTrackIterator.java           # Iterator custom (DFS)
│   │   ├── LibraryNodeFactory.java               # Factory
│   │   ├── LibraryException.java                 # Eccezione base di dominio
│   │   ├── DuplicateNodeException.java
│   │   ├── NodeNotFoundException.java
│   │   ├── Result.java                           # Generico, per Exception Shielding
│   │   ├── LibraryService.java                   # Boundary del sistema
│   │   ├── JsonSerializer.java                   # Serializzazione JSON ricorsiva
│   │   ├── JsonDeserializer.java                 # Parser ricorsivo
│   │   ├── LibraryRepository.java                # I/O su disco
│   │   ├── SortStrategy.java                     # Strategy (interfaccia)
│   │   ├── SortByBpm.java                        # Strategy concreta
│   │   ├── SortByDuration.java                   # Strategy concreta
│   │   ├── SortByTitle.java                      # Strategy concreta
│   │   ├── SortByArtist.java                     # Strategy concreta
│   │   ├── LibraryHistory.java                   # Caretaker del Memento
│   │   ├── LibraryConfig.java                    # Singleton di configurazione
│   │   └── cli/
│   │       └── MusicLibraryCLI.java              # Interfaccia a riga di comando
│   └── test/java/com/matteo/musiclibrary/
│       ├── TrackTest.java                        # 13 test
│       ├── CollectionTest.java                   # 19 test
│       ├── LibraryNodeFactoryTest.java           # 11 test
│       ├── ResultTest.java                       #  7 test
│       ├── LibraryServiceTest.java               #  8 test
│       ├── LibraryRepositoryTest.java            #  7 test
│       ├── CollectionStreamTest.java             # 12 test
│       ├── SortStrategyTest.java                 #  9 test
│       ├── TrackBuilderTest.java                 #  8 test
│       ├── LibraryHistoryTest.java               # 12 test
│       └── LibraryConfigTest.java                #  7 test
```

### Scelte progettuali principali

#### Immutabilità di `Track`

`Track` non ha setter: una volta creata, i suoi attributi non cambiano. Ho applicato il principio dei **Value Object**: garantisce thread-safety e prevedibilità del comportamento, eliminando una classe di bug legati a modifiche di stato non controllate. Se in futuro servisse modificare una traccia, si crea una nuova `Track`.

L'immutabilità ha anche permesso un'ottimizzazione nel Memento: lo snapshot fa shallow copy delle Track perché non possono essere modificate comunque.

#### `equals` e `hashCode` per identità semantica

Due `Track` sono considerate "uguali" se hanno **stesso titolo e stesso artista**, indipendentemente da maiuscole/minuscole. Questo riflette una scelta di dominio: la stessa canzone con piccole varianti di metadati (BPM diverso, key diversa, durata diversa) resta concettualmente la stessa traccia, e non ha senso averla due volte nella stessa playlist.

Ho rispettato il contratto di Java: ogni `equals` ridefinita richiede una `hashCode` consistente, altrimenti `HashMap` e `HashSet` non funzionano correttamente.

#### Encapsulation della lista dei figli

`Collection.getChildren()` ritorna una vista **non modificabile** (`Collections.unmodifiableList`) della lista interna. Senza questa precauzione, un client potrebbe modificare la lista dall'esterno bypassando il metodo `add()` e tutte le sue validazioni (es. controllo dei duplicati). Mantengo la `Collection` padrona del suo stato.

#### Parser JSON custom invece di Gson/Jackson

Ho scelto di scrivere un parser JSON a mano (`JsonSerializer` e `JsonDeserializer`) invece di usare librerie standard come Gson o Jackson. Le motivazioni:

- Il dominio dei dati è ristretto e ben definito (solo due tipi di oggetto), non serve un parser general-purpose
- Evito di introdurre dipendenze esterne con relativi rischi di vulnerabilità e breaking changes
- Il parser ricorsivo dimostra padronanza dei pattern di parsing (recursive descent)

Lo svantaggio è che il parser non gestisce ogni edge case di JSON arbitrario, ma è sufficiente per il formato prodotto dal serializzatore.

#### `Result<T>` invece di propagare eccezioni

Il `LibraryService` non propaga eccezioni: ritorna `Result<T>`. Questo trasforma gli errori in **valori di ritorno tipizzati**, eliminando la necessità di `try/catch` nel codice del client. È un approccio funzionale ispirato a Rust, Swift, Kotlin. In Java è meno comune, ma diventa naturale una volta abituati.

#### CLI come thin wrapper

La `MusicLibraryCLI` si limita a parsing degli input testuali e formattazione degli output. Non aggiunge logica di business: chiama solo metodi del `LibraryService` o della `Collection`. Questa separazione mantiene il dominio totalmente indipendente dall'interfaccia utente — un domani potrei aggiungere una GUI o un'API HTTP senza toccare il dominio.

---

## 7. Test

Il progetto include **113 test JUnit 5** organizzati in 11 suite. Tutti passano in meno di 2 secondi.

```
TrackTest:                13 test  (validazione, getter, LibraryNode, equals/hashCode)
CollectionTest:           19 test  (costruzione, add, annidamento ricorsivo, iteratore)
LibraryNodeFactoryTest:   11 test  (metodi diretti, createFromType, validazione)
ResultTest:                7 test  (success/failure, getValue/getError)
LibraryServiceTest:        8 test  (Exception Shielding, no information disclosure)
LibraryRepositoryTest:     7 test  (save/load round-trip, annidamento, errori I/O)
CollectionStreamTest:     12 test  (streamTracks, filtri, statistiche, raggruppamenti)
SortStrategyTest:          9 test  (criteri di ordinamento, immutabilità input)
TrackBuilderTest:          8 test  (sintassi fluente, default, validazione delegata)
LibraryHistoryTest:       12 test  (undo/redo, deep copy, isolamento snapshot)
LibraryConfigTest:         7 test  (singleton, thread-safety, integrazione con Track)
─────────────────────────────────
TOTALE:                  113 test
```

### Tecniche utilizzate

- **Pattern Arrange-Act-Assert** per ogni test
- **Nomi descrittivi in italiano** con underscore (il nome del test è documentazione)
- **`@TempDir` di JUnit 5** per test che usano filesystem (cleanup automatico)
- **`@AfterEach`** per resettare lo stato del Singleton tra un test e l'altro
- **`assertThrows` con lambda** per verificare il sollevamento di eccezioni
- **`assertSame`** per verificare unicità di istanza nel Singleton
- **Test anti-information-disclosure** in `LibraryServiceTest`: verifico che i messaggi di errore esposti NON contengano parole come "Exception" o riferimenti ai package interni

### Cosa testano

- **Happy path**: comportamento corretto in condizioni normali
- **Validazione del costruttore**: ogni regola di validazione ha un test che verifica il sollevamento dell'eccezione corretta
- **Comportamento ricorsivo**: collezioni annidate a 2 e 3 livelli, conteggi e durate calcolati ricorsivamente
- **Encapsulation**: la lista esposta da `getChildren()` rifiuta modifiche
- **Round-trip di persistenza**: salvataggio + ricaricamento restituisce un nodo equivalente
- **Comportamento dell'iteratore**: visita in profondità, supporto for-each, `NoSuchElementException` dopo la fine
- **Stream e aggregazioni**: filtri, medie, raggruppamenti
- **Strategie di ordinamento**: ogni criterio in isolamento + integrazione con Collection
- **Memento isolation**: modifiche dopo lo snapshot non influenzano il memento (deep copy ricorsiva)
- **Singleton unicità**: stesso oggetto ritornato sempre, configurazione coerente

---

## 8. Limitazioni note e lavori futuri

### Limitazioni note

- **CLI a singola collezione corrente**: la CLI gestisce una sola collezione "root" alla volta. Per lavorare con più alberi, l'utente deve fare `save`/`load` esplicitamente. Una versione futura potrebbe supportare uno "stack di collezioni" attive
- **Parser JSON semplificato**: gestisce il formato prodotto dal serializzatore e poco più. Non è un parser JSON general-purpose (non gestisce numeri in notazione scientifica, escape Unicode estesi, ecc.)
- **Niente concorrenza**: l'applicazione non è progettata per accessi multithread. La `Collection` non è thread-safe nelle mutazioni (anche se la lista esposta è immutabile)
- **Confronto di uguaglianza basato solo su titolo + artista**: due tracce con titolo/artista identici sono considerate uguali anche se hanno BPM o durata diversi. Per il dominio specifico è una scelta voluta, ma in scenari diversi potrebbe non essere adeguata
- **Le `Collection` non hanno un controllo sull'annidamento ciclico**: in teoria si potrebbe aggiungere una collezione `A` come figlia di se stessa, creando un ciclo. Il codice non lo previene esplicitamente (lo si potrebbe fare con una verifica DFS in `add()`)
- **Logging configurato di default**: il logger di Java standard scrive su `System.err` con formato di default. In produzione si configurerebbe un file di log con rotazione, formato strutturato e livelli filtrati per ambiente

### Lavori futuri

- **Stack di collezioni nella CLI**: permettere di navigare in collezioni annidate con comandi tipo `enter <nome>` / `back`
- **Comando di ricerca testuale**: cercare tracce per parola chiave nel titolo/artista
- **Esportazione in formati alternativi** (CSV, M3U per playlist standard)
- **Tag arbitrari** sulle tracce con `Set<String>` per categorizzazioni flessibili
- **Validazione anti-cicli** nell'`add()` della `Collection`
- **GUI desktop** con JavaFX per chi preferisce un'interfaccia visuale
- **Eventuale API HTTP** per integrazione con tool esterni (web app, mobile)

---

## Note sull'autore

Questo progetto è stato sviluppato come prima esperienza significativa di programmazione in Java. Le scelte di design (immutabilità, Exception Shielding via `Result<T>`, parser JSON custom, Memento con deep copy ottimizzata) sono state fatte in modo consapevole privilegiando la chiarezza concettuale e la robustezza rispetto alla rapidità di implementazione.

Il codice è organizzato per essere leggibile e ben testato. Ogni scelta tecnica è motivata nel codice (commenti Javadoc) e in questo README.
