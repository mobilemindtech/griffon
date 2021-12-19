package ${package};

import griffon.core.artifact.GriffonController;
import griffon.core.controller.ControllerAction;
import griffon.inject.MVCMember;
import griffon.metadata.ArtifactProviderFor;
import org.codehaus.griffon.runtime.core.artifact.AbstractGriffonController;

import griffon.transform.Threading;
import griffon.annotations.core.Nonnull;

@ArtifactProviderFor(GriffonController.class)
public class _APPController extends AbstractGriffonController {
    private _APPModel model;

    @MVCMember
    public void setModel(@Nonnull _APPModel model) {
        this.model = model;
    }

    @ControllerAction
    @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
    public void click() {
        model.setClickCount(model.getClickCount() + 1);
    }
}