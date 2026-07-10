#include <bits/stdc++.h>
using namespace std;

int global; /* Uninitialized variable stored in bss (Uninitialized Data Segment)
             */

int global2 =
    10; /* initialized global variable stored in DS (Initialized Data Segment)*/
typedef struct {
    int len;
    int arr[];
} fam;
struct Object {
    Object() { cout << "Created an Object\n"; }
    ~Object() { cout << "Destroyed an Object\n"; }
};
int main(void) {
    for (int i = 0; i < 3; i++) {
        Object o;  // both Created and Destroyed will get printed
        //= but if we dont add deconstructor then it'll not deconstruct it
        // autamatically
    }
    for (int i = 0; i < 3; i++) {
        Object* o = new Object();
        delete o;
    }
    static int i;       /* Uninitialized static variable stored in bss */
    static int j = 100; /* Initialized static variable stored in DS*/

    //!     malloc := (cast-type*) malloc(byte-size)    memory allocation
    int n = 5;
    int* ptr = (int*)malloc(n * sizeof(int));
    for (i = 0; i < n; ++i) ptr[i] = i + 1;
    for (int i = 0; i < n; i++) {
        ptr[i] = i + 1;
        *(ptr + i) = i * i;
    }
    // Since the size of int is 4 bytes, this statement will allocate 20 bytes
    // of memory. And, the pointer ptr holds the address of the first byte in
    // the allocated memory. If space is insufficient, allocation fails and
    // returns a NULL pointer.

    //!     calloc := (cast-type*)calloc(n, element-size);
    ptr = (int*)calloc(n, sizeof(int));
    // ptr = (float*) calloc(25, sizeof(float));
    //  This statement allocates contiguous space in memory for 25 elements each
    //  with the size of the float.
    //!     realloc

    ptr = (int*)realloc(ptr, (n + 3) * sizeof(int));
    for (i = 0; i < n + 3; ++i) printf("%d ", ptr[i]);
    printf("\n");
    ptr = (int*)realloc(ptr, sizeof(int) * (n - 3));
    for (i = 0; i < n - 3; ++i) printf("%d ", ptr[i]);
    printf("\n");
    // where ptr is reallocated with new size 'newSize'.
    // re-allocation of memory maintains the already present value and new
    // blocks will be initialized with the default garbage value.

    // malloc() is faster than calloc.
    // malloc() doesn’t initialize the allocated memory
    // allocates the memory and also initializes every byte in the allocated
    // memory to 0

    // 2d Array
    int r = 3, c = 4, count;
    int** arr = (int**)malloc(r * sizeof(int*));
    for (i = 0; i < r; i++) arr[i] = (int*)malloc(c * sizeof(int));

    count = 0;
    int len = sizeof(int*) * r + sizeof(int) * c * r;
    arr = (int**)malloc(len);

    fam* fam1 = (fam*)malloc(sizeof(fam*) + 5 * sizeof(int));
    for (int i = 0; i < 5; i++) {
        printf("%d, ", fam1->arr[i]);
    }
    printf("\n");

    //! memory allocated using functions malloc() and calloc() is not
    //! de-allocated on their own. Hence the free() method
    free(ptr);

    // memory leak occurs when programmers create a memory in a heap and forget
    // to delete it. When dynamically allocated memory is not freed up by
    // calling free then it leads to memory leak
    return 0;
}

// gcc memory_allocation.c -o memory_allocation && size memory_allocation

/*
?   A static int variable remains in memory while the program is running. A
normal or auto variable is destroyed when a function call where the variable was
declared is over. ?   Unlike global functions in C, access to static functions
is restricted to the file where they are declared.


?   A typical memory representation of a C program consists of the following
sections. Memory Layout of C Programs -    GeeksforGeeks
        1. Text segment  (i.e. instructions)
        2. Initialized data segment  (contains the global variables and static
variables that are initialized by the programmer)
        3. Uninitialized data segment  (bss)(contains all global variables and
static variables that are initialized to zero or do not have explicit
initialization in source code.)
        4. Heap (segment where dynamic memory allocation usually takes place.
        The heap area begins at the end of the BSS segment and grows to larger
addresses from there. The Heap area is managed by malloc, realloc, and free)
        5. Stack (adjoined the heap area and grew in the opposite direction;
when the stack pointer met the heap pointer, free memory was exhausted)

*   Dynamic Memory Allocation can be defined as a procedure in which the size of
a data structure (like Array) is changed during the runtime.


    Dynamically allocate an object
    Object* o = new Object();
    need to free it by delete o;

    Create a new object on the stack
    Object o = Object();
    gets automatically deallocated, object's destructor will get called, but the
object's stack memory will be reused.



    Java doesn't automatically remove the object at the end of each loop.
Instead it waits until there is a lot of garbage and then goes through and
collects it. Java makes no guarantees about how long it will be before the
object is collected.
*/