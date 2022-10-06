#pragma once
#include <iostream>
#include <vector>
#include <string>
#include <utility>
#include "Stock.h"

struct MergeSort {

	vector<pair<float, DayData>> vec;

public:
	void Insert(pair<float, DayData> data);
	void Sort();
    ~MergeSort();


private:
	void merge(vector<pair<float, DayData>>& input, int left, int middle, int right);
	void mergeSort(vector<pair<float, DayData>>& input, int left, int right);
};