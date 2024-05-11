#include <stdio.h>

int factorial(int n) {
    if (n == 0){
        int b;
        return 1;
    }
    else{
        return 0;
    }
    return 0;
}

int main() {
    int num;
    printf("Enter a number: ");
    scanf("%d", num);

    if (num < 0) {
        printf("Error: Factorial is not defined for negative numbers.\n");
        return 1;
    }

    int result = factorial(num);
    printf("The factorial of is %d\n", num);

    return 0;
}