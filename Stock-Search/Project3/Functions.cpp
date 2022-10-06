#include "Functions.h"

void loadData(string ticker, vector<Stock*>& Dow30, int index) {

    string filePath = "../excel/" + ticker + ".csv";

    ifstream input(filePath);

    if (input.is_open()) {
        //cout << "The file was opened." << endl;

        string line;
        getline(input, line);

        while (getline(input, line)) {

            istringstream stream(line);
            string dateString;
            string openString;
            string highString;
            string lowString;
            string closeString;

            int date;
            float open;
            float high;
            float low;
            float close;

            getline(stream, dateString, ',');
            getline(stream, openString, ',');
            getline(stream, highString, ',');
            getline(stream, lowString, ',');
            getline(stream, closeString, ',');

            /* dateString.erase(0, 2);   //remove first two characters
            for(int i = 0; i < dateString.size(); i++){
                if(dateString.at(i) == '-'){
                    dateString.erase(i, 1);
                }
            }
             */

            date = dateConverter(dateString);
            open = stof(openString);
            high = stof(highString);
            low = stof(lowString);
            close = stof(closeString);

            Dow30.at(index)->AddDate(date, DayData(ticker, date, open, close, high, low));

        }

    }
    else {
        cout << "The file failed to open" << endl;
    }

}
bool findStock(string ticker, vector<Stock*>& Dow30, int& index) {

    for (int i = 0; i < Dow30.size(); i++) {

        if (Dow30.at(i)->returnTicker() == ticker) {
            index = i;
            return true;
        }
    }

    index = -1;
    return false;

}
int dateConverter(string date) {
    date = date.substr(2, date.length() - 2);
    for (int i = 0; i < date.length(); i++) {
        if (date[i] == '-') {
            date.erase(i, 1);
        }

    }
    return stoi(date);

}
string convertIntDateToString(int date) {

    //Date in the format YY MM DD

    string stringDate = "";

    int tempDigits = date % 100;
    date /= 100;

    if (tempDigits >= 10) {
        stringDate = to_string(tempDigits);
    }
    else {
        stringDate = '0' + to_string(tempDigits);
    }

    tempDigits = date % 100;
    date /= 100;

    if (tempDigits >= 10) {
        stringDate = to_string(tempDigits) + '-' + stringDate;
    }
    else {
        stringDate = '0' + to_string(tempDigits) + '-' + stringDate;
    }

    tempDigits = date % 100;
    date /= 100;
    if (tempDigits >= 10) {
        stringDate = "20" + to_string(tempDigits) + '-' + stringDate;
    }
    else {
        stringDate = "200" + to_string(tempDigits) + '-' + stringDate;
    }

    return stringDate;

}

bool verifyDate(string date) { //Breaks the date given by user input into different strings to test for specific format
    if (date.length() != 10) {
        return true;
    }
    string year = date.substr(0, 4);
    string month = date.substr(5, 2);
    string day = date.substr(8, 2);

    for (int i = 0; i < year.length(); i++) {
        if (isdigit(year[i]) == false) {
            return true;
        }
    }

    for (int i = 0; i < month.length(); i++) {
        if (isdigit(month[i]) == false) {
            return true;
        }
        if (isdigit(day[i]) == false) {
            return true;
        }

    }
    return false;

}

timer::timer() {
    resetted = true;
    running = false;
    beg = 0;
    end = 0;
}


void timer::start() {
    if (!running) {
        if (resetted)
            beg = (unsigned long)clock();
        else
            beg -= end - (unsigned long)clock();
        running = true;
        resetted = false;
    }
}


void timer::stop() {
    if (running) {
        end = (unsigned long)clock();
        running = false;
    }
}


void timer::reset() {
    bool wereRunning = running;
    if (wereRunning)
        stop();
    resetted = true;
    beg = 0;
    end = 0;
    if (wereRunning)
        start();
}


bool timer::isRunning() {
    return running;
}


unsigned long timer::getTime() {
    if (running)
        return ((unsigned long)clock() - beg) / CLOCKS_PER_SEC;
    else
        return end - beg;
}


bool timer::isOver(unsigned long seconds) {
    return seconds >= getTime();
}
