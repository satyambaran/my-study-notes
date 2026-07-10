#include <bits/stdc++.h>
using namespace std;
class A {
private:
    int x;

public:
    friend class B;
};
class B {
public:
    void printA(A a) {
        cout << a.x << endl; //? can access private variable of A
    }
};

class Animal {
public:
    virtual void sound() const {
        std::cout << "Animal makes a sound" << std::endl;
    }
};

class Dog : public Animal {
public:
    void sound() {
        std::cout << "Dog barks" << std::endl;
    }
};
class Dhruv : public Dog {
public:
    void sound() {
        std::cout << "German Shephard barks" << std::endl;
    }
};
class Cat : public Animal {
public:
    void sound() const override { //? this is what virtual is about
        std::cout << "Cat meows" << std::endl;
    }
};
class Tiger : public Animal {
public:
};

void makeSound(const Animal& animal) {
    animal.sound();  // Calls the appropriate sound() method based on the actual object type
}

int main() {
    Dog dog;
    Cat cat;
    Dhruv dhruv;
    Tiger tiger;
    //! override is only possible with virtual funtion
    dog.sound();     //? Output: Dog barks
    makeSound(dog);  //? Output: Animal makes a sound (since we didn't use override, so defaulted to the first declaration)
    cat.sound();
    makeSound(cat);  //  Output: Cat meows
    dhruv.sound();
    makeSound(dhruv);  //? Output: Animal makes a sound (since we didn't use override, so defaulted to the first declarations)
    tiger.sound();
    makeSound(tiger);

    A a;
    // a.x = 10;
    B b;
    b.printA(a);
    return 0;
}