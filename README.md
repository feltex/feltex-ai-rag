# Projeto de Ingestão e Chat com Spring Boot

Este projeto é uma aplicação desenvolvida em **Java** utilizando o framework **Spring Boot**. Ele possui duas funcionalidades principais:

1. **Ingestão de Arquivos PDF**: Processa arquivos PDF localizados na pasta `docs` e armazena informações em um banco de dados.
2. **Chat API**: Fornece uma API REST para interagir com um cliente de chat.

## Tecnologias Utilizadas

- **Java 21**
- **Spring Boot**
- **Lombok**
- **JDBC**
- **Maven**
- **Banco de Dados Relacional (SQL)**

## Estrutura do Projeto

### Tabela no Banco de Dados

A tabela `ingested_files` é utilizada para rastrear os arquivos PDF já processados:

```sql
CREATE TABLE IF NOT EXISTS ingested_files (
    filename VARCHAR PRIMARY KEY,
    ingested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```


### Componentes Principais

- **Ingestão de Arquivos**: O componente `FileIngestionService` é responsável por processar os arquivos PDF e armazenar informações no banco de dados.

- **Chat API**: O componente `ChatController` fornece uma interface REST para interagir com o cliente de chat. Ele utiliza o serviço `ChatService` para processar as mensagens e gerar respostas.

- **Configuração do Banco de Dados**: O arquivo `application.properties` contém as configurações necessárias para conectar ao banco de dados PostgreSQL.

### Configuração do Banco de Dados

O arquivo `application.properties` contém as configurações necessárias para conectar ao banco de dados PostgreSQL. Certifique-se de ajustar as propriedades de acordo com seu ambiente:



