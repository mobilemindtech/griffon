# griffon-mini — notas de migração

Branch `griffon-mini` (fork) + branch `griffon-mini` no gym-desktop-next. Versão `1.7.0-SNAPSHOT`.

## O que mudou no fork

* Removidos: `griffon-cli`, `griffon-scripts`, `griffon-shell`, `griffon-wrapper`, `griffon-resources`,
  `griffon-guide`, `buildSrc`, instaladores (izpack/deb/rpm/dmg), `src/dist`, `src/sandbox`, demos.
  ~96k → ~45k LOC (já incluindo os plugins incorporados).
* `griffon-rt` passou a conter:
  * o antigo `plugins/griffon-swing-runtime` (o addon `swing` é sempre registrado pelo plugin Gradle);
  * as AST transformations que estavam no `griffon-cli` (`org.codehaus.griffon.ast`,
    `org.codehaus.griffon.compiler[.support]`, `griffon.transform`), sem Spring/BuildSettings.
    Opções de compilação agora são System properties (`griffon.disable.*.injection`,
    `compiler.threading.<Classe>.<acao>=false`).
* `plugins/` (`griffon-plugins`): fontes de coverflow, datasource, glazedlists, gsql, miglayout, splash
  (dos sources jars 1.x) e jidebuilder (tag `JIDEBUILDER_6.0` do github.com/griffon/jidebuilder).
  **Motivo:** o Groovy 6.0.0 final removeu `org.codehaus.groovy.runtime.callsite.CallSite/CallSiteArray`;
  os binários antigos davam `NoClassDefFoundError` ao carregar os addons. Ajustes: import de binding
  `org.codehaus.groovy.binding` → `org.apache.groovy.swing.binding` (jidebuilder); o
  `springbeans.groovy` do datasource foi descartado (só servia ao plugin spring).
* `griffon-test`: `@TestFor`, `GriffonUnitTestCase`, `GriffonMock`, mocks.
* `groovy-dateutil` (extensões `Date.format`/`Date.parse`) é dependência explícita do griffon-rt: o
  `groovy-all` 6.0.0 não o inclui mais, e antes ele só chegava ao app pelo POM antigo do griffon-rt.
* Dependências: Groovy 6.0.0, slf4j 2.0.20 (antes misturava 1.7.5 e 2.0.18), reload4j 1.2.26
  (`log4j:log4j:1.2.17` removido e excluído globalmente), sem commons-lang no runtime.
* Corrigido: os pools estáticos de `UIThreadManager`/`AbstractUIThreadHandler` morriam no primeiro
  `app.shutdown()` e nunca eram recriados — quebrava qualquer segunda instância de app na mesma JVM
  (todos os `@TestFor` depois do primeiro davam `RejectedExecutionException`).

## Decisões de compatibilidade (para o gym-desktop continuar igual)

* **Bytecode 17**, compilado com JDK 27. Sem JDK ≥ 24 para Windows x86.
* **Injeção de threading ligada por padrão** (`threadingInjection = true`), como na CLI antiga
  (v5): métodos `void`/`def` e closures de controller rodam via `UIThreadManager.executeOutside`.
  Os builds Gradle da v6 (1.6.0) perderam isso e as actions passaram a rodar na EDT — causa do
  timeout em `listObjects(users)` do Control iD (o cadastro na EDT esperava o aparelho, e o
  callback `new_user_identified` do aparelho esperava a EDT). Corrigido também na linha 1.6.x
  (versão 1.6.1). Métodos com tipo de retorno (ex.: `Boolean`) não são envolvidos; o retorno de
  métodos `def` envolvidos se perde (nenhum chamador do gym-desktop usa). Desligue com
  `griffonConfig.threadingInjection = false`.
* **Injeção de logging ligada** (padrão da CLI): `log.debug("... $x")` só monta a string se o nível
  estiver habilitado. Semântica idêntica.
* **Imports automáticos** (`griffon-default-imports.properties`/`DefaultImportCompilerCustomizer`)
  removidos: nunca foram aplicados no build Gradle.
* `SourceUnitCollector` continua vazio no Gradle (só a CLI o alimentava). Efeito: quando um artefato
  estende outra classe do mesmo projeto, a injeção vai para a subclasse — igual ao build atual.
  Dá para trocar por `ClassNode.isPrimaryClassNode()` se um dia quiser o comportamento da CLI.
* App: os plugins Griffon passaram a ser declarados em `griffonConfig.addons` (as coordenadas
  antigas, jidebuilder, svg-salamander e commons-dbcp/pool saíram do build.gradle do app).
* App: demais mudanças — versão do plugin, remoção das linhas `griffon-swing-runtime`/
  `*-compile`, `app.griffon.version`, `@TestFor(<Classe>)` nos stubs de teste e remoção do
  `InnerServiceTests` (a classe foi apagada em `0bd3689c`). O resto das coordenadas de plugin
  ainda é aceito e substituído pelo plugin Gradle (com aviso).

## Verificação feita

* Fork: `./gradlew build` (49 testes do griffon-rt ok); `test/app` sobe e encerra com smoke test
  (addon swing, AST em controller/model, `log`, janela), também com `compilerToolchainVersion = 27`
  e `threadingInjection = true` (bytecode 61, wrapper `UIThreadManager` injetado).
* gym-desktop: `clean shadowJar` ok em Zulu 17 x86; classpath comparado com o build anterior
  (só upgrades + remoções esperadas); `griffon-addons.properties` idêntico (exceto versão do swing);
  `jdeps --missing-deps` antes/depois sem novas ausências relevantes; todos os addons, o JideBuilder
  e `MainController`/`MainView`/`SyncService` carregam e instanciam na JVM 17 x86.
* **Não verificado:** subir o gym-desktop completo (sincroniza com backend/RabbitMQ/Firebase e fala
  com hardware). Fazer `admin run` numa máquina de teste antes de liberar.

## Desempenho

1. **`groovy-all` no runtime (maior ganho).** O 6.0.0 traz maven-resolver, ivy, jline, junit 6,
   jackson, javaparser, groovysh/console... para o fat jar (63 MB). Trocar por módulos explícitos
   (`groovy`, `-swing`, `-sql`, `-json`, `-xml`, `-servlet`, `-templates`, `-datetime`, `-dateutil`,
   `-nio`, `-http-builder`) reduz jar, tempo de carga de classes e o arquivo de CDS.
   Sugestão: opção `griffonConfig.groovyModules` no plugin.
2. **Compilação**: com `compilerToolchainVersion = 27` (x64) o compilador sai da JVM 32 bits e do
   teto de 1 GB (o plugin agora detecta JVM 32 bits e usa 1 GB automaticamente; o bloco
   "fix to jvm x86" do app ficou redundante).
3. `@CompileStatic` em serviços quentes (`SyncService`, `SqlService`, `CatracaService`): o CodeNarc
   já empurra para tipagem estática; com indy ligado o ganho maior é em loops de sincronização.
4. Pools: `UIThreadManager` e `AbstractUIThreadHandler` criam cada um um pool fixo com nº de CPUs.
   Poderiam compartilhar um único pool. Virtual threads/structured concurrency (e boa parte do
   `groovy.concurrent` que se apoia nelas) exigem JDK 21+, indisponível em 32 bits — por isso não
   foram usados.
5. AOT cache (JEP 483, `admin aot`) só existe no JDK 24+; na Zulu 17 x86 o equivalente é AppCDS
   (`-XX:ArchiveClassesAtExit`/`-XX:SharedArchiveFile`).
6. Remover dependências que só existem por causa do `http-builder` (`json-lib`, `httpmime`, ver segurança).

## Segurança

### App (gym-desktop)

1. **Crítico — liberação de catraca sem autenticação.** O servidor de Groovlets (`JettyService` →
   `GroovletServer`) escuta em todas as interfaces e `httpScripts/catraca.groovy?liberarGiroEntrada=1`
   libera o giro para qualquer máquina da rede. Mínimo: bind em `127.0.0.1` (ou allowlist de IPs da
   LAN) + token/HMAC compartilhado; idealmente POST. **Pendente.**
2. **Jetty 6.1.26** (2010, EOL, vários CVEs) — **removido**. Além dos CVEs, ele não funcionava mais: o
   `groovy-servlet` do Groovy 4+ exige Jakarta Servlet e o Jetty 6 só tem `javax.servlet` 2.5
   (`NoClassDefFoundError: jakarta/servlet/http/HttpServlet`, já com o Groovy 6.0.0-alpha-1). Os
   Groovlets agora rodam no `GroovletServer` (HttpServer do JDK + GroovyScriptEngine), com os mesmos
   endpoints, porta e configs.
3. **H2 1.4.191**: CVE-2021-42392 / CVE-2022-23221 (RCE via JNDI/console) e outros. A 2.x muda o
   formato do arquivo: migrar com export/`SCRIPT` + import, usando o `BackupService`.
4. **http-builder 0.8.0-SNAPSHOT** — **removido**. O app usa o helper `br.com.mobilemind.gym.http.Http`
   (`groovy.http` / `java.net.http`) e os comandos remotos usam `syncService.enviarEmail(...)`. Com
   ele saíram json-lib 2.4, commons-beanutils 1.8.0 (CVE-2014-0114, CVE-2019-10086),
   commons-collections 3.2.1 (gadget de desserialização, CVE-2015-6420), xercesImpl 2.11.0, nekohtml
   e httpmime 4.1.2. Comandos remotos antigos guardados no backend (com `import groovyx...` ou
   `prepareAndBuildEmailRequest`) não compilam mais: usar as versões de `scripts/comandos-remotos`.
5. **OkHttp 3.11 / logging-interceptor 3.9.1** — **removido** (HttpService, ControlIdService e
   FirebaseWatcher migrados para o mesmo helper).
6. **Execução remota de Groovy** (`scripts/comandos-remotos` via campo `eval`, `GroovyShell`/`Eval` em
   `DirectoryService` e `DeveloperToolsController`): é RCE por design — garantir origem
   autenticada (TLS + assinatura do script) antes de executar.
7. **Repositórios**: `mavenLocal()` primeiro e o m2 em `raw.githubusercontent.com` sem filtro de
   conteúdo permitem dependency confusion. Usar `content { includeGroup 'br.com.mobilemind.api' ... }`
   e `mavenCentral()` antes. Evitar dependências `-SNAPSHOT` em release (builds não reprodutíveis).
8. Já resolvido nesta migração: `log4j 1.2.17` (CVE-2019-17571, CVE-2021-4104, CVE-2022-23302/
   23305/23307) substituído por reload4j em todo o classpath; login com TLS validado, sem senha no
   log nem na URL (Basic auth + `username` na query, como o endpoint exige).

### Fork

* Saíram do classpath de compilação do app: Spring 3.2.7, Ivy 2.2, Ant, jsch 0.1.48, xerces 2.9,
  itext 2.0.8, Karaf, commons-collections 3.2.1 (vinham via `compileOnly griffon-cli`).
* As AST transformations globais agora estão no jar de runtime. São inertes fora do build porque
  dependem de `griffon.compiler.project.name`, o que também vale para os Groovlets e comandos remotos.

## Pendências

* Proteger o servidor de Groovlets (item 1 de segurança).
* Publicar `1.7.0-SNAPSHOT` (rt, plugins, test, gradle plugin) no m2 da mobilemind; hoje só existe
  no mavenLocal desta máquina.
* Atualizar no backend os comandos remotos guardados para as versões de `scripts/comandos-remotos`.
* H2 1.4 → 2.x (item 3).
* `admin run-app` (CLI Griffon) deixa de fazer sentido; usar `admin run`.
