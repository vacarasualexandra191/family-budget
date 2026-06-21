# family-budget
Aplicație web pentru gestionarea bugetului unei familii

## Cuprins

- [Descrierea Proiectului](#-descrierea-proiectului)
- [Arhitectură](#-arhitectură)
- [Diagrama ER](#-diagrama-er)
- [Setup Instructions](#-setup-instructions)
- [Funcționalități și Cerințe Implementate](#-funcționalități-și-cerințe-implementate)
- [API Documentation](#-api-documentation)
- [Screenshots](#-screenshots)
- [Testare](#-testare)
- [Monitorizare](#-monitorizare)
- [Securitate](#-securitate)
- [Contribuții](#-contribuții)
- [AI in Development](#-ai-in-development)

## Descrierea Proiectului

Family Budget App permite membrilor unei familii sa:
- Gestioneze conturi bancare și numerar 
- Inregistreze venituri și cheltuieli, organizate pe categorii
- Definească bugete lunare per categorie, la nivel de familie
- Urmărească automat progresul cheltuielilor față de bugetele stabilite
- Vizualizeze istoricul tranzacțiilor, cu filtrare, sortare și paginare
- Eticheteze tranzacții cu taguri libere (ex:"urgent")
- Configureze plați recurente (abonamente, facturi)

## Motivul alegerii temei aplicatiei
Am ales sa dezvolt aceasta aplicatie din pasiunea pentru finante si preocuparea optimizarii lor in mod cat mai eficient. Lucrez in domeniul financiar-bancar si observ foarte multe greseli in gestionarea finantelor personale. Am considerat ca o
aplicatie de gestionare a bugetului unei familii este esentiala pentru a asigura stabilitatea financiara si pentru a atinge obiectivele financiare pe termen lung. O aplicatie care sa ajute la organizarea veniturilor, cheltuielilor si bugetelor poate fi un instrument valoros pentru familii, oferindu-le o mai buna vizibilitate asupra situatiei lor financiare si facilitand luarea deciziilor informate.


## Arhitectură
### Model de Date
| Entitate | Descriere | Relații |
|----------|-----------|---------|
| `Family` | Grupează membrii familiei | 1→N `User`, 1→N `Budget` |
| `User` | Membru al familiei, cu cont de login | N→1 `Family`, N→N `Role`, 1→N `Account` |
| `Role` | Rol de securitate | N→N `User` |
| `Account` | Cont bancar/numerar | N→1 `User`, 1→N `Transaction`, 1→1 `RecurringPayment` |
| `Category` | Categorie venit/cheltuială | 1→N `Transaction`, 1→N `Budget` |
| `Transaction` | Tranzacție financiară | N→1 `Account`, N→1 `Category`, N→N `Tag` |
| `Budget` | Limită lunară per categorie/familie | N→1 `Category`, N→1 `Family` |
| `Tag` | Etichetă liberă | N→N `Transaction` |
| `RecurringPayment` | Plată recurentă | 1→1 `Account` |

### Diagrama ER
![Diagrama ER](docs/diagrama-er.png)

### Framework și persistență
- **Spring Boot 3.3.4** — framework principal 
- **Spring MVC** — gestionarea cererilor HTTP si controllerelor
- **Spring Data JPA** — abstractizare peste accesul la date
- **Hibernate** — implementarea ORM (Object-Relational Mapping)
- **Spring Validation (Bean Validation / JSR-380)** — validare server-side cu `@Valid`, `@NotNull`, `@NotBlank`, `@Positive`, `@Email`
### Bază de date
- **Oracle Database XE 21c** (sau altă ediție compatibilă) — bază de date relațională pentru profilul `dev`
- **Oracle SQL Developer** — client folosit pentru administrarea schemei, rulare scripturi SQL și generarea diagramei ER
- **H2 Database** (in-memory) — bază de date izolată folosită exclusiv pentru profilul `test`
- **Oracle JDBC Driver (ojdbc11)** — driver de conectare
- 
### Securitate
- **Spring Security 6** — autentificare și autorizare
- **JdbcUserDetailsManager** — autentificare JDBC cu query-uri SQL custom
- **BCrypt** — hashing parole
- **CSRF Protection** — activă implicit (token integrat în formularele Thymeleaf)

### Frontend
- **Thymeleaf** — pentru generarea paginilor HTML
- **Thymeleaf Extras Spring Security 6** 
- **Bootstrap 5.3** 
- **Bootstrap Icons** 
- **JavaScript (vanilla)** 

### Logging și monitorizare
- **SLF4J** 
- **Logback** 
- **Spring Boot Actuator** — expunere endpoint-uri de health-check și metrici

### Testare
- **JUnit 5** — framework de testare
- **Mockito** — mocking pentru teste unitare de service layer
- **Spring Boot Test** (`@SpringBootTest`) — teste de integrare end-to-end
- **AssertJ** — asserțiuni fluente în teste

### Structura Proiectului

```
src/
├── main/
│   ├── java/com/familybudget/
│   │   ├── controller/      # Controllere MVC 
│   │   ├── dto/              # DTO-uri pentru formulare (cu validare Bean Validation)
│   │   ├── entity/            # 8 entități JPA
│   │   ├── exception/        # Excepții custom + GlobalExceptionHandler centralizat
│   │   ├── repository/       # Spring Data JPA repositories (cu Pageable + Sort)
│   │   ├── security/         # SecurityConfig (JDBC auth, BCrypt, roluri)
│   │   └── service/          # Interfețe + implementări (logică de business)
│   └── resources/
│       ├── templates/        # Thymeleaf (layout + pagini per entitate + erori custom)
│       ├── static/           # CSS, JS
│       ├── application.yml          # Config comună
│       ├── application-dev.yml      # Profil Oracle (dezvoltare)
│       ├── application-test.yml     # Profil H2 (testare)
│       └── logback-spring.xml       # Configurare logging
└── test/
    └── java/com/familybudget/
        ├── service/           # Unit tests (Mockito) 
        └── integration/       # Integration tests end-to-end (H2)
```
---

