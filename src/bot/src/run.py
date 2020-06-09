import sys
import smartbot as bot
from threading import Thread

# threads = list()
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
'''
for index in range(n):
    x = Thread(target = bot.bot_function, args = [addr])
    threads.append(x)
    x.start()

for thread in threads:
    thread.join()
    print("END BOT") '''
    
# x = Thread(target = bot.bot_function, args = [addr, 8080])
x = Thread(target = bot.bot_function, args = [addr])
x.start()
x.join()
print("END BOT")