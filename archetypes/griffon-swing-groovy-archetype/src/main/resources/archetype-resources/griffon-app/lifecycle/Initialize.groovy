import griffon.core.GriffonApplication
import org.codehaus.griffon.runtime.core.AbstractLifecycleHandler

import griffon.annotations.core.Nonnull
import jakarta.inject.Inject

import static griffon.core.util.GriffonApplicationUtils.isMacOSX
import static groovy.swing.SwingBuilder.lookAndFeel

class Initialize extends AbstractLifecycleHandler {
    @Inject
    Initialize(@Nonnull GriffonApplication application) {
        super(application)
    }

    @Override
    void execute() {
        lookAndFeel((isMacOSX ? 'system' : 'nimbus'), 'gtk', ['metal', [boldFonts: false]])
    }
}