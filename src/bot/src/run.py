import sys
import bot as bot
from threading import Thread

# n = int(sys.argv[1])
addr = '127.0.0.1'
# addr = str(sys.argv[1])

print("""
===================================================
PYTHON PROJECT GAME BOT GENERATOR
===================================================
HOW TO USE
---------------------------------------------------
First argument -> server local ip address\n
e.g. python3 master_bot.py 127.0.0.1
---------------------------------------------------
""")
x = Thread(target = bot.bot_function, args = [addr])
x.start()
x.join()
print("END BOT")