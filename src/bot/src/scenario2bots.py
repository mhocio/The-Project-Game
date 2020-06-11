import sys
import bot as bot
import bot as bot
from threading import Thread
from threading import get_ident
from time import sleep

threads = list()

def placePieceFrom(my_player, x_piece, y_piece, x_goal, y_goal):
    my_player.move(x_piece, y_piece)
    my_player.pickup()
    my_player.move(x_goal, y_goal)
    my_player.place()

def firstBot():
    print("I'm " + str(get_ident()))
    my_player = bot.Player()
    my_player.start()
    
    dist_to_blue_goal_area = 36
    # my_player.move(10, 10)

    team_color = my_player.team
    print(my_player.team == None)
    print("team color: " + str(my_player.team))
    if team_color == "Blue":
        placePieceFrom(my_player, 10, 10, 4, 0)
        placePieceFrom(my_player, 5, 15, 8, 2)
    elif team_color == "Red":
        placePieceFrom(my_player, 10, 15, 4, 0 + dist_to_blue_goal_area)
        placePieceFrom(my_player, 5, 20, 8, 2 + dist_to_blue_goal_area)
    
    my_player.close()
    print("END BOT FUNCTION")

n = 1
print("First scenario running two bots reaching for two pieces.")
for index in range(n):
    x = Thread(target = firstBot)
    threads.append(x)
    x.start()

for thread in threads:
    thread.join()
    print("END BOT THREAD!!!")