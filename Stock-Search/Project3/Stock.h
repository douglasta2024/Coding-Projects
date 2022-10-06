#pragma once
#include <string>
#include <iostream>
#include <map>
#include "DayData.h"

using namespace std;

class Stock {

    string ticker;


    //key = date, value = object with all info for that date

public:
    //Constructors
    map<int, DayData> dates;
    Stock();
    Stock(string ticker);

    //Mutators
    void AddDate(int date, DayData data);

    //Accessors
    string returnTicker();
    map<int, DayData> returnDates();
    //Destructors
    ~Stock();
};

