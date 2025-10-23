import griffon.core.GriffonApplication;
import org.codehaus.griffon.runtime.core.AbstractLifecycleHandler;

import griffon.annotations.core.Nonnull;
import jakarta.inject.Inject;

public class Initialize extends AbstractLifecycleHandler {
    @Inject
    public Initialize(@Nonnull GriffonApplication application) {
        super(application);
    }

    @Override
    public void execute() {
    }
}