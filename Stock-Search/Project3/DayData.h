#pragma once
#include <string>

using std::string;

struct DayData {
    string ticker;
    int date;
    float open;
    float close;
    float high;
    float low;
    float percentReturn; //(close - open) / open
    float percentNetChange; //(high - low) / low

    //Constructor
    DayData(string ticker, int date, float open, float close, float high, float low);
    DayData();
};