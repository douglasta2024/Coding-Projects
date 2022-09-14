#pragma once
#include <SFML/Graphics.hpp>
#include "TextureManager.h"

class Tiles
{
	sf::Sprite surfaceTexture;
	sf::Sprite backgroundTexture;
	sf::Sprite mine;
	sf::Sprite flag;
	sf::Sprite menuButton;
	sf::Sprite number;
	float y_pos;
	float x_pos;
	bool recursion;

public:
	bool wasClicked;
	vector<Tiles*> adjacentMine;
	bool flagLocked;
	bool hasFlag;
	bool hasMine;
	bool rightClicked;
	int recursiveCounter;
	bool recursionC;
	Tiles();
	void MakeTile();
	void DisplayTile(sf::RenderWindow& window);
	void SetTilePosition(float x, float y);
	sf::FloatRect GetBounds();
	void wasClickedFunction();
	void wasRightClickedFunction();
	void MakeMineTile();
	void DrawTile(Tiles& tile, sf::RenderWindow& window);
	void SetTexture(string texture);
	void DrawMine(sf::RenderWindow& window);
	sf::FloatRect GetMenuPosition();
	void MakeFlagTile();
	void DrawFlag(sf::RenderWindow& window);
	void RecursiveCheck();
	void DrawNumber(sf::RenderWindow& window);
};

