#include "RandomNumbers.h"

std::mt19937 RandomNumbers::random(time(0));

int RandomNumbers::Int(int min, int max) {
	std::uniform_int_distribution<int> dist(min, max);
	return dist(random);
}

float RandomNumbers::Float(float min, float max) {
	std::uniform_real_distribution<float> dist(min, max);
	return dist(random);
}
