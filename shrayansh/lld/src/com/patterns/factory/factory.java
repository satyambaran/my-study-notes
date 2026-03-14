// ============================================
// FACTORY METHOD PATTERN
// Creates ONE type of product
// ============================================

// Product
interface Button {
    void render();
}

class WindowsButton implements Button {
    public void render() {
        System.out.println("Rendering Windows Button");
    }
}

class MacButton implements Button {
    public void render() {
        System.out.println("Rendering Mac Button");
    }
}

// Creator with Factory Method
abstract class Dialog {
    // Factory Method
    public abstract Button createButton();
    
    public void renderDialog() {
        Button button = createButton();
        button.render();
    }
}

class WindowsDialog extends Dialog {
    public Button createButton() {
        return new WindowsButton();
    }
}

class MacDialog extends Dialog {
    public Button createButton() {
        return new MacButton();
    }
}

// ============================================
// ABSTRACT FACTORY PATTERN
// Creates FAMILIES of related products
// ============================================

// Product Family 1: Buttons
interface Button2 {
    void paint();
}

class WinButton implements Button2 {
    public void paint() {
        System.out.println("Windows Button");
    }
}

class MacButton2 implements Button2 {
    public void paint() {
        System.out.println("Mac Button");
    }
}

// Product Family 2: Checkboxes
interface Checkbox {
    void paint();
}

class WinCheckbox implements Checkbox {
    public void paint() {
        System.out.println("Windows Checkbox");
    }
}

class MacCheckbox implements Checkbox {
    public void paint() {
        System.out.println("Mac Checkbox");
    }
}

// Abstract Factory Interface
interface GUIFactory {
    Button2 createButton();
    Checkbox createCheckbox();
}

// Concrete Factory 1: Creates Windows family
class WindowsFactory implements GUIFactory {
    public Button2 createButton() {
        return new WinButton();
    }
    
    public Checkbox createCheckbox() {
        return new WinCheckbox();
    }
}

// Concrete Factory 2: Creates Mac family
class MacFactory implements GUIFactory {
    public Button2 createButton() {
        return new MacButton2();
    }
    
    public Checkbox createCheckbox() {
        return new MacCheckbox();
    }
}

// Client Code
class Application {
    private Button2 button;
    private Checkbox checkbox;
    
    // Application's responsibility is to use the factory and GET the buttons and checkbox, w/o knowing what it's getting
    // The factory decides which buttons/checbox to create based on it's nature/implementation
    public Application(GUIFactory factory) {
        button = factory.createButton();
        checkbox = factory.createCheckbox();
    }
    
    public void render() {
        button.paint();
        checkbox.paint();
    }
}

// ============================================
// DEMO
// ============================================
public class FactoryComparison {
    public static void main(String[] args) {
        System.out.println("=== FACTORY METHOD ===");
        System.out.println("Creates ONE product\n");
        
        Dialog dialog;
        
        String os = "Windows";
        if (os.equals("Windows")) {
            dialog = new WindowsDialog();
        } else {
            dialog = new MacDialog();
        }
        dialog.renderDialog();
        
        System.out.println("\n=== ABSTRACT FACTORY ===");
        System.out.println("Creates FAMILY of products\n");
        
        GUIFactory factory;
        
        if (os.equals("Windows")) {
            factory = new WindowsFactory();
        } else {
            factory = new MacFactory();
        }
        
        Application app = new Application(factory);
        app.render();
        
        System.out.println("\n=== KEY DIFFERENCE ===");
        System.out.println("Factory Method: One product at a time");
        System.out.println("Abstract Factory: Multiple related products together");
    }
}