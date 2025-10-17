package new_notes.shrayansh.lld.src.com.observer.Observable;

import com.observer.Observer.Observer;

public interface Observable {
    /*
     * ~ all methods of an interface needs to be a public method in concrete class
     * ~ Cannot reduce the visibility of the inherited method from
     * ObservableJava(67109273)
     */

    void registerObserver(Observer observer);

    void removeObserver(Observer observer);

    void notifyObservers();

    void setData(Number num);
    Number getData();
}