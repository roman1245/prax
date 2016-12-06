package xyz.kandrac.library.mvpresenter;

import javax.inject.Inject;

import xyz.kandrac.library.mviewp.MainView;
import xyz.kandrac.library.utils.SharedPreferencesManager;

/**
 * Created by jan on 6.12.2016.
 */

public class MainPresenter implements Presenter<MainView> {

    @Inject
    SharedPreferencesManager manager;

    private MainView view;

    @Inject
    MainPresenter() {
    }

    @Override
    public void setView(MainView view) {
        this.view = view;
    }
}
