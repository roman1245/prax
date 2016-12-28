package xyz.kandrac.library.mvp.presenter;

/**
 * Cornerstone for Presenter part of MVP architecture. Interface is inspired by
 * <a href="https://github.com/grandstaish/hello-mvp-dagger-2/blob/master/app/src/main/java/com/example/bradcampbell/presentation/Presenter.java">
 * Android architecture guidelines.</a>
 * <p>
 * In order to properly use {@code Presenter} interface, simply follow these steps
 * <ol>
 * <li>Prepare your {@code View} - best way how to do it is to prepare interface with method
 * definitions that will be directly used by {@code View}</li>
 * <li>Prepare your {@code Presenter} implementation as follows {@code MyPresenter implements
 * Presenter<MyViewInterface>}</li>
 * <li>Implement your {@code View} target (Activity) with injected {@code Presenter}</li>
 * </ol>
 * </p>
 * Created by jan on 28.10.2016.
 */
interface Presenter<T> {

    /**
     * Sets the view for this presenter. In MVP architecture View is defined as
     * <p>
     * <blockquote cite="http://www.tinmegali.com/en/model-view-presenter-android-part-1">
     * The View, usually implemented by an Activity, will contain a reference to the presenter.
     * The only thing that the view will do is to call a method from the Presenter every time there
     * is an interface action.
     * </blockquote>
     * <p>
     * That means the all the interaction with View that is being set (mostly Activity), will be
     * handled by this Presenter
     *
     * @param view to be set
     */
    void setView(T view);
}
