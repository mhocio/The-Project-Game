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
    my_player.move_down()
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
            rv = self.recv()
            print("RECV: ", rv)
            if(len(rv) == 0):
                continue
            if(rv['action'] == "finish"):
                break
            elif rv['action'] == 'discover' and rv['status'] == "OK":
                for field in rv['fields']:
                    self.board.set_cell(field['x'], field['y'], field['cell']['distance'])
            elif rv['action'] == 'test' and rv['status'] == "OK":
                # TODO test piece status update
                pass

            self.writing = True

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
        message = {
            "action" : "start",
            "playerUuid": self.get_guid()
        }
        self.send(message)
        config = self.recv()
        print("CONFIG")
        print(config)

        if config["action"] == "startGame":
                # self.set_team(config["team"])
                # self.set_role(config["teamRole"])
                self.set_board(config["board"]["width"], config["board"]["taskAreaHeight"] + config["board"]["goalAreaHeight"], config["board"]["goalAreaHeight"])
                self.set_pos(int(config["position"]["x"]), int(config["position"]["y"]))
                print("POSITION: ", self.get_pos_x(), self.get_pos_y())
                start = self.recv()
                print("WAIT:", start)
                while start['action'] == "error":
                    print("WAIT:", start)
                    start = self.recv()
                if start['status'] == 'OK':
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