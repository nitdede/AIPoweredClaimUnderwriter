# AI-Powered Claim Underwriter

[![GitHub Repository](https://img.shields.io/badge/GitHub-AIPoweredClaimUnderwriter-blue?logo=github)](https://github.com/nitdede/AIPoweredClaimUnderwriter)
[![MCP Server](https://img.shields.io/badge/GitHub-HelpDeskMCPServer-green?logo=github)](https://github.com/nitdede/HelpDeskMCPServer)

A comprehensive Spring Boot application that leverages AI agents to automate insurance claim processing, policy ingestion, and customer support. The system combines intelligent invoice extraction, claim adjudication with ReAct agents, RAG-based policy knowledge retrieval, and real-time streaming help desk assistance.

## 🏗️ Architecture Overview

- **Frontend**: Responsive TypeScript/JavaScript web application with real-time streaming chat
- **Backend**: Spring Boot 3.5.9 with Spring AI 1.1.2 framework
- **AI Integration**: OpenAI GPT-4o for chat and ReAct agents, with text-embedding-3-small for embeddings
- **Database**: PostgreSQL with pgVector for vector similarity search
- **File Processing**: PDF and text invoice extraction using Apache PDFBox
- **Real-time Features**: Server-Sent Events for streaming AI responses

## ✨ Key Features

### 🤖 AI-Powered Claim Processing
- **Intelligent Invoice Extraction**: Automatically extracts patient information, amounts, and line items from invoices
- **ReAct Agent Pipeline**: Uses reasoning and acting agents for complex claim adjudication decisions
- **Policy Knowledge Integration**: RAG-based system retrieves relevant policy information for decision making
- **Evidence Tracking**: Maintains audit trail of claim decisions with supporting evidence

### 🛠️ MCP (Model Context Protocol) Integration
- **HelpDeskMCPServer**: Dedicated MCP server providing specialized help desk tools
- **Tool-Enabled AI**: AI agents can invoke external tools for enhanced functionality
- **Claim Lookup Tools**: Direct access to claim decision data through MCP tools
- **Ticket Management**: Create and manage help desk tickets via MCP tools
- **Policy Vector Search**: `readUserPolicy` tool performs semantic search on policy documents using vector embeddings to retrieve relevant coverage information for customer inquiries

### 📄 Policy Management
- **Document Ingestion**: Upload and process insurance policy documents
- **Vector Embeddings**: Converts policy text into searchable vector representations
- **Metadata Storage**: Tracks policy IDs, customer information, and document relationships

### 💬 Interactive Help Desk
- **Real-time Streaming Chat**: Live AI responses with streaming text updates
- **Contextual Assistance**: AI understands claim history and policy details
- **Memory-Enabled Conversations**: Maintains conversation context across interactions
- **Customer Support Tools**: Handles inquiries about claims, coverage, and policy details

### 🎨 Modern Web Interface
- **Responsive Design**: Mobile-friendly interface with professional styling
- **File Upload Support**: Drag-and-drop for invoices and policy documents
- **Real-time Feedback**: Live streaming responses and loading indicators
- **Interactive Chat**: Conversation history with message bubbles and typing indicators

## 🛠️ Technology Stack

### Backend
- **Spring Boot 3.5.9** - Main application framework
- **Spring AI 1.1.2** - AI integration and vector operations
- **Spring Data JPA** - Database access layer
- **PostgreSQL** - Primary database with pgVector extension
- **Apache PDFBox 2.0.31** - PDF text extraction
- **Jackson** - JSON processing
- **Lombok** - Code generation

### Frontend  
- **TypeScript/JavaScript** - Modern web development
- **HTML5 & CSS3** - Responsive design with CSS Grid/Flexbox
- **Fetch API** - HTTP client with streaming support
- **Server-Sent Events** - Real-time data streaming

### AI & Machine Learning
- **OpenAI GPT-4o** - Advanced language model for reasoning and chat
- **OpenAI text-embedding-3-small** - Text embeddings for semantic search
- **Vector Similarity Search** - pgVector for efficient similarity queries
- **ReAct Agents** - Reasoning and acting AI agents for complex decisions
- **MCP (Model Context Protocol)** - Tool integration protocol for AI agents
- **Spring AI MCP Client** - Client for connecting to MCP servers

## 🎨 Frontend Development

### TypeScript Configuration
The frontend is built with TypeScript and compiled to ES2020 modules. The build system is configured in `tsconfig.json`:

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "ES2020",
    "lib": ["DOM", "ES2020"],
    "strict": true,
    "outDir": "src/main/ui/assets/js",
    "rootDir": "src/main/ui/assets/js"
  },
  "include": ["src/main/ui/assets/js/*.ts"]
}
```

### Building the Frontend
```bash
# Compile TypeScript files
npm run build:ui

# Or manually compile
npx tsc
```

**Important**: The Maven build automatically copies UI files to the JAR:
- `src/main/ui/pages/` → `target/classes/static/`
- `src/main/ui/css/` → `target/classes/static/css/`
- `src/main/ui/node/src/` → `target/classes/static/node/src/`

This happens during the `generate-resources` phase via the `maven-resources-plugin`.

### File Structure
```
src/main/ui/
├── assets/
│   └── js/
│       ├── app.ts        # Main application logic
│       ├── app.js        # Compiled JavaScript (ES modules)
│       ├── model.ts      # TypeScript interfaces
│       └── model.js      # Compiled interfaces
├── pages/
│   └── index.html        # Main web page
└── README.md
```

### Key Features
- **ES Modules**: Uses modern ES2020 module system with `type="module"`
- **Interface Separation**: TypeScript interfaces are in `model.ts`
- **Event-Driven Architecture**: File upload and input method switching
- **Real-time Updates**: Server-Sent Events for streaming responses
- **Responsive Design**: Mobile-first CSS with modern layouts

## 📋 Prerequisites

- **Java 21+** (configured in pom.xml)
- **Maven 3.6+** (wrapper included: `./mvnw`)
- **PostgreSQL 15+** with **pgVector** extension
- **OpenAI API Key** (required for AI features)
- **HelpDeskMCPServer** ([GitHub Repository](https://github.com/nitdede/HelpDeskMCPServer)) - Companion MCP server for help desk tools
- **Node.js 18+** (optional, for frontend development)

## 🚀 Quick Start

### 1. Database Setup
```bash
# Install PostgreSQL and pgVector extension
# Create database
createdb insurance_ai

# Connect and enable vector extension
psql insurance_ai
CREATE EXTENSION IF NOT EXISTS vector;
```

### 2. Environment Configuration
```bash
# Set required environment variables
export OPENAI_API_KEY=your_openai_api_key_here
export MCP_CLIENT_ENABLED=true
```

### 3. Database Configuration
Update `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/insurance_ai
    username: your_username
    password: your_password
```

### 4. Build and Run
```bash
# Build the application
./mvnw clean package

# Run locally
./mvnw spring-boot:run

# Or run the JAR
java -jar target/AIPoweredClaimUnderwriter-0.0.1-SNAPSHOT.jar
```

### 5. Access the Application
- **Web Interface**: http://localhost:8081
- **Health Check**: http://localhost:8081/actuator/health
- **API Documentation**: See API Endpoints section below

## 🔗 API Endpoints

### Policy Management
- **GET** `/ingestion/test` - Health check for ingestion service
  ```bash
  # Local development
  curl http://localhost:8081/ingestion/test
  
  # Docker deployment
  curl http://localhost:8080/ingestion/test
  ```

- **POST** `/ingestion/saveDocument` - Save policy metadata and trigger RAG ingestion
  ```bash
  # Local development
  curl -X POST -H "Content-Type: application/json" \
    -d '{"policyId":"P123","customerId":"C456","policyNumber":"POL-001"}' \
    http://localhost:8081/ingestion/saveDocument
  
  # Docker deployment
  curl -X POST -H "Content-Type: application/json" \
    -d '{"policyId":"P123","customerId":"C456","policyNumber":"POL-001"}' \
    http://localhost:8080/ingestion/saveDocument
  ```

### Claim Processing
- **POST** `/claims/readInvoice` - Extract invoice fields using AI
  ```bash
  # Local development
  curl -X POST -H "Content-Type: application/json" \
    -d '{"invoiceText":"Patient: John Doe\nTotal: $1,234.56\nService: Surgery"}' \
    http://localhost:8081/claims/readInvoice
  
  # Docker deployment
  curl -X POST -H "Content-Type: application/json" \
    -d '{"invoiceText":"Patient: John Doe\nTotal: $1,234.56\nService: Surgery"}' \
    http://localhost:8080/claims/readInvoice
  ```

- **POST** `/claims/process-react` - Process claim with ReAct agents
  ```bash
  # Local development
  curl -X POST -H "Content-Type: application/json" \
    -d '{"invoiceText":"..."}' \
    "http://localhost:8081/claims/process-react?policyNumber=POL-001&userName=John"
  
  # Docker deployment
  curl -X POST -H "Content-Type: application/json" \
    -d '{"invoiceText":"..."}' \
    "http://localhost:8080/claims/process-react?policyNumber=POL-001&userName=John"
  ```

- **POST** `/claims/process-claim` - Upload and process claim files
  ```bash
  # Local development
  curl -X POST -F "file=@invoice.pdf" \
    "http://localhost:8081/claims/process-claim?policyNumber=POL-001&patientName=John"
  
  # Docker deployment
  curl -X POST -F "file=@invoice.pdf" \
    "http://localhost:8080/claims/process-claim?policyNumber=POL-001&patientName=John"
  ```

### Help Desk (Real-time Streaming)
- **POST** `/api/helpdesk-call/helpUser` - Get streaming AI assistance
  ```bash
  # Local development
  curl -X POST -H "Content-Type: application/json" \
    -d '{"claimId":"12345","issueDescription":"Question about coverage","customerName":"John Doe","policyNumber":"POL-001"}' \
    http://localhost:8081/api/helpdesk-call/helpUser
  
  # Docker deployment
  curl -X POST -H "Content-Type: application/json" \
    -d '{"claimId":"12345","issueDescription":"Question about coverage","customerName":"John Doe","policyNumber":"POL-001"}' \
    http://localhost:8080/api/helpdesk-call/helpUser
  ```

## 💾 Database Schema

### Core Tables

#### claim_ai_result
Stores AI extraction results from invoices:
```sql
CREATE TABLE claim_ai_result (
  id SERIAL PRIMARY KEY,
  patient_name VARCHAR(100),
  policy_number VARCHAR(50),
  hospital_name VARCHAR(150),
  invoice_number VARCHAR(50),
  total_amount DECIMAL(10,2),
  currency VARCHAR(10),
  confidence_score DECIMAL(3,2),
  ai_status VARCHAR(20),
  ai_output JSONB,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### claim_decisions
Records claim adjudication decisions:
```sql
CREATE TABLE claim_decisions (
  id BIGSERIAL PRIMARY KEY,
  claim_id BIGINT NOT NULL,
  decision VARCHAR(30) NOT NULL,
  payable_amount DECIMAL(10,2),
  reasons JSONB,
  letter TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### claim_decision_evidence
Tracks evidence used in claim decisions:
```sql
CREATE TABLE claim_decision_evidence (
  id BIGSERIAL PRIMARY KEY,
  decision_id BIGINT NOT NULL REFERENCES claim_decisions(id),
  chunk_text TEXT NOT NULL,
  score DECIMAL(10,6),
  created_at TIMESTAMPTZ DEFAULT NOW()
);
```

#### policy_chunks (Vector Store)
Stores policy document embeddings:
```sql
CREATE TABLE policy_chunks (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  content TEXT,
  metadata JSONB,
  embedding vector(1536) -- OpenAI embedding dimension
);
```

## 🎯 Usage Examples

### Processing a Claim
1. **Upload Policy**: Use the "Ingest Policy" button to upload policy documents
2. **Submit Claim**: Upload invoice PDF or enter text manually
3. **Review Decision**: View AI-generated claim decision with itemized analysis
4. **Get Help**: Use the help desk for questions about the decision

### Using the Help Desk
1. **Open Chat**: Click "AI Help Desk" button
2. **Fill Details**: Enter customer name, claim ID, and policy number
3. **Ask Question**: Submit your question about coverage, claims, or policies
4. **Real-time Response**: Watch the AI response stream in real-time
5. **Conversation History**: Previous messages are maintained in the chat

### API Integration
```javascript
// Example: Submit help desk query with streaming response
const response = await fetch('/api/helpdesk-call/helpUser', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    claimId: '12345',
    issueDescription: 'Why was my claim partially denied?',
    customerName: 'Jane Smith',
    policyNumber: 'POL-001'
  })
});

// Handle streaming response
const reader = response.body.getReader();
const decoder = new TextDecoder();
let result = '';

while (true) {
  const { done, value } = await reader.read();
  if (done) break;
  
  const chunk = decoder.decode(value, { stream: true });
  result += chunk;
  console.log('Streaming response:', chunk);
}
```

## 🔧 Configuration

### Application Profiles
The application supports multiple profiles for different deployment environments:

- **local** (default): 
  - Uses OpenAI models with local PostgreSQL
  - MCP server communication via STDIO (standard input/output)
  - Server runs on port **8081**
  - Static UI files served from file system during development
  
- **docker**: 
  - Configured for containerized deployment
  - MCP server communication via HTTP (http://helpdesk-mcp:8082)
  - All services orchestrated via docker-compose
  - Server runs on port **8080** (inside container and host mapping)
  - Static UI files served from JAR classpath
  - Console logging enabled for docker logs visibility
  
- **production**: 
  - Optimized for production deployment
  - Enhanced security and performance settings

### Port Configuration

| Environment | Application Port | MCP Server | Database |
|-------------|------------------|------------|----------|
| **Local** | 8081 | STDIO (no port) | 5432 |
| **Docker** | 8080→8080 | 8082→8082 | 5432 |

**Why Different Ports?**
- Local uses **8081** to avoid conflicts with common services on 8080
- Docker uses **8080** (standard HTTP) for consistency with deployment expectations

## 🛠️ MCP Tools Integration

This application integrates with the **HelpDeskMCPServer** to provide AI agents with powerful tools for enhanced customer support.

### Available MCP Tools

#### HelpUser Tool
- **Purpose**: Retrieve claim decision details for customer inquiries
- **Input**: Claim ID
- **Output**: Complete claim decision with amounts, reasons, and evidence
- **Usage**: AI automatically invokes this tool when customers ask about specific claims

```java
@Tool(name = "helpUser", description = "Provide assistance to the user regarding their claim issue by reading the claim decision details")
public ClaimDecision helpUser(String claimId)
```

#### CreateTicket Tool
- **Purpose**: Create help desk tickets for unresolved customer issues
- **Input**: Customer name and issue description
- **Output**: Ticket ID for tracking
- **Usage**: Escalates complex issues to human support

```java
@Tool(name = "createTicket", description = "Create a help desk ticket for the user's issue or problem")
public String createTicket(String userName, String issueDescription)
```

#### GetAllTickets Tool
- **Purpose**: Retrieve customer's ticket history
- **Input**: Customer name
- **Output**: List of all tickets for the customer
- **Usage**: Provides context about previous support interactions

```java
@Tool(name = "getAllTickets", description = "Get all help desk tickets for the user")
public List<HelpDeskTicket> getAllTickets(String userName)
```

#### ReadUserPolicy Tool (RAG Integration)
- **Purpose**: Search and retrieve relevant policy content using vector similarity search
- **Input**: Policy number and customer name
- **Output**: Relevant policy text chunks matching the customer's policy
- **Usage**: AI can access specific policy details to answer coverage questions accurately

```java
@Tool(name = "readUserPolicy", description = "Read the user's policy document based on policy number and customer ID")
public String readUserPolicy(String policyNumber, String patientName)
```

**How the RAG Integration Works:**
1. **Vector Search**: Performs similarity search on the `policy_chunks` table using policy number and customer name
2. **Metadata Filtering**: Filters results to match the specific customer's policy using stored metadata
3. **Content Retrieval**: Returns the most relevant policy text chunks (top 3 matches)
4. **Caching**: Caches retrieved policy content to improve response times for subsequent queries

```java
// Example of the vector search in readUserPolicy tool
SearchRequest request = SearchRequest.builder()
    .query(policyNumber + " " + patientName.toUpperCase())
    .topK(10)
    .build();

List<Document> matches = vectorStore.similaritySearch(request);

// Filter by metadata to ensure correct policy/customer match
List<Document> filtered = matches.stream()
    .filter(doc -> policyNumber.equals(doc.getMetadata().get("policyNumber")) &&
                   patientName.toUpperCase().equals(doc.getMetadata().get("customerId")))
    .limit(3)
    .toList();
```

### MCP Server Architecture

The **HelpDeskMCPServer** runs as a companion service that provides specialized tools:

```
┌─────────────────────────────┐     ┌──────────────────────────────┐
│   AIPoweredClaimUnderwriter │────▶│      HelpDeskMCPServer       │
│                             │     │                              │
│  • Spring AI MCP Client     │     │  • MCP Tools Implementation  │
│  • ChatClient with Tools    │     │  • Database Access           │
│  • Streaming Chat API       │     │  • Vector Search             │
└─────────────────────────────┘     └──────────────────────────────┘
                │                                    │
                ▼                                    ▼
        ┌──────────────────┐              ┌──────────────────┐
        │   PostgreSQL     │              │   Vector Store   │
        │                  │              │                  │
        │ • Claim Data     │              │ • Policy Chunks  │
        │ • Decisions      │              │ • Embeddings     │
        │ • Evidence       │              │ • Similarity     │
        └──────────────────┘              └──────────────────┘
```

### How MCP Tools Enhance AI Responses

1. **Contextual Understanding**: When a customer asks about claim "12345", the AI automatically uses the `helpUser` tool to fetch relevant claim details

2. **Intelligent Escalation**: If the AI cannot resolve an issue, it uses `createTicket` to escalate to human support

3. **Historical Context**: The `getAllTickets` tool provides conversation context from previous interactions

4. **Policy Knowledge Access**: The `readUserPolicy` tool performs vector searches on policy documents to retrieve relevant coverage information specific to the customer's policy, enabling accurate answers to coverage questions

### Running the MCP Server

The HelpDeskMCPServer communicates differently based on the deployment mode:

#### Local Development (STDIO Mode)
In local mode, the MCP server communicates via standard input/output (STDIO):

```bash
# Terminal 1: Start the MCP Server with 'local' profile (default)
cd HelpDeskMCPServer
./mvnw spring-boot:run
# Note: Logging goes to /tmp/helpdesk-mcp-server.log (not console)
# This keeps STDOUT clean for MCP JSON protocol messages

# Terminal 2: Start the main application
cd AIPoweredClaimUnderwriter
export MCP_CLIENT_ENABLED=true
./mvnw spring-boot:run
```

**Important**: In local mode, the MCP server **cannot** log to console because it uses STDOUT for JSON message passing. All logs go to `/tmp/helpdesk-mcp-server.log`.

#### Docker Deployment (HTTP Mode)
In Docker mode, the MCP server communicates via HTTP:

```bash
# Start all services with docker-compose
cd AIPoweredClaimUnderwriter
docker-compose up -d

# MCP communication happens over HTTP at http://helpdesk-mcp:8082
# Console logging is enabled since STDOUT is not used for protocol messages
```

**Why Different Modes?**
- **STDIO (local)**: Direct process communication, faster, simpler for development
- **HTTP (docker)**: Network-based, works across containers, easier monitoring

**Repository Links:**
- **Main Application**: [https://github.com/nitdede/AIPoweredClaimUnderwriter](https://github.com/nitdede/AIPoweredClaimUnderwriter)
- **MCP Server**: [https://github.com/nitdede/HelpDeskMCPServer](https://github.com/nitdede/HelpDeskMCPServer)

### MCP Tool Configuration

Tools are automatically registered with the Spring AI ChatClient:

```java
@Bean(name = "helpDeskClient")
public ChatClient helpDeskClient(ChatClient.Builder clientBuilder, 
                                ChatMemory memory, 
                                ToolCallbackProvider toolCallbackProvider) {
    return clientBuilder
            .defaultToolCallbacks(toolCallbackProvider)  // MCP tools here
            .defaultSystem(systemPromptTemplate)
            .defaultAdvisors(List.of(
                new SimpleLoggerAdvisor(), 
                MessageChatMemoryAdvisor.builder(memory).build()
            ))
            .build();
}
```

### AI Model Configuration
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o
          temperature: 0.7
      embedding:
        options:
          model: text-embedding-3-small
```

### Vector Store Configuration
```yaml
spring:
  ai:
    vectorstore:
      pgvector:
        table-name: policy_chunks
        dimensions: 1536
        initialize-schema: true
        remove-existing-vector-store-table: false
```

### MCP Configuration
```yaml
spring:
  ai:
    mcp:
      client:
        enabled: ${MCP_CLIENT_ENABLED:true}
        type: SYNC
        toolcallback:
          enabled: true
```

## 🐳 Docker Deployment

### Architecture
The Docker deployment uses a three-container architecture with health checks and proper startup sequencing:

```yaml
┌─────────────────┐
│  app:8080       │  ← Main application (waits for postgres + helpdesk-mcp)
└─────────────────┘
        ↓ depends_on: service_healthy
┌─────────────────┐     ┌─────────────────┐
│ helpdesk-mcp    │     │  postgres:5432  │
│ :8082           │     │  (pgvector)     │
└─────────────────┘     └─────────────────┘
```

### Health Checks
Each service includes a health check to ensure proper startup order:

- **postgres**: `pg_isready` check every 10s
- **helpdesk-mcp**: HTTP check to `/mcp` endpoint every 10s (30s start period)
- **app**: HTTP check to `/actuator/health` every 30s

### Environment Variables
Create a `.env` file in the project root:

```bash
# Database Configuration
POSTGRES_USER=your_username
POSTGRES_PASSWORD=your_password
POSTGRES_DB=insurance_ai

# OpenAI API Key
OPENAI_API_KEY=your_openai_api_key
```

### Using Docker Compose
```bash
# Start all services with automatic health checking
docker-compose up -d

# View logs from all services
docker-compose logs -f

# View logs from specific service
docker-compose logs -f app
docker-compose logs -f helpdesk-mcp

# Check service health status
docker-compose ps

# Stop services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

### Key Docker Features

#### Service Dependencies
The `app` service waits for both `postgres` and `helpdesk-mcp` to be healthy before starting:

```yaml
app:
  depends_on:
    postgres:
      condition: service_healthy
    helpdesk-mcp:
      condition: service_healthy
```

#### Profile-Based Configuration
The Docker profile enables different behavior:
- Uses `application-docker.yml` configuration
- MCP communication via HTTP instead of STDIO
- Console logging enabled (visible in docker logs)
- Static resources served from JAR classpath

### Manual Docker Build
```bash
# Build application image
docker build -t ai-claim-underwriter .

# Run with environment variables
docker run -p 8081:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e OPENAI_API_KEY=your_key \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/insurance_ai \
  ai-claim-underwriter
```

### Accessing Services

#### Local Development
- **Main Application**: http://localhost:8081
- **MCP Server**: Communicates via STDIO (no HTTP endpoint)
- **PostgreSQL**: localhost:5432

#### Docker Deployment
- **Main Application**: http://localhost:8080
- **MCP Server**: http://localhost:8082 (health: http://localhost:8082/mcp)
- **PostgreSQL**: localhost:5432

## 🧪 Testing

### Run Unit Tests
```bash
./mvnw test
```

### Run Integration Tests
```bash
./mvnw verify
```

### Test Coverage
```bash
./mvnw jacoco:report
open target/site/jacoco/index.html
```

## 📊 Monitoring & Observability

### Health Checks
- **Application Health**: `/actuator/health`
- **Database Connectivity**: Included in health endpoint
- **AI Model Status**: Monitored through Spring AI actuator endpoints

### Logging
- **Application Logs**: Configurable via `logback-spring.xml`
- **AI Operations**: Detailed logging of AI model interactions
- **Performance Metrics**: Request/response times and throughput

## 🔒 Security Considerations

- **API Key Management**: Store OpenAI API keys securely using environment variables
- **Database Security**: Use connection encryption and proper authentication
- **Input Validation**: All user inputs are validated and sanitized
- **Error Handling**: Sensitive information is not exposed in error responses

## 🚧 Future Enhancements

- [ ] **Multi-model Support**: Add support for other AI providers (Anthropic, Azure OpenAI)
- [ ] **Advanced Analytics**: Dashboard for claim processing statistics
- [ ] **Workflow Management**: Complex approval workflows for claims
- [ ] **Mobile App**: Native mobile application for field agents
- [ ] **Integration APIs**: Connect with external insurance systems
- [ ] **Advanced Security**: Implement OAuth2/OIDC authentication

## 🔧 Troubleshooting

### Frontend Issues

#### File Upload/Text Input Buttons Not Working
**Problem**: Clicking file upload or text input buttons shows no response.

**Common Causes & Solutions**:
1. **TypeScript Compilation Issues**:
   ```bash
   # Ensure all TypeScript files are included in compilation
   npm run build:ui
   ```
   
2. **Module Loading Problems**:
   - Verify `index.html` has `type="module"` in script tag:
   ```html
   <script type="module" src="/js/app.js?v=3"></script>
   ```
   
3. **Missing Interface Imports**:
   - Check `tsconfig.json` includes all `.ts` files:
   ```json
   "include": ["src/main/ui/assets/js/*.ts"]
   ```

4. **Event Listeners Not Initialized**:
   - Ensure `initializeClaimInputMethod()` and `initializeClaimFileUpload()` are called
   - Check browser console for JavaScript errors

#### TypeScript Compilation Errors
**Problem**: Changes to TypeScript files don't reflect in the browser.

**Solution**:
```bash
# Clean and rebuild
rm src/main/ui/assets/js/*.js
npm run build:ui

# Verify files were compiled
ls -la src/main/ui/assets/js/
```

### Backend Issues

#### Application Won't Start
**Problem**: Spring Boot fails to start with exit code 1.

**Common Solutions**:
1. **Check Java Version**:
   ```bash
   java -version  # Should be 21+
   ```

2. **Database Connection**:
   - Ensure PostgreSQL is running
   - Verify database credentials in `application.yml`
   - Check pgVector extension is installed

3. **Environment Variables**:
   ```bash
   export OPENAI_API_KEY=your_key_here
   export MCP_CLIENT_ENABLED=true
   ```

4. **Clean Build**:
   ```bash
   ./mvnw clean package
   ```

#### MCP Tools Not Working
**Problem**: Help desk AI doesn't have access to tools.

**Solutions**:
1. **Start MCP Server First**:
   ```bash
   cd HelpDeskMCPServer
   ./mvnw spring-boot:run
   ```

2. **Enable MCP Client**:
   ```bash
   export MCP_CLIENT_ENABLED=true
   ```

3. **Check Tool Registration**:
   - Look for "MCP tools registered" in application logs
   - Verify `ToolCallbackProvider` bean is created

### Database Issues

#### Vector Extension Missing
**Problem**: `vector` extension not found.

**Solution**:
```sql
-- Connect to your database
psql insurance_ai

-- Install extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Verify installation
\dx
```

#### Policy Embeddings Not Working
**Problem**: Policy search returns no results.

**Solutions**:
1. **Check Vector Store Table**:
   ```sql
   SELECT COUNT(*) FROM policy_chunks;
   ```

2. **Verify Embeddings Model**:
   - Ensure `text-embedding-3-small` is accessible
   - Check OpenAI API key permissions

3. **Re-ingest Policies**:
   - Use "Ingest Policy" feature to re-upload documents

## 🤝 Contributing

### Main Application
1. Fork the [AIPoweredClaimUnderwriter](https://github.com/nitdede/AIPoweredClaimUnderwriter) repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

### MCP Server
1. Fork the [HelpDeskMCPServer](https://github.com/nitdede/HelpDeskMCPServer) repository
2. Follow the same contribution workflow as above

### Development Setup
```bash
# Clone both repositories
git clone https://github.com/nitdede/AIPoweredClaimUnderwriter.git
git clone https://github.com/nitdede/HelpDeskMCPServer.git

# Set up development environment
# Follow the Quick Start guide in each repository
```

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For questions or support:
- **Issues (Main App)**: [Create a GitHub issue](https://github.com/nitdede/AIPoweredClaimUnderwriter/issues)
- **Issues (MCP Server)**: [Create a GitHub issue](https://github.com/nitdede/HelpDeskMCPServer/issues)
- **Documentation**: Check the `/docs` folder in each repository for detailed guides
- **API Help**: Use the built-in help desk feature for AI-powered assistance
- **Source Code**: 
  - [AIPoweredClaimUnderwriter](https://github.com/nitdede/AIPoweredClaimUnderwriter)
  - [HelpDeskMCPServer](https://github.com/nitdede/HelpDeskMCPServer)

---

**Built with ❤️ using Spring Boot, Spring AI, and OpenAI GPT-4o**
Default datasource is PostgreSQL (`jdbc:postgresql://localhost:5432/insurance_ai`) configured in `application.yml`.

1) Table: `claim_ai_result`
   - id INTEGER PRIMARY KEY GENERATED
   - patient_name VARCHAR(100)
   - policy_number VARCHAR(50)
   - hospital_name VARCHAR(150)
   - invoice_number VARCHAR(50)
   - total_amount NUMERIC(10,2)
   - currency VARCHAR(10)
   - confidence_score NUMERIC(3,2)
   - ai_status VARCHAR(20)
   - ai_output JSON
   - created_at TIMESTAMP

2) Table: `claim_decisions`
   - id BIGINT PRIMARY KEY GENERATED
   - claim_id BIGINT NOT NULL
   - decision VARCHAR(30) NOT NULL
   - payable_amount NUMERIC(10,2)
   - reasons JSON
   - letter TEXT
   - created_at TIMESTAMP

3) Table: `claim_decision_evidence`
   - id BIGINT PRIMARY KEY GENERATED
   - decision_id BIGINT NOT NULL  -- references claim_decisions(id)
   - chunk_text TEXT NOT NULL
   - score NUMERIC(10,6)
   - created_at TIMESTAMPTZ DEFAULT now()

4) Vectorstore table configured by application.yml: `policy_chunks` (pgvector)

### Example SQL (Postgres)
```sql
CREATE TABLE claim_ai_result (
  id serial PRIMARY KEY,
  patient_name varchar(100),
  policy_number varchar(50),
  hospital_name varchar(150),
  invoice_number varchar(50),
  total_amount numeric(10,2),
  currency varchar(10),
  confidence_score numeric(3,2),
  ai_status varchar(20),
  ai_output jsonb,
  created_at timestamp
);

CREATE TABLE claim_decisions (
  id bigserial PRIMARY KEY,
  claim_id bigint NOT NULL,
  decision varchar(30) NOT NULL,
  payable_amount numeric(10,2),
  reasons jsonb,
  letter text,
  created_at timestamp
);

CREATE TABLE claim_decision_evidence (
  id bigserial PRIMARY KEY,
  decision_id bigint NOT NULL REFERENCES claim_decisions(id),
  chunk_text text NOT NULL,
  score numeric(10,6),
  created_at timestamptz DEFAULT now()
);
```

## Notes & Next Steps
- Adjust `spring.datasource` in `src/main/resources/application.yml` to match your DB credentials.
- If you want a CI workflow or GitHub Actions to build and run tests on push, I can add it.

---
Generated from project source; controller signatures are in `src/main/java/com/ai/claim/underwriter/controller`.
