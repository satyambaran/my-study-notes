package new_notes.shrayansh.lld.src.com.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

enum ShapeType {
    CIRCLE,
    RECTANGLE,
    SQUARE
}

class ShapeFactory {
    Shape getShape(ShapeType shapeType) {
        if (shapeType == null) {
            return null;
        }
        switch (shapeType) {
            case CIRCLE:
                return new Circle();
            case RECTANGLE:
                return new Rectangle();
            case SQUARE:
                return new Square();
            default:
                return null;
        }
    }
}

class Circle implements Shape {
    @Override
    public void draw() {
        System.out.println(this.getClass().getName());
    }
}

class Rectangle implements Shape {

    @Override
    public void draw() {
        System.out.println(this.getClass().getName());
        // System.out.println("Rectangle");
    }

}

class Square implements Shape {

    @Override
    public void draw() {
        System.out.println(this.getClass().getName());
    }

}

public class Main {
    public static void main(String[] args) {
        ShapeFactory factory = new ShapeFactory();
        List<Shape> shapes = new ArrayList<>(Arrays.asList(
                factory.getShape(ShapeType.CIRCLE),
                factory.getShape(ShapeType.RECTANGLE),
                factory.getShape(ShapeType.SQUARE)));
        for (Shape shape : shapes) {
            shape.draw();
        }
    }
}
