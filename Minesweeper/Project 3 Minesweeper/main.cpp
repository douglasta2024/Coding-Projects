#include <SFML/Graphics.hpp>
#include "Board.h"
#include "Tiles.h"
#include <iostream>
#include <vector>
#include <unordered_map>
#include <fstream>
#include "TextureManager.h"
using namespace std;

int main()
{
    ifstream Config("boards/config.cfg"); //Window Render
    int columns;
    int rows;
    int mines;
    Config >> columns;
    Config >> rows;
    Config >> mines;

    sf::Sprite mine(TextureManager::GetTexture("mine"));
    sf::Sprite hidden_tile(TextureManager::GetTexture("tile_hidden"));

    int width = columns * 32;
    int height = (rows * 32) + 88;
    int tile_count = columns * rows;

    Board PlaySpace(columns, rows, mines);

    sf::RenderWindow window(sf::VideoMode(width, height), "Minesweeper");
    while (window.isOpen()) {
        sf::Event event;
        while (window.pollEvent(event)) {
            if (event.type == sf::Event::Closed)
                window.close();
            sf::Vector2f mousePosition(sf::Mouse::getPosition(window).x, sf::Mouse::getPosition(window).y);
            if (sf::Mouse::isButtonPressed(sf::Mouse::Left) && PlaySpace.IsPlaying()) {             
                for (int i = 0; i < columns; i++)
                {
                    for (int j = 0; j < rows; j++)
                    {
                        if (PlaySpace.CheckBounds(i, j).contains(mousePosition)) {
                            PlaySpace.wasClicked(i,j);
                            if (PlaySpace.HasMine(i,j) && PlaySpace.HasFlag(i,j) == false) {
                                PlaySpace.Defeat();
                            }
                            
                        }
                       
                    }
                }
                if (PlaySpace.debug.GetMenuPosition().contains(mousePosition)) {
                    if (PlaySpace.debugOn == false) {
                        PlaySpace.debugOn = true;
                    }
                    else {
                        PlaySpace.debugOn = false;
                    }
                }
                if (PlaySpace.face.GetMenuPosition().contains(mousePosition)) {
                    if (PlaySpace.IsPlaying() == true && PlaySpace.defeat == false) {
                        PlaySpace.CreateBoard(columns, rows, mines);
                    }
                }
            }
            if (sf::Mouse::isButtonPressed(sf::Mouse::Left)) {
                if (PlaySpace.face.GetMenuPosition().contains(mousePosition)) {
                    if (PlaySpace.defeat == true) {
                        PlaySpace.CreateBoard(columns, rows, mines);
                    }
                    if (PlaySpace.win == true) {
                        PlaySpace.CreateBoard(columns, rows, mines);
                    }
                }
                if (PlaySpace.test1.GetMenuPosition().contains(mousePosition)) {
                    PlaySpace.LoadPresetBoards("boards/testboard1.brd");
                }
                if (PlaySpace.test2.GetMenuPosition().contains(mousePosition)) {
                    PlaySpace.LoadPresetBoards("boards/testboard2.brd");
                }
                if (PlaySpace.test3.GetMenuPosition().contains(mousePosition)) {
                    PlaySpace.LoadPresetBoards("boards/testboard3.brd");
                }
            }

            if (sf::Mouse::isButtonPressed(sf::Mouse::Right)) {
                for (int i = 0; i < columns; i++)
                {
                    for (int j = 0; j < rows; j++)
                    {
                        if (PlaySpace.CheckBounds(i, j).contains(mousePosition)) {
                            PlaySpace.wasRightClicked(i,j);
                            PlaySpace.FlagButton(window, i, j);
                        }
                    }
                }                
            }
        }
        PlaySpace.CheckForWin();
        window.clear(sf::Color::White);
        PlaySpace.RenderBoard(window);
        PlaySpace.DrawMenuButtons(window);
        window.display();
    }
    TextureManager::Clear();
    return 0;

    
}