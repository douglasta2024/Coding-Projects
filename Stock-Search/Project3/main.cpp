#include <iostream>
#include <fstream>
#include <sstream>
#include <vector>
#include <iomanip>
#include <time.h>
#include "Stock.h"
#include "DayData.h"
#include "Heap.h"
#include "Functions.h"
#include "MergeSort.h"


using namespace std;


int main() {

    cout << "Welcome to a stock search application created by Dylan DePasquale, Douglas Ta, and Richard Qian." << endl << endl;
    cout
            << "This application allows you to see the best (or worst) performing days of a particular stock for a given criteria."
            << endl;
    cout
            << "You will be asked to enter a stock ticker, the criteria to sort by, a date range, as well as how many days to return."
            << endl << endl;

    cout << "Please enter a valid stock ticker from the Dow 30." << endl;
    cout << "Refer to \'cnbc.com/dow-30\' for available stocks." << endl << endl;
    cout << "Enter all the stocks you want to search. Type \"DONE\" when you are finished." << endl;

    string ticker;
    vector<Stock *> Dow30;
    bool check = true;
    int index = 0;

    while (ticker != "DONE") {
        cin >> ticker;
        index = 0;
        bool found = findStock(ticker, Dow30, index);


        //Check if this is a valid stock
        string filePath = "../excel/" + ticker + ".csv";
        ifstream excel(filePath);

        if (excel.is_open()) {
            excel.close();
            //create stock class and load data
            if (!found) {
                Stock *addedStock = new Stock(ticker);
                Dow30.push_back(addedStock);
                index = Dow30.size() - 1;

                loadData(ticker, Dow30, index);
            }
        } else {
            if (ticker != "DONE") {
                cout << "\'" + ticker + "\'" << " is not a valid ticker symbol." << endl;
                cout << "Try again!" << endl;
            }
        }

        if (ticker != "DONE") {
            cout << "Would you like to input another stock? Type \"DONE\" if you're finished." << endl;
        }

    }


    //print debugging
    /*
    for(int i = 0; i < Dow30.size(); i++){
        cout << Dow30.at(i)->returnTicker() << endl;
        cout << "Number of Days: " << Dow30.at(i)->returnDates().size() << endl;
    }
     */

    cout << endl
         << "You will now be asked for two dates which will serve as a range in which your data will be searched"
         << endl;
    cout << "Data is available from 2017-04-03 to 2022-03-31. Only valid ranges are accepted." << endl << endl;


    string startDate, endDate;
    check = true;
    while (check) {
        cout << "Please enter the first date (in the format YYYY-MM-DD)" << endl;
        cin >> startDate;
        cout << "Please enter the last date (in the format YYYY-MM-DD)" << endl;
        cin >> endDate;

        if (verifyDate(startDate) || verifyDate(endDate)) {
            cout << "This is an invalid input." << endl;
            continue;
        }
        if (dateConverter(startDate) > dateConverter(endDate)) {
            cout << "This is an invalid range." << endl;
            continue;
        }
        check = false;
    }

    cout << "You selected to search from " << startDate << " to " << endDate << "." << endl << endl;

    int menuSelection;
    int numDays;
    int start;
    int end;
    bool valid;
    Stock *stockPtr = Dow30.at(0);
    map<int, DayData>::iterator iterStart = stockPtr->dates.begin();
    map<int, DayData>::iterator iterEnd = stockPtr->dates.begin();

    bool exceed = false;

    check = true;
    while (check) {
        cout << "Please select one of the following criteria to search for." << endl;
        cout << "1. Most Returns" << endl;
        cout << "2. Least Returns (Most Losses)" << endl;
        cout << "3. Most Net Change (Measure for Volatility)" << endl;
        cout << "4. Least Net Change (Measure for Stability)" << endl;
        cin >> menuSelection;
        if (menuSelection <= 0 || menuSelection > 4) {
            cout << "Invalid input!" << endl;
            continue;
        }


        cout << "Please input the number of days you want to search for. (i.e. Top X days with most returns)" << endl;
        cin >> numDays;


        start = dateConverter(startDate);
        end = dateConverter(endDate);
        check = false;

        if (iterStart == stockPtr->dates.end() || iterEnd == stockPtr->dates.end()) {
            cout << "Your start date is invalid (you chose a day when the market was closed)." << endl << "Try again."
                 << endl;
            check = true;
            valid = false;
        }

    }

    MaxHeap *maxH = new MaxHeap();
    MergeSort *mrgSrt = new MergeSort();
    MinHeap *minH = new MinHeap();

    int heapTime = 0;
    int mergeTime = 0;
    timer clock;

    for (int i = 0; i < Dow30.size(); i++) {

        stockPtr = Dow30.at(i);
        iterStart = stockPtr->dates.find(start);
        iterEnd = stockPtr->dates.find(end);
        auto tempIter = iterStart;
        valid = true;
        check = false;



        //HEAP SORT and MERGESORT for each Selection
        if (valid) {

            if (menuSelection == 1) {     //MOST RETURNS

                /* ====== HEAPSORT ====== */

                //map<int, DayData> copyOfDates = Dow30.at(index)->returnDates();
                //map<int, DayData>::iterator copyIter = copyOfDates.begin();

                iterStart = tempIter;

                int daysWithinRange = 0;

                clock.reset();
                clock.start();
                for (; iterStart != iterEnd; ++iterStart) {

                    pair<float, DayData> temp;
                    temp.first = iterStart->second.percentReturn;
                    temp.second = iterStart->second;
                    maxH->Insert(temp);
                    daysWithinRange++;

                }
                clock.stop();
                heapTime += clock.getTime();
                clock.reset();

                //TODO: Heap destructor and MERGESORT DESTRUCTOR?
                if (numDays < daysWithinRange) {

                    //MERGE SORT
                    iterStart = tempIter;

                    clock.reset();
                    clock.start();
                    for (; iterStart != iterEnd; ++iterStart) {

                        pair<float, DayData> temp;
                        temp.first = iterStart->second.percentReturn;
                        temp.second = iterStart->second;
                        mrgSrt->Insert(temp);

                    }

                    clock.stop();
                    mergeTime += clock.getTime();
                    clock.reset();


                } else {
                    cout
                            << "Error: The number of days you want to display exceed the amount of days within your range!"
                            << endl;
                    exceed = true;
                    break;
                }
            } else if (menuSelection == 2) {

                //HEAP SORT

                iterStart = tempIter;

                int daysWithinRange = 0;

                clock.reset();
                clock.start();
                for (; iterStart != iterEnd; ++iterStart) {

                    pair<float, DayData> temp;
                    temp.first = iterStart->second.percentReturn;
                    temp.second = iterStart->second;
                    minH->Insert(temp);
                    daysWithinRange++;

                }
                clock.stop();
                heapTime += clock.getTime();
                clock.reset();

                if (numDays < daysWithinRange) {


                    //MERGE SORT
                    iterStart = tempIter;

                    clock.reset();
                    clock.start();
                    for (; iterStart != iterEnd; ++iterStart) {

                        pair<float, DayData> temp;
                        temp.first = iterStart->second.percentReturn;
                        temp.second = iterStart->second;
                        mrgSrt->Insert(temp);

                    }


                    clock.stop();
                    mergeTime += clock.getTime();
                    clock.reset();

                } else {
                    cout
                            << "Error: The number of days you want to display exceed the amount of days within your range!"
                            << endl;
                    exceed = true;
                    break;
                }
            } else if (menuSelection == 3) {

                iterStart = tempIter;

                int daysWithinRange = 0;

                clock.reset();
                clock.start();
                for (; iterStart != iterEnd; ++iterStart) {

                    pair<float, DayData> temp;
                    temp.first = iterStart->second.percentNetChange;
                    temp.second = iterStart->second;
                    maxH->Insert(temp);
                    daysWithinRange++;
                }

                clock.stop();
                heapTime += clock.getTime();
                clock.reset();

                //TODO: Heap destructor and MERGESORT DESTRUCTOR?


                if (numDays < daysWithinRange) {
                    //MERGE SORT
                    iterStart = tempIter;

                    clock.reset();
                    clock.start();
                    for (; iterStart != iterEnd; ++iterStart) {

                        pair<float, DayData> temp;
                        temp.first = iterStart->second.percentNetChange;
                        temp.second = iterStart->second;
                        mrgSrt->Insert(temp);

                    }

                    clock.stop();
                    mergeTime += clock.getTime();
                    clock.reset();

                } else {
                    cout
                            << "Error: The number of days you want to display exceed the amount of days within your range!"
                            << endl;
                    exceed = true;
                    break;
                }
            }
            else if (menuSelection == 4) {

                iterStart = tempIter;

                int daysWithinRange = 0;

                clock.reset();
                clock.start();
                for (; iterStart != iterEnd; ++iterStart) {

                    pair<float, DayData> temp;
                    temp.first = iterStart->second.percentNetChange;
                    temp.second = iterStart->second;
                    minH->Insert(temp);
                    daysWithinRange++;
                }
                clock.stop();
                heapTime += clock.getTime();
                clock.reset();
                //TODO: Heap destructor?

                if (numDays < daysWithinRange) {
                    //MERGE SORT
                    iterStart = tempIter;

                    clock.reset();
                    clock.start();
                    for (; iterStart != iterEnd; ++iterStart) {

                        pair<float, DayData> temp;
                        temp.first = iterStart->second.percentNetChange;
                        temp.second = iterStart->second;
                        mrgSrt->Insert(temp);

                    }

                    clock.stop();
                    mergeTime += clock.getTime();
                    clock.reset();
                } else {
                    cout
                            << "Error: The number of days you want to display exceed the amount of days within your range!"
                            << endl;
                    exceed = true;
                    break;
                }
            }
        }
    }

    if(!exceed) {

        cout << endl << "HEAP SORT:" << endl;
        if (menuSelection == 1 || menuSelection == 3) {
            clock.reset();
            clock.start();
            for (int i = 1; i <= numDays; i++) {

                pair<float, DayData> temp = maxH->Extract();
                cout << i << ". " << temp.second.ticker << " " << convertIntDateToString(temp.second.date) << " ";
                cout << fixed << setprecision(5) << temp.second.percentReturn << "%" << endl;

            }
            clock.stop();
            heapTime += clock.getTime();
            cout << endl << "Time: " << heapTime << " ms" << endl;
            clock.reset();
        } else {
            clock.reset();
            clock.start();
            for (int i = 1; i <= numDays; i++) {

                pair<float, DayData> temp = minH->Extract();
                cout << i << ". " << temp.second.ticker << " " << convertIntDateToString(temp.second.date) << " ";
                cout << fixed << setprecision(5) << temp.second.percentReturn << "%" << endl;

            }
            clock.stop();
            heapTime += clock.getTime();
            cout << endl << "Time: " << heapTime << " ms" << endl;
        }

        clock.reset();
        clock.start();
        mrgSrt->Sort();
        if (menuSelection == 1 || menuSelection == 3) {
            cout << endl << "MERGE SORT:" << endl;
            for (int i = 1; i <= numDays; i++) {
                pair<float, DayData> temp = mrgSrt->vec.at(mrgSrt->vec.size() - i);
                cout << i << ". " << temp.second.ticker << " " << convertIntDateToString(temp.second.date) << " ";
                cout << fixed << setprecision(5) << temp.second.percentReturn << "%" << endl;

            }
            clock.stop();
            mergeTime += clock.getTime();
            cout << endl << "Time: " << mergeTime << " ms" << endl;
        } else {
            cout << endl << "MERGE SORT:" << endl;
            for (int i = 0; i < numDays; i++) {
                pair<float, DayData> temp = mrgSrt->vec.at(i);
                cout << i + 1 << ". " << temp.second.ticker << " " << convertIntDateToString(temp.second.date) << " ";
                cout << fixed << setprecision(5) << temp.second.percentReturn << "%" << endl;

            }
            clock.stop();
            mergeTime += clock.getTime();
            cout << endl << "Time: " << mergeTime << " ms" << endl;
        }
    } else {
        cout << "Program terminated: invalid input!" << endl;
    }

    //call destructors
    delete mrgSrt;
    delete maxH;
    delete minH;


    return 0;
}
