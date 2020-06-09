import sys
from os.path import dirname, join, abspath
sys.path.insert(0, abspath(join(dirname(__file__), '..')))
from src import bot

from time import sleep
from threading import Thread
from threading import get_ident

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
    # dist_to_blue_goal_area = 36
    
    team_color = my_player.team
    if team_color == "Red":
        sleep(15)
        my_player.discover()
        my_player.move(9, 9)
        my_player.discover()
        my_player.move(1, 1)
        my_player.discover()
        my_player.test()
        my_player.move(8, 8)
        my_player.discover()
        my_player.move(10, 10)
        my_player.pickup()
        my_player.test()
    
    my_player.finish()
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
