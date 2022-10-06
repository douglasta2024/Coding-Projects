#include "MergeSort.h"

using namespace std;

void MergeSort::Insert(pair<float, DayData> data) {
    vec.push_back(data);
}
void MergeSort::Sort() {
    mergeSort(vec, 0, vec.size() - 1);
}
MergeSort::~MergeSort() {

    vec.clear();

}


//PRIVATES
void MergeSort::merge(vector<pair<float, DayData>>& input, int left, int middle, int right) {

    int i = left; //starting index for left subarray
    int j = middle + 1; //starting index for right subarray
    int k = left; //starting array for temporary array;

    int sizeOfTemp = input.size();

    pair<float, DayData>* temp = new pair<float, DayData>[sizeOfTemp];

    while (i <= middle && j <= right) {

        if (input[i].first <= input[j].first) {
            temp[k] = input[i];
            i++;
            k++;
        }
        else {
            temp[k] = input[j];
            j++;
            k++;
        }

    }

    while (i <= middle) {
        temp[k] = input[i];
        i++;
        k++;
    }

    while (j <= right) {
        temp[k] = input[j];
        j++;
        k++;
    }

    for (int x = left; x <= right; x++) {
        input[x] = temp[x];
    }

    delete[] temp;


}
void MergeSort::mergeSort(vector<pair<float, DayData>>& input, int left, int right) {
    if (left < right) {
        int middle = (left + right) / 2;
        mergeSort(input, left, middle);
        mergeSort(input, middle + 1, right);
        merge(input, left, middle, right);
    }
}