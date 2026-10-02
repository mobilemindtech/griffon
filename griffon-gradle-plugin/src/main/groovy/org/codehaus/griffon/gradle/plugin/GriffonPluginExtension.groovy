package org.codehaus.griffon.gradle.plugin

import org.gradle.api.provider.Property

interface GriffonPluginExtension {
    Property<String> getGroovyVersion()
    Property<String> getGriffonVersion()
    Property<String> getReload4jVersion()
    Property<String> getSlf4jVersion()

    /** Bytecode (release) gerado para o app. Padrão: 17. */
    Property<Integer> getJavaVersion()

    /** JDK usado só para compilar (ex.: 27). Vazio = JVM que roda o Gradle. */
    Property<Integer> getCompilerToolchainVersion()

    /** Envolve log.xxx(..) em if (log.isXxxEnabled()). Padrão: true. */
    Property<Boolean> getLoggingInjection()

    /** Executa métodos/closures de controller fora da UI thread (comportamento da CLI). Padrão: false. */
    Property<Boolean> getThreadingInjection()
}
