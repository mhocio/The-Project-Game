import socket
import json
from threading import get_ident
from enum import Enum
from threading import Thread
from time import sleep 

BUFFER_SIZE = 1024

def bot_function(addr):
    print("I'm " + str(get_ident()))
    my_player = Player(_host=addr)
    
    my_player.start()
    my_player.move_right()
    my_player.move_left()
    my_player.close()


class Role(Enum):
    LEADER: 1
    MEMBER: 2

class Team(Enum):
    RED: 3
    BLUE: 4

class Board:
    cells = []

    def __init__(self, x, y, h):
        self.cells = [[0 for col in range(x)] for row in range(y)]
        self.goal_area_height = h

    def set_cell(self, x, y, d):
        self.cells[x][y] = d
    
    def get_cell(self, x, y):
        return self.cells[x][y]


class Player:
    writing = True

    def __init__(self, _host = '127.0.0.1', _port = 8080):
        self.HOST = _host
        self.PORT = _port
        self.GUID = '0000'
        self.connect()
        
    def set_guid(self, guid):
        self.GUID = guid

    def bot_read(self):
        while(True):
            if self.writing == False:
                print("WRITING IN BOT "+str(self.writing))
                rv = self.recv()
                print("RECV: ", rv)
                if(rv['action'] == "finish"):
                    print("BOT_READ finish")
                    break
                elif rv['action'] == 'start' and rv['status'] == "OK":
                    print("BOT_READ start")
                    self.writing = False
                    pass
                # response for start message from host
                elif rv['action'] == 'discover' and rv['status'] == "OK":
                    print("BOT_READ discover")
                    for field in rv['fields']:
                        self.board.set_cell(field['x'], field['y'], field['cell']['distance'])
                elif rv['action'] == 'test' and rv['status'] == "OK":
                    # TODO test piece status update
                    pass
                elif rv['action'] == 'move':
                    print("BOT_READ move")
                    if rv['status'] == "OK":
                        print("BOT_READ position1 "+str(self.get_pos_x())+" "+str(self.get_pos_y()))
                        self.set_pos(rv['position']['x'], rv['position']['y'])
                        print("BOT_READ position2 "+str(self.get_pos_x())+" "+str(self.get_pos_y()))

                self.writing = True

    def set_host(self, host):
        self.host = host

    def get_host(self):
        return self.host

    def get_guid(self):
        return self.GUID

    def set_board(self, x, y, h):
        self.board = Board(x, y, h)
    
    def set_team(self, color):
        self.team = color

    def set_role(self, role):
        self.role = role

    def set_pos(self, pos_x, pos_y):
        self.pos_x = pos_x
        self.pos_y = pos_y

    def get_pos_x(self):
        return self.pos_x

    def get_pos_y(self):
        return self.pos_y

    def wait(self):
        while(self.writing == False):
            pass
        print("WRITING "+str(self.writing))


    def move_right(self):
        self.wait()
        MoveMessage={
            "action": "move",
            "playerUuid": self.get_guid(),
            "direction": "RIGHT",
            "position" : {
                "x" : self.get_pos_x(),
                "y" : self.get_pos_y()
            }
        }
        self.send(MoveMessage)
        self.writing = False
        print("WRITING "+str(self.writing))

    def move_left(self):
        self.wait()
        MoveMessage={
            "action": "move",
            "playerUuid": self.get_guid(),
            "direction": "LEFT",
            "position" : {
                "x" : self.get_pos_x(),
                "y" : self.get_pos_y()
            }
        }
        self.send(MoveMessage)
        self.writing = False
        print("WRITING "+str(self.writing))


    def move_up(self):
        self.wait()
        MoveMessage={
            "action": "move",
            "playerUuid": self.get_guid(),
            "direction": "UP",
            "position" : {
                "x" : self.get_pos_x(),
                "y" : self.get_pos_y()
            }
        }
        self.send(MoveMessage)
        self.writing = False
        print("WRITING "+str(self.writing))
        
    def move_down(self):
        self.wait()
        MoveMessage={
            "action": "move",
            "playerUuid": self.get_guid(),
            "direction": "DOWN",
            "position" : {
                "x" : self.get_pos_x(),
                "y" : self.get_pos_y()
            }
        }
        self.send(MoveMessage)
        self.writing = False
        print("WRITING "+str(self.writing))

    def pickup(self):
        self.wait()
        MoveMessage={
            "action": "pickUp",
            "playerUuid": self.get_guid(),
        }
        self.send(MoveMessage)
        self.writing = False
    
    def test(self):
        self.wait()
        MoveMessage={
            "action": "test",
            "playerUuid": self.get_guid(),
        }
        self.send(MoveMessage)
        self.writing = False

    def place(self):
        self.wait()
        MoveMessage={
            "action": "place",
            "playerUuid": self.get_guid(),
        }
        self.send(MoveMessage)
        self.writing = False

    def discover(self):
        self.wait()
        MoveMessage={
            "action": "discover",
            "playerUuid": self.get_guid(),
        }
        self.send(MoveMessage)
        self.writing = False

    def connect(self):
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.socket.connect((self.HOST, self.PORT))
    
    def close(self):
        self.socket.close()

    def send(self, message):
        self.socket.sendall(bytes(json.JSONEncoder().encode(message), "utf-8"))

    def recv(self):
        return json.loads(self.socket.recv(BUFFER_SIZE))

    def init_config(self):
        print(self.get_guid())
        print(self.get_host())
        
        if self.get_host() == True:
            message = {
                "action" : "start",
                "playerUuid": self.get_guid()
            }
            self.send(message)
            print("SEND")
            print(message)

        config = self.recv()
        print("CONFIG")
        print(config)

        # wait until all the players are ready
        while config["status"] == "DENIED":
            sleep(5)
            if self.get_host() == True:
                self.send(message)
                print("SEND")
                print(message)
            config = self.recv()
            print("CONFIG while")
            print(config)

        if config["action"] == "startGame":
                self.set_board(config["board"]["width"], config["board"]["taskAreaHeight"] + config["board"]["goalAreaHeight"], config["board"]["goalAreaHeight"])
                self.set_pos(int(config["position"]["x"]), int(config["position"]["y"]))

                if config['status'] == 'OK':
                    self.x = Thread(target = self.bot_read)
                    self.x.start()


    def start(self):
        message = {
            "action" : "connect",
        }

        self.send(message)
        print("sent")

        connected = self.recv()
        print("CONNECTED")
        print(connected)

        if "status" in connected and connected["status"] == "OK":
            self.set_guid(connected["playerUuid"])
            self.set_role(connected["teamRole"])
            self.set_team(connected["teamColor"])
            self.set_host(connected["host"])

            print(self.get_guid())
            message = {
                "action" : "ready",
                "playerUuid": self.get_guid(),
                "status" : "YES"
            }
            
            self.send(message)

            ready = self.recv()
            print("READY")
            print(ready)

            if "status" in ready and ready["status"] == "OK":
                self.init_config()