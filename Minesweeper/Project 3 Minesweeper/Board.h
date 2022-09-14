#pragma once
#include <SFML/Graphics.hpp>
#include "TextureManager.h"
#include "Tiles.h"
#include <vector>
#include <iostream>
#include <fstream>
#include <string>
#include <cstring>
#include "RandomNumbers.h"
using namespace std;

class Board
{
	int columns;
	int rows;
	int mines;
	vector<vector<Tiles>> board;
	
public:
	int mineCount;
	int flagCount;
	bool defeat;
	bool win;
	bool playing;
	bool debugOn;
	Tiles debug;
	Tiles face;
	Tiles test1;
	Tiles test2;
	Tiles test3;
	sf::Sprite counter1;
	sf::Sprite counter2;
	sf::Sprite counter3;
	sf::Sprite negativeCounter;
	vector<Tiles*> mineArr;
	Board(int columns, int rows, int mines);
	vector<vector<Tiles>>& GetBoard();
	void RenderBoard(sf::RenderWindow& window);
	int GetMines();
	sf::FloatRect CheckBounds(int i, int j);
	void wasClicked(int i, int j);
	void wasRightClicked(int i, int j);
	void DrawMenuButtons(sf::RenderWindow& window);
	void DebugButton(sf::RenderWindow& window);
	void FaceButton(sf::RenderWindow& window);
	void FlagButton(sf::RenderWindow& window, int i, int j);
	void CreateBoard(int columns, int rows, int mines);
	void Defeat();
	bool HasMine(int x, int y);
	bool IsPlaying();
	bool HasFlag(int x, int y);
	void CheckForWin();
	void LoadPresetBoards(string file);
	void MineCountFunction(sf::RenderWindow& window);
	void TurnMineToFlags();
};

