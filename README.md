# 🌟 Pokémon TCG Finder - Serviço de Catálogo e Coleção

[![Build Status](https://img.shields.io/badge/Status-Development-blue)](https://github.com/seu-usuario/seu-repo) 
[![Linguagem Principal](https://img.shields.io/badge/Linguagem-Kotlin-purple)](https://kotlinlang.org/)
[![Framework](https://img.shields.io/badge/Framework-Spring%20Boot%203-brightgreen)](https://spring.io/projects/spring-boot)

Este é o **Microserviço Principal** da aplicação Pokémon TCG Finder. Ele é responsável pela segurança (Keycloak), persistência das coleções (PostgreSQL) e integração com a API externa de dados de cartas.

### 🗺️ Arquitetura

O projeto segue a **Arquitetura Hexagonal (Ports & Adapters)** para garantir baixo acoplamento, alta testabilidade e separação clara entre o domínio de negócio e os detalhes de infraestrutura (DB, API externa, Kafka).

### 🎯 Objetivo do MVP (Épico PKMTCG-1)

Fornecer endpoints **seguros (via JWT/Keycloak)** para que o Front-end possa consultar a lista de expansões e detalhes das cartas, estabelecendo a fundação de segurança para as funcionalidades futuras (Economia e Coleção).

---

## 🛠️ 1. Setup do Ambiente Local (Docker Compose)

Todo o ambiente de infraestrutura local é gerenciado por um único arquivo `docker-compose.yml`.

### Pré-requisitos

* **Docker** e **Docker Compose** instalados (Essenciais para nossa Infra).
* Portas `5432`, `8080`, `9092` e `9000` livres.

### ⬇️ Iniciando os Serviços (O Comando Mágico)

Execute o comando abaixo na raiz do projeto para subir todos os contêineres:

```bash
docker compose up -d
````

Para verificar o status dos contêineres: `docker compose ps`

### ✅ Verificação dos Serviços Essenciais

Acesse as URLs abaixo e use as credenciais definidas na Task PKMTCG-T1.1 e T1.2:

| Serviço | Contêiner | Porta | Acesso (URL de UI) | Credenciais Admin |
| :--- | :--- | :--- | :--- | :--- |
| **Keycloak (Auth)** | `keycloak-auth` | 8080 | [http://localhost:8080](https://www.google.com/search?q=http://localhost:8080) | `keycloak_admin` / Senha 50 char |
| **PostgreSQL (DB)** | `postgres-db` | 5432 | (Acesso via IDE/DBeaver) | `pokemon_master` / Senha 50 char |
| **SonarQube (Qualidade)** | `sonarqube-analysis` | 9000 | [http://localhost:9000](https://www.google.com/search?q=http://localhost:9000) | `admin` / `admin` (Mude na primeira vez\!) |
| **Kafka (Mensageria)** | `kafka-broker` | 9092 | (Acesso interno pelo Spring Boot) | N/A |

### 🛑 Parando e Limpando

Para desligar e remover todos os contêineres e volumes (resetando o DB e Keycloak H2):

```bash
docker compose down -v
```
