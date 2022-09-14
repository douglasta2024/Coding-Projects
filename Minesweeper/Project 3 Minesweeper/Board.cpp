#include "Board.h"

Board::Board(int columns, int rows, int mines) {
	CreateBoard(columns, rows, mines);
}

void Board::CreateBoard(int columns, int rows, int mines) {
	this->columns = columns;
	this->rows = rows;
	board.resize(columns);
	debugOn = false; 
	defeat = false;
	playing = true;
	win = false;
	flagCount = 0;
	mineCount = mines;
	counter1.setTexture(TextureManager::GetTexture("digits"));
	counter2.setTexture(TextureManager::GetTexture("digits"));
	counter3.setTexture(TextureManager::GetTexture("digits"));
	negativeCounter.setTexture(TextureManager::GetTexture("digits"));
	negativeCounter.setPosition(0, rows * 32);
	counter1.setPosition(21,rows * 32);
	counter2.setPosition(21 * 2, rows * 32);
	counter3.setPosition(21 * 3, rows * 32);


	for (int i = 0; i < columns; i++)
	{
		board[i].resize(rows);
	}


	for (int i = 0; i < columns; i++)
	{
		for (int j = 0; j < rows; j++)
		{
			board[i].pop_back();
		}
	}
	

	for (int i = 0; i < columns; i++)
	{
		board[i].resize(rows);
	}

	for (int i = 0; i < columns; i++)
	{
		for (int j = 0; j < rows; j++)
		{
			board[i][j].SetTilePosition((i * 32), (j * 32));
		}
	}

	for (int i = 0; i < mines; i++)
	{
		int x;
		int y;
		x = RandomNumbers::Int(0, (columns - 1));
		y = RandomNumbers::Int(0, (rows - 1));
		if (board[x][y].hasMine == true) {
			i -= 1;
		}
		else {
			board[x][y].MakeMineTile();
			mineArr.push_back(&board[x][y]);
		}
		
	}

	for (int i = 0; i < columns; i++)
	{
		for (int j = 0; j < rows; j++)
		{
			if (i == 0 && j == 0) { //top left corner
				board[i][j].adjacentMine.push_back(&board[i][j + 1]);
				board[i][j].adjacentMine.push_back(&board[i + 1][j + 1]);
				board[i][j].adjacentMine.push_back(&board[i + 1][j]);
			}
			else if (i == columns - 1 && j == 0) { //top right corner
				board[i][j].adjacentMine.push_back(&board[i - 1][j]);
				board[i][j].adjacentMine.push_back(&board[i][j + 1]);
				board[i][j].adjacentMine.push_back(&board[i - 1][j + 1]);
			}
			else if (i == 0 && j == rows - 1) { //bottom left corner
				board[i][j].adjacentMine.push_back(&board[i + 1][j]);
				board[i][j].adjacentMine.push_back(&board[i + 1][j - 1]);
				board[i][j].adjacentMine.push_back(&board[i][j - 1]);
			}
			else if (i == columns - 1 && j == rows - 1) { //bottom right corner
				board[i][j].adjacentMine.push_back(&board[i - 1][j]);
				board[i][j].adjacentMine.push_back(&board[i - 1][j - 1]);
				board[i][j].adjacentMine.push_back(&board[i][j - 1]);
			}
			else if (j == 0 && i != 0 && i != columns - 1) { //top edge
				board[i][j].adjacentMine.push_back(&board[i - 1][j]);
				board[i][j].adjacentMine.push_back(&board[i - 1][j + 1]);
				board[i][j].adjacentMine.push_back(&board[i][j + 1]);
				board[i][j].adjacentMine.push_back(&board[i + 1][j + 1]);
				board[i][j].adjacentMine.push_back(&board[i + 1][j]);
			}
			else if (j == rows - 1 && i != 0 && i != columns - 1) { //bottom edge
				board[i][j].adjacentMine.push_back(&board[i - 1][j]);
				board[i][j].adjacentMine.push_back(&board[i - 1][j - 1]);
				board[i][j].adjacentMine.push_back(&board[i][j - 1]);
				board[i][j].adjacentMine.push_back(&board[i + 1][j - 1]);
				board[i][j].adjacentMine.push_back(&board[i + 1][j]);
			}
			else if (i == 0 && j != 0 && j != rows - 1) { //left edg
				board[i][j].adjacentMine.push_back(&board[i][j - 1]);
				board[i][j].adjacentMine.push_back(&board[i + 1][j - 1]);
				board[i][j].adjacentMine.push_back(&board[i + 1][j]);
				board[i][j].adjacentMine.push_back(&board[i + 1][j + 1]);
				board[i][j].adjacentMine.push_back(&board[i][j + 1]);
			}
			else if (i == columns - 1 && j != 0 && j != rows - 1) { //right edge
				board[i][j].adjacentMine.push_back(&board[i][j - 1]);
				board[i][j].adjacentMine.push_back(&board[i - 1][j - 1]);
				board[i][j].adjacentMine.push_back(&board[i - 1][j]);
				board[i][j].adjacentMine.push_back(&board[i - 1][j + 1]);
				board[i][j].adjacentMine.push_back(&board[i][j + 1]);
			}
			else {
				board[i][j].adjacentMine.push_back(&board[i - 1][j - 1]);
				board[i][j].adjacentMine.push_back(&board[i][j - 1]);
				board[i][j].adjacentMine.push_back(&board[i + 1][j - 1]);
				board[i][j].adjacentMine.push_back(&board[i - 1][j]);
				board[i][j].adjacentMine.push_back(&board[i + 1][j]);
				board[i][j].adjacentMine.push_back(&board[i - 1][j + 1]);
				board[i][j].adjacentMine.push_back(&board[i][j + 1]);
				board[i][j].adjacentMine.push_back(&board[i + 1][j + 1]);
			}


		}
	}

	board[0][0].RecursiveCheck();

	debug.SetTilePosition(columns * 20, (rows * 32));
	face.SetTilePosition(columns * 20 - 64 * 2, (rows * 32));
	test1.SetTilePosition(columns * 20 + 64, (rows * 32));
	test2.SetTilePosition(columns * 20 + 64 * 2, (rows * 32));
	test3.SetTilePosition(columns * 20 + 64 * 3, rows * 32);
}

void Board::RenderBoard(sf::RenderWindow& window) {

	for (int i = 0; i < columns; i++)
	{
		for (int j = 0; j < rows; j++)
		{
			board[i][j].DisplayTile(window);
			if (board[i][j].hasFlag) {
				board[i][j].DrawFlag(window);
			}
		}
	}
	MineCountFunction(window);
	window.draw(counter1);
	window.draw(counter2);
	window.draw(counter3);

	DebugButton(window);
	FaceButton(window);
}

vector<vector<Tiles>>& Board::GetBoard() {
	return board;
}

int Board::GetMines() {
	return mines;
}

sf::FloatRect Board::CheckBounds(int i, int j) {

	return board[i][j].GetBounds();
}

void Board::wasClicked(int i, int j) {
	board[i][j].wasClickedFunction();
}

void Board::DrawMenuButtons(sf::RenderWindow& window) {
	debug.Tiles::SetTexture("debug");
	debug.DrawTile(debug, window);

	test1.SetTexture("test_1");
	test1.DrawTile(test1, window);
	test2.SetTexture("test_2");
	test2.DrawTile(test2, window);
	test3.SetTexture("test_3");
	test3.DrawTile(test3, window);

	if (playing == true && defeat == false) {
		face.SetTexture("face_happy");
		face.DrawTile(face, window);
		
	}
	else if (defeat == true) {
		face.SetTexture("face_lose");
		face.DrawTile(face, window);
	}
	else if (win == true) {
		face.SetTexture("face_win");
		face.DrawTile(face, window);
		
	}
}

void Board::DebugButton(sf::RenderWindow& window) {
	
	if(debugOn == true){
		for (int i = 0; i < columns; i++)
		{
			for (int j = 0; j < rows; j++)
			{
				if (board[i][j].hasMine == true) {
					board[i][j].DrawMine(window);
				}
			}
		}
	}
}

void Board::FaceButton(sf::RenderWindow& window) {
	if (defeat == true) {
		for (int i = 0; i < columns; i++)
		{
			for (int j = 0; j < rows; j++)
			{
				if (board[i][j].hasMine == true) {
					board[i][j].DrawMine(window);
				}
			}
		}
	}
}

void Board::wasRightClicked(int i, int j) {
	board[i][j].wasRightClickedFunction();
}

void Board::FlagButton(sf::RenderWindow& window, int x, int y) {
	board[x][y].MakeFlagTile();
	if (board[x][y].hasFlag == true) {
		for (int i = 0; i < columns; i++)
		{
			for (int j = 0; j < rows; j++)
			{
				if (board[i][j].hasFlag) {
					board[i][j].DrawFlag(window);
				}
			}
		}
	}
	if (board[x][y].hasFlag == false && board[x][y].wasClicked == false) {
		mineCount++;
	}
	else if(board[x][y].hasFlag == true) {
		mineCount--;
	}
}

void Board::Defeat() {
	defeat = true;
	playing = false;
}

bool Board::HasMine(int x, int y) {
	return board[x][y].hasMine;
}

bool Board::HasFlag(int x, int y) {
	return board[x][y].hasFlag;
}

bool Board::IsPlaying() {
	return playing;
}

void Board::CheckForWin() { 
	bool temp = true;
	for (int i = 0; i < columns; i++)
	{
		for (int j = 0; j < rows; j++)
		{
			if (board[i][j].hasMine == false && board[i][j].wasClicked == false) {
				temp = false;
			}
		}
	}
	if (temp == true) {
		win = true;
		playing = false;
		debugOn = false;
		TurnMineToFlags();
	}
	else {
		win = false;
	}
}

void Board::TurnMineToFlags() {
	for (int i = 0; i < mineArr.size(); i++)
	{
		if (mineArr[i]->hasFlag == true) {
			continue;
		}
		mineArr[i]->MakeFlagTile();
	}
	mineCount = 0;
}

void Board::LoadPresetBoards(string file) {
	for (int i = 0; i < columns; i++)
	{
		for (int j = 0; j < rows; j++)
		{
			board[i].pop_back();
		}
	}
	mineArr.clear();
	ifstream test(file);
	string data;
	int _mines = 0;
	vector<vector<bool>> twoDvector;
		while (getline(test, data)) {
			vector<bool> oneDvector;
			for (int i = 0; i < data.length(); i++)
			{
				if (data[i] == '1') {
					oneDvector.push_back(true);
					_mines += 1;
				}
				else {
					oneDvector.push_back(false);
				}
			}
			twoDvector.push_back(oneDvector);
		}
		columns = twoDvector[0].size();
		rows = twoDvector.size();
		mines = _mines;

		for (int i = 0; i < columns; i++)
		{
			board[i].resize(rows);
		}

		for (int i = 0; i < columns; i++)
		{
			for (int j = 0; j < rows; j++)
			{
				board[i][j].SetTilePosition((i * 32), (j * 32));
				if (twoDvector[j][i] == false) {
					continue;
				}
				else if (twoDvector[j][i] == true) {
					board[i][j].MakeMineTile();
					mineArr.push_back(&board[i][j]);
				}
			}
		}
		debugOn = false;
		defeat = false;
		playing = true;
		win = false;
		flagCount = 0;
		mineCount = mines;
		counter1.setTexture(TextureManager::GetTexture("digits"));
		counter2.setTexture(TextureManager::GetTexture("digits"));
		counter3.setTexture(TextureManager::GetTexture("digits"));
		negativeCounter.setTexture(TextureManager::GetTexture("digits"));
		negativeCounter.setPosition(0, rows * 32);
		counter1.setPosition(21, rows * 32);
		counter2.setPosition(21 * 2, rows * 32);
		counter3.setPosition(21 * 3, rows * 32);

		for (int i = 0; i < columns; i++)
		{
			for (int j = 0; j < rows; j++)
			{
				if (i == 0 && j == 0) { //top left corner
					board[i][j].adjacentMine.push_back(&board[i][j + 1]);
					board[i][j].adjacentMine.push_back(&board[i + 1][j + 1]);
					board[i][j].adjacentMine.push_back(&board[i + 1][j]);
				}
				else if (i == columns - 1 && j == 0) { //top right corner
					board[i][j].adjacentMine.push_back(&board[i - 1][j]);
					board[i][j].adjacentMine.push_back(&board[i][j + 1]);
					board[i][j].adjacentMine.push_back(&board[i - 1][j + 1]);
				}
				else if (i == 0 && j == rows - 1) { //bottom left corner
					board[i][j].adjacentMine.push_back(&board[i + 1][j]);
					board[i][j].adjacentMine.push_back(&board[i + 1][j - 1]);
					board[i][j].adjacentMine.push_back(&board[i][j - 1]);
				}
				else if (i == columns - 1 && j == rows - 1) { //bottom right corner
					board[i][j].adjacentMine.push_back(&board[i - 1][j]);
					board[i][j].adjacentMine.push_back(&board[i - 1][j - 1]);
					board[i][j].adjacentMine.push_back(&board[i][j - 1]);
				}
				else if (j == 0 && i != 0 && i != columns - 1) { //top edge
					board[i][j].adjacentMine.push_back(&board[i - 1][j]);
					board[i][j].adjacentMine.push_back(&board[i - 1][j + 1]);
					board[i][j].adjacentMine.push_back(&board[i][j + 1]);
					board[i][j].adjacentMine.push_back(&board[i + 1][j + 1]);
					board[i][j].adjacentMine.push_back(&board[i + 1][j]);
				}
				else if (j == rows - 1 && i != 0 && i != columns - 1) { //bottom edge
					board[i][j].adjacentMine.push_back(&board[i - 1][j]);
					board[i][j].adjacentMine.push_back(&board[i - 1][j - 1]);
					board[i][j].adjacentMine.push_back(&board[i][j - 1]);
					board[i][j].adjacentMine.push_back(&board[i + 1][j - 1]);
					board[i][j].adjacentMine.push_back(&board[i + 1][j]);
				}
				else if (i == 0 && j != 0 && j != rows - 1) { //left edge
					board[i][j].adjacentMine.push_back(&board[i][j - 1]);
					board[i][j].adjacentMine.push_back(&board[i + 1][j - 1]);
					board[i][j].adjacentMine.push_back(&board[i + 1][j]);
					board[i][j].adjacentMine.push_back(&board[i + 1][j + 1]);
					board[i][j].adjacentMine.push_back(&board[i][j + 1]);
				}
				else if (i == columns - 1 && j != 0 && j != rows - 1) { //right edge
					board[i][j].adjacentMine.push_back(&board[i][j - 1]);
					board[i][j].adjacentMine.push_back(&board[i - 1][j - 1]);
					board[i][j].adjacentMine.push_back(&board[i - 1][j]);
					board[i][j].adjacentMine.push_back(&board[i - 1][j + 1]);
					board[i][j].adjacentMine.push_back(&board[i][j + 1]);
				}
				else {
					board[i][j].adjacentMine.push_back(&board[i - 1][j - 1]);
					board[i][j].adjacentMine.push_back(&board[i][j - 1]);
					board[i][j].adjacentMine.push_back(&board[i + 1][j - 1]);
					board[i][j].adjacentMine.push_back(&board[i - 1][j]);
					board[i][j].adjacentMine.push_back(&board[i + 1][j]);
					board[i][j].adjacentMine.push_back(&board[i - 1][j + 1]);
					board[i][j].adjacentMine.push_back(&board[i][j + 1]);
					board[i][j].adjacentMine.push_back(&board[i + 1][j + 1]);
				}


			}
		}

		board[0][0].RecursiveCheck();

		debug.SetTilePosition(columns * 20, (rows * 32));
		face.SetTilePosition(columns * 20 - 64 * 2, (rows * 32));
		test1.SetTilePosition(columns * 20 + 64, (rows * 32));
		test2.SetTilePosition(columns * 20 + 64 * 2, (rows * 32));
		test3.SetTilePosition(columns * 20 + 64 * 3, rows * 32);
}

void Board::MineCountFunction(sf::RenderWindow& window) {
	int x = mineCount;
	if (x < 0) {
		negativeCounter.setTextureRect(sf::IntRect(210, 0, 21, 32));
		window.draw(negativeCounter);
		x *= -1;
	}
	string counter = to_string(x);
	int one = 0;
	int ten = 0;
	int hundred = 0;

	if (counter.size() == 1) {
		one = stoi(counter);
		counter1.setTextureRect(sf::IntRect(21 * hundred, 0, 21, 32));
		counter2.setTextureRect(sf::IntRect(21 * ten, 0, 21, 32));
		counter3.setTextureRect(sf::IntRect(21 * one, 0, 21, 32));

	}
	else if (counter.size() == 2) {
		string temp = "";
		temp += counter[1];
		one = stoi(temp);
		temp = "";
		temp += counter[0];
		ten = stoi(temp);
		counter1.setTextureRect(sf::IntRect(21 * hundred, 0, 21, 32));
		counter2.setTextureRect(sf::IntRect(21 * ten, 0, 21, 32));
		counter3.setTextureRect(sf::IntRect(21 * one, 0, 21, 32));
	}
	else if (counter.size() == 3) {
		string temp = "";
		temp += counter[2];
		one = stoi(temp);
		temp = "";
		temp += counter[1];
		ten = stoi(temp);
		temp = "";
		temp += counter[0];
		hundred = stoi(temp);
		counter1.setTextureRect(sf::IntRect(21 * hundred, 0, 21, 32));
		counter2.setTextureRect(sf::IntRect(21 * ten, 0, 21, 32));
		counter3.setTextureRect(sf::IntRect(21 * one, 0, 21, 32));
	}
}





