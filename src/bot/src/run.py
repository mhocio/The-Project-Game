import sys
import bot as bot
from threading import Thread

# addr = '20.39.48.176'
addr = '127.0.0.1'
player_num = int(sys.argv[1])

print("""
===================================================
PYTHON PROJECT GAME BOT
===================================================
HOW TO USE
---------------------------------------------------
First argument -> number of the bot in a team (starting from zero)\n
e.g. python3 master_bot.py 
---------------------------------------------------
""")
x = Thread(target = bot.bot_function, args = [addr, player_num])
x.start()
x.join()
print("END BOT")
