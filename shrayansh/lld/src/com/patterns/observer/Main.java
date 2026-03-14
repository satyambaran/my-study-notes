package new_notes.shrayansh.lld.src.com.observer;

import java.util.*;
import com.observer.Observable.Observable;
import com.observer.Observer.Observer;

// import java.util.*;
class CricketScore implements Observable {
    private List<Observer> observers = new ArrayList<>();
    private int score;

    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }

    @Override
    public void setData(Number data) {
        score = (int) data;
        notifyObservers();
    }

    @Override
    public Number getData() {
        return this.score;
    }

    // public int getScore() {
    // return score;
    // }

    // public void setScore(int score) {
    // this.score = score;
    // notifyObservers();
    // }

}

class Weather implements Observable {
    private List<Observer> observers = new ArrayList<>();
    private float temperature;

    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }

    @Override
    public void setData(Number data) {
        temperature = (float) data;
        notifyObservers();
    }

    @Override
    public Number getData() {
        return this.temperature;
    }

    // public float getTemperature() {
    // return temperature;
    // }

    // public void setTemperature(float temperature) {
    // this.temperature = temperature;
    // notifyObservers();
    // }

}

class StockPrice implements Observable {
    private List<Observer> observers = new ArrayList<>();
    private double price;

    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }

    @Override
    public void setData(Number data) {
        price = (double) data;
        notifyObservers();
    }

    @Override
    public Number getData() {
        return this.price;
    }

    // public void setPrice(double price) {
    // this.price = price;
    // notifyObservers();
    // }

    // public double getPrice() {
    // return price;
    // }

}

class MobileObserver implements Observer {
    private Observable obj;
    
    public MobileObserver(Observable obj) {
        this.obj = obj;
        this.obj.registerObserver(this);
    }

    @Override
    public void update() {
        System.out.println("Mobile: " + obj.getClass().getName() + " is " + obj.getData());
    }
}

class TVObserver implements Observer {
    private Observable obj;

    public TVObserver(Observable obj) {
        this.obj = obj;
        this.obj.registerObserver(this);
    }

    @Override
    public void update() {
        System.out.println("TV: " + obj.getClass().getName() + " is " + obj.getData());
    }
}

public class Main {
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        System.out.println("Hello world.");

        CricketScore cricketScore = new CricketScore();
        Weather weather = new Weather();
        StockPrice stockPrice = new StockPrice();

        MobileObserver mobileObserverCricket = new MobileObserver(cricketScore);
        MobileObserver mobileObserverWeather = new MobileObserver(weather);
        MobileObserver mobileObserverStockPrice = new MobileObserver(stockPrice);

        TVObserver tvObserverCricket = new TVObserver(cricketScore);
        TVObserver tvObserverWeather = new TVObserver(weather);
        TVObserver tvObserverStockPrice = new TVObserver(stockPrice);

        cricketScore.setData(300);
        weather.setData(28.5f);
        stockPrice.setData(1250.75);
    }
}