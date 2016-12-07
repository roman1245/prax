package xyz.kandrac.library.dagger.component;

import javax.inject.Singleton;

import dagger.Component;
import xyz.kandrac.library.dagger.module.ApplicationModule;
import xyz.kandrac.library.mvpresenter.MainPresenter;

/**
 * Created by jan on 28.10.2016.
 */
@Component(modules = ApplicationModule.class)
@Singleton
public interface ApplicationComponent {

    void inject(MainPresenter presenter);
}
