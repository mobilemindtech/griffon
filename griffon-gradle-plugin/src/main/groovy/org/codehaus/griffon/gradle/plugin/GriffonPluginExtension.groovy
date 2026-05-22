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
}
