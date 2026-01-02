# 🚛 GLP ERP System

Sistema ERP para Revendedora de Gás GLP - Goiás

## 🎯 Sobre o Projeto

Sistema completo de gestão para revendedoras de gás GLP, incluindo:

- 📝 Emissão de Notas Fiscais Eletrônicas (NFe)
- 📦 Controle de Estoque (Botijões Cheios e Vazios)
- 🚚 Gestão de Rotas e Frotas
- 💰 Financeiro (Contas a Pagar/Receber)
- 👥 Cadastro de Clientes e Funcionários
- 📊 Relatórios Gerenciais

## 🛠️ Stack Tecnológica

### Backend
- ☕ Java 17
- 🍃 Spring Boot 3.2.11
- 🗄️ PostgreSQL 16
- 🔄 Flyway (Migrations)
- 🐳 Docker & Docker Compose

### Frontend (Em desenvolvimento)
- 🅰️ Angular 20
- 💅 Angular Material

## 🚀 Como Executar

### Pré-requisitos
- Java 17+
- Maven 3.9+
- Docker & Docker Compose

### 1. Clonar o repositório
```bash
git clone https://github.com/Ozeias-Oz/glp-erp-system.git
cd glp-erp-system
```

### 2. Subir o banco de dados
```bash
docker compose up -d postgres
```

### 3. Executar o backend
```bash
cd backend
./mvnw spring-boot:run
```

### 4. Verificar saúde da aplicação
```bash
curl http://localhost:8080/actuator/health
```

Resposta esperada:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    }
  }
}
```

## 📂 Estrutura do Projeto

```
glp-erp-system/
├── backend/          # API REST (Spring Boot)
├── frontend/         # Interface Web (Angular)
├── docs/             # Documentação
└── docker-compose.yml
```

## 🗺️ Roadmap

- [x] Setup inicial do projeto
- [ ] Spring Security + JWT
- [ ] Módulo Fiscal (NFe)
- [ ] Controle de Estoque
- [ ] Gestão de Rotas
- [ ] Módulo Financeiro
- [ ] Relatórios

## 📄 Licença

MIT License - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 👨‍💻 Autor

**Ozeias Campos de Souza**
- GitHub: [@Ozeias-Oz](https://github.com/Ozeias-Oz)
