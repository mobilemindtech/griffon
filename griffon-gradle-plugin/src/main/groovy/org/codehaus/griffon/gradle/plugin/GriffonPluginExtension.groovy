package org.codehaus.griffon.gradle.plugin

import org.gradle.api.provider.Property

interface GriffonPluginExtension {
    Property<String> getGroovyVersion()
    Property<String> getGriffonVersion()
    Property<String> getLog4jVersion()
    Property<String> getSlf4jReload4jVersion()
    Property<String> getReload4jVersion()
    Property<String> getSlf4jVersion()
    Property<Integer> getJavaVersion()

    /** log.xxx(..) envolvido em if (log.isXxxEnabled()), como a CLI do Griffon. Padrão: true. */
    Property<Boolean> getLoggingInjection()

    /** Métodos/closures de controller executados fora da UI thread, como a CLI do Griffon. Padrão: true. */
    Property<Boolean> getThreadingInjection()
}
