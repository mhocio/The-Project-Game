import sys
# this mess is needed until we have no package
from os.path import dirname, join, abspath
sys.path.insert(0, abspath(join(dirname(__file__), '..')))
from src import smartbot

from time import sleep
from threading import Thread
from threading import get_ident

threads = list()
n = 2

def firstBot():
    print("I'm " + str(get_ident()))
    my_player = smartbot.Player()
   
    my_player.start()
    team_color = my_player.team
    if team_color == "RED":
        my_player.move(10, 10)
        my_player.pickup()
        my_player.move(4, 0)
        my_player.place()
        
        my_player.move(5, 15)
        my_player.pickup()
        my_player.move(8, 2)
        my_player.place()

    my_player.close()
    print("END BOT FUNCTION")

print("First scenario running two bots reaching for two pieces.")
for index in range(n):
    x = Thread(target = firstBot)
    threads.append(x)
    x.start()

for thread in threads:
    thread.join()
    print("END BOT")