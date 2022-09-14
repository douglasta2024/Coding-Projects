#include "Tiles.h"


Tiles::Tiles() {
	MakeTile();
}

void Tiles::MakeTile() {
	surfaceTexture.setTexture(TextureManager::GetTexture("tile_hidden"));
	backgroundTexture.setTexture(TextureManager::GetTexture("tile_revealed"));
	mine.setTexture(TextureManager::GetTexture("mine"));
	flag.setTexture(TextureManager::GetTexture("flag"));
	wasClicked = false;
	hasMine = false;
	hasFlag = false;
	rightClicked = false;
	flagLocked = false;
	recursion = false;
	recursionC = false;
	recursiveCounter = 0;


}

void Tiles::DisplayTile(sf::RenderWindow& window) {

	surfaceTexture.setPosition(x_pos, y_pos);
	backgroundTexture.setPosition(x_pos, y_pos);

	if (!wasClicked) {
		window.draw(surfaceTexture);
		if (rightClicked) {
			if (hasFlag == true) {
				flag.setPosition(x_pos, y_pos);
				window.draw(flag);
			}
		}

	}
	else {
		if (flagLocked == true) {
			wasClicked = false;
			return;
		}
		window.draw(backgroundTexture);
		if (hasMine == true) {
			mine.setPosition(x_pos, y_pos);
			window.draw(mine);
		}
		else {
			DrawNumber(window);
		}
	}
	


}

void Tiles::SetTilePosition(float x, float y) {
	x_pos = x;
	y_pos = y;
}

sf::FloatRect Tiles::GetBounds() {
	return surfaceTexture.getGlobalBounds();
}

void Tiles::wasClickedFunction() {
	wasClicked = true;
	if (recursiveCounter == 0) {
		if (recursionC == true) {
			return;
		}
		if (hasMine == true) {
			return;
		}
		if (hasFlag == true) {
			return;
		}
		recursionC = true;
		for (int i = 0; i < adjacentMine.size(); i++)
		{
			adjacentMine[i]->wasClickedFunction();
		}
	}
}

void Tiles::MakeMineTile() {
	hasMine = true;
}

void Tiles::DrawTile(Tiles& tile, sf::RenderWindow& window) {
	menuButton.setPosition(x_pos, y_pos);
	window.draw(tile.menuButton);
}

void Tiles::SetTexture(string texture) {
	menuButton.setTexture(TextureManager::GetTexture(texture));
}

void Tiles::DrawMine(sf::RenderWindow& window) {
	mine.setPosition(x_pos,y_pos);
	window.draw(mine);
}

sf::FloatRect Tiles::GetMenuPosition() {
	return menuButton.getGlobalBounds();
}

void Tiles::MakeFlagTile() {
	if (wasClicked == false) {
		if (hasFlag == false) {
			hasFlag = true;
			flagLocked = true;
		}
		else {
			hasFlag = false;
			flagLocked = false;
		}
	}

}

void Tiles::DrawFlag(sf::RenderWindow& window) {
	flag.setPosition(x_pos, y_pos);
	window.draw(flag);
}
void Tiles::wasRightClickedFunction() {
	rightClicked = true;
}

void Tiles::RecursiveCheck() {
	if (recursion) {
		return;
	}
	recursion = true;
	for (int i = 0; i < adjacentMine.size(); i++)
	{
		if (adjacentMine[i]->hasMine) {
			recursiveCounter += 1;
		}
		adjacentMine[i]->RecursiveCheck();
	}
}

void Tiles::DrawNumber(sf::RenderWindow& window) {
	if (recursiveCounter == 0) {
		return;
	}
	else if (recursiveCounter == 1) {
		number.setTexture(TextureManager::GetTexture("number_1"));
		number.setPosition(x_pos,y_pos);
		window.draw(number);
	}
	else if (recursiveCounter == 2) {
		number.setTexture(TextureManager::GetTexture("number_2"));
		number.setPosition(x_pos, y_pos);
		window.draw(number);
	}
	else if (recursiveCounter == 3) {
		number.setTexture(TextureManager::GetTexture("number_3"));
		number.setPosition(x_pos, y_pos);
		window.draw(number);
	}
	else if (recursiveCounter == 4) {
		number.setTexture(TextureManager::GetTexture("number_4"));
		number.setPosition(x_pos, y_pos);
		window.draw(number);
	}
	else if (recursiveCounter == 5) {
		number.setTexture(TextureManager::GetTexture("number_5"));
		number.setPosition(x_pos, y_pos);
		window.draw(number);
	}
	else if (recursiveCounter == 6) {
		number.setTexture(TextureManager::GetTexture("number_6"));
		number.setPosition(x_pos, y_pos);
		window.draw(number);
	}
	else if (recursiveCounter == 7) {
		number.setTexture(TextureManager::GetTexture("number_7"));
		number.setPosition(x_pos, y_pos);
		window.draw(number);
	}
	else if (recursiveCounter == 8) {
		number.setTexture(TextureManager::GetTexture("number_8"));
		number.setPosition(x_pos, y_pos);
		window.draw(number);
	}

}





