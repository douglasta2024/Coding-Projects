#include "DayData.h"

DayData::DayData(string ticker, int date, float open, float close, float high, float low) {

    this->ticker = ticker;
    this->date = date;
    this->open = open;
    this->close = close;
    this->high = high;
    this->low = low;
    percentReturn = ((close - open) / open) * 100;
    percentNetChange = ((high - low) / low) * 100;

}

DayData::DayData() {

    date = 0;
    open = 0;
    close = 0;
    high = 0;
    low = 0;
    percentReturn = 0;
    percentNetChange = 0;

}