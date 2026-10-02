griffon-mini
============

Fork enxuto do [Griffon](https://github.com/griffon/griffon) 1.x mantido pela mobilemind para
rodar apps Griffon 1 (Swing) em Java moderno, Groovy 6 e Gradle — sem a CLI, os scripts Gant,
o shell, o wrapper, o guia e as dependências de build (Spring, Ivy, Ant, Grails docs...).

Módulos
-------

| Módulo                  | Artefato                                   | Conteúdo |
|-------------------------|--------------------------------------------|----------|
| `griffon-rt`            | `org.codehaus.griffon:griffon-rt`          | runtime, toolkit Swing (antigo `griffon-swing-runtime`) e AST transformations de compilação (antes no `griffon-cli`) |
| `plugins`               | `org.codehaus.griffon:griffon-plugins`     | plugins Griffon 1.x recompilados com Groovy 6: coverflow, datasource, glazedlists, gsql, miglayout, splash e jidebuilder |
| `griffon-test`          | `org.codehaus.griffon:griffon-test`        | suporte mínimo a testes: `@TestFor`, `GriffonUnitTestCase`, mocks |
| `griffon-gradle-plugin` | `org.codehaus.griffon.gradle.plugin`       | plugin Gradle que configura um app Griffon 1 |

Java
----

O fork é compilado com o toolchain **Zulu JDK 27** (`javaToolchainVersion`), mas gera bytecode
**Java 17** (`javaRelease`). O gym-desktop precisa de JVM 32 bits por causa dos SDKs nativos e o
port Windows x86 foi removido no JDK 24 (JEP 479), então o runtime continua sendo a Zulu 17 x86.

Build
-----

    ./gradlew build publishToMavenLocal     # ou: dev install
    dev run-test                            # smoke test com test/app

Usando no app
-------------

```groovy
plugins {
    id 'org.codehaus.griffon.gradle.plugin' version '1.7.0-SNAPSHOT'
}

griffonConfig {
    //javaVersion = 17                // bytecode gerado (padrão 17)
    //compilerToolchainVersion = 27   // JDK só para compilar; 'run' continua na JVM do Gradle
    //loggingInjection = true         // log.debug(x) -> if (log.debugEnabled) log.debug(x)
    //threadingInjection = false      // injeção de threading da CLI em métodos de controller
    //groovyVersion = '6.0.0'
}
```

O plugin:

* injeta `griffon-rt`, Groovy, slf4j + reload4j e, para testes, `griffon-test`;
* configura o layout `griffon-app/*`, `src/main/*` e `test/unit`, `test/integration`;
* substitui as coordenadas antigas dos plugins (`org.codehaus.griffon.plugins:griffon-*-runtime`,
  `org.codehaus.griffon:jidebuilder`) por `griffon-plugins`, e usa essas declarações para gerar
  `META-INF/griffon-addons.properties` na mesma ordem;
* exclui artefatos que não devem mais aparecer no classpath (`griffon-swing-runtime`, `griffon-cli`,
  `log4j:log4j`...);
* gera `application.properties`, `griffon-artifacts.properties` e o fat jar (`shadowJar`).

Veja [NOTAS-MIGRACAO.md](NOTAS-MIGRACAO.md) para decisões de compatibilidade, pontos de
desempenho/segurança e pendências.

Licença: Apache 2.0 (ver [LICENSE](LICENSE)).
