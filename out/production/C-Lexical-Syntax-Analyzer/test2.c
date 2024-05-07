#include <stdio.h>

int main() {
    int num;
    int arr[10];
    int sum = 0;
    for(int i = 0;i<10;i++){
        scanf(num);
        arr[i] = num;
        sum += num;
    }

    printf("The sum is: %d", sum);
    return 0;
}