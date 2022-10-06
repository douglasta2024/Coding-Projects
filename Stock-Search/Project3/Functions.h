#pragma once

#include <string>
#include <iostream>
#include <vector>
#include <fstream>
#include <sstream>
#include <time.h>
#include <conio.h>
#include "Stock.h"

void loadData(string ticker, vector<Stock*>& Dow30, int index);
bool findStock(string ticker, vector<Stock*>& Dow30, int& index);
int dateConverter(string date);
string convertIntDateToString(int date);
bool verifyDate(string date);


class timer {
public:
    timer();
    void start();
    void stop();
    void reset();
    bool isRunning();
    unsigned long getTime();
    bool isOver(unsigned long seconds);
private:
    bool resetted;
    bool running;
    unsigned long beg;
    unsigned long end;
};


