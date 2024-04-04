import tkinter as tk
import colors as c
import random
from copy import deepcopy

class Game(tk.Frame):

    def __init__(self):
        tk.Frame.__init__(self)
        self.grid()
        self.master.title("2048")
        self.main_grid = tk.Frame(
            self, bg=c.GRID_COLOR, bd=3, width=600, height=600
        )
        self.main_grid.grid(pady=(100,0))
        self.make_GUI()
        self.start_game()
        self.master.bind("<Left>", self.left)
        self.master.bind("<Right>", self.right)
        self.master.bind("<Up>", self.up)
        self.master.bind("<Down>", self.down)
        self.mainloop()

    def make_GUI(self):
        self.cells = []
        for i in range(4):
            row = []
            for j in range(4):
                cell_frame = tk.Frame(
                    self.main_grid,
                    bg=c.EMPTY_GRID_COLOR,
                    width=150,
                    height=150
                )
                cell_frame.grid(row=i, column=j, padx=5, pady=5)
                cell_number = tk.Label(self.main_grid, bg=c.EMPTY_GRID_COLOR)
                cell_number.grid(row=i, column=j)
                cell_data = {"frame" : cell_frame, "number": cell_number}
                row.append(cell_data)
            self.cells.append(row)  

    def start_game(self):
        self.matrix = [[0] * 4 for _ in range(4)]

        #fill 2 random ceels with 2s
        row = random.randint(0,3)
        col = random.randint(0,3)
        self.matrix[row][col] = 2
        self.cells[row][col]["frame"].configure(bg=c.CELL_COLORS[2])
        self.cells[row][col]["number"].configure(
            bg=c.CELL_COLORS[2],
            fg=c.CELL_NUMBER_COLORS[2],
            font=c.CELL_NUMBER_FONTS[2],
            text="2"
        )
        while(self.matrix[row][col] != 0):
            row = random.randint(0,3)
            col = random.randint(0,3)
        self.matrix[row][col] = 2
        self.cells[row][col]["frame"].configure(bg=c.CELL_COLORS[2])
        self.cells[row][col]["number"].configure(
            bg=c.CELL_COLORS[2],
            fg=c.CELL_NUMBER_COLORS[2],
            font=c.CELL_NUMBER_FONTS[2],
            text="2"
        )            
        self.score = 0       

    #Matrix Manipulation Functions
    def stack(self):
        new_matrix = [[0] * 4 for _ in range(4)]
        for i in range(4):
            fill_position = 0
            for j in range(4):
                if self.matrix[i][j] != 0:
                    new_matrix[i][fill_position] = self.matrix[i][j]
                    fill_position += 1
        self.matrix = new_matrix
    
    def combine(self):
        for i in range(4):
            for j in range(3):
                if self.matrix[i][j] != 0 and self.matrix[i][j] == self.matrix[i][j + 1]:
                    self.matrix[i][j] *= 2
                    self.matrix[i][j + 1] = 0
                    self.score += self.matrix[i][j]

    def reverse(self):
        new_matrix = []
        for i in range(4):
            new_matrix.append([])
            for j in range(4):
                new_matrix[i].append(self.matrix[i][3 - j])
        self.matrix = new_matrix

    def transpose(self):
        new_matrix = [[0] * 4 for _ in range(4)]
        for i in range(4):
            for j in range(4):
                new_matrix[i][j] = self.matrix[j][i]
        self.matrix = new_matrix

    #add a new 2 or 4 tile randomly to an empty cel
    def add_new_tile(self):
        row = random.randint(0,3)
        col = random.randint(0,3)
        while(self.matrix[row][col] != 0):
            row = random.randint(0,3)
            col = random.randint(0,3)
        temp = random.choices(population=(2,4), weights=[9,1])
        self.matrix[row][col] = temp[0]

    #update the GUI to match the matrix
    def update_GUI(self):
        for i in range(4):
            for j in range(4):
                cell_value = self.matrix[i][j]
                if cell_value == 0:
                    self.cells[i][j]["frame"].configure(bg=c.EMPTY_GRID_COLOR)
                    self.cells[i][j]["number"].configure(bg=c.EMPTY_GRID_COLOR, text="")
                else:
                    self.cells[i][j]["frame"].configure(bg=c.CELL_COLORS[cell_value])
                    self.cells[i][j]["number"].configure(
                        bg=c.CELL_COLORS[cell_value],
                        fg=c.CELL_NUMBER_COLORS[cell_value],
                        font=c.CELL_NUMBER_FONTS[cell_value],
                        text=str(cell_value)
                        )
        self.score_label.configure(text=self.score)
        self.update_idletasks()
    
    #arrow functions
    def left(self, event):
        copy_matrix = deepcopy(self.matrix)
        self.stack()
        self.combine()
        self.stack()
        if copy_matrix == self.matrix:
            self.matrix = copy_matrix
            return
        self.add_new_tile()
        self.update_GUI()
        self.game_over()

    def right(self, event):
        copy_matrix = deepcopy(self.matrix)
        self.reverse()
        self.stack()
        self.combine()
        self.stack()
        self.reverse()
        if copy_matrix == self.matrix:
            self.matrix = copy_matrix
            return
        self.add_new_tile()
        self.update_GUI()
        self.game_over()

    def up(self, event):
        copy_matrix = deepcopy(self.matrix)
        self.transpose()
        self.stack()
        self.combine()
        self.stack()
        self.transpose()
        if copy_matrix == self.matrix:
            self.matrix = copy_matrix
            return
        self.add_new_tile()
        self.update_GUI()
        self.game_over()

    def down(self, event):
        copy_matrix = deepcopy(self.matrix)
        self.transpose()
        self.reverse()
        self.stack()
        self.combine()
        self.stack()
        self.reverse()
        self.transpose()
        if copy_matrix == self.matrix:
            self.matrix = copy_matrix
            return
        self.add_new_tile()
        self.update_GUI()
        self.game_over()

    #checking for possible game moves left
    def horizontal_move_exists(self):
        for i in range(4):
            for j in range(3):
                if self.matrix[i][j] == self.matrix[i][j + 1]:
                    return True
        return False
    
    def vertical_move_exists(self):
        for i in range(3):
            for j in range(4):
                if self.matrix[i][j] == self.matrix[i + 1][j]:
                    return True
        return False
    
    def retrieve_score(self):
        return self.score

    #game over condtion
    def game_over(self):
        if any(2048 in row for row in self.matrix):
            game_over_frame = tk.Frame(self.main_grid, borderwidth=2)
            game_over_frame.place(relx=0.5, rely=0.5, anchor="center")
            tk.Label(
                game_over_frame,
                text="You win!",
                bg=c.WINNER_BG,
                fg=c.GAME_OVER_FONT_COLOR,
                font=c.GAME_OVER_FONT
            ).pack()
            play_again = tk.Button(game_over_frame, text="Play Again", command=self.reset_matrix).pack()
        elif not any(0 in row for row in self.matrix) and not self.horizontal_move_exists() and not self.vertical_move_exists():
            game_over_frame = tk.Frame(self.main_grid, borderwidth=2)
            game_over_frame.place(relx=0.5, rely=0.5, anchor="center")
            tk.Label(
                game_over_frame,
                text="You lose!",
                bg=c.LOSER_BG,
                fg=c.GAME_OVER_FONT_COLOR,
                font=c.GAME_OVER_FONT
            ).pack()
            play_again = tk.Button(game_over_frame, text="Play Again", command=self.reset_matrix).pack()


    #resets the internal matrix to 0s
    def reset_matrix(self):
        Game.destroy(self)
        Game()



def main():
    Game()

if __name__ == "__main__":
    main()
