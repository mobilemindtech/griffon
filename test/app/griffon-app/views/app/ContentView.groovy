package app

import org.codehaus.griffon.runtime.core.AbstractGriffonViewScript
import groovy.transform.BaseScript

//@BaseScript
//AbstractGriffonViewScript baseScript

panel() {
    // Elemento aninhado profundamente
    button('Clique aqui', id: 'meuBotaoMagico', action: doAction)
    button('Clique aqui', id: 'meuBotaoMagico', action: hideAction)
}