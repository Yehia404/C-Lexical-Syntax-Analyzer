#include <stdio.h>

int a;

int sum (int a, int b){
    int value = a + b;
    if (value > a){
        value++;
    }
    switch (a){
        case 1: b=10;
                break;
        case 2: a++;
                return;
    }
    return value;
}

int main(){
    float d;
    int c = 5;
    int b = 10;
    int a = sum (c,b);

    printf("Yehia %d", a);
    if(c>b){
        for(int i=0;i<9;i++){
            int g = 10 + 9;
            switch(b){
                case 1: d = 1.5;
                        break;
                case 2: c =6;
                default: g = 4;
            }
        }
        return;
    }

    return 0;
}