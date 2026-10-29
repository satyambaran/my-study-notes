package new_notes.shrayansh.lld.src.com.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class StrategyOne implements Strategy {
    public String name;

    StrategyOne(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }
}

class StrategyTwo implements Strategy {
    public String name;

    StrategyTwo(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }
}

class Helicopter implements FlyingMachine {
    public Strategy strategy;

    Helicopter(Strategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void fly() {
        System.out.println("Flying using " + strategy.getName());
    }
}

class MilitaryHelicopter implements FlyingMachine {
    public Strategy strategy;

    MilitaryHelicopter(Strategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void fly() {
        System.out.println("Flying using " + strategy.getName());
    }
}

class FighterJet implements FlyingMachine {
    public Strategy strategy;

    FighterJet(Strategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void fly() {
        System.out.println("Flying using " + strategy.getName());
    }
}

public class Main {
    public static void main(String[] args) {
        List<Strategy> strategyList = new ArrayList<>(Arrays.asList(new StrategyOne("One"), new StrategyOne("Two")));
        List<FlyingMachine> flyingMachines = new ArrayList<>(Arrays.asList(
                new Helicopter(strategyList.get(0)),
                new MilitaryHelicopter(strategyList.get(0)),
                new FighterJet(strategyList.get(1))));
        for (FlyingMachine flyingMachine : flyingMachines) {
            flyingMachine.fly();
        }
    }
}
