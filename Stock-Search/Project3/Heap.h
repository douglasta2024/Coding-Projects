#pragma once
#include "Stock.h"
#include <iostream>
#include <vector>
#include <string>
#include <utility>
using namespace std;

//if looking for highest returns we use a max heap
//if looking for lowest returns we use a min heap
//if looking for highest net chanlge we use max heap
//if looking for lowest net change we use min heap


class MinHeap {

	vector<pair<float, DayData>> vec;

public:
	pair<float, DayData> Extract();
	void Insert(pair<float, DayData> data);
	void HeapifyDown(int rootIndex);
	void HeapifyUp(int rootIndex);
    ~MinHeap();
};

class MaxHeap {

	vector<pair<float, DayData>> vec;

public:
	pair<float, DayData> Extract();
	void Insert(pair<float, DayData> data);
	void HeapifyDown(int rootIndex);
	void HeapifyUp(int rootIndex);
    ~MaxHeap();

};
