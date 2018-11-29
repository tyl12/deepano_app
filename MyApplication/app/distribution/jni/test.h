#ifndef A_H
#define A_H


#include <iostream>
using namespace std;

class A{
public:
    A(){
        cout<<__FUNCTION__<<endl;
    }
    virtual ~A(){
        cout<<__FUNCTION__<<endl;
    }
    void test();
    /*
    void test(){
        cout<<__FUNCTION__<<endl;
    }
    */
};

#endif
