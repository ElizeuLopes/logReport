<img width="870" height="386" alt="image" src="https://github.com/user-attachments/assets/4e4c4a0b-af6c-474b-a14a-729683eead82" />

<img width="743" height="513" alt="image" src="https://github.com/user-attachments/assets/2d0b52f9-331d-4375-a82c-852ff9811352" />

<img width="590" height="568" alt="image" src="https://github.com/user-attachments/assets/5ae21d3d-f169-4334-9510-cd2c5a003a38" />

<img width="2566" height="2412" alt="how_it_works" src="https://github.com/user-attachments/assets/6b1105a4-a9f7-4acd-acb4-8a06487ec59a" />

Componentes Principais

    Agent Core:

        Inicialização e lifecycle management

        Configuração de ambiente

        Gerenciamento de threads

    Instrumentation Engine:

        Byte Buddy para manipulação de bytecode

        Interceptores específicos por tecnologia

    Data Collection:

        Captura de requests/responses

        Coleta de metadados de ambiente

        Health checks periódicos

    Dispatcher:

        Envio assíncrono via OkHttp

        Thread pool controlado

        Serialização com Gson

    Payload Management:

        Limitação de tamanho (configurável)

        Estrutura de dados otimizada

Configurações no dockerfile 

FROM eclipse-temurin:17-jdk-alpine

# Download do agent do repositório
ADD "https://artifactory.minhaempresa.com/logreport-agent-1.0.0.jar" /app/agent.jar

# Configurações da aplicação
COPY minha-aplicacao.jar /app/app.jar

# Configurações do agent
ENV SERVICE_NAME=pedidos-service
ENV METRIC_ENDPOINT=http://log-collector.prod/logs
ENV HEALTH_URL=http://localhost:8080/health
ENV METRIC_THREADS=20
ENV MAX_PAYLOAD_SIZE=30720

# Kubernetes settings (auto-preenchidas)
ENV POD_NAME=pedidos-service-xyz
ENV HOSTNAME=pedidos-container-abc
ENV NODE_NAME=worker-node-02
ENV POD_NAMESPACE=production

CMD java -javaagent:/app/agent.jar -jar /app/app.jar
