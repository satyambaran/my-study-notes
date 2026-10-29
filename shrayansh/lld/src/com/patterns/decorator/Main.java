package new_notes.shrayansh.lld.src.com.decorator;

// varieties
class VeggieDelight extends BasePizza {
    @Override
    public int getCost() {
        return 200;
    }

    @Override
    public String getName() {
        return "Veggie Delight";
    }
}

class FarmHouse extends BasePizza {
    @Override
    public int getCost() {
        return 190;
    }

    @Override
    public String getName() {
        return "Farm House";
    }
}

class Margherita extends BasePizza {
    @Override
    public int getCost() {
        return 120;
    }

    @Override
    public String getName() {
        return "Margherita";
    }
}

// toppings
class ExtraCheese extends BasePizza {
    private BasePizza pizza;

    ExtraCheese(BasePizza pizza) {
        this.pizza = pizza;
    }

    @Override
    public int getCost() {
        return pizza.getCost() + 30;
    }

    @Override
    public String getName() {
        return "Extra Cheese " + pizza.getName();
    }
}

class Mushroom extends BasePizza {
    private BasePizza pizza;

    Mushroom(BasePizza pizza) {
        this.pizza = pizza;
    }

    @Override
    public int getCost() {
        return pizza.getCost() + 10;
    }

    @Override
    public String getName() {
        return "Mushroom " + pizza.getName();
    }
}

public class Main {
    public static void main(String[] args) {
        BasePizza pizza = new Margherita();
        pizza = new ExtraCheese(pizza);
        System.out.println(pizza.getCost());
        System.out.println(pizza.getName());
        System.out.println(pizza.getClass());
    }
}
