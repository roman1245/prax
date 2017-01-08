package xyz.kandrac.library.dagger.component;

import javax.inject.Singleton;

import dagger.Component;
import xyz.kandrac.library.mvp.view.bookdetail.BookDetailBasicFragment;
import xyz.kandrac.library.mvp.view.bookdetail.BookDetailOthersFragment;
import xyz.kandrac.library.dagger.module.ApplicationModule;
import xyz.kandrac.library.dagger.module.NetModule;
import xyz.kandrac.library.mvp.view.MainActivity;

/**
 * Every Dagger component is used to create dependency graph and have to define which
 * {@link dagger.Module} will be used in which class as dependency. This {@link Component} is
 * {@link Singleton} that defines injections to {@link MainActivity} class.
 */
@Singleton
@Component(modules = {ApplicationModule.class, NetModule.class})
public interface NetComponent {
    void inject(MainActivity activity);
    void inject(BookDetailOthersFragment fragment);
    void inject(BookDetailBasicFragment fragment);
}
