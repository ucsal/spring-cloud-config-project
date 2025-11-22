# spring-cloud-config-project

### # Projeto: Arquitetura de Configuração Centralizada com Spring Cloud Config

Este projeto implementa a arquitetura de **Configuração Externalizada e Centralizada** (Spring Cloud Config) em um contexto de **Microsserviços**. O sistema é modularizado em dois processos distintos que se comunicam via rede (Config Server e Account Service), demonstrando as práticas de segurança e agilidade necessárias para um ambiente de banco digital.

### Questões da Atividade (Análise e Justificativa)

#### Explique o propósito, funcionalidade do projeto e justifique sua necessidade no cenário do banco digital

O **propósito principal** deste projeto é estabelecer um ponto de controle único para todas as configurações operacionais da aplicação, removendo-as do código-fonte ou do JAR. A **funcionalidade** essencial é permitir que o **Config Server** atue como um *hub* central, fornecendo o conjunto correto de propriedades (ex: senhas, URLs, chaves) ao **Account Service** em tempo real, via HTTP.

**No cenário de um banco digital, isso é indispensável** e não negociável:

1.  **Segurança e Conformidade (Requisito D):** A arquitetura garante que dados sensíveis (senhas) sejam armazenados em formato **criptografado** (`{cipher}`) no `config-repo` e só sejam decifrados pelo Config Server no momento da entrega ao cliente. Isso protege as credenciais de produção de forma rigorosa.

2.  **Agilidade e Disponibilidade (Requisito C):** Permite que alterações críticas, como a **mudança de uma *feature flag*** ou **parâmetro de um serviço**, sejam aplicadas imediatamente através do **Refresh Dinâmico** (`/actuator/refresh`), minimizando o tempo de inatividade (downtime) sem a necessidade de reiniciar o serviço.


#### a) Explique o conceito de configuração externalizada e centralizada.

O conceito de **Configuração Externalizada** é o princípio de desacoplamento, onde as variáveis de ambiente e regras operacionais são removidas do artefato de *deploy* (o JAR). Já a **Configuração Centralizada** é a consolidação de todas essas variáveis em um único ponto de gestão (nosso Config Server), que se torna a "fonte da verdade" para todos os serviços consumidores. Essa unificação simplifica a auditoria e a manutenção de centenas de parâmetros distribuídos.

#### b) Por que é importante ter um Config Server em um sistema bancário com múltiplos ambientes (dev, homologação, produção)?

Em um ambiente bancário, a integridade dos dados e a segurança são a prioridade máxima. O Config Server é vital para:

1.  **Isolamento de Segurança (Requisito B):** Usando **perfis** (`dev`, `prod`), ele impede que o ambiente de desenvolvimento, que é mais maleável, acesse ou utilize, por engano, as credenciais e APIs do ambiente de produção, garantindo a separação total dos dados e acessos.

2.  **Consistência e Versionamento:** Como o servidor utiliza um backend de arquivos versionado (nosso `config-repo`), qualquer alteração de configuração é **rastreável e reversível**. Se uma mudança causar falhas em PROD, podemos reverter para a versão anterior imediatamente, o que é fundamental para o compliance.



## Validação e Demonstrações Técnicas

O projeto foi validado com sucesso, demonstrando a funcionalidade de microsserviços. Os comandos abaixo provam a implementação dos Requisitos C e D.

**(Pré-requisito):** Inicie o **Config Server (8888)** com a VM Option da chave de criptografia e o **Account Service (8080)** (que está configurado para o perfil `dev`).

### 1\. Prova de Refresh Dinâmico (Requisito C)

O **Account Service** deve atualizar a propriedade `message` em tempo real, sem restart.

  * **Ação Inicial:** Verifique o valor inicial da propriedade `message` no perfil `dev`.
    ```bash
    # 1. Verifique a mensagem inicial:
    curl http://localhost:8080/message
    ```
  * **Ação Manual:** Altere o valor da `message` no arquivo **`config-repo/account-service-dev.properties`**.
  * **Ação Final (Refresh):** Dispare o recarregamento do Bean `AccountController` e verifique a nova mensagem.
    ```bash
    # 2. Dispare o recarregamento (Refresh):
    curl -X POST http://localhost:8080/actuator/refresh

    # 3. Verifique a nova mensagem (Deve mudar sem restart):
    curl http://localhost:8080/message
    ```


### 2\. Prova de Criptografia e Decriptografia (Requisito D)

O servidor deve decifrar a senha armazenada no formato `{cipher}` e fornecê-la ao cliente.

  * **Validação da Criptografia (Servidor):** Prova que o mecanismo de segurança do servidor está ativo (requer `raw/text` no body).
    ```bash
    # 1. Validação do Mecanismo de Criptografia:
    # (Deve retornar a string criptografada)
    curl -X POST http://localhost:8888/encrypt -d "senha-do-banco-real"
    ```
  * **Verificação da Decriptografia (Client):** Altere o cliente para o perfil **`prod`** e **reinicie**. O endpoint `/db-password` injeta a senha que o servidor decifrou.
    ```bash
    # 2. Verifique a Decriptografia no Cliente (Após reiniciar o Account Service no perfil 'prod'):
    curl http://localhost:8080/db-password
    ```

**Saída Esperada:** `Status do Ambiente: PROD | Senha Decriptografada: senha-do-banco-real`
