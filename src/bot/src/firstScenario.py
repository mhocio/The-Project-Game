import sys
import bot as bot
from threading import Thread
from threading import get_ident
from time import sleep

threads = list()
n = 1

def firstBot():
    print("I'm " + str(get_ident()))
    my_player = bot.Player()
    
    my_player.start()
    team_color = my_player.team
    if team_color == "RED":
        # sleep(10)
        my_player.move(10, 10)
        # sleep(3)
        my_player.pickup()
        my_player.move(4, 0)
        my_player.place()
        
        my_player.move(5, 15)
        my_player.pickup()
        my_player.move(8, 2)
        my_player.place()
    # else:
    #    my_player.move(4, 36)
    
    # my_player.finish()
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