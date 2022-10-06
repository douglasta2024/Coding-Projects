#include "Stock.h"

Stock::Stock(string _ticker) {
    ticker = _ticker;
}
void Stock::AddDate(int date, DayData data) {
    dates[date] = data;
}

string Stock::returnTicker() {
    return ticker;

}

map<int, DayData> Stock::returnDates() {
    return dates;
}

Stock::~Stock() {

    dates.clear();

}
